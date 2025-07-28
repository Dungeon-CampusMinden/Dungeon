package core.network;

import static contrib.entities.HeroFactory.ENABLE_MOUSE_MOVEMENT;

import com.badlogic.gdx.ai.pfa.GraphPath;
import contrib.components.HealthComponent;
import contrib.components.InteractionComponent;
import contrib.components.PathComponent;
import contrib.entities.HeroFactory;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.utils.LevelUtils;
import core.network.messages.EntityStateUpdate;
import core.network.messages.NetworkEvent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * NetworkHandler implementation for single-player or local processing. Messages are processed
 * directly within the same JVM, simulating network behavior.
 */
public class LocalNetworkHandler implements INetworkHandler {

  private static final Logger LOGGER = Logger.getLogger(LocalNetworkHandler.class.getName());
  private Consumer<EntityStateUpdate> stateUpdateListener;
  private Consumer<NetworkEvent> eventReceivedListener;
  private boolean isRunning = false;
  private boolean isInitialized = false;

  @Override
  public void initialize(boolean isServer, String serverAddress, int port) throws NetworkException {
    // No actual network initialization needed for local handler.
    // We can use the parameters for potential future logging or mock setup if needed.
    this.isInitialized = true;
    LOGGER.info(
        "LocalNetworkHandler initialized. (Server: "
            + isServer
            + ", Address: "
            + serverAddress
            + ", Port: "
            + port
            + ")");
  }

  @Override
  public void sendHeroMovement(Direction direction) {
    if (!isRunning || !isInitialized) {
      LOGGER.warning("LocalNetworkHandler not running or initialized, ignoring sendHeroMovement.");
      return;
    }

    Game.hero()
        .ifPresent(
            hero -> {
              VelocityComponent vc =
                  hero.fetch(VelocityComponent.class)
                      .orElseThrow(
                          () -> MissingComponentException.build(hero, VelocityComponent.class));

              Vector2 newVelocity = vc.currentVelocity();
              if (direction.x() != 0) {
                newVelocity = Vector2.of(direction.scale(vc.velocity()).x(), newVelocity.y());
              }
              if (direction.y() != 0) {
                newVelocity = Vector2.of(newVelocity.x(), direction.scale(vc.velocity()).y());
              }
              vc.currentVelocity(newVelocity);

              // Abort any path finding on own movement
              if (ENABLE_MOUSE_MOVEMENT) {
                hero.fetch(PathComponent.class).ifPresent(PathComponent::clear);
              }
            });
  }

  @Override
  public void sendHeroMovement(Point targetPoint) {
    if (!isRunning || !isInitialized) {
      LOGGER.warning("LocalNetworkHandler not running or initialized, ignoring sendHeroMovement.");
      return;
    }

    Game.hero()
        .ifPresent(
            hero -> {
              Point heroPos =
                  hero.fetch(PositionComponent.class).map(PositionComponent::position).orElse(null);
              if (heroPos == null) return;

              GraphPath<Tile> path = LevelUtils.calculatePath(heroPos, targetPoint);
              // If the path is null or empty, try to find a nearby tile that is accessible and
              // calculate a path to it
              if (path == null || path.getCount() == 0) {
                Tile nearTile =
                    LevelUtils.tilesInRange(targetPoint, 1f).stream()
                        .filter(tile -> LevelUtils.calculatePath(heroPos, tile.position()) != null)
                        .findFirst()
                        .orElse(null);
                // If no accessible tile is found, abort
                if (nearTile == null) return;
                path = LevelUtils.calculatePath(heroPos, nearTile.position());
              }

              // Stores the path in Hero's PathComponent
              GraphPath<Tile> finalPath = path;
              hero.fetch(PathComponent.class)
                  .ifPresentOrElse(
                      pathComponent -> pathComponent.path(finalPath),
                      () -> hero.add(new PathComponent(finalPath)));
            });
  }

  @Override
  public void sendUseSkill(int skillIndex, Point targetPoint) {
    if (!isRunning || !isInitialized) {
      LOGGER.warning("LocalNetworkHandler not running or initialized, ignoring sendUseSkill.");
      return;
    }
    Game.hero()
        .ifPresent(
            hero -> {
              HeroFactory.getHeroSkill().execute(hero);
            });
  }

