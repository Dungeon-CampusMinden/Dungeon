package mushRoom.modules.journal;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import contrib.components.InventoryComponent;
import contrib.components.UIComponent;
import contrib.hud.DialogUtils;
import contrib.hud.UIUtils;
import contrib.item.Item;
import core.Entity;
import core.Game;
import core.utils.components.draw.TextureMap;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import mushRoom.Sounds;
import mushRoom.modules.mushrooms.Mushrooms;

public class JournalItem extends Item {

  private static final int BASE_PAGES_UNLOCKED = 2;

  private static final SimpleIPath inventoryPath = new SimpleIPath("items/rpg/item_book_blue_lines.png");

  private int unlockedPages = BASE_PAGES_UNLOCKED;

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

  public static void openJournal(Entity p) {
    p.fetch(InventoryComponent.class).ifPresent(ic -> {
      if (!ic.hasItem(JournalItem.class)) return;
      ic.itemOfClass(JournalItem.class).ifPresent(i -> {
        JournalItem journalItem = (JournalItem) i;
        openJournalUI(p, journalItem.unlockedPages);
        Sounds.OPEN_INVENTORY_SOUND.play();
      });
    });
  }

  private static void openJournalUI(Entity p, int unlockedPages) {
    Skin skin = UIUtils.defaultSkin();
    Texture bookTex = TextureMap.instance().textureAt(new SimpleIPath("images/open-book.png"));

    JournalUI bookUI = new JournalUI(skin, new TextureRegionDrawable(new TextureRegion(bookTex)));

    // Loop over all Mushrooms and add their entries
    for (Mushrooms mushroomType : Mushrooms.values()) {
      Texture t = TextureMap.instance().textureAt(new SimpleIPath(mushroomType.getTexturePath()));
      String text = "Ich kenne diesen Pilz noch nicht...";
      if (mushroomType.ordinal() < unlockedPages * 2) { // Each page has 2 mushrooms
        text = mushroomType.getDescription();
      }
      bookUI.addEntry(t, text);
    }

    p.remove(UIComponent.class);
    p.add(new UIComponent(bookUI, true, true));
  }


  @Override
  public boolean collect(Entity itemEntity, Entity collector) {
    return super.collect(itemEntity, collector);
  }

  public void unlockPage() {
    this.unlockedPages++;
    this.description(unlockedPages + " / 6 Seiten beschrieben");
  }
}
