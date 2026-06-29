package client.hud;

import client.Client;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import contrib.hud.UIUtils;
import core.Game;
import core.utils.Vector2;

/**
 * Configures the LibGDX HUD stage for the Blockly client.
 *
 * <p>Sets up the UI layout with the velocity control widget, which allows adjusting the hero's
 * movement {@link Client#MOVEMENT_FORCE} at runtime via speed buttons (1×, 4×, 8×).
 */
public class StageConfig {

  private static final float MEDIUM_SPEED = 2f;
  private static final float HIGH_SPEED = 4f;

  /**
   * Configures the stage layout.
   *
   * <p>Makes the stage clickable, creates a root table that spans the full screen, and adds the
   * speed buttons to it. Only one button can be active at a time.
   */
  public static void setupStage() {

    Game.stage()
        .ifPresent(
            stage -> {
              Gdx.input.setInputProcessor(stage);
              Table rootTable = new Table();
              rootTable.setFillParent(true);

              TextButton btnBaseSpeed = new TextButton("1x", UIUtils.defaultSkin());
              TextButton btnMediumSpeed =
                  new TextButton((int) MEDIUM_SPEED + "x", UIUtils.defaultSkin());
              TextButton btnHighSpeed =
                  new TextButton((int) HIGH_SPEED + "x", UIUtils.defaultSkin());
              ButtonGroup<TextButton> group =
                  new ButtonGroup<>(btnBaseSpeed, btnMediumSpeed, btnHighSpeed);
              Vector2 baseForce = Client.MOVEMENT_FORCE;

              // max. 1 velocity button at the same
              group.setMaxCheckCount(1);
              group.setMinCheckCount(1);
              group.setUncheckLast(true);

              // each button gives a different MOVEMENT_FORCE value
              btnBaseSpeed.addListener(
                  new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                      if (btnBaseSpeed.isChecked())
                        Client.MOVEMENT_FORCE = Vector2.of(baseForce.x(), baseForce.y());
                    }
                  });
              btnMediumSpeed.addListener(
                  new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                      if (btnMediumSpeed.isChecked())
                        Client.MOVEMENT_FORCE =
                            Vector2.of(baseForce.x() * MEDIUM_SPEED, baseForce.y() * MEDIUM_SPEED);
                    }
                  });
              btnHighSpeed.addListener(
                  new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                      if (btnHighSpeed.isChecked())
                        Client.MOVEMENT_FORCE =
                            Vector2.of(baseForce.x() * HIGH_SPEED, baseForce.y() * HIGH_SPEED);
                    }
                  });

              Table bottomCenter = new Table();
              bottomCenter.add(btnBaseSpeed).width(55);
              bottomCenter.add(btnMediumSpeed).width(55);
              bottomCenter.add(btnHighSpeed).width(55);

              // buttons positioned at bottom center
              rootTable.add(bottomCenter).expand().center().bottom();
              stage.addActor(rootTable);
            });
  }
}
