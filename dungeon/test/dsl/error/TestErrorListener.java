package dsl.error;

import dsl.antlr.ParseTracerForTokenType;
import dsl.antlr.TreeUtils;
import dsl.helpers.Helpers;
import dsl.interpreter.TestEnvironment;
import dsl.profile.ProfilingTimer;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Assert;
import org.junit.Test;

public class TestErrorListener {

  @Test
  public void test() {
    String program = """
                fn test(int x, int, int) { }
                """;

    ErrorListener el = ErrorListener.INSTANCE;
    var stream = CharStreams.fromString(program);
    var lexer = new dsl.antlr.DungeonDSLLexer(stream);
    lexer.removeErrorListeners();
    lexer.addErrorListener(el);

    var tokenStream = new CommonTokenStream(lexer);
    var parser = new dsl.antlr.DungeonDSLParser(tokenStream);
    parser.removeErrorListeners();
    parser.addErrorListener(el);
    parser.setErrorHandler(new ErrorStrategy(lexer.getVocabulary(), true, true));

    // var eh = parser.getErrorHandler();

    var programParseTree = parser.program();
  }

  @Test
  // TODO: does not sync to my_type...
  public void testObjectDefinition() {
    String program =
        """
                    //obj: my_type obj1 {
                    //    val: id,
                    //    val:
                    //}

                    //obj: my_type obj2 {
                    //    val: id,
                    //    val: id
                    //}

                    my_type obj1 {
                        val: id,
                        val:
                    }

                    my_type obj2 {
                        val: id,
                        val: id
                    }
                """;

    ErrorListener el = ErrorListener.INSTANCE;
    var stream = CharStreams.fromString(program);
    TestEnvironment testEnvironment = new TestEnvironment();
    testEnvironment.addMockTypeName("my_type");
    var lexer = new dsl.antlr.DungeonDSLLexer(stream, testEnvironment);
    lexer.removeErrorListeners();
    lexer.addErrorListener(el);

    var tokenStream = new CommonTokenStream(lexer);
    var parser = new dsl.antlr.DungeonDSLParser(tokenStream, testEnvironment);
    parser.removeErrorListeners();
    parser.addErrorListener(el);
    parser.setErrorHandler(new ErrorStrategy(lexer.getVocabulary(), true, true));
    parser.setTrace(true);

    // var eh = parser.getErrorHandler();

    var programParseTree = parser.program();

    List<String> ruleNamesList = Arrays.asList(parser.getRuleNames());
    String prettyTree = TreeUtils.toPrettyTree(programParseTree, ruleNamesList);
    System.out.println(prettyTree);

    var logger = Logger.getLogger(ErrorListener.class.getName());
  }

  @Test
  public void testObjectDefinitionExtraCurly() {
    String program =
        """
                  my_type obj1 {
                      val: id,
                      val: id
                  } }

                  my_type obj2 {
                      val: id,
                      val: id
                  }
              """;

    ErrorListener el = ErrorListener.INSTANCE;
    var stream = CharStreams.fromString(program);
    TestEnvironment testEnvironment = new TestEnvironment();
    testEnvironment.addMockTypeName("my_type");
    var lexer = new dsl.antlr.DungeonDSLLexer(stream, testEnvironment);
    lexer.removeErrorListeners();
    lexer.addErrorListener(el);

    var tokenStream = new CommonTokenStream(lexer);
    var parser = new dsl.antlr.DungeonDSLParser(tokenStream, testEnvironment);
    parser.removeErrorListeners();
    parser.addErrorListener(el);
    parser.setErrorHandler(new ErrorStrategy(lexer.getVocabulary(), true, true));
    parser.setTrace(true);

    // var eh = parser.getErrorHandler();

    var programParseTree = parser.program();

    List<String> ruleNamesList = Arrays.asList(parser.getRuleNames());
    String prettyTree = TreeUtils.toPrettyTree(programParseTree, ruleNamesList);
    System.out.println(prettyTree);

    var logger = Logger.getLogger(ErrorListener.class.getName());
  }

