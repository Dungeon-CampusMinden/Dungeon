package rooms.lasthour.level;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import engine.Entity;
import engine.Game;
import engine.components.PlayerComponent;
import engine.components.PositionComponent;
import engine.game.ECSManagement;
import engine.game.ServerProcess;
import engine.level.elements.tile.DoorTile;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.network.handler.INetworkHandler;
import engine.network.server.ClientState;
import engine.utils.EntityIdProvider;
import engine.utils.EntitySystemMapper;
import engine.utils.Point;
import escaperoom.foundation.ui.BlackFadeCutscene;
import feature.components.InventoryComponent;
import feature.entities.CharacterClass;
import feature.interaction.keypad.KeypadComponent;
import feature.petrinet.PetriNetSystem;
import feature.timer.WorldTimerComponent;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import rooms.lasthour.modules.computer.ComputerProgress;
import rooms.lasthour.modules.computer.ComputerStateComponent;
import rooms.lasthour.modules.usbstick.UsbStickItem;
import rooms.lasthour.petrinet.LastHourMilestone;
import rooms.lasthour.petrinet.LastHourProgressNet;
import rooms.lasthour.save.LastHourLoad;
import rooms.lasthour.save.LastHourSave;
import testingUtils.MockNetworkHandler;

/** Regression coverage for player inventory ownership across multiplayer checkpoints. */
class LastHourPlayerCheckpointTest {
  @TempDir Path tempDir;

  private LastHourLevel level;
  private LastHourLevel previousLevel;
  private INetworkHandler previousNetwork;

  @BeforeEach
  void setUp() throws ReflectiveOperationException {
    Field network = Game.class.getDeclaredField("networkHandler");
    network.setAccessible(true);
    previousNetwork = (INetworkHandler) network.get(null);
    MockNetworkHandler.useLocalNetworkHandler();
    LastHourProgressNet.reset();
    Game.removeAllEntities();
    Game.removeAllSystems();
    Game.add(new PetriNetSystem());
    LastHourProgressNet.initialize();
    UsbStickItem.ensureRegistration();

    previousLevel = LastHourLevel.getInstance();
    level =
        new LastHourLevel(
            new LevelElement[][] {{LevelElement.FLOOR}}, DesignLabel.DEFAULT, Map.of());
    level.playerStates = new LastHourPlayerStateSystem();
    Game.add(level.playerStates);
    LastHourLevel.INTRO_SHOWN_TO.clear();
    level.storageDoor = mock(DoorTile.class);
    level.keypad = new Entity("checkpoint-test-keypad");
    level.keypad.add(new KeypadComponent(List.of(1, 2, 3), () -> {}, true));
    level.worldTimer = new Entity("checkpoint-test-timer");
    level.worldTimer.add(new WorldTimerComponent(Game.playClock().activeMs(), 3600));

    Entity computer = new Entity("checkpoint-test-computer");
    computer.add(ComputerStateComponent.of(ComputerProgress.ON, false, null, 0));
    Game.add(computer);
  }

