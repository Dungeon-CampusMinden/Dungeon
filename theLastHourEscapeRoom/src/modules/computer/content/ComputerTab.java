package modules.computer.content;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import contrib.hud.UIUtils;
import modules.computer.ComputerDialog;
import modules.computer.ComputerStateComponent;

public abstract class ComputerTab extends Table {

  private final String key;
  private String title;
  private boolean closeable;
  private ComputerStateComponent sharedState;

  protected Skin skin;

  public ComputerTab(ComputerStateComponent sharedState, String key, String title, boolean closeable){
    this.sharedState = sharedState;
    this.key = key;
    this.title = title;
    this.closeable = closeable;

    this.skin = UIUtils.defaultSkin();

    this.top();
    this.createActors();
  }

  public String key(){
    return key;
  }

  public String title(){
    return title;
  }

  public void title(String title){
    this.title = title;
    ComputerDialog.getInstance().ifPresent(ComputerDialog::buildTabs);
  }

  public boolean closeable(){
    return closeable;
  }

  public void closeable(boolean closeable){
    this.closeable = closeable;
    ComputerDialog.getInstance().ifPresent(ComputerDialog::buildTabs);
  }

  public ComputerStateComponent sharedState(){
    return sharedState;
  }

  public void setSharedState(ComputerStateComponent sharedState){
    updateState(sharedState);
    this.sharedState = sharedState;
  }

  /**
   * Creates and adds the actors to the tab.
   */
  protected abstract void createActors();

  /**
   * Updates the tab's content based on the new shared {@link ComputerStateComponent}.
   * @param newStateComp The new shared ComputerStateComponent.
   */
  protected abstract void updateState(ComputerStateComponent newStateComp);
}
