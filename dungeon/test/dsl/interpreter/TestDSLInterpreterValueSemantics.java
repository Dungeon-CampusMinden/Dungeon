package dsl.interpreter;

import contrib.components.InteractionComponent;
import contrib.components.ItemComponent;
import core.Entity;
import core.components.DrawComponent;
import dsl.helpers.Helpers;
import dsl.interpreter.mockecs.TestComponent2;
import dsl.interpreter.mockecs.TestComponentTestComponent2ConsumerCallback;
import dsl.runtime.value.ListValue;
import dsl.runtime.value.Value;
import dsl.semanticanalysis.environment.GameEnvironment;
import dsl.semanticanalysis.scope.Scope;
import dsl.semanticanalysis.typesystem.typebuilding.type.BuiltInType;
import dsl.semanticanalysis.typesystem.typebuilding.type.ListType;
import entrypoint.DungeonConfig;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import org.junit.Assert;
import org.junit.Test;
import task.game.content.QuestItem;
import task.tasktype.Quiz;
import task.tasktype.quizquestion.SingleChoice;

public class TestDSLInterpreterValueSemantics {
  @Test
  public void testPrototypeAssignment() {
    String program =
        """
    single_choice_task t1 {
        description: "Task1",
        answers: ["1", "2", "3"],
        correct_answer_index: 2,
        scenario_builder: build_scenario1
    }

    graph g {
        t1
    }

    dungeon_config c {
        dependency_graph: g
    }

    entity_type wizard_type {
        draw_component {
            path: "character/wizard"
        },
        hitbox_component {},
        position_component{},
        task_component{}
    }

    fn build_scenario1(single_choice_task t) -> entity<><> {
        var ret_set : entity<><>;

        var room_set : entity<>;

        var wizard : entity;
        var my_type : prototype;
        my_type = wizard_type;

        wizard = instantiate(my_type);
        wizard.task_component.task = t;

        room_set.add(wizard);

        ret_set.add(room_set);

        return ret_set;
    }
    """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);

    var task = config.dependencyGraph().nodeIterator().next().task();
    var builtTask = (HashSet<HashSet<Entity>>) interpreter.buildTask(task).get();
    var roomIter = builtTask.iterator();

