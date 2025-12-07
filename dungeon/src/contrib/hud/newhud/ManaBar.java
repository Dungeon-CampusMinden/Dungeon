package contrib.hud.newhud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import contrib.components.ManaComponent;
import core.Game;

public class ManaBar extends ProgressBar implements HUDElement {

  private static final float BAR_WIDTH = 300;
  private static final float BAR_HEIGHT = 20;

  public ManaBar(Skin skin) {
    super(0f, 100f, 1f, false, skin, "manabarhud");
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
    setPosition(80, Gdx.graphics.getHeight() - BAR_HEIGHT - 40);
  }

  @Override
  public void update() {
    Game.player()
        .flatMap(player -> player.fetch(ManaComponent.class))
        .ifPresent(
            mc -> {
              setMana(mc.currentAmount(), mc.maxAmount());
            });
  }

  public void setMana(float current, float max) {
    float value = (current / max) * 100f;
    setValue(value);
  }
}
