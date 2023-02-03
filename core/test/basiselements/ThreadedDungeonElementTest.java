package basiselements;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import tools.Point;

/**
 * Tests for the threaded dungeon element
 *
 * @author Maxim Fruendt
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({})
public class ThreadedDungeonElementTest {
    private Point spawnPos;
    private ThreadedFakeDungeonElement fakeElement;
    private ThreadedDungeonElement threadedDungeonElement;

    @Before
    public void setUp() {
        spawnPos = Mockito.mock(Point.class);
        fakeElement = Mockito.mock(ThreadedFakeDungeonElement.class);
        threadedDungeonElement =
                Mockito.mock(
                        ThreadedDungeonElement.class,
                        withSettings().useConstructor().defaultAnswer(CALLS_REAL_METHODS));
        when(threadedDungeonElement.getTexturePath()).thenReturn("texture");
        threadedDungeonElement.setPosition(spawnPos);
    }

    @Test
    public void test_run_withFakeElement() {
        threadedDungeonElement.setFakeElement(fakeElement);

        threadedDungeonElement.run();

        verify(fakeElement).setPosition(spawnPos);
        verify(fakeElement).updateTexture("texture");
    }
}
