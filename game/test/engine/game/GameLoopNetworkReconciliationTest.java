package engine.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.scenes.scene2d.Group;
import engine.Entity;
import engine.Game;
import engine.level.utils.Coordinate;
import engine.network.MessageDispatcher;
import engine.network.NetworkTelemetry;
import engine.network.SnapshotTranslator;
import engine.network.delta.SnapshotDeltaCompressor;
import engine.network.messages.s2c.DeltaSnapshotMessage;
import engine.network.messages.s2c.DialogCloseMessage;
import engine.network.messages.s2c.DialogShowMessage;
import engine.network.messages.s2c.DoorTileState;
import engine.network.messages.s2c.EntityDelta;
import engine.network.messages.s2c.EntityState;
import engine.network.messages.s2c.LevelState;
import engine.network.messages.s2c.SnapshotMessage;
import engine.network.server.ClientState;
import engine.network.server.Session;
import engine.utils.Direction;
import engine.utils.Point;
import feature.components.UIComponent;
import feature.entities.CharacterClass;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import testingUtils.MockNetworkHandler;

/** Tests for client-side network entity reconciliation in {@link GameLoop}. */
public class GameLoopNetworkReconciliationTest {

  /** Cleans up global game state after each test. */
  @AfterEach
  public void cleanup() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    Game.currentLevel(null);
    NetworkTelemetry.reset();
    PreRunConfiguration.multiplayerEnabled(false);
    PreRunConfiguration.isNetworkServer(true);
  }

  /** A repeated server show preserves the local dialog and its current presentation state. */
  @Test
  public void repeatedDialogShowKeepsOneInstanceUntilServerClose() throws Exception {
    Game.removeAllEntities();
    Game.removeAllSystems();
    MockNetworkHandler.useLocalNetworkHandler();
    PreRunConfiguration.multiplayerEnabled(true);
    PreRunConfiguration.isNetworkServer(false);
    DialogType type = () -> "network-reconciliation-test";
    DialogFactory.register(type, ignored -> new Group());
    setupGameLoopMessageHandlers();
    MessageDispatcher dispatcher = Game.network().messageDispatcher();
    // Each delivery is decoded separately and still names the server's absent UI owner.
    dispatcher.dispatch(
        null,
        new DialogShowMessage(
            new DialogContext(
                type, false, Map.of(DialogContextKeys.OWNER_ENTITY, 1000000), "intro"),
            false));
    UIComponent first =
        Game.levelEntities()
            .flatMap(e -> e.fetch(UIComponent.class).stream())
            .findFirst()
            .orElseThrow();
    Group dialog = first.dialog();
    dialog.setUserObject("page two");

    dispatcher.dispatch(
        null,
        new DialogShowMessage(
            new DialogContext(
                type, false, Map.of(DialogContextKeys.OWNER_ENTITY, 1000000), "intro"),
            false));

    List<UIComponent> open =
        Game.levelEntities().flatMap(e -> e.fetch(UIComponent.class).stream()).toList();
    assertEquals(1, open.size(), "Repeated synchronization must not stack another intro");
    assertSame(first, open.getFirst());
    assertSame(dialog, open.getFirst().dialog());
    assertEquals("page two", dialog.getUserObject());
    dispatcher.dispatch(null, new DialogCloseMessage("intro"));
    assertTrue(
        Game.levelEntities().flatMap(e -> e.fetch(UIComponent.class).stream()).findAny().isEmpty());
  }

  /** Verifies authoritative full snapshots remove only tracked network entities. */
  @Test
  public void reconcileNetworkEntitiesRemovesOnlyTrackedEntitiesMissingFromSnapshot() {
    ClientState state =
        new ClientState((short) 1, "tester", 1, new byte[] {1, 2, 3}, CharacterClass.WIZARD);
    Entity keptNetworkEntity = new Entity();
    Entity staleNetworkEntity = new Entity();
    Entity localEntity = new Entity();
    Game.add(keptNetworkEntity);
    Game.add(staleNetworkEntity);
    Game.add(localEntity);
    state.trackNetworkEntity(keptNetworkEntity.id());
    state.trackNetworkEntity(staleNetworkEntity.id());
    SnapshotMessage fullSnapshot =
        new SnapshotMessage(
            20,
            List.of(EntityState.builder().entityId(keptNetworkEntity.id()).build()),
            new LevelState(Set.of()));

    GameLoop.reconcileNetworkEntities(state, fullSnapshot);

    assertTrue(Game.findEntityById(keptNetworkEntity.id()).isPresent());
    assertTrue(Game.findEntityById(localEntity.id()).isPresent());
    assertTrue(Game.findEntityById(staleNetworkEntity.id()).isEmpty());
    assertTrue(state.networkSyncedEntityIds().contains(keptNetworkEntity.id()));
    assertFalse(state.networkSyncedEntityIds().contains(staleNetworkEntity.id()));
  }

  /** Verifies delta application forwards only changed entities with materialized field state. */
  @Test
  public void changedSnapshotForDeltaContainsOnlyMaterializedChangedEntities() {
    Coordinate changedDoor = new Coordinate(2, 3);
    SnapshotMessage baseline =
        new SnapshotMessage(
            10,
            List.of(
                EntityState.builder()
                    .entityId(1)
                    .position(new Point(1, 1))
                    .viewDirection(Direction.LEFT)
                    .build(),
                EntityState.builder().entityId(2).position(new Point(5, 5)).build()),
            new LevelState(Set.of(new DoorTileState(changedDoor, false))));
    DeltaSnapshotMessage delta =
        new DeltaSnapshotMessage(
            10,
            11,
            List.of(
                new EntityDelta(
                    1,
                    EntityState.builder().entityId(1).position(new Point(3, 4)).build(),
                    Set.of())),
            List.of(),
            new LevelState(Set.of(new DoorTileState(changedDoor, true))));
    SnapshotMessage materializedSnapshot =
        SnapshotDeltaCompressor.materializeSnapshot(baseline, delta);

    SnapshotMessage changedSnapshot = GameLoop.changedSnapshotForDelta(delta, materializedSnapshot);

    assertEquals(1, changedSnapshot.entities().size());
    EntityState changedEntity = changedSnapshot.entities().getFirst();
    assertEquals(1, changedEntity.entityId());
    assertEquals(new Point(3, 4), changedEntity.position().orElseThrow());
    assertEquals("LEFT", changedEntity.viewDirection().orElseThrow());
    assertEquals(1, changedSnapshot.levelState().doorStates().size());
    assertTrue(changedSnapshot.levelState().doorStates().iterator().next().open());
  }

  /** Verifies client snapshot handlers update telemetry for accepted and stale snapshots. */
  @Test
  public void snapshotHandlersRecordApplyAndStaleTelemetry() throws Exception {
    MockNetworkHandler.useLocalNetworkHandler();
    Game.network().snapshotTranslator(noopSnapshotTranslator());
    setupGameLoopMessageHandlers();
    NetworkTelemetry.reset();
    Session session = testSession();
    SnapshotMessage fullSnapshot =
        new SnapshotMessage(
            10, List.of(EntityState.builder().entityId(1).build()), new LevelState(Set.of()));
    SnapshotMessage staleFullSnapshot =
        new SnapshotMessage(
            10, List.of(EntityState.builder().entityId(2).build()), new LevelState(Set.of()));
    DeltaSnapshotMessage deltaSnapshot =
        new DeltaSnapshotMessage(
            10,
            11,
            List.of(
                new EntityDelta(
                    1,
                    EntityState.builder().entityId(1).position(new Point(2, 3)).build(),
                    Set.of())),
            List.of(),
            new LevelState(Set.of()));
    DeltaSnapshotMessage staleDeltaSnapshot =
        new DeltaSnapshotMessage(9, 10, List.of(), List.of(), new LevelState(Set.of()));

    Game.network().messageDispatcher().dispatch(session, fullSnapshot);
    Game.network().messageDispatcher().dispatch(session, staleFullSnapshot);
    Game.network().messageDispatcher().dispatch(session, deltaSnapshot);
    Game.network().messageDispatcher().dispatch(session, staleDeltaSnapshot);

    String debugText = NetworkTelemetry.debugText();
    assertTrue(debugText.contains("Snapshots: applied full=1 delta=1"));
    assertTrue(debugText.contains("stale=1 (expected 0)/1 (expected 0)"));
    assertTrue(debugText.contains("last=delta@11"));
    assertTrue(debugText.contains("handler=1 (expected 0)/1 (expected 0)"));
  }

  private static void setupGameLoopMessageHandlers() throws Exception {
    Constructor<GameLoop> constructor = GameLoop.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    GameLoop gameLoop = constructor.newInstance();
    Method method = GameLoop.class.getDeclaredMethod("setupMessageHandlers");
    method.setAccessible(true);
    method.invoke(gameLoop);
  }

  private static Session testSession() {
    Session session =
        new Session(
            null,
            (target, message) -> CompletableFuture.completedFuture(true),
            (ctx, message) -> CompletableFuture.completedFuture(true));
    session.attachClientState(
        new ClientState((short) 1, "tester", 1, new byte[] {1, 2, 3}, CharacterClass.WIZARD));
    return session;
  }

  private static SnapshotTranslator noopSnapshotTranslator() {
    return new SnapshotTranslator() {
      @Override
      public Optional<SnapshotMessage> translateToSnapshot(int serverTick) {
        return Optional.empty();
      }

      @Override
      public void applySnapshot(SnapshotMessage snapshot, MessageDispatcher dispatcher) {}
    };
  }
}
