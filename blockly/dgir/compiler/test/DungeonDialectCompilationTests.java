import dgir.vm.dialect.io.IoRunners;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class DungeonDialectCompilationTests extends CompilerTestBase {
  @Test
  void heroMove() {
    String code =
"""
import Dungeon.Hero;

public class %ClassName {
  public static void main() {
    Hero.move();
  }
}
""";
    testSource(code);
  }

  @Test
  void allHeroOps() {
    String code =
"""
import Dungeon.Hero;

public class %ClassName {
  public static void main() {
    Hero.move();
    Hero.turnLeft();
    Hero.turnRight();
    Hero.useHere();
    Hero.useLeft();
    Hero.useRight();
    Hero.useUp();
    Hero.useDown();
    Hero.push();
    Hero.pull();
    Hero.dropClover();
    Hero.dropBreadCrumbs();
    Hero.pickUp();
    Hero.fireball();
    Hero.rest();
  }
}
""";
    testSource(code);
  }

  @Test
  void allIoOps() {
    String code =
"""
import Dungeon.IO;

public class %ClassName {
  public static void main() {
    IO.print("Hello, world!\\n");
    IO.println("Hello, world!");
    IO.printf("Hello, %s!\\n", "world");
    IO.printf("Hello, %s! the %snd\\n", "world", 2);
    String input = IO.nextLine();
    boolean bool = IO.nextBoolean();
    byte b = IO.nextByte();
    short s = IO.nextShort();
    int i = IO.nextInt();
    long l = IO.nextLong();
    float f = IO.nextFloat();
    double d = IO.nextDouble();
    IO.printf("You entered: %s, %b, %d, %d, %d, %d, %f, %f\\n", input, bool, b, s, i, l, f, d);
  }
}
""";
    String simulatedInput =
        "Sex-Dungeon\ntrue\n42\n12345\n67890\n1234567890123456789\n3.14\n2.71828\n";
    IoRunners.ConsoleInRunner.setInputStream(
        new ByteArrayInputStream(simulatedInput.getBytes(UTF_8)));
    testSource(code);
  }
}
