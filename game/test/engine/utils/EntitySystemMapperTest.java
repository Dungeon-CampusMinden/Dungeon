package engine.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import engine.Entity;
import engine.System;
import engine.components.PositionComponent;
import engine.components.VelocityComponent;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Tests for {@link EntitySystemMapper}. */
class EntitySystemMapperTest {

  private final Entity entity = mock(Entity.class);
  private final System system = mock(System.class);

  @Test
  void acceptsAnEntityOnlyOnceAndOnlyWithAllRequiredComponents() {
    EntitySystemMapper mapper =
        new EntitySystemMapper(Set.of(PositionComponent.class, VelocityComponent.class));
    when(entity.isPresent(PositionComponent.class)).thenReturn(true);
    assertFalse(mapper.add(entity));

    when(entity.isPresent(VelocityComponent.class)).thenReturn(true);
    assertTrue(mapper.add(entity));
    assertFalse(mapper.add(entity));
    assertEquals(List.of(entity), mapper.stream().toList());
  }

  @Test
  void notifiesTheSystemWhenAnEntityGainsOrLosesAComponent() {
    EntitySystemMapper mapper = new EntitySystemMapper(Set.of(PositionComponent.class));
    mapper.add(system);

    when(entity.isPresent(PositionComponent.class)).thenReturn(true);
    mapper.update(entity);
    mapper.update(entity);
    verify(system).triggerOnAdd(entity);

    when(entity.isPresent(PositionComponent.class)).thenReturn(false);
    mapper.update(entity);
    verify(system).triggerOnRemove(entity);
    assertEquals(0L, mapper.stream().count());
  }

  @Test
  void registersAndRemovesASystemForExistingEntities() {
    EntitySystemMapper mapper = new EntitySystemMapper();
    mapper.add(entity);

    assertTrue(mapper.add(system));
    assertTrue(mapper.has(system));
    verify(system).triggerOnAdd(entity);

    assertTrue(mapper.remove(system));
    assertFalse(mapper.has(system));
    verify(system).triggerOnRemove(entity);

    assertTrue(mapper.remove(entity));
    verifyNoMoreInteractions(system);
  }

  @Test
  void removingAnUnknownEntityReturnsFalseAndKeepsTheExistingEntity() {
    EntitySystemMapper mapper = new EntitySystemMapper();
    mapper.add(entity);
    Entity unknown = mock(Entity.class);

    assertFalse(mapper.remove(unknown));
    assertEquals(List.of(entity), mapper.stream().toList());
  }

  @Test
  void matchesOnlyTheSameComponentFilter() {
    EntitySystemMapper mapper = new EntitySystemMapper(Set.of(PositionComponent.class));

    assertTrue(mapper.equals(Set.of(PositionComponent.class)));
    assertFalse(mapper.equals(Set.of(VelocityComponent.class)));
  }

  @Test
  void mappersAreEqualWhenTheirComponentFiltersMatch() {
    EntitySystemMapper first = new EntitySystemMapper(Set.of(PositionComponent.class));
    EntitySystemMapper sameFilter = new EntitySystemMapper(Set.of(PositionComponent.class));
    EntitySystemMapper otherFilter = new EntitySystemMapper(Set.of(VelocityComponent.class));
    first.add(system);

    assertEquals(first, sameFilter);
    assertNotEquals(first, otherFilter);
  }
}