  @Test
  public void testObjectDefinitionExtraComma() {
    String program =
        """
                  my_type obj1 {
                      val: id,,
                      val: id
                  }

                  my_type obj2 {
                      val: id,
                      val: id
                  }
              """;

    ErrorListener el = ErrorListener.INSTANCE;
    var stream = CharStreams.fromString(program);
    TestEnvironment testEnvironment = new TestEnvironment();
    testEnvironment.addMockTypeName("my_type");
    var lexer = new dsl.antlr.DungeonDSLLexer(stream, testEnvironment);
    lexer.removeErrorListeners();
    lexer.addErrorListener(el);

    var tokenStream = new CommonTokenStream(lexer);
    var parser = new dsl.antlr.DungeonDSLParser(tokenStream, testEnvironment);
    parser.removeErrorListeners();
    parser.addErrorListener(el);
    parser.setErrorHandler(new ErrorStrategy(lexer.getVocabulary(), true, true));
    parser.setTrace(true);

    // var eh = parser.getErrorHandler();

    var programParseTree = parser.program();

    List<String> ruleNamesList = Arrays.asList(parser.getRuleNames());
    String prettyTree = TreeUtils.toPrettyTree(programParseTree, ruleNamesList);
    System.out.println(prettyTree);

    var logger = Logger.getLogger(ErrorListener.class.getName());
  }

  @Test
  public void testObjectDefinitionBonkers() {
    String program =
        """
                asdf_type obj1 {
                    val1: id,,,,,asdfasl
                    val2:
                }

                asdf_type obj2 {
                    val3: id,
                    val4: id
                }

                /*my_type obj2 {
                    val: id,
                    val: id
                };*/

                fn test() {
                  print("Hello");
                  var count = 42;
                  var derp = 13;
                }
            """;

    ErrorListener el = ErrorListener.INSTANCE;
    var stream = CharStreams.fromString(program);
    TestEnvironment testEnvironment = new TestEnvironment();
    testEnvironment.addMockTypeName("asdf_type");
    var lexer = new dsl.antlr.DungeonDSLLexer(stream, testEnvironment);
    lexer.removeErrorListeners();
    lexer.addErrorListener(el);

    var tokenStream = new CommonTokenStream(lexer);
    var parser = new dsl.antlr.DungeonDSLParser(tokenStream, testEnvironment);
    parser.removeErrorListeners();
    parser.addErrorListener(el);
    parser.setErrorHandler(new ErrorStrategy(lexer.getVocabulary(), true, true));
    parser.setTrace(false);
    ParseTracerForTokenType ptftt = new ParseTracerForTokenType(parser);
    parser.addParseListener(ptftt);

    // var eh = parser.getErrorHandler();

    ProfilingTimer pt = new ProfilingTimer();
    pt.start();
    var programParseTree = parser.program();
    pt.stopAndPrint("After Parse");

    List<String> ruleNamesList = Arrays.asList(parser.getRuleNames());
    String prettyTree = TreeUtils.toPrettyTree(programParseTree, ruleNamesList);
    System.out.println(prettyTree);

    var logger = Logger.getLogger(ErrorListener.class.getName());
  }

  @Test
  public void testEntityTypeDefinition() {
    String program =
        """
              entity_type type1 {
                  comp1 {
                    val1: id,
                    val2:
                  },
                  comp2 {
                    val3: id,
                    val4: id
                  }
              }

              asdf_type obj1 {
                  val5: id,
                  val6: id
              }
          """;

    ErrorListener el = ErrorListener.INSTANCE;
    var stream = CharStreams.fromString(program);
    TestEnvironment testEnvironment = new TestEnvironment();
    testEnvironment.addMockTypeName("asdf_type");
    var lexer = new dsl.antlr.DungeonDSLLexer(stream, testEnvironment);
    lexer.removeErrorListeners();
    lexer.addErrorListener(el);

    var tokenStream = new CommonTokenStream(lexer);
    var parser = new dsl.antlr.DungeonDSLParser(tokenStream, testEnvironment);
    parser.removeErrorListeners();
    parser.addErrorListener(el);
    parser.setErrorHandler(new ErrorStrategy(lexer.getVocabulary(), true, true));
    parser.setTrace(false);

    // var eh = parser.getErrorHandler();

    ProfilingTimer pt = new ProfilingTimer();
    pt.start();
    var programParseTree = parser.program();
    pt.stopAndPrint("After Parse");

    List<String> ruleNamesList = Arrays.asList(parser.getRuleNames());
    String prettyTree = TreeUtils.toPrettyTree(programParseTree, ruleNamesList);
    System.out.println(prettyTree);

    var logger = Logger.getLogger(ErrorListener.class.getName());
  }

