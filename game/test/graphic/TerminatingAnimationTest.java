package graphic;

import static org.junit.Assert.*;

import java.util.List;
import org.junit.Test;

public class TerminatingAnimationTest {
    @Test
    public void CheckAnimationIteration() {
        List<String> testStrings = List.of("a", "b");
        TerminatingAnimation ta = new TerminatingAnimation(testStrings, 1);
        assertEquals(testStrings.get(0), ta.getNextAnimationTexturePath());
        assertEquals(testStrings.get(1), ta.getNextAnimationTexturePath());
        assertEquals(testStrings.get(1), ta.getNextAnimationTexturePath());
    }

    @Test
    public void CheckAnimationIterationBiggerOne() {
        List<String> testStrings = List.of("a", "b");
        TerminatingAnimation ta = new TerminatingAnimation(testStrings, 2);
        assertEquals(testStrings.get(0), ta.getNextAnimationTexturePath());
        assertEquals(testStrings.get(0), ta.getNextAnimationTexturePath());
        assertEquals(testStrings.get(1), ta.getNextAnimationTexturePath());
        assertEquals(testStrings.get(1), ta.getNextAnimationTexturePath());
        assertEquals(testStrings.get(1), ta.getNextAnimationTexturePath());
    }
}
