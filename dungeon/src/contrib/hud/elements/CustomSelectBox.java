package contrib.hud.elements;

import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import java.util.function.Function;

public class CustomSelectBox<T> extends SelectBox<T> {

  private Function<T, String> valueFormatter;

  public CustomSelectBox(SelectBoxStyle style) {
    super(style);
  }

  public CustomSelectBox(Skin skin, String styleName) {
    super(skin, styleName);
  }

  public CustomSelectBox(Skin skin) {
    super(skin);
  }

  @Override
  protected String toString(T item) {
    return valueFormatter != null ? valueFormatter.apply(item) : super.toString(item);
  }

  public void setValueFormatter(Function<T, String> valueFormatter) {
    this.valueFormatter = valueFormatter;
  }
}