  @Test
  public void syncDot1() {
    String program =
        """
          graph afternoon_graph {
            // statement bound
              task1_a -> ; // missing rhs; sync auf nächstes statement (sync dot 1)
              task2_1_a -> task2_2_a [type=c_f];
          }

          asdf_type obj1 {
            val1: id1,
            val2: id2
          }
        """;

    String expectedTree =
        """
        program
          definition
            dot_def graph
              id afternoon_graph
              {
              dot_stmt_list
                dot_stmt
                  dot_edge_stmt
                    dot_node_list
                      id task1_a
                    dot_edge_RHS ->
                      dot_node_list
                        id
                          ;[ERROR_NODE]task2_1_a // this is the id, which we are interested in!
                    dot_edge_RHS ->
                      dot_node_list
                        id task2_2_a
                    dot_attr_list [
                      dot_attr
                        id type
                        =
                        id
                          dependency_type c_f
                      ]
                  ;
              }
          definition
            object_def asdf_typeobj1{
              property_def_list
                property_def
                  id val1
                  :
                  expression
                    logic_or
                      logic_and
                        equality
                          comparison
                            term
                              factor
                                unary
                                  primary
                                    id id1
                ,
                property_def
                  id val2
                  :
                  expression
                    logic_or
                      logic_and
                        equality
                          comparison
                            term
                              factor
                                unary
                                  primary
                                    id id2
              }
          <EOF>
        """;

    TestEnvironment testEnvironment = new TestEnvironment();
    testEnvironment.addMockTypeName("asdf_type");

    var mismatches = Helpers.validateParseTree(program, testEnvironment, expectedTree);
    System.out.println(mismatches);
    var tree = Helpers.getPrettyPrintedParseTree(program, testEnvironment);
    System.out.println(tree);
    Assert.assertEquals(0, mismatches.size());
  }

  @Test
  public void syncDot2() {
    String program =
        """
      graph name {
      	// statement bound
          task1_a -> // missing rhs and missing semicolon

          -> task2_2_a [type=c_f]; // missing lhs; sync auf nächstes statement
      	  task3 -> task4 [type=c_f];
      }

      asdf_type obj1 {
        val1: id
      }
      """;

    String expectedTree =
        """
          program
            definition
              dot_def graph
                id name
                {
                dot_stmt_list
                  dot_stmt
                    dot_edge_stmt
                      dot_node_list
                        id task1_a
                      dot_edge_RHS ->
                        dot_node_list
                          id
                            ->[ERROR_NODE]task2_2_a
                      dot_attr_list [
                        dot_attr
                          id type
                          =
                          id
                            dependency_type c_f
                        ]
                    ;
                  dot_stmt
                    dot_edge_stmt
                      dot_node_list
                        id task3
                      dot_edge_RHS ->
                        dot_node_list
                          id task4
                      dot_attr_list [
                        dot_attr
                          id type
                          =
                          id
                            dependency_type c_f
                        ]
                    ;
                }
            definition
              object_def asdf_typeobj1{
                property_def_list
                  property_def
                    id val1
                    :
                    expression
                      logic_or
                        logic_and
                          equality
                            comparison
                              term
                                factor
                                  unary
                                    primary
                                      id id
                }
            <EOF>
        """;

    TestEnvironment testEnvironment = new TestEnvironment();
    testEnvironment.addMockTypeName("asdf_type");

    String tree = Helpers.getPrettyPrintedParseTree(program, testEnvironment, true);
    System.out.println(tree);
    var mismatches = Helpers.validateParseTree(program, testEnvironment, expectedTree, true);
    System.out.println(mismatches);
    Assert.assertEquals(0, mismatches.size());
  }

