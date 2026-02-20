package core.utils;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import core.Game;
import core.game.GameLoop;
import core.game.IResizable;
import core.sound.CoreSounds;
import core.sound.Sounds;

/**
 * A container UI that fills the entire stage and positions a single child actor using alignment
 * anchors and offsets.
 *
 * <p>This class should be placed directly on a Stage. It automatically resizes to fill the stage
 * and repositions the child actor accordingly.
 */
public class BaseContainerUI extends Table implements IResizable {

  private Actor content;
  private int align;
  private float offsetX;
  private float offsetY;
  private boolean grow;

  /**
   * Creates a new BaseContainerUI with the specified content actor.
   *
   * @param content The actor to display.
   */
  public BaseContainerUI(Actor content) {
    this(content, Align.center);
  }

  /**
   * Creates a new BaseContainerUI with the specified content actor and alignment.
   *
   * @param content The actor to display.
   * @param align The alignment/anchor point.
   */
  public BaseContainerUI(Actor content, int align) {
    this(content, align, 0, 0, false, true);
  }

  /**
   * Creates a new BaseContainerUI with the specified content actor, alignment, and offsets.
   *
   * @param content The actor to display.
   * @param grow Whether the content should grow to fill available space in its cell.
   * @param playSound Whether to play the dialog open sound effect when this UI is created.
   */
  public BaseContainerUI(Actor content, boolean grow, boolean playSound) {
    this(content, Align.center, 0, 0, grow, playSound);
  }

  /**
   * Creates a new BaseContainerUI with the specified content actor, alignment, and offsets.
   *
   * @param content The actor to display.
   * @param align The alignment/anchor point.
   * @param offsetX The X offset from the anchor point.
   * @param offsetY The Y offset from the anchor point.
   */
  public BaseContainerUI(
      Actor content, int align, float offsetX, float offsetY, boolean grow, boolean playSound) {
    this.align = align;
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    this.grow = grow;

    this.setFillParent(true);
    setSize(Game.windowWidth(), Game.windowHeight());

    setContent(content);

    if (playSound) Sounds.playLocal(CoreSounds.INTERFACE_DIALOG_OPENED);
  }

  @Override
  protected void setStage(Stage stage) {
    super.setStage(stage);
    if (stage == null) {
      GameLoop.removeResizable(this);
    } else {
      GameLoop.registerResizable(this);
      setSize(stage.getWidth(), stage.getHeight());
      positionContent();
    }
  }

  /**
   * Called when the viewport is resized.
   *
   * @param width The new width of the viewport.
   * @param height The new height of the viewport.
   */
  @Override
  public void onResize(int width, int height) {
    setSize(width, height);
    positionContent();
  }

  /**
   * Sets the content actor to display.
   *
   * @param actor The actor to display.
   */
  public void setContent(Actor actor) {
    if (this.content != null) {
      super.removeActor(this.content);
    }
    this.content = actor;
    if (actor != null) {
      positionContent();
    }
  }

  /**
   * Gets the content actor.
   *
   * @return The content actor, or null if none is set.
   */
  public Actor getContent() {
    return content;
  }

  /**
   * Sets the alignment/anchor point for the content.
   *
   * @param align The alignment (use {@link Align} constants).
   */
  public void setAlign(int align) {
    this.align = align;
    positionContent();
  }

  /**
   * Sets the offset from the anchor point.
   *
   * @param offsetX The X offset.
   * @param offsetY The Y offset.
   */
  public void setOffset(float offsetX, float offsetY) {
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    positionContent();
  }

  /**
   * Sets the alignment and offset.
   *
   * @param align The alignment (use {@link Align} constants).
   * @param offsetX The X offset.
   * @param offsetY The Y offset.
   */
  public void setPlacement(int align, float offsetX, float offsetY) {
    this.align = align;
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    positionContent();
  }

  private void positionContent() {
    if (content == null) {
      return;
    }

    this.clearChildren();
    Cell<Actor> cell = this.add(content).align(align);

    if (grow) cell.grow();

    if ((align & Align.right) != 0) {
      cell.padRight(offsetX);
    } else {
      cell.padLeft(offsetX);
    }

    if ((align & Align.top) != 0) {
      cell.padTop(offsetY);
    } else {
      cell.padBottom(offsetY);
    }
  }
}
