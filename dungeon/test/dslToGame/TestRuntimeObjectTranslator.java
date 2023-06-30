package dslToGame;

import contrib.components.HealthComponent;

import core.Entity;
import core.components.CameraComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Point;

import helpers.Helpers;

import interpreter.DSLInterpreter;

import org.junit.Assert;
import org.junit.Test;

import runtime.AggregateValue;
import runtime.GameEnvironment;
import runtime.Value;

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

        var rtEnv = interpreter.getRuntimeEnvironment();
        var globalMs = interpreter.getGlobalMemorySpace();
        var translator = new EntityTranslator();
        AggregateValue entityAsValue = translator.translate(entity, rtEnv, globalMs, interpreter);

        var velocityComponent =
                (AggregateValue) entityAsValue.getMemorySpace().resolve("velocity_component");
        Assert.assertNotEquals(Value.NONE, velocityComponent);

        var positionComponent =
                (AggregateValue) entityAsValue.getMemorySpace().resolve("position_component");
        Assert.assertNotEquals(Value.NONE, positionComponent);
    }
}
