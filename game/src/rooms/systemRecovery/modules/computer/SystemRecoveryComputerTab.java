package rooms.systemRecovery.modules.computer;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;

/** Base class for simple System Recovery computer tabs. */
public abstract class SystemRecoveryComputerTab extends Table {

  private final String key;
  private final String title;
  protected final Skin skin;
  private DialogContext context;

  protected SystemRecoveryComputerTab(String key, String title) {
    this.key = key;
    this.title = title;
    this.skin = UIUtils.defaultSkin();
    top();
  }

  String key() {
    return key;
  }

  String title() {
    return title;
  }

  protected DialogContext context() {
    return context;
  }

  void context(DialogContext context) {
    this.context = context;
  }

  protected abstract void createActors();
}
