package graphic;

import static org.junit.Assert.assertEquals;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberMatcher;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest({HUDCamera.class})
public class HUDCameraTest {
    HUDCamera cam;
    Vector3 vector3;

    @Before
    public void setUp() {
        vector3 = Mockito.mock(Vector3.class);
        PowerMockito.suppress(MemberMatcher.defaultConstructorIn(OrthographicCamera.class));
        cam = PowerMockito.spy(new HUDCamera());
        Whitebox.setInternalState(cam, "position", vector3);
    }

    @Test
    public void test_getPosition() {
        assertEquals(vector3, cam.getPosition());
        Mockito.verify(cam).getPosition();
        Mockito.verifyNoMoreInteractions(cam, vector3);
    }
}
