package de.fwatermann.dungine.utils.pair;

import org.joml.Vector3i;

public record Vector3iPair(Vector3i a, Vector3i b) {

  public static Vector3iPair of(Vector3i a, Vector3i b) {
    return new Vector3iPair(a, b);
  }

  public Pair<Vector3i, Vector3i> getPair() {
    return new Pair<Vector3i, Vector3i>(this.a, this.b);
  }

}
