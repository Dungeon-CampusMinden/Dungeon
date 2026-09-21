package rooms.programming.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import engine.Game;
import engine.utils.FontHelper;
import engine.utils.Scene2dElementFactory;
import feature.canvas.CanvasGraphics;

/** Shared visual language for Nox's workbenches, references and observation controls. */
final class ProgrammingUI {
  static final Color INK = Color.valueOf("171c1e");
  static final Color SURFACE = Color.valueOf("283033");
  static final Color TEXT = Color.valueOf("eee5d4");
  static final Color MUTED = Color.valueOf("a6aeaa");
  static final Color GOLD = Color.valueOf("d6ae70");
  static final Color SUCCESS = Color.valueOf("9ccca8");
  static final Color ERROR = Color.valueOf("ffb0a3");

  private ProgrammingUI() {}

  static float density() {
    return Math.max(
        (float) Gdx.graphics.getBackBufferWidth() / Math.max(1, Game.windowWidth()),
        (float) Gdx.graphics.getBackBufferHeight() / Math.max(1, Game.windowHeight()));
  }

  static Label label(String text, int size, Color color) {
    int raster = Math.max(1, (int) Math.ceil(size * density()));
    Label label = Scene2dElementFactory.createLabel(text, raster, color);
    label.setFontScale((float) size / raster);
    label.setWrap(true);
    label.setAlignment(Align.topLeft);
    label.setTouchable(Touchable.disabled);
    return label;
  }

  static Label zoomLabel(String text, int size, Color color) {
    Label label = new ZoomLabel(text, size, color, Scene2dElementFactory.FONT_PATH);
    label.setWrap(true);
    label.setAlignment(Align.topLeft);
    label.setTouchable(Touchable.disabled);
    return label;
  }

  /** Syntax colors share the same zoom-aware typography as ordinary canvas labels. */
  static Label zoomSyntaxLabel(String text, int size) {
    Label label = new ZoomLabel(text, size, Color.WHITE, Scene2dElementFactory.FONT_PATH, true);
    label.setWrap(true);
    label.setAlignment(Align.topLeft);
    label.setTouchable(Touchable.disabled);
    return label;
  }

  /** Rerasterizes text for the canvas or book transform without changing its logical font size. */
  private static final class ZoomLabel extends Label {
    private final int size;
    private final Color ink;
    private final String path;
    private final Vector2 origin = new Vector2();
    private final Vector2 axis = new Vector2();
    private int raster;
    private final boolean markup;

    private ZoomLabel(String text, int size, Color ink, String path) {
      this(text, size, ink, path, false);
    }

    private ZoomLabel(String text, int size, Color ink, String path, boolean markup) {
      super(text, new LabelStyle(FontHelper.getFont(path, size, ink, 0, Color.BLACK), null));
      this.markup = markup;
      this.size = size;
      this.ink = ink.cpy();
      this.path = path;
      raster = size;
      getBitmapFontCache().setUseIntegerPositions(false);
    }

    @Override
    public float getPrefWidth() {
      var data = getStyle().font.getData();
      boolean previous = data.markupEnabled;
      data.markupEnabled = markup;
      try {
        return super.getPrefWidth();
      } finally {
        data.markupEnabled = previous;
      }
    }

    @Override
    public float getPrefHeight() {
      var data = getStyle().font.getData();
      boolean previous = data.markupEnabled;
      data.markupEnabled = markup;
      try {
        return super.getPrefHeight();
      } finally {
        data.markupEnabled = previous;
      }
    }

    @Override
    public void layout() {
      var data = getStyle().font.getData();
      boolean previous = data.markupEnabled;
      data.markupEnabled = markup;
      try {
        super.layout();
      } finally {
        data.markupEnabled = previous;
      }
    }

