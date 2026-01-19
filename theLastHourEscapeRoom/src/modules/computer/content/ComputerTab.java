package modules.computer.content;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

public abstract class ComputerTab extends Table {

  private final String key;
  private final String title;
  private final boolean closeable;

  public ComputerTab(String key, String title, boolean closeable){
    this.key = key;
    this.title = title;
    this.closeable = closeable;

    this.top();
    this.createActors();
  }

  public String key(){
    return key;
  }

  public String title(){
    return title;
  }

  public boolean closeable(){
    return closeable;
  }

  protected abstract void createActors();

}
