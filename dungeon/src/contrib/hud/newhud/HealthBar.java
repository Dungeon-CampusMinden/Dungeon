package contrib.hud.newhud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import contrib.components.HealthComponent;
import core.Game;

public class HealthBar extends ProgressBar implements HUDElement {

  private static final float BAR_WIDTH = 300;
  private static final float BAR_HEIGHT = 20;

  public HealthBar(Skin skin) {
    super(0f, 100f, 1f, false, skin, "healthbarhud");
    setSize(BAR_WIDTH, BAR_HEIGHT);
    setAnimateDuration(0.2f);
  }

  @Override
  public void init() {
    layout();
    setValue(100); // Start voll
  }

  @Override
  public void layout() {
    setPosition(80, Gdx.graphics.getHeight() - BAR_HEIGHT - 20);
  }

  @Override
  public void update() {
    Game.player()
        .flatMap(player -> player.fetch(HealthComponent.class))
        .ifPresent(
            hc -> {
              setHealth(hc.currentHealthpoints(), hc.maximalHealthpoints());
            });
  }

  public void setHealth(float current, float max) {
    float value = (current / max) * 100f;
    setValue(value);
  }
}