  @Test
  // TODO: FIX
  public void syncDot3() {
    String program =
        """
    graph name {
      // statement bound
        task1_a -> ; // missing rhs

        -> task2_2_a [type=c_f]; // missing lhs; sync auf nächste übergeordnete Struktur
        task3 -> task4 [type=c_f];
    }

    asdf_type obj1 {
      val1: id
    }
    """;

    String expectedTree =
        """
          program
            definition
              dot_def graph
                id name
                {
                dot_stmt_list
                  dot_stmt
                    dot_edge_stmt
                      dot_node_list
                        id task1_a
                      dot_edge_RHS ->
                        dot_node_list
                          id[EXCEPTION IN NODE]
                    ;
                ->[ERROR_NODE]
                task2_2_a[ERROR_NODE]
                [[ERROR_NODE]
                type[ERROR_NODE]
                $DC$ // don't care about all nodes
            definition
              object_def asdf_typeobj1{
                property_def_list
                  property_def
                    id val1
                    :
                    expression
                      $DC$ // don't care about all following child nodes
                }
            <EOF>
      """;

    TestEnvironment testEnvironment = new TestEnvironment();
    testEnvironment.addMockTypeName("asdf_type");

    String tree = Helpers.getPrettyPrintedParseTree(program, testEnvironment, false);
    System.out.println(tree);
    var mismatches = Helpers.validateParseTree(program, testEnvironment, expectedTree, false);
    System.out.println(mismatches);
    Assert.assertEquals(0, mismatches.size());
  }

  @Test
  public void syncImport1() {
    String program =
        """
    #import "path/with/no/end : my_name;

    fn test() {
      println("Hello");
    }
    """;

    String expectedTree =
        """
        program
          definition
            import_def[EXCEPTION IN NODE]
          definition // TODO: have to find out, why this second definition node exists
            import_def
              #import[ERROR_NODE]
          definition
            fn_def fn // this is the important part, that we correctly sync to the function definition
              id test
              ()
              stmt_block {
                $DC$ // dont care about children
          <EOF>
      """;

    TestEnvironment testEnvironment = new TestEnvironment();
    testEnvironment.addMockTypeName("asdf_type");

    String tree = Helpers.getPrettyPrintedParseTree(program, testEnvironment, true);
    System.out.println(tree);
    var mismatches = Helpers.validateParseTree(program, testEnvironment, expectedTree, true);
    System.out.println(mismatches);
    Assert.assertEquals(0, mismatches.size());
  }

  @Test
  public void syncObj1() {
    String program =
      """
      asdf_type obj1 {
      	val1: id,
      	val2
      }

      asdf_type obj2 {
      	val3: id
      }
      """;

    String expectedTree =
      """
      program
            definition
              object_def asdf_typeobj1{
                property_def_list
                  property_def
                    id val1
                    :
                    expression
                      logic_or
                        logic_and
                          equality
                            comparison
                              term
                                factor
                                  unary
                                    primary
                                      id id
                  ,
                  property_def
                    val2[ERROR_NODE] // we have a dedicated error alternative for this case
                }
            definition
              object_def asdf_typeobj2{
                property_def_list
                  property_def
                    id val3
                    :
                    expression
                      $DC$
                }
            <EOF>
    """;

    TestEnvironment testEnvironment = new TestEnvironment();
    testEnvironment.addMockTypeName("asdf_type");

    String tree = Helpers.getPrettyPrintedParseTree(program, testEnvironment, true);
    System.out.println(tree);
    var mismatches = Helpers.validateParseTree(program, testEnvironment, expectedTree, true);
    System.out.println(mismatches);
    Assert.assertEquals(0, mismatches.size());
  }

  @Test
  public void syncObj2() {
    String program =
      """
      asdf_type { // broken, will sync to next object definition
      	val1: id,
      	val2
      }

      asdf_type obj2 {
      	val3: id
      }
      """;

    String expectedTree =
      """
    program
      definition
        object_def asdf_type
          {[ERROR_NODE]val1
          $DC$ // don't care from here on
      definition
        object_def asdf_typeobj2{
          property_def_list
            property_def
              id val3
              :
              expression
                $DC$
          }
      <EOF>
    """;

    TestEnvironment testEnvironment = new TestEnvironment();
    testEnvironment.addMockTypeName("asdf_type");

    String tree = Helpers.getPrettyPrintedParseTree(program, testEnvironment, false);
    System.out.println(tree);
    var mismatches = Helpers.validateParseTree(program, testEnvironment, expectedTree, false);
    System.out.println(mismatches);
    Assert.assertEquals(0, mismatches.size());
  }

