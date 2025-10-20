package contrib.entities;

import com.badlogic.gdx.ai.pfa.GraphPath;
import contrib.components.InteractionComponent;
import contrib.components.PathComponent;
import contrib.components.SkillComponent;
import contrib.utils.components.skill.cursorSkill.CursorSkill;
import contrib.utils.components.skill.projectileSkill.ProjectileSkill;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.utils.LevelUtils;
import core.network.messages.c2s.InputMessage;
import core.network.server.ClientState;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import core.utils.logging.DungeonLogger;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

/**
 * Controller class for handling hero entity actions such as movement, skill usage, and
 * interactions.
 *
 * <p>Provides static methods to manipulate the hero entity based on input commands, including
 * moving in a direction, following a path, using skills, and interacting with entities.
 *
 * <p>Also manages an input queue for processing client inputs in a server-authoritative manner.
 *
 * @see HeroFactory HeroFactory for creating hero entities
 */
public class HeroController {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(HeroController.class);
  private static final Queue<Tuple<ClientState, InputMessage>> inputQueue =
      new ConcurrentLinkedQueue<>();

  /** If true, the hero can be moved with the mouse. */
  public static final boolean ENABLE_MOUSE_MOVEMENT = true;

  /** The ID for the movement force. */
  public static final String MOVEMENT_ID = "Movement";

  private HeroController() {}

  /**
   * Moves the hero entity in the specified direction by applying a force to its VelocityComponent.
   *
   * @param hero the hero entity to move
   * @param direction the direction to move the hero
   */
  public static void moveHero(Entity hero, Direction direction) {
    LOGGER.debug("Moving hero {} in direction {}", hero.id(), direction);
    // TODO: get correct class
    CharacterClass heroClass = HeroFactory.DEFAULT_HERO_CLASS;

    VelocityComponent vc =
        hero.fetch(VelocityComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, VelocityComponent.class));

    Optional<Vector2> existingForceOpt = vc.force(MOVEMENT_ID);
    Vector2 newForce = heroClass.speed().scale(direction);

    Vector2 updatedForce =
        existingForceOpt.map(existing -> existing.add(newForce)).orElse(newForce);

    if (updatedForce.lengthSquared() > 0) {
      updatedForce = updatedForce.normalize().scale(heroClass.speed().length());
      vc.applyForce(MOVEMENT_ID, updatedForce);
    }

