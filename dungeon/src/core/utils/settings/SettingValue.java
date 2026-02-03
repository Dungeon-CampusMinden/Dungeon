package core.utils.settings;

import com.badlogic.gdx.scenes.scene2d.Actor;

import java.util.function.Consumer;

public abstract class SettingValue<T> {

  private String name;
  private T value;

  private Consumer<T> onChange;

  public SettingValue(String name, T defaultValue) {
    this.name = name;
    this.value = defaultValue;
  }

  public SettingValue(String name, T defaultValue, Consumer<T> onChange) {
    this(name, defaultValue);
    this.onChange = onChange;
  }

  public String name(){
    return name;
  }
  public void name(String name){
    this.name = name;
  }

  public T value() {
    return value;
  }

  public void value(T value) {
    if(onChange != null){
      onChange.accept(value);
    }
    this.value = value;
  }

  public void onChange(Consumer<T> listener){
    this.onChange = listener;
  }

  public abstract Actor toUIActor();

}
