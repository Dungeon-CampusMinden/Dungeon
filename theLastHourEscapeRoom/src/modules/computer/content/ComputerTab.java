package modules.computer.content;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogContext;
import modules.computer.ComputerDialog;
import modules.computer.ComputerStateComponent;
import modules.computer.ComputerStateLocal;

/** Abstract base class for tabs in the ComputerDialog. */
public abstract class ComputerTab extends Table {

  private final String key;
  private String title;
  private boolean closeable;
  private ComputerStateComponent sharedState;
  private DialogContext ctx;

  protected Skin skin;

  /**
   * Constructs a new ComputerTab with the specified parameters.
   *
   * @param sharedState The shared ComputerStateComponent that this tab will use to display and
   *     update computer state.
   * @param key A unique key identifying this tab, used for tab management in ComputerDialog
   * @param title The title of the tab to be displayed in the ComputerDialog's tab header
   * @param closeable Whether this tab can be closed by the user (if true, a close button will be
   *     shown on the tab header)
   */
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

  /**
   * Returns the unique key identifying this tab, used for tab management in ComputerDialog.
   *
   * @return the unique key for this tab
   */
  public String key() {
    return key;
  }

  /**
   * Returns the title of this tab to be displayed in the ComputerDialog's tab header.
   *
   * @return the title of this tab
   */
  public String title() {
    return title;
  }

  /**
   * Sets the title of this tab and triggers a rebuild of the ComputerDialog's tabs to update the
   * display.
   *
   * @param title the new title for this tab
   */
  public void title(String title) {
    this.title = title;
    ComputerDialog.getInstance().ifPresent(ComputerDialog::buildTabs);
  }

  /**
   * Returns whether this tab can be closed by the user. If true, a close button will be shown on
   * the tab header.
   *
   * @return true if this tab is closeable, false otherwise
   */
  public boolean closeable() {
    return closeable;
  }

  /**
   * Sets whether this tab can be closed by the user and triggers a rebuild of the ComputerDialog's
   * tabs to update the display.
   *
   * @param closeable true if this tab should be closeable, false otherwise
   */
  public void closeable(boolean closeable) {
    this.closeable = closeable;
    ComputerDialog.getInstance().ifPresent(ComputerDialog::buildTabs);
  }

  /**
   * Returns the shared ComputerStateComponent that this tab uses to display and update computer
   * state.
   *
   * @return the shared ComputerStateComponent for this tab
   */
  public ComputerStateComponent sharedState() {
    return sharedState;
  }

  /**
   * Sets the shared ComputerStateComponent for this tab and updates the tab's content based on the
   * new state.
   *
   * @param sharedState the new shared ComputerStateComponent to set for this tab
   */
  public void setSharedState(ComputerStateComponent sharedState) {
    updateState(sharedState);
    this.sharedState = sharedState;
  }

  /**
   * Returns the local ComputerStateLocal instance that this tab can use to access local state and
   * utility methods.
   *
   * @return the ComputerStateLocal instance for this tab
   */
  public ComputerStateLocal localState() {
    return ComputerStateLocal.getInstance();
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
   * Sets the DialogContext for this tab, allowing it to access dialog-specific parameters and the
   * owner dialog itself.
   *
   * @param ctx The DialogContext to set for this tab
   */
  public void context(DialogContext ctx) {
    this.ctx = ctx;
  }

  /**
   * Retrieves the DialogContext associated with the ComputerDialog.
   *
   * @return The DialogContext for the dialog
   */
  public DialogContext context() {
    return ctx;
  }

  /** Called when the tab is removed from the dialog. */
  public void onRemove() {}
}
