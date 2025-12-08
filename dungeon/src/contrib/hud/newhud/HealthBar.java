package contrib.hud.newhud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.utils.Align;
import contrib.components.HealthComponent;
import core.Game;

public class HealthBar extends Stack implements HUDElement {

  private static final float BAR_WIDTH = 300;
  private static final float BAR_HEIGHT = 20;

  private final ProgressBar bar;
  private final Label hpLabel;

  public HealthBar(Skin skin) {
    bar = new ProgressBar(0f, 100f, 1f, false, skin, "healthbarhud");
    bar.setSize(BAR_WIDTH, BAR_HEIGHT);
    bar.setAnimateDuration(0.2f);

    hpLabel = new Label("100 / 100", skin, "healthlabel");
    hpLabel.setSize(80, BAR_HEIGHT);
    hpLabel.setFontScale(0.5f);
    hpLabel.setAlignment(Align.center);

    add(bar);
    add(hpLabel);
  }

  @Override
  public void init() {
    layout();
    bar.setValue(100);
  }

  @Override
  public void layout() {
    setPosition(80, Gdx.graphics.getHeight() - BAR_HEIGHT - 20);
  }

  @Override
  public void update() {
    Game.player()
        .flatMap(player -> player.fetch(HealthComponent.class))
        .ifPresent(hc -> setHealth(hc.currentHealthpoints(), hc.maximalHealthpoints()));
  }

  public void setHealth(float current, float max) {
    float value = (current / max) * 100f;
    bar.setValue(value);

    hpLabel.setText((int) current + " / " + (int) max);
  }
}
