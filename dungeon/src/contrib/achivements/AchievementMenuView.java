package contrib.achivements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import contrib.hud.UIUtils;
import core.utils.FontSpec;
import core.utils.Scene2dElementFactory;
import core.utils.components.draw.TextureMap;
import core.utils.components.path.SimpleIPath;
import java.util.List;

/** Scene2D factory for the achievements page in the main menu. */
public final class AchievementMenuView {

  private static final String TITLE_FONT = "fonts/Roboto-Bold.ttf";
  private static final String BODY_FONT = "fonts/Roboto-Regular.ttf";
  private static final String HIDDEN_NAME = "Hidden Achievement";
  private static final String HIDDEN_DESCRIPTION = "Unlock this achievement to reveal it.";
  private static final Color TEXT = Color.BLACK;
  private static final Color LOCKED_TEXT = new Color(0.38f, 0.38f, 0.38f, 1f);
  private static final Color LOCKED_ICON = new Color(0.35f, 0.35f, 0.35f, 1f);
  private static final String FALLBACK_IMAGE = "animation/missing_texture.png";

  private AchievementMenuView() {}

  /**
   * Builds the achievements page.
   *
   * @param backButton button used to return to the main menu
   * @return achievements page
   */
  public static Table build(TextButton backButton) {
    Label title =
        Scene2dElementFactory.createLabel("Achievements", FontSpec.of(TITLE_FONT, 48, Color.BLACK));

    Table achievementList = new Table();
    achievementList.defaults().growX().padBottom(10f);
    List<Achievement> achievements = AchievementSystem.menuAchievements();
    for (Achievement achievement : achievements) {
      achievementList.add(row(achievement)).row();
    }

    ScrollPane scrollPane = Scene2dElementFactory.createScrollPane(achievementList, false, true);

    Table menu = new Table();
    menu.add(title).padBottom(15).align(Align.center).row();
    menu.add(Scene2dElementFactory.createHorizontalDivider()).growX().padBottom(5).row();
    menu.add(scrollPane).width(650).height(420).row();
    menu.add(Scene2dElementFactory.createHorizontalDivider()).growX().padTop(5).row();
    menu.add(backButton).width(300).padTop(15).padBottom(15).row();
    return menu;
  }

  private static Table row(Achievement achievement) {
    boolean unlocked = achievement.unlocked();
    boolean hidden = achievement.hidden() && !unlocked;
    String name = hidden ? HIDDEN_NAME : achievement.name();
    String description = hidden ? HIDDEN_DESCRIPTION : achievement.neschreibung();
    Color textColor = unlocked ? TEXT : LOCKED_TEXT;

    Image icon = new Image(texture(achievement.imagePath()));
    icon.setScaling(com.badlogic.gdx.utils.Scaling.fit);
    if (!unlocked) {
      icon.setColor(LOCKED_ICON);
    }

    Label nameLabel =
        Scene2dElementFactory.createLabel(name, FontSpec.of(TITLE_FONT, 24, textColor));
    Label descriptionLabel =
        Scene2dElementFactory.createLabel(description, FontSpec.of(BODY_FONT, 18, textColor));
    descriptionLabel.setWrap(true);

    Table text = new Table();
    text.left();
    text.add(nameLabel).left().growX().row();
    text.add(descriptionLabel).left().width(500).padTop(4f).row();

    Table row = new Table(UIUtils.defaultSkin());
    row.setBackground("generic-area");
    row.pad(12f);
    row.add(icon).size(64f).padRight(14f);
    row.add(text).growX().left();
    return row;
  }

  private static Texture texture(String path) {
    Texture texture = TextureMap.instance().textureAt(new SimpleIPath(path));
    if (texture != null) {
      return texture;
    }
    return TextureMap.instance().textureAt(new SimpleIPath(FALLBACK_IMAGE));
  }
}
