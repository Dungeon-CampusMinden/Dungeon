package feature.leveleditor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import engine.utils.FontHelper;
import engine.utils.Scene2dElementFactory;
import feature.hud.dialogs.DialogDesign;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A labeled select box setting backed by a getter and setter.
 *
 * @param <T> the type of value in the select box.
 */
public class SelectSetting<T> extends Table {

  private static final int FONT_SIZE = 16;

  private final Supplier<T> getter;
  private final SelectBox<T> selectBox;

  /**
   * Creates a select setting.
   *
   * @param label the text shown in front of the select box.
   * @param values the selectable values.
   * @param getter supplies the current value.
   * @param setter applies a selected value.
   * @param formatter converts values to display text.
   */
  public SelectSetting(
      String label,
      T[] values,
      Supplier<T> getter,
      java.util.function.Consumer<T> setter,
      Function<T, String> formatter) {
    this.getter = getter;
    selectBox = Scene2dElementFactory.createSelectBox(formatter);
    BitmapFont font = FontHelper.getFont(DialogDesign.DIALOG_FONT_SPEC_NORMAL.withSize(FONT_SIZE));
    selectBox.getStyle().font = font;
    selectBox.getList().getStyle().font = font;
    selectBox.setItems(values);
    selectBox.setSelected(getter.get());
    selectBox.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            setter.accept(selectBox.getSelected());
          }
        });

    add(Scene2dElementFactory.createLabel(label, 16, Color.BLACK)).growX().left();
    add(selectBox).width(200f).right();
  }

  /** Synchronizes the displayed value with the current setting. */
  public void refresh() {
    T current = getter.get();
    if (current != null && current != selectBox.getSelected()) {
      selectBox.setSelected(current);
    }
  }
}
