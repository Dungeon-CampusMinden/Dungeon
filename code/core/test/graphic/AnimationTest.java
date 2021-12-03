package graphic;

import com.badlogic.gdx.graphics.Texture;
import jdk.jfr.Description;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AnimationTest {

    private List mockedList = mock(List.class);

    @Test
    @Description("Create Animation with empty list")
    public void constructor_EmptyTextureList_ThrowsException() {
        when(mockedList.isEmpty()).thenReturn(true);
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            new Animation(mockedList, 2);
                        });
        assertEquals("An animation must have at least 1 frame", exception.getMessage());
    }

    @Test
    @Description("Create Animation with 0 frameTime")
    public void constructor_0FrameTime_NoException() {
        when(mockedList.isEmpty()).thenReturn(false);
        when(mockedList.size()).thenReturn(2);
        new Animation(mockedList, 0);
    }

    @Test
    @Description("Create Animation with negative frameTime")
    public void constructor_NegativeFrameTime_ThrowsException() {
        when(mockedList.isEmpty()).thenReturn(false);
        when(mockedList.size()).thenReturn(2);
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            new Animation(mockedList, -1);
                        });
        assertEquals("frameTime cant be lower than 0", exception.getMessage());
    }

    @Test
    @Description("Rotate Animation with frameTime=1")
    public void getNextAnimation_FrameTime1_True() {
        Texture mockedTexture1 = mock(Texture.class);
        Texture mockedTexture2 = mock(Texture.class);
        when(mockedList.isEmpty()).thenReturn(false);
        when(mockedList.size()).thenReturn(2);
        when(mockedList.get(0)).thenReturn(mockedTexture1);
        when(mockedList.get(1)).thenReturn(mockedTexture2);
        Animation a = new Animation(mockedList, 1);
        assertEquals(mockedTexture1, a.getNextAnimationTexture());
        assertEquals(mockedTexture1, a.getNextAnimationTexture());
        assertEquals(mockedTexture2, a.getNextAnimationTexture());
        assertEquals(mockedTexture2, a.getNextAnimationTexture());
        assertEquals(mockedTexture1, a.getNextAnimationTexture());
        assertEquals(mockedTexture1, a.getNextAnimationTexture());
        assertEquals(mockedTexture2, a.getNextAnimationTexture());
        assertEquals(mockedTexture2, a.getNextAnimationTexture());
    }

    @Test
    @Description("Rotate Animation with frameTime=0")
    public void getNextAnimation_FrameTime0_True() {
        Texture mockedTexture1 = mock(Texture.class);
        Texture mockedTexture2 = mock(Texture.class);
        when(mockedList.isEmpty()).thenReturn(false);
        when(mockedList.size()).thenReturn(2);
        when(mockedList.get(0)).thenReturn(mockedTexture1);
        when(mockedList.get(1)).thenReturn(mockedTexture2);
        Animation a = new Animation(mockedList, 0);
        assertEquals(mockedTexture1, a.getNextAnimationTexture());
        assertEquals(mockedTexture2, a.getNextAnimationTexture());
        assertEquals(mockedTexture1, a.getNextAnimationTexture());
        assertEquals(mockedTexture2, a.getNextAnimationTexture());
    }

    @Test
    @Description("Rotate Animation with frameTime=2")
    public void getNextAnimation_FrameTime2_True() {
        Texture mockedTexture1 = mock(Texture.class);
        Texture mockedTexture2 = mock(Texture.class);
        when(mockedList.isEmpty()).thenReturn(false);
        when(mockedList.size()).thenReturn(2);
        when(mockedList.get(0)).thenReturn(mockedTexture1);
        when(mockedList.get(1)).thenReturn(mockedTexture2);
        Animation a = new Animation(mockedList, 2);
        assertEquals(mockedTexture1, a.getNextAnimationTexture());
        assertEquals(mockedTexture1, a.getNextAnimationTexture());
        assertEquals(mockedTexture1, a.getNextAnimationTexture());
        assertEquals(mockedTexture2, a.getNextAnimationTexture());
        assertEquals(mockedTexture2, a.getNextAnimationTexture());
        assertEquals(mockedTexture2, a.getNextAnimationTexture());
        assertEquals(mockedTexture1, a.getNextAnimationTexture());
        assertEquals(mockedTexture1, a.getNextAnimationTexture());
        assertEquals(mockedTexture1, a.getNextAnimationTexture());
        assertEquals(mockedTexture2, a.getNextAnimationTexture());
        assertEquals(mockedTexture2, a.getNextAnimationTexture());
        assertEquals(mockedTexture2, a.getNextAnimationTexture());
    }
}
