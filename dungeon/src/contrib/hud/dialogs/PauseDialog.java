package contrib.hud.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import contrib.hud.UIUtils;
import core.Game;
import core.utils.components.draw.TextureGenerator;
import core.utils.components.draw.TextureMap;
import core.utils.components.path.SimpleIPath;

import java.awt.font.ImageGraphicAttribute;

/**
 * Package-private builder for the pause menu.
 *
 * <p>Creates a simple pause dialog.
 */
final class PauseDialog {

  private PauseDialog() {}

  /**
   * Builds a pause menu from the given dialog context.
   *
   * <p>On headless servers, returns a {@link HeadlessDialogGroup} placeholder.
   *
   * @param ctx The dialog context containing the message, title, and confirmation callback
   * @return A fully configured pause menu or HeadlessDialogGroup
   */
  static Group build(DialogContext ctx) {

    // On headless server, return a placeholder
    if (Game.isHeadless()) {
      return new HeadlessDialogGroup();
    }

    return create(UIUtils.defaultSkin(), ctx.dialogId());
  }

  private static Group create(Skin skin, String dialogId) {
    Group pauseMenu = new Group() {
      @Override
      public void act(float delta) {
        super.act(delta);
        if (getStage() != null && (getWidth() != getStage().getWidth() || getHeight() != getStage().getHeight())) {
          setSize(getStage().getWidth(), getStage().getHeight());
        }
      }
    };

    pauseMenu.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

    // Very simple pause menu for now: Just a transluscent box at the top-center of the screen with a label "Game Paused\n\nPress <P> to resume"

    Table layout = new Table();
    layout.setFillParent(true);

    // Simple 1-color background texture
    Table container = new Table();
    Texture t = TextureGenerator.generateColorTexture(1, 1, new Color(0, 0, 0, 0.7f));
    container.setBackground(new TextureRegionDrawable(t));

    // Label
    Label label = new Label("Game Paused\n\nPress <P> to resume", skin, "blank-white");
    label.setAlignment(Align.center);

    container.add(label).width(400).height(200);
    layout.add(container).expand().top().padTop(50);

    pauseMenu.addActor(layout);


    return pauseMenu;
  }
}
