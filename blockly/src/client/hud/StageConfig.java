package client.hud;

import client.Client;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import contrib.hud.UIUtils;
import core.Game;
import core.utils.Vector2;

/**
 * Configures the LibGDX HUD stage for the Blockly client.
 *
 * <p>Sets up the UI layout with the velocity control widget, which allows adjusting the hero's
 * movement {@link Client#MOVEMENT_FORCE} at runtime via a slider.
 */
public class StageConfig {

  /**
   * Configures the stage layout.
   *
   * <p>Makes the stage clickable, creates a root table that spans the full screen, and adds the
   * velocity label and slider to it.
   */
  public static void setupStage() {

    Game.stage()
        .ifPresent(
            stage -> {
              Gdx.input.setInputProcessor(stage);
              Table rootTable = new Table();

              //  expands the table to fill the full screen by matching the stage size
              rootTable.setFillParent(true);

              Label velocityLabel = new Label("Speed: 1x", UIUtils.defaultSkin(), "blank-white");
              Slider slider =
                  new Slider(1.0f, 8f, 1f, false, UIUtils.defaultSkin(), "clean-horizontal");

              slider.addListener(
                  new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                      float value = slider.getValue();
                      velocityLabel.setText("velocity: " + value + "x");
                      Client.MOVEMENT_FORCE = Vector2.of(7.5f * value, 7.5f * value);
                    }
                  });

              // child table grouping the velocity label and slider
              Table topRight = new Table();
              topRight.add(velocityLabel).row();
              topRight.add(slider).width(150);

              // nests topRight inside rootTable, anchored to the top-right corner
              rootTable.add(topRight).expand().top().right();

              // attaches rootTable directly to the stage, making it the layout root
              stage.addActor(rootTable);
            });
  }
}
