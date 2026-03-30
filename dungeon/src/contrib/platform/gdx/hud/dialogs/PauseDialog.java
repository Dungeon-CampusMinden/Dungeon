package contrib.platform.gdx.hud.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import core.ui.gdx.GdxUiAssetLoader;

/**
 * Package-private Scene2D pause menu.
 *
 * <p>Creates a simple pause overlay.
 */
final class PauseDialog {

  private PauseDialog() {}

  static Group create(Skin skin) {
    Group pauseMenu =
      new Group() {
        @Override
        public void act(float delta) {
          super.act(delta);
          if (getStage() != null
            && (getWidth() != getStage().getWidth()
            || getHeight() != getStage().getHeight())) {
            setSize(getStage().getWidth(), getStage().getHeight());
          }
        }
      };

    pauseMenu.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

    // Very simple pause menu for now:
    // Just a translucent box at the top-center of the screen with
    // a label "Game Paused\n\nPress <P> to resume"
    Table layout = new Table();
    layout.setFillParent(true);

    Table container = new Table();
    Texture t = GdxUiAssetLoader.createSolidColorTexture(new Color(0, 0, 0, 0.7f));
    container.setBackground(new TextureRegionDrawable(t));

    Label label = new Label("Game Paused\n\nPress <P> to resume", skin, "blank-white");
    label.setAlignment(Align.center);
    container.add(label).width(400).height(200);

    layout.add(container).expand().top().padTop(50);
    pauseMenu.addActor(layout);

    return pauseMenu;
  }
}
