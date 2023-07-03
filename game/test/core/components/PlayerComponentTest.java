package core.components;

import static org.junit.Assert.*;

import core.Entity;

import org.junit.Before;
import org.junit.Test;

import java.util.function.Consumer;

public class PlayerComponentTest {

    private static int counter = 0;
    private PlayerComponent playableComponent;

    @Before
    public void setup() {
        playableComponent = new PlayerComponent(new Entity());
    }

    @Test
    public void addFunction() {
        Consumer<Entity> function =
                new Consumer<Entity>() {
                    @Override
                    public void accept(Entity entity) {}
                };
        assertTrue(playableComponent.registerCallback(1, function).isEmpty());
    }

    public void addFunction_exisitng() {
        Consumer<Entity> function =
                new Consumer<Entity>() {
                    @Override
                    public void accept(Entity entity) {}
                };
        Consumer<Entity> newfunction =
                new Consumer<Entity>() {
                    @Override
                    public void accept(Entity entity) {}
                };
        playableComponent.registerCallback(1, function).get();
        assertEquals(function, playableComponent.registerCallback(1, newfunction).get());
    }
}
