package rooms.morphco.level;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import engine.utils.Rectangle;
import engine.utils.components.draw.DepthLayer;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.draw.animation.AnimationConfig;
import engine.utils.components.draw.state.State;
import engine.utils.components.draw.state.StateMachine;
import engine.utils.components.path.SimpleIPath;
import feature.components.CollideComponent;
import feature.components.DecoComponent;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.hud.DialogUtils;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.utils.EntityUtils;
import java.util.List;
import java.util.function.BiConsumer;
import rooms.morphco.util.MorphCoText;

/** Creates the interactable objects of the room at the named points of the level. */
final class MorphCoProps {

  /** Folder of the room's own pixel art. */
  static final String ART = "rooms/morphCo/art/";

  /** Default reach of an interaction in tiles. */
  static final float REACH = 2.1f;

  private MorphCoProps() {}

  /**
   * Turns a decoration into an interactable object.
   *
   * @param name entity name
   * @param position bottom-left corner
   * @param deco decoration
   * @param onInteract called with the object and the acting player
   * @return the added entity
   */
  static Entity deco(
      String name, Point position, Deco deco, BiConsumer<Entity, Entity> onInteract) {
    Entity entity = buildDeco(name, position, deco, onInteract);
    Game.add(entity);
    return entity;
  }

  private static Entity buildDeco(
      String name, Point position, Deco deco, BiConsumer<Entity, Entity> onInteract) {
    Entity entity = DecoFactory.createDeco(position, deco);
    entity.name(name);
    entity.remove(DecoComponent.class);
    entity.add(new InteractionComponent(new Interaction(onInteract, REACH, true)));
    return entity;
  }

  /**
   * Turns a decoration that hangs on a wall or lies on the floor into an interactable object.
   *
   * <p>Office decorations such as paintings and boards come with a solid collider for standing
   * furniture. In front of a wall that collider would block the walkway, so it is removed.
   *
   * @param name entity name
   * @param position bottom-left corner
   * @param deco decoration
   * @param onInteract called with the object and the acting player
   * @return the added entity
   */
  static Entity flatDeco(
      String name, Point position, Deco deco, BiConsumer<Entity, Entity> onInteract) {
    Entity entity = buildDeco(name, position, deco, onInteract);
    entity.remove(CollideComponent.class);
    entity
        .fetch(DrawComponent.class)
        .ifPresent(draw -> draw.depth(DepthLayer.BackgroundDeco.depth()));
    Game.add(entity);
    return entity;
  }

  /**
   * Turns a small decoration that stands on a table into an interactable object.
   *
   * @param name entity name
   * @param position bottom-left corner, on the table top
   * @param tableFront front edge of the table (its bottom-left corner)
   * @param deco decoration
   * @param onInteract called with the object and the acting player
   * @return the added entity
   * @see #onTable(Entity, Point)
   */
  static Entity tableDeco(
      String name,
      Point position,
      Point tableFront,
      Deco deco,
      BiConsumer<Entity, Entity> onInteract) {
    Entity entity = onTable(buildDeco(name, position, deco, onInteract), tableFront);
    Game.add(entity);
    return entity;
  }

  /**
   * Creates an interactable object from a texture that stands on a table.
   *
   * @param name entity name
   * @param position bottom-left corner, on the table top
   * @param tableFront front edge of the table (its bottom-left corner)
   * @param texture texture path inside the assets
   * @param size length of the texture's shorter side in tiles
   * @param onInteract called with the object and the acting player
   * @return the added entity
   * @see #onTable(Entity, Point)
   */
  static Entity tableTexture(
      String name,
      Point position,
      Point tableFront,
      String texture,
      float size,
      BiConsumer<Entity, Entity> onInteract) {
    Entity entity =
        onTable(buildTexture(name, position, texture, size, null, onInteract), tableFront);
    Game.add(entity);
    return entity;
  }

  /**
   * Prepares an object that lies on a table, before it is added to the game.
   *
   * <p>The object has no collider of its own (the table blocks the way). It is drawn in the same
   * layer as the table and the players, but sorted as if it stood at the table's front edge: it
   * lies on the table, and a player in front of the table still covers it.
   *
   * @param entity the object, positioned on the table top
   * @param tableFront front edge of the table (its bottom-left corner)
   * @return the same object
   */
  private static Entity onTable(Entity entity, Point tableFront) {
    entity.remove(CollideComponent.class);
    // The draw order uses the sprite center of an object without a collider.
    float sortY = EntityUtils.getPosition(entity).y();
    entity
        .fetch(DrawComponent.class)
        .ifPresent(
            draw -> {
              draw.depth(DepthLayer.Player.depth());
              draw.sortOffset(tableFront.y() - 0.01f - sortY);
            });
    return entity;
  }

