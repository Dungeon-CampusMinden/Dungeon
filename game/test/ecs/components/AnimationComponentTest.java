package ecs.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import ecs.entitys.Entity;
import graphic.Animation;
import java.util.HashMap;
import mydungeon.ECS;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class AnimationComponentTest {

    private final Animation animation1 = Mockito.mock(Animation.class);
    private final Animation animation2 = Mockito.mock(Animation.class);
    private Entity entity;
    AnimationList animations;

    @Before
    public void setup() {
        ECS.animationComponentMap = new HashMap<>();
        entity = new Entity();
        animations = new AnimationList();
        animations.setIdleRight(animation1);
        animations.setIdleLeft(animation2);
    }

    @Test
    public void testConstructor() {
        AnimationComponent component =
                new AnimationComponent(entity, animations, animations.getIdleRight());
        assertNotNull(component);
        assertNotNull(ECS.animationComponentMap.get(entity));
    }

    @Test
    public void testSetCurrentAnimation() {
        Animation currentAnimation = animations.getIdleRight();
        AnimationComponent component = new AnimationComponent(entity, animations, currentAnimation);
        // Ensure that the current animation is initially set to the expected value
        assertEquals(currentAnimation, component.getCurrentAnimation());

        // Set a new animation and ensure that it is correctly set
        Animation newAnimation = animations.getIdleLeft();
        component.setCurrentAnimation(newAnimation);
        assertEquals(newAnimation, component.getCurrentAnimation());
    }

    @Test
    public void testSetCurrentAnimationList() {
        AnimationComponent component =
                new AnimationComponent(entity, animations, animations.getIdleRight());
        // Ensure that the current animationList is initially set to the expected value
        assertEquals(animations, component.getAnimationList());

        // Set a new animationList and ensure that it is correctly set
        AnimationList newList = new AnimationList();
        newList.setIdleLeft(animation1);
        newList.setIdleRight(animation2);
        component.setAnimationList(newList, newList.getIdleLeft());
        assertEquals(newList, component.getAnimationList());
        assertEquals(newList.getIdleLeft(), component.getCurrentAnimation());
    }
}
