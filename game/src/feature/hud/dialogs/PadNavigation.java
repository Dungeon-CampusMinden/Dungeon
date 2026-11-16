package feature.hud.dialogs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;
import engine.input.GameInput;
import engine.input.InputDevice;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;
import java.util.ArrayList;
import java.util.List;

/**
 * Lets the D-pad move the pad cursor from one control of a dialog to the next, so that dialogs made
 * for the mouse work with the pad: A then clicks, picks up and drops at the cursor as it does after
 * moving the cursor with the right stick. A frame marks the control under the cursor.
 *
 * <p>Controls are the innermost visible, touchable actors with an input listener, buttons for
 * example, and the movable nodes of a canvas, whose drags the canvas handles. A control partly
 * covered by others is aimed at where it shows, one hidden entirely is skipped; one scrolled out of
 * its scroll pane is scrolled into view.
 */
final class PadNavigation {
  private static final Color FRAME = Color.valueOf("f0b43c");
  private static final float[][] SPOTS = {
    {.5f, .5f},
    {.5f, .25f},
    {.5f, .75f},
    {.25f, .5f},
    {.75f, .5f},
    {.25f, .25f},
    {.75f, .25f},
    {.25f, .75f},
    {.75f, .75f}
  };

  private PadNavigation() {}

  /**
   * Moves the pad cursor to the nearest control in a direction, preferring controls within 45
   * degrees of it. If the cursor rests on no control, it moves to the nearest control in any
   * direction.
   *
   * @param dialog the dialog's outermost actor
   * @param dx horizontal direction, -1, 0 or 1
   * @param dy vertical direction in stage coordinates (up is 1), -1, 0 or 1
   * @return true if the cursor moved
   */
  static boolean move(Group dialog, int dx, int dy) {
    Stage stage = dialog.getStage();
    if (stage == null) return false;
    Vector2 pointer =
        stage.screenToStageCoordinates(
            new Vector2(GameInput.pointer().x(), GameInput.pointer().y()));
    List<Actor> controls = new ArrayList<>();
    collect(dialog, controls);
    Actor current =
        controls.stream().filter(control -> hits(stage, control, pointer)).findFirst().orElse(null);

    Actor best = null;
    boolean bestInCone = false;
    float bestScore = Float.MAX_VALUE;
    for (Actor control : controls) {
      if (control == current) continue;
      Vector2 point = visiblePoint(stage, control);
      if (point == null) continue;
      Vector2 offset = point.sub(pointer);
      float along = offset.x * dx + offset.y * dy;
      float across = Math.abs(offset.x * dy - offset.y * dx);
      if (current != null && along <= 0) continue;
      boolean inCone = current == null || across <= along;
      float score = current != null ? along + 2 * across : offset.len();
      if (inCone && !bestInCone || inCone == bestInCone && score < bestScore) {
        best = control;
        bestInCone = inCone;
        bestScore = score;
      }
    }
    if (best == null) return false;

    scrollIntoView(best);
    Vector2 point = visiblePoint(stage, best);
    Vector2 screen = stage.stageToScreenCoordinates(point != null ? point : center(best));
    GameInput.resolver().pointer(0, screen.x, screen.y);
    frame(dialog).target = best;
    return true;
  }

  private static void collect(Actor actor, List<Actor> controls) {
    if (!actor.isVisible() || actor.getTouchable() == Touchable.disabled) return;
    int before = controls.size();
    if (actor instanceof Group group) {
      for (Actor child : group.getChildren()) collect(child, controls);
    }
    if (controls.size() == before
        && actor.getTouchable() == Touchable.enabled
        && isControl(actor)) {
      controls.add(actor);
    }
  }

  private static boolean isControl(Actor actor) {
    if (actor instanceof Disableable disableable && disableable.isDisabled()) return false;
    if (actor instanceof CanvasNode node && node.canvas() != null && node.movable()) return true;
    for (EventListener listener : actor.getListeners()) {
      if (listener instanceof InputListener) return true;
    }
    return false;
  }

  private static boolean contains(Actor actor, Vector2 stagePoint) {
    Vector2 local = actor.stageToLocalCoordinates(stagePoint.cpy());
    return local.x >= 0
        && local.x < actor.getWidth()
        && local.y >= 0
        && local.y < actor.getHeight();
  }

  private static Vector2 center(Actor actor) {
    return actor.localToStageCoordinates(new Vector2(actor.getWidth() / 2, actor.getHeight() / 2));
  }

  private static boolean hits(Stage stage, Actor control, Vector2 stagePoint) {
    Actor hit = stage.hit(stagePoint.x, stagePoint.y, true);
    return hit != null && (hit == control || hit.isDescendantOf(control));
  }

  // The first point of a grid over the control that no other actor covers, the centre first; null
  // if it is hidden. A control scrolled out of its pane counts with its centre: the pane scrolls it
  // into view first.
  private static Vector2 visiblePoint(Stage stage, Actor control) {
    Vector2 center = center(control);
    ScrollPane pane = scrollPane(control);
    if (pane != null && !contains(pane, center)) return center;
    for (float[] spot : SPOTS) {
      Vector2 point =
          control.localToStageCoordinates(
              new Vector2(control.getWidth() * spot[0], control.getHeight() * spot[1]));
      if (hits(stage, control, point)) return point;
    }
    return null;
  }

  private static ScrollPane scrollPane(Actor actor) {
    for (Group parent = actor.getParent(); parent != null; parent = parent.getParent()) {
      if (parent instanceof ScrollPane pane) return pane;
    }
    return null;
  }

  private static void scrollIntoView(Actor control) {
    ScrollPane pane = scrollPane(control);
    if (pane == null) return;
    Vector2 corner = control.localToActorCoordinates(pane.getWidget(), new Vector2());
    pane.scrollTo(corner.x, corner.y, control.getWidth(), control.getHeight());
    pane.updateVisualScroll();
    pane.layout();
  }

  private static Frame frame(Group dialog) {
    Frame frame = null;
    for (Actor child : dialog.getChildren()) {
      if (child instanceof Frame existing) frame = existing;
    }
    if (frame == null) {
      frame = new Frame();
      dialog.addActor(frame);
    }
    frame.toFront();
    return frame;
  }

  /** The frame around the control the D-pad moved to, while the pad cursor stays on it. */
  private static final class Frame extends Actor {
    private Actor target;

    private Frame() {
      setTouchable(Touchable.disabled);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
      if (target == null || target.getStage() == null) return;
      if (GameInput.device() != InputDevice.PAD) return;
      Stage stage = target.getStage();
      Vector2 pointer =
          stage.screenToStageCoordinates(
              new Vector2(GameInput.pointer().x(), GameInput.pointer().y()));
      if (!contains(target, pointer)) return;
      Vector2 from = target.localToActorCoordinates(getParent(), new Vector2());
      Vector2 to =
          target.localToActorCoordinates(
              getParent(), new Vector2(target.getWidth(), target.getHeight()));
      float x = getX() + Math.min(from.x, to.x);
      float y = getY() + Math.min(from.y, to.y);
      float width = Math.abs(to.x - from.x);
      float height = Math.abs(to.y - from.y);
      CanvasGraphics.outline(
          batch, Color.BLACK, parentAlpha, x - 5, y - 5, width + 10, height + 10, 2);
      CanvasGraphics.outline(batch, FRAME, parentAlpha, x - 3, y - 3, width + 6, height + 6, 3);
    }
  }
}
