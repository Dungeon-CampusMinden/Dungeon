package contrib.hud.newhud;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class QuickAccessSlot extends Table {
  private final Image itemIcon;
  private final Table iconTable;
  private final Label keybindLable;
  private final Table keybindTable;

  public QuickAccessSlot(Skin skin) {
    setSize(48, 48);
    setBackground(skin.getDrawable("gray"));

    Stack stack = new Stack();
    stack.setSize(48, 48);
    addActor(stack);

    itemIcon = new Image();

    iconTable = new Table();
    iconTable.add(itemIcon).size(32, 32);

    keybindLable = new Label("Q", skin);

    keybindTable = new Table();
    keybindTable.bottom();
    keybindTable.add(keybindLable).size(16, 16);

    stack.add(iconTable);
    stack.add(keybindTable);
  }

  public void setItemTexture(Texture texture) {
    itemIcon.setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));
  }

  public void setKeybind(String key) {
    keybindLable.setText(key);
  }
}
