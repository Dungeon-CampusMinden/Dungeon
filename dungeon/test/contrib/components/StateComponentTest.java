package contrib.components;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link StateComponent}. */
class StateComponentTest {

  private StateComponent stateComponent;

  @BeforeEach
  void setUp() {
    stateComponent = new StateComponent();
  }

  /** Tests that a newly added state can be retrieved correctly. */
  @Test
  void testAddAndGetValue() {
    stateComponent.add("score", 10);

    Optional<Object> value = stateComponent.value("score");

    assertTrue(value.isPresent(), "Value should be present after adding");
    assertEquals(10, value.get(), "Stored value should match the added value");
  }

  /** Tests that an existing state can be updated by adding a new value with the same key. */
  @Test
  void testUpdateValue() {
    stateComponent.add("score", 10);
    stateComponent.add("score", 20);

    Optional<Object> value = stateComponent.value("score");

    assertTrue(value.isPresent(), "Value should be present after update");
    assertEquals(20, value.get(), "Value should be updated to new value");
  }

  /** Tests that contains() returns true if the state exists, false otherwise. */
  @Test
  void testContains() {
    assertFalse(stateComponent.contains("score"), "Should not contain 'score' initially");

    stateComponent.add("score", 10);

    assertTrue(stateComponent.contains("score"), "Should contain 'score' after adding");
  }

  /** Tests that remove() deletes the state and it is no longer retrievable or contained. */
  @Test
  void testRemove() {
    stateComponent.add("score", 10);
    assertTrue(stateComponent.contains("score"), "Should contain 'score' before removal");

    stateComponent.remove("score");

    assertFalse(stateComponent.contains("score"), "Should not contain 'score' after removal");
    assertTrue(stateComponent.value("score").isEmpty(), "Value should be empty after removal");
  }

  /** Tests that retrieving a non-existent state returns an empty Optional. */
  @Test
  void testGetNonExistentValue() {
    Optional<Object> value = stateComponent.value("nonexistent");

    assertTrue(value.isEmpty(), "Value should be empty for non-existent key");
  }
}
