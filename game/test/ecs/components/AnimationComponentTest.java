package ecs.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import ecs.entitys.Entity;
import graphic.Animation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import mydungeon.ECS;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class AnimationComponentTest {

    private final Animation animation1 = Mockito.mock(Animation.class);
    private final Animation animation2 = Mockito.mock(Animation.class);
    private Entity entity;
    List<Animation> animations;

    @Before
    public void setup() {
        ECS.animationComponentMap = new HashMap<>();
        entity = new Entity();
        animations = Arrays.asList(animation1, animation2);
    }

    @Test
    public void testConstructor() {
        AnimationComponent component =
                new AnimationComponent(entity, animations, animations.get(0));
        assertNotNull(component);
        assertNotNull(ECS.animationComponentMap.get(entity));
    }

    @Test
    public void testSetCurrentAnimation() {
        Animation currentAnimation = animations.get(0);
        AnimationComponent component = new AnimationComponent(entity, animations, currentAnimation);
        // Ensure that the current animation is initially set to the expected value
        assertEquals(currentAnimation, component.getCurrentAnimation());

        // Set a new animation and ensure that it is correctly set
        Animation newAnimation = animations.get(1);
        component.setCurrentAnimation(newAnimation);
        assertEquals(newAnimation, component.getCurrentAnimation());
    }

    @Test
    public void testSetCurrentAnimationList() {
        AnimationComponent component =
                new AnimationComponent(entity, animations, animations.get(0));
        // Ensure that the current animationList is initially set to the expected value
        assertEquals(animations, component.getAnimationList());

        // Set a new animationList and ensure that it is correctly set
        List<Animation> newList = Arrays.asList(animation2, animation1, animation1);
        component.setAnimationList(newList, newList.get(0));
        assertEquals(newList, component.getAnimationList());
        assertEquals(newList.get(0), component.getCurrentAnimation());
    }
}
