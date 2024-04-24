package dslToGame;

import contrib.components.HealthComponent;
import core.Entity;
import core.Game;
import core.components.CameraComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Point;
import dsl.helpers.Helpers;
import dsl.interpreter.DSLInterpreter;
import dsl.interpreter.TestEnvironment;
import dsl.interpreter.mockecs.ExternalType;
import dsl.interpreter.mockecs.ExternalTypeBuilder;
import dsl.interpreter.mockecs.ExternalTypeBuilderMultiParam;
import dsl.interpreter.mockecs.TestComponentWithExternalType;
import dsl.runtime.value.AggregateValue;
import dsl.runtime.value.Value;
import dsl.semanticanalysis.environment.GameEnvironment;
import org.junit.Assert;
import org.junit.Test;

/** WTF? . */
public class TestRuntimeObjectTranslator {
  /** WTF? . */
  @Test
  public void testEntityTranslation() {
    Entity entity = new Entity();
    entity.add(new CameraComponent());
    entity.add(new PositionComponent(new Point(0, 0)));
    entity.add(new VelocityComponent());
    entity.add(new HealthComponent());
    Game.add(entity);

    String program = """
            dungeon_config my_quest_config {}
            """;

    var env = new GameEnvironment();
    var interpreter = new DSLInterpreter();
    Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

    AggregateValue entityAsValue =
        (AggregateValue)
            interpreter
                .getRuntimeEnvironment()
                .translateRuntimeObject(entity, interpreter.getGlobalMemorySpace());

    var velocityComponent =
        (AggregateValue) entityAsValue.getMemorySpace().resolve("velocity_component");
    Assert.assertNotEquals(Value.NONE, velocityComponent);

    var positionComponent =
        (AggregateValue) entityAsValue.getMemorySpace().resolve("position_component");
    Assert.assertNotEquals(Value.NONE, positionComponent);
  }

  /** WTF? . */
  @Test
  public void testIsolatedComponentTranslation() {
    Entity entity = new Entity();
    entity.add(new CameraComponent());
    entity.add(new PositionComponent(new Point(0, 0)));
    entity.add(new VelocityComponent());
    entity.add(new HealthComponent());
    Game.add(entity);

    String program = """
            dungeon_config my_quest_config {}
            """;

    var env = new GameEnvironment();
    var interpreter = new DSLInterpreter();
    Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

    var componentObject = entity.fetch(VelocityComponent.class).get();

    AggregateValue velocityValue =
        (AggregateValue)
            interpreter
                .getRuntimeEnvironment()
                .translateRuntimeObject(componentObject, interpreter.getGlobalMemorySpace());

    var xVelocityValue = velocityValue.getMemorySpace().resolve("x_velocity");
    var internalXVelocityValue = xVelocityValue.getInternalValue();
    Assert.assertEquals(0.0f, internalXVelocityValue);

    xVelocityValue.setInternalValue(42.0f);
    float xVelocityFromComponent = componentObject.xVelocity();
    Assert.assertEquals(42.0f, xVelocityFromComponent, 0.0f);
  }

  /** WTF? . */
  @Test
  public void testIsolatedComponentTranslationPODAdapted() {
    String program = """
            quest_config my_quest_config {}
            """;

    var env = new TestEnvironment();
    env.getTypeBuilder().registerTypeAdapter(ExternalTypeBuilder.class, env.getGlobalScope());
    var interpreter = new DSLInterpreter();
    Helpers.generateQuestConfigWithCustomTypes(
        program,
        env,
        interpreter,
        dsl.interpreter.mockecs.Entity.class,
        ExternalType.class,
        TestComponentWithExternalType.class);

    dsl.interpreter.mockecs.Entity entity = new dsl.interpreter.mockecs.Entity();
    TestComponentWithExternalType componentObject = new TestComponentWithExternalType(entity);
    componentObject.setMemberExternalType(ExternalTypeBuilder.buildExternalType("Hello"));

    Value externalTypeValue =
        (Value)
            interpreter
                .getRuntimeEnvironment()
                .translateRuntimeObject(
                    componentObject.getMemberExternalType(), interpreter.getGlobalMemorySpace());
    ExternalType object = (ExternalType) externalTypeValue.getInternalValue();
    Assert.assertEquals(42, object.member1);
    Assert.assertEquals(12, object.member2);
    Assert.assertEquals("Hello", object.member3);
  }

  /** WTF? . */
  @Test
  public void testIsolatedComponentTranslationAggregateAdapted() {
    String program = """
            quest_config my_quest_config {}
            """;

    var env = new TestEnvironment();
    env.getTypeBuilder()
        .registerTypeAdapter(ExternalTypeBuilderMultiParam.class, env.getGlobalScope());
    var interpreter = new DSLInterpreter();
    Helpers.generateQuestConfigWithCustomTypes(
        program,
        env,
        interpreter,
        dsl.interpreter.mockecs.Entity.class,
        ExternalType.class,
        TestComponentWithExternalType.class);

    dsl.interpreter.mockecs.Entity entity = new dsl.interpreter.mockecs.Entity();
    TestComponentWithExternalType componentObject = new TestComponentWithExternalType(entity);
    componentObject.setMemberExternalType(ExternalTypeBuilder.buildExternalType("Hello"));

    Value externalTypeValue =
        (Value)
            interpreter
                .getRuntimeEnvironment()
                .translateRuntimeObject(
                    componentObject.getMemberExternalType(), interpreter.getGlobalMemorySpace());
    Assert.assertTrue(true);
  }
}
