package rooms.systemRecovery.save;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import engine.Game;
import engine.utils.FontSpec;
import engine.utils.Scene2dElementFactory;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.path.SimpleIPath;
import feature.hud.UIUtils;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Brief client HUD confirmation for checkpoints that the authoritative server has written. */
public final class SystemRecoveryAutoSaveHud {
  private static final String CHECK_ICON = "hud/check.png";
  private static final float VISIBLE_SECONDS = 2.5f;
  private static Stage attachedStage;
  private static Table badge;
  private static int lastRevision;

  private SystemRecoveryAutoSaveHud() {}

  /** Clears the previous run's revision and HUD actor before a new client session. */
  public static void reset() {
    if (badge != null) badge.remove();
    badge = null;
    attachedStage = null;
    lastRevision = 0;
  }

  /** Shows one confirmation when a newer server-written revision arrives in a snapshot.
   *
   * @param encodedRevision revision supplied by the authoritative server
   */
  public static void acceptRevision(String encodedRevision) {
    if (encodedRevision == null || Game.isHeadless()) return;
    final int revision;
    try {
      revision = Integer.parseInt(encodedRevision);
    } catch (NumberFormatException ignored) {
      return;
    }
    if (revision <= lastRevision) return;
    Game.stage().ifPresent(
        stage -> {
          if (stage != attachedStage) {
            reset();
            attachedStage = stage;
          }
          if (revision <= lastRevision) return;
          lastRevision = revision;
          show(stage);
        });
  }

  private static void show(Stage stage) {
    if (badge == null) {
      Table content = new Table(UIUtils.defaultSkin());
      content.setBackground("window_background_big");
      content.pad(5f, 9f, 5f, 9f);
      Texture texture = TextureMap.instance().textureAt(new SimpleIPath(CHECK_ICON));
      Image icon = new Image(texture);
      icon.setColor(Color.GREEN);
      Label label =
          Scene2dElementFactory.createLabel(
              SystemRecoveryText.text("hud.autosave"),
              FontSpec.of("fonts/Lexend-Regular.ttf", 17, Color.WHITE));
      content.add(icon).size(20f).padRight(7f);
      content.add(label);

      badge = new Table();
      badge.setFillParent(true);
      badge.setTouchable(Touchable.disabled);
      badge.bottom().right();
      badge.add(content).padRight(18f).padBottom(18f);
      stage.addActor(badge);
    }
    badge.clearActions();
    badge.setColor(1f, 1f, 1f, 1f);
    badge.setVisible(true);
    badge.toFront();
    badge.addAction(
        Actions.sequence(
            Actions.delay(VISIBLE_SECONDS), Actions.fadeOut(0.4f), Actions.visible(false)));
  }
}
