package dsl.semanticanalysis;

import dsl.helpers.Helpers;
import dsl.parser.ast.FuncDefNode;
import dsl.parser.ast.TermNode;
import dsl.parser.ast.VarDeclNode;
import dsl.semanticanalysis.environment.GameEnvironment;
import dsl.semanticanalysis.groum.*;
import dsl.semanticanalysis.symbol.FunctionSymbol;
import dsl.semanticanalysis.symbol.Symbol;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import org.junit.Assert;
import org.junit.Test;

public class TestGroum {
  private static final Path testLibPath = Path.of("test_resources/testlib");

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
    Assert.assertEquals(2, sourceNodes.size());
    var firstParam = sourceNodes.get(0);
    Assert.assertEquals(
        ActionNode.ActionType.parameterInstantiation, ((ActionNode) firstParam).actionType());
    Assert.assertEquals(1, firstParam.outgoing().size());
    var secondParam = sourceNodes.get(1);
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
    Assert.assertEquals(2, sourceNodes.size());

    var xParam = sourceNodes.get(0);
    Assert.assertEquals(3, xParam.outgoing().size());

    var yParam = sourceNodes.get(1);

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
    Assert.assertEquals(2, sourceNodes.size());

    var xParam = sourceNodes.get(0);
    Assert.assertEquals(3, xParam.outgoing().size());

    var yParam = sourceNodes.get(1);

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
  public void graphDataDependency() {

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
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);

    FinalGroumBuilder finalGroumBuilder = new FinalGroumBuilder();
    finalGroumBuilder.finalize(temporalGroum, instanceMap);

