package dsl.parser.ast;

import java.util.ArrayList;
import java.util.List;

// TODO: temporary test
public class RelationshipRecorder {
  public static RelationshipRecorder instance = new RelationshipRecorder();

  private List<ParentOf> parentRelationShips = new ArrayList<>();

  public void add(Node parentNode, Node childNode, int idx) {
    parentRelationShips.add(new ParentOf(parentNode, childNode, idx));
  }

  public void clear() {
    this.parentRelationShips.clear();
  }

  public List<ParentOf> get() {
    return parentRelationShips;
  }
}
