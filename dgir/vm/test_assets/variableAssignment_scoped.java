public class variableAssignment_scoped {
  public static void main() {
    int x = 5;
    {
      int y = 10;
      x = y;
    }
    int z = x + 5;
  }
}
