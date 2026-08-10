package contrib.achievements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import contrib.hud.UIUtils;
import core.language.Translation;
import core.utils.FontSpec;
import core.utils.Scene2dElementFactory;
import core.utils.components.draw.TextureMap;
import core.utils.components.path.SimpleIPath;
import java.util.List;
import java.util.stream.Collectors;

/** Scene2D factory for the achievements page in the main menu. */
public final class AchievementMenuView extends Table {

  private static final String TITLE_FONT = "fonts/Roboto-Bold.ttf";
  private static final String BODY_FONT = "fonts/Roboto-Regular.ttf";
  private static final String T_TITLE = "title";
  private static final String T_HIDDEN_NAME = "hidden_name";
  private static final String T_HIDDEN_DESCRIPTION = "hidden_description";
  private static final String T_PROGRESS = "progress";
  private static final Color TEXT = Color.BLACK;
  private static final Color LOCKED_TEXT = new Color(0.38f, 0.38f, 0.38f, 1f);
  private static final String FALLBACK_IMAGE = "animation/missing_texture.png";
  private static final float REFRESH_INTERVAL_SECONDS = 0.25f;
  private static final Translation TRANS = new Translation("achievement.menu");

  private final Table achievementList = new Table();
  private final Label progressLabel =
      Scene2dElementFactory.createLabel("", FontSpec.of(BODY_FONT, 22, Color.DARK_GRAY));
  private float refreshTimer;
  private String lastSnapshot = "";

  private AchievementMenuView(TextButton backButton) {
    buildLayout(backButton);
    refreshIfChanged();
  }

  /**
   * Builds the achievements page.
   *
   * @param backButton button used to return to the main menu
   * @return achievements page
   */
  public static Table build(TextButton backButton) {
    return new AchievementMenuView(backButton);
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    refreshTimer += delta;
    if (refreshTimer >= REFRESH_INTERVAL_SECONDS) {
      refreshTimer = 0f;
      refreshIfChanged();
    }
  }

  private void buildLayout(TextButton backButton) {
    Label title =
        Scene2dElementFactory.createLabel(
            TRANS.text(T_TITLE), FontSpec.of(TITLE_FONT, 48, Color.BLACK));

    achievementList.defaults().growX().padBottom(10f);

    ScrollPane scrollPane = Scene2dElementFactory.createScrollPane(achievementList, false, true);

    add(title).padBottom(15).align(Align.center).row();
    add(progressLabel).padBottom(10).align(Align.center).row();
    add(Scene2dElementFactory.createHorizontalDivider()).growX().padBottom(5).row();
    add(scrollPane).width(650).height(420).row();
    add(Scene2dElementFactory.createHorizontalDivider()).growX().padTop(5).row();
    add(backButton).width(300).padTop(15).padBottom(15).row();
  }

  private void refreshIfChanged() {
    List<Achievement> achievements = AchievementManager.menuAchievements();
    String snapshot = snapshot(achievements);
    if (snapshot.equals(lastSnapshot)) {
      return;
    }
    lastSnapshot = snapshot;
    long unlockedCount =
        achievements.stream()
            .filter(achievement -> AchievementManager.isUnlockedInMenu(achievement.name()))
            .count();
    progressLabel.setText(TRANS.text(T_PROGRESS, unlockedCount, achievements.size()));
    achievementList.clearChildren();
    for (Achievement achievement : achievements) {
      achievementList.add(row(achievement)).row();
    }
  }

  private String snapshot(List<Achievement> achievements) {
    return achievements.stream()
        .map(
            achievement ->
                achievement.name()
                    + ":"
                    + AchievementManager.isUnlockedInMenu(achievement.name())
                    + ":"
                    + achievement.displayName()
                    + ":"
                    + achievement.displayDescription())
        .collect(Collectors.joining("|"));
  }

  private static Table row(Achievement achievement) {
    boolean unlocked = AchievementManager.isUnlockedInMenu(achievement.name());
    boolean hidden = achievement.hidden() && !unlocked;
    String name = hidden ? TRANS.text(T_HIDDEN_NAME) : achievement.displayName();
    String description =
        hidden ? TRANS.text(T_HIDDEN_DESCRIPTION) : achievement.displayDescription();
    Color textColor = unlocked ? TEXT : LOCKED_TEXT;

    Image icon = new Image(iconDrawable(achievement.imagePath(), unlocked));
    icon.setScaling(com.badlogic.gdx.utils.Scaling.fit);

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

  private static TextureRegionDrawable iconDrawable(String path, boolean unlocked) {
    Texture texture = texture(path);
    TextureRegion region = new TextureRegion(texture);
    if (unlocked) {
      return new TextureRegionDrawable(region);
    }
    return new GrayscaleTextureRegionDrawable(region);
  }

  private static Texture texture(String path) {
    Texture texture = TextureMap.instance().textureAt(new SimpleIPath(path));
    if (texture != null) {
      return texture;
    }
    return TextureMap.instance().textureAt(new SimpleIPath(FALLBACK_IMAGE));
  }

  private static final class GrayscaleTextureRegionDrawable extends TextureRegionDrawable {
    private static final String VERTEX_SHADER =
        """
        attribute vec4 a_position;
        attribute vec4 a_color;
        attribute vec2 a_texCoord0;
        uniform mat4 u_projTrans;
        varying vec4 v_color;
        varying vec2 v_texCoords;

        void main() {
          v_color = a_color;
          v_color.a = v_color.a * (255.0 / 254.0);
          v_texCoords = a_texCoord0;
          gl_Position = u_projTrans * a_position;
        }
        """;
    private static final String FRAGMENT_SHADER =
        """
        #ifdef GL_ES
        precision mediump float;
        #endif

        varying vec4 v_color;
        varying vec2 v_texCoords;
        uniform sampler2D u_texture;

        void main() {
          vec4 texel = texture2D(u_texture, v_texCoords);
          float luminance = dot(texel.rgb, vec3(0.299, 0.587, 0.114));
          gl_FragColor = vec4(vec3(luminance * 0.35), texel.a) * v_color;
        }
        """;

    private static ShaderProgram shader;

    private GrayscaleTextureRegionDrawable(TextureRegion region) {
      super(region);
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
      ShaderProgram previousShader = batch.getShader();
      batch.flush();
      batch.setShader(shader());
      super.draw(batch, x, y, width, height);
      batch.flush();
      batch.setShader(previousShader);
    }

    private static ShaderProgram shader() {
      if (shader == null) {
        ShaderProgram.pedantic = false;
        shader = new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        if (!shader.isCompiled()) {
          throw new IllegalStateException(
              "Grayscale achievement icon shader compilation failed:\n" + shader.getLog());
        }
      }
      return shader;
    }
  }
}
