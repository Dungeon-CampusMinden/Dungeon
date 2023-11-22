package core.utils.components.draw;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AnimationTest {

    /** Creating an Animation without a Collection of frames should not be allowed */
    @Test(expected = AssertionError.class)
    public void test_constructor_1() {
        Animation.fromCollection(null);
    }

    /** Creating an Animation without any frames in the Collection should not be allowed */
    @Test(expected = AssertionError.class)
    public void test_constructor_2() {
        Animation.fromCollection(List.of());
    }

    @Test
    public void test_getNextAnimationTexture() {
        // Idea: An animation with 3 textures and a frame time of 10 should return 10 (or 11) times
        // the current texture.

        Animation animation =
                Animation.fromCollection(
                        Stream.of("1", "2", "3").map(SimpleIPath::new).collect(Collectors.toList()),
                        10,
                        1);
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 10; j++) {
                assertEquals(String.valueOf(i % 3 + 1), animation.nextAnimationTexturePath());
            }
        }
    }

    @Test
    public void CheckAnimation_isFinished_OneMoreFrameAndNotLooping() {
        List<IPath> testStrings =
                Stream.of("a", "b").map(SimpleIPath::new).collect(Collectors.toList());
        Animation ta = Animation.fromCollection(testStrings, 1, false, 1);
        assertFalse("has still one Frame for the Animation to go", ta.isFinished());
    }

    @Test
    public void CheckAnimation_isFinished_NoMoreFrameAndNotLooping() {
        List<IPath> testStrings =
                Stream.of("a", "b").map(SimpleIPath::new).collect(Collectors.toList());
        Animation ta = Animation.fromCollection(testStrings, 1, false, 1);
        ta.nextAnimationTexturePath();
        assertTrue("last Frame reached and should not loop", ta.isFinished());
    }

    @Test
    public void CheckAnimation_isFinished_OneMoreFrameAndLooping() {
        List<IPath> testStrings =
                Stream.of("a", "b").map(SimpleIPath::new).collect(Collectors.toList());
        Animation ta = Animation.fromCollection(testStrings, 1, true, 1);
        assertFalse("has still one Frame for the Animation to go", ta.isFinished());
    }

    @Test
    public void CheckAnimation_isFinished_NoMoreFrameAndLooping() {
        List<IPath> testStrings =
                Stream.of("a", "b").map(SimpleIPath::new).collect(Collectors.toList());
        Animation ta = Animation.fromCollection(testStrings, 1, true, 1);
        ta.nextAnimationTexturePath();
        assertFalse("last Frame reached and should loop", ta.isFinished());
    }

    @Test
    public void CheckAnimation_getNextAnimationTexturePath_NonLoopingFrameTime1() {
        List<IPath> testStrings =
                Stream.of("a", "b").map(SimpleIPath::new).collect(Collectors.toList());
        Animation ta = Animation.fromCollection(testStrings, 1, false, 1);
        assertEquals(testStrings.get(0).pathString(), ta.nextAnimationTexturePath());
        assertEquals(testStrings.get(1).pathString(), ta.nextAnimationTexturePath());
        assertEquals(testStrings.get(1).pathString(), ta.nextAnimationTexturePath());
    }

    @Test
    public void CheckAnimation_getNextAnimationTexturePath_NonLoopingFrameTime2() {
        List<IPath> testStrings =
                Stream.of("a", "b").map(SimpleIPath::new).collect(Collectors.toList());
        Animation ta = Animation.fromCollection(testStrings, 2, false, 1);
        assertEquals(testStrings.get(0).pathString(), ta.nextAnimationTexturePath());
        assertEquals(testStrings.get(0).pathString(), ta.nextAnimationTexturePath());
        assertEquals(testStrings.get(1).pathString(), ta.nextAnimationTexturePath());
        assertEquals(testStrings.get(1).pathString(), ta.nextAnimationTexturePath());
        assertEquals(testStrings.get(1).pathString(), ta.nextAnimationTexturePath());
    }
}
