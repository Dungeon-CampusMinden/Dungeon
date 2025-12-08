package contrib.hud.newhud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import contrib.components.InventoryComponent;
import core.Game;
import java.util.ArrayList;
import java.util.List;

public class QuickAccessInventory extends Table implements HUDElement {
  private final List<QuickAccessSlot> slots = new ArrayList<>();

  public QuickAccessInventory(Skin skin) {
    super(skin);

    setBackground(skin.getDrawable("dark-gray"));
    pad(1);

    // Abstand zwischen Slots
    defaults().pad(5);

    for (int i = 0; i < 2; i++) {
      QuickAccessSlot slot = new QuickAccessSlot(skin);
      slots.add(slot);
      add(slot).size(48, 48);
    }
  }

  @Override
  public void init() {
    layoutElement();
  }

  @Override
  public void layoutElement() {
    setPosition((Gdx.graphics.getWidth() - getWidth()) / 2f + 300, 35);
    pack();
  }

  @Override
  public void update() {}

  public void setItemForQuickAccessSlot(QuickAccessSlot slot) {
    Game.player()
        .flatMap(player -> player.fetch(InventoryComponent.class))
        .ifPresent(inventoryComp -> {});
  }
}
