package contrib.entities;

import contrib.components.*;
import contrib.components.InventoryComponent;
import contrib.components.SkillComponent;
import contrib.components.UIComponent;
import contrib.hud.IInventoryHolder;
import contrib.hud.UIUtils;
import contrib.hud.elements.GUICombination;
import contrib.hud.inventory.InventoryGUI;
import contrib.item.Item;
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
import core.network.messages.c2s.InventoryUIMessage;
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
      // When moving diagonally, this function is called once per axis. On the first call, only the
      // force for one axis might be present, so we need to ensure we only scale by the speed of the
      // moving axes.
      // TODO: Inputs should be batched and calculated together (explanation in PR #2724)
      Vector2 unitSpeed =
          Vector2.of(direction.x() != 0 ? speed.x() : 0, direction.y() != 0 ? speed.y() : 0);
      updatedForce = updatedForce.normalize().scale(unitSpeed.length());
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

    boolean isUIOpen = false;
    UIComponent uiComponent = hero.fetch(UIComponent.class).orElse(null);
    if (uiComponent != null) {
      if (uiComponent.dialog() instanceof GUICombination) {
        hero.remove(UIComponent.class);
      }
    } else {
      hero.add(new UIComponent(new GUICombination(new InventoryGUI(ic)), true));
      isUIOpen = true;
    }
    LOGGER.trace("Inventory UI for hero {} is now {}", hero.id(), isUIOpen ? "open" : "closed");

    if (!Game.network().isServer()) {
      Game.network().send((short) 0, new InventoryUIMessage(isUIOpen), true);
    }
    InventoryGUI.setInventoryOpen(hero, isUIOpen);
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
   * Drops the item at the specified inventory slot from the hero entity's inventory.
   *
   * @param entity the hero entity dropping the item
   * @param sourceInv the target inventory component from which to drop the item
   * @param itemSlot the inventory slot index of the item to drop
   * @throws IllegalArgumentException if there is no item in the specified slot
   */
  public static void dropItem(Entity entity, InventoryComponent sourceInv, int itemSlot) {
    LOGGER.debug("Entity {} dropping item from slot {}", entity.id(), itemSlot);

    Item item =
        sourceInv
            .remove(itemSlot)
            .orElseThrow(() -> new IllegalArgumentException("No item in slot " + itemSlot));

    Point dropPosition = Game.positionOf(entity).orElseThrow();
    boolean success = item.drop(dropPosition).isPresent();
    if (!success) {
      LOGGER.warn("Failed to drop item {} from slot {} for entity {}", item, itemSlot, entity.id());
      returnItemToInventory(sourceInv, item, itemSlot, entity);
    }
  }

  /**
   * Moves or swaps an item between two inventory slots. Supports moving items within the player's
   * own inventory or between the player's inventory and another open inventory (e.g., a chest or
   * NPC shop).
   *
   * <p><b>Parameter encoding:</b>
   *
   * <ul>
   *   <li>Positive slot indices refer to the other inventory (e.g., chest, shop)
   *   <li>Negative slot indices refer to the player's own inventory, where -1 maps to slot 0, -2 to
   *       slot 1, etc. (formula: actual slot = -(index + 1))
   * </ul>
   *
   * <p><b>Step-by-step process:</b>
   *
   * <ol>
   *   <li>Validates that the player has a UI component and an accessible inventory
   *   <li>Retrieves the player's inventory and the other open inventory (if any)
   *   <li>Decodes the slot indices based on the encoding scheme to determine source and target
   *       inventories
   *   <li>Removes the item from the source inventory
   *   <li>If the target slot is occupied, swaps the items; otherwise, places the item in the target
   *       slot
   * </ol>
   *
   * @param player the player entity performing the item movement
   * @param fromSlot the source slot index (negative for player inventory, positive for other)
   * @param toSlot the target slot index (negative for player inventory, positive for other)
   * @return true if the move/swap was successful, false otherwise
   */
  public static boolean moveItem(Entity player, int fromSlot, int toSlot) {
    LOGGER.debug("Entity {} moving item from slot {} to slot {}", player.id(), fromSlot, toSlot);

    Optional<UIComponent> uiComp = player.fetch(UIComponent.class);
    if (uiComp.isEmpty()) {
      LOGGER.debug("No UI component found for entity {}", player.id());
      return false;
    }

    Optional<InventoryComponent> playerInv =
        InventoryGUI.getPlayerInventoryGUI(player).map(IInventoryHolder::inventoryComponent);
    if (playerInv.isEmpty()) {
      LOGGER.debug("No inventory GUI found for entity {}", player.id());
      return false;
    }

    Optional<InventoryComponent> otherInv =
        UIUtils.getInventoriesFromUI(uiComp.get())
            .filter(inv -> inv != playerInv.get())
            .findFirst();

    // negative source, mean the player's own inventory
    // e.g. -1 means slot 0 in player's inventory
    InventoryComponent source = fromSlot < 0 ? playerInv.get() : otherInv.orElse(playerInv.get());
    InventoryComponent target = toSlot < 0 ? playerInv.get() : otherInv.orElse(playerInv.get());

    int adjustedFromSlot = fromSlot < 0 ? -(fromSlot + 1) : fromSlot;
    int adjustedToSlot = toSlot < 0 ? -(toSlot + 1) : toSlot;

    Optional<Item> itemToMove = source.remove(adjustedFromSlot);
    if (itemToMove.isEmpty()) {
      LOGGER.debug(
          "No item in slot {} of source inventory for entity {}", adjustedFromSlot, player.id());
      return false;
    }

    target
        .get(adjustedToSlot)
        .ifPresentOrElse(
            existingItem -> {
              // Slot occupied, swap items
              source.set(adjustedFromSlot, existingItem);
              target.set(adjustedToSlot, itemToMove.get());
              LOGGER.debug(
                  "Swapped items between inventories in slots {} and {} for entity {}",
                  adjustedFromSlot,
                  adjustedToSlot,
                  player.id());
            },
            () -> {
              target.set(adjustedToSlot, itemToMove.get());
              LOGGER.debug(
                  "Moved item to slot {} of target inventory for entity {}",
                  adjustedToSlot,
                  player.id());
            });

    return true;
  }

  /**
   * Uses the item at the specified inventory slot for the given entity.
   *
   * @param entity the entity using the item
   * @param itemSlot the inventory slot index of the item to use
   * @return true if the item was used successfully, false if no item was found in the slot
   */
  public static boolean useItem(Entity entity, int itemSlot) {
    LOGGER.debug("Entity {} using item from slot {}", entity.id(), itemSlot);

    InventoryComponent inventory =
        entity
            .fetch(InventoryComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, InventoryComponent.class));
    Item item = inventory.get(itemSlot).orElse(null);
    if (item == null) {
      LOGGER.debug("No item in slot {} for entity {}", itemSlot, entity.id());
      return false;
    }

    item.use(entity);
    return true;
  }

  /**
   * Attempts to return the item to the inventory, preferring the original slot if empty.
   *
   * <p>If the original slot is occupied, tries to add the item to any available slot. If both
   * attempts fail, logs an error.
   *
   * @param inventory the inventory component
   * @param item the item to return
   * @param itemSlot the original slot index
   * @param entity the entity for logging
   */
  private static void returnItemToInventory(
      InventoryComponent inventory, Item item, int itemSlot, Entity entity) {
    try {
      if (inventory.get(itemSlot).isEmpty()) {
        inventory.set(itemSlot, item);
      } else if (!inventory.add(item)) {
        throw new RuntimeException("No space to return item to inventory");
      }
    } catch (RuntimeException e) {
      LOGGER.error(
          "Failed to return item {} to inventory for entity {}: {}",
          item,
          entity.id(),
          e.getMessage(),
          e);
    }
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
      Entity playerEntity = clientState.playerEntity().orElse(null);
      if (playerEntity == null) {
        LOGGER.warn("No hero entity for client {}", clientState);
        continue;
      }

      // Apply input
      try {
        switch (msg.action()) {
          case MOVE -> {
            CharacterClass heroClass =
                playerEntity.fetch(CharacterClassComponent.class).orElseThrow().characterClass();
            HeroController.moveHero(
                playerEntity, Vector2.of(msg.point()).direction(), heroClass.speed());
          }
          case CAST_SKILL -> HeroController.useSkill(playerEntity, msg.point());
          case NEXT_SKILL -> HeroController.changeSkill(playerEntity, true);
          case PREV_SKILL -> HeroController.changeSkill(playerEntity, false);
          case INTERACT -> HeroController.interact(playerEntity, msg.point());
          case INV_DROP -> {
            Optional<InventoryComponent> invComp =
                clientState
                    .playerEntity()
                    .flatMap(InventoryGUI::getPlayerInventoryGUI)
                    .map(InventoryGUI::inventoryComponent);
            if (invComp.isEmpty()) {
              LOGGER.warn(
                  "No inventory component found for entity {} to drop item", playerEntity.id());
              break;
            }
            int itemIndex = (int) msg.point().x();
            HeroController.dropItem(playerEntity, invComp.get(), itemIndex);
          }
          case INV_MOVE -> {
            int fromIndex = (int) msg.point().x();
            int toIndex = (int) msg.point().y();
            HeroController.moveItem(playerEntity, fromIndex, toIndex);
          }
          case INV_USE -> {
            int itemIndex = (int) msg.point().x();
            HeroController.useItem(playerEntity, itemIndex);
          }
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
