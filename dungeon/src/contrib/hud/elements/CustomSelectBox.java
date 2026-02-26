package contrib.hud.elements;

import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import java.util.function.Function;

/**
 * CustomSelectBox is an extension of the standard SelectBox that allows for custom formatting of
 * the displayed values.
 *
 * @param <T> the type of items contained in the SelectBox
 */
public class CustomSelectBox<T> extends SelectBox<T> {

  private Function<T, String> valueFormatter;

  /**
   * Creates a new CustomSelectBox with the specified style.
   *
   * @param style the style to use for this SelectBox
   */
  public CustomSelectBox(SelectBoxStyle style) {
    super(style);
  }

  /**
   * Creates a new CustomSelectBox with the specified skin and style name.
   *
   * @param skin the skin to use for this SelectBox
   * @param styleName the name of the style to use from the skin
   */
  public CustomSelectBox(Skin skin, String styleName) {
    super(skin, styleName);
  }

  /**
   * Creates a new CustomSelectBox with the specified skin.
   *
   * @param skin the skin to use for this SelectBox
   */
  public CustomSelectBox(Skin skin) {
    super(skin);
  }

  @Override
  protected String toString(T item) {
    return valueFormatter != null ? valueFormatter.apply(item) : super.toString(item);
  }

  /**
   * Sets the value formatter function that converts items to their display string.
   *
   * @param valueFormatter a function that takes an item of type T and returns its display string
   */
  public void setValueFormatter(Function<T, String> valueFormatter) {
    this.valueFormatter = valueFormatter;
  }
}
