package controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import basiselements.Entity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({EntityController.class})
public class EntityControllerTest {
    private Entity entity1, entity2;
    private EntityController controller;

    @Before
    public void setUp() {
        entity1 = Mockito.mock(Entity.class);
        entity2 = Mockito.mock(Entity.class);
        controller = new EntityController();
    }

    @Test
    public void test_update_withEmptyController() {
        EntityController ecSpy = Mockito.spy(new EntityController());
        PowerMockito.doNothing().when(ecSpy).forEach(any());
        assumeTrue(ecSpy.isEmpty());
        verify(ecSpy).isEmpty();

        ecSpy.update();
        verify(ecSpy).update();
        verify(ecSpy).iterator();
        Mockito.verifyNoMoreInteractions(ecSpy);
        assertFalse(ecSpy.contains(entity1));
        assertFalse(ecSpy.contains(entity2));
        assertTrue(ecSpy.isEmpty());
    }

    @Test
    public void test_update_withRemove() {
        // should be removed
        when(entity1.removable()).thenReturn(true);
        assumeTrue(controller.add(entity1));

        controller.update();
        verify(entity1).removable();
        Mockito.verifyNoMoreInteractions(entity1);
        assertFalse(controller.contains(entity1));
        assertFalse(controller.contains(entity2));
        assertTrue(controller.isEmpty());
    }

    @Test
    public void test_update_withRemoveAndTwoElements() {
        // should be removed
        when(entity1.removable()).thenReturn(true);
        when(entity2.removable()).thenReturn(true);
        assumeTrue(controller.add(entity1));
        assumeTrue(controller.add(entity2));

        controller.update();
        verify(entity1).removable();
        verify(entity2).removable();
        Mockito.verifyNoMoreInteractions(entity1, entity2);
        assertFalse(controller.contains(entity1));
        assertFalse(controller.contains(entity2));
        assertTrue(controller.isEmpty());
    }

    @Test
    public void test_update_withoutRemove() {
        // should not be removed
        when(entity1.removable()).thenReturn(false);
        assumeTrue(controller.add(entity1));

        controller.update();
        verify(entity1).removable();
        verify(entity1).update();
        verify(entity1).draw();
        Mockito.verifyNoMoreInteractions(entity1);
        assertTrue(controller.contains(entity1));
        assertFalse(controller.contains(entity2));
        assertFalse(controller.isEmpty());
    }

    @Test
    public void test_update_withoutRemoveAndTwoElements() {
        // should not be removed
        when(entity1.removable()).thenReturn(false);
        when(entity2.removable()).thenReturn(false);
        assumeTrue(controller.add(entity1));
        assumeTrue(controller.add(entity2));

        controller.update();
        verify(entity1).removable();
        verify(entity1).update();
        verify(entity1).draw();
        verify(entity2).removable();
        verify(entity2).update();
        verify(entity2).draw();
        Mockito.verifyNoMoreInteractions(entity1, entity2);
        assertTrue(controller.contains(entity1));
        assertTrue(controller.contains(entity2));
        assertFalse(controller.isEmpty());
    }

    @Test
    public void test_update_withDifferentLayer() {
        // should not be removed
        when(entity1.removable()).thenReturn(false);
        when(entity2.removable()).thenReturn(false);
        assumeTrue(controller.add(entity1, ControllerLayer.TOP));
        assumeTrue(controller.add(entity2, ControllerLayer.BOTTOM));
        assumeTrue(controller.remove(entity1));
        assumeTrue(controller.add(entity1, ControllerLayer.TOP));

        controller.update();
        verify(entity1).removable();
        verify(entity1).update();
        verify(entity1).draw();
        verify(entity2).removable();
        verify(entity2).update();
        verify(entity2).draw();
        Mockito.verifyNoMoreInteractions(entity1, entity2);
        assertTrue(controller.contains(entity1));
        assertTrue(controller.contains(entity2));
        assertFalse(controller.isEmpty());
    }

    @Test
    public void test_update_withDifferentLayerAndDuplicates() {
        // should not be removed
        when(entity1.removable()).thenReturn(false);
        when(entity2.removable()).thenReturn(false);
        assumeTrue(controller.add(entity1, ControllerLayer.TOP));
        assumeTrue(controller.add(entity2, ControllerLayer.BOTTOM));
        assumeFalse(controller.add(entity1, ControllerLayer.BOTTOM));
        assumeFalse(controller.add(entity2, ControllerLayer.TOP));
        assumeTrue(controller.remove(entity1));
        assumeTrue(controller.add(entity1, ControllerLayer.TOP));

        controller.update();

        when(entity2.removable()).thenReturn(true);
        controller.update();

        verify(entity1, times(2)).removable();
        verify(entity1, times(2)).update();
        verify(entity1, times(2)).draw();
        verify(entity2, times(2)).removable();
        verify(entity2, times(1)).update();
        verify(entity2, times(1)).draw();
        Mockito.verifyNoMoreInteractions(entity1, entity2);

        assertTrue(controller.contains(entity1));
        assertFalse(controller.contains(entity2));
        assertFalse(controller.isEmpty());
    }
}
