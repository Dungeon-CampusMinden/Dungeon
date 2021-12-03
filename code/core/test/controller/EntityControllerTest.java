package controller;

import interfaces.IEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

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
        ec.addEntity(e1);
        ec.addEntity(e2);
        ec.removeAll();
        assertTrue(ec.getEntities().isEmpty());
    }

    @Test
    public void update_FilledListWithOneRemove_True() {
        IEntity e1 = mock(IEntity.class);
        IEntity e2 = mock(IEntity.class);
        ec.addEntity(e1);
        ec.addEntity(e2);
        ec.removeEntity(e2);
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
        ec.addEntity(e1);
        ec.addEntity(e2);
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