  @Test
  public void syncObj3() {
    String program =
      """
      asdf_type obj1 {
      	val1: id,
      	val2: aggregate_val {
      	  val01: id,
      	  val02  // broken, will sync to next outer property-definition
      	},
      	val3: id
      }
      """;

    String expectedTree =
      """
        program
          definition
            object_def asdf_typeobj1{
              property_def_list
                property_def
                  id val1
                  :
                  expression
                    $DC$ // don't care about children
                ,
                property_def
                  id val2
                  :
                  expression
                    logic_or
                      logic_and
                        equality
                          comparison
                            term
                              factor
                                unary
                                  primary
                                    aggregate_value_def
                                      id aggregate_val
                                      {
                                      property_def_list
                                        property_def
                                          id val01
                                          :
                                          expression
                                            logic_or
                                              logic_and
                                                equality
                                                  comparison
                                                    term
                                                      factor
                                                        unary
                                                          primary
                                                            id id
                                        ,
                                        property_def
                                          val02[ERROR_NODE]
                                      }
                ,
                property_def
                  id val3
                  :
                  expression
                    logic_or
                      logic_and
                        equality
                          comparison
                            term
                              factor
                                unary
                                  primary
                                    id id
              }
          <EOF>
    """;

    TestEnvironment testEnvironment = new TestEnvironment();
    testEnvironment.addMockTypeName("asdf_type");

    String tree = Helpers.getPrettyPrintedParseTree(program, testEnvironment, false);
    System.out.println(tree);
    var mismatches = Helpers.validateParseTree(program, testEnvironment, expectedTree, false);
    System.out.println(mismatches);
    Assert.assertEquals(0, mismatches.size());
  }

  @Test
  public void syncEnt1() {
    String program =
      """
        entity_type type {
          comp1 {
        	  val1: id,
        	  val2: // broken, sync to next comp (comp2)
          },
          comp2 {
        	  val3: id,
        	  val4: jo
          }
        }
      """;

    String expectedTree =
      """
        program
              definition
                entity_type_def entity_type
                  id type
                  {
                  component_def_list
                    aggregate_value_def
                      id comp1
                      {
                      property_def_list
                        property_def
                          id val1
                          :
                          expression
                            logic_or
                              logic_and
                                equality
                                  comparison
                                    term
                                      factor
                                        unary
                                          primary
                                            id id
                        ,
                        property_def val2:
                      }
                    ,
                    aggregate_value_def
                      id comp2
                      {
                      property_def_list
                        property_def
                          id val3
                          :
                          expression
                            $DC$
                        ,
                        property_def
                          id val4
                          :
                          expression
                            $DC$
                      }
                  }
              <EOF>
    """;

    TestEnvironment testEnvironment = new TestEnvironment();

    String tree = Helpers.getPrettyPrintedParseTree(program, testEnvironment, false);
    System.out.println(tree);
    var mismatches = Helpers.validateParseTree(program, testEnvironment, expectedTree, false);
    System.out.println(mismatches);
    Assert.assertEquals(0, mismatches.size());
  }

  @Test
  public void syncEnt2() {
    String program =
      """
        entity_type type {
          comp1 {
        	  val1: id,
        	  val2: id
          }  // missing comma and missing id
          {
        	  val3: id,
        	  val4: jo
          }
        }

        fn test() {
        	println("Hello");
        }
      """;

    String expectedTree =
      """
        program
              definition
                entity_type_def entity_type
                  id type
                  {
                  component_def_list
                    aggregate_value_def
                      id comp1
                      {
                      property_def_list
                        property_def
                          id val1
                          :
                          expression
                            $DC$
                        ,
                        property_def
                          id val2
                          :
                          expression
                            $DC$
                      }
                  {[ERROR_NODE]
                  val3[ERROR_NODE]
                  :[ERROR_NODE]
                  id[ERROR_NODE]
                  ,[ERROR_NODE]
                  val4[ERROR_NODE]
                  :[ERROR_NODE]
                  jo[ERROR_NODE]
                  }[ERROR_NODE]
                  }[ERROR_NODE]
              definition
                fn_def fn
                  id test
                  ()
                  stmt_block {
                    stmt
                      expression
                        logic_or
                          logic_and
                            equality
                              comparison
                                term
                                  factor
                                    unary
                                      primary
                                        func_call
                                          $DC$
                      ;
                    }
              <EOF>
    """;

    TestEnvironment testEnvironment = new TestEnvironment();

    String tree = Helpers.getPrettyPrintedParseTree(program, testEnvironment, false);
    System.out.println(tree);
    var mismatches = Helpers.validateParseTree(program, testEnvironment, expectedTree, false);
    System.out.println(mismatches);
    Assert.assertEquals(0, mismatches.size());
  }

