package dslToGame;

import static org.junit.Assert.*;

import dslToGame.loadFiles.DslFileLoader;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class TestDslFileLoader {
    private static final Path PATH_TO_DNG = Paths.get(".", "dslFileLoader", "simple.dng");
    private static final Path PATH_TO_JAR = Paths.get(".", "dslFileLoader", "testjar.jar");
    private static final Path PATH_OF_FIRST_FILE =
            Paths.get(".", "dslFileLoader", "testjar.jar", "scripts", "first.dng");
    private static final Path PATH_OF_SECOND_FILE =
            Paths.get(".", "dslFileLoader", "testjar.jar", "scripts", "sub", "sub", "second.dng");
    private static final Path PATH_OF_THIRD_FILE =
            Paths.get(".", "dslFileLoader", "testjar.jar", "scripts", "test.txt");

    private static final Path PATH_TO_EMPTY_DNG = Paths.get(".", "dslFileLoader", "empty.dng");
    private static final Path PATH_TO_TXT = Paths.get(".", "dslFileLoader", "test.txt");

    @Test
    public void processArguments_oneJar() throws IOException {
        String[] args = {PATH_TO_JAR.toString()};
        Set<Path> paths = DslFileLoader.processArguments(args);
        assertEquals(2, paths.size());
        assertTrue(paths.stream().anyMatch(p -> p.endsWith(PATH_OF_FIRST_FILE.normalize())));
        assertTrue(paths.stream().anyMatch(p -> p.endsWith(PATH_OF_SECOND_FILE.normalize())));
        assertFalse(paths.stream().anyMatch(p -> p.endsWith(PATH_OF_THIRD_FILE.normalize())));
    }

    @Test
    public void processArguments_oneDSLFIle() throws IOException {
        String[] args = {PATH_TO_DNG.toString()};
        Set<Path> paths = DslFileLoader.processArguments(args);
        assertEquals(1, paths.size());
        Path p = (Path) paths.toArray()[0];
        assertTrue(p.endsWith(PATH_TO_DNG.normalize()));
    }

    @Test
    public void processArguments_oneJarOneDSL() throws IOException {
        String[] args = {PATH_TO_JAR.toString(), PATH_TO_DNG.toString()};
        Set<Path> paths = DslFileLoader.processArguments(args);
        assertEquals(3, paths.size());
        assertTrue(paths.stream().anyMatch(p -> p.endsWith(PATH_OF_FIRST_FILE.normalize())));
        assertTrue(paths.stream().anyMatch(p -> p.endsWith(PATH_OF_SECOND_FILE.normalize())));
        assertTrue(paths.stream().anyMatch(p -> p.endsWith(PATH_TO_DNG.normalize())));
        assertFalse(paths.stream().anyMatch(p -> p.endsWith(PATH_OF_THIRD_FILE.normalize())));
    }

    @Test
    public void processArguments_mixed() throws IOException {
        String[] args = {
            PATH_TO_EMPTY_DNG.toString(), PATH_TO_JAR.toString(), PATH_TO_DNG.toString()
        };
        Set<Path> paths = DslFileLoader.processArguments(args);
        assertEquals(4, paths.size());
        assertTrue(paths.stream().anyMatch(p -> p.endsWith(PATH_OF_FIRST_FILE.normalize())));
        assertTrue(paths.stream().anyMatch(p -> p.endsWith(PATH_OF_SECOND_FILE.normalize())));
        assertTrue(paths.stream().anyMatch(p -> p.endsWith(PATH_TO_DNG.normalize())));
        assertTrue(paths.stream().anyMatch(p -> p.endsWith(PATH_TO_EMPTY_DNG.normalize())));
        assertFalse(paths.stream().anyMatch(p -> p.endsWith(PATH_OF_THIRD_FILE.normalize())));
    }

    @Test
    public void processArguments_nonDSLFile() throws IOException {
        String[] args = {PATH_TO_TXT.toString()};
        Set<Path> paths = DslFileLoader.processArguments(args);
        assertEquals(0, paths.size());
    }

    @Test
    public void fileToString() {
        ClassLoader classLoader = getClass().getClassLoader();
        File f = new File(classLoader.getResource(PATH_TO_DNG.toString()).getFile());
        String expectedContent =
                "some test text."
                        + System.lineSeparator()
                        + "some test text, second line."
                        + System.lineSeparator()
                        + System.lineSeparator()
                        + "some test text, fourth line."
                        + System.lineSeparator();

        String read = DslFileLoader.fileToString(f);
        assertEquals(expectedContent, read);
    }

    @Test
    public void emptyFileToString() {
        ClassLoader classLoader = getClass().getClassLoader();
        File f = new File(classLoader.getResource(PATH_TO_EMPTY_DNG.toString()).getFile());
        String expectedContent = "";
        String read = DslFileLoader.fileToString(f);
        assertEquals(expectedContent, read);
    }
}
