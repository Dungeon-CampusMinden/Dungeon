package dsl.programmanalyzer;

import java.util.HashMap;
import java.util.List;

public class Relationship {
  Long startIdx;
  String name;
  List<Long> endIdxs;
  boolean forceIdxProperty;
  Object startObject;
  HashMap<String, Object> properties;

  public Relationship(Long startIdx, String name, List<Long> endIdxs, boolean forceIdxProperty) {
    this.startIdx = startIdx;
    this.name = name;
    // this.endLabel = endLabel;
    this.endIdxs = endIdxs;
    this.forceIdxProperty = forceIdxProperty;
    this.properties = new HashMap<>();
  }

  public Relationship(Long startIdx, String name, List<Long> endIdxs) {
    this(startIdx, name, endIdxs, false);
  }

  public void addProperties(HashMap<String, Object> properties) {
    this.properties = properties;
  }

  public HashMap<String, Object> getProperties() {
    return this.properties;
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
