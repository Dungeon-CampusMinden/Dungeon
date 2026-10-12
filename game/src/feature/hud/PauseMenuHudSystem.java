package feature.hud;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import engine.Game;
import engine.System;
import engine.utils.FontSpec;
import engine.utils.Scene2dElementFactory;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.path.SimpleIPath;
import feature.hud.dialogs.PauseDialog;
import feature.input.configuration.KeyboardConfig;

/** Client-side HUD button in the bottom-left corner that opens the pause menu. */
public final class PauseMenuHudSystem extends System {

  private static final String ICON_PATH = "hud/settings.png";
  private static final float ICON_SIZE = 56f;
  private static final float SCREEN_MARGIN = 16f;
  private static final FontSpec FONT_KEY = FontSpec.of(18, Color.WHITE, 2f, Color.BLACK);

  private Table hud;
  private Label keyLabel;

  /** Creates the pause menu HUD system. */
  public PauseMenuHudSystem() {
    super(AuthoritativeSide.CLIENT);
  }

  @Override
  public void execute() {
    if (Game.isHeadless()) return;
    Game.stage()
        .ifPresent(
            stage -> {
              if (hud == null) hud = buildHud();
              if (hud.getStage() != stage) {
                stage.addActor(hud);
                hud.toBack();
              }
              hud.setVisible(Game.player().isPresent());
              if (!hud.isVisible()) return;

              keyLabel.setText(Input.Keys.toString(KeyboardConfig.PAUSE_MENU.value()));
              hud.pack();
              hud.setPosition(SCREEN_MARGIN, SCREEN_MARGIN, Align.bottomLeft);
            });
  }

  private Table buildHud() {
    Image icon = new Image(TextureMap.instance().textureAt(new SimpleIPath(ICON_PATH)));
    keyLabel = Scene2dElementFactory.createLabel("", FONT_KEY);

    Table table = new Table();
    table.setTouchable(Touchable.enabled);
    table.add(icon).size(ICON_SIZE).row();
    table.add(keyLabel);
    table.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            Game.player().ifPresent(PauseDialog::showPauseDialog);
          }
        });
    return table;
  }
}
