package dslToGame;

import static org.junit.Assert.*;

import dslToGame.loadFiles.DslFileLoader;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public class TestDslFileLoader {

    @Test
    public void processArguments_oneJar() throws IOException {
        String pathToJar = "dslFileLoader/testjar.jar";
        String pathOfFirstFile = "scripts/first.dng";
        String pathOfSecondFile = "scripts/sub/sub/second.dng";
        String pathOfThirdFile = "scripts/test.txt";
        String[] args = {pathToJar};
        Set<Path> paths = DslFileLoader.processArguments(args);
        assertEquals(2, paths.size());
        assertTrue(paths.stream().anyMatch(p -> p.endsWith(pathOfFirstFile)));
        assertTrue(paths.stream().anyMatch(p -> p.endsWith(pathOfSecondFile)));
        assertFalse(paths.stream().anyMatch(p -> p.endsWith(pathOfThirdFile)));
    }

    @Test
    public void processArguments_oneDSLFIle() throws IOException {
        String pathToDng = "dslFileLoader/simple.dng";
        String[] args = {pathToDng};
        Set<Path> paths = DslFileLoader.processArguments(args);
        assertEquals(1, paths.size());
        Path p = (Path) paths.toArray()[0];
        assertTrue(p.endsWith(pathToDng));
    }

    @Test
    public void processArguments_oneJarOneDSL() throws IOException {
        String pathToJar = "dslFileLoader/testjar.jar";
        String pathToDng = "dslFileLoader/simple.dng";
        String pathOfFirstFile = "scripts/first.dng";
        String pathOfSecondFile = "scripts/sub/sub/second.dng";
        String pathOfThirdFile = "scripts/test.txt";
        String[] args = {pathToJar, pathToDng};
        Set<Path> paths = DslFileLoader.processArguments(args);
        assertEquals(3, paths.size());
        assertTrue(paths.stream().anyMatch(p -> p.endsWith(pathOfFirstFile)));
        assertTrue(paths.stream().anyMatch(p -> p.endsWith(pathOfSecondFile)));
        assertTrue(paths.stream().anyMatch(p -> p.endsWith(pathToDng)));
        assertFalse(paths.stream().anyMatch(p -> p.endsWith(pathOfThirdFile)));
    }

    @Test
    public void processArguments_mixed() throws IOException {
        String pathToJar = "dslFileLoader/testjar.jar";
        String pathToDng = "dslFileLoader/simple.dng";
        String pathToEmptyDng = "dslFileLoader/empty.dng";
        String pathOfFirstFile = "scripts/first.dng";
        String pathOfSecondFile = "scripts/sub/sub/second.dng";
        String pathOfThirdFile = "scripts/test.txt";
        String[] args = {pathToEmptyDng, pathToJar, pathToDng};
        Set<Path> paths = DslFileLoader.processArguments(args);
        assertEquals(4, paths.size());
        assertTrue(paths.stream().anyMatch(p -> p.endsWith(pathOfFirstFile)));
        assertTrue(paths.stream().anyMatch(p -> p.endsWith(pathOfSecondFile)));
        assertTrue(paths.stream().anyMatch(p -> p.endsWith(pathToDng)));
        assertTrue(paths.stream().anyMatch(p -> p.endsWith(pathToEmptyDng)));
        assertFalse(paths.stream().anyMatch(p -> p.endsWith(pathOfThirdFile)));
    }

    @Test
    public void processArguments_nonDSLFile() throws IOException {
        String pathToTxt = "dslFileLoader/test.txt";
        String[] args = {pathToTxt};
        Set<Path> paths = DslFileLoader.processArguments(args);
        assertEquals(0, paths.size());
    }

    @Test
    public void fileToString() {
        String simpleFile = "dslFileLoader/simple.dng";
        ClassLoader classLoader = getClass().getClassLoader();
        File f = new File(classLoader.getResource(simpleFile).getFile());
        String expectedContent =
                "some test text."
                        + System.lineSeparator()
                        + "some test text, second line."
                        + System.lineSeparator()
                        + System.lineSeparator()
                        + "some test text, fourth line."
                        + System.lineSeparator();

        String read = DslFileLoader.fileToString(f);
        System.out.println(expectedContent);
        System.out.println(read);
        assertEquals(expectedContent, read);
    }

    @Test
    public void emptyFileToString() {
        String empty = "dslFileLoader/empty.dng";
        ClassLoader classLoader = getClass().getClassLoader();
        File f = new File(classLoader.getResource(empty).getFile());
        String expectedContent = "";
        String read = DslFileLoader.fileToString(f);
        assertEquals(expectedContent, read);
    }
}
