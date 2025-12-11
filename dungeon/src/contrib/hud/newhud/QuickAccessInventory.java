package contrib.hud.newhud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import contrib.components.InventoryComponent;
import contrib.configuration.KeyboardConfig;
import contrib.item.Item;
import core.Game;
import core.components.InputComponent;
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
    setItemForQuickAccessSlot();
    initKeybinds();
  }

  @Override
  public void layoutElement() {
    setPosition((Gdx.graphics.getWidth() - getWidth()) / 2f + 300, 10);
    pack();
  }

  @Override
  public void update() {
    setItemForQuickAccessSlot();
  }

  public void setItemForQuickAccessSlot() {
    Game.player()
        .flatMap(player -> player.fetch(InventoryComponent.class))
        .ifPresent(
            inventoryComp -> {
              for (int i = 0; i < 2; i++) {
                Item currentItem = inventoryComp.get(i).orElse(null);
                if (currentItem != null) {
                  slots
                      .get(i)
                      .setItemTexture(currentItem.inventoryAnimation().getSprite().getTexture());
                } else {
                  slots.get(i).removeItemTexture();
                }
              }
            });
  }

  public void initKeybinds() {
    Game.player()
        .flatMap(player -> player.fetch(InputComponent.class))
        .ifPresent(
            ic -> {
              ic.registerCallback(
                  KeyboardConfig.QUICK_ACCESS_1.value(),
                  (caller) -> {
                    caller
                        .fetch(InventoryComponent.class)
                        .flatMap(invComp -> invComp.get(0))
                        .ifPresent(item -> item.use(caller));
                  });

              ic.registerCallback(
                  KeyboardConfig.QUICK_ACCESS_2.value(),
                  (caller) -> {
                    caller
                        .fetch(InventoryComponent.class)
                        .flatMap(invComp -> invComp.get(1))
                        .ifPresent(item -> item.use(caller));
                  });
            });
    slots.get(0).setKeybindLabel("1");
    slots.get(1).setKeybindLabel("2");
  }
}
