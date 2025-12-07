package contrib.hud.newhud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import contrib.components.StaminaComponent;
import core.Game;

public class StaminaBar extends ProgressBar implements HUDElement {

  private static final float BAR_WIDTH = 300;
  private static final float BAR_HEIGHT = 20;

  public StaminaBar(Skin skin) {
    super(0f, 100f, 1f, false, skin, "staminabarhud");
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
    setPosition(80, Gdx.graphics.getHeight() - BAR_HEIGHT - 60);
  }

  @Override
  public void update() {
    Game.player()
        .flatMap(player -> player.fetch(StaminaComponent.class))
        .ifPresent(
            sc -> {
              setStamina(sc.currentAmount(), sc.maxAmount());
            });
  }

  public void setStamina(float current, float max) {
    float value = (current / max) * 100f;
    setValue(value);
  }
}
