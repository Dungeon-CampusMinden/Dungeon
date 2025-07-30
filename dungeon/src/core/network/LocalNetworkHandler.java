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
import core.network.messages.NetworkMessage;
import core.network.messages.server2client.DrawUpdate;
import core.network.messages.server2client.HealthUpdate;
import core.network.messages.server2client.PositionUpdate;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * NetworkHandler implementation for single-player or local processing. Messages are processed
 * directly within the same JVM, simulating network behavior.
 */
public class LocalNetworkHandler implements INetworkHandler {

  private static final Logger LOGGER = Logger.getLogger(LocalNetworkHandler.class.getName());

  private final MessageDispatcher dispatcher = new MessageDispatcher(); // Instantiate dispatcher
  private Consumer<NetworkMessage> rawMessageConsumer; // Internal consumer for raw messages
  private boolean isRunning = false;
  private boolean isInitialized = false;

  private final Map<Integer, Integer> lastKnownHealth = new HashMap<>();
  private final Map<Integer, String> lastKnownAnimation = new HashMap<>();

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
  public void start() {
    if (!isInitialized) {
      LOGGER.severe("LocalNetworkHandler cannot start because it is not initialized.");
      return;
    }
    this.isRunning = true;
    LOGGER.info("LocalNetworkHandler started.");
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

  @Override
  public MessageDispatcher messageDispatcher() {
    return dispatcher;
  }

  @Override
  public void _setRawMessageConsumer(Consumer<NetworkMessage> rawMessageConsumer) {
    this.rawMessageConsumer = rawMessageConsumer;
  }

  /**
   * Collects the current state of relevant entities and sends it to the state update listener. This
   * simulates the server sending periodic state updates. This method should be called by the game
   * loop.
   */
  public void triggerStateUpdate() {
    if (!isRunning || !isInitialized || rawMessageConsumer == null) {
      LOGGER.info(
          "LocalNetworkHandler not ready to send updates. Running: "
              + isRunning
              + ", Init: "
              + isInitialized
              + ", Consumer: "
              + (rawMessageConsumer != null));
      return;
    }

    Set<Integer> activeEntityIds = new HashSet<>();

    Game.entityStream()
        .forEach(
            entity -> {
              int entityId = entity.id();
              activeEntityIds.add(entityId);

              // Always send position data
              entity
                  .fetch(PositionComponent.class)
                  .ifPresent(
                      pc -> {
                        PositionUpdate posUpdate =
                            new PositionUpdate(entityId, pc.position(), pc.viewDirection());
                        rawMessageConsumer.accept(posUpdate);
                      });

              // Send health data
              entity
                  .fetch(HealthComponent.class)
                  .ifPresent(
                      hc -> {
                        int currentHealth = hc.currentHealthpoints();
                        if (!lastKnownHealth
                            .getOrDefault(entityId, Integer.MIN_VALUE)
                            .equals(currentHealth)) {
                          HealthUpdate healthUpdate =
                              new HealthUpdate(entityId, currentHealth, hc.maximalHealthpoints());
                          rawMessageConsumer.accept(healthUpdate);
                          lastKnownHealth.put(entityId, currentHealth);
                        }
                      });

              entity
                  .fetch(DrawComponent.class)
                  .ifPresent(
                      dc -> {
                        String currentAnimation = dc.currentAnimationName();
                        if (!lastKnownAnimation
                            .getOrDefault(entityId, "")
                            .equals(currentAnimation)) {
                          DrawUpdate drawUpdate =
                              new DrawUpdate(entityId, currentAnimation, dc.tintColor());
                          rawMessageConsumer.accept(drawUpdate);
                          lastKnownAnimation.put(entityId, currentAnimation);
                        }
                      });
            });
    lastKnownHealth.keySet().retainAll(activeEntityIds);
  }
}
