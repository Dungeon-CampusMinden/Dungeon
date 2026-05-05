package modules.computer.content;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import contrib.hud.elements.RichLabel;
import core.utils.Scene2dElementFactory;
import modules.computer.ComputerStateComponent;
import util.Lore;

/**
 * Always-visible tab that shows a styled "About" page for the in-game security research company
 * Ciphera Labs.
 */
public class AboutTab extends ComputerTab {

  /** Key for identifying the about tab in the computer dialog. */
  public static final String KEY = "about";

  /** Inner gap between the two body columns. */
  private static final int COLUMN_GAP = 30;

  /** Rich text content for the page header section. */
  private static final String HEADER_TEXT =
      "[align=center]"
          + "[size=56][color=#3399ff]Ciphera[/color]"
          + " [color=#aa00aa]Labs[/color][/size][n]"
          + "[size=18][color=gray][img=items/rpg/potion_lightblue.png] Security Research"
          + " [color=#888888]·[/color] Established 1984"
          + " [img=items/rpg/potion_teal.png][/color][/size][n][n]"
          + "[size=22][color=#222244]"
          + "[word-space=1.7]Decoding tomorrow.  Defending today.[word-space=1.0]"
          + "[/color][/size]";

  /** Rich text content for the left body column. */
  private static final String INFO_TEXT =
      "[line-space=1.25][size=26][color=#222244]"
          + "[img=items/rpg/shield_gold.png] Who we are[/color][/size][n]"
          + "[size=18]For more than four decades, [color=#3399ff]Ciphera[/color]"
          + " [color=#aa00aa]Labs[/color] has"
          + " been at the forefront of cryptographic research and digital security.[n]"
          + "Founded in 1984 in a single basement office, we have grown into an"
          + " internationally recognised institute trusted by governments, financial"
          + " institutions, hospitals, and independent innovators alike.[n]"
          + "Our research spans secure communications, intrusion analysis,"
          + " hardware security modules, biometric authentication, and the next"
          + " generation of post-quantum cryptography.[/size]"
          + "[n][n]"
          + "[size=26][color=#222244]"
          + "[img=items/rpg/key1.png] Leadership[/color][/size][n]"
          + "[size=18]Under the guidance of our CEO,"
          + " [color=#aa0000]Adrian Voss[/color], [color=#3399ff]Ciphera[/color]"
          + " [color=#aa00aa]Labs[/color] continues to push the"
          + " boundaries of what is possible in cybersecurity research while upholding the"
          + " highest standards of scientific integrity and transparency.[/size]";

  /** Rich text content for the right body column. */
  private static final String QNA_TEXT =
      "[line-space=1.15][size=26][color=#222244]"
          + "[img=items/rpg/potion_purple.png] A few quick questions for our CEO"
          + " [img=items/rpg/potion_red.png][/color][/size][n]"
          + "[line-space=1.45][size=18]"
          + "[color=#3399ff]Q:[/color] How long has Ciphera Labs been operating?[n]"
          + "[color=#aa00aa]A:[/color] Since [color=#222244]1984[/color]"
          + " - over forty years of dedicated research.[n][n]"
          + "[color=#3399ff]Q:[/color] What drives your team?[n]"
          + "[color=#aa00aa]A:[/color] A relentless commitment to a safer digital world."
          + "[n][n]"
          + "[color=#3399ff]Q:[/color] What is your favorite color?[n]"
          + "[color=#aa00aa]A:[/color] [color=blue]Blue[/color]."
          + "[/size][line-space=1.0]";

  /** Rich text content for the footer. */
  private static final String FOOTER_TEXT =
      "[align=center][size=14][color=light_gray]"
          + "© 1984 - 2026 Ciphera Labs | Secure by design, transparent by principle."
          + "[/color][/size]";

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
    RichLabel headerLabel = new RichLabel(HEADER_TEXT, 20, Color.BLACK, false);
    content.add(headerLabel).minWidth(0f).prefWidth(0f).expandX().fillX().padBottom(25f).row();

    // ----- Two-column body: info on the left, Q&A on the right -----
    Table columns = new Table();
    columns.top();

    RichLabel infoLabel = new RichLabel(INFO_TEXT, 18, Color.BLACK, false);

    RichLabel qnaLabel = new RichLabel(QNA_TEXT, 18, Color.BLACK, false);

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
    RichLabel footerLabel = new RichLabel(FOOTER_TEXT, 14, Color.BLACK, false);

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
