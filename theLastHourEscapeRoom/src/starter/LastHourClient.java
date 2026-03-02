package starter;

import contrib.entities.CharacterClass;
import contrib.entities.HeroBuilder;
import contrib.hud.dialogs.DialogFactory;
import contrib.modules.interaction.InteractionComponent;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.configuration.KeyboardConfig;
import core.game.PreRunConfiguration;
import core.level.loader.DungeonLoader;
import core.network.config.NetworkConfig;
import core.network.messages.s2c.EntitySpawnEvent;
import core.utils.Tuple;
import core.utils.components.draw.DrawComponentFactory;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.*;
import level.LastHourLevelClient;
import modules.computer.*;
import modules.trash.TrashMinigameUI;
import network.LastHourEntitySpawnStrategy;
import network.LastHourSnapshotTranslator;
import util.ui.BlackFadeCutscene;

/** The main class for the Multiplayer Client for development and testing purposes. */
public final class LastHourClient {

  /**
   * Main method to start the dev client.
   *
   * @param args command line arguments
   * @throws IOException if an I/O error occurs
   */
  public static void main(String[] args) throws IOException {
    // PreRun configuration for multiplayer client
    PreRunConfiguration.multiplayerEnabled(true);
    PreRunConfiguration.isNetworkServer(false);
    PreRunConfiguration.networkServerAddress("127.0.0.1");
    PreRunConfiguration.networkPort(7777);
    PreRunConfiguration.username("Player1");

    registerCustomDialogs();

    DungeonLoader.addLevel(Tuple.of("lasthour", LastHourLevelClient.class));

    // Game Settings
    Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
    Game.disableAudio(false);
    Game.frameRate(60);
    Game.windowTitle("Dev Client - " + PreRunConfiguration.username());
    NetworkConfig.SNAPSHOT_TRANSLATOR = new LastHourSnapshotTranslator();
    NetworkConfig.ENTITY_SPAWN_STRATEGY = new LastHourEntitySpawnStrategy();
    Game.userOnSetup(
        () -> {
          registerEntitySpawnHandler();
          Game.add(new Debugger());
          System.out.println("DevClient started");
        });

    // Start the game
    Game.run();
  }

  private static void registerCustomDialogs() {
    ComputerFactory.ensureRegistration();
    DialogFactory.register(LastHourDialogTypes.TRASHCAN, TrashMinigameUI::build);
    DialogFactory.register(LastHourDialogTypes.TEXT_CUTSCENE, BlackFadeCutscene::build);
  }

  /**
   * Registers a custom spawn handler that supports metadata-only entities for computer state
   * synchronization.
   */
  private static void registerEntitySpawnHandler() {
    Game.network()
        .messageDispatcher()
        .registerHandler(
            EntitySpawnEvent.class,
            (ctx, event) -> {
              if (Game.allEntities().anyMatch(e -> e.id() == event.entityId())) {
                return;
              }

              if (event.playerComponent() != null) {
                spawnPlayer(event);
                return;
              }

              Entity newEntity = new Entity(event.entityId());
              if (event.positionComponent() != null) {
                newEntity.add(event.positionComponent());
              }
              if (event.drawInfo() != null) {
                newEntity.add(DrawComponentFactory.fromDrawInfo(event.drawInfo()));
              }
              if (event.metadata().containsKey(LastHourEntitySpawnStrategy.METADATA_INTERACTABLE)) {
                newEntity.add(new InteractionComponent());
              }
              LastHourSnapshotTranslator.computerStateFromMetadata(event.metadata())
                  .ifPresent(newEntity::add);
              LastHourSnapshotTranslator.keypadStateFromMetadata(event.metadata())
                  .ifPresent(newEntity::add);
              LastHourSnapshotTranslator.worldTimerStateFromMetadata(event.metadata())
                  .ifPresent(newEntity::add);
              newEntity.persistent(event.isPersistent());
              Game.add(newEntity);
            });
  }

  private static void spawnPlayer(EntitySpawnEvent event) {
    PlayerComponent playerComponent = event.playerComponent();
    if (playerComponent == null) {
      return;
    }

    boolean alreadyGotAHero = Game.player().isPresent();
    boolean isLocal = Objects.equals(playerComponent.playerName(), PreRunConfiguration.username());
    if (alreadyGotAHero && isLocal) {
      return;
    }

    Game.add(
        HeroBuilder.builder()
            .id(event.entityId())
            .characterClass(CharacterClass.fromByteId(event.characterClassId()))
            .isLocalPlayer(isLocal)
            .username(playerComponent.playerName())
            .build());
  }
}
