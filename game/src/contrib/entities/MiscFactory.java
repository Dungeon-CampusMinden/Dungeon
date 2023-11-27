package contrib.entities;

import contrib.components.CollideComponent;
import contrib.components.InteractionComponent;
import contrib.components.InventoryComponent;
import contrib.components.UIComponent;
import contrib.hud.GUICombination;
import contrib.hud.crafting.CraftingGUI;
import contrib.hud.inventory.InventoryGUI;
import contrib.item.Item;
import contrib.utils.components.draw.ChestAnimations;
import contrib.utils.components.interaction.DropItemsInteraction;
import contrib.utils.components.item.ItemGenerator;

import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.draw.CoreAnimations;

import java.io.IOException;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** A utility class for building different miscellaneous entities in the game world. */
public class MiscFactory {

    private static final Random RANDOM = new Random();

    /**
     * Create a new Entity that can be used as a chest.
     *
     * <p>It will have a {@link InteractionComponent}. {@link PositionComponent}, {@link
     * DrawComponent}, {@link CollideComponent} and {@link InventoryComponent}. It will use the
     * {@link DropItemsInteraction} on interaction.
     *
     * <p>{@link ItemGenerator} is used to generate random items
     *
     * @return Created Entity
     */
    public static Entity newChest() throws IOException {
        Set<Item> items =
                IntStream.range(0, RANDOM.nextInt(1, 3))
                        .mapToObj(i -> ItemGenerator.generateItemData())
                        .collect(Collectors.toSet());
        return newChest(items, PositionComponent.ILLEGAL_POSITION);
    }

    /**
     * Create a new Entity that can be used as a chest.
     *
     * <p>It will have a {@link InteractionComponent}. {@link PositionComponent}, {@link
     * DrawComponent}, {@link CollideComponent} and {@link InventoryComponent}. It will use the
     * {@link DropItemsInteraction} on interaction.
     *
     * @param item The {@link Item} for the Items inside the chest.
     * @param position The position of the chest.
     * @return Created Entity
     */
    public static Entity newChest(Set<Item> item, Point position) throws IOException {
        final float defaultInteractionRadius = 1f;
        Entity chest = new Entity("chest");

        if (position == null) chest.add(new PositionComponent());
        else chest.add(new PositionComponent(position));
        InventoryComponent ic = new InventoryComponent(item.size());
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
                                                                        new InventoryGUI(whoIc),
                                                                        new InventoryGUI(ic)),
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
                                                                                    interactedDC
                                                                                            .deQueueByPriority(
                                                                                                    ChestAnimations
                                                                                                            .OPEN_FULL
                                                                                                            .priority());
                                                                                    if (ic.count()
                                                                                            > 0) {
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
                                                                                        interactedDC
                                                                                                .queueAnimation(
                                                                                                        ChestAnimations
                                                                                                                .OPEN_FULL);
                                                                                    } else {
                                                                                        // empty
                                                                                        // chest
                                                                                        // show the
                                                                                        // empty
                                                                                        // animation
                                                                                        interactedDC
                                                                                                .queueAnimation(
                                                                                                        ChestAnimations
                                                                                                                .OPEN_EMPTY);
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
                                                        .getAnimation(ChestAnimations.OPENING)
                                                        .map(animation -> !animation.isFinished())
                                                        .orElse(true)) {
                                                    interactedDC.queueAnimation(
                                                            ChestAnimations.OPENING);
                                                }
                                            });
                        }));
        DrawComponent dc = new DrawComponent("objects/treasurechest");
        var mapping = dc.animationMap();
        // set the closed chest as default idle
        mapping.put(
                CoreAnimations.IDLE.pathString(), mapping.get(ChestAnimations.CLOSED.pathString()));
        // opening animation should not loop
        mapping.get(ChestAnimations.OPENING.pathString()).loop(false);
        // reset Idle Animation
        dc.deQueueByPriority(CoreAnimations.IDLE.priority());
        dc.currentAnimation(CoreAnimations.IDLE);
        chest.add(dc);

        return chest;
    }

    /**
     * Create a new Entity that can be used as a crafting cauldron.
     *
     * @return Created Entity
     * @throws IOException if the textures do not exist
     */
    public static Entity newCraftingCauldron() throws IOException {
        Entity cauldron = new Entity("cauldron");
        cauldron.add(new PositionComponent());
        cauldron.add(new DrawComponent("objects/cauldron"));
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
                                                                    new GUICombination(
                                                                            new InventoryGUI(ic),
                                                                            craftingGUI),
                                                                    true);
                                                    component.onClose(craftingGUI::cancel);
                                                    who.add(component);
                                                })));
        return cauldron;
    }
}
