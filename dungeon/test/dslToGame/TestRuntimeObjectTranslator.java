package dslToGame;

import contrib.components.HealthComponent;

import core.Entity;
import core.components.CameraComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Point;

import helpers.Helpers;

import interpreter.DSLInterpreter;
import interpreter.TestEnvironment;
import interpreter.mockecs.*;

import org.junit.Assert;
import org.junit.Test;

import runtime.AggregateValue;
import runtime.GameEnvironment;
import runtime.Value;
ANDRE WAS HERE
import semanticanalysis.Scope;

public class TestRuntimeObjectTranslator {
    @Test
    public void testEntityTranslation() {
        var entity = new Entity();
        entity.addComponent(new CameraComponent(entity));
        entity.addComponent(new PositionComponent(entity, new Point(0, 0)));
        entity.addComponent(new VelocityComponent(entity));
        entity.addComponent(new HealthComponent(entity));

        String program = """
            quest_config my_quest_config {}
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

    @Test
    public void testIsolatedComponentTranslation() {
        var entity = new Entity();
        entity.addComponent(new CameraComponent(entity));
        entity.addComponent(new PositionComponent(entity, new Point(0, 0)));
        entity.addComponent(new VelocityComponent(entity));
        entity.addComponent(new HealthComponent(entity));

        String program = """
            quest_config my_quest_config {}
            """;

        var env = new GameEnvironment();
        var interpreter = new DSLInterpreter();
        Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

        var componentObject = entity.fetch(VelocityComponent.class).get();

        AggregateValue velocityValue =
                (AggregateValue)
                        interpreter
                                .getRuntimeEnvironment()
                                .translateRuntimeObject(
                                        componentObject, interpreter.getGlobalMemorySpace());

        var xVelocityValue = velocityValue.getMemorySpace().resolve("x_velocity");
        var internalXVelocityValue = xVelocityValue.getInternalValue();
        Assert.assertEquals(0.0f, internalXVelocityValue);

        xVelocityValue.setInternalValue(42.0f);
        float xVelocityFromComponent = componentObject.xVelocity();
        Assert.assertEquals(42.0f, xVelocityFromComponent, 0.0f);
    }

    @Test
    public void testIsolatedComponentTranslationPODAdapted() {
        String program = """
            quest_config my_quest_config {}
            """;

        var env = new TestEnvironment();
        env.getTypeBuilder().registerTypeAdapter(ExternalTypeBuilder.class, Scope.NULL);
        var interpreter = new DSLInterpreter();
        Helpers.generateQuestConfigWithCustomTypes(
                program,
                env,
                interpreter,
                interpreter.mockecs.Entity.class,
                ExternalType.class,
                TestComponentWithExternalType.class);

        interpreter.mockecs.Entity entity = new interpreter.mockecs.Entity();
        TestComponentWithExternalType componentObject = new TestComponentWithExternalType(entity);
        componentObject.setMemberExternalType(ExternalTypeBuilder.buildExternalType("Hello"));

        Value externalTypeValue =
                (Value)
                        interpreter
                                .getRuntimeEnvironment()
                                .translateRuntimeObject(
                                        componentObject.getMemberExternalType(),
                                        interpreter.getGlobalMemorySpace());
        Assert.assertTrue(true);
    }

    @Test
    public void testIsolatedComponentTranslationAggregateAdapted() {
        String program = """
            quest_config my_quest_config {}
            """;

        var env = new TestEnvironment();
        env.getTypeBuilder().registerTypeAdapter(ExternalTypeBuilderMultiParam.class, Scope.NULL);
        var interpreter = new DSLInterpreter();
        Helpers.generateQuestConfigWithCustomTypes(
                program,
                env,
                interpreter,
                interpreter.mockecs.Entity.class,
                ExternalType.class,
                TestComponentWithExternalType.class);

        interpreter.mockecs.Entity entity = new interpreter.mockecs.Entity();
        TestComponentWithExternalType componentObject = new TestComponentWithExternalType(entity);
        componentObject.setMemberExternalType(ExternalTypeBuilder.buildExternalType("Hello"));

        Value externalTypeValue =
                (Value)
                        interpreter
                                .getRuntimeEnvironment()
                                .translateRuntimeObject(
                                        componentObject.getMemberExternalType(),
                                        interpreter.getGlobalMemorySpace());
        Assert.assertTrue(true);
    }
}
