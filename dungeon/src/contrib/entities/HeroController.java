package contrib.entities;

import contrib.components.*;
import contrib.components.InventoryComponent;
import contrib.components.SkillComponent;
import contrib.components.UIComponent;
import contrib.hud.elements.GUICombination;
import contrib.hud.inventory.InventoryGUI;
import contrib.modules.interaction.InteractionComponent;
import contrib.utils.EntityUtils;
import contrib.utils.components.skill.cursorSkill.CursorSkill;
import contrib.utils.components.skill.projectileSkill.ProjectileSkill;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.components.VelocityComponent;
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
 * @see HeroBuilder HeroFactory for creating hero entities
 */
public class HeroController {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(HeroController.class);
  private static final Queue<Tuple<ClientState, InputMessage>> inputQueue =
      new ConcurrentLinkedQueue<>();

  /** The ID for the movement force. */
  public static final String MOVEMENT_ID = "Movement";

  private HeroController() {}

  /**
   * Moves the hero entity in the specified direction by applying a force to its VelocityComponent.
   *
   * @param hero the hero entity to move
   * @param direction the direction to move the hero
   * @param speed the speed vector to scale the movement force
   */
  public static void moveHero(Entity hero, Direction direction, Vector2 speed) {
    LOGGER.debug("Moving hero {} in direction {}", hero.id(), direction);

    VelocityComponent vc =
        hero.fetch(VelocityComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, VelocityComponent.class));

    Optional<Vector2> existingForceOpt = vc.force(MOVEMENT_ID);
    Vector2 newForce = speed.scale(direction);

    Vector2 updatedForce =
        existingForceOpt.map(existing -> existing.add(newForce)).orElse(newForce);

    if (updatedForce.lengthSquared() > 0) {
      updatedForce = updatedForce.normalize().scale(speed.length());
      vc.applyForce(MOVEMENT_ID, updatedForce);
    }
  }

  /**
   * Uses the hero's active skill targeting the specified point. If the active skill is a
   * CursorSkill or ProjectileSkill, sets the target position accordingly before executing the
   * skill.
   *
   * @param hero the hero entity using the skill
   * @param target the target point for the skill (can be null if not applicable)
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
                try {
                  projSkill.endPointSupplier().get(); // test if supplier wants cursor position
                } catch (IllegalStateException ignored) {
                  projSkill.endPointSupplier(() -> target);
                }
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

    // Try finding interactable at the exact point first
    Optional<Entity> target =
        Game.tileAt(point)
            .map(Game::entityAtTile)
            .orElse(Stream.empty())
            .filter(e -> e.fetch(InteractionComponent.class).isPresent())
            .findFirst();

    // If nothing found at point, search in 1-tile radius around hero
    if (target.isEmpty()) {
      LOGGER.trace(
          "No interactable found at point {}, searching in radius around hero {}",
          point,
          hero.id());
      target =
          LevelUtils.tilesInRange(EntityUtils.getPosition(hero), 1f).stream()
              .flatMap(Game::entityAtTile)
              .filter(e -> e.fetch(InteractionComponent.class).isPresent())
              .findFirst();
    }

    // Trigger interaction if entity found
    target.ifPresentOrElse(
        entity -> {
          InteractionComponent ic = entity.fetch(InteractionComponent.class).orElseThrow();
          LOGGER.trace("Hero {} interacting with entity {}", hero.id(), entity.id());
          ic.triggerInteraction(entity, hero);
        },
        () -> LOGGER.trace("No interactable entity found for hero {} to interact with", hero.id()));
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
   * Toggles the inventory UI for the hero entity. If the inventory UI is currently open, it will be
   * closed; if it is closed, it will be opened.
   *
   * @param hero the hero entity whose inventory UI is to be toggled
   */
  public static void toggleInventory(Entity hero) {
    LOGGER.debug("Hero {} toggling inventory UI", hero.id());
    Optional<InventoryComponent> invComp = hero.fetch(InventoryComponent.class);
    Optional<PlayerComponent> playerComp = hero.fetch(PlayerComponent.class);
    if (invComp.isEmpty() || playerComp.isEmpty()) {
      LOGGER.error("Trying to open inventory for non-player entity or entity without inventory.");
      return;
    }
    var ic = invComp.get();
    var pc = playerComp.get();

    if (pc.openDialogs()) {
      LOGGER.debug("Player {} has other dialogs open, cannot toggle inventory.", hero.id());
      return;
    }

    UIComponent uiComponent = hero.fetch(UIComponent.class).orElse(null);
    if (uiComponent != null) {
      if (uiComponent.dialog() instanceof GUICombination) {
        hero.remove(UIComponent.class);
      }
    } else {
      hero.add(new UIComponent(new GUICombination(new InventoryGUI(ic)), true));
    }
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
      Entity entity = clientState.playerEntity().orElse(null);
      if (entity == null) {
        LOGGER.warn("No hero entity for client {}", clientState);
        continue;
      }

      // Apply input
      try {
        switch (msg.action()) {
          case MOVE -> {
            CharacterClass heroClass =
                entity.fetch(CharacterClassComponent.class).orElseThrow().characterClass();
            HeroController.moveHero(entity, Vector2.of(msg.point()).direction(), heroClass.speed());
          }
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
        LOGGER.error("Failed to apply input for client {}: {}", clientState, e.getMessage(), e);
      }
    }
  }
}
