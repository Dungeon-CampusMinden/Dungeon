package rooms.systemRecovery.save;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import engine.Game;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.path.SimpleIPath;

/** Brief client HUD confirmation for checkpoints that the authoritative server has written. */
public final class SystemRecoveryAutoSaveHud {
  private static final String CHECK_ICON = "hud/check.png";
  private static final float ICON_SIZE = 20f;
  private static final float VISIBLE_SECONDS = 2.5f;
  private static final Color ICON_COLOR = new Color(0.25f, 1f, 0.4f, 1f);
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

  /**
   * Shows one confirmation when a newer server-written revision arrives in a snapshot.
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
    Game.stage()
        .ifPresent(
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
      Texture texture = TextureMap.instance().textureAt(new SimpleIPath(CHECK_ICON));
      Image icon = new Image(texture);
      icon.setColor(ICON_COLOR);

      badge = new Table();
      badge.setFillParent(true);
      badge.setTouchable(Touchable.disabled);
      badge.bottom().right();
      badge.add(icon).size(ICON_SIZE).padRight(18f).padBottom(18f);
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