  @Test
  public void syncEnt3() {
    String program =
      """
      entity_type type {
        comp1 // missing '{'
      	  val1: id,
      	  val2: id;;; // extraneous ';'
        } // missing ','
        comp2 // missing '{}'
      }

      fn test() {
      	println("Hello");
      }
      """;

    String expectedTree =
      """
        program
          definition
            entity_type_def entity_type
              id type
              {
              component_def_list
                aggregate_value_def
                  id comp1
                  <missing '{'>[ERROR_NODE]
                  val1[ERROR_NODE]
                  :[ERROR_NODE]
                  id[ERROR_NODE]
                ,
                aggregate_value_def
                  id val2
                  :[ERROR_NODE]
                  id[ERROR_NODE]
                  ;[ERROR_NODE]
                  ;[ERROR_NODE]
                  ;[ERROR_NODE]
              }
          comp2[ERROR_NODE]
          }[ERROR_NODE]
          definition
            fn_def fn
              id test
              ()
              stmt_block {
                stmt
                  expression
                    logic_or
                      logic_and
                        equality
                          comparison
                            term
                              factor
                                unary
                                  primary
                                    func_call
                                      id println
                                      (
                                      expression_list
                                        $DC$
                                      )
                  ;
                }
          <EOF>
    """;

    TestEnvironment testEnvironment = new TestEnvironment();

    String tree = Helpers.getPrettyPrintedParseTree(program, testEnvironment, false);
    System.out.println(tree);
    var mismatches = Helpers.validateParseTree(program, testEnvironment, expectedTree, false);
    System.out.println(mismatches);
    Assert.assertEquals(0, mismatches.size());
  }

  @Test
  public void syncItem1() {
    String program =
      """
        item_type type1 {
          display_name: "Ein Itemtyp",
          description: "Ja!",
          texture_path: // missing expression
        }

        item_type type2 {
          display_name: "Typ"
        }
      """;

    String expectedTree =
      """
        program
              definition
                item_type_def item_type
                  id type1
                  {
                  property_def_list
                    property_def
                      id display_name
                      :
                      expression
                        $DC$
                    ,
                    property_def
                      id description
                      :
                      expression
                        $DC$
                    ,
                    property_def texture_path:
                  }
              definition
                item_type_def item_type
                  id type2
                  {
                  property_def_list
                    property_def
                      id display_name
                      :
                      expression
                        logic_or
                          logic_and
                            equality
                              comparison
                                term
                                  factor
                                    unary
                                      primary "Typ"
                  }
              <EOF>
    """;

    TestEnvironment testEnvironment = new TestEnvironment();

    String tree = Helpers.getPrettyPrintedParseTree(program, testEnvironment, false);
    System.out.println(tree);
    var mismatches = Helpers.validateParseTree(program, testEnvironment, expectedTree, false);
    System.out.println(mismatches);
    Assert.assertEquals(0, mismatches.size());
  }

  @Test
  public void syncItem2() {
    String program =
      """
        item_type type {
          display_name: "Ein Itemtyp",
          description: (;lkj;a,,sdf), // sollte auf texture_path synchronisieren
          texture_path: "Nein"
        }
      """;

    String expectedTree =
      """
        program
              definition
                item_type_def item_type
                  id type
                  {
                  property_def_list
                    property_def
                      id display_name
                      :
                      expression
                        logic_or
                          logic_and
                            equality
                              comparison
                                term
                                  factor
                                    unary
                                      primary "Ein Itemtyp"
                    ,
                    property_def
                      id description
                      :
                      expression
                        logic_or
                          logic_and
                            equality
                              comparison
                                term
                                  factor
                                    unary
                                      primary
                                        grouped_expression (
                                          expression
                                            ;[ERROR_NODE]
                                            logic_or
                                              logic_and
                                                equality
                                                  comparison
                                                    term
                                                      factor
                                                        unary
                                                          primary
                                                            id lkj
                                          ;[ERROR_NODE]
                                          a[ERROR_NODE]
                    ,
                    property_def
                      ,[ERROR_NODE]
                      sdf[ERROR_NODE]
                      )[ERROR_NODE]
                    ,
                    property_def
                      id texture_path
                      :
                      expression
                        logic_or
                          logic_and
                            equality
                              comparison
                                term
                                  factor
                                    unary
                                      primary "Nein"
                  }
              <EOF>
    """;

    TestEnvironment testEnvironment = new TestEnvironment();

    String tree = Helpers.getPrettyPrintedParseTree(program, testEnvironment, false);
    System.out.println(tree);
    var mismatches = Helpers.validateParseTree(program, testEnvironment, expectedTree, false);
    System.out.println(mismatches);
    Assert.assertEquals(0, mismatches.size());
  }

