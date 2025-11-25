package mushRoom.modules.journal;

import contrib.hud.DialogUtils;
import contrib.item.Item;
import core.Entity;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;

public class JournalItem extends Item {

  private static final int BASE_PAGES_UNLOCKED = 3;

  private static final SimpleIPath inventoryPath = new SimpleIPath("objects/mailbox/mailbox_2.png");
  private static final SimpleIPath worldPath = new SimpleIPath("objects/mailbox/mailbox_2.png");

  private int unlockedPages = BASE_PAGES_UNLOCKED;

  public JournalItem() {
    super(
        "Notizbuch",
        BASE_PAGES_UNLOCKED + " / 6 Seiten beschrieben",
        new Animation(inventoryPath),
        new Animation(worldPath),
        1,
        1);
  }

  @Override
  public void use(final Entity user) {
    DialogUtils.showTextPopup("Dis da journal. Unlocked pages: " + unlockedPages, "Notizbuch");
  }

  //  public void setUnlockedPages(int unlockedPages) {
  //    this.unlockedPages = unlockedPages;
  //    this.description(unlockedPages + " / 6 Seiten beschrieben");
  //  }

  public void unlockPage() {
    this.unlockedPages++;
    this.description(unlockedPages + " / 6 Seiten beschrieben");
  }
}
