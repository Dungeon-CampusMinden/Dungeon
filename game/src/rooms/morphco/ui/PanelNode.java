package rooms.morphco.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;

/**
 * A fixed part of a control panel on the canvas: a steel plate, a conveyor belt, a screen or a
 * sheet of paper, with scene2d content inside.
 */
final class PanelNode extends CanvasNode {

  /** How the node looks. */
  enum Look {
    /** Riveted steel plate. */
    PLATE,
    /** Conveyor belt between two rails. */
    BELT,
    /** Monitor with a dark display in a steel bezel. */
    SCREEN,
    /** Sheet of paper with a strip of tape. */
    PAPER
  }

  private final Look look;
  private final float pad;
  private final Table content = new Table();

  /**
   * Creates an empty panel.
   *
   * @param id node ID
   * @param look how the panel looks
   * @param pad space between the panel's edge and its content
   */
  PanelNode(String id, Look look, float pad) {
    super(id, 100, 100);
    this.look = look;
    this.pad = pad;
    movable(false);
    deletable(false);
    selectable(false);
    content.top().left();
    // Panels are scenery: a press must not let the canvas raise them above the chips on them.
    addListener(
        new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            return true;
          }
        });
  }

  /**
   * Returns the content, which the dialog fills with labels and buttons.
   *
   * @return content table
   */
  Table content() {
    return content;
  }

  /**
   * Sizes the panel to the height its content needs at a given width.
   *
   * @param width panel width
   * @return panel height
   */
  float fit(float width) {
    ensureContentBuilt();
    float height = content.getPrefHeight();
    // Wrapped labels learn their width in a layout pass; the second pass measures their height.
    for (int pass = 0; pass < 2; pass++) {
      content.setSize(width - 2 * pad, height);
      content.validate();
      content.invalidate();
      height = content.getPrefHeight();
    }
    size(width, height + 2 * pad);
    return height();
  }

  @Override
  protected void buildContent() {
    addActor(content);
  }

  @Override
  protected void layoutContent() {
    content.setBounds(pad, pad, width() - 2 * pad, height() - 2 * pad);
  }

  @Override
  protected void drawBackground(Batch batch, float alpha) {
    switch (look) {
      case PLATE -> MorphCoUI.plate(batch, MorphCoUI.STEEL, alpha, x(), y(), width(), height());
      case BELT -> MorphCoUI.belt(batch, alpha, x(), y(), width(), height());
      case SCREEN -> {
        MorphCoUI.plate(batch, MorphCoUI.STEEL_DARK, alpha, x(), y(), width(), height());
        MorphCoUI.screen(batch, alpha, x() + 14, y() + 14, width() - 28, height() - 28);
      }
      case PAPER -> {
        CanvasGraphics.fill(batch, Color.BLACK, alpha * .35f, x() + 6, y() - 6, width(), height());
        CanvasGraphics.fill(batch, MorphCoUI.PAPER, alpha, x(), y(), width(), height());
        CanvasGraphics.fill(
            batch,
            MorphCoUI.SIGNAL,
            alpha * .75f,
            x() + width() / 2 - 60,
            y() + height() - 14,
            120,
            24);
      }
    }
  }
}
