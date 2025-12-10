package contrib.hud.newhud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import contrib.components.AIComponent;
import core.Game;

public class EnemyCounter extends Table implements HUDElement {

  private final Label counterLabel;
  private long enemyCount = 0;

  public EnemyCounter(Skin skin) {
    setSize(64, 64);
    setBackground(skin.getDrawable("dark-red"));

    Image skullIcon = new Image();
    skullIcon.setDrawable(
        new TextureRegionDrawable(new Texture("dungeon/assets/hud/skull_icon.png")));
    add(skullIcon).size(48, 48);
    counterLabel = new Label("", skin, "enemycounterlabel");
    add(counterLabel).size(24, 24);
  }

  @Override
  public void init() {
    layoutElement();
  }

  @Override
  public void layoutElement() {
    setPosition(0, Gdx.graphics.getHeight() / 2f);
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
      enemyCount = currentEnemyCount;
    }
  }
}
