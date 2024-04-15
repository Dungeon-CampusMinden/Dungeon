package contrib.entities;

import contrib.components.CollideComponent;
import contrib.components.InteractionComponent;
import contrib.components.InventoryComponent;
import contrib.components.UIComponent;
import contrib.hud.crafting.CraftingGUI;
import contrib.hud.elements.GUICombination;
import contrib.hud.inventory.InventoryGUI;
import contrib.item.Item;
import contrib.utils.components.draw.ChestAnimations;
import contrib.utils.components.item.ItemGenerator;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.draw.CoreAnimations;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** A utility class for building different miscellaneous entities in the game world. */
public final class MiscFactory {

  private static final Random RANDOM = new Random();
  private static final int DEFAULT_CHEST_SIZE = 12;
  private static final int MAX_AMOUNT_OF_ITEMS_ON_RANDOM = 5;
  private static final int MIN_AMOUNT_OF_ITEMS_ON_RANDOM = 1;

  /**
   * This method is used to create a new chest entity. The chest entity can either be empty or
   * filled with random items.
   *
   * <p>The Entity is not added to the game yet.
   *
   * <p>The Entity will have a {@link InteractionComponent}, {@link PositionComponent}, {@link
   * core.components.DrawComponent}, {@link contrib.components.CollideComponent} and {@link
   * contrib.components.InventoryComponent}. It will use the {@link
   * contrib.utils.components.interaction.DropItemsInteraction} on interaction.
   *
   * @param type The type of chest to be created. It can either be RANDOM (filled with random items)
   *     or EMPTY.
   * @return A new Entity representing the chest.
   * @throws IOException if the animation could not be loaded.
   */
  public static Entity newChest(FILL_CHEST type) throws IOException {
    return switch (type) {
      case RANDOM ->
          newChest(
              generateRandomItems(MIN_AMOUNT_OF_ITEMS_ON_RANDOM, MAX_AMOUNT_OF_ITEMS_ON_RANDOM),
              PositionComponent.ILLEGAL_POSITION);
      case EMPTY -> newChest(Set.of(), PositionComponent.ILLEGAL_POSITION);
    };
  }

  private static Set<Item> generateRandomItems(int min, int max) {
    return IntStream.range(0, RANDOM.nextInt(min, max))
        .mapToObj(i -> ItemGenerator.generateItemData())
        .collect(Collectors.toSet());
  }

  /**
   * Get an Entity that can be used as a chest.
   *
   * <p>It will contain the given items.
   *
   * <p>The Entity is not added to the game yet.
   *
   * <p>It will have a {@link InteractionComponent}. {@link PositionComponent}, {@link
   * core.components.DrawComponent}, {@link contrib.components.CollideComponent} and {@link
   * contrib.components.InventoryComponent}. It will use the {@link
   * contrib.utils.components.interaction.DropItemsInteraction} on interaction.
   *
   * @param item Items that should be in the chest.
   * @param position Where should the chest be placed?
   * @return A new Entity.
   * @throws IOException If the animation could not be loaded.
   */
  public static Entity newChest(final Set<Item> item, final Point position) throws IOException {
    final float defaultInteractionRadius = 1f;
    Entity chest = new Entity("chest");

    if (position == null) chest.add(new PositionComponent());
    else chest.add(new PositionComponent(position));
    InventoryComponent ic = new InventoryComponent(DEFAULT_CHEST_SIZE);
    chest.add(ic);
    item.forEach(ic::add);
    chest.add(
        new InteractionComponent(
            defaultInteractionRadius,
            true,
            (interacted, interactor) -> {
              interactor
                  .fetch(InventoryComponent.class)
                  .ifPresent(
                      whoIc -> {
                        UIComponent uiComponent =
                            new UIComponent(
                                new GUICombination(
                                    new InventoryGUI(whoIc), new InventoryGUI("Chest", ic, 6)),
                                true);
                        uiComponent.onClose(
                            () ->
                                interacted
                                    .fetch(DrawComponent.class)
                                    .ifPresent(
                                        interactedDC -> {
                                          // remove all
                                          // prior
                                          // opened
                                          // animations
                                          interactedDC.deQueueByPriority(
                                              ChestAnimations.OPEN_FULL.priority());
                                          if (ic.count() > 0) {
                                            // as long
                                            // as
                                            // there is
                                            // an
                                            // item
                                            // inside
                                            // the chest
                                            // show a
                                            // full
                                            // chest
                                            interactedDC.queueAnimation(ChestAnimations.OPEN_FULL);
                                          } else {
                                            // empty
                                            // chest
                                            // show the
                                            // empty
                                            // animation
                                            interactedDC.queueAnimation(ChestAnimations.OPEN_EMPTY);
                                          }
                                        }));
                        interactor.add(uiComponent);
                      });
              interacted
                  .fetch(DrawComponent.class)
                  .ifPresent(
                      interactedDC -> {
                        // only add opening animation when it is not
                        // finished
                        if (interactedDC
                            .animation(ChestAnimations.OPENING)
                            .map(animation -> !animation.isFinished())
                            .orElse(true)) {
                          interactedDC.queueAnimation(ChestAnimations.OPENING);
                        }
                      });
            }));
    DrawComponent dc = new DrawComponent(new SimpleIPath("objects/treasurechest"));
    var mapping = dc.animationMap();
    // set the closed chest as default idle
    mapping.put(CoreAnimations.IDLE.pathString(), mapping.get(ChestAnimations.CLOSED.pathString()));
    // opening animation should not loop
    mapping.get(ChestAnimations.OPENING.pathString()).loop(false);
    dc.animationMap(mapping);
    // reset Idle Animation
    dc.deQueueByPriority(CoreAnimations.IDLE.priority());
    dc.currentAnimation(CoreAnimations.IDLE);
    chest.add(dc);

    return chest;
  }

  /**
   * Get an Entity that can be used as a crafting cauldron.
   *
   * <p>The Entity is not added to the game yet.
   *
   * @return A new Entity.
   * @throws IOException if the animation could not be loaded.
   */
  public static Entity newCraftingCauldron() throws IOException {
    Entity cauldron = new Entity("cauldron");
    cauldron.add(new PositionComponent());
    cauldron.add(new DrawComponent(new SimpleIPath("objects/cauldron")));
    cauldron.add(new CollideComponent());
    cauldron.add(
        new InteractionComponent(
            1f,
            true,
            (entity, who) ->
                who.fetch(InventoryComponent.class)
                    .ifPresent(
                        ic -> {
                          CraftingGUI craftingGUI = new CraftingGUI(ic);
                          UIComponent component =
                              new UIComponent(
                                  new GUICombination(new InventoryGUI(ic), craftingGUI), true);
                          component.onClose(craftingGUI::cancel);
                          who.add(component);
                        })));
    return cauldron;
  }

  /**
   * Enum representing the types of chest fillings. The chest can either be filled with random items
   * (RANDOM) or be empty (EMPTY).
   *
   * @see MiscFactory#newChest(FILL_CHEST)
   */
  public enum FILL_CHEST {
    RANDOM, // Represents a chest filled with random items
    EMPTY, // Represents an empty chest
  }
}
