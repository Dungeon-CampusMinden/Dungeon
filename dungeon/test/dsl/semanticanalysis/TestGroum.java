package dsl.semanticanalysis;

import dsl.helpers.Helpers;
import dsl.parser.ast.FuncDefNode;
import dsl.parser.ast.ReturnStmtNode;
import dsl.parser.ast.TermNode;
import dsl.parser.ast.VarDeclNode;
import dsl.semanticanalysis.groum.GroumPrinter;
import dsl.semanticanalysis.groum.TemporaryGroumBuilder;
import dsl.semanticanalysis.symbol.FunctionSymbol;
import dsl.semanticanalysis.symbol.Symbol;
import org.junit.Assert;
import org.junit.Test;

public class TestGroum {
  @Test
  public void poc() {
    String program = """
      fn add(int x, int y) -> int {
        var sum = x + y;
        var derp = y + sum;
        return derp;
      }
      """;

    var ast = Helpers.getASTFromString(program);
    var result = Helpers.getSymtableForAST(ast);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    FunctionSymbol functionSymbol = (FunctionSymbol) fs.resolve("add");
    var xParamSymbol = functionSymbol.resolve("x");
    var yParamSymbol = functionSymbol.resolve("y");

    var node = (FuncDefNode)symbolTable.getCreationAstNode(functionSymbol);
    var varDecl = (VarDeclNode)node.getStmts().get(0);
    var idDef = varDecl.getIdentifier();
    var sumSymbol = symbolTable.getSymbolsForAstNode(idDef).get(0);
    Assert.assertNotEquals(Symbol.NULL, sumSymbol);

    var term = (TermNode)varDecl.getRhs();
    var xRef = term.getLhs();
    var xTermSymbol = symbolTable.getSymbolsForAstNode(xRef).get(0);
    Assert.assertEquals(xParamSymbol, xTermSymbol);

    var yRef = term.getRhs();
    var yTermSymbol = symbolTable.getSymbolsForAstNode(yRef).get(0);
    Assert.assertEquals(yParamSymbol, yTermSymbol);

    /*var returnStmt = (ReturnStmtNode)node.getStmts().get(1);
    var expr = returnStmt.getInnerStmtNode();
    var exprSymbol = symbolTable.getSymbolsForAstNode(expr).get(0);
    Assert.assertEquals(sumSymbol, exprSymbol);*/

    TemporaryGroumBuilder builder = new TemporaryGroumBuilder();
    var groum = builder.walk(ast, symbolTable, env);
    GroumPrinter p = new GroumPrinter();
    String str = p.print(groum);

  }
}
