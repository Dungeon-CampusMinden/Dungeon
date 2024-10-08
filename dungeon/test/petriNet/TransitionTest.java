package petriNet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import graph.petrinet.Place;
import graph.petrinet.Transition;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** WTF? . */
public class TransitionTest {

  /** Test case for firing a transition with one dependent place. */
  @Test
  public void fire_oneDepPlace() {
    Place dependency = new Place();
    Place addToken = new Place();
    Transition transition = new Transition(Set.of(dependency), Set.of(addToken));
    dependency.placeToken();
    assertEquals(1, addToken.tokenCount());
    assertEquals(0, dependency.tokenCount());
  }

  /** A test to verify firing a transition with multiple dependencies in a Petri net. */
  @Test
  public void fire_mulDepPlace() {
    Place dependencyA = new Place();
    Place dependencyB = new Place();
    Place addToken = new Place();
    Transition transition = new Transition(Set.of(dependencyA, dependencyB), Set.of(addToken));
    dependencyA.placeToken();
    assertEquals(0, addToken.tokenCount());
    dependencyB.placeToken();
    assertEquals(1, addToken.tokenCount());
    assertEquals(0, dependencyA.tokenCount());
    assertEquals(0, dependencyB.tokenCount());
  }

  /** WTF? . */
  @Test
  public void fire_mulTimes() {
    Place dependency = new Place();
    Place addToken = new Place();
    Transition transition = new Transition(Set.of(dependency), Set.of(addToken));
    dependency.placeToken();
    assertEquals(1, addToken.tokenCount());
    dependency.placeToken();
    assertEquals(2, addToken.tokenCount());
    assertEquals(0, dependency.tokenCount());
  }

  /** WTF? . */
  @Test
  public void fire_mulToAdd() {
    Place dependency = new Place();
    Place addTokenA = new Place();
    Place addTokenB = new Place();
    Transition transition = new Transition(Set.of(dependency), Set.of(addTokenA, addTokenB));
    dependency.placeToken();
    assertEquals(1, addTokenA.tokenCount());
    assertEquals(1, addTokenB.tokenCount());
    assertEquals(0, dependency.tokenCount());
  }
}
