package contrib.achivements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.HeadlessDialogGroup;
import core.Game;
import core.utils.BaseContainerUI;
import core.utils.FontSpec;
import core.utils.Scene2dElementFactory;
import core.utils.components.draw.TextureMap;
import core.utils.components.path.SimpleIPath;

/** Achievement unlock popup. */
public final class AchievementPopup {

  public static final String KEY_IMAGE_PATH = "achievement.imagePath";
  public static final String KEY_NAME = "achievement.name";
  public static final String KEY_DESCRIPTION = "achievement.description";
  private static final String FALLBACK_IMAGE = "animation/missing_texture.png";

  private AchievementPopup() {}

  /**
   * Builds the popup from a dialog context.
   *
   * @param ctx dialog context
   * @return popup group
   */
  public static Group build(DialogContext ctx) {
    String imagePath = ctx.require(KEY_IMAGE_PATH, String.class);
    String name = ctx.require(KEY_NAME, String.class);
    String description = ctx.require(KEY_DESCRIPTION, String.class);

    if (Game.isHeadless()) {
      return new HeadlessDialogGroup("Achievement unlocked", name + "\n" + description);
    }

    Table card = new Table(UIUtils.defaultSkin());
    card.setBackground("window_background_big");
    card.pad(14f);

    Image icon = new Image(texture(imagePath));
    icon.setScaling(com.badlogic.gdx.utils.Scaling.fit);
    Label header =
        Scene2dElementFactory.createLabel(
            "Achievement Unlocked", FontSpec.of("fonts/Roboto-Bold.ttf", 18, Color.DARK_GRAY));
    Label nameLabel =
        Scene2dElementFactory.createLabel(
            name, FontSpec.of("fonts/Roboto-Bold.ttf", 24, Color.BLACK));
    Label descriptionLabel =
        Scene2dElementFactory.createLabel(
            description, FontSpec.of("fonts/Roboto-Regular.ttf", 16, Color.BLACK));
    descriptionLabel.setWrap(true);

    Table text = new Table();
    text.left();
    text.add(header).left().row();
    text.add(nameLabel).left().padTop(2f).row();
    text.add(descriptionLabel).left().width(300f).padTop(4f).row();

    card.add(icon).size(64f).padRight(12f);
    card.add(text).width(310f).left();
    card.pack();
    card.addAction(Actions.sequence(Actions.delay(3.7f), Actions.fadeOut(0.7f)));

    return new BaseContainerUI(card, Align.topRight, -24f, -24f, false, true);
  }

  private static Texture texture(String path) {
    Texture texture = TextureMap.instance().textureAt(new SimpleIPath(path));
    if (texture != null) {
      return texture;
    }
    return TextureMap.instance().textureAt(new SimpleIPath(FALLBACK_IMAGE));
  }
}
