package core.utils.components.draw.state;

public class Signal {

  public final String signal;
  public final Object data;

  public Signal(String signal, Object data){
    this.signal = signal;
    this.data = data;
  }
  public Signal(String signal){
    this(signal, null);
  }

}
