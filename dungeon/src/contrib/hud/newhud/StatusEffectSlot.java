package contrib.hud.newhud;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class StatusEffectSlot extends Table {

  private final Label counterLabel;
  private final Image effectIcon;

  public StatusEffectSlot(Skin skin) {
    setSize(32, 32);
    setBackground(skin.getDrawable("gray"));

    Stack stack = new Stack();
    stack.setSize(32, 32);
    addActor(stack);

    effectIcon = new Image();

    Table iconTable = new Table();
    iconTable.top().left();
    iconTable.add(effectIcon).size(24, 24);

    counterLabel = new Label("", skin, "staminalabel");
    counterLabel.setFontScale(0.4f);

    Table counterTable = new Table();
    counterTable.bottom().right();
    counterTable.add(counterLabel).size(14, 14);

    stack.add(iconTable);
    stack.add(counterTable);
  }

  public void setEffectIcon(Texture texture) {
    effectIcon.setDrawable(new TextureRegionDrawable(texture));
  }

  public void setCounterLabel(int count) {
    counterLabel.setText(String.valueOf(count));
  }
}
