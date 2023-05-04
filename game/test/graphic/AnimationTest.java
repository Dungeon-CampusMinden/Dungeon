package graphic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

public class AnimationTest {

    @Test(expected = AssertionError.class)
    public void test_constructor_1() {
        new Animation(null, 10);
    }

    @Test(expected = AssertionError.class)
    public void test_constructor_2() {
        new Animation(List.of(), 10);
    }

    @Test(expected = AssertionError.class)
    public void test_constructor_3() {
        new Animation(List.of("someValidTexture"), -10);
    }

    @Test
    public void test_getNextAnimationTexture() {
        // Idea: An animation with 3 textures and a frame time of 10 should return 10 (or 11) times
        // the current texture.

        Animation animation = new Animation(List.of("1", "2", "3"), 10);
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 10; j++) {
                assertEquals(String.valueOf(i % 3 + 1), animation.getNextAnimationTexturePath());
            }
        }
    }

    @Test
    public void CheckAnimation_isFinished_OneMoreFrameAndNotLooping() {
        List<String> testStrings = List.of("a", "b");
        Animation ta = new Animation(testStrings, 1, false);
        assertFalse("has still one Frame for the Animation to go", ta.isFinished());
    }

    @Test
    public void CheckAnimation_isFinished_NoMoreFrameAndNotLooping() {
        List<String> testStrings = List.of("a", "b");
        Animation ta = new Animation(testStrings, 1, false);
        ta.getNextAnimationTexturePath();
        assertTrue("last Frame reached and should not loop", ta.isFinished());
    }

    @Test
    public void CheckAnimation_isFinished_OneMoreFrameAndLooping() {
        List<String> testStrings = List.of("a", "b");
        Animation ta = new Animation(testStrings, 1, true);
        assertFalse("has still one Frame for the Animation to go", ta.isFinished());
    }

    @Test
    public void CheckAnimation_isFinished_NoMoreFrameAndLooping() {
        List<String> testStrings = List.of("a", "b");
        Animation ta = new Animation(testStrings, 1, true);
        ta.getNextAnimationTexturePath();
        assertFalse("last Frame reached and should loop", ta.isFinished());
    }

    @Test
    public void CheckAnimation_getNextAnimationTexturePath_NonLoopingFrameTime1() {
        List<String> testStrings = List.of("a", "b");
        Animation ta = new Animation(testStrings, 1, false);
        assertEquals(testStrings.get(0), ta.getNextAnimationTexturePath());
        assertEquals(testStrings.get(1), ta.getNextAnimationTexturePath());
        assertEquals(testStrings.get(1), ta.getNextAnimationTexturePath());
    }

    @Test
    public void CheckAnimation_getNextAnimationTexturePath_NonLoopingFrameTime2() {
        List<String> testStrings = List.of("a", "b");
        Animation ta = new Animation(testStrings, 2, false);
        assertEquals(testStrings.get(0), ta.getNextAnimationTexturePath());
        assertEquals(testStrings.get(0), ta.getNextAnimationTexturePath());
        assertEquals(testStrings.get(1), ta.getNextAnimationTexturePath());
        assertEquals(testStrings.get(1), ta.getNextAnimationTexturePath());
        assertEquals(testStrings.get(1), ta.getNextAnimationTexturePath());
    }
}
