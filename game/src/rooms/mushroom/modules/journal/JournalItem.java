package rooms.mushroom.modules.journal;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import engine.Entity;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.SimpleIPath;
import feature.components.InventoryComponent;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.inventory.Item;
import java.util.function.IntSupplier;
import rooms.mushroom.Sounds;
import rooms.mushroom.modules.EscapeRoomDialogTypes;
import rooms.mushroom.modules.mushrooms.Mushrooms;

/**
 * Item representing a journal that can be used to document discovered mushrooms.
 *
 * <p>The journal does not store its progress; the room owns the unlocked page count and hands the
 * journal a supplier for it.
 */
public class JournalItem extends Item {

  /** Number of pages that are unlocked when the player receives the journal. */
  public static final int BASE_PAGES_UNLOCKED = 2;

  private static final SimpleIPath inventoryPath =
      new SimpleIPath("items/rpg/item_book_blue_lines.png");

  private final IntSupplier unlockedPages;

  static {
    DialogFactory.register(EscapeRoomDialogTypes.JOURNAL, JournalItem::buildMushroomJournalDialog);
  }

  /**
   * Constructs a new JournalItem.
   *
   * @param unlockedPages supplies the number of pages the room has unlocked so far
   */
  public JournalItem(IntSupplier unlockedPages) {
    super("Notizbuch", "", new Animation(inventoryPath), new Animation(inventoryPath));
    this.unlockedPages = unlockedPages;
  }

  @Override
  public String description() {
    return unlockedPages.getAsInt() + " / 6 Seiten beschrieben";
  }

  @Override
  public void use(final Entity user) {
    openJournal(user, unlockedPages.getAsInt());
  }

  /**
   * Opens the journal UI if the given entity has a {@link JournalItem} in its inventory.
   *
   * @param user the entity to open the journal for
   */
  public static void openJournal(Entity user) {
    user.fetch(InventoryComponent.class)
        .flatMap(inventory -> inventory.itemOfClass(JournalItem.class))
        .ifPresent(item -> item.use(user));
  }

  private static void openJournal(Entity user, int unlockedPages) {
    DialogContext ctx =
        DialogContext.builder()
            .type(EscapeRoomDialogTypes.JOURNAL)
            .put("unlockedPages", unlockedPages)
            .put(DialogContextKeys.OWNER_ENTITY, user.id())
            .build();

    user.add(new UIComponent(ctx, true, user.id()));
  }

  private static Group buildMushroomJournalDialog(DialogContext dialogContext) {
    int unlockedPages = dialogContext.require("unlockedPages", Integer.class);

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

    engine.sound.Sounds.playUi(Sounds.OPEN_INVENTORY_SOUND);
    return bookUI;
  }
}
