package mushRoom.modules.journal;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import contrib.components.InventoryComponent;
import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import contrib.item.Item;
import core.Entity;
import core.Game;
import core.utils.components.draw.TextureMap;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import mushRoom.Sounds;
import mushRoom.modules.mushrooms.Mushrooms;

/** Item representing a journal that can be used to document discovered mushrooms. */
public class JournalItem extends Item {

  private static final int BASE_PAGES_UNLOCKED = 2;

  private static final SimpleIPath inventoryPath =
      new SimpleIPath("items/rpg/item_book_blue_lines.png");

  private int unlockedPages = BASE_PAGES_UNLOCKED;

  /** Constructs a new JournalItem. */
  public JournalItem() {
    super(
        "Notizbuch",
        BASE_PAGES_UNLOCKED + " / 6 Seiten beschrieben",
        new Animation(inventoryPath),
        new Animation(inventoryPath),
        1,
        1);
  }

  @Override
  public void use(final Entity user) {
    Game.player().ifPresent(JournalItem::openJournal);
  }

  /**
   * Opens the journal UI if the given entity has a {@link JournalItem} in its inventory.
   *
   * @param player the entity to open the journal for
   */
  public static void openJournal(Entity player) {
    player
        .fetch(InventoryComponent.class)
        .ifPresent(
            ic -> {
              if (!ic.hasItem(JournalItem.class)) return;
              ic.itemOfClass(JournalItem.class)
                  .ifPresent(
                      i -> {
                        JournalItem journalItem = (JournalItem) i;
                        openJournalUI(player, journalItem.unlockedPages);
                        Sounds.OPEN_INVENTORY_SOUND.play();
                      });
            });
  }

  private static void openJournalUI(Entity player, int unlockedPages) {
    Skin skin = UIUtils.defaultSkin();
    Texture bookTex = TextureMap.instance().textureAt(new SimpleIPath("images/open-book.png"));

    JournalUI bookUI = new JournalUI(skin, new TextureRegionDrawable(new TextureRegion(bookTex)));

    // Loop over all Mushrooms and add their entries
    for (Mushrooms mushroomType : Mushrooms.values()) {
      Texture t = TextureMap.instance().textureAt(new SimpleIPath(mushroomType.getTexturePath()));
      String text = "Ich kenne diesen Pilz noch nicht...";
      if (mushroomType.ordinal() < unlockedPages * 2) { // Each page has 2 mushrooms
        text = mushroomType.descriptionLong();
      }
      bookUI.addEntry(t, text);
    }

    player.remove(UIComponent.class);
    player.add(new UIComponent(bookUI, true, true));
  }

  /** Unlocks a new page in the journal and updates the description. */
  public void unlockPage() {
    this.unlockedPages++;
    this.description(unlockedPages + " / 6 Seiten beschrieben");
  }
}
