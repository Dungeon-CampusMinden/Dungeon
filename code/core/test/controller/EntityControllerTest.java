package controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import interfaces.IEntity;
import java.util.List;
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
    private IEntity entity1, entity2;
    private EntityController controller;

    @Before
    public void setUp() {
        entity1 = Mockito.mock(IEntity.class);
        entity2 = Mockito.mock(IEntity.class);
        controller = new EntityController();
    }

    @Test
    public void test_update_withEmptyController() {
        EntityController ecSpy = Mockito.spy(new EntityController());
        PowerMockito.doReturn(false).when(ecSpy).removeIf(any());
        PowerMockito.doNothing().when(ecSpy).forEach(any());
        assumeTrue(ecSpy.isEmpty());
        verify(ecSpy).isEmpty();

        ecSpy.update();
        verify(ecSpy).update();
        verify(ecSpy).removeIf(any());
        verify(ecSpy, times(2)).forEach(any());
        Mockito.verifyNoMoreInteractions(ecSpy);
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
        assertTrue(controller.containsAll(List.of(entity1, entity2)));
    }
}
