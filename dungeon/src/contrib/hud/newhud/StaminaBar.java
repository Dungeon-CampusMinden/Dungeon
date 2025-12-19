package contrib.hud.newhud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import contrib.components.StaminaComponent;
import core.Game;

public class StaminaBar extends Stack implements HUDElement {

  private static final float BAR_WIDTH = 300;
  private static final float BAR_HEIGHT = 20;

  private final ProgressBar bar;
  private final Label staminaLabel;

  public StaminaBar(Skin skin) {
    bar = new ProgressBar(0f, 100f, 1f, false, skin, "staminabarhud");
    bar.setSize(BAR_WIDTH, BAR_HEIGHT);
    bar.setAnimateDuration(0.2f);

    staminaLabel = new Label("100 / 100", skin, "staminalabel"); // Style aus Skin
    staminaLabel.setSize(80, BAR_HEIGHT);
    staminaLabel.setFontScale(0.5f);
    staminaLabel.setAlignment(Align.left);

    Label tooltipLabel = new Label("Stamina", skin);
    Tooltip<Label> tooltip = new Tooltip<>(tooltipLabel);
    tooltip.setInstant(true);
    addListener(tooltip);

    add(bar);
    add(staminaLabel);
  }

  @Override
  public void init() {
    layoutElement();
    bar.setValue(100);
  }

  @Override
  public void layoutElement() {
    setSize(BAR_WIDTH, BAR_HEIGHT);
    setPosition(80, Gdx.graphics.getHeight() - BAR_HEIGHT - 60);
  }

  @Override
  public void update() {
    Game.player()
        .flatMap(player -> player.fetch(StaminaComponent.class))
        .ifPresent(sc -> setStamina(sc.currentAmount(), sc.maxAmount()));
  }

  private void setStamina(float current, float max) {
    float value = (current / max) * 100f;
    bar.setValue(value);

    staminaLabel.setText((int) current + " / " + (int) max);
  }
}
