package dsl.semanticanalysis;

import dsl.helpers.Helpers;
import dsl.parser.ast.FuncDefNode;
import dsl.parser.ast.TermNode;
import dsl.parser.ast.VarDeclNode;
import dsl.semanticanalysis.environment.GameEnvironment;
import dsl.semanticanalysis.groum.*;
import dsl.semanticanalysis.groum.node.*;
import dsl.semanticanalysis.symbol.FunctionSymbol;
import dsl.semanticanalysis.symbol.Symbol;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import org.junit.Assert;
import org.junit.Test;

public class TestGroum {
  private static final Path testLibPath = Path.of("test_resources/testlib");
  private static final String tempImgDirectory = "temp-img";

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

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
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

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    var groum = builder.walk(ast, symbolTable, env);
    GroumPrinter p = new GroumPrinter();
    String str = p.print(groum);

    var sourceNodes = groum.sourceNodes();
    // only 'beginFunc' Node expected
    Assert.assertEquals(1, sourceNodes.size());
    var beginFunc = sourceNodes.get(0);
    var paramNodes = beginFunc.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_TEMPORAL);
    Assert.assertEquals(2, paramNodes.size());
    var firstParam = paramNodes.get(0);
    Assert.assertEquals(
        ActionNode.ActionType.parameterInstantiation, ((ActionNode) firstParam).actionType());
    Assert.assertEquals(1, firstParam.outgoing().size());
    var secondParam = paramNodes.get(1);
    Assert.assertEquals(
        ActionNode.ActionType.parameterInstantiation, ((ActionNode) secondParam).actionType());
    Assert.assertEquals(1, secondParam.outgoing().size());

    var paramRef = firstParam.outgoing().get(0).end();
    Assert.assertEquals(
        ActionNode.ActionType.referencedInExpression, ((ActionNode) paramRef).actionType());

    Assert.assertEquals(1, paramRef.outgoing().size());

    var expr = paramRef.outgoing().get(0).end();
    Assert.assertEquals(ActionNode.ActionType.expression, ((ActionNode) expr).actionType());

    var defNode = expr.outgoing().get(0).end();
    Assert.assertEquals(ActionNode.ActionType.definition, ((ActionNode) defNode).actionType());
    Assert.assertEquals(2, defNode.outgoing().size());

    var ref = defNode.outgoing().get(0).end();
    Assert.assertEquals(
        ActionNode.ActionType.referencedInExpression, ((ActionNode) ref).actionType());
    var refId = ((ActionNode) ref).referencedInstanceId();
    var defId = ((ActionNode) defNode).referencedInstanceId();
    Assert.assertEquals(defId, refId);

    var constRef = defNode.outgoing().get(1).end();
    Assert.assertEquals(ActionNode.ActionType.constRef, ((ActionNode) constRef).actionType());

    var whileExpr = ref.outgoing().get(0).end();
    Assert.assertEquals(ActionNode.ActionType.expression, ((ActionNode) whileExpr).actionType());

    var whileNode = whileExpr.outgoing().get(0).end();
    Assert.assertEquals(ControlNode.ControlType.whileLoop, ((ControlNode) whileNode).controlType());
    var block = whileNode.outgoing().get(0).end();
    Assert.assertEquals(ControlNode.ControlType.block, ((ControlNode) block).controlType());
    var refInBlock = block.outgoing().get(0).end();
    Assert.assertEquals(
        ActionNode.ActionType.referencedInExpression, ((ActionNode) refInBlock).actionType());
    var exprOfDefInBlock = refInBlock.outgoing().get(0).end();
    Assert.assertEquals(
        ActionNode.ActionType.expression, ((ActionNode) exprOfDefInBlock).actionType());

    var reDef = exprOfDefInBlock.outgoing().get(0).end();
    Assert.assertEquals(ActionNode.ActionType.definition, ((ActionNode) reDef).actionType());

    var returnRef = reDef.outgoing().get(0).end();
    Assert.assertEquals(
        ActionNode.ActionType.referencedInExpression, ((ActionNode) returnRef).actionType());

    var returnNode = returnRef.outgoing().get(0).end();
    Assert.assertEquals(
        ControlNode.ControlType.returnStmt, ((ControlNode) returnNode).controlType());

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

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    var groum = builder.walk(ast, symbolTable, env);
    GroumPrinter p = new GroumPrinter();
    String str = p.print(groum);

    var sourceNodes = groum.sourceNodes();
    Assert.assertEquals(1, sourceNodes.size());

    var funcStartNode = sourceNodes.get(0);
    var paramNodes = funcStartNode.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_TEMPORAL);
    Assert.assertEquals(2, paramNodes.size());

    var xParam = paramNodes.get(0);
    Assert.assertEquals(3, xParam.outgoing().size());

    var yParam = paramNodes.get(1);

    var constRef = xParam.outgoing().get(0).end();
    Assert.assertEquals(ActionNode.ActionType.constRef, ((ActionNode) constRef).actionType());

    var xRef = xParam.outgoing().get(1).end();
    Assert.assertEquals(
        ActionNode.ActionType.referencedInExpression, ((ActionNode) xRef).actionType());
    Assert.assertEquals(
        ((ActionNode) xParam).referencedInstanceId(), ((ActionNode) xRef).referencedInstanceId());

    var yRef = xParam.outgoing().get(2).end();
    Assert.assertEquals(
        ActionNode.ActionType.referencedInExpression, ((ActionNode) yRef).actionType());
    Assert.assertEquals(
        ((ActionNode) yParam).referencedInstanceId(), ((ActionNode) yRef).referencedInstanceId());

    var expr = constRef.outgoing().get(0).end();
    Assert.assertEquals(ActionNode.ActionType.expression, ((ActionNode) expr).actionType());

    var def = expr.outgoing().get(0).end();
    Assert.assertEquals(ActionNode.ActionType.definition, ((ActionNode) def).actionType());
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

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    var groum = builder.walk(ast, symbolTable, env);
    GroumPrinter p = new GroumPrinter();
    String str = p.print(groum);

    var sourceNodes = groum.sourceNodes();
    Assert.assertEquals(1, sourceNodes.size());
    var funcBegin = sourceNodes.get(0);
    var paramNodes = funcBegin.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_TEMPORAL);
    Assert.assertEquals(2, paramNodes.size());

    var xParam = paramNodes.get(0);
    Assert.assertEquals(3, xParam.outgoing().size());

    var yParam = paramNodes.get(1);

    var constRef = xParam.outgoing().get(0).end();
    Assert.assertEquals(ActionNode.ActionType.constRef, ((ActionNode) constRef).actionType());

    var xRef = xParam.outgoing().get(1).end();
    Assert.assertEquals(
        ActionNode.ActionType.referencedInExpression, ((ActionNode) xRef).actionType());
    Assert.assertEquals(
        ((ActionNode) xParam).referencedInstanceId(), ((ActionNode) xRef).referencedInstanceId());

    var yRef = xParam.outgoing().get(2).end();
    Assert.assertEquals(
        ActionNode.ActionType.referencedInExpression, ((ActionNode) yRef).actionType());
    Assert.assertEquals(
        ((ActionNode) yParam).referencedInstanceId(), ((ActionNode) yRef).referencedInstanceId());

    var expr = constRef.outgoing().get(0).end();
    Assert.assertEquals(ActionNode.ActionType.expression, ((ActionNode) expr).actionType());

    var def = expr.outgoing().get(0).end();
    Assert.assertEquals(ActionNode.ActionType.definition, ((ActionNode) def).actionType());
    DefinitionAction defAction = (DefinitionAction) def;
    var type = defAction.instancedType();
    Assert.assertEquals("int<>", type.getName());
  }

  @Test
  public void graph() {

    String program =
        """
      single_choice_task t1 {
        description: "t1",
        answers: [ "test", "other test"],
        correct_answer_index: 0
      }

      single_choice_task t2 {
        description: "t2",
        answers: [ "test", "other test"],
        correct_answer_index: 0
      }

      single_choice_task t3 {
        description: "t3",
        answers: [ "test", "other test"],
        correct_answer_index: 0
      }

      single_choice_task t4 {
        description: "t4",
        answers: [ "test", "other test"],
        correct_answer_index: 0
      }

      single_choice_task t5 {
        description: "t5",
        answers: [ "test", "other test"],
        correct_answer_index: 0
      }

      single_choice_task t6 {
        description: "t6",
        answers: [ "test", "other test"],
        correct_answer_index: 0
      }

      graph g {
        t1 -> t2 [type=seq];
        t3,t4 -> t5 [type=seq];
        t6;
      }
      """;

    var gameEnv = new GameEnvironment();
    var ast = Helpers.getASTFromString(program, gameEnv);

    var result = Helpers.getSymtableForAST(ast, gameEnv);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    var groum = builder.walk(ast, symbolTable, env);
    GroumPrinter p = new GroumPrinter();
    String str = p.print(groum);
  }

  @Test
  public void funcCall() {

    String program = """
      fn test(string x)  {
        print(x);
      }
      """;

    var gameEnv = new GameEnvironment();
    var ast = Helpers.getASTFromString(program, gameEnv);

    var result = Helpers.getSymtableForAST(ast, gameEnv);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new dsl.semanticanalysis.groum.TemporalGroumBuilder();
    var groum = builder.walk(ast, symbolTable, env);
    GroumPrinter p = new GroumPrinter();
    String str = p.print(groum);
  }

  @Test
  public void methodCall() {

    String program =
        """
      fn test(entity ent, quest_item item, single_choice_task t) {
        var ic = ent.inventory_component;
        var noType = ent.mark_as_task_container(t, "hello");

        ent.inventory_component.add_item(item);
        var size = t.get_content().size();
      }
      """;

    var gameEnv = new GameEnvironment();
    var ast = Helpers.getASTFromString(program, gameEnv);

    var result = Helpers.getSymtableForAST(ast, gameEnv);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    var groum = builder.walk(ast, symbolTable, env);
    GroumPrinter p = new GroumPrinter();
    String str = p.print(groum);
  }

  @Test
  public void singleMethodCall() {

    String program =
        """
      fn test(entity ent, quest_item item) {
        ent.inventory_component.add_item(item);
      }
      """;

    var gameEnv = new GameEnvironment();
    var ast = Helpers.getASTFromString(program, gameEnv);

    var result = Helpers.getSymtableForAST(ast, gameEnv);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    var groum = builder.walk(ast, symbolTable, env);
    GroumPrinter p = new GroumPrinter();
    String str = p.print(groum);
  }

  @Test
  public void prototypeDef() {
    String program =
        """
      entity_type knight_type {
          draw_component {
              path: "character/blue_knight"
          },
          hitbox_component {},
          position_component{},
          interaction_component{
              radius: 1.5
          },
          task_component{}
      }

      entity_type wizard_type {
          draw_component {
              path: "character/wizard"
          },
          hitbox_component {},
          position_component{},
          interaction_component{
              radius: 1.5
          },
          task_component{}
      }

      fn test() -> entity {
        var knight = instantiate_named(knight_type, "Questgeber");
        var wizard = instantiate_named(wizard_type, "Zauberererer");
        return knight;
      }
      """;

    var gameEnv = new GameEnvironment();
    var ast = Helpers.getASTFromString(program, gameEnv);

    var result = Helpers.getSymtableForAST(ast, gameEnv);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    var groum = builder.walk(ast, symbolTable, env);
    GroumPrinter p = new GroumPrinter();
    String str = p.print(groum);
  }

  @Test
  public void itemPrototypeDef() {
    String program =
        """
      item_type scroll_type {
          display_name: "Eine Schriftrolle",
          description: "Lies mich",
          texture_path: "items/book/wisdom_scroll.png"
      }

      item_type mushroom_type {
          display_name: "Ein Pilz",
          description: "Iss mich (nicht)",
          texture_path: "items/resource/toadstool.png"
      }

      fn build_task_single_chest(single_choice_task t) -> entity<><> {
        var return_set : entity<><>;
        var room_set : entity<>;

        var content = t.get_content().get(0);
        var item = build_quest_item(scroll_type, content);
        place_quest_item(item, room_set);

        return_set.add(room_set);
        return return_set;
      }
      """;

    var gameEnv = new GameEnvironment();
    var ast = Helpers.getASTFromString(program, gameEnv);

    var result = Helpers.getSymtableForAST(ast, gameEnv);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    var groum = builder.walk(ast, symbolTable, env);
    GroumPrinter p = new GroumPrinter();
    String str = p.print(groum);
  }

  @Test
  public void forLoop() {
    String program =
        """
      fn test() {
        var my_list = [1,2,3];
        for int entry in my_list {
          print(entry);
        }
      }
      """;

    var gameEnv = new GameEnvironment();
    var ast = Helpers.getASTFromString(program, gameEnv);

    var result = Helpers.getSymtableForAST(ast, gameEnv);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    var groum = builder.walk(ast, symbolTable, env);
    GroumPrinter p = new GroumPrinter();
    String str = p.print(groum);
  }

  @Test
  public void countingForLoop() {
    String program =
        """
      fn test() {
        var my_list = [1,2,3];
        for int entry in my_list count counter {
          print(entry);
        }
      }
      """;

    var gameEnv = new GameEnvironment();
    var ast = Helpers.getASTFromString(program, gameEnv);

    var result = Helpers.getSymtableForAST(ast, gameEnv);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    var groum = builder.walk(ast, symbolTable, env);
    GroumPrinter p = new GroumPrinter();
    String str = p.print(groum);
  }

  @Test
  public void conditionalIf() {
    String program =
        """
      fn test(int x) {
        if (x == 0) {
          print("Hello");
        }
      }
      """;

    var gameEnv = new GameEnvironment();
    var ast = Helpers.getASTFromString(program, gameEnv);

    var result = Helpers.getSymtableForAST(ast, gameEnv);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    var groum = builder.walk(ast, symbolTable, env);
    GroumPrinter p = new GroumPrinter();
    String str = p.print(groum);
  }

  @Test
  public void conditionalIfElse() {
    String program =
        """
      fn test(int x) {
        if (x == 0) {
          print("Hello");
        } else {
          print("World");
        }
      }
      """;

    var gameEnv = new GameEnvironment();
    var ast = Helpers.getASTFromString(program, gameEnv);

    var result = Helpers.getSymtableForAST(ast, gameEnv);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    var groum = builder.walk(ast, symbolTable, env);
    GroumPrinter p = new GroumPrinter();
    String str = p.print(groum);
  }

  @Test
  public void importFunc() {
    String program =
        """
      #import "test.dng":test_fn_param as my_func
      #import "test.dng":my_ent_type as my_type

      fn test(int x) {
        my_func(x);
      }
      """;

    var gameEnv = new GameEnvironment(testLibPath);
    var ast = Helpers.getASTFromString(program, gameEnv);

    var result = Helpers.getSymtableForAST(ast, gameEnv);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    // TODO: do this for all files!
    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    var groum = builder.walk(ast, symbolTable, env);
    GroumPrinter p = new GroumPrinter();
    String str = p.print(groum);
  }

  @Test
  public void funcValueSetting() {
    String program =
        """
      #import "test.dng":my_ent_type as my_type

      single_choice_task t1 {
          description: "Task1",
          answers: ["1", "HELLO", "3"],
          correct_answer_index: 2,
          scenario_builder: mock_builder
      }

      graph g {
          t1
      }

      dungeon_config c {
          dependency_graph: g
      }

      fn mock_builder(single_choice_task t) -> entity<><> {
          var return_set : entity<><>;
          var room_set : entity<>;

          var my_ent : entity;
          my_ent = instantiate(my_type);
          room_set.add(my_ent);
          return_set.add(room_set);
          return return_set;
      }
      """;

    var gameEnv = new GameEnvironment(testLibPath);
    var ast = Helpers.getASTFromString(program, gameEnv);

    var result = Helpers.getSymtableForAST(ast, gameEnv);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    // TODO: do this for all files!
    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    var groum = builder.walk(ast, symbolTable, env);
    GroumPrinter p = new GroumPrinter();
    String str = p.print(groum);
  }

  @Test
  public void simpleDataDependencies() {
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

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum);
  }

  @Test
  public void loopDataDependency() {
    String program =
        """
    fn add(int x, int y) {
      while y {
        y = x;
      }

      var other_y = y;
      var sum = 2 + 4;
      y = sum;
    }
    """;

    var ast = Helpers.getASTFromString(program);
    var result = Helpers.getSymtableForAST(ast);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum);
    write(finalizedGroumStr, "final_groum.dot");
  }

  @Test
  public void dataDependencyConditional() {
    String program =
        """
    fn add(int x, int y) -> int {
      if x {
        y = 42;
      } else {
        y = 123;
      }
      var sum = x + y;
      y = 21;
      return y;
    }
    """;

    var ast = Helpers.getASTFromString(program);
    var result = Helpers.getSymtableForAST(ast);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, true);
    write(finalizedGroumStr, "final_groum.dot");
  }

  @Test
  // TODO: test
  public void dataDependencyConditionalNestedIf() {
    String program =
        """
    // param y def idx: 2
    fn add(int x, int y, int z) -> int {
      if x {
        // y def idx: 10
        y = 42;
      } else if z {
        // y def idx: 18
        y = 123;
      } else {
        // y def idx: 24
        y = 321;
      }
      var sum = x + y;
      y = 21;
      return y;
    }
    """;

    var ast = Helpers.getASTFromString(program);
    var result = Helpers.getSymtableForAST(ast);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, true);
    write(finalizedGroumStr, "final_groum.dot");
  }

  @Test
  // TODO: redefinition broken!!
  public void dataDependencySequentialConditional() {
    String program =
        """
  // y param idx: 3
  fn add(int x, int y, int z) -> int {
    if x {
      // idx: 11
      y = 42;
    } else {
      // idx: 16
      y = 321;
    }

    if x {
      // idx: 22
      y = 1;
    }

    // param ref idx: 23
    print(y);

    if x {
      // idx: 32
      y = 2;
    } else if z {
      // idx: 40
      y = 3;
    } else {
      // idx: 45
      y = 4;
    }

    // ref idx: 46
    print(y);

    // idx: 51
    y = 21;
    return y;
  }
  """;

    var ast = Helpers.getASTFromString(program);
    var result = Helpers.getSymtableForAST(ast);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, false);
    write(finalizedGroumStr, "final_groum.dot");

    // tests:
    var paramDef = findNodeByProcessIdx(finalizedGroum, 3);
    var firstIfDef = findNodeByProcessIdx(finalizedGroum, 11);
    var firstElseDef = findNodeByProcessIdx(finalizedGroum, 16);
    var secondIfDef = findNodeByProcessIdx(finalizedGroum, 22);
    var firstPrintParamRef = findNodeByProcessIdx(finalizedGroum, 23);
    var thirdIfDef = findNodeByProcessIdx(finalizedGroum, 32);
    var elseIfDef = findNodeByProcessIdx(finalizedGroum, 40);
    var secondElseDef = findNodeByProcessIdx(finalizedGroum, 45);
    var secondPrintParamRef = findNodeByProcessIdx(finalizedGroum, 46);
    var finalDef = findNodeByProcessIdx(finalizedGroum, 51);

    // check param redefs
    var paramRedefs = paramDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertEquals(2, paramRedefs.size());
    Assert.assertTrue(paramRedefs.contains(firstIfDef));
    Assert.assertTrue(paramRedefs.contains(firstElseDef));

    // check first if-else redefs
    var firstIfDefRedefs = firstIfDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertEquals(4, firstIfDefRedefs.size());
    Assert.assertTrue(firstIfDefRedefs.contains(secondIfDef));
    Assert.assertTrue(firstIfDefRedefs.contains(thirdIfDef));
    Assert.assertTrue(firstIfDefRedefs.contains(elseIfDef));
    Assert.assertTrue(firstIfDefRedefs.contains(secondElseDef));

    var firstElseDefRedefs =
        firstElseDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertEquals(4, firstIfDefRedefs.size());
    Assert.assertTrue(firstElseDefRedefs.contains(secondIfDef));
    Assert.assertTrue(firstElseDefRedefs.contains(thirdIfDef));
    Assert.assertTrue(firstElseDefRedefs.contains(elseIfDef));
    Assert.assertTrue(firstElseDefRedefs.contains(secondElseDef));

    // check first print param refs
    var firstPrintParamRefs =
        firstPrintParamRef.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(3, firstPrintParamRefs.size());
    Assert.assertTrue(firstPrintParamRefs.contains(firstIfDef));
    Assert.assertTrue(firstPrintParamRefs.contains(firstElseDef));
    Assert.assertTrue(firstPrintParamRefs.contains(secondIfDef));

    // check only one read on first defs
    var firstIfDefReads = firstIfDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(3, firstIfDefReads.size());

    var firstElseDefReads = firstElseDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(3, firstElseDefReads.size());

    var secondIfDefReads = secondIfDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(3, secondIfDefReads.size());

    // check second print param refs
    var secondPrintParamRefs =
        secondPrintParamRef.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(3, secondPrintParamRefs.size());
    Assert.assertTrue(secondPrintParamRefs.contains(thirdIfDef));
    Assert.assertTrue(secondPrintParamRefs.contains(elseIfDef));
    Assert.assertTrue(secondPrintParamRefs.contains(secondElseDef));

    // check final redefs
    var finalRedefs = finalDef.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertEquals(3, finalRedefs.size());
    Assert.assertTrue(finalRedefs.contains(thirdIfDef));
    Assert.assertTrue(finalRedefs.contains(elseIfDef));
    Assert.assertTrue(finalRedefs.contains(secondElseDef));
  }

  @Test
  // TODO: actually broken, final y def overwrites the first redef, which should not be visible
  // afterwards..
  public void dataDependencyConditionalShadowing() {
    String program =
        """
      // param idx: 3
      fn add(int x, int y, int z) -> int {
        // redef idx: 7
        y = 1;
        if x {
          // idx: 14
          y = 2;
        } else if z {
          // idx: 22
          y = 3;
        } else {
          // idx: 27
          y = 4;
        }

        // param ref idx: 28
        print(y);

        // redef idx: 33
        y = 21;
        return y;
      }
      """;

    var ast = Helpers.getASTFromString(program);
    var result = Helpers.getSymtableForAST(ast);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, true);
    write(finalizedGroumStr, "final_groum.dot");

    // tests
    var firstRedef = findNodeByProcessIdx(finalizedGroum, 7);
    var secondRedef = findNodeByProcessIdx(finalizedGroum, 14);
    var thirdRedef = findNodeByProcessIdx(finalizedGroum, 22);
    var forthRedef = findNodeByProcessIdx(finalizedGroum, 27);
    var paramRef = findNodeByProcessIdx(finalizedGroum, 28);
    var finalRedef = findNodeByProcessIdx(finalizedGroum, 33);

    // shadowing of first redefinition
    var firstRedefShadowing = firstRedef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertEquals(3, firstRedefShadowing.size());
    Assert.assertEquals(secondRedef, firstRedefShadowing.get(0));
    Assert.assertEquals(thirdRedef, firstRedefShadowing.get(1));
    Assert.assertEquals(forthRedef, firstRedefShadowing.get(2));

    var firstRedefReads = firstRedef.getOutgoingOfType(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(0, firstRedefReads.size());

    // param references
    var paramReads = paramRef.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(3, paramReads.size());
    Assert.assertTrue(paramReads.contains(secondRedef));
    Assert.assertTrue(paramReads.contains(thirdRedef));
    Assert.assertTrue(paramReads.contains(forthRedef));

    // final redef
    var finalRedefs = finalRedef.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertEquals(3, finalRedefs.size());
    Assert.assertTrue(finalRedefs.contains(secondRedef));
    Assert.assertTrue(finalRedefs.contains(thirdRedef));
    Assert.assertTrue(finalRedefs.contains(forthRedef));
  }

  @Test
  // TODO: test
  public void dataDependencyConditionalSequentialIfElse() {
    String program =
        """
    fn add(int x, int y, int z) -> int {
      if x {
        y = 42;
        if z {
          y = 56;
        }
        print(y);
      } else {
        y = 123;
      }

      if x {
        y = 1;
      } else {
        y = 2;
        {{{
          y = 66;
        }}}
      }

      print(y);
      y = 21;
      return y;
    }
    """;

    var ast = Helpers.getASTFromString(program);
    var result = Helpers.getSymtableForAST(ast);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, true);
    write(finalizedGroumStr, "final_groum.dot");
  }

  @Test
  // TODO: does not work, y gets redefined by final y def... fucking hell
  public void dataDependencyBlock() {
    String program =
        """
  //y idx: 3
  fn add(int x, int y, int z) -> int {
    if x {
      // idx: 11
      y = 42;
      if z {
        // idx: 17
        y = 56;
        {
          // idx: 21
          y = 12;
          // idx: 24
          y = 1;
        }
        {{{
          // idx: 30
          y = 4321;
        }}}
        // ref idx: 32
        print(y);
      }
    } else {
      // idx: 38
      y = 123;
    }

    // y ref idx: 40
    var sum = x + y;
    // idx: 45
    y = 21;
    // ref idx: 46
    return y;
  }
  """;

    var ast = Helpers.getASTFromString(program);
    var result = Helpers.getSymtableForAST(ast);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, true);
    write(finalizedGroumStr, "final_groum.dot");

    // tests
    var paramDef = findNodeByProcessIdx(finalizedGroum, 3);
    var firstIfDef = findNodeByProcessIdx(finalizedGroum, 11);
    var secondIfDef = findNodeByProcessIdx(finalizedGroum, 17);
    var firstBlockDef = findNodeByProcessIdx(finalizedGroum, 21);
    var secondBlockDef = findNodeByProcessIdx(finalizedGroum, 24);
    var thirdBlockDef = findNodeByProcessIdx(finalizedGroum, 30);
    var firstPrintParamRef = findNodeByProcessIdx(finalizedGroum, 31);
    var elseDef = findNodeByProcessIdx(finalizedGroum, 38);
    var termYRef = findNodeByProcessIdx(finalizedGroum, 40);
    var sumDef = findNodeByProcessIdx(finalizedGroum, 42);
    var finalRedef = findNodeByProcessIdx(finalizedGroum, 45);
    var returnRef = findNodeByProcessIdx(finalizedGroum, 46);

    // check param redefs
    var paramRedefs = paramDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertEquals(2, paramRedefs.size());
    Assert.assertTrue(paramRedefs.contains(firstIfDef));
    Assert.assertTrue(paramRedefs.contains(elseDef));

    // check first redef redefs
    var firstIfDefRedefs = firstIfDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertEquals(2, firstIfDefRedefs.size());
    Assert.assertTrue(firstIfDefRedefs.contains(secondIfDef));
    Assert.assertTrue(firstIfDefRedefs.contains(finalRedef));

    // check first def reads
    var firstIfDefReads = firstIfDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(3, firstIfDefReads.size());
    Assert.assertTrue(firstIfDefReads.contains(termYRef));
    Assert.assertTrue(firstIfDefReads.contains(sumDef));

    // check second def redefs
    var secondIfDefRedefs = secondIfDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertEquals(1, secondIfDefRedefs.size());
    Assert.assertTrue(secondIfDefRedefs.contains(firstBlockDef));

    // check no reads on second def
    var secondDefReads = secondIfDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(0, secondDefReads.size());

    // check first block redefs
    var firstBlockRedefs = firstBlockDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertEquals(1, firstBlockRedefs.size());
    Assert.assertTrue(firstBlockRedefs.contains(secondBlockDef));

    // check no reads on first block def
    var firstBlockDefReads =
        firstBlockDef.getOutgoingOfType(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(0, firstBlockDefReads.size());

    // check second block redefs
    var secondBlockDefRedefs =
        secondBlockDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertEquals(1, secondBlockDefRedefs.size());
    Assert.assertTrue(secondBlockDefRedefs.contains(thirdBlockDef));

    // check reads on third block def
    var thirdBlockDefReads =
        thirdBlockDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(6, thirdBlockDefReads.size());
    Assert.assertTrue(thirdBlockDefReads.contains(termYRef));
    Assert.assertTrue(thirdBlockDefReads.contains(sumDef));
    Assert.assertTrue(thirdBlockDefReads.contains(firstPrintParamRef));

    // check redefs on third block def
    var thirdBlockDefRedefs =
        thirdBlockDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertEquals(1, thirdBlockDefRedefs.size());
    Assert.assertTrue(thirdBlockDefRedefs.contains(finalRedef));

    // check final redefs
    var finalRedefs = finalRedef.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertEquals(3, finalRedefs.size());
    Assert.assertTrue(finalRedefs.contains(thirdBlockDef));
    Assert.assertTrue(finalRedefs.contains(elseDef));
    Assert.assertTrue(finalRedefs.contains(firstIfDef));
  }

  @Test
  // TODO: test
  public void dataDependencyMultiBlock() {
    String program =
        """
  fn add(int x, int y, int z) -> int {
    if x {
      y = 1;
      {
        z = 21;
        {{{
          y = 2;
          z = 31;
        }}}
        var test = y;
      }
      print(y);
    } else {
      y = 5;
      z = 41;
    }

    var sum = z + y;
    y = 6;
    return y;
  }
  """;

    var ast = Helpers.getASTFromString(program);
    var result = Helpers.getSymtableForAST(ast);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, true);
    write(finalizedGroumStr, "final_groum.dot");
  }

  @Test
  public void dataDependencyConditionalComplex() {
    String program =
        """
        // x param idx: 2
        fn test(int x, int y, int z) {
        	if x {
        	  // idx: 10
        	  x = 1;
        		if y {
        		  // idx: 17
        			x = 12;
        			if z {
        			  // idx: 23
        				x = 123;
        			}
        			// idx: 26
        			x = 1234;
        			// ref idx: 28
        			print(x);
        		} else {
        		  // idx: 34
        			x = 42;
        		}
        	}
        	// ref idx: 35
        	print(x);
        }
    """;

    var ast = Helpers.getASTFromString(program);
    var result = Helpers.getSymtableForAST(ast);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, false);
    write(finalizedGroumStr, "final_groum.dot");

    // tests
    var paramDef = findNodeByProcessIdx(finalizedGroum, 2);
    var firstRedef = findNodeByProcessIdx(finalizedGroum, 10);
    var secondRedef = findNodeByProcessIdx(finalizedGroum, 17);
    var thirdRedef = findNodeByProcessIdx(finalizedGroum, 23);
    var forthRedef = findNodeByProcessIdx(finalizedGroum, 26);
    var fifthRedef = findNodeByProcessIdx(finalizedGroum, 34);
    var firstPrintParamRef = findNodeByProcessIdx(finalizedGroum, 27);
    var secondPrintParamRef = findNodeByProcessIdx(finalizedGroum, 35);

    // check param reads
    var paramReads = paramDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(5, paramReads.size());
    Assert.assertTrue(paramReads.contains(secondPrintParamRef));

    // check param redef
    var paramRedef = paramDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertEquals(1, paramRedef.size());
    Assert.assertTrue(paramRedef.contains(firstRedef));

    // check first redef redefs
    var firstRedefRedefs = firstRedef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertEquals(2, firstRedefRedefs.size());
    Assert.assertTrue(firstRedefRedefs.contains(fifthRedef));
    Assert.assertTrue(firstRedefRedefs.contains(secondRedef));

    // check second redef redefs
    var secondRedefRedefs = secondRedef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertEquals(2, secondRedefRedefs.size());
    Assert.assertTrue(secondRedefRedefs.contains(thirdRedef));
    Assert.assertTrue(secondRedefRedefs.contains(forthRedef));

    var secondRedefsReads = secondRedef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(0, secondRedefsReads.size());

    // check third redef redefs
    var thirdRedefRedefs = thirdRedef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertEquals(1, thirdRedefRedefs.size());

    // check forth redefs reads
    var forthRedefsReads = forthRedef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(2, firstRedefRedefs.size());
    Assert.assertTrue(forthRedefsReads.contains(firstPrintParamRef));
    Assert.assertTrue(forthRedefsReads.contains(secondPrintParamRef));

    // check referenced values in final print statement
    var secondPrintsRefs =
        secondPrintParamRef.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(3, secondPrintsRefs.size());
    Assert.assertTrue(secondPrintsRefs.contains(fifthRedef));
    Assert.assertTrue(secondPrintsRefs.contains(forthRedef));
    Assert.assertTrue(secondPrintsRefs.contains(paramDef));
  }

  @Test
  public void dataDependencyConditionalRedef() {
    String program =
        """
      // param y idx: 3
      fn test(int x, int y, int z) {
        if x {
          if z {
            // def action idx: 14
            y = 1;
            // print param ref idx: 16
            print(y);
          }
          // def action idx: 20
          y = 2;
        } else {
          // def action idx: 25
          y = 3;
        }

        // ref in expression idx: 26
        print(y);
      }
      """;

    var ast = Helpers.getASTFromString(program);
    var result = Helpers.getSymtableForAST(ast);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, true);
    write(finalizedGroumStr, "final_groum.dot");

    // tests:
    var paramYDefNode = findNodeByProcessIdx(finalizedGroum, 3);
    var nestedIfDefNode = findNodeByProcessIdx(finalizedGroum, 14);
    var nestedFuncParamRefNode = findNodeByProcessIdx(finalizedGroum, 15);
    var ifDefNode = findNodeByProcessIdx(finalizedGroum, 20);

    var elseDefNode = findNodeByProcessIdx(finalizedGroum, 25);
    var funcParamRefNode = findNodeByProcessIdx(finalizedGroum, 26);

    var redefsOfParam = paramYDefNode.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertEquals(3, redefsOfParam.size());

    var startsOfRedefNestedIfDefNode =
        nestedIfDefNode.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertEquals(1, startsOfRedefNestedIfDefNode.size());
    Assert.assertEquals(paramYDefNode, startsOfRedefNestedIfDefNode.get(0));

    // check for print refererence
    var endsOfDataRefsNestedIf =
        nestedIfDefNode.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(3, endsOfDataRefsNestedIf.size());
    Assert.assertEquals(nestedFuncParamRefNode, endsOfDataRefsNestedIf.get(0));

    var startsOfRedefIfDefNode =
        ifDefNode.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertEquals(2, startsOfRedefIfDefNode.size());
    Assert.assertTrue(startsOfRedefIfDefNode.contains(paramYDefNode));
    Assert.assertTrue(startsOfRedefIfDefNode.contains(nestedIfDefNode));

    // check for final print reference
    var endsOfDataRefIf = ifDefNode.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(3, endsOfDataRefIf.size());
    Assert.assertEquals(funcParamRefNode, endsOfDataRefIf.get(0));

    var startsOfRedefElseNode =
        elseDefNode.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertEquals(1, startsOfRedefElseNode.size());
    Assert.assertEquals(paramYDefNode, startsOfRedefElseNode.get(0));

    // check for final print reference
    var endsOfDataRefElse = elseDefNode.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(3, endsOfDataRefElse.size());
    Assert.assertEquals(funcParamRefNode, endsOfDataRefElse.get(0));
  }

  @Test
  // TODO: works in isolation but not together with other tests, it's an issue with
  //  processing order of nodes..
  public void functionCall() {
    String program =
        """
        // x idx: 10
        // y idx: 11
        // z idx: 12
        fn add(int x, int y, int z) {
          // redef idx: 20
          // func call idx: 18
          x = other_func(x, y + z);
          print(x);
        }

        fn other_func(int x, int y) -> int {
          return x + y;
        }
      """;

    var ast = Helpers.getASTFromString(program);
    var result = Helpers.getSymtableForAST(ast);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, true);
    write(finalizedGroumStr, "final_groum.dot");

    // tests
    var paramXDef = findNodeByProcessIdx(finalizedGroum, 10);
    var paramYDef = findNodeByProcessIdx(finalizedGroum, 11);
    var paramZDef = findNodeByProcessIdx(finalizedGroum, 12);
    var funcCall = findNodeByProcessIdx(finalizedGroum, 18);
    var xRedef = findNodeByProcessIdx(finalizedGroum, 20);

    var funcCallReads = funcCall.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertTrue(funcCallReads.contains(paramXDef));
    Assert.assertTrue(funcCallReads.contains(paramYDef));
    Assert.assertTrue(funcCallReads.contains(paramZDef));

    var xRedefReads = xRedef.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertTrue(xRedefReads.contains(paramXDef));
    Assert.assertTrue(xRedefReads.contains(paramYDef));
    Assert.assertTrue(xRedefReads.contains(paramZDef));
  }

  @Test
  public void propertyAccessDifferentInstanceIds() {
    String program =
        """
        fn func(entity ent1, entity ent2, int x) {
          // count reference idx: 6
          var c1 = ent1.inventory_component.count;
          // count reference idx: 10
          var c2 = ent2.inventory_component.count;

          // count def idx: 17
          ent1.inventory_component.count = x;
          // count def idx: 22
          ent2.inventory_component.count = 21;
        }
      """;

    var ast = Helpers.getASTFromString(program);
    var result = Helpers.getSymtableForAST(ast);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, false);
    write(finalizedGroumStr, "final_groum.dot");

    // tests
    var firstCountAccess = (PropertyAccessAction) findNodeByProcessIdx(finalizedGroum, 6);
    var secondCountAccess = (PropertyAccessAction) findNodeByProcessIdx(finalizedGroum, 10);
    Assert.assertNotEquals(
        firstCountAccess.propertyInstanceId, secondCountAccess.propertyInstanceId);

    var firstCountWrite = (DefinitionAction) findNodeByProcessIdx(finalizedGroum, 17);
    var secondCountWrite = (DefinitionAction) findNodeByProcessIdx(finalizedGroum, 22);
    Assert.assertEquals(
        firstCountAccess.propertyInstanceId, firstCountWrite.referencedInstanceId());
    Assert.assertEquals(
        secondCountAccess.propertyInstanceId, secondCountWrite.referencedInstanceId());
  }

  @Test
  public void propertyAccessChained() {
    String program =
        """
      // param def idx: 2
      fn func(entity ent) {
        // t def idx: 7
        // task_content_component access idx: 3
        // content access idx: 4
        // task access idx: 5
        var t = ent.task_content_component.content.task;
      }
    """;

    var ast = Helpers.getASTFromString(program);
    var result = Helpers.getSymtableForAST(ast);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, true);
    write(finalizedGroumStr, "final_groum.dot");

    // tests
    var tDef = (DefinitionAction) findNodeByProcessIdx(finalizedGroum, 7);
    var paramDef = findNodeByProcessIdx(finalizedGroum, 2);
    var taskContentComponentAccess = findNodeByProcessIdx(finalizedGroum, 3);
    var contentAccess = findNodeByProcessIdx(finalizedGroum, 4);
    var taskAccess = findNodeByProcessIdx(finalizedGroum, 5);
    var tDefReads = tDef.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(4, tDefReads.size());
    Assert.assertTrue(tDefReads.contains(paramDef));
    Assert.assertTrue(tDefReads.contains(taskContentComponentAccess));
    Assert.assertTrue(tDefReads.contains(contentAccess));
    Assert.assertTrue(tDefReads.contains(taskAccess));
  }

  @Test
  public void propertyAccessChainedTwice() {
    String program =
        """
      // param def idx: 2
      fn func(entity ent) {
        // t def idx: 7
        // task_content_component access idx: 3
        // content access idx: 4
        // task access idx: 5
        var t = ent.task_content_component.content.task;
        var x = ent.task_content_component.content.task;
      }
    """;

    var ast = Helpers.getASTFromString(program);
    var result = Helpers.getSymtableForAST(ast);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, true);
    write(finalizedGroumStr, "final_groum.dot");

    // tests
    var tDef = (DefinitionAction) findNodeByProcessIdx(finalizedGroum, 7);
    var paramDef = findNodeByProcessIdx(finalizedGroum, 2);
    var taskContentComponentAccess = findNodeByProcessIdx(finalizedGroum, 3);
    var contentAccess = findNodeByProcessIdx(finalizedGroum, 4);
    var taskAccess = findNodeByProcessIdx(finalizedGroum, 5);
    var tDefReads = tDef.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(4, tDefReads.size());
    Assert.assertTrue(tDefReads.contains(paramDef));
    Assert.assertTrue(tDefReads.contains(taskContentComponentAccess));
    Assert.assertTrue(tDefReads.contains(contentAccess));
    Assert.assertTrue(tDefReads.contains(taskAccess));
  }

  @Test
  public void propertyAccessWrite() {
    String program =
        """
      // param c idx: 3
      fn func(entity ent1, content c, int y) {

        // def idx: 7
        var ent = ent1;

        // task_content_component property access idx: 8
        // content property access idx: 9
        // content def idx: 12
        ent.task_content_component.content = c;

        // content propertyaccess idx: 14
        // cont1 def idx: 16
        var cont1 = ent.task_content_component.content;

        print(cont1);
      }
    """;

    var ast = Helpers.getASTFromString(program);
    var result = Helpers.getSymtableForAST(ast);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, true);
    write(finalizedGroumStr, "final_groum.dot");

    // tests
    var paramCDef = findNodeByProcessIdx(finalizedGroum, 3);
    var entDef = findNodeByProcessIdx(finalizedGroum, 7);
    var taskContentComponentPropAccess = findNodeByProcessIdx(finalizedGroum, 8);
    var firstContentPropAccess = findNodeByProcessIdx(finalizedGroum, 9);
    var contentDef = findNodeByProcessIdx(finalizedGroum, 12);
    var secondContentPropAccess = findNodeByProcessIdx(finalizedGroum, 14);
    var cont1Def = findNodeByProcessIdx(finalizedGroum, 16);

    // first content property access should reference ent and previous task_content_component access
    var firstContentPropAccessReads =
        firstContentPropAccess.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(2, firstContentPropAccessReads.size());
    Assert.assertTrue(firstContentPropAccessReads.contains(entDef));
    Assert.assertTrue(firstContentPropAccessReads.contains(taskContentComponentPropAccess));

    // content prop access should be redefined by content def
    var firstContentPropRedefs =
        firstContentPropAccess.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertTrue(firstContentPropRedefs.contains(contentDef));

    // content def should reference param c
    var readsForContentDef = contentDef.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(3, readsForContentDef.size());
    Assert.assertTrue(readsForContentDef.contains(paramCDef));

    // content def should be read twice (by second content property access and cont1 def)
    var readsOfContentDef = contentDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(3, readsOfContentDef.size());
    Assert.assertTrue(readsOfContentDef.contains(secondContentPropAccess));
    Assert.assertTrue(readsOfContentDef.contains(cont1Def));
  }

  @Test
  public void propertyAccessWriteInvalidation() {
    String program =
        """
      fn func(entity ent1, content c, int y, task_content_component tcc) {
        var ent = ent1;

        // creates a definition for content-property of the ent.task_content_component.instance
        // def idx: 13
        ent.task_content_component.content = c;
        // count def idx: 18
        ent.inventory_component.count = 4;

        // this definition invalidates the definition of the content component, because the parent
        // property of content is changed
        // def idx: 22
        ent.task_content_component = tcc;

        // task content component access idx: 23
        // content access idx: 24
        // cont1 def idx: 26
        var cont1 = ent.task_content_component.content;

        // inventory_count should still reference the definition from above (idx 17)
        // def idx: 30
        // ic ref idx: 28
        var inventory_count = ent.inventory_component.count;

        print(cont1);
      }
    """;

    var ast = Helpers.getASTFromString(program);
    var result = Helpers.getSymtableForAST(ast);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, true);
    write(finalizedGroumStr, "final_groum.dot");

    // tests
    var contentDef = findNodeByProcessIdx(finalizedGroum, 13);
    var countDef = findNodeByProcessIdx(finalizedGroum, 18);
    var tccRedef = findNodeByProcessIdx(finalizedGroum, 22);
    var taskContentCompRef = findNodeByProcessIdx(finalizedGroum, 23);
    var contentRef = findNodeByProcessIdx(finalizedGroum, 24);
    var cont1Def = findNodeByProcessIdx(finalizedGroum, 26);
    var icRef = findNodeByProcessIdx(finalizedGroum, 28);
    var inventoryCountDef = findNodeByProcessIdx(finalizedGroum, 30);

    // contentDef should have no reads
    var contentDefReads = contentDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(0, contentDefReads.size());

    // contentDef should be redefined by tccRedef
    var contentRedefs = contentDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertEquals(1, contentRedefs.size());
    Assert.assertTrue(contentRedefs.contains(tccRedef));

    // tccRedef should have three reads, first the access on rhs of cont1 def, the content property
    // access and the cont1 def itself
    var tccRedefReads = tccRedef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(4, tccRedefReads.size());
    Assert.assertTrue(tccRedefReads.contains(taskContentCompRef));
    Assert.assertTrue(tccRedefReads.contains(contentRef));
    Assert.assertTrue(tccRedefReads.contains(cont1Def));

    // the icRef and inventoryCountDef should still reference the countDefinition from above (idx
    // 17)
    var countDefReads = countDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(3, countDefReads.size());
    Assert.assertTrue(countDefReads.contains(icRef));
    Assert.assertTrue(countDefReads.contains(inventoryCountDef));
  }

  @Test
  // TODO: redefinition broken!!
  public void propertyAccessWriteConditional() {
    String program =
        """
      fn func(entity ent1, entity ent2, content c, int y) {
        var ent = ent1;

        // this creates a definition for the content property of the ent.task_content_component instance
        // def idx: 13
        ent.task_content_component.content = c;
        // content ref idx: 15
        // cont1 def idx: 17
        var cont1 = ent.task_content_component.content;

        // setting the variable itself invalidates all definitions of the
        // child properties in the current scope
        if y {
          // def idx: 24
          ent = ent2;
        } else {
          // def idx: 29
          ent = ent1;
        }

        // content ref idx: 31
        // cont2 def idx: 33
        var cont2 = ent.task_content_component.content;
      }
    """;

    var ast = Helpers.getASTFromString(program);
    var result = Helpers.getSymtableForAST(ast);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, true);
    write(finalizedGroumStr, "final_groum.dot");

    // tests
    var contentDef = findNodeByProcessIdx(finalizedGroum, 13);
    var contentRef = findNodeByProcessIdx(finalizedGroum, 15);
    var cont1Def = findNodeByProcessIdx(finalizedGroum, 17);
    var ifDef = findNodeByProcessIdx(finalizedGroum, 24);
    var elseDef = findNodeByProcessIdx(finalizedGroum, 29);
    var finalContentRef = findNodeByProcessIdx(finalizedGroum, 31);
    var cont2Def = findNodeByProcessIdx(finalizedGroum, 33);

    // contentDef should be read by contentRef and cont1Def
    var contentDefReads = contentDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(3, contentDefReads.size());
    Assert.assertTrue(contentDefReads.contains(contentRef));
    Assert.assertTrue(contentDefReads.contains(cont1Def));

    // ifDef and elseDef should redefine contentDef
    var contentDefRedefs = contentDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertEquals(2, contentDefRedefs.size());
    Assert.assertTrue(contentDefRedefs.contains(ifDef));
    Assert.assertTrue(contentDefRedefs.contains(elseDef));

    // final content ref should not reference contentDef
    var finalContentRefs =
        finalContentRef.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(3, finalContentRefs.size());
    Assert.assertFalse(finalContentRefs.contains(contentDef));
    Assert.assertTrue(finalContentRefs.contains(ifDef));
    Assert.assertTrue(finalContentRefs.contains(elseDef));

    // cont2 def should reference if def and else def
    var cont2Refs = cont2Def.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertFalse(cont2Refs.contains(contentDef));
    Assert.assertTrue(cont2Refs.contains(ifDef));
    Assert.assertTrue(cont2Refs.contains(elseDef));
  }

  @Test
  public void multiTerm() {
    String program =
        """
      fn func(int x, int y, int z) {
        var term = x + y * z;
      }
    """;

    var ast = Helpers.getASTFromString(program);
    var result = Helpers.getSymtableForAST(ast);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, true);
    write(finalizedGroumStr, "final_groum.dot");
  }

  @Test
  public void methodAccessSimple() {
    String program =
        """
      // ic def idx: 3
      // idx def idx: 4
      fn func(entity ent, inventory_component ic, int idx) {
        // method access idx: 8
        // i1 def idx: 10
        // inventory_component redef idx: 6
        var i1 = ic.get_item(idx);
      }
    """;

    var ast = Helpers.getASTFromString(program);
    var result = Helpers.getSymtableForAST(ast);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, true);
    write(finalizedGroumStr, "final_groum.dot");

    // test
    var icParamDef = findNodeByProcessIdx(finalizedGroum, 3);
    var idxParamDef = findNodeByProcessIdx(finalizedGroum, 4);
    var icRedef = findNodeByProcessIdx(finalizedGroum, 6);
    var methodAccess = findNodeByProcessIdx(finalizedGroum, 8);
    var i1Def = findNodeByProcessIdx(finalizedGroum, 10);

    // test param ic redef
    var icRedefs = icRedef.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertTrue(icRedefs.contains(icParamDef));
    Assert.assertEquals(1, icRedefs.size());

    // test method access reads
    var methodAccessReads =
        methodAccess.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(2, methodAccessReads.size());
    Assert.assertTrue(methodAccessReads.contains(icRedef));
    Assert.assertTrue(methodAccessReads.contains(idxParamDef));

    // test i1 reads
    var i1defReads = i1Def.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(3, i1defReads.size());
    Assert.assertTrue(i1defReads.contains(icRedef));
    Assert.assertTrue(i1defReads.contains(methodAccess));
    Assert.assertTrue(i1defReads.contains(idxParamDef));
  }

  @Test
  public void methodAccessInvalidation() {
    String program =
        """
      // ent def idx: 2
      // ic def idx: 3
      // idx def idx: 4
      // name def idx: 5
      fn func(entity ent, inventory_component ic, int idx, string name) {
        // def idx: 9
        ent.inventory_component = ic;
        // this redefines the ent definition and invalidates the inventory_component
        // ent redef idx: 11
        ent.set_name(name);

        // this defines the component
        // redef idx: 17
        ent.inventory_component = ic;

        // this redefines the inventory_component and invalidates the definition from above
        // ic redef idx: 21
        // i2 def idx: 24
        var i2 = ent.inventory_component.get_item(idx);
        // def idx: 27
        var my_other_ic = ent.inventory_component;
      }
    """;

    var ast = Helpers.getASTFromString(program);
    var result = Helpers.getSymtableForAST(ast);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, true);
    write(finalizedGroumStr, "final_groum.dot");

    // test
    var entParamDef = findNodeByProcessIdx(finalizedGroum, 2);
    Assert.assertTrue(entParamDef instanceof ParameterInstantiationAction);
    var firstEntRedef = findNodeByProcessIdx(finalizedGroum, 9);
    Assert.assertTrue(firstEntRedef instanceof DefinitionAction);
    var setNameRedef = findNodeByProcessIdx(finalizedGroum, 11);
    Assert.assertTrue(setNameRedef instanceof DefinitionAction);
    var icRedef = findNodeByProcessIdx(finalizedGroum, 17);
    Assert.assertTrue(icRedef instanceof DefinitionAction);
    var getItemIcRedef = findNodeByProcessIdx(finalizedGroum, 21);
    Assert.assertTrue(getItemIcRedef instanceof DefinitionAction);

    var i2Def = findNodeByProcessIdx(finalizedGroum, 24);
    Assert.assertTrue(i2Def instanceof DefinitionAction);
    var myOtherIcDef = findNodeByProcessIdx(finalizedGroum, 27);
    Assert.assertTrue(myOtherIcDef instanceof DefinitionAction);

    // the setName call should invalidate the firstEntRedef and ent param def
    var setNameRedefs = setNameRedef.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertTrue(setNameRedefs.contains(firstEntRedef));
    Assert.assertTrue(setNameRedefs.contains(entParamDef));
    Assert.assertEquals(2, setNameRedefs.size());

    // firstEntRedef should not be read
    var firstEntRedefReads =
        firstEntRedef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(0, firstEntRedefReads.size());

    // icRedef should reference setNameRedef
    var icRedefRead = icRedef.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertTrue(icRedefRead.contains(setNameRedef));

    // getItemIcRedef should redefine icRedef
    var getItemIcRedefRedefs =
        getItemIcRedef.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertTrue(getItemIcRedefRedefs.contains(icRedef));

    // i2Def should read from getItemIcRedef
    var i2DefReads = i2Def.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertTrue(i2DefReads.contains(getItemIcRedef));

    // myOtherIcDef should read from getItemIcRedef
    var myOtherIcDefReads =
        myOtherIcDef.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertTrue(myOtherIcDefReads.contains(getItemIcRedef));
  }

  @Test
  public void mixedMemberAccess() {
    String program =
        """
      // ent def idx: 2
      fn func(entity ent) {
        // inventory_component access idx: 2
        // 0 const def idx: 4
        // get item access idx: 6
        // inventory_component redefinition idx: 7
        // task_content_component access idx: 8
        // content access idx: 9
        // c def idx: 11
        var c = ent.inventory_component.get_item(0).task_content_component.content;
      }
    """;

    var ast = Helpers.getASTFromString(program);
    var result = Helpers.getSymtableForAST(ast);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, true);
    write(finalizedGroumStr, "final_groum.dot");

    // test
    var entDef = findNodeByProcessIdx(finalizedGroum, 2);
    var inventoryCompAccess = findNodeByProcessIdx(finalizedGroum, 3);
    var constDef = findNodeByProcessIdx(finalizedGroum, 4);
    var getItemAccess = findNodeByProcessIdx(finalizedGroum, 6);
    var tccAccess = findNodeByProcessIdx(finalizedGroum, 8);
    var contentAccess = findNodeByProcessIdx(finalizedGroum, 9);
    var icRedef = findNodeByProcessIdx(finalizedGroum, 7);
    var cDef = findNodeByProcessIdx(finalizedGroum, 11);

    var inventoryCompAccessReads =
        inventoryCompAccess.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(1, inventoryCompAccessReads.size());
    Assert.assertTrue(inventoryCompAccessReads.contains(entDef));

    var itemAccessReads = getItemAccess.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(2, itemAccessReads.size());
    Assert.assertTrue(itemAccessReads.contains(entDef));
    Assert.assertTrue(itemAccessReads.contains(inventoryCompAccess));

    var icRedefs = icRedef.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertTrue(icRedefs.contains(inventoryCompAccess));

    var tccAccessReads = tccAccess.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(4, tccAccessReads.size());
    Assert.assertTrue(tccAccessReads.contains(icRedef));
    Assert.assertTrue(tccAccessReads.contains(constDef));
    Assert.assertTrue(tccAccessReads.contains(entDef));
    Assert.assertTrue(tccAccessReads.contains(getItemAccess));

    var contentAccessReads =
        contentAccess.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(5, contentAccessReads.size());
    Assert.assertTrue(contentAccessReads.contains(entDef));
    Assert.assertTrue(contentAccessReads.contains(constDef));
    Assert.assertTrue(contentAccessReads.contains(icRedef));
    Assert.assertTrue(contentAccessReads.contains(getItemAccess));
    Assert.assertTrue(contentAccessReads.contains(tccAccess));

    var cDefReads = cDef.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(6, cDefReads.size());
    Assert.assertTrue(cDefReads.contains(entDef));
    Assert.assertTrue(cDefReads.contains(icRedef));
    Assert.assertTrue(cDefReads.contains(constDef));
    Assert.assertTrue(cDefReads.contains(getItemAccess));
    Assert.assertTrue(cDefReads.contains(tccAccess));
    Assert.assertTrue(cDefReads.contains(contentAccess));
  }

  @Test
  public void mixedMemberAccessChainedMethods() {
    String program =
        """
      // ent def idx: 2
      // other_end def idx: 2
      // idx def idx: 4
      fn func(entity ent, entity other_ent, int idx) {
        // inventory_component access idx: 5
        // get item access idx: 10
        // inventory_component redef idx: 12
        // use method access idx: 11
        // quest_item redef: 13
        // c def idx: 15
        var c = ent.inventory_component.get_item(idx).use(other_ent);
      }
    """;

    var ast = Helpers.getASTFromString(program);
    var result = Helpers.getSymtableForAST(ast);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, true);
    write(finalizedGroumStr, "final_groum.dot");

    // test
    var entParamDef = findNodeByProcessIdx(finalizedGroum, 2);
    var otherEntParamDef = findNodeByProcessIdx(finalizedGroum, 3);
    var idxParamDef = findNodeByProcessIdx(finalizedGroum, 4);
    var inventoryComponentAccess = findNodeByProcessIdx(finalizedGroum, 5);
    var getItemAccess = findNodeByProcessIdx(finalizedGroum, 10);
    var inventoryComponentRedef = findNodeByProcessIdx(finalizedGroum, 12);
    var useMethodAccess = findNodeByProcessIdx(finalizedGroum, 11);
    var questItemRedef = findNodeByProcessIdx(finalizedGroum, 13);
    var cDef = findNodeByProcessIdx(finalizedGroum, 15);

    // get item access should read ent, idx, and inventory_component
    var getItemAccessReads =
        getItemAccess.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(3, getItemAccessReads.size());
    Assert.assertTrue(getItemAccessReads.contains(entParamDef));
    Assert.assertTrue(getItemAccessReads.contains(inventoryComponentAccess));
    Assert.assertTrue(getItemAccessReads.contains(idxParamDef));

    // inventory component redef should redefine inventoryComponentAccess
    var inventoryComponentRedefs =
        inventoryComponentRedef.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertTrue(inventoryComponentRedefs.contains(inventoryComponentAccess));

    // use method access should read ent, inventoryComponentRedef, idx, other_ent, getItemAccess
    var useMethodReads =
        useMethodAccess.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(5, useMethodReads.size());
    Assert.assertTrue(useMethodReads.contains(entParamDef));
    Assert.assertTrue(useMethodReads.contains(otherEntParamDef));
    Assert.assertTrue(useMethodReads.contains(inventoryComponentRedef));
    Assert.assertTrue(useMethodReads.contains(idxParamDef));
    Assert.assertTrue(useMethodReads.contains(getItemAccess));

    // quest item redefs should not actually redefine anything, because it is chained after a
    // method..
    var questItemRedefs =
        questItemRedef.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
    Assert.assertEquals(0, questItemRedefs.size());

    // cdef should reference ent, use method access, getItemAccess, questItemRedef,
    // inventoryComponentRedef, idx, other_ent
    var cDefReads = cDef.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertEquals(7, cDefReads.size());
    Assert.assertTrue(cDefReads.contains(entParamDef));
    Assert.assertTrue(cDefReads.contains(useMethodAccess));
    Assert.assertTrue(cDefReads.contains(getItemAccess));
    Assert.assertTrue(cDefReads.contains(questItemRedef));
    Assert.assertTrue(cDefReads.contains(inventoryComponentRedef));
    Assert.assertTrue(cDefReads.contains(idxParamDef));
    Assert.assertTrue(cDefReads.contains(otherEntParamDef));
    Assert.assertFalse(cDefReads.contains(inventoryComponentAccess));
  }

  @Test
  public void propertyAccess() {
    String program =
        """
        fn func(entity ent, inventory_component ic) {
          //var my_ic = ent.inventory_component;

          ent.inventory_component.count = 12;

          //ent.inventory_component.count = ent.inventory_component.count = 42;
        }
      """;

    var ast = Helpers.getASTFromString(program);
    var result = Helpers.getSymtableForAST(ast);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, true);
    write(finalizedGroumStr, "final_groum.dot");
  }

  @Test
  // TODO: in isolation, this test case works, it is just an issue with the processed idxs
  //  beeing different
  public void interObjectDependencies() {
    String program =
        """
    // my_point def idx: 49
    point my_point {
      x: 1.0,
      y: 11.0
    }

    entity_type monster_type {
      health_component {
          max_health: 10,
          start_health: 10,
          on_death: drop_items // drop items ref idx: 4
      },
      position_component {
        position: my_point // point ref idx: 5
      },
      draw_component {
          path: "character/monster/chort"
      },
      velocity_component {
          x_velocity: 4.0,
          y_velocity: 4.0
      }
    }

    // t1 def idx: 28
    single_choice_task t1 {
      description: "t1",
      answers: [ "test", "other test"],
      correct_answer_index: 0
    }

    // t2 def idx: 44
    single_choice_task t2 {
      description: "t2",
      answers: [ "test", "other test"],
      correct_answer_index: 0
    }

    // drop items def idx: 36
    fn drop_items(entity me) {
        me.inventory_component.drop_items();
    }

    graph g {
      // t1 ref idx: 29
      // t2 ref idx: 30
      t1 -> t2 [type=seq];
    }
    """;

    var gameEnv = new GameEnvironment();
    var ast = Helpers.getASTFromString(program, gameEnv);

    var result = Helpers.getSymtableForAST(ast, gameEnv);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, true);
    write(finalizedGroumStr, "final_groum_test_fail.dot");

    // tests
    var pointDef = findNodeByProcessIdx(finalizedGroum, 49);
    var pointRef = findNodeByProcessIdx(finalizedGroum, 5);
    System.out.println(pointRef);
    Assert.assertTrue(pointRef instanceof VariableReferenceAction);
    var entityTypeDef = findNodeByProcessIdx(finalizedGroum, 20);
    var graphDef = findNodeByProcessIdx(finalizedGroum, 31);
    var dropItemsDef = findNodeByProcessIdx(finalizedGroum, 36);
    var dropItemsRef = findNodeByProcessIdx(finalizedGroum, 4);
    var t1Def = findNodeByProcessIdx(finalizedGroum, 28);
    var t2Def = findNodeByProcessIdx(finalizedGroum, 44);
    var t1Ref = findNodeByProcessIdx(finalizedGroum, 29);
    var t2Ref = findNodeByProcessIdx(finalizedGroum, 30);

    var pointRefReads = pointRef.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertTrue(pointRefReads.contains(pointDef));

    var dropItemsRefReads =
        dropItemsRef.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertTrue(dropItemsRefReads.contains(dropItemsDef));

    var t1RefReads = t1Ref.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertTrue(t1RefReads.contains(t1Def));

    var t2RefReads = t2Ref.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertTrue(t2RefReads.contains(t2Def));

    var graphDefReads = graphDef.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertTrue(graphDefReads.contains(t1Def));
    Assert.assertTrue(graphDefReads.contains(t2Def));

    var entityTypeDefReads =
        entityTypeDef.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertTrue(entityTypeDefReads.contains(dropItemsDef));
    Assert.assertTrue(entityTypeDefReads.contains(pointDef));
  }

  @Test
  public void cyclicalDefinition() {
    String program =
        """
    // t1 def idx: 5
    entity_type type1 {
      position_component {
        // position_component access idx: 1
        // position access idx: 2
        // position def idx: 3
        position: type2.position_component.position
      }
    }

    // t2 def idx: 10
    entity_type type2 {
      position_component {
        // position_component access idx: 6
        // position access idx: 7
        // position def idx: 8
        position: type1.position_component.position
      }
    }
    """;

    var gameEnv = new GameEnvironment();
    var ast = Helpers.getASTFromString(program, gameEnv);

    var result = Helpers.getSymtableForAST(ast, gameEnv);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, true);
    write(finalizedGroumStr, "final_groum.dot");

    // test
    var t1PosCompAccess = findNodeByProcessIdx(finalizedGroum, 1);
    var t1PosAccess = findNodeByProcessIdx(finalizedGroum, 2);
    var t1PosDef = findNodeByProcessIdx(finalizedGroum, 3);
    var t2PosCompAccess = findNodeByProcessIdx(finalizedGroum, 6);
    var t2PosAccess = findNodeByProcessIdx(finalizedGroum, 7);
    var t2PosDef = findNodeByProcessIdx(finalizedGroum, 8);
    var t1Def = findNodeByProcessIdx(finalizedGroum, 5);
    var t2Def = findNodeByProcessIdx(finalizedGroum, 10);

    var t1PosCompAccessReads =
        t1PosCompAccess.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertTrue(t1PosCompAccessReads.contains(t2Def));
    var t1PosAccessReads = t1PosAccess.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertTrue(t1PosAccessReads.contains(t2Def));
    var t1PosDefReads = t1PosDef.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertTrue(t1PosDefReads.contains(t2Def));

    var t2PosCompAccessReads =
        t2PosCompAccess.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertTrue(t2PosCompAccessReads.contains(t1Def));
    var t2PosAccessReads = t2PosAccess.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertTrue(t2PosAccessReads.contains(t1Def));
    var t2PosDefReads = t2PosDef.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertTrue(t2PosDefReads.contains(t1Def));

    var t1DefReads = t1Def.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertTrue(t1DefReads.contains(t2Def));

    var t2DefReads = t2Def.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    Assert.assertTrue(t2DefReads.contains(t1Def));
  }

  @Test
  public void condWrite() {
    String program =
      """
        fn func(bool param) {
            var my_var = 42;
            if param {
                my_var = 123;
            }
            print(my_var);
            my_var = 1;
            print(my_var);
        }
        """;

    var gameEnv = new GameEnvironment();
    var ast = Helpers.getASTFromString(program, gameEnv);

    var result = Helpers.getSymtableForAST(ast, gameEnv);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, false);
    write(finalizedGroumStr, "final_groum.dot");
  }

  @Test
  public void condPrint() {
    String program =
      """
        fn print_conditional(int number) {
            if number > 42 {
              print("Greater than 42");
            } else {
              print("Equal or less than 42");
            }
        }
        """;

    var gameEnv = new GameEnvironment();
    var ast = Helpers.getASTFromString(program, gameEnv);

    var result = Helpers.getSymtableForAST(ast, gameEnv);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, true);
    write(finalizedGroumStr, "final_groum.dot");
  }

  @Test
  public void differentGroumStructures() {
    String program =
      """
      fn func() {
          var x : int;
          var y : int;
      }

      point p {
        x: 42.0,
        y: 123.0
      }
      """;

    var gameEnv = new GameEnvironment();
    var ast = Helpers.getASTFromString(program, gameEnv);

    var result = Helpers.getSymtableForAST(ast, gameEnv);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, true);
    write(finalizedGroumStr, "final_groum.dot");
  }

  @Test
  public void ref() {
    String program =
        """
        point p {
          x: 42.0,
          y: 123.0
        }

        entity_type type1 {
          position_component {
            position: point {
              x: p.x,
              y: p.y
            }
          }
        }
        """;

    var gameEnv = new GameEnvironment();
    var ast = Helpers.getASTFromString(program, gameEnv);

    var result = Helpers.getSymtableForAST(ast, gameEnv);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, true);
    write(finalizedGroumStr, "final_groum.dot");
  }

  @Test
  public void cyclicalDefinitionCursed() {
    String program =
        """
        // t1 def idx: 5
        entity_type type1 {
          position_component {
            position: point {
              x: type2.position_component.position.x,
              y: type2.position_component.position.y
            }
          }
        }

        // t2 def idx: 10
        entity_type type2 {
          position_component {
            // position_component access idx: 6
            // position access idx: 7
            // position def idx: 8
            position: type1.position_component.position
          }
        }
        """;

    var gameEnv = new GameEnvironment();
    var ast = Helpers.getASTFromString(program, gameEnv);

    var result = Helpers.getSymtableForAST(ast, gameEnv);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, true);
    write(finalizedGroumStr, "final_groum.dot");
  }

  @Test
  public void complexFunction() {
    String program =
        """
    fn ask_task_finished(entity knight, entity who) {
        var my_task : task;
        my_task =  knight.task_component.task;
        if my_task.is_active() {
            ask_task_yes_no(my_task);
        } else {
            show_info("Du hast die Aufgabe schon bearbeitet.");
        }
    }

    fn open_container(entity chest, entity who) {
        chest.inventory_component.open(who);
    }

    fn drop_items(entity me) {
        me.inventory_component.drop_items();
    }

    item_type scroll_type {
        display_name: "Eine Schriftrolle",
        description: "Lies mich",
        texture_path: "items/book/wisdom_scroll.png"
    }

    entity_type knight_type {
        draw_component {
            path: "character/blue_knight"
        },
        hitbox_component {},
        position_component{},
        interaction_component{
            radius: 1.5
        },
        task_component{}
    }

    entity_type chest_type {
        inventory_component {},
        draw_component {
            path: "objects/treasurechest"
        },
        hitbox_component {},
        position_component{},
        interaction_component{
            radius: 1.5,
            on_interaction: open_container
        },
        task_content_component{}
    }

    entity_type monster_type {
        inventory_component {},
        health_component {
            max_health: 10,
            start_health: 10,
            on_death: drop_items
        },
        position_component {},
        draw_component {
            path: "character/monster/chort"
        },
        velocity_component {
            x_velocity: 4.0,
            y_velocity: 4.0
        },
        hitbox_component {},
        ai_component{}
    }

    fn build_task_single_chest_with_monster(single_choice_task t) -> entity<><> {
        var return_set : entity<><>;
        var room_set : entity<>;

        for task_content content in t.get_content() {
            var item : quest_item;
            item = build_quest_item(scroll_type, content);

            var monster: entity;
            monster = instantiate(monster_type);
            monster.inventory_component.add_item(item);
            room_set.add(monster);
        }

        var chest : entity;
        chest = instantiate(chest_type);
        chest.mark_as_task_container(t, "Quest-Truhe");

        room_set.add(chest);
        t.set_scenario_text("Hilfe! Monster haben die Schriftrollen geklaut! Platziere die richtige Schriftrolle in der Quest-Truhe!");
        t.set_answer_picker_function(answer_picker_single_chest);

        // quest giver knight
        var knight : entity;
        knight = instantiate_named(knight_type, "Questgeber");
        knight.task_component.task = t;
        knight.interaction_component.on_interaction = ask_task_finished;
        room_set.add(knight);

        var random_entity : entity;
        random_entity = get_random_content();
        room_set.add(random_entity);
        return_set.add(room_set);
        return return_set;
    }
    """;

    var gameEnv = new GameEnvironment();
    var ast = Helpers.getASTFromString(program, gameEnv);

    var result = Helpers.getSymtableForAST(ast, gameEnv);
    var symbolTable = result.symbolTable;
    var env = result.environment;
    var fs = env.getFileScope(null);

    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    GroumPrinter p1 = new GroumPrinter();
    String temporalGroumStr = p1.print(temporalGroum);
    write(temporalGroumStr, "temp_groum.dot");

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    var finalizedGroum = finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p2 = new GroumPrinter();
    String finalizedGroumStr = p2.print(finalizedGroum, true);
    write(finalizedGroumStr, "final_groum.dot");
  }

  public static void write(String content, String fileName) {
    try {
      Files.createDirectories(Paths.get(tempImgDirectory));
      Path path = Path.of(tempImgDirectory, fileName);
      FileWriter writer = new FileWriter(path.toString());
      writer.append(content);
      writer.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static GroumNode findNodeByProcessIdx(Groum groumToSearch, long idx) {
    return groumToSearch.nodes().stream()
        .filter(n -> n.processedCounter() == idx)
        .findFirst()
        .orElse(GroumNode.NONE);
  }
}
