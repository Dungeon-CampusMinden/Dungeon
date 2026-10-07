package feature.credits;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import engine.Game;
import engine.language.Language;
import engine.utils.FontSpec;
import engine.utils.Scene2dElementFactory;
import java.util.Objects;

/** Builds the credits contents shared by the main-menu view and the in-game ECS dialog. */
public final class CreditsView {

  private static final Color TEXT_COLOR = Color.BLACK;
  private static final float CONTENT_WIDTH = 700f;
  private static final float CONTENT_HEIGHT = 360f;

  private CreditsView() {}

  /**
   * Builds the main-menu credits view, including its back button.
   *
   * @param definition credits data to display
   * @param skin menu skin
   * @param backLabel localized back-button label
   * @param onBack action for the back button
   * @return the credits view table
   */
  public static Table buildMenuView(
      CreditsDefinition definition, Skin skin, String backLabel, Runnable onBack) {
    Objects.requireNonNull(definition, "definition");
    Objects.requireNonNull(skin, "skin");
    Objects.requireNonNull(onBack, "onBack");

    Table view = new Table();
    ScrollPane scrollPane = contentPane(definition, skin);
    scrollPane.setFadeScrollBars(false);
    scrollPane.setScrollbarsVisible(true);
    view.add(scrollPane).width(CONTENT_WIDTH).height(CONTENT_HEIGHT).row();

    TextButton backButton = new TextButton(backLabel, skin, "green");
    backButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            onBack.run();
          }
        });
    view.add(backButton).width(300).padTop(16).padBottom(8);
    return view;
  }

  /**
   * Creates the scrollable contents without menu or dialog controls.
   *
   * @param definition credits data to display
   * @param skin skin for the scroll pane
   * @return scroll pane containing the localized title, sections, and entries
   */
  public static ScrollPane contentPane(CreditsDefinition definition, Skin skin) {
    Objects.requireNonNull(definition, "definition");
    Objects.requireNonNull(skin, "skin");
    Language language = Game.localization().currentLanguage();
    Table content = new Table();
    content.top().left();

    Label title = label(definition.title().text(language), 30, true);
    title.setWrap(true);
    title.setAlignment(Align.center);
    content.add(title).growX().padBottom(18).row();

    for (CreditsDefinition.Section section : definition.sections()) {
      Label headline = label(section.headline().text(language), 23, true);
      headline.setWrap(true);
      content.add(headline).growX().padBottom(6).left().row();
      content.add(Scene2dElementFactory.createHorizontalDivider()).growX().padBottom(10).row();

      for (CreditsDefinition.Entry entry : section.entries()) {
        addEntry(content, entry, language);
      }
      content.add().height(14).row();
    }

    ScrollPane pane = Scene2dElementFactory.createScrollPane(content, false, true);
    pane.setScrollbarsOnTop(false);
    pane.setFadeScrollBars(false);
    pane.setScrollbarsVisible(true);
    pane.setScrollingDisabled(true, false);
    return pane;
  }

  /**
   * Returns the preferred height for credits content in a dialog.
   *
   * @param viewportHeight available stage height
   * @return dialog content height
   */
  public static float dialogContentHeight(float viewportHeight) {
    return Math.max(180f, Math.min(CONTENT_HEIGHT, viewportHeight - 220f));
  }

  private static void addEntry(Table content, CreditsDefinition.Entry entry, Language language) {
    if (entry.name() != null) {
      Label name = label(entry.name(), 20, true);
      name.setWrap(true);
      content.add(name).growX().left().row();
    }
    if (entry.role() != null) {
      addBodyText(content, entry.role().text(language));
    }
    if (entry.description() != null) {
      addBodyText(content, entry.description().text(language));
    }
    content.add().height(8).row();
  }

  private static void addBodyText(Table content, String text) {
    Label body = label(text, 17, false);
    body.setWrap(true);
    content.add(body).growX().left().padBottom(3).row();
  }

  private static Label label(String text, int size, boolean bold) {
    String font = bold ? "fonts/Lexend-Bold.ttf" : "fonts/Lexend-Regular.ttf";
    Label label = Scene2dElementFactory.createLabel(text, FontSpec.of(font, size, TEXT_COLOR));
    label.setAlignment(Align.left);
    return label;
  }
}
