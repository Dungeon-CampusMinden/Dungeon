package newdsl.interpreter;

import newdsl.common.CustomDSLErrorListener;
import newdsl.common.DSLError;
import newdsl.common.Utils;
import newdsl.antlr.DSLLexer;
import newdsl.antlr.DSLParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DSLImporter {

    private static final String DSL_FILE_EXTIONSION = ".task";

    private ArrayList<DSLError> errors;
    private Set<String> importedFiles = new HashSet<>();
    private CommonTokenStream tokens;
    private DSLLexer lexer;
    private DSLParser parser;
    private DSLParser.StartContext mergedProgram;

    public DSLImporter(ArrayList<DSLError> errors) {
        this.mergedProgram = new DSLParser.StartContext(null, -1);
        this.errors = errors;
    }

    public Path getPath(String[] args) throws FileNotFoundException {
        for (String arg : args) {
            Path path = Paths.get(arg);
            if (Files.exists(path)) {
                String fileName = path.getFileName().toString();
                if (fileName.endsWith(DSL_FILE_EXTIONSION)) {
                    return path.toAbsolutePath();
                }
            }
        }
        throw new FileNotFoundException(String.format("No suitable DSL file found. Make sure that it exists and uses the %s file extension.", DSL_FILE_EXTIONSION));
    }

    public void parseFile(String filePath) throws IOException {
        if (importedFiles.contains(filePath)) {
            System.err.println("No circular imports allowed: " + filePath);
            return; // Avoid circular imports
        }

        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            importedFiles.add(filePath);
            lexer = new DSLLexer(CharStreams.fromString(content));
            lexer.setTokenFactory(new DSLTokenFactory(filePath));
            tokens = new CommonTokenStream(lexer);
            parser = new DSLParser(tokens);

            parser.removeErrorListeners();
            parser.addErrorListener(new CustomDSLErrorListener(errors));

            DSLParser.StartContext startContext = parser.start();
            mergeProgramContexts(mergedProgram, startContext);

            processImports(startContext, filePath);
        } catch (IOException e) {
            throw e;
        }

    }

    private void processImports(DSLParser.StartContext startContext, String referencePath) {
        for (ParseTree child : startContext.children) {
            if (child instanceof DSLParser.Import_statementContext) {
                String relativePath = ((DSLParser.Import_statementContext) child).path.getText();
                Path relative = Paths.get(relativePath);
                Path reference = Paths.get(referencePath);
                String abolutePath = reference.getParent().resolve(relative).normalize().toString();
                try {
                    parseFile(abolutePath);
                } catch (IOException e) {
                    errors.add(new DSLError(String.format("Failed to import file: %s", abolutePath), null));
                }
            }
        }
    }

    private void mergeProgramContexts(DSLParser.StartContext main, DSLParser.StartContext toMerge) {
        for (ParseTree child : toMerge.children) {
            main.addAnyChild(child);
        }
    }

    public DSLParser.StartContext getProgram() {
        return mergedProgram;
    }
}