    @Override
    public void draw(Batch batch, float alpha) {
      localToStageCoordinates(origin.set(0, 0));
      localToStageCoordinates(axis.set(128, 0));
      // Quarter-step densities reuse cached fonts while dragging the window edge.
      float resolution =
          Math.max(1, (float) Math.ceil(axis.dst(origin) / 128 * density() * 4 - .0001f) / 4);
      int required = (int) Math.ceil(size * resolution);
      if (raster != required) {
        raster = required;
        LabelStyle style = new LabelStyle(getStyle());
        style.font = FontHelper.getFont(path, raster, ink, 0, Color.BLACK);
        setStyle(style);
        setFontScale((float) size / raster);
        getBitmapFontCache().setUseIntegerPositions(false);
      }
      super.draw(batch, alpha);
    }
  }

  static TextButton zoomButton(String caption, boolean primary, Runnable action) {
    TextButton button = button(caption, primary, action);
    Label label = new ZoomLabel(caption, 17, Color.WHITE, Scene2dElementFactory.FONT_PATH_BOLD);
    label.setAlignment(Align.center);
    label.setWrap(true);
    label.setTouchable(Touchable.disabled);
    button.setLabel(label);
    return button;
  }

  static BaseDrawable background(Color color, boolean border) {
    return new BaseDrawable() {
      @Override
      public void draw(Batch batch, float x, float y, float width, float height) {
        CanvasGraphics.fill(batch, color, batch.getColor().a, x, y, width, height);
        if (border) {
          var transform = batch.getTransformMatrix();
          float scale =
              Math.min(
                  (float)
                      Math.hypot(
                          transform.val[com.badlogic.gdx.math.Matrix4.M00],
                          transform.val[com.badlogic.gdx.math.Matrix4.M10]),
                  (float)
                      Math.hypot(
                          transform.val[com.badlogic.gdx.math.Matrix4.M01],
                          transform.val[com.badlogic.gdx.math.Matrix4.M11]));
          // Zoomed-out one-unit borders can fall between raster pixels and disappear.
          float thickness = scale > 0 && scale < 1 ? 1.25f / scale : 1;
          CanvasGraphics.outline(batch, GOLD, .65f, x, y, width, height, thickness);
        }
      }
    };
  }

  static TextButton button(String caption, boolean primary, Runnable action) {
    int raster = Math.max(1, (int) Math.ceil(17 * density()));
    TextButton button = Scene2dElementFactory.createButton(caption, "default", raster);
    TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(button.getStyle());
    style.font = button.getLabel().getStyle().font;
    style.fontColor = primary ? GOLD : TEXT;
    style.disabledFontColor = MUTED;
    style.up = background(Color.valueOf("343d3e"), primary);
    style.over = background(Color.valueOf("41443b"), true);
    style.down = background(Color.valueOf("55503c"), true);
    style.disabled = background(SURFACE, false);
    button.setStyle(style);
    button.getLabel().setFontScale(17f / raster);
    button.getLabel().setWrap(true);
    button.pad(10);
    button.setDisabled(action == null);
    if (action != null)
      button.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              action.run();
            }
          });
    return button;
  }

  static TextButton referenceButton(String caption, Runnable action) {
    TextButton button = button(caption, false, action);
    button.getStyle().checked = background(Color.valueOf("343d3e"), true);
    button.getStyle().checkedFontColor = GOLD;
    button.setProgrammaticChangeEvents(false);
    return button;
  }

  static Table header(String title, Table actions, Runnable close) {
    Label heading = label(title, 27, TEXT);
    TextButton leave = button("Zurück zum Raum", false, close);
    return new Table() {
      private boolean compact;

      {
        arrange();
      }

      private void arrange() {
        compact = Game.windowWidth() < 1180 && actions.getChildren().size > 0;
        clearChildren();
        add(heading).growX().padRight(20);
        if (compact) {
          add(leave).width(190).minHeight(44).row();
          add(actions).colspan(2).right().padTop(10);
        } else {
          add(actions).right();
          add(leave).width(190).minHeight(44).padLeft(10);
        }
      }

      @Override
      public void layout() {
        if (compact != (Game.windowWidth() < 1180 && actions.getChildren().size > 0)) arrange();
        super.layout();
      }
    };
  }
}
