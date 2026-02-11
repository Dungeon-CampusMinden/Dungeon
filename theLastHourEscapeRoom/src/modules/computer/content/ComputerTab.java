package modules.computer.content;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogContext;
import modules.computer.ComputerDialog;
import modules.computer.ComputerStateComponent;
import modules.computer.ComputerStateLocal;

public abstract class ComputerTab extends Table {

  private final String key;
  private String title;
  private boolean closeable;
  private ComputerStateComponent sharedState;
  private DialogContext ctx;

  protected Skin skin;

  public ComputerTab(
      ComputerStateComponent sharedState, String key, String title, boolean closeable) {
    this.sharedState = sharedState;
    this.key = key;
    this.title = title;
    this.closeable = closeable;

    this.skin = UIUtils.defaultSkin();

    this.top();
    this.createActors();
  }

  public String key() {
    return key;
  }

  public String title() {
    return title;
  }

  public void title(String title) {
    this.title = title;
    ComputerDialog.getInstance().ifPresent(ComputerDialog::buildTabs);
  }

  public boolean closeable() {
    return closeable;
  }

  public void closeable(boolean closeable) {
    this.closeable = closeable;
    ComputerDialog.getInstance().ifPresent(ComputerDialog::buildTabs);
  }

  public ComputerStateComponent sharedState() {
    return sharedState;
  }

  public void setSharedState(ComputerStateComponent sharedState) {
    updateState(sharedState);
    this.sharedState = sharedState;
  }

  public ComputerStateLocal localState() {
    return ComputerStateLocal.Instance;
  }

  /** Creates and adds the actors to the tab. */
  protected abstract void createActors();

  /**
   * Updates the tab's content based on the new shared {@link ComputerStateComponent}.
   *
   * @param newStateComp The new shared ComputerStateComponent.
   */
  protected abstract void updateState(ComputerStateComponent newStateComp);

  /**
   * Sets the DialogContext for this tab, allowing it to access dialog-specific parameters and the owner dialog itself.
   * @param ctx The DialogContext to set for this tab
   */
  public void context(DialogContext ctx) {
    this.ctx = ctx;
  }

  /**
   * Retrieves the DialogContext associated with the ComputerDialog
   * @return The DialogContext for the dialog
   */
  public DialogContext context() {
    return ctx;
  }

  /**
   * Called when the tab is removed from the dialog.
   */
  public void onRemove() {

  }
}
