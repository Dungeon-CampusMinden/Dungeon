package controller;

import interfaces.IEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.testng.AssertJUnit.assertTrue;

public class EntityControllerTest {

    private EntityController ec;

    @BeforeEach
    public void init() {
        ec = new EntityController();
    }

    @Test
    public void removeAll_True() {
        IEntity e1 = mock(IEntity.class);
        IEntity e2 = mock(IEntity.class);
        ec.add(e1);
        ec.add(e2);
        ec.removeAll();
        assertTrue(ec.getList().isEmpty());
    }

    @Test
    public void update_FilledListWithOneRemove_True() {
        IEntity e1 = mock(IEntity.class);
        IEntity e2 = mock(IEntity.class);
        ec.add(e1);
        ec.add(e2);
        ec.remove(e2);
        ec.update();
        verify(e1).update();
        verify(e1).draw();
        verify(e2, never()).update();
        verify(e2, never()).draw();
    }

    @Test
    public void update_FilledList_verify() {
        IEntity e1 = mock(IEntity.class);
        IEntity e2 = mock(IEntity.class);
        ec.add(e1);
        ec.add(e2);
        ec.update();
        verify(e1).update();
        verify(e1).draw();
        verify(e2).update();
        verify(e2).draw();
    }

    @Test
    public void update_EmptyList_NoException() {
        ec.update();
    }
}