  @Test
  public void syncFn1() {
    String program =
      """
      fn test ( { // missing ')', sync to next definition

      }

      asdf_type obj1 {
        val1: id
      }
      """;

    String expectedTree =
      """
        program
          definition
            fn_def fn
              id test
              (
              {[ERROR_NODE]
              }[ERROR_NODE]
          definition
            object_def asdf_typeobj1{
              property_def_list
                property_def
                  id val1
                  :
                  expression
                    $DC$
              }
          <EOF>
    """;

    TestEnvironment testEnvironment = new TestEnvironment();
    testEnvironment.addMockTypeName("asdf_type");

    String tree = Helpers.getPrettyPrintedParseTree(program, testEnvironment, false);
    System.out.println(tree);
    var mismatches = Helpers.validateParseTree(program, testEnvironment, expectedTree, true);
    System.out.println(mismatches);
    Assert.assertEquals(0, mismatches.size());
  }

  @Test
  public void syncFn2() {
    String program =
      """
      fn test (int) { // wrong parameter definition, sync to next definition

      }

      asdf_type obj1 {
        val1: id
      }
      """;

    String expectedTree =
      """
        program
          definition
            fn_def fn
              id test
              (
              param_def_list
                param_def
                  type_decl int
              )
              stmt_block {}
          definition
            object_def asdf_typeobj1{
              property_def_list
                property_def
                  id val1
                  :
                  expression
                    logic_or
                      logic_and
                        equality
                          comparison
                            term
                              factor
                                unary
                                  primary
                                    id id
              }
          <EOF>
    """;

    TestEnvironment testEnvironment = new TestEnvironment();
    testEnvironment.addMockTypeName("asdf_type");

    String tree = Helpers.getPrettyPrintedParseTree(program, testEnvironment, false);
    System.out.println(tree);
    var mismatches = Helpers.validateParseTree(program, testEnvironment, expectedTree, true);
    System.out.println(mismatches);
    Assert.assertEquals(0, mismatches.size());
  }

  @Test
  public void syncFn3() {
    String program =
      """
      fn test (int x, int, int) { // wrong parameter declaration, sync to next definition

      }

      asdf_type obj1 {
        val1: id
      }
      """;

    String expectedTree =
      """
        program
          definition
            fn_def fn
              id test
              (
              param_def_list
                param_def
                  type_decl int
                  id x
                ,
                param_def
                  type_decl int
                ,
                param_def
                  type_decl int
              )
              stmt_block {}
          definition
            object_def asdf_typeobj1{
              property_def_list
                property_def
                  id val1
                  :
                  expression
                    logic_or
                      logic_and
                        equality
                          comparison
                            term
                              factor
                                unary
                                  primary
                                    id id
              }
          <EOF>
    """;

    TestEnvironment testEnvironment = new TestEnvironment();
    testEnvironment.addMockTypeName("asdf_type");

    String tree = Helpers.getPrettyPrintedParseTree(program, testEnvironment, false);
    System.out.println(tree);
    var mismatches = Helpers.validateParseTree(program, testEnvironment, expectedTree, true);
    System.out.println(mismatches);
    Assert.assertEquals(0, mismatches.size());
  }

