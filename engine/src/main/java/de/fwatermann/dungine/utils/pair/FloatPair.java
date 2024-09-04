package de.fwatermann.dungine.utils.pair;

public record FloatPair(float a, float b) {

  public static FloatPair of(float a, float b) {
    return new FloatPair(a, b);
  }

  public Pair<Float, Float> getPair() {
    return new Pair<Float, Float>(this.a, this.b);
  }


}
