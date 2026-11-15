package rooms.morphco.level;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import engine.utils.Rectangle;
import engine.utils.Vector2;
import engine.utils.components.draw.DepthLayer;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.draw.animation.AnimationConfig;
import engine.utils.components.draw.state.State;
import engine.utils.components.draw.state.StateMachine;
import engine.utils.components.path.SimpleIPath;
import feature.components.CollideComponent;
import feature.components.DecoComponent;
import feature.emote.Emote;
import feature.emote.EmoteFactory;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.hud.DialogUtils;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.utils.EntityUtils;
import java.util.List;
import java.util.function.BiConsumer;
import rooms.morphco.util.MorphCoText;

/** Building blocks for the room's objects: their entities, interactions and popups. */
final class MorphCoProps {

  /** Folder of the room's own pixel art. */
  static final String ART = "rooms/morphCo/art/";

  /** Default reach of an interaction in tiles. */
  static final float REACH = 2.1f;

  private static final int EMOTE_DURATION_MS = 3000;

  private MorphCoProps() {}

  /**
   * Builds an object from a shared decoration, not added to the game yet.
   *
   * @param name entity name
   * @param position bottom-left corner
   * @param deco decoration
   * @return the entity
   */
  static Entity buildDeco(String name, Point position, Deco deco) {
    Entity entity = DecoFactory.createDeco(position, deco);
    entity.name(name);
    // The prefab owns persistence; the editor must not save it again as a loose decoration.
    entity.remove(DecoComponent.class);
    return entity;
  }

  /**
   * Prepares an object that lies on a table or hangs in front of a machine, before it is added to
   * the game.
   *
   * <p>The object has no collider of its own (the table or machine blocks the way). It is drawn in
   * the same layer as the furniture and the players, but sorted as if it stood at the given front
   * edge: it lies on the table or hangs on the machine, and a player in front still covers it.
   *
   * @param entity the object, positioned on the table top or on the machine
   * @param tableFront front edge to sort by, e.g. the table's bottom-left corner
   * @return the same object
   */
  static Entity onTable(Entity entity, Point tableFront) {
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
   * Builds an object from a texture, not added to the game yet.
   *
   * @param name entity name
   * @param position bottom-left corner
   * @param texture texture path inside the assets
   * @param size length of the texture's shorter side in tiles (the engine scales the shorter side);
   *     the room's own art uses 16 pixels per tile, so a 26x16 display takes {@code 1}
   * @param collider solid collider, or {@code null} for a walkable object
   * @return the entity
   */
  static Entity buildTexture(
      String name, Point position, String texture, float size, Rectangle collider) {
    Entity entity = new Entity(name);
    entity.add(new PositionComponent(position));
    DrawComponent draw =
        new DrawComponent(
            new Animation(new SimpleIPath(texture), new AnimationConfig().scaleX(size)));
    draw.depth(collider == null ? DepthLayer.BackgroundDeco.depth() : DepthLayer.Player.depth());
    entity.add(draw);
    if (collider != null) entity.add(new CollideComponent(collider));
    return entity;
  }

  /**
   * Builds an object with an "off" and an "on" texture, not added to the game yet.
   *
   * @param name entity name
   * @param position bottom-left corner
   * @param off texture while off
   * @param on texture after {@link #switchOn(Entity)}
   * @param size length of the textures' shorter side in tiles
   * @param collider solid collider, or {@code null} for a walkable object
   * @return the entity
   */
  static Entity buildSwitchable(
      String name, Point position, String off, String on, float size, Rectangle collider) {
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
    return entity;
  }

  /**
   * Makes an object interactable, replacing a previous interaction.
   *
   * @param entity the object
   * @param onInteract called with the object and the acting player
   */
  static void interact(Entity entity, BiConsumer<Entity, Entity> onInteract) {
    entity.remove(InteractionComponent.class);
    entity.add(new InteractionComponent(new Interaction(onInteract, REACH, true)));
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
   * Shows an emote above a character for a few seconds, e.g. a reaction instead of a sentence.
   *
   * <p>The emote is a level entity of the server, so every player sees it. Dialogs cover the middle
   * of the screen, where the characters stand, so an emote belongs after a dialog.
   *
   * @param target character or object the emote belongs to
   * @param emote emote to show
   * @return the emote entity
   */
  static Entity emote(Entity target, Emote emote) {
    return emote(target, emote, EMOTE_DURATION_MS);
  }

  /**
   * Shows an emote above a character or object. The emote follows it, e.g. when the character walks
   * on.
   *
   * @param target character or object the emote belongs to
   * @param emote emote to show
   * @param durationMs how long the emote stays
   * @return the emote entity, e.g. to remove it earlier
   */
  static Entity emote(Entity target, Emote emote, int durationMs) {
    Entity entity = EmoteFactory.createEmote(target, Vector2.of(0f, 0.7f), emote, durationMs);
    Game.add(entity);
    return entity;
  }

  /**
   * Shows a picture from the wall large, then its text.
   *
   * @param player acting player
   * @param art picture in the room's art folder, without extension; its {@code _large} version is
   *     shown
   * @param titleKey key of the text title
   * @param textKey key of the text
   */
  static void picture(Entity player, String art, String titleKey, String textKey) {
    DialogUtils.showImagePopUp(
        ART + art + "_large.png", () -> popup(player, titleKey, textKey), player.id());
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
   * Shows a short text popup and, once it has been closed, the player's reaction as an emote.
   *
   * @param player acting player
   * @param reaction emote above the player
   * @param titleKey title key below {@code morphCo}
   * @param textKey text key below {@code morphCo}
   */
  static void popupReaction(Entity player, Emote reaction, String titleKey, String textKey) {
    popupThen(player, titleKey, textKey, () -> emote(player, reaction));
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
