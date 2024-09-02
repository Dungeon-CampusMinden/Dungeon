package de.fwatermann.dungine.utils;

public record IntPair(int a, int b) {

  public static IntPair of(int a, int b) {
    return new IntPair(a, b);
  }

  public Pair<Integer, Integer> getPair() {
    return new Pair<Integer, Integer>(this.a, this.b);
  }

}
