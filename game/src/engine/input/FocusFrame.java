package engine.input;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import feature.canvas.CanvasGraphics;
import feature.interaction.InteractionDialogColors;

/**
 * The frame around the control a key or the D-pad moved to. It is an actor in the group that holds
 * the control, drawn above the group's other children and below anything that lies above the group,
 * and it shows only inside the control's scroll pane. Also holds what moving such a focus needs:
 * whether a point hits a control and scrolling a control into view.
 */
public abstract class FocusFrame extends Actor {
  private Actor target;

  protected FocusFrame() {
    setTouchable(Touchable.disabled);
  }

  /**
   * Gets the framed control.
   *
   * @return the control, or null
   */
  Actor target() {
    return target;
  }

  /**
   * Sets the framed control.
   *
   * @param target the control, a descendant of the group holding the frame
   */
  public void target(Actor target) {
    this.target = target;
  }

  /**
   * Checks whether the frame shows right now.
   *
   * @param stage the stage of the target
   * @param target the framed control
   * @return true to draw the frame
   */
  protected abstract boolean shows(Stage stage, Actor target);

  @Override
  protected void setParent(Group parent) {
    // A group that swaps its children drops the frame with them, and the framed control with it.
    if (parent == null && target != null) {
      exit(target, null);
      target = null;
    }
    super.setParent(parent);
  }

  /**
   * Tells a control that the focus left it, like the mouse leaving it, so that a button can take
   * back what a first press started. Only the control hears it; a scroll pane around it keeps the
   * scroll focus.
   *
   * @param control the control the focus leaves
   * @param next the control the focus moves to, or null
   */
  static void exit(Actor control, Actor next) {
    InputEvent exit = new InputEvent();
    exit.setType(InputEvent.Type.exit);
    exit.setStage(control.getStage());
    exit.setPointer(-1);
    exit.setRelatedActor(next);
    exit.setBubbles(false);
    control.fire(exit);
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    Stage stage = target == null ? null : target.getStage();
    if (stage == null || !shows(stage, target)) return;
    // A control wider than its scroll pane shows only inside it, and so does its frame; a control
    // scrolled out of it shows no frame.
    Rectangle box = bounds(target);
    ScrollPane pane = scrollPane(target);
    if (pane != null && !Intersector.intersectRectangles(bounds(target), bounds(pane), box)) return;
    float x = getX() + box.x;
    float y = getY() + box.y;
    CanvasGraphics.outline(
        batch, Color.BLACK, parentAlpha, x - 5, y - 5, box.width + 10, box.height + 10, 2);
    CanvasGraphics.outline(
        batch,
        InteractionDialogColors.SELECTED,
        parentAlpha,
        x - 3,
        y - 3,
        box.width + 6,
        box.height + 6,
        3);
  }

  // The bounds of an actor in the group, which holds the frame.
  private Rectangle bounds(Actor actor) {
    Vector2 from = actor.localToActorCoordinates(getParent(), new Vector2());
    Vector2 to =
        actor.localToActorCoordinates(
            getParent(), new Vector2(actor.getWidth(), actor.getHeight()));
    return new Rectangle(
        Math.min(from.x, to.x),
        Math.min(from.y, to.y),
        Math.abs(to.x - from.x),
        Math.abs(to.y - from.y));
  }

  /**
   * Checks whether a point on the stage reaches a control, that is, no other actor covers the
   * control there.
   *
   * @param stage the stage
   * @param control the control
   * @param stagePoint the point in stage coordinates
   * @return true if a press at the point lands on the control or one of its children
   */
  public static boolean hits(Stage stage, Actor control, Vector2 stagePoint) {
    Actor hit = stage.hit(stagePoint.x, stagePoint.y, true);
    return hit != null && (hit == control || hit.isDescendantOf(control));
  }

  /**
   * Finds the innermost scroll pane around an actor.
   *
   * @param actor the actor
   * @return the scroll pane, or null
   */
  public static ScrollPane scrollPane(Actor actor) {
    for (Group parent = actor.getParent(); parent != null; parent = parent.getParent()) {
      if (parent instanceof ScrollPane pane) return pane;
    }
    return null;
  }

  /**
   * Scrolls the innermost scroll pane around a control so that the control shows in it, at once.
   *
   * @param control the control
   */
  public static void scrollIntoView(Actor control) {
    ScrollPane pane = scrollPane(control);
    if (pane == null) return;
    Vector2 corner = control.localToActorCoordinates(pane.getWidget(), new Vector2());
    // libGDX's scrollTo takes the top edge as y, despite its y-up coordinates; with the bottom edge
    // it misses by the control's height.
    pane.scrollTo(
        corner.x, corner.y + control.getHeight(), control.getWidth(), control.getHeight());
    pane.updateVisualScroll();
    pane.layout();
  }
}
