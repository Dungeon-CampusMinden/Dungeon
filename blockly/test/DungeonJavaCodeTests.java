import org.junit.jupiter.api.Test;

import java.io.IOException;

public class DungeonJavaCodeTests extends DungeonCompilerTestBase {
  @Test
  void testHelloWorld() throws InterruptedException, IOException {
    sendCode(
        """
          public static void main() {
            IO.println("Hello, world!");
            Hero.move();
        }
        """);
    waitForCompletion();
  }
}
