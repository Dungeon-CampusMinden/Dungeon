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
