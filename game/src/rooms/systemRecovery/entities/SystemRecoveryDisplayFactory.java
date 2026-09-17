package rooms.systemRecovery.entities;

import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.SimpleIPath;
import feature.components.CollideComponent;
import feature.hud.DialogUtils;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import rooms.systemRecovery.modules.display.DisplayTextComponent;
import rooms.systemRecovery.util.SystemRecoveryText;

/**
 * Builds the interactive displays used by the System Recovery riddles.
 *
 * <p>The factory owns only display construction and text updates. Riddle progression stays in the
 * corresponding riddle controller, and the synchronized display text remains a translation key.
 */
public final class SystemRecoveryDisplayFactory {

  private SystemRecoveryDisplayFactory() {}

  /**
   * Creates a display using the standard module-display interaction title.
   *
   * @param point display position
   * @param textSupplier supplies the current display text
   * @return configured display entity
   */
  public static Entity moduleDisplay(Point point, Supplier<String> textSupplier) {
    return moduleDisplay(
        point,
        textSupplier,
        (display, who) ->
            DialogUtils.showTextPopup(
                display.fetch(DisplayTextComponent.class).orElseThrow().text(),
                SystemRecoveryText.key("world.module.display-title"),
                who.id()));
  }

  /**
   * Creates an interactable riddle display with a custom interaction callback.
   *
   * @param point display position
   * @param textSupplier supplies the current display text
   * @param onInteract callback receiving the display and interacting player
   * @return configured display entity
   */
  public static Entity moduleDisplay(
      Point point, Supplier<String> textSupplier, BiConsumer<Entity, Entity> onInteract) {
    Entity entity = new Entity("module_display");
    entity.add(new PositionComponent(point));
    entity.add(new CollideComponent());
    entity.add(new DrawComponent(new Animation(new SimpleIPath("objects/tech/Screen_device.png"))));
    entity.add(new DisplayTextComponent(textSupplier.get()));
    entity.add(new InteractionComponent(new Interaction(onInteract)));
    return entity;
  }

  /**
   * Creates a display whose popup title is supplied by the caller.
   *
   * @param point display position
   * @param textSupplier supplies the current display text
   * @param title localized title shown when the display is examined
   * @return configured display entity
   */
  public static Entity hintDisplay(Point point, Supplier<String> textSupplier, String title) {
    return moduleDisplay(
        point,
        textSupplier,
        (display, who) ->
            DialogUtils.showTextPopup(
                display.fetch(DisplayTextComponent.class).orElseThrow().text(), title, who.id()));
  }

  /**
   * Updates the synchronized text component of a display.
   *
   * @param display display entity
   * @param text new translation key or display text
   */
  public static void updateDisplayText(Entity display, String text) {
    display.fetch(DisplayTextComponent.class).ifPresent(component -> component.text(text));
  }
}
