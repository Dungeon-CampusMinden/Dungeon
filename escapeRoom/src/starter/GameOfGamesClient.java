package starter;

import com.badlogic.gdx.Input;
import contrib.components.CollideComponent;
import contrib.entities.CharacterClass;
import contrib.entities.HeroBuilder;
import contrib.systems.AttributeBarSystem;
import contrib.systems.DebugDrawSystem;
import contrib.systems.LevelEditorSystem;
import contrib.systems.PositionSync;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.configuration.KeyboardConfig;
import core.game.PreRunConfiguration;
import core.network.ConnectionListener;
import core.network.messages.s2c.EntitySpawnEvent;
import core.utils.CursorUtil;
import core.utils.components.draw.DrawComponentFactory;
import gameOfGames.network.GameOfGamesSnapshotTranslator;
import java.util.Map;
import java.util.Objects;

/** Client-side setup for Game of Games. */
public final class GameOfGamesClient {

  private GameOfGamesClient() {}

  /** Registers client-side handlers and systems for Game of Games. */
  public static void clientSetup() {
    registerEntitySpawnHandler();
    Game.stage().ifPresent(CursorUtil::initListener);
    Game.remove(AttributeBarSystem.class);

    if (GameOfGames.DEBUG_MODE) {
      Game.add(new Debugger());
      KeyboardConfig.PAUSE.value(Input.Keys.UNKNOWN);
      Game.add(new DebugDrawSystem());
      Game.add(new LevelEditorSystem());
    }

    Game.network()
        .addConnectionListener(
            new ConnectionListener() {
              @Override
              public void onConnected() {
                Game.windowTitle("Game of Games Client - " + PreRunConfiguration.username());
              }

              @Override
              public void onDisconnected(String reason) {}
            });
  }

  private static void registerEntitySpawnHandler() {
    Game.network()
        .messageDispatcher()
        .registerHandler(
            EntitySpawnEvent.class,
            (ctx, event) -> {
              if (Game.levelEntities().anyMatch(e -> e.id() == event.entityId())) {
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
              if (event.drawInfo() != null) {
                newEntity.add(DrawComponentFactory.fromDrawInfo(event.drawInfo()));
              }
              GameOfGamesSnapshotTranslator.applyInteractableMetadata(newEntity, event.metadata());
              applyCollideMetadata(newEntity, event.metadata());
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
    GameOfGamesSnapshotTranslator.collideComponentFromMetadata(metadata)
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
}
