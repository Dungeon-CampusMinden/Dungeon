package contrib.hud.newhud;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * A QuickAccessSlot represents a single slot within the {@link QuickAccessInventory}.
 *
 * <p>It is responsible for displaying the item's texture and providing a tooltip.
 */
public class QuickAccessSlot extends Table {
  private final Image itemIcon;
  private final Table iconTable;
  private final Label keybindLable;
  private final Table keybindTable;

  /**
   * Creates a new QuickAccessSlot.
   *
   * @param skin The skin that defines the appearance of UI elements.
   */
  public QuickAccessSlot(Skin skin) {
    super(skin);
    setSize(48, 48);
    setBackground(skin.getDrawable("gray"));

    Stack stack = new Stack();
    stack.setSize(48, 48);
    addActor(stack);

    itemIcon = new Image();

    iconTable = new Table();
    iconTable.add(itemIcon).size(32, 32);

    keybindLable = new Label("", skin);

    keybindTable = new Table();
    keybindTable.bottom();
    keybindTable.add(keybindLable).size(16, 16);

    stack.add(iconTable);
    stack.add(keybindTable);
  }

  /**
   * Sets the item's texture.
   *
   * @param texture The item's texture.
   */
  public void setItemTexture(Texture texture) {
    itemIcon.setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));
  }

  /**
   * Displays the hotkey for using the item in this slot.
   *
   * @param key Hotkey for item use.
   */
  public void setKeybindLabel(String key) {
    keybindLable.setText(key);
    addTooltip(this, "use Item (" + key + ")", getSkin());
  }

  /** Removes the item's texture for this slot. */
  public void removeItemTexture() {
    itemIcon.setDrawable(null);
  }

  /** Clears the Label visualizing the hotkey. */
  public void removeKeybindLabel() {
    keybindLable.setText("");
  }

  private void addTooltip(Table table, String text, Skin skin) {
    Label label = new Label(text, skin);
    Tooltip<Label> tooltip = new Tooltip<>(label);
    tooltip.setInstant(true);
    table.addListener(tooltip);
  }
}
