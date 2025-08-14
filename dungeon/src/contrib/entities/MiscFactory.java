package contrib.entities;

import contrib.components.CollideComponent;
import contrib.components.InteractionComponent;
import contrib.components.InventoryComponent;
import contrib.components.UIComponent;
import contrib.hud.crafting.CraftingGUI;
import contrib.hud.elements.GUICombination;
import contrib.hud.inventory.InventoryGUI;
import contrib.item.Item;
import contrib.utils.components.item.ItemGenerator;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.animation.AnimationConfig;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
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
   * The {@link ItemGenerator} used to generate random items for chests.
   *
   * @see ItemGenerator
   * @see ItemGenerator#defaultItemGenerator()
   */
  private static ItemGenerator randomItemGenerator = ItemGenerator.defaultItemGenerator();

  /**
   * This method is used to create a new chest entity. The chest will be filled with random items.
   *
   * <p>The Entity is not added to the game yet.
   *
   * <p>The Entity will have a {@link InteractionComponent}, {@link PositionComponent}, {@link
   * core.components.DrawComponent}, {@link contrib.components.CollideComponent} and {@link
   * contrib.components.InventoryComponent}. It will use the {@link
   * contrib.utils.components.interaction.DropItemsInteraction} on interaction.
   *
   * @return A new Entity representing the chest.
   * @throws IOException if the animation could not be loaded.
   * @see MiscFactory#generateRandomItems(int, int) generateRandomItems
   */
  public static Entity newChest() throws IOException {
    return newChest(FILL_CHEST.RANDOM);
  }

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
        .mapToObj(i -> randomItemGenerator.generateItemData())
        .collect(Collectors.toSet());
  }

  /**
   * Sets the ItemGenerator used to generate random items for monsters upon death.
   *
   * @param randomItemGenerator The ItemGenerator to use for generating random items.
   * @see ItemGenerator
   */
  public static void randomItemGenerator(ItemGenerator randomItemGenerator) {
    MiscFactory.randomItemGenerator = randomItemGenerator;
  }

  /**
   * Gets the ItemGenerator used to generate random items for randomly filled chests.
   *
   * <p>The default ItemGenerator is {@link ItemGenerator#defaultItemGenerator()}.
   *
   * @return The current ItemGenerator used for generating random items.
   * @see ItemGenerator
   */
  public static ItemGenerator randomItemGenerator() {
    return randomItemGenerator;
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

    Map<String, Animation> animationMap =
        Animation.loadAnimationSpritesheet(new SimpleIPath("objects/treasurechest"));
    State stClosed = State.fromMap(animationMap, "closed");
    State stOpening = State.fromMap(animationMap, "opening");
    State stOpen = FillState.fromMap(animationMap, "open");
    StateMachine sm = new StateMachine(Arrays.asList(stClosed, stOpening, stOpen));
    sm.addTransition(stClosed, "open", stOpening);

    // Automatically transition to open state when opening animation is finished playing
    sm.addEpsilonTransition(stOpening, State::isAnimationFinished, stOpen, () -> ic.count() == 0);

    // If we didn't have a direct way of controlling when the full/empty check should happen, an
    // epsilon transition to itself would still work
    //    sm.addEpsilonTransition(stOpen, s -> (boolean)s.getData() != (ic.count() == 0), stOpen, ()
    // -> ic.count() == 0);
    DrawComponent dc = new DrawComponent(sm);
    chest.add(dc);

    chest.add(
        new InteractionComponent(
            defaultInteractionRadius,
            true,
            (interacted, interactor) ->
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
                                            // only add opening animation when it is not finished.
                                            // If
                                            // we close the GUI before the opening
                                            // animation finishes, the epsilon transition will
                                            // handle
                                            // setting the data correctly
                                            if (!interactedDC
                                                .stateMachine()
                                                .getCurrentStateName()
                                                .equals("opening")) {
                                              interactedDC.sendSignal("open", ic.count() == 0);
                                            }
                                          }));
                          interactor.add(uiComponent);
                        })));

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
    /** Represents a chest filled with random items. */
    RANDOM,
    /** Represents an empty chest. */
    EMPTY,
  }

  private static class FillState extends State {
    private Animation empty;

    public FillState(String name, IPath pathFull, IPath pathEmpty, AnimationConfig config) {
      super(name, pathFull, config);
      empty = new Animation(pathEmpty, config);
    }

    public FillState(String name, IPath pathFull, IPath pathEmpty) {
      this(name, pathFull, pathEmpty, null);
    }

    public FillState(String name, Animation full, Animation empty) {
      super(name, full);
      this.empty = empty;
    }

    @Override
    public Animation getAnimation() {
      boolean isEmpty = (boolean) data;
      return isEmpty ? empty : super.getAnimation();
    }

    public static FillState fromMap(Map<String, Animation> animationMap, String name) {
      return new FillState(
          name, animationMap.get(name + "_full"), animationMap.get(name + "_empty"));
    }
  }
}
