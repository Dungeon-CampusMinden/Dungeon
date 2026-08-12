package feature.achievements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import engine.Game;
import engine.language.Translation;
import engine.sound.SoundSpec;
import engine.utils.BaseContainerUI;
import engine.utils.FontSpec;
import engine.utils.Scene2dElementFactory;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.path.SimpleIPath;
import engine.utils.settings.ClientSettings;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;

/** Achievement unlock popup. */
public final class AchievementPopup {

  public static final String KEY_IMAGE_PATH = "achievement.imagePath";
  public static final String KEY_ID = "achievement.id";
  private static final float CORNER_MARGIN = 40f;
  private static final String TITLE_FONT = "fonts/Roboto-Bold.ttf";
  private static final String BODY_FONT = "fonts/Roboto-Regular.ttf";
  private static final String T_UNLOCKED = "unlocked";
  private static final String UNLOCK_SOUND = "kenney_ui_confirmation_004";
  private static final float UNLOCK_SOUND_VOLUME = 1f;
  private static final String FALLBACK_IMAGE = "animation/missing_texture.png";
  private static final Translation TRANS = new Translation("achievement.popup");
  private static final Translation TEXT_TRANS = new Translation();
  private static final String ACHIEVEMENT_TRANSLATION_PREFIX = "achievements.";

  private AchievementPopup() {}

  /**
   * Builds the popup from a dialog context.
   *
   * @param ctx dialog context
   * @return popup group
   */
  public static Group build(DialogContext ctx) {
    String imagePath = ctx.require(KEY_IMAGE_PATH, String.class);
    String achievementId = ctx.require(KEY_ID, String.class);
    String name = localized(translationKey(achievementId, "name"), achievementId);
    String description = localized(translationKey(achievementId, "description"), "");
    if (Game.isHeadless()) {
      return new HeadlessDialogGroup("Achievement unlocked", name + "\n" + description);
    }

    boolean newlyUnlocked = AchievementManager.markUnlockedFromPopup(achievementId);
    if (!newlyUnlocked) {
      return new HeadlessDialogGroup();
    }

    Table card = new Table(UIUtils.defaultSkin());
    card.setBackground("window_background_big");
    card.pad(14f);

    Image icon = new Image(texture(imagePath));
    icon.setScaling(Scaling.fit);
    Label header =
        Scene2dElementFactory.createLabel(
            TRANS.text(T_UNLOCKED), FontSpec.of(TITLE_FONT, 18, Color.DARK_GRAY));
    Label nameLabel =
        Scene2dElementFactory.createLabel(name, FontSpec.of(TITLE_FONT, 24, Color.BLACK));
    Label descriptionLabel =
        Scene2dElementFactory.createLabel(description, FontSpec.of(BODY_FONT, 16, Color.BLACK));
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
    playUnlockSound();

    return new AlwaysOnTopContainer(card);
  }

  private static void playUnlockSound() {
    // This build method runs on the client that renders the popup. In multiplayer, the server sends
    // only the dialog context; the receiving client builds the popup and plays this sound locally.
    Game.audio()
        .playGlobal(
            new SoundSpec.Builder(UNLOCK_SOUND)
                .volume(effectiveEffectsVolume() * UNLOCK_SOUND_VOLUME));
  }

  private static float effectiveEffectsVolume() {
    return (ClientSettings.masterVolume() / 100f) * (ClientSettings.effectsVolume() / 100f);
  }

  private static String localized(String key, String fallback) {
    if (key == null || key.isBlank()) {
      return fallback;
    }
    String translated = TEXT_TRANS.text(key);
    return translated.equals("{" + key + "}") ? fallback : translated;
  }

  private static String translationKey(String id, String field) {
    return ACHIEVEMENT_TRANSLATION_PREFIX + id + "." + field;
  }

  private static Texture texture(String path) {
    Texture texture = TextureMap.instance().textureAt(new SimpleIPath(path));
    if (texture != null) {
      return texture;
    }
    return TextureMap.instance().textureAt(new SimpleIPath(FALLBACK_IMAGE));
  }

  private static final class AlwaysOnTopContainer extends BaseContainerUI {

    private AlwaysOnTopContainer(Table card) {
      super(card, Align.topRight, CORNER_MARGIN, CORNER_MARGIN, false, false);
    }

    @Override
    public void act(float delta) {
      super.act(delta);
      toFront();
    }
  }
}