    GroumPrinter p = new GroumPrinter();
    String str = p.print(temporalGroum);
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
        // y def idx: 23
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
  public void dataDependencySequentialConditional() {
    String program =
        """
  // y param idx: 2
  fn add(int x, int y, int z) -> int {
    if x {
      // idx: 10
      y = 42;
    } else {
      // idx: 15
      y = 321;
    }

    if x {
      // idx: 21
      y = 1;
    }

    // param ref idx: 22
    print(y);

    if x {
      // idx: 31
      y = 2;
    } else if z {
      // idx: 39
      y = 3;
    } else {
      // idx: 44
      y = 4;
    }

    // ref idx: 45
    print(y);

    // idx: 50
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
    var paramDef = findNodeByProcessIdx(finalizedGroum, 2);
    var firstIfDef = findNodeByProcessIdx(finalizedGroum, 10);
    var firstElseDef = findNodeByProcessIdx(finalizedGroum, 15);
    var secondIfDef = findNodeByProcessIdx(finalizedGroum, 21);
    var firstPrintParamRef = findNodeByProcessIdx(finalizedGroum, 22);
    var thirdIfDef = findNodeByProcessIdx(finalizedGroum, 31);
    var elseIfDef = findNodeByProcessIdx(finalizedGroum, 39);
    var secondElseDef = findNodeByProcessIdx(finalizedGroum, 44);
    var secondPrintParamRef = findNodeByProcessIdx(finalizedGroum, 45);
    var finalDef = findNodeByProcessIdx(finalizedGroum, 50);

    // check param redefs
    var paramRedefs =
        paramDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
    Assert.assertEquals(2, paramRedefs.size());
    Assert.assertTrue(paramRedefs.contains(firstIfDef));
    Assert.assertTrue(paramRedefs.contains(firstElseDef));

    // check first if-else redefs
    var firstIfDefRedefs =
        firstIfDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
    Assert.assertEquals(4, firstIfDefRedefs.size());
    Assert.assertTrue(firstIfDefRedefs.contains(secondIfDef));
    Assert.assertTrue(firstIfDefRedefs.contains(thirdIfDef));
    Assert.assertTrue(firstIfDefRedefs.contains(elseIfDef));
    Assert.assertTrue(firstIfDefRedefs.contains(secondElseDef));

    var firstElseDefRedefs =
        firstElseDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
    Assert.assertEquals(4, firstIfDefRedefs.size());
    Assert.assertTrue(firstElseDefRedefs.contains(secondIfDef));
    Assert.assertTrue(firstElseDefRedefs.contains(thirdIfDef));
    Assert.assertTrue(firstElseDefRedefs.contains(elseIfDef));
    Assert.assertTrue(firstElseDefRedefs.contains(secondElseDef));

    // check first print param refs
    var firstPrintParamRefs =
        firstPrintParamRef.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(3, firstPrintParamRefs.size());
    Assert.assertTrue(firstPrintParamRefs.contains(firstIfDef));
    Assert.assertTrue(firstPrintParamRefs.contains(firstElseDef));
    Assert.assertTrue(firstPrintParamRefs.contains(secondIfDef));

    // check only one read on first defs
    var firstIfDefReads = firstIfDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(1, firstIfDefReads.size());

    var firstElseDefReads =
        firstElseDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(1, firstElseDefReads.size());

    var secondIfDefReads =
        secondIfDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(1, secondIfDefReads.size());

    // check second print param refs
    var secondPrintParamRefs =
        secondPrintParamRef.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(3, secondPrintParamRefs.size());
    Assert.assertTrue(secondPrintParamRefs.contains(thirdIfDef));
    Assert.assertTrue(secondPrintParamRefs.contains(elseIfDef));
    Assert.assertTrue(secondPrintParamRefs.contains(secondElseDef));

    // check final redefs
    var finalRedefs =
        finalDef.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
    Assert.assertEquals(3, finalRedefs.size());
    Assert.assertTrue(finalRedefs.contains(thirdIfDef));
    Assert.assertTrue(finalRedefs.contains(elseIfDef));
    Assert.assertTrue(finalRedefs.contains(secondElseDef));
  }

  @Test
  public void dataDependencyConditionalShadowing() {
    String program =
        """
      // param idx: 2
      fn add(int x, int y, int z) -> int {
        // redef idx: 6
        y = 1;
        if x {
          // idx: 13
          y = 2;
        } else if z {
          // idx: 21
          y = 3;
        } else {
          // idx: 26
          y = 4;
        }

        // param ref idx: 27
        print(y);

        // redef idx: 32
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
    var firstRedef = findNodeByProcessIdx(finalizedGroum, 6);
    var secondRedef = findNodeByProcessIdx(finalizedGroum, 13);
    var thirdRedef = findNodeByProcessIdx(finalizedGroum, 21);
    var forthRedef = findNodeByProcessIdx(finalizedGroum, 26);
    var paramRef = findNodeByProcessIdx(finalizedGroum, 27);
    var finalRedef = findNodeByProcessIdx(finalizedGroum, 32);

    // shadowing of first redefinition
    var firstRedefShadowing =
        firstRedef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
    Assert.assertEquals(3, firstRedefShadowing.size());
    Assert.assertEquals(secondRedef, firstRedefShadowing.get(0));
    Assert.assertEquals(thirdRedef, firstRedefShadowing.get(1));
    Assert.assertEquals(forthRedef, firstRedefShadowing.get(2));

    var firstRedefReads = firstRedef.getOutgoingOfType(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(0, firstRedefReads.size());

    // param references
    var paramReads = paramRef.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(3, paramReads.size());
    Assert.assertTrue(paramReads.contains(secondRedef));
    Assert.assertTrue(paramReads.contains(thirdRedef));
    Assert.assertTrue(paramReads.contains(forthRedef));

    // final redef
    var finalRedefs =
        finalRedef.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
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
  public void dataDependencyBlock() {
    String program =
        """
  //y idx: 2
  fn add(int x, int y, int z) -> int {
    if x {
      // idx: 10
      y = 42;
      if z {
        // idx: 16
        y = 56;
        {
          // idx: 20
          y = 12;
          // idx: 23
          y = 1;
        }
        {{{
          // idx: 29
          y = 4321;
        }}}
        // ref idx: 31
        print(y);
      }
    } else {
      // idx: 37
      y = 123;
    }

    // y ref idx: 39
    var sum = x + y;
    // idx: 44
    y = 21;
    // ref idx: 45
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
    var paramDef = findNodeByProcessIdx(finalizedGroum, 2);
    var firstIfDef = findNodeByProcessIdx(finalizedGroum, 10);
    var secondIfDef = findNodeByProcessIdx(finalizedGroum, 16);
    var firstBlockDef = findNodeByProcessIdx(finalizedGroum, 20);
    var secondBlockDef = findNodeByProcessIdx(finalizedGroum, 23);
    var thirdBlockDef = findNodeByProcessIdx(finalizedGroum, 29);
    var firstPrintParamRef = findNodeByProcessIdx(finalizedGroum, 31);
    var elseDef = findNodeByProcessIdx(finalizedGroum, 37);
    var termYRef = findNodeByProcessIdx(finalizedGroum, 39);
    var sumDef = findNodeByProcessIdx(finalizedGroum, 41);
    var finalRedef = findNodeByProcessIdx(finalizedGroum, 44);
    var returnRef = findNodeByProcessIdx(finalizedGroum, 45);

    // check param redefs
    var paramRedefs =
        paramDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
    Assert.assertEquals(2, paramRedefs.size());
    Assert.assertTrue(paramRedefs.contains(firstIfDef));
    Assert.assertTrue(paramRedefs.contains(elseDef));

    // check first redef redefs
    var firstIfDefRedefs =
        firstIfDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
    Assert.assertEquals(2, firstIfDefRedefs.size());
    Assert.assertTrue(firstIfDefRedefs.contains(secondIfDef));
    Assert.assertTrue(firstIfDefRedefs.contains(finalRedef));

    // check first def reads
    var firstIfDefReads = firstIfDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(2, firstIfDefReads.size());
    Assert.assertTrue(firstIfDefReads.contains(termYRef));
    Assert.assertTrue(firstIfDefReads.contains(sumDef));

    // check second def redefs
    var secondIfDefRedefs =
        secondIfDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
    Assert.assertEquals(1, secondIfDefRedefs.size());
    Assert.assertTrue(secondIfDefRedefs.contains(firstBlockDef));

    // check no reads on second def
    var secondDefReads = secondIfDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(0, secondDefReads.size());

    // check first block redefs
    var firstBlockRedefs =
        firstBlockDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
    Assert.assertEquals(1, firstBlockRedefs.size());
    Assert.assertTrue(firstBlockRedefs.contains(secondBlockDef));

    // check no reads on first block def
    var firstBlockDefReads =
        firstBlockDef.getOutgoingOfType(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(0, firstBlockDefReads.size());

    // check second block redefs
    var secondBlockDefRedefs =
        secondBlockDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
    Assert.assertEquals(1, secondBlockDefRedefs.size());
    Assert.assertTrue(secondBlockDefRedefs.contains(thirdBlockDef));

    // check reads on third block def
    var thirdBlockDefReads =
        thirdBlockDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(3, thirdBlockDefReads.size());
    Assert.assertTrue(thirdBlockDefReads.contains(termYRef));
    Assert.assertTrue(thirdBlockDefReads.contains(sumDef));
    Assert.assertTrue(thirdBlockDefReads.contains(firstPrintParamRef));

    // check redefs on third block def
    var thirdBlockDefRedefs =
        thirdBlockDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
    Assert.assertEquals(1, thirdBlockDefRedefs.size());
    Assert.assertTrue(thirdBlockDefRedefs.contains(finalRedef));

    // check final redefs
    var finalRedefs =
        finalRedef.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
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
        // x param idx: 1
        fn test(int x, int y, int z) {
        	if x {
        	  // idx: 9
        	  x = 1;
        		if y {
        		  // idx: 16
        			x = 12;
        			if z {
        			  // idx: 22
        				x = 123;
        			}
        			// idx: 25
        			x = 1234;
        			// ref idx: 27
        			print(x);
        		} else {
        		  // idx: 33
        			x = 42;
        		}
        	}
        	// ref idx: 34
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
    var paramDef = findNodeByProcessIdx(finalizedGroum, 1);
    var firstRedef = findNodeByProcessIdx(finalizedGroum, 9);
    var secondRedef = findNodeByProcessIdx(finalizedGroum, 16);
    var thirdRedef = findNodeByProcessIdx(finalizedGroum, 22);
    var forthRedef = findNodeByProcessIdx(finalizedGroum, 25);
    var fifthRedef = findNodeByProcessIdx(finalizedGroum, 33);
    var firstPrintParamRef = findNodeByProcessIdx(finalizedGroum, 27);
    var secondPrintParamRef = findNodeByProcessIdx(finalizedGroum, 34);

    // check param reads
    var paramReads = paramDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(2, paramReads.size());
    Assert.assertTrue(paramReads.contains(secondPrintParamRef));

    // check param redef
    var paramRedef = paramDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
    Assert.assertEquals(1, paramRedef.size());
    Assert.assertTrue(paramRedef.contains(firstRedef));

    // check first redef redefs
    var firstRedefRedefs =
        firstRedef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
    Assert.assertEquals(2, firstRedefRedefs.size());
    Assert.assertTrue(firstRedefRedefs.contains(fifthRedef));
    Assert.assertTrue(firstRedefRedefs.contains(secondRedef));

    // check second redef redefs
    var secondRedefRedefs =
        secondRedef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
    Assert.assertEquals(2, secondRedefRedefs.size());
    Assert.assertTrue(secondRedefRedefs.contains(thirdRedef));
    Assert.assertTrue(secondRedefRedefs.contains(forthRedef));

    var secondRedefsReads =
        secondRedef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(0, secondRedefsReads.size());

    // check third redef redefs
    var thirdRedefRedefs =
        thirdRedef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
    Assert.assertEquals(1, thirdRedefRedefs.size());

    // check forth redefs reads
    var forthRedefsReads = forthRedef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(2, firstRedefRedefs.size());
    Assert.assertTrue(forthRedefsReads.contains(firstPrintParamRef));
    Assert.assertTrue(forthRedefsReads.contains(secondPrintParamRef));

    // check referenced values in final print statement
    var secondPrintsRefs =
        secondPrintParamRef.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(3, secondPrintsRefs.size());
    Assert.assertTrue(secondPrintsRefs.contains(fifthRedef));
    Assert.assertTrue(secondPrintsRefs.contains(forthRedef));
    Assert.assertTrue(secondPrintsRefs.contains(paramDef));
  }

  @Test
  public void dataDependencyConditionalRedef() {
    String program =
        """
      // param y idx: 2
      fn test(int x, int y, int z) {
        if x {
          if z {
            // def action idx: 13
            y = 1;
            // print param ref idx: 15
            print(y);
          }
          // def action idx: 19
          y = 2;
        } else {
          // def action idx: 24
          y = 3;
        }

        // ref in expression idx: 25
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
    var paramYDefNode = findNodeByProcessIdx(finalizedGroum, 2);
    var nestedIfDefNode = findNodeByProcessIdx(finalizedGroum, 13);
    var nestedFuncParamRefNode = findNodeByProcessIdx(finalizedGroum, 15);
    var ifDefNode = findNodeByProcessIdx(finalizedGroum, 19);

    var elseDefNode = findNodeByProcessIdx(finalizedGroum, 24);
    var funcParamRefNode = findNodeByProcessIdx(finalizedGroum, 25);

    var redefsOfParam =
        paramYDefNode.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
    Assert.assertEquals(3, redefsOfParam.size());

    var startsOfRedefNestedIfDefNode =
        nestedIfDefNode.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
    Assert.assertEquals(1, startsOfRedefNestedIfDefNode.size());
    Assert.assertEquals(paramYDefNode, startsOfRedefNestedIfDefNode.get(0));

    // check for print refererence
    var endsOfDataRefsNestedIf =
        nestedIfDefNode.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(1, endsOfDataRefsNestedIf.size());
    Assert.assertEquals(nestedFuncParamRefNode, endsOfDataRefsNestedIf.get(0));

    var startsOfRedefIfDefNode =
        ifDefNode.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
    Assert.assertEquals(2, startsOfRedefIfDefNode.size());
    Assert.assertTrue(startsOfRedefIfDefNode.contains(paramYDefNode));
    Assert.assertTrue(startsOfRedefIfDefNode.contains(nestedIfDefNode));

    // check for final print reference
    var endsOfDataRefIf = ifDefNode.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(1, endsOfDataRefIf.size());
    Assert.assertEquals(funcParamRefNode, endsOfDataRefIf.get(0));

    var startsOfRedefElseNode =
        elseDefNode.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
    Assert.assertEquals(1, startsOfRedefElseNode.size());
    Assert.assertEquals(paramYDefNode, startsOfRedefElseNode.get(0));

    // check for final print reference
    var endsOfDataRefElse =
        elseDefNode.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(1, endsOfDataRefElse.size());
    Assert.assertEquals(funcParamRefNode, endsOfDataRefElse.get(0));
  }

  @Test
  public void functionCall() {
    String program =
        """
        // x idx: 7
        // y idx: 8
        // z idx: 9
        fn add(int x, int y, int z) {
          // redef idx: 17
          // func call idx: 15
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
    var paramXDef = findNodeByProcessIdx(finalizedGroum, 7);
    var paramYDef = findNodeByProcessIdx(finalizedGroum, 8);
    var paramZDef = findNodeByProcessIdx(finalizedGroum, 9);
    var funcCall = findNodeByProcessIdx(finalizedGroum, 15);
    var xRedef = findNodeByProcessIdx(finalizedGroum, 17);

    var funcCallReads = funcCall.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertTrue(funcCallReads.contains(paramXDef));
    Assert.assertTrue(funcCallReads.contains(paramYDef));
    Assert.assertTrue(funcCallReads.contains(paramZDef));

    var xRedefReads = xRedef.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertTrue(xRedefReads.contains(paramXDef));
    Assert.assertTrue(xRedefReads.contains(paramYDef));
    Assert.assertTrue(xRedefReads.contains(paramZDef));
  }

  @Test
  public void propertyAccessDifferentInstanceIds() {
    String program =
        """
        fn func(entity ent1, entity ent2, int x) {
          // count reference idx: 5
          var c1 = ent1.inventory_component.count;
          // count reference idx: 9
          var c2 = ent2.inventory_component.count;

          // count def idx: 16
          ent1.inventory_component.count = x;
          // count def idx: 21
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
    var firstCountAccess = (PropertyAccessAction)findNodeByProcessIdx(finalizedGroum, 5);
    var secondCountAccess = (PropertyAccessAction)findNodeByProcessIdx(finalizedGroum, 9);
    Assert.assertNotEquals(firstCountAccess.propertyInstanceId, secondCountAccess.propertyInstanceId);

    var firstCountWrite = (DefinitionAction)findNodeByProcessIdx(finalizedGroum, 16);
    var secondCountWrite = (DefinitionAction)findNodeByProcessIdx(finalizedGroum, 21);
    Assert.assertEquals(firstCountAccess.propertyInstanceId, firstCountWrite.referencedInstanceId());
    Assert.assertEquals(secondCountAccess.propertyInstanceId, secondCountWrite.referencedInstanceId());
  }

  @Test
  public void propertyAccessChained() {
    String program =
      """
      // param def idx: 1
      fn func(entity ent) {
        // t def idx: 6
        // task_content_component access idx: 2
        // content access idx: 3
        // task access idx: 4
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
    var tDef = (DefinitionAction)findNodeByProcessIdx(finalizedGroum, 6);
    var paramDef = findNodeByProcessIdx(finalizedGroum, 1);
    var taskContentComponentAccess = findNodeByProcessIdx(finalizedGroum, 2);
    var contentAccess = findNodeByProcessIdx(finalizedGroum, 3);
    var taskAccess = findNodeByProcessIdx(finalizedGroum, 4);
    var tDefReads = tDef.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRead);
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
      // param def idx: 1
      fn func(entity ent) {
        // t def idx: 6
        // task_content_component access idx: 2
        // content access idx: 3
        // task access idx: 4
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
    var tDef = (DefinitionAction)findNodeByProcessIdx(finalizedGroum, 6);
    var paramDef = findNodeByProcessIdx(finalizedGroum, 1);
    var taskContentComponentAccess = findNodeByProcessIdx(finalizedGroum, 2);
    var contentAccess = findNodeByProcessIdx(finalizedGroum, 3);
    var taskAccess = findNodeByProcessIdx(finalizedGroum, 4);
    var tDefReads = tDef.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRead);
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
      // param c idx: 2
      fn func(entity ent1, content c, int y) {

        // def idx: 6
        var ent = ent1;

        // task_content_component property access idx: 7
        // content property access idx: 8
        // content def idx: 11
        ent.task_content_component.content = c;

        // content propertyaccess idx: 13
        // cont1 def idx: 15
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
    var paramCDef = findNodeByProcessIdx(finalizedGroum, 2);
    var entDef = findNodeByProcessIdx(finalizedGroum, 6);
    var taskContentComponentPropAccess = findNodeByProcessIdx(finalizedGroum, 7);
    var firstContentPropAccess = findNodeByProcessIdx(finalizedGroum, 8);
    var contentDef = findNodeByProcessIdx(finalizedGroum, 11);
    var secondContentPropAccess = findNodeByProcessIdx(finalizedGroum, 13);
    var cont1Def = findNodeByProcessIdx(finalizedGroum, 15);

    // first content property access should reference ent and previous task_content_component access
    var firstContentPropAccessReads = firstContentPropAccess.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(2, firstContentPropAccessReads.size());
    Assert.assertTrue(firstContentPropAccessReads.contains(entDef));
    Assert.assertTrue(firstContentPropAccessReads.contains(taskContentComponentPropAccess));

    // content prop access should be redefined by content def
    var firstContentPropRedefs = firstContentPropAccess.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
    Assert.assertTrue(firstContentPropRedefs.contains(contentDef));

    // content def should reference param c
    var readsForContentDef = contentDef.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(3, readsForContentDef.size());
    Assert.assertTrue(readsForContentDef.contains(paramCDef));

    // content def should be read twice (by second content property access and cont1 def)
    var readsOfContentDef = contentDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(2, readsOfContentDef.size());
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
        // def idx: 12
        ent.task_content_component.content = c;
        // count def idx: 17
        ent.inventory_component.count = 4;

        // this definition invalidates the definition of the content component, because the parent
        // property of content is changed
        // def idx: 21
        ent.task_content_component = tcc;

        // task content component access idx: 22
        // content access idx: 23
        // cont1 def idx: 25
        var cont1 = ent.task_content_component.content;

        // inventory_count should still reference the definition from above (idx 17)
        // def idx: 29
        // ic ref idx: 27
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
    var contentDef = findNodeByProcessIdx(finalizedGroum, 12);
    var countDef = findNodeByProcessIdx(finalizedGroum, 17);
    var tccRedef = findNodeByProcessIdx(finalizedGroum, 21);
    var taskContentCompRef = findNodeByProcessIdx(finalizedGroum, 22);
    var contentRef = findNodeByProcessIdx(finalizedGroum, 23);
    var cont1Def = findNodeByProcessIdx(finalizedGroum, 25);
    var icRef = findNodeByProcessIdx(finalizedGroum, 27);
    var inventoryCountDef = findNodeByProcessIdx(finalizedGroum, 29);

    // contentDef should have no reads
    var contentDefReads = contentDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(0, contentDefReads.size());

    // contentDef should be redefined by tccRedef
    var contentRedefs = contentDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
    Assert.assertEquals(1, contentRedefs.size());
    Assert.assertTrue(contentRedefs.contains(tccRedef));


    // tccRedef should have three reads, first the access on rhs of cont1 def, the content property
    // access and the cont1 def itself
    var tccRedefReads = tccRedef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(3, tccRedefReads.size());
    Assert.assertTrue(tccRedefReads.contains(taskContentCompRef));
    Assert.assertTrue(tccRedefReads.contains(contentRef));
    Assert.assertTrue(tccRedefReads.contains(cont1Def));

    // the icRef and inventoryCountDef should still reference the countDefinition from above (idx 17)
    var countDefReads = countDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(2, countDefReads.size());
    Assert.assertTrue(countDefReads.contains(icRef));
    Assert.assertTrue(countDefReads.contains(inventoryCountDef));
  }

  @Test
  public void propertyAccessWriteConditional() {
    String program =
      """
      fn func(entity ent1, entity ent2, content c, int y) {
        var ent = ent1;

        // this creates a definition for the content property of the ent.task_content_component instance
        // def idx: 12
        ent.task_content_component.content = c;
        // content ref idx: 14
        // cont1 def idx: 16
        var cont1 = ent.task_content_component.content;

        // setting the variable itself invalidates all definitions of the
        // child properties in the current scope
        if y {
          // def idx: 23
          ent = ent2;
        } else {
          // def idx: 28
          ent = ent1;
        }

        // content ref idx: 30
        // cont2 def idx: 32
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
    var contentDef = findNodeByProcessIdx(finalizedGroum, 12);
    var contentRef = findNodeByProcessIdx(finalizedGroum, 14);
    var cont1Def = findNodeByProcessIdx(finalizedGroum, 16);
    var ifDef = findNodeByProcessIdx(finalizedGroum, 23);
    var elseDef = findNodeByProcessIdx(finalizedGroum, 28);
    var finalContentRef = findNodeByProcessIdx(finalizedGroum, 30);
    var cont2Def = findNodeByProcessIdx(finalizedGroum, 32);

    // contentDef should be read by contentRef and cont1Def
    var contentDefReads =contentDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(2, contentDefReads.size());
    Assert.assertTrue(contentDefReads.contains(contentRef));
    Assert.assertTrue(contentDefReads.contains(cont1Def));

    // ifDef and elseDef should redefine contentDef
    var contentDefRedefs = contentDef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
    Assert.assertEquals(2, contentDefRedefs.size());
    Assert.assertTrue(contentDefRedefs.contains(ifDef));
    Assert.assertTrue(contentDefRedefs.contains(elseDef));

    // final content ref should not reference contentDef
    var finalContentRefs = finalContentRef.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(3, finalContentRefs.size());
    Assert.assertFalse(finalContentRefs.contains(contentDef));
    Assert.assertTrue(finalContentRefs.contains(ifDef));
    Assert.assertTrue(finalContentRefs.contains(elseDef));

    // cont2 def should reference if def and else def
    var cont2Refs = cont2Def.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRead);
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
      // ic def idx: 2
      // idx def idx: 3
      fn func(entity ent, inventory_component ic, int idx) {
        // method access idx: 7
        // i1 def idx: 9
        // inventory_component redef idx: 5
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
    var icParamDef = findNodeByProcessIdx(finalizedGroum, 2);
    var idxParamDef = findNodeByProcessIdx(finalizedGroum, 3);
    var icRedef = findNodeByProcessIdx(finalizedGroum, 5);
    var methodAccess = findNodeByProcessIdx(finalizedGroum, 7);
    var i1Def = findNodeByProcessIdx(finalizedGroum, 9);

    // test param ic redef
    var icRedefs = icRedef.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
    Assert.assertTrue(icRedefs.contains(icParamDef));
    Assert.assertEquals(1, icRedefs.size());

    // test method access reads
    var methodAccessReads = methodAccess.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(2, methodAccessReads.size());
    Assert.assertTrue(methodAccessReads.contains(icRedef));
    Assert.assertTrue(methodAccessReads.contains(idxParamDef));

    // test i1 reads
    var i1defReads = i1Def.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(3, i1defReads.size());
    Assert.assertTrue(i1defReads.contains(icRedef));
    Assert.assertTrue(i1defReads.contains(methodAccess));
    Assert.assertTrue(i1defReads.contains(idxParamDef));

  }

  @Test
  public void methodAccessInvalidation() {
    String program =
      """
      // ent def idx: 1
      // ic def idx: 2
      // idx def idx: 3
      // name def idx: 4
      fn func(entity ent, inventory_component ic, int idx, string name) {
        // def idx: 8
        ent.inventory_component = ic;
        // this redefines the ent definition and invalidates the inventory_component
        // ent redef idx: 10
        ent.set_name(name);

        // this defines the component
        // redef idx: 16
        ent.inventory_component = ic;

        // this redefines the inventory_component and invalidates the definition from above
        // ic redef idx: 20
        // i2 def idx: 23
        var i2 = ent.inventory_component.get_item(idx);
        // def idx: 26
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
    var entParamDef = findNodeByProcessIdx(finalizedGroum, 1);
    Assert.assertTrue(entParamDef instanceof ParameterInstantiationAction);
    var firstEntRedef = findNodeByProcessIdx(finalizedGroum, 8);
    Assert.assertTrue(firstEntRedef instanceof DefinitionAction);
    var setNameRedef = findNodeByProcessIdx(finalizedGroum, 10);
    Assert.assertTrue(setNameRedef instanceof DefinitionAction);
    var icRedef = findNodeByProcessIdx(finalizedGroum, 16);
    Assert.assertTrue(icRedef instanceof DefinitionAction);
    var getItemIcRedef = findNodeByProcessIdx(finalizedGroum, 20);
    Assert.assertTrue(getItemIcRedef instanceof DefinitionAction);

    var i2Def = findNodeByProcessIdx(finalizedGroum, 23);
    Assert.assertTrue(i2Def instanceof DefinitionAction);
    var myOtherIcDef = findNodeByProcessIdx(finalizedGroum, 26);
    Assert.assertTrue(myOtherIcDef instanceof DefinitionAction);

    // the setName call should invalidate the firstEntRedef and ent param def
    var setNameRedefs = setNameRedef.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
    Assert.assertTrue(setNameRedefs.contains(firstEntRedef));
    Assert.assertTrue(setNameRedefs.contains(entParamDef));
    Assert.assertEquals(2, setNameRedefs.size());

    // firstEntRedef should not be read
    var firstEntRedefReads = firstEntRedef.getEndsOfOutgoing(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(0, firstEntRedefReads.size());

    // icRedef should reference setNameRedef
    var icRedefRead = icRedef.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertTrue(icRedefRead.contains(setNameRedef));

    // getItemIcRedef should redefine icRedef
    var getItemIcRedefRedefs = getItemIcRedef.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
    Assert.assertTrue(getItemIcRedefRedefs.contains(icRedef));

    //i2Def should read from getItemIcRedef
    var i2DefReads = i2Def.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertTrue(i2DefReads.contains(getItemIcRedef));

    // myOtherIcDef should read from getItemIcRedef
    var myOtherIcDefReads = myOtherIcDef.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertTrue(myOtherIcDefReads.contains(getItemIcRedef));
  }

  @Test
  public void mixedMemberAccess() {
    String program =
      """
      // ent def idx: 1
      fn func(entity ent) {
        // inventory_component access idx: 2
        // 0 const def idx: 3
        // get item access idx: 5
        // inventory_component redefinition idx: 6
        // task_content_component access idx: 7
        // content access idx: 8
        // c def idx: 10
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
    var entDef = findNodeByProcessIdx(finalizedGroum, 1);
    var inventoryCompAccess = findNodeByProcessIdx(finalizedGroum, 2);
    var constDef = findNodeByProcessIdx(finalizedGroum, 3);
    var getItemAccess = findNodeByProcessIdx(finalizedGroum, 5);
    var tccAccess =findNodeByProcessIdx(finalizedGroum, 7);
    var contentAccess =findNodeByProcessIdx(finalizedGroum, 8);
    var icRedef = findNodeByProcessIdx(finalizedGroum, 6);
    var cDef = findNodeByProcessIdx(finalizedGroum, 10);

    var inventoryCompAccessReads = inventoryCompAccess.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(1, inventoryCompAccessReads.size());
    Assert.assertTrue(inventoryCompAccessReads.contains(entDef));

    var itemAccessReads = getItemAccess.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(2, itemAccessReads.size());
    Assert.assertTrue(itemAccessReads.contains(entDef));
    Assert.assertTrue(itemAccessReads.contains(inventoryCompAccess));

    var icRedefs = icRedef.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
    Assert.assertTrue(icRedefs.contains(inventoryCompAccess));

    var tccAccessReads = tccAccess.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(4, tccAccessReads.size());
    Assert.assertTrue(tccAccessReads.contains(icRedef));
    Assert.assertTrue(tccAccessReads.contains(constDef));
    Assert.assertTrue(tccAccessReads.contains(entDef));
    Assert.assertTrue(tccAccessReads.contains(getItemAccess));

    var contentAccessReads = contentAccess.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(5, contentAccessReads.size());
    Assert.assertTrue(contentAccessReads.contains(entDef));
    Assert.assertTrue(contentAccessReads.contains(constDef));
    Assert.assertTrue(contentAccessReads.contains(icRedef));
    Assert.assertTrue(contentAccessReads.contains(getItemAccess));
    Assert.assertTrue(contentAccessReads.contains(tccAccess));

    var cDefReads = cDef.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRead);
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
      // ent def idx: 1
      // other_end def idx: 2
      // idx def idx: 3
      fn func(entity ent, entity other_ent, int idx) {
        // inventory_component access idx: 4
        // get item access idx: 9
        // inventory_component redef idx: 11
        // use method access idx: 10
        // quest_item redef: 12
        // c def idx: 14
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
    var entParamDef = findNodeByProcessIdx(finalizedGroum, 1);
    var otherEntParamDef = findNodeByProcessIdx(finalizedGroum, 2);
    var idxParamDef = findNodeByProcessIdx(finalizedGroum, 3);
    var inventoryComponentAccess = findNodeByProcessIdx(finalizedGroum, 4);
    var getItemAccess = findNodeByProcessIdx(finalizedGroum, 9);
    var inventoryComponentRedef = findNodeByProcessIdx(finalizedGroum, 11);
    var useMethodAccess = findNodeByProcessIdx(finalizedGroum, 10);
    var questItemRedef = findNodeByProcessIdx(finalizedGroum, 12);
    var cDef = findNodeByProcessIdx(finalizedGroum, 14);

    // get item access should read ent, idx, and inventory_component
    var getItemAccessReads = getItemAccess.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(3, getItemAccessReads.size());
    Assert.assertTrue(getItemAccessReads.contains(entParamDef));
    Assert.assertTrue(getItemAccessReads.contains(inventoryComponentAccess));
    Assert.assertTrue(getItemAccessReads.contains(idxParamDef));

    // inventory component redef should redefine inventoryComponentAccess
    var inventoryComponentRedefs = inventoryComponentRedef.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
    Assert.assertTrue(inventoryComponentRedefs.contains(inventoryComponentAccess));

    // use method access should read ent, inventoryComponentRedef, idx, other_ent, getItemAccess
    var useMethodReads = useMethodAccess.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRead);
    Assert.assertEquals(5, useMethodReads.size());
    Assert.assertTrue(useMethodReads.contains(entParamDef));
    Assert.assertTrue(useMethodReads.contains(otherEntParamDef));
    Assert.assertTrue(useMethodReads.contains(inventoryComponentRedef));
    Assert.assertTrue(useMethodReads.contains(idxParamDef));
    Assert.assertTrue(useMethodReads.contains(getItemAccess));

    // quest item redefs should not actually redefine anything, because it is chained after a method..
    var questItemRedefs = questItemRedef.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRedefinition);
    Assert.assertEquals(0, questItemRedefs.size());

    // cdef should reference ent, use method access, getItemAccess, questItemRedef, inventoryComponentRedef, idx, other_ent
    var cDefReads = cDef.getStartsOfIncoming(GroumEdge.GroumEdgeType.dataDependencyRead);
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

  public static void write(String content, String path) {
    try {
      FileWriter writer = new FileWriter(path);
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
