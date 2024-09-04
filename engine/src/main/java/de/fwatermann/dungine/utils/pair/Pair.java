package de.fwatermann.dungine.utils.pair;

public class Pair<A, B> {

  private A a;
  private B b;

  /**
   * Constructs a Pair with the specified values.
   *
   * @param a the first value
   * @param b the second value
   */
  public Pair(A a, B b) {
    this.a = a;
    this.b = b;
  }

  /**
   * Gets the first value of the pair.
   *
   * @return the first value
   */
  public A a() {
    return this.a;
  }

  /**
   * Gets the second value of the pair.
   *
   * @return the second value
   */
  public B b() {
    return this.b;
  }

  /**
   * Sets the first value of the pair.
   *
   * @param a the new first value
   * @return this pair instance for method chaining
   */
  public Pair<A, B> a(A a) {
    this.a = a;
    return this;
  }

  /**
   * Sets the second value of the pair.
   *
   * @param b the new second value
   * @return this pair instance for method chaining
   */
  public Pair<A, B> b(B b) {
    this.b = b;
    return this;
  }

  /**
   * Sets both values of the pair.
   *
   * @param a the new first value
   * @param b the new second value
   * @return this pair instance for method chaining
   */
  public Pair<A, B> a(A a, B b) {
    this.a = a;
    this.b = b;
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if(this.getClass().isAssignableFrom(obj.getClass())) {
      Pair<?, ?> pair = (Pair<?, ?>) obj;
      return this.a.equals(pair.a) && this.b.equals(pair.b);
    }
    return false;
  }
}
