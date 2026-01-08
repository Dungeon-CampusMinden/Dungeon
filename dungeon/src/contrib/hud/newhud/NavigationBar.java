package contrib.hud.newhud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import contrib.entities.HeroController;
import core.Game;

/**
 * The NavigationBar is a HUD element containing multiple sections to provide quick access to game
 * menus such as the main menu, character screen and inventory.
 *
 * <p>The main menu and character screen remain to be implemented in the future. Each section
 * requires a dedicated icon, tooltip and clickListener.
 */
public class NavigationBar extends Table implements HUDElement {

  /**
   * Creates a new NavigationBar currently including three sections.
   *
   * @param skin The skin that defines the appearance of UI elements.
   */
  public NavigationBar(Skin skin) {
    setBackground(skin.getDrawable("dark-gray"));
    defaults().pad(5);

    // --- MENU ---
    Table menuTable = new Table();
    menuTable.setBackground(skin.getDrawable("gray"));
    Image menuIcon = new Image();
    menuIcon.setDrawable(
        new TextureRegionDrawable(new Texture("dungeon/assets/hud/menu_icon.png")));
    menuTable.add(menuIcon).size(32, 32);
    add(menuTable).size(48, 48);
    addTooltip(menuTable, "Menu (M)", skin);

    // TODO: dem menuTable einen Listener zum öffnen einer Menu UI geben (sobald es existiert)

    // --- CHARACTER ---
    Table charTable = new Table();
    charTable.setBackground(skin.getDrawable("gray"));
    Image charIcon = new Image();
    charIcon.setDrawable(
        new TextureRegionDrawable(new Texture("dungeon/assets/hud/char_icon.png")));
    charTable.add(charIcon).size(32, 32);
    add(charTable).size(48, 48);
    addTooltip(charTable, "Character (C)", skin);

    // TODO: dem charTable einen Listener zum öffnen einer charakter/equipment UI geben (sobald es
    // existiert)

    // --- INVENTORY ---
    Table inventoryTable = new Table();
    inventoryTable.setBackground(skin.getDrawable("gray"));
    Image inventoryIcon = new Image();
    inventoryIcon.setDrawable(
        new TextureRegionDrawable(new Texture("dungeon/assets/hud/inventory_icon.png")));
    inventoryTable.add(inventoryIcon).size(32, 32);
    add(inventoryTable).size(48, 48);
    addTooltip(inventoryTable, "Inventory (I)", skin);

    inventoryTable.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            Game.player().ifPresent(HeroController::toggleInventory);
          }
        });
  }

  @Override
  public void init() {
    layoutElement();
  }

  @Override
  public void layoutElement() {
    setPosition(Gdx.graphics.getWidth() - 180f, Gdx.graphics.getHeight() - 60f);
    pack();
  }

  @Override
  public void update() {}

  private void addTooltip(Table table, String text, Skin skin) {
    Label label = new Label(text, skin);
    Tooltip<Label> tooltip = new Tooltip<>(label);
    tooltip.setInstant(true);
    table.addListener(tooltip);
  }
}