  /**
   * Creates an interactable object from a texture.
   *
   * @param name entity name
   * @param position bottom-left corner
   * @param texture texture path inside the assets
   * @param size length of the texture's shorter side in tiles (the engine scales the shorter side);
   *     the room's own art uses 16 pixels per tile, so a 26x16 display takes {@code 1}
   * @param collider solid collider, or {@code null} for a walkable object
   * @param onInteract called with the object and the acting player
   * @return the added entity
   */
  static Entity texture(
      String name,
      Point position,
      String texture,
      float size,
      Rectangle collider,
      BiConsumer<Entity, Entity> onInteract) {
    Entity entity = buildTexture(name, position, texture, size, collider, onInteract);
    Game.add(entity);
    return entity;
  }

  private static Entity buildTexture(
      String name,
      Point position,
      String texture,
      float size,
      Rectangle collider,
      BiConsumer<Entity, Entity> onInteract) {
    Entity entity = new Entity(name);
    entity.add(new PositionComponent(position));
    DrawComponent draw =
        new DrawComponent(
            new Animation(new SimpleIPath(texture), new AnimationConfig().scaleX(size)));
    draw.depth(collider == null ? DepthLayer.BackgroundDeco.depth() : DepthLayer.Player.depth());
    entity.add(draw);
    if (collider != null) entity.add(new CollideComponent(collider));
    if (onInteract != null) {
      entity.add(new InteractionComponent(new Interaction(onInteract, REACH, true)));
    }
    return entity;
  }

  /**
   * Creates an interactable object with two looks, for example a broken and a repaired screen.
   *
   * @param name entity name
   * @param position bottom-left corner
   * @param off texture of the state {@code off}
   * @param on texture of the state {@code on}
   * @param size length of the texture's shorter side in tiles, see {@link #texture}
   * @param collider solid collider, or {@code null}
   * @param onInteract called with the object and the acting player
   * @return the added entity
   */
  static Entity switchable(
      String name,
      Point position,
      String off,
      String on,
      float size,
      Rectangle collider,
      BiConsumer<Entity, Entity> onInteract) {
    Entity entity = new Entity(name);
    entity.add(new PositionComponent(position));
    AnimationConfig config = new AnimationConfig().scaleX(size);
    State offState = new State("off", new SimpleIPath(off), config);
    State onState = new State("on", new SimpleIPath(on), config);
    StateMachine machine = new StateMachine(List.of(offState, onState));
    machine.addTransition(offState, "on", onState);
    machine.addTransition(onState, "off", offState);
    DrawComponent draw = new DrawComponent(machine);
    draw.depth(collider == null ? DepthLayer.BackgroundDeco.depth() : DepthLayer.Player.depth());
    entity.add(draw);
    if (collider != null) entity.add(new CollideComponent(collider));
    if (onInteract != null) {
      entity.add(new InteractionComponent(new Interaction(onInteract, REACH, true)));
    }
    Game.add(entity);
    return entity;
  }

  /**
   * Switches an object created by {@link #switchable} on.
   *
   * @param entity switchable object
   */
  static void switchOn(Entity entity) {
    if (entity == null) return;
    entity.fetch(DrawComponent.class).ifPresent(draw -> draw.stateMachine().setState("on", null));
  }

  /**
   * Shows a short text popup to one player.
   *
   * @param player acting player
   * @param titleKey title key below {@code morphCo}
   * @param textKey text key below {@code morphCo}
   * @param values optional arguments of the text
   */
  static void popup(Entity player, String titleKey, String textKey, Object... values) {
    DialogUtils.showTextPopup(
        MorphCoText.key(textKey, values), MorphCoText.key(titleKey), player.id());
  }

  /**
   * Shows a short text popup and runs an action after it has been closed.
   *
   * @param player acting player
   * @param titleKey title key below {@code morphCo}
   * @param textKey text key below {@code morphCo}
   * @param then action after the popup
   * @param values optional arguments of the text
   */
  static void popupThen(
      Entity player, String titleKey, String textKey, Runnable then, Object... values) {
    DialogUtils.showTextPopup(
        MorphCoText.key(textKey, values), MorphCoText.key(titleKey), then::run, player.id());
  }
}
