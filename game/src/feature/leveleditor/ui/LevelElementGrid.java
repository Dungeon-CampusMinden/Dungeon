package feature.leveleditor.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import engine.level.utils.LevelElement;
import engine.utils.FontHelper;
import engine.utils.Scene2dElementFactory;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.path.SimpleIPath;
import feature.hud.UIUtils;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * A grid of image buttons, one for every {@link LevelElement}.
 *
 * <p>Left clicking a button selects the element as the primary ({@code [L]}) element, right
 * clicking it selects the element as the secondary ({@code [R]}) element. Both selections are
 * highlighted with their own button style and a small marker in the corner of the button.
 */
public class LevelElementGrid extends Table {

  private static final String STYLE_DEFAULT = "default";
  private static final String STYLE_PRIMARY = "green";
  private static final String STYLE_SECONDARY = "blue-outline";
  private static final float BUTTON_SIZE = 60f;
  private static final float IMAGE_SIZE = 34f;
  private static final int MARKER_FONT_SIZE = 12;

  private final ImageButton[] buttons = new ImageButton[LevelElement.values().length];
  private final Label[] markers = new Label[LevelElement.values().length];
  private final IntSupplier primary;
  private final IntSupplier secondary;

  private int lastPrimary = Integer.MIN_VALUE;
  private int lastSecondary = Integer.MIN_VALUE;

  /**
   * Creates a new grid of {@link LevelElement} buttons.
   *
   * @param columns the number of buttons per row.
   * @param texturePath maps a level element to the path of the texture shown on its button.
   * @param primary supplies the ordinal of the currently selected primary element.
   * @param secondary supplies the ordinal of the currently selected secondary element.
   * @param onPrimary called with the ordinal of the element that was left clicked.
   * @param onSecondary called with the ordinal of the element that was right clicked.
   */
  public LevelElementGrid(
      int columns,
      Function<LevelElement, String> texturePath,
      IntSupplier primary,
      IntSupplier secondary,
      IntConsumer onPrimary,
      IntConsumer onSecondary) {
    this.primary = primary;
    this.secondary = secondary;
    defaults().pad(3f).size(BUTTON_SIZE);

    Label.LabelStyle markerStyle =
        new Label.LabelStyle(
            FontHelper.getFont(
                Scene2dElementFactory.FONT_PATH_BOLD, MARKER_FONT_SIZE, Color.BLACK, 0),
            Color.BLACK.cpy());

    LevelElement[] elements = LevelElement.values();
    for (int i = 0; i < elements.length; i++) {
      final int ordinal = i;
      ImageButton button = new ImageButton(style(STYLE_DEFAULT, texturePath.apply(elements[i])));
      button.getImageCell().size(IMAGE_SIZE);
      button.getImage().setScaling(Scaling.fit);
      button.addListener(
          new ClickListener(Input.Buttons.LEFT) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              onPrimary.accept(ordinal);
              refresh();
            }
          });
      button.addListener(
          new ClickListener(Input.Buttons.RIGHT) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              onSecondary.accept(ordinal);
              refresh();
            }
          });
      buttons[i] = button;

      Label marker = new Label("", markerStyle);
      markers[i] = marker;
      Table overlay = new Table();
      overlay.setTouchable(Touchable.disabled);
      overlay.add(marker).expand().top().left().pad(2f);

      Stack stack = new Stack();
      stack.add(button);
      stack.add(overlay);
      add(stack);
      if ((i + 1) % columns == 0) row();
    }
    // Fill up the last row so all buttons keep the same column width
    int remaining = (columns - (elements.length % columns)) % columns;
    for (int i = 0; i < remaining; i++) {
      add();
    }
    row();
    forceRefresh();
  }

  /** Updates the button styles and markers if the selection changed. */
  public void refresh() {
    if (primary.getAsInt() == lastPrimary && secondary.getAsInt() == lastSecondary) return;
    forceRefresh();
  }

  private void forceRefresh() {
    lastPrimary = primary.getAsInt();
    lastSecondary = secondary.getAsInt();
    int count = LevelElement.values().length;
    for (int i = 0; i < count; i++) {
      boolean isPrimary = i == Math.floorMod(lastPrimary, count);
      boolean isSecondary = i == Math.floorMod(lastSecondary, count);

      String styleName = STYLE_DEFAULT;
      if (isPrimary) {
        styleName = STYLE_PRIMARY;
      } else if (isSecondary) {
        styleName = STYLE_SECONDARY;
      }
      applyBackground(buttons[i], styleName);

      String marker = (isPrimary ? "L" : "") + (isSecondary ? "R" : "");
      markers[i].setText(marker.isEmpty() ? "" : "[" + marker + "]");
      markers[i].setAlignment(Align.topLeft);
    }
  }

  private static void applyBackground(ImageButton button, String styleName) {
    TextButton.TextButtonStyle base =
        UIUtils.defaultSkin().get(styleName, TextButton.TextButtonStyle.class);
    ImageButton.ImageButtonStyle style = button.getStyle();
    style.up = base.up;
    style.down = base.down;
    style.over = base.over;
    button.setStyle(style);
  }

  private static ImageButton.ImageButtonStyle style(String styleName, String texturePath) {
    TextButton.TextButtonStyle base =
        UIUtils.defaultSkin().get(styleName, TextButton.TextButtonStyle.class);
    ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
    style.up = base.up;
    style.down = base.down;
    style.over = base.over;
    Texture texture = TextureMap.instance().textureAt(new SimpleIPath(texturePath));
    style.imageUp = new TextureRegionDrawable(new TextureRegion(texture));
    return style;
  }
}
