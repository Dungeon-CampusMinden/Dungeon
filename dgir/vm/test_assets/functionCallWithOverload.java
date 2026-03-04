public class functionCallWithOverload {
  public static void main() {
    int result = add(5, 10);
    float resultfloat = add(5f, 10f);
  }

  public static int add(int a, int b) {
    return a + b;
  }

  public static float add(float a, float b) {
    return a + b;
  }
}
