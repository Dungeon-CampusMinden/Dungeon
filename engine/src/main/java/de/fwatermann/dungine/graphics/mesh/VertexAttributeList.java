package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.utils.ReadOnlyIterator;
import java.util.Iterator;

public class VertexAttributeList
    implements Iterable<VertexAttribute>, Comparable<VertexAttributeList> {

  private VertexAttribute[] attributes;
  private Iterator<VertexAttribute> iterator;

  @Override
  public int compareTo(VertexAttributeList o) {
    return 0;
  }

  @Override
  public Iterator<VertexAttribute> iterator() {
    if(this.iterator == null) {
      this.iterator = new ReadOnlyIterator<>(this.attributes);
    }
    return this.iterator;
  }
}
