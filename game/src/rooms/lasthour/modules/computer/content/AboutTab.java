package rooms.lasthour.modules.computer.content;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import engine.language.Localization;
import engine.utils.Scene2dElementFactory;
import feature.hud.elements.RichLabel;
import rooms.lasthour.modules.computer.ComputerStateComponent;
import rooms.lasthour.util.Lore;
import rooms.lasthour.util.translation.TranslationKey;

/**
 * Always-visible tab that shows a styled "About" page for the in-game security research company
 * Ciphera Labs.
 */
public class AboutTab extends ComputerTab {

  /** Key for identifying the about tab in the computer dialog. */
  public static final String KEY = "about";

  /** Inner gap between the two body columns. */
  private static final int COLUMN_GAP = 30;

  /**
   * Creates a new AboutTab with the given shared computer state.
   *
   * @param sharedState the shared computer state component
   */
  public AboutTab(ComputerStateComponent sharedState) {
    super(sharedState, KEY, "About", false);
  }

  @Override
  protected void createActors() {
    // ----- Inner content table; will be wrapped in the ScrollPane below. -----
    Table content = new Table();
    content.top();

    // ----- Company logo (same drawable as the LoginTab) -----
    Image companyLogo = new Image(skin, Lore.CompanyDrawable);
    content.add(companyLogo).size(140f).center().padBottom(4f).row();

    // ----- Header: name + subtitle + mission strapline -----
    RichLabel headerLabel =
        new RichLabel(
            Localization.getInstance()
                .getCurrentTranslator()
                .translate(TranslationKey.AboutHeaderText),
            20,
            Color.BLACK,
            false);
    content.add(headerLabel).minWidth(0f).prefWidth(0f).expandX().fillX().padBottom(25f).row();

    // ----- Two-column body: info on the left, Q&A on the right -----
    Table columns = new Table();
    columns.top();

    RichLabel infoLabel =
        new RichLabel(
            Localization.getInstance()
                .getCurrentTranslator()
                .translate(TranslationKey.AboutInfoText),
            18,
            Color.BLACK,
            false);

    RichLabel qnaLabel =
        new RichLabel(
            Localization.getInstance()
                .getCurrentTranslator()
                .translate(TranslationKey.AboutQnAText),
            18,
            Color.BLACK,
            false);

    columns
        .add(infoLabel)
        .top()
        .minWidth(0f)
        .prefWidth(0f)
        .expandX()
        .fillX()
        .uniformX()
        .padRight(COLUMN_GAP / 2f);
    columns
        .add(qnaLabel)
        .top()
        .minWidth(0f)
        .prefWidth(0f)
        .expandX()
        .fillX()
        .uniformX()
        .padLeft(COLUMN_GAP / 2f);

    // Let the two-column body consume spare vertical room; the footer is rendered outside the
    // ScrollPane and remains pinned to the bottom of the tab.
    content.add(columns).minWidth(0f).prefWidth(0f).expand().fill().top().row();

    // ----- Footer -----
    RichLabel footerLabel =
        new RichLabel(
            Localization.getInstance()
                .getCurrentTranslator()
                .translate(TranslationKey.AboutFooterText),
            14,
            Color.BLACK,
            false);

    // ----- Wrap everything in a vertically-scrolling ScrollPane (factory variant has the
    // proper scroll-focus handling and overlay scrollbars). -----
    ScrollPane scroll = Scene2dElementFactory.createScrollPane(content, false, true);
    scroll.setOverscroll(false, false);
    this.add(scroll).grow().row();
    this.add(footerLabel).minWidth(0f).prefWidth(0f).expandX().fillX().padTop(4f);
  }

  @Override
  protected void updateState(ComputerStateComponent newStateComp) {
    // Static page; no state-dependent updates.
  }
}
