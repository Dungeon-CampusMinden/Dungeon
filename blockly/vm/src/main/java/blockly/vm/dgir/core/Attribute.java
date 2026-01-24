package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

public abstract class Attribute implements Serializable {
  public abstract AttributeName.Impl createImpl();

  public Attribute() {
  }
}
