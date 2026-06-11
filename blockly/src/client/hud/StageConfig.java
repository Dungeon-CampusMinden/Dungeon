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

              TextButton btn1x = new TextButton("1x", UIUtils.defaultSkin());
              TextButton btn4x = new TextButton("4x", UIUtils.defaultSkin());
              TextButton btn8x = new TextButton("8x", UIUtils.defaultSkin());

              ButtonGroup<TextButton> group = new ButtonGroup<>(btn1x, btn4x, btn8x);
              Vector2 baseForce = Client.MOVEMENT_FORCE;

              // max. 1 velocity button at the same
              group.setMaxCheckCount(1);
              group.setMinCheckCount(1);
              group.setUncheckLast(true);

              // each button gives a different MOVEMENT_FORCE value
              btn1x.addListener(
                  new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                      if (btn1x.isChecked())
                        Client.MOVEMENT_FORCE = Vector2.of(baseForce.x(), baseForce.y());
                    }
                  });
              btn4x.addListener(
                  new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                      if (btn4x.isChecked())
                        Client.MOVEMENT_FORCE = Vector2.of(baseForce.x() * 4f, baseForce.y() * 4f);
                    }
                  });
              btn8x.addListener(
                  new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                      if (btn8x.isChecked())
                        Client.MOVEMENT_FORCE = Vector2.of(baseForce.x() * 8f, baseForce.y() * 8f);
                    }
                  });

              Table bottomCenter = new Table();
              bottomCenter.add(btn1x).width(55);
              bottomCenter.add(btn4x).width(55);
              bottomCenter.add(btn8x).width(55);

              // buttons positioned at bottom center
              rootTable.add(bottomCenter).expand().center().bottom();
              stage.addActor(rootTable);
            });
  }
}