  @AfterEach
  void tearDown() throws ReflectiveOperationException {
    LastHourProgressNet.reset();
    Game.removeAllEntities();
    Game.removeAllSystems();
    LastHourLevel.INTRO_SHOWN_TO.clear();
    Field instance = LastHourLevel.class.getDeclaredField("Instance");
    instance.setAccessible(true);
    instance.set(null, previousLevel);
    MockNetworkHandler.useNetworkHandler(previousNetwork);
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void checkpointKeepsOwnerNameAfterDisconnect(boolean allDisconnected) throws Exception {
    Entity first = player("A", new Point(1, 2));
    Entity second = player("B", new Point(3, 4));
    assertTrue(LastHourProgressNet.complete(LastHourMilestone.POWER_ON));
    Path saveFile = tempDir.resolve("checkpoint.json");
    LastHourSave.SaveData previous = writeCheckpoint(saveFile);
    String owner = previous.playerName();

    Game.remove(owner.equals("A") ? first : second);
    if (allDisconnected) Game.remove(owner.equals("A") ? second : first);
    completeLogin();
    LastHourSave.SaveData next = writeCheckpoint(saveFile);

    assertEquals(owner, next.playerName());
    assertEquals(owner, LastHourLoad.read(saveFile).orElseThrow().playerName());
  }

  @Test
  void firstCheckpointUsesManagedHostNameInsteadOfFirstPlayer() throws Exception {
    String previousHost = System.getProperty(ServerProcess.HOST_PLAYER_NAME_PROPERTY);
    try {
      System.setProperty(ServerProcess.HOST_PLAYER_NAME_PROPERTY, "Z-Host");
      player("A-Guest", new Point(1, 2));
      player("Z-Host", new Point(3, 4));
      assertTrue(LastHourProgressNet.complete(LastHourMilestone.POWER_ON));

      LastHourSave.SaveData saved = writeCheckpoint(tempDir.resolve("checkpoint.json"));

      assertEquals("Z-Host", saved.playerName());
    } finally {
      if (previousHost == null) System.clearProperty(ServerProcess.HOST_PLAYER_NAME_PROPERTY);
      else System.setProperty(ServerProcess.HOST_PLAYER_NAME_PROPERTY, previousHost);
    }
  }

  @Test
  void checkpointCapturesLatestPlayerBeforeDisconnectCallbackIsQueued() throws Exception {
    Entity sender = player("A", new Point(1, 2));
    player("B", new Point(3, 4));
    assertTrue(LastHourProgressNet.complete(LastHourMilestone.POWER_ON));
    Path saveFile = tempDir.resolve("checkpoint.json");
    writeCheckpoint(saveFile);
    int revision = LastHourLevel.saveRevision();
    assertTrue(inventory(sender).add(new UsbStickItem.RedUsbStick()));
    sender.fetch(PositionComponent.class).orElseThrow().position(new Point(9, 8));
    LastHourLevel.INTRO_SHOWN_TO.add(sender.id());

    Set<EntitySystemMapper> filters = beginEntityRemoval(sender);
    try {
      completeLogin();
      level.persistAtPetriMilestone(saveFile);
      assertEquals(revision + 1, LastHourLevel.saveRevision());
      LastHourSave.PlayerData saved = savedPlayer(LastHourLoad.read(saveFile).orElseThrow(), "A");
      assertEquals(9, saved.x());
      assertEquals(8, saved.y());
      assertTrue(saved.introShown());
      assertEquals("RedUsbStick", saved.items().getFirst().state().itemType());
    } finally {
      finishEntityRemoval(sender, filters);
    }

    level.playerStates.execute();
    level.persistAtPetriMilestone(saveFile);
    assertEquals(revision + 1, LastHourLevel.saveRevision());
    assertEquals(level.lastSaved, LastHourLoad.read(saveFile).orElseThrow());
  }

  @Test
  void transferredUsbIsNotDuplicatedBeforeDisconnectCallbackIsQueued() throws Exception {
    Entity sender = player("A", new Point(1, 2));
    Entity recipient = player("B", new Point(3, 4));
    UsbStickItem.BlueUsbStick stick = new UsbStickItem.BlueUsbStick();
    assertTrue(inventory(sender).add(stick));
    assertTrue(LastHourProgressNet.complete(LastHourMilestone.POWER_ON));
    Path saveFile = tempDir.resolve("checkpoint.json");
    writeCheckpoint(saveFile);
    int revision = LastHourLevel.saveRevision();
    assertTrue(inventory(sender).transfer(stick, inventory(recipient)));

    Set<EntitySystemMapper> filters = beginEntityRemoval(sender);
    try {
      completeLogin();
      level.persistAtPetriMilestone(saveFile);

      assertEquals(revision + 1, LastHourLevel.saveRevision());
      LastHourSave.SaveData saved = LastHourLoad.read(saveFile).orElseThrow();
      assertEquals(List.of("B"), usbOwners(saved));
      assertTrue(savedPlayer(saved, "A").items().isEmpty());
    } finally {
      finishEntityRemoval(sender, filters);
    }
  }

  @Test
  void introWaitsForConnectedPlayersRestoreWithoutBlockingOtherPlayers() throws Exception {
    level.playerStates.restore(List.of(new LastHourSave.PlayerData("A", 5, 6, true, List.of())));
    Entity reconnected = new Entity("A");
    reconnected.add(new PlayerComponent(false, "A"));
    reconnected.add(new PositionComponent(new Point(0, 0)));
    Game.add(reconnected);
    Entity otherPlayer = player("B", new Point(3, 4));
    level.playerStates.execute();

    assertTrue(level.playerStates.isUpdated());
    assertFalse(level.playerStates.isReadyForIntro(reconnected));
    assertTrue(level.playerStates.isReadyForIntro(otherPlayer));
    Game.remove(otherPlayer);
    level.playerStates.execute();
    var showIntros = LastHourLevel.class.getDeclaredMethod("showIntroForNewPlayers");
    showIntros.setAccessible(true);
    try (var cutscene = mockStatic(BlackFadeCutscene.class)) {
      showIntros.invoke(level);
      cutscene.verifyNoInteractions();
      reconnected.add(new InventoryComponent(2));
      level.playerStates.execute();
      assertTrue(level.playerStates.isReadyForIntro(reconnected));
      assertTrue(LastHourLevel.INTRO_SHOWN_TO.contains(reconnected.id()));
      showIntros.invoke(level);
      cutscene.verifyNoInteractions();
    }
  }

  @Test
  void sameEntityReconnectBeforeInitialRestorePreservesSavedInventory() throws Exception {
    UsbStickItem.BlueUsbStick stick = new UsbStickItem.BlueUsbStick();
    level.playerStates.restore(
        List.of(
            new LastHourSave.PlayerData(
                "A",
                5,
                6,
                true,
                List.of(
                    new LastHourSave.ItemData(
                        0, engine.network.messages.s2c.ItemState.fromItem(stick))))));
    Entity reconnected = new Entity("A");
    reconnected.add(new PlayerComponent(false, "A"));
    reconnected.add(new PositionComponent(new Point(0, 0)));
    Game.add(reconnected);
    level.playerStates.execute();
    Game.remove(reconnected);
    level.playerStates.execute();
    reconnected.add(new InventoryComponent(2));
    Game.add(reconnected);

    level.playerStates.execute();

    assertTrue(inventory(reconnected).get(0).orElseThrow() instanceof UsbStickItem.BlueUsbStick);
    assertEquals(
        new Point(5, 6), reconnected.fetch(PositionComponent.class).orElseThrow().position());
    assertTrue(LastHourLevel.INTRO_SHOWN_TO.contains(reconnected.id()));
  }

  @Test
  void transferredUsbIsSavedOnlyForRecipientWhileSenderRemainsConnected() throws Exception {
    assertTransferredUsbOwnershipAtNextCheckpoint(false);
  }

  @Test
  void transferredUsbIsSavedOnlyForRecipientAfterSenderDisconnects() throws Exception {
    assertTransferredUsbOwnershipAtNextCheckpoint(true);
  }

  @Test
  void disconnectPreservesLatestPositionInventoryAndIntroWithoutWritingToDisk() throws Exception {
    Entity player = player("A", new Point(1, 2));
    Path saveFile = tempDir.resolve("checkpoint.json");
    assertTrue(LastHourProgressNet.complete(LastHourMilestone.POWER_ON));
    LastHourSave.SaveData previous = writeCheckpoint(saveFile);
    String previousJson = Files.readString(saveFile);
    int revision = LastHourLevel.saveRevision();

    assertTrue(inventory(player).add(new UsbStickItem.BlueUsbStick()));
    player.fetch(PositionComponent.class).orElseThrow().position(new Point(5, 6));
    LastHourLevel.INTRO_SHOWN_TO.add(player.id());
    Game.remove(player);
    level.playerStates.execute();
    level.persistAtPetriMilestone(saveFile);

    assertSame(previous, level.lastSaved);
    assertEquals(previousJson, Files.readString(saveFile));
    assertEquals(revision, LastHourLevel.saveRevision());

    completeLogin();
    LastHourSave.SaveData next = writeCheckpoint(saveFile);
    assertEquals(List.of("A"), usbOwners(next));
    assertEquals(5, savedPlayer(next, "A").x());
    assertEquals(6, savedPlayer(next, "A").y());
    assertTrue(savedPlayer(next, "A").introShown());
  }

  @Test
  void reconnectBeforeNextTickRestoresLatestStateOnceByPlayerName() throws Exception {
    Entity original = player("A", new Point(1, 2));
    assertTrue(LastHourProgressNet.complete(LastHourMilestone.POWER_ON));
    writeCheckpoint(tempDir.resolve("checkpoint.json"));
    assertTrue(inventory(original).add(new UsbStickItem.BlueUsbStick()));
    original.fetch(PositionComponent.class).orElseThrow().position(new Point(5, 6));
    LastHourLevel.INTRO_SHOWN_TO.add(original.id());

    Game.remove(original);
    Entity reconnected = player("A", new Point(0, 0));
    level.playerStates.execute();

    assertEquals(
        new Point(5, 6), reconnected.fetch(PositionComponent.class).orElseThrow().position());
    assertTrue(inventory(reconnected).items()[0] instanceof UsbStickItem.BlueUsbStick);
    assertTrue(LastHourLevel.INTRO_SHOWN_TO.contains(reconnected.id()));
    inventory(reconnected).set(0, null);
    reconnected.fetch(PositionComponent.class).orElseThrow().position(new Point(7, 8));
    level.playerStates.execute();
    assertEquals(
        new Point(7, 8), reconnected.fetch(PositionComponent.class).orElseThrow().position());
    assertTrue(level.playerStates.capture().getFirst().items().isEmpty());
  }

  @Test
  void reconnectAfterTransferDoesNotRestoreStaleUsb() throws Exception {
    Entity sender = player("A", new Point(1, 2));
    Entity recipient = player("B", new Point(3, 4));
    UsbStickItem.BlueUsbStick usb = new UsbStickItem.BlueUsbStick();
    assertTrue(inventory(sender).add(usb));
    assertTrue(LastHourProgressNet.complete(LastHourMilestone.POWER_ON));
    Path saveFile = tempDir.resolve("checkpoint.json");
    writeCheckpoint(saveFile);

    assertTrue(inventory(sender).transfer(usb, inventory(recipient)));
    Game.remove(sender);
    Entity reconnected = player("A", new Point(0, 0));
    level.playerStates.execute();
    assertFalse(inventory(reconnected).items()[0] instanceof UsbStickItem.BlueUsbStick);

    completeLogin();
    LastHourSave.SaveData next = writeCheckpoint(saveFile);
    assertEquals(List.of("B"), usbOwners(next));
    assertTrue(savedPlayer(next, "A").items().isEmpty());
  }

  @Test
  void checkpointPreservesFinalInventoriesWhenAllPlayersDisconnect() throws Exception {
    Entity sender = player("A", new Point(1, 2));
    Entity recipient = player("B", new Point(3, 4));
    UsbStickItem.BlueUsbStick usb = new UsbStickItem.BlueUsbStick();
    assertTrue(inventory(sender).add(usb));
    assertTrue(LastHourProgressNet.complete(LastHourMilestone.POWER_ON));
    Path saveFile = tempDir.resolve("checkpoint.json");
    writeCheckpoint(saveFile);

    assertTrue(inventory(sender).transfer(usb, inventory(recipient)));
    Game.remove(sender);
    Game.remove(recipient);
    completeLogin();

    LastHourSave.SaveData next = writeCheckpoint(saveFile);
    assertEquals(2, next.players().size());
    assertEquals(List.of("B"), usbOwners(next));
    assertTrue(savedPlayer(next, "A").items().isEmpty());
  }

  @Test
  void insertedUsbIsNotResurrectedFromDisconnectedSenderCheckpoint() throws Exception {
    Entity sender = player("A", new Point(1, 2));
    Entity recipient = player("B", new Point(3, 4));
    UsbStickItem.BlueUsbStick usb = new UsbStickItem.BlueUsbStick();
    assertTrue(inventory(sender).add(usb));
    assertTrue(LastHourProgressNet.complete(LastHourMilestone.POWER_ON));
    Path saveFile = tempDir.resolve("checkpoint.json");
    writeCheckpoint(saveFile);

    assertTrue(inventory(sender).transfer(usb, inventory(recipient)));
    Game.remove(sender);
    assertTrue(inventory(recipient).remove(usb).isPresent());
    completeLogin();
    assertTrue(LastHourProgressNet.complete(LastHourMilestone.STORAGE_OPENED));
    ComputerStateComponent.setUsbInserted(true);
    assertTrue(LastHourProgressNet.complete(LastHourMilestone.BLUE_USB_INSERTED));

    LastHourSave.SaveData next = writeCheckpoint(saveFile);
    assertTrue(usbOwners(next).isEmpty());
    assertTrue(next.computer().usbInserted());
    Entity reconnected = player("A", new Point(0, 0));
    level.playerStates.execute();
    assertFalse(inventory(reconnected).items()[0] instanceof UsbStickItem.BlueUsbStick);
  }

  @Test
  void loadedOfflinePlayerIsPreservedUntilInventoryAndPositionAreReady() throws Exception {
    Entity original = player("A", new Point(5, 6));
    assertTrue(inventory(original).add(new UsbStickItem.BlueUsbStick()));
    LastHourLevel.INTRO_SHOWN_TO.add(original.id());
    assertTrue(LastHourProgressNet.complete(LastHourMilestone.POWER_ON));
    Path saveFile = tempDir.resolve("checkpoint.json");
    LastHourSave.SaveData saved = writeCheckpoint(saveFile);
    Game.remove(original);

    level.playerStates = new LastHourPlayerStateSystem();
    level.playerStates.restore(saved.players());
    Game.add(level.playerStates);
    player("B", new Point(3, 4));
    completeLogin();
    LastHourSave.SaveData next = writeCheckpoint(saveFile);
    assertEquals(savedPlayer(saved, "A"), savedPlayer(next, "A"));

    Entity reconnected = new Entity("reconnected-A");
    reconnected.add(new PlayerComponent(false, "A"));
    Game.add(reconnected);
    level.playerStates.execute();
    assertEquals(savedPlayer(saved, "A"), level.playerStates.capture().getFirst());
    reconnected.add(new InventoryComponent(2));
    level.playerStates.execute();
    assertTrue(inventory(reconnected).items()[0] == null);
    reconnected.add(new PositionComponent(new Point(0, 0)));
    level.playerStates.execute();
    assertTrue(inventory(reconnected).items()[0] instanceof UsbStickItem.BlueUsbStick);
    assertEquals(
        new Point(5, 6), reconnected.fetch(PositionComponent.class).orElseThrow().position());
    assertTrue(LastHourLevel.INTRO_SHOWN_TO.contains(reconnected.id()));
  }

  @Test
  void removingAndReaddingSystemDoesNotRestoreOldConnectedPlayerInventory() throws Exception {
    Entity player = player("A", new Point(1, 2));
    assertTrue(inventory(player).add(new UsbStickItem.BlueUsbStick()));
    assertTrue(LastHourProgressNet.complete(LastHourMilestone.POWER_ON));
    writeCheckpoint(tempDir.resolve("checkpoint.json"));

    Game.remove(LastHourPlayerStateSystem.class);
    inventory(player).set(0, null);
    Game.add(level.playerStates);
    level.playerStates.execute();

    assertTrue(level.playerStates.capture().getFirst().items().isEmpty());
  }

  @Test
  void invalidSnapshotKeepsPreviousSaveAndRevisionAndCanBeRetried() throws Exception {
    Entity sender = player("A", new Point(1, 2));
    assertTrue(inventory(sender).add(new UsbStickItem.BlueUsbStick()));
    assertTrue(LastHourProgressNet.complete(LastHourMilestone.POWER_ON));
    Path saveFile = tempDir.resolve("checkpoint.json");
    LastHourSave.SaveData previous = writeCheckpoint(saveFile);
    String previousJson = Files.readString(saveFile);
    int revision = LastHourLevel.saveRevision();

    Entity recipient = player("B", new Point(3, 4));
    assertTrue(inventory(recipient).add(new UsbStickItem.BlueUsbStick()));
    completeLogin();
    level.playerStates.execute();
    assertDoesNotThrow(() -> level.persistAtPetriMilestone(saveFile));

    assertSame(previous, level.lastSaved);
    assertEquals(revision, LastHourLevel.saveRevision());
    assertEquals(previousJson, Files.readString(saveFile));
    inventory(recipient).set(0, null);
    LastHourSave.SaveData next = writeCheckpoint(saveFile);
    assertEquals(List.of("A"), usbOwners(next));
    assertEquals(revision + 1, LastHourLevel.saveRevision());
  }

  @Test
  void writeFailureKeepsPreviousSaveAndRevisionAndCanBeRetried() throws Exception {
    player("A", new Point(1, 2));
    assertTrue(LastHourProgressNet.complete(LastHourMilestone.POWER_ON));
    Path saveFile = tempDir.resolve("checkpoint.json");
    LastHourSave.SaveData previous = writeCheckpoint(saveFile);
    String previousJson = Files.readString(saveFile);
    int revision = LastHourLevel.saveRevision();

    completeLogin();
    Path blockedPath = Files.createDirectory(tempDir.resolve("blocked-save.json"));
    assertDoesNotThrow(() -> level.persistAtPetriMilestone(blockedPath));

    assertSame(previous, level.lastSaved);
    assertEquals(revision, LastHourLevel.saveRevision());
    assertEquals(previousJson, Files.readString(saveFile));
    assertTrue(Files.isDirectory(blockedPath));
    writeCheckpoint(saveFile);
    assertEquals(revision + 1, LastHourLevel.saveRevision());
  }

  @Test
  void failedDisconnectSnapshotIsRetriedWithoutReplacingPreviousSave() throws Exception {
    Entity sender = player("A", new Point(1, 2));
    Entity recipient = player("B", new Point(3, 4));
    UsbStickItem.BlueUsbStick blue = new UsbStickItem.BlueUsbStick();
    assertTrue(inventory(sender).add(blue));
    assertTrue(LastHourProgressNet.complete(LastHourMilestone.POWER_ON));
    Path saveFile = tempDir.resolve("checkpoint.json");
    LastHourSave.SaveData previous = writeCheckpoint(saveFile);
    String previousJson = Files.readString(saveFile);
    int revision = LastHourLevel.saveRevision();

    assertTrue(inventory(sender).transfer(blue, inventory(recipient)));
    UsbStickItem.RedUsbStick red = new UsbStickItem.RedUsbStick();
    assertTrue(inventory(sender).add(red));
    red.stackSize(0);
    Game.remove(sender);
    completeLogin();

    assertDoesNotThrow(() -> level.playerStates.execute());
    assertFalse(level.playerStates.isUpdated());
    assertThrows(IllegalStateException.class, () -> level.playerStates.capture());
    assertDoesNotThrow(() -> level.playerStates.execute());
    assertFalse(level.playerStates.isUpdated());
    assertDoesNotThrow(() -> level.persistAtPetriMilestone(saveFile));
    assertSame(previous, level.lastSaved);
    assertEquals(revision, LastHourLevel.saveRevision());
    assertEquals(previousJson, Files.readString(saveFile));

    red.stackSize(1);
    level.playerStates.execute();
    assertTrue(level.playerStates.isUpdated());
    LastHourSave.SaveData next = writeCheckpoint(saveFile);
    assertEquals(List.of("B"), usbOwners(next));
    assertEquals("RedUsbStick", savedPlayer(next, "A").items().getFirst().state().itemType());
    assertEquals(revision + 1, LastHourLevel.saveRevision());
  }

  @Test
  void ecsTicksRestorePlayersBeforePendingCheckpointIsWritten() throws Exception {
    Entity original = player("A", new Point(1, 2));
    assertTrue(inventory(original).add(new UsbStickItem.BlueUsbStick()));
    LastHourLevel.INTRO_SHOWN_TO.add(original.id());
    assertTrue(LastHourProgressNet.complete(LastHourMilestone.POWER_ON));
    Path saveFile = tempDir.resolve("checkpoint.json");
    LastHourSave.SaveData previous = writeCheckpoint(saveFile);
    int revision = LastHourLevel.saveRevision();

    Game.remove(original);
    Entity replacement = player("A", new Point(0, 0));
    completeLogin();
    level.persistAtPetriMilestone(saveFile);
    assertSame(previous, level.lastSaved);
    assertEquals(revision, LastHourLevel.saveRevision());
    assertEquals(previous, LastHourLoad.read(saveFile).orElseThrow());

    ECSManagement.executeOneTick(engine.System.AuthoritativeSide.SERVER);

    assertTrue(level.playerStates.isUpdated());
    assertTrue(inventory(replacement).get(0).orElseThrow() instanceof UsbStickItem.BlueUsbStick);
    assertEquals(
        new Point(1, 2), replacement.fetch(PositionComponent.class).orElseThrow().position());
    assertTrue(LastHourLevel.INTRO_SHOWN_TO.contains(replacement.id()));
    level.persistAtPetriMilestone(saveFile);
    assertEquals(revision + 1, LastHourLevel.saveRevision());
    assertEquals(List.of("A"), usbOwners(LastHourLoad.read(saveFile).orElseThrow()));

    inventory(replacement).set(0, null);
    replacement.fetch(PositionComponent.class).orElseThrow().position(new Point(5, 6));
    Game.remove(replacement);
    Entity reconnected = player("A", new Point(0, 0));

    ECSManagement.executeOneTick(engine.System.AuthoritativeSide.SERVER);

    assertTrue(inventory(reconnected).get(0).isEmpty());
    assertEquals(
        new Point(5, 6), reconnected.fetch(PositionComponent.class).orElseThrow().position());
    assertTrue(LastHourLevel.INTRO_SHOWN_TO.contains(reconnected.id()));
  }

  @Test
  void captureDoesNotProcessQueuedRestores() throws Exception {
    Entity original = player("A", new Point(1, 2));
    assertTrue(inventory(original).add(new UsbStickItem.BlueUsbStick()));
    assertTrue(LastHourProgressNet.complete(LastHourMilestone.POWER_ON));
    writeCheckpoint(tempDir.resolve("checkpoint.json"));
    Game.remove(original);
    Entity replacement = player("A", new Point(0, 0));

    assertTrue(inventory(replacement).get(0).isEmpty());
    assertThrows(IllegalStateException.class, () -> level.playerStates.capture());
    assertEquals(
        new Point(0, 0), replacement.fetch(PositionComponent.class).orElseThrow().position());
    assertTrue(inventory(replacement).get(0).isEmpty());

    level.playerStates.execute();
    assertTrue(inventory(replacement).get(0).orElseThrow() instanceof UsbStickItem.BlueUsbStick);
    assertEquals(
        "BlueUsbStick",
        level.playerStates.capture().getFirst().items().getFirst().state().itemType());
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void sessionReconnectRetainsSameEntityWithoutReaddingItsItems(boolean tickWhileDisconnected)
      throws Exception {
    Entity original = player("A", new Point(1, 2));
    UsbStickItem.BlueUsbStick blue = new UsbStickItem.BlueUsbStick();
    assertTrue(inventory(original).add(blue));
    assertTrue(LastHourProgressNet.complete(LastHourMilestone.POWER_ON));
    Path saveFile = tempDir.resolve("checkpoint.json");
    writeCheckpoint(saveFile);
    UsbStickItem.RedUsbStick red = new UsbStickItem.RedUsbStick();
    assertTrue(inventory(original).add(red));
    original.fetch(PositionComponent.class).orElseThrow().position(new Point(5, 6));
    LastHourLevel.INTRO_SHOWN_TO.add(original.id());
    AtomicInteger itemAddedCalls = new AtomicInteger();
    inventory(original).onItemAdded(item -> itemAddedCalls.incrementAndGet());
    ClientState client =
        new ClientState((short) 1, "A", 1, new byte[] {1, 2, 3}, CharacterClass.WIZARD);
    client.playerEntity(original);

    Game.remove(original);
    if (tickWhileDisconnected) level.playerStates.execute();
    client.resetForReconnect(2, new byte[] {4, 5, 6}, true);
    level.playerStates.execute();

    assertSame(original, client.playerEntity().orElseThrow());
    assertSame(original, Game.findEntityById(original.id()).orElseThrow());
    assertSame(blue, inventory(original).get(0).orElseThrow());
    assertSame(red, inventory(original).get(1).orElseThrow());
    assertEquals(0, itemAddedCalls.get(), "A retained inventory must not replay item-added hooks.");
    assertEquals(new Point(5, 6), original.fetch(PositionComponent.class).orElseThrow().position());
    assertTrue(LastHourLevel.INTRO_SHOWN_TO.contains(original.id()));

    assertTrue(inventory(original).remove(blue).isPresent());
    original.fetch(PositionComponent.class).orElseThrow().position(new Point(7, 8));
    completeLogin();
    LastHourSave.SaveData next = writeCheckpoint(saveFile);
    assertTrue(usbOwners(next).isEmpty());
    assertEquals(7, savedPlayer(next, "A").x());
    assertEquals(8, savedPlayer(next, "A").y());
    assertEquals(0, itemAddedCalls.get());
  }

  private void assertTransferredUsbOwnershipAtNextCheckpoint(boolean disconnectSender)
      throws Exception {
    Entity sender = player("A", new Point(1, 2));
    Entity recipient = player("B", new Point(3, 4));
    InventoryComponent senderInventory = sender.fetch(InventoryComponent.class).orElseThrow();
    InventoryComponent recipientInventory = recipient.fetch(InventoryComponent.class).orElseThrow();
    UsbStickItem.BlueUsbStick usb = new UsbStickItem.BlueUsbStick();
    assertTrue(senderInventory.add(usb));

    Path saveFile = tempDir.resolve("checkpoint.json");
    assertTrue(LastHourProgressNet.complete(LastHourMilestone.POWER_ON));
    LastHourSave.SaveData firstCheckpoint = writeCheckpoint(saveFile);
    assertEquals(List.of("A"), usbOwners(firstCheckpoint));

    assertTrue(senderInventory.transfer(usb, recipientInventory));
    assertFalse(senderInventory.hasItem(usb));
    assertTrue(recipientInventory.hasItem(usb));
    if (disconnectSender) CompletableFuture.runAsync(() -> Game.remove(sender)).join();

    completeLogin();

    LastHourSave.SaveData nextCheckpoint =
        assertDoesNotThrow(
            () -> writeCheckpoint(saveFile),
            "The next checkpoint must remain saveable after a USB transfer and disconnect.");

    assertTrue(nextCheckpoint.milestones().contains(LastHourMilestone.LOGIN_SUCCEEDED));
    assertEquals(List.of("A", "B"), nextCheckpoint.players().stream().map(p -> p.name()).toList());
    assertEquals(List.of("B"), usbOwners(nextCheckpoint));
    assertTrue(
        nextCheckpoint.players().stream()
            .filter(player -> player.name().equals("A"))
            .findFirst()
            .orElseThrow()
            .items()
            .isEmpty());
  }

  private Entity player(String name, Point position) {
    Entity player = new Entity(name);
    player.add(new PlayerComponent(false, name));
    player.add(new PositionComponent(position));
    player.add(new InventoryComponent(2));
    Game.add(player);
    return player;
  }

  @SuppressWarnings("unchecked")
  private Set<EntitySystemMapper> beginEntityRemoval(Entity player)
      throws ReflectiveOperationException {
    // Simulate the gap in ECSManagement.remove before the player mapper queues the disconnect.
    Field entitiesField = ECSManagement.class.getDeclaredField("allEntities");
    entitiesField.setAccessible(true);
    Map<Integer, Entity> entities = (Map<Integer, Entity>) entitiesField.get(null);
    assertTrue(entities.remove(player.id(), player));
    Field filtersField = ECSManagement.class.getDeclaredField("entityFilters");
    filtersField.setAccessible(true);
    Set<EntitySystemMapper> filters = (Set<EntitySystemMapper>) filtersField.get(null);
    filters.stream()
        .filter(mapper -> mapper.equals(Set.of()))
        .findFirst()
        .orElseThrow()
        .remove(player);
    assertTrue(Game.allPlayers().noneMatch(entity -> entity == player));
    return filters;
  }

  private void finishEntityRemoval(Entity player, Set<EntitySystemMapper> filters) {
    filters.forEach(mapper -> mapper.remove(player));
    EntityIdProvider.unregister(player.id());
  }

  private LastHourSave.SaveData writeCheckpoint(Path file) {
    int revision = LastHourLevel.saveRevision();
    level.playerStates.execute();
    level.persistAtPetriMilestone(file);
    assertEquals(
        revision + 1, LastHourLevel.saveRevision(), "Checkpoint must actually be written.");
    assertEquals(level.lastSaved, LastHourLoad.read(file).orElseThrow());
    return level.lastSaved;
  }

  private void completeLogin() {
    ComputerStateComponent.setState(ComputerProgress.LOGGED_IN);
    ComputerStateComponent.setTimestampOfLogin((int) (Game.playClock().activeMs() / 1000L) + 1);
    assertTrue(LastHourProgressNet.complete(LastHourMilestone.LOGIN_SUCCEEDED));
  }

  private InventoryComponent inventory(Entity player) {
    return player.fetch(InventoryComponent.class).orElseThrow();
  }

  private LastHourSave.PlayerData savedPlayer(LastHourSave.SaveData save, String name) {
    return save.players().stream()
        .filter(player -> player.name().equals(name))
        .findFirst()
        .orElseThrow();
  }

  private List<String> usbOwners(LastHourSave.SaveData save) {
    return save.players().stream()
        .filter(
            player ->
                player.items().stream()
                    .anyMatch(item -> item.state().itemType().equals("BlueUsbStick")))
        .map(player -> player.name())
        .toList();
  }
}
