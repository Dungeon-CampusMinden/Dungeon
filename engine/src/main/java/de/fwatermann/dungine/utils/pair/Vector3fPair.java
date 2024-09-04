package de.fwatermann.dungine.utils.pair;

import org.joml.Vector3f;

public record Vector3fPair(Vector3f a, Vector3f b) {

  public static Vector3fPair of(Vector3f a, Vector3f b) {
    return new Vector3fPair(a, b);
  }

  public Pair<Vector3f, Vector3f> getPair() {
    return new Pair<Vector3f, Vector3f>(this.a, this.b);
  }

}
