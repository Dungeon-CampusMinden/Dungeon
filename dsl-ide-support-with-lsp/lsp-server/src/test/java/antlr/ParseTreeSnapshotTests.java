package antlr;

import static org.junit.jupiter.api.Assertions.assertEquals;

import antlr.treePrinter.ParseTreeToString;
import antlr_gen.AntlrGrammarLexer;
import antlr_gen.AntlrGrammarParser;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/** Tests that all task types are parsed as expected. */
public class ParseTreeSnapshotTests {

  @TestFactory
  Collection<DynamicTest> snapshotTests() throws IOException {
    ArrayList<DynamicTest> result = new ArrayList<>();
    Path antlrTestsRoot = Path.of("src/test/resources/antlrParseTreeSnapshotTests");
    Files.walkFileTree(
        antlrTestsRoot,
        new SimpleFileVisitor<>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            if (file.endsWith("test.dng")) {
              Path txtFileWithExpected = file.resolveSibling("ParseTree.txt");

              if (Files.exists(txtFileWithExpected)) {
                String nonRootPackagePartsTheHelpedClassIsIn =
                    antlrTestsRoot.relativize(file.getParent()).toString().replace("\\", "/");
                result.add(
                    DynamicTest.dynamicTest(
                        nonRootPackagePartsTheHelpedClassIsIn,
                        () -> {
                          CharStream charStreamOfGivenFilePath = CharStreams.fromPath(file);
                          Lexer lexer = new AntlrGrammarLexer(charStreamOfGivenFilePath);
                          AntlrGrammarParser parser =
                              new AntlrGrammarParser(new CommonTokenStream(lexer));

                          ParseTree tree = parser.start();
                          String actual = ParseTreeToString.parseTreeToString(tree);
                          assertEquals(
                              Files.readString(txtFileWithExpected).replace("\r", ""), actual);
                        }));
              }
            }
            return super.visitFile(file, attrs);
          }
        });

    return result;
  }
}
