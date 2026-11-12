package feature.leveleditor.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import engine.utils.Cursors;
import feature.entities.deco.Deco;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Setting choosing a decoration: a button showing the chosen decoration, with the name of the
 * setting and of the decoration next to it. Clicking either opens the {@link DecoSelector}.
 *
 * <p>The names sit next to the button instead of above it, so the setting is not higher than a
 * select box with its label.
 */
public final class DecoSetting extends EditorSetting {

  private static final float BUTTON_SIZE = 48f;
  private static final float IMAGE_SIZE = 38f;

  private final Supplier<Deco> getter;
  private final DecoButton button;
  private final Label name;
  private Deco shown;

  /**
   * Creates a decoration setting.
   *
   * @param label setting name
   * @param choices decorations to choose from, in display order
   * @param getter supplies the current decoration
   * @param setter applies a chosen decoration
   */
  public DecoSetting(
      String label, List<Deco> choices, Supplier<Deco> getter, Consumer<Deco> setter) {
    this(label, choices, false, getter, setter);
  }

  /**
   * Creates a decoration setting that optionally allows no decoration.
   *
   * @param label setting name
   * @param choices decorations to choose from, in display order
   * @param optional whether no decoration can be chosen, which is passed as {@code null}
   * @param getter supplies the current decoration, {@code null} for none
   * @param setter applies a chosen decoration, {@code null} for none
   */
  public DecoSetting(
      String label,
      List<Deco> choices,
      boolean optional,
      Supplier<Deco> getter,
      Consumer<Deco> setter) {
    super(label);
    this.getter = Objects.requireNonNull(getter, "getter");
    Objects.requireNonNull(setter, "setter");
    // The label of the setting is placed next to the button, not above it.
    clearChildren();
    shown = getter.get();
    Runnable open =
        () ->
            DecoSelector.show(
                choices,
                optional,
                getter.get(),
                deco -> {
                  setter.accept(deco);
                  refresh();
                });

    Table names = new Table();
    names.setTouchable(Touchable.enabled);
    names.setUserObject(Cursors.INTERACT);
    names.add(EditorTheme.label(label, 14, EditorTheme.TEXT_MUTED)).left().row();
    name = EditorTheme.label(DecoSelector.name(shown), 15, EditorTheme.TEXT);
    name.setEllipsis(true);
    names.add(name).left().growX().minWidth(0f).padTop(1f);
    names.addListener(
        new ClickListener(Input.Buttons.LEFT) {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            open.run();
          }
        });

    button = new DecoButton(shown, BUTTON_SIZE, IMAGE_SIZE, false);
    button.onClick(open);
    add(names).growX().minWidth(0f);
    add(button).size(BUTTON_SIZE).padLeft(8f);
  }

  /** Shows the current decoration, if it changed. */
  public void refresh() {
    Deco current = getter.get();
    if (current == shown) return;
    shown = current;
    button.setDeco(current);
    name.setText(DecoSelector.name(current));
  }
}
