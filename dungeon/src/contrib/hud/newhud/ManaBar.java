package contrib.hud.newhud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.utils.Align;
import contrib.components.ManaComponent;
import core.Game;

public class ManaBar extends Stack implements HUDElement {

  private static final float BAR_WIDTH = 300;
  private static final float BAR_HEIGHT = 20;

  private final ProgressBar bar;
  private final Label manaLabel;

  public ManaBar(Skin skin) {
    bar = new ProgressBar(0f, 100f, 1f, false, skin, "manabarhud");
    bar.setSize(BAR_WIDTH, BAR_HEIGHT);
    bar.setAnimateDuration(0.2f);

    manaLabel = new Label("100 / 100", skin, "manalabel"); // Style aus Skin
    manaLabel.setSize(80, BAR_HEIGHT);
    manaLabel.setFontScale(0.5f);
    manaLabel.setAlignment(Align.left);

    add(bar);
    add(manaLabel);
  }

  @Override
  public void init() {
    layoutElement();
    bar.setValue(100);
  }

  @Override
  public void layoutElement() {
    setSize(BAR_WIDTH, BAR_HEIGHT);
    setPosition(80, Gdx.graphics.getHeight() - BAR_HEIGHT - 40);
  }

  @Override
  public void update() {
    Game.player()
        .flatMap(player -> player.fetch(ManaComponent.class))
        .ifPresent(mc -> setMana(mc.currentAmount(), mc.maxAmount()));
  }

  public void setMana(float current, float max) {
    float value = (current / max) * 100f;
    bar.setValue(value);

    manaLabel.setText((int) current + " / " + (int) max);
  }
}
