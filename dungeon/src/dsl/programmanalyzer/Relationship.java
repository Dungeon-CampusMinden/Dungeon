package dsl.programmanalyzer;

import java.util.HashMap;
import java.util.List;

public class Relationship {
  Long startIdx;
  String name;
  List<Long> endIdxs;
  boolean forceIdxProperty;
  Object startObject;
  Relate.Direction direction;
  HashMap<String, Object> properties;

  public Relationship(Long startIdx, String name, List<Long> endIdxs, Relate.Direction direction, boolean forceIdxProperty) {
    this.startIdx = startIdx;
    this.name = name;
    // this.endLabel = endLabel;
    this.endIdxs = endIdxs;
    this.forceIdxProperty = forceIdxProperty;
    this.direction = direction;
    this.properties = new HashMap<>();
  }

  public Relationship(Long startIdx, String name, List<Long> endIdxs, Relate.Direction direction) {
    this(startIdx, name, endIdxs, direction, false);
  }

  public void addProperties(HashMap<String, Object> properties) {
    this.properties = properties;
  }

  public HashMap<String, Object> getProperties() {
    return this.properties;
  }

  public Relate.Direction direction() {
    return this.direction;
  }

  public void startObject(Object object) {
    this.startObject = object;
  }

  public Long startId() {
    return this.startIdx;
  }

  public String name() {
    return this.name;
  }

  public List<Long> endIdxs() {
    return this.endIdxs;
  }

  public boolean forceIdxProperty() {
    return this.forceIdxProperty;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