  @Test
  public void syncFn4() {
    String program =
      """
      fn test (int x, int y, int z) {
      	println(x+); // sync to next statement
      	println();
      }
      """;

    String expectedTree =
      """
        program
          definition
            fn_def fn
              id test
              (
              param_def_list
                $DC$
              )
              stmt_block {
                stmt
                  expression
                    logic_or
                      logic_and
                        equality
                          comparison
                            term
                              factor
                                unary
                                  primary
                                    id println
                  <missing ';'>[ERROR_NODE]
                stmt
                  expression
                    logic_or
                      logic_and
                        equality
                          comparison
                            term
                              factor
                                unary
                                  primary
                                    grouped_expression (
                                      expression
                                        logic_or
                                          logic_and
                                            equality
                                              comparison
                                                term
                                                  term
                                                    factor
                                                      unary
                                                        primary
                                                          id x
                                                  +
                                                  factor
                                                    unary[EXCEPTION IN NODE]
                                      )
                  ;
                stmt
                  expression
                    logic_or
                      logic_and
                        equality
                          comparison
                            term
                              factor
                                unary
                                  primary
                                    func_call
                                      id println
                                      ()
                  ;
                }
          <EOF>
    """;

    TestEnvironment testEnvironment = new TestEnvironment();
    testEnvironment.addMockTypeName("asdf_type");

    String tree = Helpers.getPrettyPrintedParseTree(program, testEnvironment, false);
    System.out.println(tree);
    var mismatches = Helpers.validateParseTree(program, testEnvironment, expectedTree, true);
    System.out.println(mismatches);
    Assert.assertEquals(0, mismatches.size());
  }

  @Test
  public void syncFn5() {
    String program =
      """
        fn test (int x, int y, int z) {
        	var x = ((x+y)*z)); // extraneous ')', sync to next statement
        	println();
        }
      """;

    String expectedTree =
      """
        program
          definition
            fn_def fn
              id test
              (
              param_def_list
                $DC$
              )
              stmt_block {
                stmt
                  var_decl var
                    id x
                    =
                    expression
                      $DC$
                    )[ERROR_NODE];
                stmt // we mostly care about syncing to next statement
                  expression
                    logic_or
                      logic_and
                        equality
                          comparison
                            term
                              factor
                                unary
                                  primary
                                    func_call
                                      id println
                                      ()
                  ;
                }
          <EOF>
    """;

    TestEnvironment testEnvironment = new TestEnvironment();
    testEnvironment.addMockTypeName("asdf_type");

    String tree = Helpers.getPrettyPrintedParseTree(program, testEnvironment, false);
    System.out.println(tree);
    var mismatches = Helpers.validateParseTree(program, testEnvironment, expectedTree, true);
    System.out.println(mismatches);
    Assert.assertEquals(0, mismatches.size());
  }

  @Test
  public void syncFn6() {
    String program =
      """
        fn test (int x, int y, int z) {
        	var x = y;
        	println();

        asdf_type obj1 {
          val1: id
        }

      """;

    String expectedTree =
      """
        program
          definition
            fn_def fn
              id test
              (
              param_def_list
                $DC$
              )
              stmt_block {
                stmt
                  var_decl var
                    id x
                    =
                    expression
                      logic_or
                        logic_and
                          equality
                            comparison
                              term
                                factor
                                  unary
                                    primary
                                      id y
                    ;
                stmt
                  expression
                    logic_or
                      logic_and
                        equality
                          comparison
                            term
                              factor
                                unary
                                  primary
                                    func_call
                                      id println
                                      ()
                  ;
                stmt
                  expression
                    logic_or
                      logic_and
                        equality
                          comparison
                            term
                              factor
                                unary
                                  primary
                                    id asdf_type
                  <missing ';'>[ERROR_NODE]
                stmt
                  expression
                    logic_or
                      logic_and
                        equality
                          comparison
                            term
                              factor
                                unary
                                  primary
                                    aggregate_value_def
                                      id obj1
                                      {
                                      property_def_list
                                        property_def
                                          id val1
                                          :
                                          expression
                                            logic_or
                                              logic_and
                                                equality
                                                  comparison
                                                    term
                                                      factor
                                                        unary
                                                          primary
                                                            id id
                                      }
                <missing '}'>[ERROR_NODE]
          <EOF>
    """;

    TestEnvironment testEnvironment = new TestEnvironment();
    testEnvironment.addMockTypeName("asdf_type");

    String tree = Helpers.getPrettyPrintedParseTree(program, testEnvironment, false);
    System.out.println(tree);
    var mismatches = Helpers.validateParseTree(program, testEnvironment, expectedTree, true);
    System.out.println(mismatches);
    Assert.assertEquals(0, mismatches.size());
  }
}
