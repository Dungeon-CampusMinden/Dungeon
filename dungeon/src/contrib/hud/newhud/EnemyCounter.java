package contrib.hud.newhud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import contrib.components.AIComponent;
import core.Game;

public class EnemyCounter extends Table implements HUDElement {

  private final Label counterLabel;
  private final Label tooltipLabel;
  private long enemyCount = 0;

  public EnemyCounter(Skin skin) {
    super(skin);

    setSize(64, 64);
    setBackground(skin.getDrawable("dark-red"));
    pad(5);

    Image skullIcon = new Image();
    skullIcon.setDrawable(
        new TextureRegionDrawable(new Texture("dungeon/assets/hud/skull_icon.png")));
    add(skullIcon).size(48, 48);
    counterLabel = new Label("", skin, "enemycounterlabel");
    add(counterLabel).size(24, 24);

    tooltipLabel = new Label("", skin);
    Tooltip<Label> tooltip = new Tooltip<>(tooltipLabel);
    tooltip.setInstant(true);
    addListener(tooltip);
  }

  @Override
  public void init() {
    layoutElement();
  }

  @Override
  public void layoutElement() {
    pack();
    setPosition(0, Gdx.graphics.getHeight() / 2f - getHeight() / 2f);
  }

  @Override
  public void update() {
    countEnemies();
  }

  private void countEnemies() {
    long currentEnemyCount =
        Game.allEntities().filter(entity -> entity.fetch(AIComponent.class).isPresent()).count();

    if (currentEnemyCount != enemyCount) {
      counterLabel.setText((int) currentEnemyCount);
      tooltipLabel.setText("There are " + currentEnemyCount + " enemies nearby!");
      enemyCount = currentEnemyCount;
    }
  }
}
