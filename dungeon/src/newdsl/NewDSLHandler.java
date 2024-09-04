package newdsl;

import newdsl.antlr.DSLParser;
import newdsl.ast.ASTNodes;
import newdsl.ast.ParseTreeVisitor;
import newdsl.common.DSLError;
import newdsl.common.DSLErrorHandler;
import newdsl.common.DSLException;
import newdsl.interpreter.DSLImporter;
import newdsl.interpreter.DSLInterpreter;
import newdsl.semanticanalysis.DSLValidationTraverser;
import newdsl.semanticanalysis.RefPhaseListener;
import newdsl.symboltable.SymbolTable;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class NewDSLHandler {

    public class SemanticAnalysisResult {
        SymbolTable symbolTable;
        List<DSLError> errors;

        public SemanticAnalysisResult(SymbolTable symbolTable, List<DSLError> errors) {
            this.symbolTable = symbolTable;
            this.errors = errors;
        }

        public SymbolTable getSymbolTable() {
            return symbolTable;
        }

        public List<DSLError> getErrors() {
            return errors;
        }
    }

    String filePath;

    public NewDSLHandler(String filePath) {
        String currentDir = System.getProperty("user.dir");
        this.filePath = currentDir + filePath;
    }

    public DSLInterpreter getInterpreter() throws DSLException {
        ArrayList<DSLError> errors = new ArrayList<>();
        DSLImporter importer = new DSLImporter(errors);

        try {
            importer.parseFile(filePath);
        } catch (IOException e) {
            errors.add(new DSLError(e.getMessage(), null));
        }

        DSLParser.StartContext mergedProgram = importer.getProgram();
        ParseTreeVisitor parseTreeVisitor = new ParseTreeVisitor(errors);
        ASTNodes.Visitable ast = parseTreeVisitor.visit(mergedProgram);

        ParseTreeWalker walker = new ParseTreeWalker();
        RefPhaseListener listener = new RefPhaseListener(parseTreeVisitor.symbolTable, errors);
        walker.walk(listener, mergedProgram);

        DSLValidationTraverser validator = new DSLValidationTraverser(parseTreeVisitor.symbolTable, errors);
        ast.accept(validator);

        if (!errors.isEmpty()) {
            DSLErrorHandler.printErrors(errors);
            throw new DSLException("Please fix the errors first.");
        }

        DSLInterpreter interpreter = new DSLInterpreter();
        ast.accept(interpreter);

        return interpreter;

    }

    public SemanticAnalysisResult getSemanticAnalysisResult() throws DSLException {
        ArrayList<DSLError> errors = new ArrayList<>();
        DSLImporter importer = new DSLImporter(errors);

        try {
            importer.parseFile(filePath);
        } catch (IOException e) {
            errors.add(new DSLError(e.getMessage(), null));
        }

        DSLParser.StartContext mergedProgram = importer.getProgram();
        ParseTreeVisitor parseTreeVisitor = new ParseTreeVisitor(errors);
        ASTNodes.Visitable ast = parseTreeVisitor.visit(mergedProgram);

        ParseTreeWalker walker = new ParseTreeWalker();
        RefPhaseListener listener = new RefPhaseListener(parseTreeVisitor.symbolTable, errors);
        walker.walk(listener, mergedProgram);

        DSLValidationTraverser validator = new DSLValidationTraverser(parseTreeVisitor.symbolTable, errors);
        ast.accept(validator);

        return new SemanticAnalysisResult(parseTreeVisitor.symbolTable, errors);

    }

}