  @Override
  public void sendInteract(Entity interactable) {
    if (!isRunning || !isInitialized) {
      LOGGER.warning("LocalNetworkHandler not running or initialized, ignoring sendInteract.");
      return;
    }

    Game.hero()
        .ifPresent(
            hero -> {
              InteractionComponent ic =
                  interactable
                      .fetch(InteractionComponent.class)
                      .orElseThrow(
                          () ->
                              MissingComponentException.build(
                                  interactable, InteractionComponent.class));
              ic.triggerInteraction(interactable, hero);
            });
  }

  @Override
  public void setOnStateUpdateListener(Consumer<EntityStateUpdate> listener) {
    this.stateUpdateListener = listener;
  }

  @Override
  public void setOnEventReceivedListener(Consumer<NetworkEvent> listener) {
    this.eventReceivedListener = listener;
  }

  @Override
  public void start() {
    if (!isInitialized) {
      LOGGER.severe("LocalNetworkHandler cannot start because it is not initialized.");
      return;
    }
    this.isRunning = true;
    LOGGER.info("LocalNetworkHandler started.");
    // In a real scenario, this might start a thread or integrate with the game loop
    // to periodically generate and send EntityStateUpdates.
    // For single player, the game loop itself can call triggerStateUpdate().
  }

  @Override
  public void shutdown() {
    this.isRunning = false;
    this.isInitialized = false;
    LOGGER.info("LocalNetworkHandler shutdown.");
  }

  @Override
  public boolean isConnected() {
    return isRunning && isInitialized;
  }

  @Override
  public boolean isServer() {
    // In single player context managed by this handler, it acts as the authority.
    return true;
  }

  /**
   * Collects the current state of relevant entities and sends it to the state update listener. This
   * simulates the server sending periodic state updates. This method should be called by the game
   * loop.
   */
  @Override
  public void triggerStateUpdate() {
    if (stateUpdateListener != null && isRunning && isInitialized) {
      EntityStateUpdate update = collectCurrentEntityStates();
      if (update != null && !update.entityStates().isEmpty()) {
        stateUpdateListener.accept(update);
      }
    } else if (isRunning && isInitialized) {
      // Listener might not be set yet, or intentionally null
      LOGGER.fine("State update triggered but listener is null or not ready.");
    }
  }

  private EntityStateUpdate collectCurrentEntityStates() {
    EntityStateUpdate update = new EntityStateUpdate();
    try {
      Game.entityStream()
          .forEach(
              entity -> {
                var posCompOpt = entity.fetch(PositionComponent.class);
                var velCompOpt = entity.fetch(VelocityComponent.class);
                var drawCompOpt = entity.fetch(DrawComponent.class);
                var healthCompOpt = entity.fetch(HealthComponent.class);

                if (posCompOpt.isPresent()) { // Only send state for entities with a position
                  var posComp = posCompOpt.get();
                  var velComp = velCompOpt.orElse(null);
                  var drawComp = drawCompOpt.orElse(null);
                  var healthComp = healthCompOpt.orElse(null);

                  String animationState = "idle";
                  boolean isVisible = false;
                  if (drawComp != null) {
                    // TODO: WIP
                    animationState = velComp != null && velComp.currentVelocity().length() > 0
                        ? velComp.currentVelocity().x() < 0
                            ? "run_left"
                            : "run_right"
                        : "idle";
                    isVisible = drawComp.isVisible();
                  }

                  int health = healthComp != null ? healthComp.currentHealthpoints() : -1;

                  EntityStateUpdate.EntityState state =
                      new EntityStateUpdate.EntityState(
                          posComp.position(),
                          velComp != null ? velComp.currentVelocity() : Vector2.ZERO,
                          posComp.viewDirection(),
                          animationState,
                          isVisible,
                          health);
                  update.addEntityState(entity.id(), state);
                }
              });
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Error collecting entity states", e);
      return null; // Return null or an empty update on error?
    }
    return update;
  }
}
