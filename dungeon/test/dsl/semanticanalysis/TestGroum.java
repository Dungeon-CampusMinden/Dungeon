package dsl.semanticanalysis;

import dsl.helpers.Helpers;
import dsl.parser.ast.FuncDefNode;
import dsl.parser.ast.TermNode;
import dsl.parser.ast.VarDeclNode;
import dsl.semanticanalysis.groum.*;
import dsl.semanticanalysis.symbol.FunctionSymbol;
import dsl.semanticanalysis.symbol.Symbol;
import org.junit.Assert;
import org.junit.Test;

import javax.naming.ldap.Control;

public class TestGroum {
  @Test
  public void simple() {
    String program =
        """
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

    var node = (FuncDefNode) symbolTable.getCreationAstNode(functionSymbol);
    var varDecl = (VarDeclNode) node.getStmts().get(0);
    var idDef = varDecl.getIdentifier();
    var sumSymbol = symbolTable.getSymbolsForAstNode(idDef).get(0);
    Assert.assertNotEquals(Symbol.NULL, sumSymbol);

    var term = (TermNode) varDecl.getRhs();
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

  @Test
  public void whileLoop() {
    String program =
        """
      fn add(int x, int y) -> int {
        var sum = x;
        while (sum < 42) {
          sum = sum + 1;
        }
        return sum;
      }
      """;

    var ast = Helpers.getASTFromString(program);
    var result = Helpers.getSymtableForAST(ast);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporaryGroumBuilder builder = new TemporaryGroumBuilder();
    var groum = builder.walk(ast, symbolTable, env);
    GroumPrinter p = new GroumPrinter();
    String str = p.print(groum);

    var sourceNodes = groum.sourceNodes();
    Assert.assertEquals(2, sourceNodes.size());
    var firstParam = sourceNodes.get(0);
    Assert.assertEquals(ActionNode.ActionType.parameterInstantiation, ((ActionNode)firstParam).actionType());
    Assert.assertEquals(1, firstParam.outgoing().size());
    var secondParam = sourceNodes.get(1);
    Assert.assertEquals(ActionNode.ActionType.parameterInstantiation, ((ActionNode)secondParam).actionType());
    Assert.assertEquals(1, secondParam.outgoing().size());

    var paramRef = firstParam.outgoing().get(0).end();
    Assert.assertEquals(ActionNode.ActionType.referencedInExpression, ((ActionNode)paramRef).actionType());

    Assert.assertEquals(1, paramRef.outgoing().size());

    var expr = paramRef.outgoing().get(0).end();
    Assert.assertEquals(ActionNode.ActionType.expression, ((ActionNode)expr).actionType());

    var defNode = expr.outgoing().get(0).end();
    Assert.assertEquals(ActionNode.ActionType.definition, ((ActionNode)defNode).actionType());
    Assert.assertEquals(2, defNode.outgoing().size());

    var ref = defNode.outgoing().get(0).end();
    Assert.assertEquals(ActionNode.ActionType.referencedInExpression, ((ActionNode)ref).actionType());
    var refId = ((ActionNode)ref).referencedInstanceId();
    var defId = ((ActionNode)defNode).referencedInstanceId();
    Assert.assertEquals(defId, refId);

    var constRef = defNode.outgoing().get(1).end();
    Assert.assertEquals(ActionNode.ActionType.constRef, ((ActionNode)constRef).actionType());

    var whileExpr = ref.outgoing().get(0).end();
    Assert.assertEquals(ActionNode.ActionType.expression, ((ActionNode)whileExpr).actionType());

    var whileNode = whileExpr.outgoing().get(0).end();
    Assert.assertEquals(ControlNode.ControlType.whileLoop, ((ControlNode)whileNode).controlType());
    var block = whileNode.outgoing().get(0).end();
    Assert.assertEquals(ControlNode.ControlType.block, ((ControlNode)block).controlType());
    var refInBlock = block.outgoing().get(0).end();
    Assert.assertEquals(ActionNode.ActionType.referencedInExpression, ((ActionNode)refInBlock).actionType());
    var exprOfDefInBlock = refInBlock.outgoing().get(0).end();
    Assert.assertEquals(ActionNode.ActionType.expression, ((ActionNode)exprOfDefInBlock).actionType());

    var reDef = exprOfDefInBlock.outgoing().get(0).end();
    Assert.assertEquals(ActionNode.ActionType.definition, ((ActionNode)reDef).actionType());

    var returnRef = reDef.outgoing().get(0).end();
    Assert.assertEquals(ActionNode.ActionType.referencedInExpression, ((ActionNode)returnRef).actionType());

    var returnNode = returnRef.outgoing().get(0).end();
    Assert.assertEquals(ControlNode.ControlType.returnStmt, ((ControlNode)returnNode).controlType());

    // check scoping
    // while:
    Assert.assertTrue(whileNode.children().contains(whileExpr));
    Assert.assertTrue(whileNode.children().contains(block));

    // block:
    Assert.assertTrue(block.children().contains(exprOfDefInBlock));
    Assert.assertTrue(block.children().contains(reDef));
  }

  @Test
  public void listDefinition() {

    String program =
      """
    fn add(int x, int y) -> int[] {
      var list = [1,x,y];
      return list;
    }
    """;

    var ast = Helpers.getASTFromString(program);
    var result = Helpers.getSymtableForAST(ast);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporaryGroumBuilder builder = new TemporaryGroumBuilder();
    var groum = builder.walk(ast, symbolTable, env);
    GroumPrinter p = new GroumPrinter();
    String str = p.print(groum);

    var sourceNodes = groum.sourceNodes();
    Assert.assertEquals(2, sourceNodes.size());

    var xParam = sourceNodes.get(0);
    Assert.assertEquals(3, xParam.outgoing().size());

    var yParam = sourceNodes.get(1);

    var constRef = xParam.outgoing().get(0).end();
    Assert.assertEquals(ActionNode.ActionType.constRef, ((ActionNode)constRef).actionType());

    var xRef = xParam.outgoing().get(1).end();
    Assert.assertEquals(ActionNode.ActionType.referencedInExpression, ((ActionNode)xRef).actionType());
    Assert.assertEquals(((ActionNode)xParam).referencedInstanceId(), ((ActionNode) xRef).referencedInstanceId());

    var yRef = xParam.outgoing().get(2).end();
    Assert.assertEquals(ActionNode.ActionType.referencedInExpression, ((ActionNode)yRef).actionType());
    Assert.assertEquals(((ActionNode)yParam).referencedInstanceId(), ((ActionNode) yRef).referencedInstanceId());

    var expr = constRef.outgoing().get(0).end();
    Assert.assertEquals(ActionNode.ActionType.expression, ((ActionNode)expr).actionType());

    var def = expr.outgoing().get(0).end();
    Assert.assertEquals(ActionNode.ActionType.definition, ((ActionNode)def).actionType());
    DefinitionAction defAction = (DefinitionAction) def;
    var type = defAction.instancedType();
    Assert.assertEquals("int[]", type.getName());
  }

  @Test
  public void setDefinition() {

    String program =
      """
    fn add(int x, int y) -> int[] {
      var list = <1,x,y>;
      return list;
    }
    """;

    var ast = Helpers.getASTFromString(program);
    var result = Helpers.getSymtableForAST(ast);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporaryGroumBuilder builder = new TemporaryGroumBuilder();
    var groum = builder.walk(ast, symbolTable, env);
    GroumPrinter p = new GroumPrinter();
    String str = p.print(groum);

    var sourceNodes = groum.sourceNodes();
    Assert.assertEquals(2, sourceNodes.size());

    var xParam = sourceNodes.get(0);
    Assert.assertEquals(3, xParam.outgoing().size());

    var yParam = sourceNodes.get(1);

    var constRef = xParam.outgoing().get(0).end();
    Assert.assertEquals(ActionNode.ActionType.constRef, ((ActionNode)constRef).actionType());

    var xRef = xParam.outgoing().get(1).end();
    Assert.assertEquals(ActionNode.ActionType.referencedInExpression, ((ActionNode)xRef).actionType());
    Assert.assertEquals(((ActionNode)xParam).referencedInstanceId(), ((ActionNode) xRef).referencedInstanceId());

    var yRef = xParam.outgoing().get(2).end();
    Assert.assertEquals(ActionNode.ActionType.referencedInExpression, ((ActionNode)yRef).actionType());
    Assert.assertEquals(((ActionNode)yParam).referencedInstanceId(), ((ActionNode) yRef).referencedInstanceId());

    var expr = constRef.outgoing().get(0).end();
    Assert.assertEquals(ActionNode.ActionType.expression, ((ActionNode)expr).actionType());

    var def = expr.outgoing().get(0).end();
    Assert.assertEquals(ActionNode.ActionType.definition, ((ActionNode)def).actionType());
    DefinitionAction defAction = (DefinitionAction) def;
    var type = defAction.instancedType();
    Assert.assertEquals("int<>", type.getName());
  }
}
