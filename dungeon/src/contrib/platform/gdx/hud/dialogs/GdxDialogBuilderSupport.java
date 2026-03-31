package contrib.platform.gdx.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.HeadlessDialogGroup;
import core.Game;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Shared helper methods for libGDX-backed dialog builders.
 *
 * <p>Keeps repeated headless fallback and skin access logic out of the individual builders.
 */
final class GdxDialogBuilderSupport {

  private GdxDialogBuilderSupport() {}

  static Skin defaultSkin() {
    return UIUtils.defaultSkin();
  }

  static Group build(Group headlessDialog, Supplier<? extends Group> gdxBuilder) {
    if (Game.isHeadless()) {
      return headlessDialog;
    }
    return gdxBuilder.get();
  }

  static HeadlessDialogGroup headless(String title, String text, String... buttons) {
    return new HeadlessDialogGroup(title, text, buttons);
  }

  static String[] buttons(String primary, String secondary, String[] additional) {
    List<String> allButtons = new ArrayList<>();
    if (primary != null) {
      allButtons.add(primary);
    }
    if (secondary != null) {
      allButtons.add(secondary);
    }
    if (additional != null) {
      allButtons.addAll(Arrays.asList(additional));
    }
    return allButtons.toArray(new String[0]);
  }
}