    if (ENABLE_MOUSE_MOVEMENT) {
      hero.fetch(PathComponent.class).ifPresent(PathComponent::clear);
    }
  }

  /**
   * Moves the hero entity along a path to the specified target point. Calculates a path from the
   * hero's current position to the target using LevelUtils. If a valid path is found, it is stored
   * in the hero's {@link PathComponent} for path-following behavior.
   *
   * @param hero the hero entity to move
   * @param target the target point to move towards
   * @see contrib.systems.PathSystem PathSystem
   */
  public static void moveHeroPath(Entity hero, Point target) {
    LOGGER.debug("Moving hero {} to point {}", hero.id(), target);
    Point heroPos =
        hero.fetch(PositionComponent.class).map(PositionComponent::position).orElse(null);
    if (heroPos == null) return;

    GraphPath<Tile> path = LevelUtils.calculatePath(heroPos, target);
    // If the path is null or empty, try to find a nearby tile that is accessible and
    // calculate a path to it
    if (path == null || path.getCount() == 0) {
      Tile nearTile =
          LevelUtils.tilesInRange(target, 1f).stream()
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
  }

  /**
   * Uses the hero's active skill targeting the specified point. If the active skill is a
   * CursorSkill or ProjectileSkill, sets the target position accordingly before executing the
   * skill.
   *
   * @param hero the hero entity using the skill
   * @param target the target point for the skill
   */
  public static void useSkill(Entity hero, Point target) {
    LOGGER.debug("Hero {} using skill at point {}", hero.id(), target);
    hero.fetch(SkillComponent.class)
        .flatMap(SkillComponent::activeSkill)
        .ifPresent(
            skill -> {
              if (skill instanceof CursorSkill cursorSkill) {
                cursorSkill.cursorPositionSupplier(() -> target);
              } else if (skill instanceof ProjectileSkill projSkill) {
                projSkill.endPointSupplier(() -> target);
              }
              skill.execute(hero);
            });
  }

  /**
   * Handles interaction between the hero and an interactable entity. First attempts to find an
   * interactable entity at the specified point (e.g., mouse cursor position). If no interactable
   * entity is found or the entity is out of range, it searches within a 1-tile radius around the
   * hero. If an interactable entity is found and within its interaction radius, the interaction is
   * triggered.
   *
   * @param hero the hero entity attempting the interaction
   * @param point the target point where the interaction is attempted (e.g., cursor position)
   */
  public static void interact(Entity hero, Point point) {
    LOGGER.debug("Hero {} interacting at point {}", hero.id(), point);
    PositionComponent heroPc =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));

    // Try finding interactable at the exact point first
    Optional<Entity> target =
        Game.tileAt(point)
            .map(Game::entityAtTile)
            .orElse(Stream.empty())
            .filter(e -> e.fetch(InteractionComponent.class).isPresent())
            .findFirst();

    // Check if target at point is in range
    boolean targetInRange = target.map(entity -> canInteract(entity, heroPc)).orElse(false);

    // If nothing found at point OR found but out of range, search in 1-tile radius around hero
    if (target.isEmpty() || !targetInRange) {
      LOGGER.trace(
          "No interactable found at point {}, searching in radius around hero {}",
          point,
          hero.id());
      target =
          LevelUtils.tilesInRange(heroPc.position(), 1f).stream()
              .flatMap(Game::entityAtTile)
              .filter(e -> e.fetch(InteractionComponent.class).isPresent())
              .findFirst();
    }

    // Trigger interaction if entity found and within interaction radius
    target.ifPresentOrElse(
        entity -> {
          InteractionComponent ic = entity.fetch(InteractionComponent.class).orElseThrow();
          PositionComponent targetPc =
              entity
                  .fetch(PositionComponent.class)
                  .orElseThrow(
                      () -> MissingComponentException.build(entity, PositionComponent.class));

          if (heroPc.position().distance(targetPc.position()) <= ic.radius()) {
            LOGGER.trace("Hero {} interacting with entity {}", hero.id(), entity.id());
            ic.triggerInteraction(entity, hero);
          } else {
            LOGGER.trace("Entity {} out of interaction range for hero {}", entity.id(), hero.id());
          }
        },
        () -> LOGGER.trace("No interactable entity found for hero {} to interact with", hero.id()));
  }

  /**
   * Checks if the hero can interact with the given entity. Returns true if the entity has both
   * position and interaction components, and the hero is within the interaction radius.
   *
   * @param entity the entity to check
   * @param heroPc the hero's position component
   * @return true if interaction is possible, false otherwise
   */
  private static boolean canInteract(Entity entity, PositionComponent heroPc) {
    PositionComponent targetPc = entity.fetch(PositionComponent.class).orElse(null);
    InteractionComponent ic = entity.fetch(InteractionComponent.class).orElse(null);
    return targetPc != null
        && ic != null
        && heroPc.position().distance(targetPc.position()) <= ic.radius();
  }

  /**
   * Changes the active skill of the hero entity. If nextSkill is true, switches to the next skill;
   * otherwise, switches to the previous skill.
   *
   * @param hero the hero entity whose skill is to be changed
   * @param nextSkill if true, switch to the next skill; if false, switch to the previous skill
   */
  public static void changeSkill(Entity hero, boolean nextSkill) {
    LOGGER.debug("Hero {} changing skill, nextSkill={}", hero.id(), nextSkill);
    hero.fetch(SkillComponent.class)
        .ifPresent(
            skillComponent -> {
              if (nextSkill) skillComponent.nextSkill();
              else skillComponent.prevSkill();
            });
  }

  /**
   * Adds an input message from a client to the input queue for processing.
   *
   * @param clientState The state of the client sending the input.
   * @param msg The input message to enqueue.
   */
  public static void enqueueInput(ClientState clientState, InputMessage msg) {
    inputQueue.add(Tuple.of(clientState, msg));
  }

  /**
   * Drains the input queue and applies valid inputs to the corresponding hero entities. Processes
   * messages in arrival order (FIFO), but discards stale/duplicate inputs based on sequence
   * numbers. Intended to be called per server tick in the AuthoritativeServerLoop or equivalent.
   */
  public static void drainAndApplyInputs() {
    Tuple<ClientState, InputMessage> tuple;
    while ((tuple = inputQueue.poll()) != null) {
      ClientState clientState = tuple.a();
      InputMessage msg = tuple.b();

      // TODO: Reconcile inputs based on clientTick vs serverTick and RTT

      // Get hero entity
      Entity entity = clientState.heroEntity().orElse(null);
      if (entity == null) {
        LOGGER.warn("No hero entity for client {}", clientState);
        continue;
      }

      // Apply input
      try {
        switch (msg.action()) {
          case MOVE -> HeroController.moveHero(entity, Vector2.of(msg.point()).direction());
          case MOVE_PATH -> HeroController.moveHeroPath(entity, msg.point());
          case CAST_SKILL -> HeroController.useSkill(entity, msg.point());
          case NEXT_SKILL -> HeroController.changeSkill(entity, true);
          case PREV_SKILL -> HeroController.changeSkill(entity, false);
          case INTERACT -> HeroController.interact(entity, msg.point());
          default -> LOGGER.warn("Unknown action {} for client {}", msg.action(), clientState);
        }
        // On success: Update processed seq and activity
        clientState.updateProcessedSeq(msg.sequence());
        clientState.updateLastActivity();
        LOGGER.trace("Applied input for client {} (action: {})", clientState, msg.action());
      } catch (Exception e) {
        LOGGER.error("Failed to apply input for client {}: {}", clientState, e.getMessage());
      }
    }
  }
}
