package starter;

import contrib.components.CollideComponent;
import contrib.entities.CharacterClass;
import contrib.entities.HeroBuilder;
import contrib.hud.dialogs.DialogFactory;
import contrib.modules.interaction.InteractionComponent;
import contrib.modules.puzzle.PuzzleMaker;
import contrib.modules.puzzle.PuzzlePieceItem;
import contrib.modules.puzzle.PuzzleTextureGenerator;
import contrib.systems.AttributeBarSystem;
import contrib.systems.PositionSync;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.configuration.KeyboardConfig;
import core.game.PreRunConfiguration;
import core.level.loader.DungeonLoader;
import core.network.ConnectionListener;
import core.network.config.NetworkConfig;
import core.network.messages.s2c.EntitySpawnEvent;
import core.utils.CursorUtil;
import core.utils.Tuple;
import core.utils.components.draw.DrawComponentFactory;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import level.LastHourLevel;
import level.LastHourLevelClient;
import modules.computer.ComputerFactory;
import modules.computer.ComputerStateSyncSystem;
import modules.computer.LastHourDialogTypes;
import modules.trash.TrashMinigameUI;
import modules.usbstick.UsbStickItem;
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
    PreRunConfiguration.networkPort(7777);
    PreRunConfiguration.multiplayerCharacterClass(null); // server decides

    registerCustomDialogs();
    UsbStickItem.ensureRegistration();
    PuzzlePieceItem.ensureRegistration();

    DungeonLoader.addLevel(Tuple.of("lasthour", LastHourLevelClient.class));

    // Game Settings
    Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
    Game.disableAudio(false);
    Game.frameRate(60);
    NetworkConfig.SNAPSHOT_TRANSLATOR = new LastHourSnapshotTranslator();
    NetworkConfig.ENTITY_SPAWN_STRATEGY = new LastHourEntitySpawnStrategy();
    Game.userOnSetup(
        () -> {
          registerEntitySpawnHandler();
          LastHourLevel.ensureClientPuzzles();
          if (TheLastHour.DEBUG_MODE) {
            Game.add(new Debugger());
          }
          TheLastHour.registerLocalWorldTimerSystem();
          Game.stage().ifPresent(CursorUtil::initListener);
          Game.remove(AttributeBarSystem.class);
          Game.add(new ComputerStateSyncSystem());
          TheLastHour.setupMusic();
          TheLastHour.staticRenderTextures();

          Game.network()
              .addConnectionListener(
                  new ConnectionListener() {
                    @Override
                    public void onConnected() {
                      Game.windowTitle("TheLastHour Client - " + PreRunConfiguration.username());
                    }

                    @Override
                    public void onDisconnected(String reason) {}
                  });
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
   * Registers a custom spawn handler that supports metadata-only Last Hour entities and collider
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
                if (spawnPlayer(event) && ctx != null) {
                  ctx.clientState().ifPresent(state -> state.trackNetworkEntity(event.entityId()));
                }
                return;
              }

              Entity newEntity = new Entity(event.entityId());
              if (event.positionComponent() != null) {
                newEntity.add(event.positionComponent());
              }
              ensurePuzzlePieceTextures(event.metadata());
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
              applyCollideMetadata(newEntity, event.metadata());
              newEntity.persistent(event.isPersistent());
              Game.add(newEntity);
              if (ctx != null) {
                ctx.clientState().ifPresent(state -> state.trackNetworkEntity(event.entityId()));
              }
            });
  }

  private static boolean spawnPlayer(EntitySpawnEvent event) {
    PlayerComponent playerComponent = event.playerComponent();
    if (playerComponent == null) {
      return false;
    }

    boolean alreadyGotAHero = Game.player().isPresent();
    boolean isLocal = Objects.equals(playerComponent.playerName(), PreRunConfiguration.username());
    if (alreadyGotAHero && isLocal) {
      return false;
    }

    Entity hero =
        HeroBuilder.builder()
            .id(event.entityId())
            .characterClass(CharacterClass.fromByteId(event.characterClassId()))
            .persistent(event.isPersistent())
            .isLocalPlayer(isLocal)
            .username(playerComponent.playerName())
            .build();
    applySpawnPosition(hero, event.positionComponent());
    applyCollideMetadata(hero, event.metadata());
    Game.add(hero);
    return true;
  }

  private static void applySpawnPosition(Entity entity, PositionComponent positionComponent) {
    if (positionComponent == null) {
      return;
    }

    entity
        .fetch(PositionComponent.class)
        .ifPresent(
            existingPosition -> {
              existingPosition.position(positionComponent.position());
              existingPosition.viewDirection(positionComponent.viewDirection());
              existingPosition.rotation(positionComponent.rotation());
              existingPosition.scale(positionComponent.scale());
              PositionSync.syncPosition(entity);
            });
  }

  private static void applyCollideMetadata(Entity entity, Map<String, String> metadata) {
    LastHourSnapshotTranslator.collideComponentFromMetadata(metadata)
        .ifPresent(
            collideComponent -> {
              CollideComponent component =
                  entity
                      .fetch(CollideComponent.class)
                      .orElseGet(
                          () -> {
                            CollideComponent newComponent = new CollideComponent();
                            entity.add(newComponent);
                            return newComponent;
                          });
              component.isSolid(collideComponent.isSolid());
              component.collider(collideComponent.collider());
              PositionSync.syncPosition(entity);
            });
  }

  /**
   * If the spawn event represents a puzzle piece world item, materializes the parent puzzle locally
   * (so {@link contrib.modules.puzzle.PuzzleDialog} can look it up later) and generates the
   * {@code @gen/puzzle/<id>/<idx>.png} textures so the draw component built right after this call
   * resolves to the correct image fragment.
   *
   * @param metadata the spawn event metadata
   */
  private static void ensurePuzzlePieceTextures(Map<String, String> metadata) {
    if (metadata == null) return;
    String puzzleId = metadata.get(LastHourEntitySpawnStrategy.METADATA_PUZZLE_PIECE_ID);
    if (puzzleId == null) return;
    String imagePath = metadata.get(LastHourEntitySpawnStrategy.METADATA_PUZZLE_PIECE_IMAGE);
    String pieceCount = metadata.get(LastHourEntitySpawnStrategy.METADATA_PUZZLE_PIECE_COUNT);
    String seed = metadata.get(LastHourEntitySpawnStrategy.METADATA_PUZZLE_PIECE_SEED);
    if (imagePath == null || pieceCount == null || seed == null) return;
    try {
      var puzzle =
          PuzzleMaker.ensurePuzzle(
              puzzleId,
              new SimpleIPath(imagePath),
              Integer.parseInt(pieceCount),
              Long.parseLong(seed));
      PuzzleTextureGenerator.ensureRegistered(puzzle);
    } catch (RuntimeException ignored) {
      // Best-effort: if anything goes wrong, fall through to the default texture loading
      // and let it fail loudly (the resulting visible error is clearer than a partial spawn).
    }
  }
}
