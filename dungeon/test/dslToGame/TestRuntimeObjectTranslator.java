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
import interpreter.mockecs.ExternalType;
import interpreter.mockecs.ExternalTypeBuilder;
import interpreter.mockecs.ExternalTypeBuilderMultiParam;
import interpreter.mockecs.TestComponentWithExternalType;
import org.junit.Assert;
import org.junit.Test;

import runtime.AggregateValue;
import runtime.GameEnvironment;
import runtime.Value;
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
                (AggregateValue) env.translateRuntimeObject(entity, interpreter, interpreter.getGlobalMemorySpace());

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
            (AggregateValue) env.translateRuntimeObject(componentObject, interpreter, interpreter.getGlobalMemorySpace());

        var xVelocityValue = velocityValue.getMemorySpace().resolve("x_velocity");
        var internalXVelocityValue = xVelocityValue.getInternalValue();
        Assert.assertEquals(0.0f, internalXVelocityValue);

        xVelocityValue.setInternalValue(42.0f);
        float xVelocityFromComponent = componentObject.xVelocity();
        Assert.assertEquals(42.0f, xVelocityFromComponent, 0.0f);
    }

    // TODO: mocking this requires the following:
    //  - extend TestInvronment to load other translators
    @Test
    public void testIsolatedComponentTranslationAdapted() {
        String program = """
            quest_config my_quest_config {}
            """;

        var env = new TestEnvironment();
        env.getTypeBuilder().registerTypeAdapter(ExternalTypeBuilder.class, Scope.NULL);
        var interpreter = new DSLInterpreter();
        Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter, ExternalType.class, TestComponentWithExternalType.class);

        TestComponentWithExternalType componentObject = new TestComponentWithExternalType();

        // TODO: test this further and define, how encapsulated objects should behave
        //  externally and which use cases exist for them (translation and instantiation are kind of related, because
        //  the logic performed for instantiation right now is basically setting/applying defaults and then
        //  translating) -> this should be unified!
        AggregateValue componentDSLValue =
            (AggregateValue) env.translateRuntimeObject(componentObject, interpreter, interpreter.getGlobalMemorySpace());
    }
}
