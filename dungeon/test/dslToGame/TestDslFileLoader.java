package dslToGame;

import static org.junit.Assert.*;

import entrypoint.DSLFileLoader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Set;
import org.junit.Test;

/** WTF? . */
public class TestDslFileLoader {

  private static final String PATH_TO_ASSETS_AS_STRING = "./test_resources/dslFileLoader";
  private static final String PATH_TO_DNGFILE_AS_STRING = PATH_TO_ASSETS_AS_STRING + "/simple.dng";

  private static final String PATH_TO_JAR_AS_STRING = PATH_TO_ASSETS_AS_STRING + "/testjar.jar";
  private static final String PATH_TO_EMPTY_DNGFILE_AS_STRING =
      PATH_TO_ASSETS_AS_STRING + "/empty.dng";
  private static final String PAHT_TO_TXTFILE_AS_STRING = PATH_TO_ASSETS_AS_STRING + "/test.txt";

  private static final Path PATH_TO_DNGFILE = Paths.get(".", "dslFileLoader", "simple.dng");
  private static final Path PATH_OF_FIRST_DNGFILE_IN_JAR =
      Paths.get(".", "dslFileLoader", "testjar.jar", "scripts", "first.dng");
  private static final Path PATH_OF_SECOND_DNGFILE_IN_JAR =
      Paths.get(".", "dslFileLoader", "testjar.jar", "scripts", "sub", "sub", "second.dng");
  private static final Path PATH_OF_TXTFILE_IN_JAR =
      Paths.get(".", "dslFileLoader", "testjar.jar", "scripts", "test.txt");

  private static final Path PATH_TO_EMPTY_DNGFILE = Paths.get(".", "dslFileLoader", "empty.dng");

  /** WTF? . */
  @Test
  public void processArguments_oneJar() throws IOException {
    String[] args = {PATH_TO_JAR_AS_STRING};
    Set<Path> paths = DSLFileLoader.processArguments(args);
    assertEquals(2, paths.size());
    assertTrue(paths.stream().anyMatch(p -> p.endsWith(PATH_OF_FIRST_DNGFILE_IN_JAR.normalize())));
    assertTrue(paths.stream().anyMatch(p -> p.endsWith(PATH_OF_SECOND_DNGFILE_IN_JAR.normalize())));
    assertFalse(paths.stream().anyMatch(p -> p.endsWith(PATH_OF_TXTFILE_IN_JAR.normalize())));
  }

  /** WTF? . */
  @Test
  public void processArguments_oneDSLFile() throws IOException {
    String[] args = {PATH_TO_DNGFILE_AS_STRING};
    Set<Path> paths = DSLFileLoader.processArguments(args);
    assertEquals(1, paths.size());
    Path p = (Path) paths.toArray()[0];
    assertTrue(p.endsWith(PATH_TO_DNGFILE.normalize()));
  }

  /** WTF? . */
  @Test
  public void processArguments_oneJarOneDSL() throws IOException {
    String[] args = {PATH_TO_JAR_AS_STRING, PATH_TO_DNGFILE_AS_STRING};
    Set<Path> paths = DSLFileLoader.processArguments(args);
    assertEquals(3, paths.size());
    assertTrue(paths.stream().anyMatch(p -> p.endsWith(PATH_OF_FIRST_DNGFILE_IN_JAR.normalize())));
    assertTrue(paths.stream().anyMatch(p -> p.endsWith(PATH_OF_SECOND_DNGFILE_IN_JAR.normalize())));
    assertTrue(paths.stream().anyMatch(p -> p.endsWith(PATH_TO_DNGFILE.normalize())));
    assertFalse(paths.stream().anyMatch(p -> p.endsWith(PATH_OF_TXTFILE_IN_JAR.normalize())));
  }

  /** WTF? . */
  @Test
  public void processArguments_mixed() throws IOException {
    String[] args = {
      PATH_TO_EMPTY_DNGFILE_AS_STRING, PATH_TO_JAR_AS_STRING, PATH_TO_DNGFILE_AS_STRING
    };
    Set<Path> paths = DSLFileLoader.processArguments(args);
    assertEquals(4, paths.size());
    assertTrue(paths.stream().anyMatch(p -> p.endsWith(PATH_OF_FIRST_DNGFILE_IN_JAR.normalize())));
    assertTrue(paths.stream().anyMatch(p -> p.endsWith(PATH_OF_SECOND_DNGFILE_IN_JAR.normalize())));
    assertTrue(paths.stream().anyMatch(p -> p.endsWith(PATH_TO_DNGFILE.normalize())));
    assertTrue(paths.stream().anyMatch(p -> p.endsWith(PATH_TO_EMPTY_DNGFILE.normalize())));
    assertFalse(paths.stream().anyMatch(p -> p.endsWith(PATH_OF_TXTFILE_IN_JAR.normalize())));
  }

  /** WTF? . */
  @Test
  public void processArguments_nonDSLFile() throws IOException {
    String[] args = {PAHT_TO_TXTFILE_AS_STRING};
    Set<Path> paths = DSLFileLoader.processArguments(args);
    assertEquals(0, paths.size());
  }

  /** WTF? . */
  @Test
  public void fileToString() {
    ClassLoader classLoader = getClass().getClassLoader();
    File f =
        new File(
            Objects.requireNonNull(
                    classLoader.getResource(PATH_TO_DNGFILE.toString().replace("\\", "/")))
                .getFile());
    String expectedContent =
        "some test text."
            + System.lineSeparator()
            + "some test text, second line."
            + System.lineSeparator()
            + System.lineSeparator()
            + "some test text, fourth line."
            + System.lineSeparator();

    String read = DSLFileLoader.fileToString(f);
    assertEquals(expectedContent, read);
  }

  /** WTF? . */
  @Test
  public void argFileToString() throws IOException {
    String[] args = {PATH_TO_DNGFILE_AS_STRING};
    Set<Path> paths = DSLFileLoader.processArguments(args);
    assertEquals(1, paths.size());
    Path p = (Path) paths.toArray()[0];
    String read = DSLFileLoader.fileToString(p.toFile());

    String expectedContent =
        "some test text."
            + System.lineSeparator()
            + "some test text, second line."
            + System.lineSeparator()
            + System.lineSeparator()
            + "some test text, fourth line."
            + System.lineSeparator();
    assertEquals(expectedContent, read);
  }

  /** WTF? . */
  @Test
  public void emptyFileToString() {
    ClassLoader classLoader = getClass().getClassLoader();
    File f =
        new File(
            Objects.requireNonNull(
                    classLoader.getResource(PATH_TO_EMPTY_DNGFILE.toString().replace("\\", "/")))
                .getFile());
    String expectedContent = "";
    String read = DSLFileLoader.fileToString(f);
    assertEquals(expectedContent, read);
  }
}