    var firstRoomSet = roomIter.next();
    var entityInFirstRoom = firstRoomSet.iterator().next();
    DrawComponent drawComp1 = entityInFirstRoom.fetch(DrawComponent.class).get();
    var frameDrawComp1 = drawComp1.currentAnimation().animationFrames().get(0).pathString();
    Assert.assertTrue(frameDrawComp1.contains("wizard"));
  }

  @Test
  public void testAggregateValueAssignment() {
    String program =
        """
            single_choice_task t1 {
                description: "Task1",
                answers: ["1", "2", "3"],
                correct_answer_index: 2,
                scenario_builder: build_scenario1
            }

            graph g {
                t1
            }

            dungeon_config c {
                dependency_graph: g
            }

            entity_type wizard_type {
                draw_component {
                    path: "character/wizard"
                },
                hitbox_component {},
                interaction_component{},
                position_component{},
                task_component{}
            }

            fn build_scenario1(single_choice_task t) -> entity<><> {
                var ret_set : entity<><>;

                var room_set : entity<>;

                var wizard1 : entity;
                var wizard2 : entity;
                var my_type : prototype;
                my_type = wizard_type;

                wizard1 = instantiate(my_type);
                wizard1.task_component.task = t;

                wizard2 = wizard1;
                room_set.add(wizard2);

                wizard1.interaction_component.radius = 42.0;

                ret_set.add(room_set);
                return ret_set;
            }
            """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);

    var task = config.dependencyGraph().nodeIterator().next().task();
    var builtTask = (HashSet<HashSet<core.Entity>>) interpreter.buildTask(task).get();
    var roomIter = builtTask.iterator();

    var firstRoomSet = roomIter.next();
    var entityInFirstRoom = firstRoomSet.iterator().next();
    InteractionComponent interactionComponent =
        entityInFirstRoom.fetch(InteractionComponent.class).get();
    float radius = interactionComponent.radius();
    Assert.assertEquals(42.f, radius, 0.f);
  }

  @Test
  public void testEnumAssignment() {
    String program =
        """
        single_choice_task t1 {
            description: "Task1",
            answers: ["1", "2", "3"],
            correct_answer_index: 2,
            scenario_builder: build_scenario1
        }

        graph g {
            t1
        }

        dungeon_config c {
            dependency_graph: g
        }

        fn build_scenario1(single_choice_task t) -> entity<><> {
            var ret_set : entity<><>;
            var room_set : entity<>;

            var my_val : tile_direction;
            var my_other_val : tile_direction;
            my_other_val = tile_direction.N;
            my_val = my_other_val;
            print(my_val);

            ret_set.add(room_set);
            return ret_set;
        }
        """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    var task = config.dependencyGraph().nodeIterator().next().task();
    var builtTask = (HashSet<HashSet<core.Entity>>) interpreter.buildTask(task).get();

    String output = outputStream.toString();
    Assert.assertEquals("tile_direction.N" + System.lineSeparator(), output);
  }

  @Test
  public void testAggregateValueEquality() {
    String program =
        """
            single_choice_task t1 {
                description: "Task1",
                answers: ["1", "2", "3"],
                correct_answer_index: 2,
                scenario_builder: build_scenario1
            }

            graph g {
                t1
            }

            dungeon_config c {
                dependency_graph: g
            }

            entity_type wizard_type {
                draw_component {
                    path: "character/wizard"
                },
                hitbox_component {},
                interaction_component{},
                position_component{},
                task_component{}
            }

            fn build_scenario1(single_choice_task t) -> entity<><> {
                var ret_set : entity<><>;

                var room_set : entity<>;

                var wizard1 : entity;
                var wizard2 : entity;
                var my_type : prototype;
                my_type = wizard_type;

                wizard1 = instantiate(my_type);
                wizard1.task_component.task = t;
                wizard2 = wizard1;

                print(wizard1 == wizard2);

                ret_set.add(room_set);
                return ret_set;
            }
            """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    var task = config.dependencyGraph().nodeIterator().next().task();
    var builtTask = (HashSet<HashSet<core.Entity>>) interpreter.buildTask(task).get();

    String output = outputStream.toString();
    Assert.assertEquals("true" + System.lineSeparator(), output);
  }

  @Test
  public void testAggregatePropertyValueEquality() {
    String program =
        """
            single_choice_task t1 {
                description: "Task1",
                answers: ["1", "2", "3"],
                correct_answer_index: 2,
                scenario_builder: build_scenario1
            }

            graph g {
                t1
            }

            dungeon_config c {
                dependency_graph: g
            }

            entity_type wizard_type {
                draw_component {
                    path: "character/wizard"
                },
                hitbox_component {},
                interaction_component{},
                position_component{},
                task_component{}
            }

            fn build_scenario1(single_choice_task t) -> entity<><> {
                var ret_set : entity<><>;

                var room_set : entity<>;

                var wizard1 : entity;
                var wizard2 : entity;

                wizard1 = instantiate(wizard_type);
                wizard2 = instantiate(wizard_type);

                wizard1.task_component.task = t;
                wizard2.task_component.task = t;

                print(wizard1.task_component.task == wizard2.task_component.task);

                room_set.add(wizard2);
                ret_set.add(room_set);
                return ret_set;
            }
            """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    var task = config.dependencyGraph().nodeIterator().next().task();
    var builtTask = (HashSet<HashSet<core.Entity>>) interpreter.buildTask(task).get();

    String output = outputStream.toString();
    Assert.assertEquals("true" + System.lineSeparator(), output);
  }

  @Test
  public void testEncapsulatedObjectEquality() {
    String program =
        """
            single_choice_task t1 {
                description: "Task1",
                answers: ["1", "2", "3"],
                correct_answer_index: 2,
                scenario_builder: build_scenario1
            }

            graph g {
                t1
            }

            dungeon_config c {
                dependency_graph: g
            }

            entity_type wizard_type {
                draw_component {
                    path: "character/wizard"
                },
                hitbox_component {},
                interaction_component{},
                position_component{},
                task_component{}
            }

            fn build_scenario1(single_choice_task t) -> entity<><> {
                var ret_set : entity<><>;

                var room_set : entity<>;

                var wizard1 : entity;
                var wizard2 : entity;

                wizard1 = instantiate(wizard_type);
                wizard2 = instantiate(wizard_type);
                print(wizard1 == wizard2);

                wizard1 = wizard2;
                print(wizard1 == wizard2);

                room_set.add(wizard2);
                ret_set.add(room_set);
                return ret_set;
            }
            """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    var task = config.dependencyGraph().nodeIterator().next().task();
    var builtTask = (HashSet<HashSet<core.Entity>>) interpreter.buildTask(task).get();

    String output = outputStream.toString();
    Assert.assertEquals("false" + System.lineSeparator() + "true" + System.lineSeparator(), output);
  }

  @Test
  public void testEnumValueEquality() {
    String program =
        """
            single_choice_task t1 {
                description: "Task1",
                answers: ["1", "2", "3"],
                correct_answer_index: 2,
                scenario_builder: build_scenario1
            }

            graph g {
                t1
            }

            dungeon_config c {
                dependency_graph: g
            }

            fn build_scenario1(single_choice_task t) -> entity<><> {
                var ret_set : entity<><>;
                var room_set : entity<>;

                var val1 : tile_direction;
                var val2 : tile_direction;
                val1 = tile_direction.N;
                val2 = tile_direction.N;
                print(val1 == val2);

                ret_set.add(room_set);
                return ret_set;
            }
            """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    var task = config.dependencyGraph().nodeIterator().next().task();
    var builtTask = (HashSet<HashSet<core.Entity>>) interpreter.buildTask(task).get();

    String output = outputStream.toString();
    Assert.assertEquals("true" + System.lineSeparator(), output);
  }

  @Test
  public void testCallbackAdapterEquality() {
    String program =
        """
            single_choice_task t1 {
                description: "Task1",
                answers: ["1", "2", "3"],
                correct_answer_index: 2,
                scenario_builder: build_scenario1
            }

            entity_type wizard_type {
                draw_component {
                    path: "character/wizard"
                },
                interaction_component{}
            }

            graph g {
                t1
            }

            dungeon_config c {
                dependency_graph: g
            }

            fn on_interaction(entity ent1, entity ent2) {
                print("Hello");
            }

            fn build_scenario1(single_choice_task t) -> entity<><> {
                var ret_set : entity<><>;
                var room_set : entity<>;

                var wizard1 : entity;
                var wizard2 : entity;
                wizard1 = instantiate(wizard_type);
                wizard2 = instantiate(wizard_type);

                wizard1.interaction_component.on_interaction = on_interaction;
                wizard2.interaction_component.on_interaction = on_interaction;
                print(wizard1.interaction_component.on_interaction == wizard2.interaction_component.on_interaction);

                ret_set.add(room_set);
                return ret_set;
            }
            """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    var task = config.dependencyGraph().nodeIterator().next().task();
    var builtTask = (HashSet<HashSet<core.Entity>>) interpreter.buildTask(task).get();

    String output = outputStream.toString();
    Assert.assertEquals("true" + System.lineSeparator(), output);
  }

  @Test
  public void testFunctionValueEquality() {
    String program =
        """
            single_choice_task t1 {
                description: "Task1",
                answers: ["1", "2", "3"],
                correct_answer_index: 2,
                scenario_builder: build_scenario1
            }

            graph g {
                t1
            }

            dungeon_config c {
                dependency_graph: g
            }

            fn on_interaction(entity ent1, entity ent2) {
                print("Hello");
            }

            fn other_func() {}

            fn build_scenario1(single_choice_task t) -> entity<><> {
                var ret_set : entity<><>;
                var room_set : entity<>;

                print(on_interaction == other_func);
                print(on_interaction == on_interaction);

                ret_set.add(room_set);
                return ret_set;
            }
            """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    var task = config.dependencyGraph().nodeIterator().next().task();
    var builtTask = (HashSet<HashSet<core.Entity>>) interpreter.buildTask(task).get();

    String output = outputStream.toString();
    Assert.assertEquals("false" + System.lineSeparator() + "true" + System.lineSeparator(), output);
  }

  @Test
  public void testNonAssignableFunctionValue() {
    String program =
        """
            single_choice_task t1 {
                description: "Task1",
                answers: ["1", "2", "3"],
                correct_answer_index: 2,
                scenario_builder: build_scenario1
            }

            graph g {
                t1
            }

            dungeon_config c {
                dependency_graph: g
            }

            fn func() {
                print("Hello");
            }

            fn other_func() {
                print("Not Hello");
            }

            fn build_scenario1(single_choice_task t) -> entity<><> {
                var ret_set : entity<><>;
                var room_set : entity<>;

                func = other_func;
                func();

                ret_set.add(room_set);
                return ret_set;
            }
            """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    var task = config.dependencyGraph().nodeIterator().next().task();
    var builtTask = (HashSet<HashSet<core.Entity>>) interpreter.buildTask(task).get();

    String output = outputStream.toString();
    Assert.assertEquals("Hello" + System.lineSeparator(), output);
  }

  @Test
  public void testFunctionValueCallbackAdapterEquality() {
    String program =
        """
            single_choice_task t1 {
                description: "Task1",
                answers: ["1", "2", "3"],
                correct_answer_index: 2,
                scenario_builder: build_scenario1
            }

            graph g {
                t1
            }

            dungeon_config c {
                dependency_graph: g
            }

            entity_type wizard_type {
                draw_component {
                    path: "character/wizard"
                },
                interaction_component{}
            }

            fn func(entity ent1, entity ent2) {
                print("Hello");
            }

            fn other_func(entity ent1, entity ent2) {
                print("Not Hello");
            }

            fn build_scenario1(single_choice_task t) -> entity<><> {
                var ret_set : entity<><>;
                var room_set : entity<>;

                var wizard1 : entity;
                wizard1 = instantiate(wizard_type);

                wizard1.interaction_component.on_interaction = func;
                print(wizard1.interaction_component.on_interaction == func);
                print(wizard1.interaction_component.on_interaction == other_func);

                ret_set.add(room_set);
                return ret_set;
            }
            """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    var task = config.dependencyGraph().nodeIterator().next().task();
    var builtTask = (HashSet<HashSet<core.Entity>>) interpreter.buildTask(task).get();

    String output = outputStream.toString();
    Assert.assertEquals("true" + System.lineSeparator() + "false" + System.lineSeparator(), output);
  }

  @Test
  public void testEncapsulatedFieldValueEquality() {
    String program =
        """
            single_choice_task t1 {
                description: "Task1",
                answers: ["1", "2", "3"],
                correct_answer_index: 2,
                scenario_builder: build_scenario1
            }

            entity_type wizard_type {
                draw_component {
                    path: "character/wizard"
                },
                interaction_component{}
            }

            graph g {
                t1
            }

            dungeon_config c {
                dependency_graph: g
            }

            fn on_interaction(entity ent1, entity ent2) {
                print("Hello");
            }

            fn build_scenario1(single_choice_task t) -> entity<><> {
                var ret_set : entity<><>;
                var room_set : entity<>;

                var wizard1 : entity;
                wizard1 = instantiate(wizard_type);

                wizard1.interaction_component.radius = 42.0;
                print(wizard1.interaction_component.radius == 42.0);

                ret_set.add(room_set);
                return ret_set;
            }
            """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    var task = config.dependencyGraph().nodeIterator().next().task();
    var builtTask = (HashSet<HashSet<core.Entity>>) interpreter.buildTask(task).get();

    String output = outputStream.toString();
    Assert.assertEquals("true" + System.lineSeparator(), output);
  }

  @Test
  public void testListEqualsInteger() {
    String program =
        """
            single_choice_task t1 {
                description: "Task1",
                answers: ["1", "2", "3"],
                correct_answer_index: 2,
                scenario_builder: build_scenario1
            }

            graph g {
                t1
            }

            dungeon_config c {
                dependency_graph: g
            }

            fn build_scenario1(single_choice_task t) -> entity<><> {
                var ret_set : entity<><>;
                var room_set : entity<>;

                var list1 : int[];
                var list2 : int[];
                list1.add(1);
                list1.add(2);
                list1.add(3);
                list2.add(1);
                list2.add(2);
                list2.add(3);
                print(list1 == list2);

                ret_set.add(room_set);
                return ret_set;
            }
            """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    var task = config.dependencyGraph().nodeIterator().next().task();
    var builtTask = (HashSet<HashSet<core.Entity>>) interpreter.buildTask(task).get();

    String output = outputStream.toString();
    Assert.assertEquals("true" + System.lineSeparator(), output);
  }

  @Test
  public void testListEqualsAggregateValue() {
    String program =
        """
            single_choice_task t1 {
                description: "Task1",
                answers: ["1", "2", "3"],
                correct_answer_index: 2,
                scenario_builder: build_scenario1
            }

            entity_type wizard_type {
                draw_component {
                    path: "character/wizard"
                },
                interaction_component{}
            }

            graph g {
                t1
            }

            dungeon_config c {
                dependency_graph: g
            }

            fn build_scenario1(single_choice_task t) -> entity<><> {
                var ret_set : entity<><>;
                var room_set : entity<>;

                var list1 : entity[];
                var list2 : entity[];
                var wizard1 : entity;
                wizard1 = instantiate(wizard_type);
                var wizard2 : entity;
                wizard2 = instantiate(wizard_type);
                var wizard3 : entity;
                wizard3 = instantiate(wizard_type);

                list1.add(wizard1);
                list1.add(wizard2);
                list1.add(wizard3);
                list2.add(wizard1);
                list2.add(wizard2);
                list2.add(wizard3);
                print(list1 == list2);

                ret_set.add(room_set);
                return ret_set;
            }
            """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    var task = config.dependencyGraph().nodeIterator().next().task();
    var builtTask = (HashSet<HashSet<core.Entity>>) interpreter.buildTask(task).get();

    String output = outputStream.toString();
    Assert.assertEquals("true" + System.lineSeparator(), output);
  }

  @Test
  public void testSetEqualsInteger() {
    String program =
        """
            single_choice_task t1 {
                description: "Task1",
                answers: ["1", "2", "3"],
                correct_answer_index: 2,
                scenario_builder: build_scenario1
            }

            entity_type wizard_type {
                draw_component {
                    path: "character/wizard"
                },
                interaction_component{}
            }

            graph g {
                t1
            }

            dungeon_config c {
                dependency_graph: g
            }

            fn build_scenario1(single_choice_task t) -> entity<><> {
                var ret_set : entity<><>;
                var room_set : entity<>;

                var set1 : int<>;
                var set2 : int<>;

                set1.add(1);
                set1.add(2);
                set1.add(3);
                set2.add(1);
                set2.add(2);
                set2.add(3);
                print(set1 == set2);

                ret_set.add(room_set);
                return ret_set;
            }
            """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    var task = config.dependencyGraph().nodeIterator().next().task();
    var builtTask = (HashSet<HashSet<core.Entity>>) interpreter.buildTask(task).get();

    String output = outputStream.toString();
    Assert.assertEquals("true" + System.lineSeparator(), output);
  }

  @Test
  public void testSetEqualsAggregateValue() {
    String program =
        """
            single_choice_task t1 {
                description: "Task1",
                answers: ["1", "2", "3"],
                correct_answer_index: 2,
                scenario_builder: build_scenario1
            }

            entity_type wizard_type {
                draw_component {
                    path: "character/wizard"
                },
                interaction_component{}
            }

            graph g {
                t1
            }

            dungeon_config c {
                dependency_graph: g
            }

            fn build_scenario1(single_choice_task t) -> entity<><> {
                var ret_set : entity<><>;
                var room_set : entity<>;

                var set1 : entity<>;
                var set2 : entity<>;
                var wizard1 : entity;
                wizard1 = instantiate(wizard_type);
                var wizard2 : entity;
                wizard2 = instantiate(wizard_type);
                var wizard3 : entity;
                wizard3 = instantiate(wizard_type);

                set1.add(wizard1);
                set1.add(wizard2);
                set1.add(wizard3);
                set2.add(wizard1);
                set2.add(wizard2);
                set2.add(wizard3);
                print(set1 == set2);

                ret_set.add(room_set);
                return ret_set;
            }
            """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    var task = config.dependencyGraph().nodeIterator().next().task();
    var builtTask = (HashSet<HashSet<core.Entity>>) interpreter.buildTask(task).get();

    String output = outputStream.toString();
    Assert.assertEquals("true" + System.lineSeparator(), output);
  }

  @Test
  public void testMapEquals() {
    String program =
        """
            single_choice_task t1 {
                description: "Task1",
                answers: ["1", "2", "3"],
                correct_answer_index: 2,
                scenario_builder: build_scenario1
            }

            graph g {
                t1
            }

            dungeon_config c {
                dependency_graph: g
            }

            fn build_scenario1(single_choice_task t) -> entity<><> {
                var ret_set : entity<><>;
                var room_set : entity<>;

                var map1 : [int -> string];
                var map2 : [int -> string];

                map1.add(1, "Hello");
                map1.add(2, "World");
                map1.add(3, "!");
                map2.add(1, "Hello");
                map2.add(2, "World");
                map2.add(3, "!");

                print(map1 == map2);

                map2.clear();
                map2.add(1, "Hello");
                map2.add(2, "World");

                print(map1 == map2);

                map1 = map2;
                print(map1 == map2);

                ret_set.add(room_set);
                return ret_set;
            }
            """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    var task = config.dependencyGraph().nodeIterator().next().task();
    var builtTask = (HashSet<HashSet<core.Entity>>) interpreter.buildTask(task).get();

    String output = outputStream.toString();
    Assert.assertEquals(
        "true"
            + System.lineSeparator()
            + "false"
            + System.lineSeparator()
            + "true"
            + System.lineSeparator(),
        output);
  }

  @Test
  public void testPropertyValueEquality() {
    String program =
        """
            entity_type my_type {
                test_component2 {
                    member2: 42,
                    this_is_a_float: 3.14
                },
                test_component_with_callback {
                    consumer: get_property
                }
            }

            fn get_property(test_component2 comp) {
                var other_entity : entity;
                other_entity = instantiate(my_type);
                print(comp.this_is_a_float == other_entity.test_component2.this_is_a_float);
            }

            quest_config c {
                entity: instantiate(my_type)
            }
            """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(
            env.getGlobalScope(), dsl.interpreter.mockecs.Entity.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(env.getGlobalScope(), TestComponent2.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(
            env.getGlobalScope(), TestComponentTestComponent2ConsumerCallback.class);
    env.getTypeBuilder()
        .bindProperty(env.getGlobalScope(), TestComponent2.TestComponentPseudoProperty.instance);
    env.getTypeBuilder()
        .bindProperty(env.getGlobalScope(), TestComponent2.TestComponent2EntityProperty.instance);

    var config =
        (CustomQuestConfig) Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

    var entity = config.entity();
    var componentWithConsumer =
        (TestComponentTestComponent2ConsumerCallback) entity.components.get(0);
    var testComponent2 = (TestComponent2) entity.components.get(1);
    componentWithConsumer.consumer.accept(testComponent2);

    String output = outputStream.toString();
    Assert.assertEquals("true" + System.lineSeparator(), output);
  }

  @Test
  public void testPrototypeEquals() {
    String program =
        """
            single_choice_task t1 {
                description: "Task1",
                answers: ["1", "2", "3"],
                correct_answer_index: 2,
                scenario_builder: build_scenario1
            }

            entity_type wizard_type {
                draw_component {
                    path: "character/wizard"
                },
                interaction_component{}
            }

            graph g {
                t1
            }

            dungeon_config c {
                dependency_graph: g
            }

            fn build_scenario1(single_choice_task t) -> entity<><> {
                var ret_set : entity<><>;
                var room_set : entity<>;

                var type1 : prototype;
                var type2 : prototype;
                type1 = wizard_type;
                type2 = wizard_type;
                // trivial, but should still test it
                print(type1 == type2);

                ret_set.add(room_set);
                return ret_set;
            }
            """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    var task = config.dependencyGraph().nodeIterator().next().task();
    var builtTask = (HashSet<HashSet<core.Entity>>) interpreter.buildTask(task).get();

    String output = outputStream.toString();
    Assert.assertEquals("true" + System.lineSeparator(), output);
  }

  @Test
  public void testSetInsertion() {
    String program =
        """
        single_choice_task t1 {
            description: "Task1",
            answers: [ "1", "2", "3", "4"],
            correct_answer_index: 3
        }

        graph g {
            t1
        }

        dungeon_config c {
            dependency_graph: g
        }

        item_type scroll_type {
            display_name: "A scroll",
            description: "Please read me",
            texture_path: "items/book/wisdom_scroll.png"
        }

        fn build_task(single_choice_task t) -> entity<><> {
            var return_set : entity<><>;
            var room_set : entity<>;

            // instantiate items
            var item_entity : entity;
            for task_content answer in t.get_content() {
                var item : quest_item;
                item = build_quest_item(scroll_type, answer);
                item_entity = build_item_entity(item);
                room_set.add(item_entity);
            }

            return_set.add(room_set);
            return return_set;
        }
        """;
    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);
    var task = (SingleChoice) config.dependencyGraph().nodeIterator().next().task();

    var builtTask = (HashSet<HashSet<core.Entity>>) interpreter.buildTask(task).get();

    // find all "scrolls"
    HashSet<String> elementContents = new HashSet<>();
    for (var roomSet : builtTask) {
      for (core.Entity entity : roomSet) {
        var optionalItemComp = entity.fetch(ItemComponent.class);
        if (optionalItemComp.isPresent()) {
          ItemComponent itemComp = optionalItemComp.get();
          QuestItem questItem = (QuestItem) itemComp.item();
          var element = (Quiz.Content) questItem.taskContentComponent().content();
          elementContents.add(element.content());
        }
      }
    }

    Assert.assertEquals(4, elementContents.size());
    Assert.assertTrue(elementContents.contains("1"));
    Assert.assertTrue(elementContents.contains("2"));
    Assert.assertTrue(elementContents.contains("3"));
    Assert.assertTrue(elementContents.contains("4"));
  }

  @Test
  public void testListManipulationThroughSet() {
    String program =
        """
        single_choice_task t1 {
            description: "Task1",
            answers: [ "1", "2", "3", "4"],
            correct_answer_index: 3
        }

        graph g {
            t1
        }

        dungeon_config c {
            dependency_graph: g
        }

        item_type scroll_type {
            display_name: "A scroll",
            description: "Please read me",
            texture_path: "items/book/wisdom_scroll.png"
        }

        fn build_task(single_choice_task t) -> entity<><> {
            var return_set : entity<><>;
            var room_set : entity<>;

            var my_int_list_set : int[]<>;
            var my_int_list : int[];
            my_int_list.add(1);
            my_int_list.add(2);
            my_int_list.add(3);
            my_int_list.add(4);
            my_int_list_set.add(my_int_list);

            for int[] list in my_int_list_set {
                list.clear();
                list.add(5);
                list.add(6);
                list.add(7);
                list.add(8);
            }

            for int[] list in my_int_list_set {
                for int val in list {
                    print(val);
                }
            }

            return_set.add(room_set);
            return return_set;
        }
        """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);
    var task = (SingleChoice) config.dependencyGraph().nodeIterator().next().task();

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    var builtTask = (HashSet<HashSet<core.Entity>>) interpreter.buildTask(task).get();

    String output = outputStream.toString();
    Assert.assertEquals(
        "5"
            + System.lineSeparator()
            + "6"
            + System.lineSeparator()
            + "7"
            + System.lineSeparator()
            + "8"
            + System.lineSeparator(),
        output);
  }

  @Test
  public void testListAssignment() {
    String program =
        """
        single_choice_task t1 {
            description: "Task1",
            answers: [ "1", "2", "3", "4"],
            correct_answer_index: 3
        }

        graph g {
            t1
        }

        dungeon_config c {
            dependency_graph: g
        }

        fn build_task(single_choice_task t) -> entity<><> {
            var return_set : entity<><>;
            var room_set : entity<>;

            var my_int_list : int[];
            var my_other_int_list : int[];

            // initialize int list
            my_int_list.add(1);
            my_int_list.add(2);

            // assign int list to other int list
            my_other_int_list = my_int_list;

            // modify int list through other int list
            my_other_int_list.add(3);

            for int val in my_int_list {
                print(val);
            }

            return_set.add(room_set);
            return return_set;
        }
        """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);
    var task = (SingleChoice) config.dependencyGraph().nodeIterator().next().task();

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    var builtTask = (HashSet<HashSet<core.Entity>>) interpreter.buildTask(task).get();

    String output = outputStream.toString();
    Assert.assertEquals(
        "1" + System.lineSeparator() + "2" + System.lineSeparator() + "3" + System.lineSeparator(),
        output);
  }

  @Test
  public void testSetAssignment() {
    String program =
        """
        single_choice_task t1 {
            description: "Task1",
            answers: [ "1", "2", "3", "4"],
            correct_answer_index: 3
        }

        graph g {
            t1
        }

        dungeon_config c {
            dependency_graph: g
        }

        fn build_task(single_choice_task t) -> entity<><> {
            var return_set : entity<><>;
            var room_set : entity<>;

            var my_int_set : int<>;
            var my_other_int_set : int<>;

            // initialize int set
            my_int_set.add(1);
            my_int_set.add(2);

            // assign int set to other int set
            my_other_int_set = my_int_set;

            // modify int set through other int set
            my_other_int_set.add(3);

            if my_int_set.contains(3) {
                print("true");
            }

            return_set.add(room_set);
            return return_set;
        }
        """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);
    var task = (SingleChoice) config.dependencyGraph().nodeIterator().next().task();

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    var builtTask = (HashSet<HashSet<core.Entity>>) interpreter.buildTask(task).get();

    String output = outputStream.toString();
    Assert.assertEquals("true" + System.lineSeparator(), output);
  }

  @Test
  public void testValueEqualityNONE() {
    Value val = Value.NONE;
    // because of the custom `equals`-implementation of Value,
    // it is in fact NOT trivially always true, that `val` is `equal`
    // to `Value.NONE` and needs to be tested
    @SuppressWarnings("all")
    boolean equal = val.equals(Value.NONE);
    // see above
    Assert.assertTrue(equal);
  }

  @Test
  public void testValueEqualityPOD() {
    Value val = new Value(BuiltInType.intType, 42);
    Value otherVal = new Value(BuiltInType.intType, 42);
    boolean equal = val.equals(otherVal);
    Assert.assertTrue(equal);
  }

  @Test
  public void testValueEqualityListInternalValues() {
    ListType type = new ListType(BuiltInType.intType, Scope.NULL);
    ListValue val1 = new ListValue(type);
    ListValue val2 = new ListValue(type);

    val1.addValue(new Value(BuiltInType.intType, 1));
    val1.addValue(new Value(BuiltInType.intType, 2));

    val2.addValue(new Value(BuiltInType.intType, 1));
    val2.addValue(new Value(BuiltInType.intType, 2));

    boolean equals = val1.equals(val2);
    Assert.assertTrue(equals);
  }

  @Test
  public void testValueEqualityListReference() {
    ListType type = new ListType(BuiltInType.intType, Scope.NULL);
    ListValue val1 = new ListValue(type);
    ListValue val2 = new ListValue(type);

    val1.addValue(new Value(BuiltInType.intType, 1));
    val1.addValue(new Value(BuiltInType.intType, 2));

    val2.addValue(new Value(BuiltInType.intType, 3));
    val2.addValue(new Value(BuiltInType.intType, 4));

    // setup interpreter -> let interpreter analyze
    DSLInterpreter interpreter = new DSLInterpreter();

    interpreter.initializeRuntime(new GameEnvironment(), null);

    interpreter.setValue(val1, val2);
    boolean equals = val1.equals(val2);
    Assert.assertTrue(equals);
  }

  @Test
  public void testSetInsertionOfSet() {
    String program =
        """
        single_choice_task t1 {
            description: "Task1",
            answers: [ "1", "2", "3", "4"],
            correct_answer_index: 3
        }

        graph g {
            t1
        }

        dungeon_config c {
            dependency_graph: g
        }

        fn build_task(single_choice_task t) -> entity<><> {
            var return_set : entity<><>;
            var room_set : entity<>;

            var main_set : int<><>;
            var cont : bool;
            var counter : int;
            var counter_list : int[];
            counter_list.add(1);
            counter_list.add(2);
            counter_list.add(3);
            counter_list.add(4);
            cont = true;

            while cont {
                var insert_set : int<>;
                insert_set.add(0);
                insert_set.add(1);
                insert_set.add(2);
                main_set.add(insert_set);

                // check loop condition
                counter = counter_list.get(counter);
                if counter == 4  {
                    cont = false;
                }
            }

            var first_entry : bool;
            first_entry = true;
            for int<> entry in main_set {
                if first_entry {
                    entry.add(4);
                    first_entry = false;
                }
            }

            print(main_set.size());

            var sets_with_three_entries : int;
            var sets_with_four_entries : int;

            for int<> entry in main_set {
                if entry.size() == 3 {
                    sets_with_three_entries = counter_list.get(sets_with_three_entries);
                }
                if entry.size() == 4 {
                    sets_with_four_entries = counter_list.get(sets_with_four_entries);
                }
            }
            print(sets_with_three_entries);
            print(sets_with_four_entries);

            return_set.add(room_set);
            return return_set;
        }
        """;
    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);
    var task = (SingleChoice) config.dependencyGraph().nodeIterator().next().task();

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));
    var builtTask = (HashSet<HashSet<core.Entity>>) interpreter.buildTask(task).get();

    String output = outputStream.toString();
    Assert.assertEquals(
        "4" + System.lineSeparator() + "3" + System.lineSeparator() + "1" + System.lineSeparator(),
        output);
  }
}
