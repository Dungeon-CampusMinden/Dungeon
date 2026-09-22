package rooms.systemRecovery;

import com.badlogic.gdx.Input;
import engine.Entity;
import engine.Game;
import engine.components.PlayerComponent;
import engine.components.PositionComponent;
import engine.configuration.KeyboardConfig;
import engine.game.PreRunConfiguration;
import engine.network.ConnectionListener;
import engine.network.codec.ShaderComponentCodec;
import engine.network.messages.s2c.EntitySpawnEvent;
import engine.utils.CursorUtil;
import engine.utils.components.draw.DrawComponentFactory;
import engine.utils.components.draw.TextureGenerator;
import engine.utils.components.draw.shader.ColorGradeShader;
import engine.utils.components.draw.shader.ShaderList;
import feature.components.CollideComponent;
import feature.components.Debugger;
import feature.entities.CharacterClass;
import feature.entities.HeroBuilder;
import feature.systems.AttributeBarSystem;
import feature.systems.DebugDrawSystem;
import feature.systems.LevelEditorSystem;
import feature.systems.PositionSync;
import java.util.Map;
import java.util.Objects;
import rooms.systemRecovery.network.SystemCoreVisualSync;
import rooms.systemRecovery.network.SystemRecoveryComponentSync;
import rooms.systemRecovery.save.SystemRecoveryAutoSaveHud;
import rooms.systemRecovery.network.SystemRecoveryEntitySpawnStrategy;

/** Client-side setup for System Recovery. */
public final class SystemRecoveryClient {

  private SystemRecoveryClient() {}

  /** Registers client-side handlers and systems for System Recovery. */
  public static void clientSetup() {
    SystemRecoveryAutoSaveHud.reset();
    registerEntitySpawnHandler();
    Game.stage().ifPresent(CursorUtil::initListener);
    Game.remove(AttributeBarSystem.class);
    registerInputPromptTexture();

    if (SystemRecovery.debugMode()) {
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
                Game.windowTitle("System Recovery Client - " + PreRunConfiguration.username());
              }

              @Override
              public void onDisconnected(String reason) {}
            });
  }

  /**
   * Applies the same input-prompt treatment as Last Hour.
   *
   * <p>The source spritesheet uses transparent cut-outs for key glyphs. Rendering it through the
   * shared inversion pipeline turns the prompts into the dark key style used by the tutorial, while
   * preserving the texture path expected by the rich-label parser.
   */
  private static void registerInputPromptTexture() {
    String keyboardPromptPath = "hud/input/keyboard_mouse.png";
    ShaderList shaders = new ShaderList();
    shaders.add("invert", new ColorGradeShader().invert(true));
    TextureGenerator.registerRenderShaderTexture(keyboardPromptPath, keyboardPromptPath, shaders);
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
              if (event.shaderComponent() != null) {
                newEntity.add(ShaderComponentCodec.fromState(event.shaderComponent()));
              }
              SystemRecoveryComponentSync.applyEntityMetadata(newEntity, event.metadata());
              SystemRecoveryAutoSaveHud.acceptRevision(
                  event.metadata().get(SystemRecoveryEntitySpawnStrategy.METADATA_SAVE_REVISION));
              SystemCoreVisualSync.applyAlarm(event.metadata());
              SystemCoreVisualSync.applyCompletionMetadata(newEntity, event.metadata());
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
    if (event.shaderComponent() != null) {
      hero.add(ShaderComponentCodec.fromState(event.shaderComponent()));
    }
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
    SystemRecoveryComponentSync.collideComponentFromMetadata(metadata)
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
