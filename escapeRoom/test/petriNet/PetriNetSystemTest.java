package petriNet;

import static org.junit.jupiter.api.Assertions.*;

import core.Game;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PetriNetSystem}.
 *
 * <p>Tests basic firing behavior of transitions with simple input and output places.
 */
public class PetriNetSystemTest {

  private PetriNetSystem system;

  @BeforeEach
  void setUp() {
    system = new PetriNetSystem();
  }

  @AfterEach
  void cleanup() {
    Game.removeAllSystems();
    Game.removeAllEntities();
  }

  /**
   * Tests that a transition fires correctly: the input token is consumed and the output token is
   * produced.
   */
  @Test
  void testSingleTransitionFires() {

    // Create places and a transition
    PlaceComponent outputPlace;
    PlaceComponent inputPlace;
    TransitionComponent transition;
    inputPlace = new PlaceComponent();
    outputPlace = new PlaceComponent();
    transition = new TransitionComponent();

    // Connect input and output places to the transition
    system.addInputArc(transition, inputPlace);
    system.addOutputArc(transition, outputPlace);

    // Start with one token in the input place
    inputPlace.produce();
    assertEquals(1, inputPlace.tokenCount(), "Input place should start with 1 token");
    assertEquals(0, outputPlace.tokenCount(), "Output place should start with 0 tokens");

    // Execute the Petri net
    system.execute();

    // Verify that the transition fired
    assertEquals(0, inputPlace.tokenCount(), "Input place should have 0 tokens after firing");
    assertEquals(1, outputPlace.tokenCount(), "Output place should have 1 token after firing");
  }

  /** Tests that a transition does not fire if input place has zero tokens. */
  @Test
  void testTransitionDoesNotFireWithoutTokens() {

    // Create places and a transition
    PlaceComponent outputPlace;
    PlaceComponent inputPlace;
    inputPlace = new PlaceComponent();
    outputPlace = new PlaceComponent();

    // No tokens in input place
    assertEquals(0, inputPlace.tokenCount(), "Input place should start with 0 tokens");

    // Execute the Petri net
    system.execute();

    // Transition should not fire
    assertEquals(0, inputPlace.tokenCount(), "Input place should still have 0 tokens");
    assertEquals(0, outputPlace.tokenCount(), "Output place should still have 0 tokens");
  }

  /**
   * Tests a transition with two input places and one output place. The transition should fire only
   * if both input places have at least one token.
   */
  @Test
  void testTransitionWithTwoInputs() {

    // Create places and a transition
    PlaceComponent inputPlace1 = new PlaceComponent();
    PlaceComponent inputPlace2 = new PlaceComponent();
    PlaceComponent outputPlace = new PlaceComponent();
    TransitionComponent transition = new TransitionComponent();

    // Connect input and output places to the transition
    system.addInputArc(transition, inputPlace1);
    system.addInputArc(transition, inputPlace2);
    system.addOutputArc(transition, outputPlace);

    // Case 1: Only one input has a token, transition should NOT fire
    inputPlace1.produce();
    assertEquals(1, inputPlace1.tokenCount());
    assertEquals(0, inputPlace2.tokenCount());
    assertEquals(0, outputPlace.tokenCount());

    system.execute();

    assertEquals(
        1, inputPlace1.tokenCount(), "Transition should not fire with only one input token");
    assertEquals(
        0, inputPlace2.tokenCount(), "Transition should not fire with only one input token");
    assertEquals(0, outputPlace.tokenCount(), "Output place should remain empty");

    // Case 2: Both inputs have a token, transition SHOULD fire
    inputPlace2.produce();
    assertEquals(1, inputPlace1.tokenCount());
    assertEquals(1, inputPlace2.tokenCount());

    system.execute();

    assertEquals(0, inputPlace1.tokenCount(), "Input tokens should be consumed");
    assertEquals(0, inputPlace2.tokenCount(), "Input tokens should be consumed");
    assertEquals(1, outputPlace.tokenCount(), "Output token should be produced");
  }

  /**
   * Tests a transition with one input place and two output places. The transition should fire if
   * the input place has a token, producing tokens in both output places.
   */
  @Test
  void testTransitionWithTwoOutputs() {

    // Create places and a transition
    PlaceComponent inputPlace = new PlaceComponent();
    PlaceComponent outputPlace1 = new PlaceComponent();
    PlaceComponent outputPlace2 = new PlaceComponent();
    TransitionComponent transition = new TransitionComponent();

    // Connect input and output places to the transition
    system.addInputArc(transition, inputPlace);
    system.addOutputArc(transition, outputPlace1);
    system.addOutputArc(transition, outputPlace2);

    // Start with one token in the input place
    inputPlace.produce();
    assertEquals(1, inputPlace.tokenCount(), "Input place should start with 1 token");
    assertEquals(0, outputPlace1.tokenCount(), "Output place 1 should start with 0 tokens");
    assertEquals(0, outputPlace2.tokenCount(), "Output place 2 should start with 0 tokens");

    // Execute the Petri net
    system.execute();

    // Verify that the transition fired and produced tokens in both outputs
    assertEquals(0, inputPlace.tokenCount(), "Input place should have 0 tokens after firing");
    assertEquals(1, outputPlace1.tokenCount(), "Output place 1 should have 1 token after firing");
    assertEquals(1, outputPlace2.tokenCount(), "Output place 2 should have 1 token after firing");
  }

  /**
   * Tests a transition with two input places and two output places. The transition should only fire
   * if both input places have at least one token, producing one token in each output place.
   */
  @Test
  void testTransitionWithTwoInputsTwoOutputs() {

    // Create places and a transition
    PlaceComponent inputPlace1 = new PlaceComponent();
    PlaceComponent inputPlace2 = new PlaceComponent();
    PlaceComponent outputPlace1 = new PlaceComponent();
    PlaceComponent outputPlace2 = new PlaceComponent();
    TransitionComponent transition = new TransitionComponent();

    // Connect input and output places to the transition
    system.addInputArc(transition, inputPlace1);
    system.addInputArc(transition, inputPlace2);
    system.addOutputArc(transition, outputPlace1);
    system.addOutputArc(transition, outputPlace2);

    // Start with only one token in inputPlace1
    inputPlace1.produce();
    assertEquals(1, inputPlace1.tokenCount(), "Input place 1 should start with 1 token");
    assertEquals(0, inputPlace2.tokenCount(), "Input place 2 should start with 0 tokens");

    // Execute the Petri net - should NOT fire
    system.execute();
    assertEquals(1, inputPlace1.tokenCount(), "Input place 1 should still have 1 token");
    assertEquals(0, inputPlace2.tokenCount(), "Input place 2 should still have 0 tokens");
    assertEquals(0, outputPlace1.tokenCount(), "Output place 1 should have 0 tokens");
    assertEquals(0, outputPlace2.tokenCount(), "Output place 2 should have 0 tokens");

    // Add token to inputPlace2
    inputPlace2.produce();

    // Execute the Petri net - should now fire
    system.execute();
    assertEquals(0, inputPlace1.tokenCount(), "Input place 1 should have 0 tokens after firing");
    assertEquals(0, inputPlace2.tokenCount(), "Input place 2 should have 0 tokens after firing");
    assertEquals(1, outputPlace1.tokenCount(), "Output place 1 should have 1 token after firing");
    assertEquals(1, outputPlace2.tokenCount(), "Output place 2 should have 1 token after firing");
  }

  /**
   * Tests a single place connected as input to two transitions.
   *
   * <p>The shared input place must have enough tokens for both transitions to fire independently.
   */
  @Test
  void testSinglePlaceMultipleTransitionsAsInput() {

    PlaceComponent sharedInput = new PlaceComponent();
    PlaceComponent output1 = new PlaceComponent();
    PlaceComponent output2 = new PlaceComponent();

    TransitionComponent t1 = new TransitionComponent();
    TransitionComponent t2 = new TransitionComponent();

    system.addInputArc(t1, sharedInput);
    system.addInputArc(t2, sharedInput);

    system.addOutputArc(t1, output1);
    system.addOutputArc(t2, output2);

    // Start with 2 tokens so both transitions can fire
    sharedInput.produce();
    sharedInput.produce();
    assertEquals(2, sharedInput.tokenCount(), "Shared input place should start with 2 tokens");

    system.execute();

    // Both transitions fire
    assertEquals(
        0, sharedInput.tokenCount(), "Shared input place should have 0 tokens after both fire");
    assertEquals(1, output1.tokenCount(), "Output1 should have 1 token after firing");
    assertEquals(1, output2.tokenCount(), "Output2 should have 1 token after firing");
  }

  /**
   * Tests a single place as input for two transitions but only enough tokens for one.
   *
   * <p>Only one transition should fire, leaving the other disabled.
   */
  @Test
  void testSinglePlaceMultipleTransitionsInsufficientTokens() {

    PlaceComponent sharedInput = new PlaceComponent();
    PlaceComponent output1 = new PlaceComponent();
    PlaceComponent output2 = new PlaceComponent();

    TransitionComponent t1 = new TransitionComponent();
    TransitionComponent t2 = new TransitionComponent();

    system.addInputArc(t1, sharedInput);
    system.addInputArc(t2, sharedInput);

    system.addOutputArc(t1, output1);
    system.addOutputArc(t2, output2);

    // Only 1 token available
    sharedInput.produce();
    assertEquals(1, sharedInput.tokenCount(), "Shared input place should start with 1 token");

    system.execute();

    // Only one transition fires
    assertEquals(
        0, sharedInput.tokenCount(), "Shared input place should have 0 tokens after one fires");
    assertTrue(
        (output1.tokenCount() == 1 && output2.tokenCount() == 0)
            || (output1.tokenCount() == 0 && output2.tokenCount() == 1),
        "Exactly one output place should have 1 token");
  }

  /**
   * Tests a single transition with multiple output places.
   *
   * <p>The transition should produce a token in each output place when fired.
   */
  @Test
  void testSingleTransitionMultipleOutputs() {

    PlaceComponent input = new PlaceComponent();
    PlaceComponent output1 = new PlaceComponent();
    PlaceComponent output2 = new PlaceComponent();

    TransitionComponent transition = new TransitionComponent();

    system.addInputArc(transition, input);
    system.addOutputArc(transition, output1);
    system.addOutputArc(transition, output2);

    // Start with one token in the input place
    input.produce();
    assertEquals(1, input.tokenCount(), "Input place should start with 1 token");
    assertEquals(0, output1.tokenCount(), "Output1 should start with 0 tokens");
    assertEquals(0, output2.tokenCount(), "Output2 should start with 0 tokens");

    system.execute();

    // Verify that the transition fired and produced tokens in both outputs
    assertEquals(0, input.tokenCount(), "Input place should have 0 tokens after firing");
    assertEquals(1, output1.tokenCount(), "Output1 should have 1 token after firing");
    assertEquals(1, output2.tokenCount(), "Output2 should have 1 token after firing");
  }

  /**
   * Tests multiple transitions producing to the same output place.
   *
   * <p>Each transition should independently produce tokens into the shared output place.
   */
  @Test
  void testMultipleTransitionsSharedOutput() {

    PlaceComponent input1 = new PlaceComponent();
    PlaceComponent input2 = new PlaceComponent();
    PlaceComponent sharedOutput = new PlaceComponent();

    TransitionComponent t1 = new TransitionComponent();
    TransitionComponent t2 = new TransitionComponent();

    system.addInputArc(t1, input1);
    system.addInputArc(t2, input2);

    system.addOutputArc(t1, sharedOutput);
    system.addOutputArc(t2, sharedOutput);

    // Give each input one token
    input1.produce();
    input2.produce();

    assertEquals(1, input1.tokenCount(), "Input1 should start with 1 token");
    assertEquals(1, input2.tokenCount(), "Input2 should start with 1 token");
    assertEquals(0, sharedOutput.tokenCount(), "Shared output should start with 0 tokens");

    system.execute();

    // Both transitions fire, adding tokens to shared output
    assertEquals(0, input1.tokenCount(), "Input1 should have 0 tokens after firing");
    assertEquals(0, input2.tokenCount(), "Input2 should have 0 tokens after firing");
    assertEquals(
        2, sharedOutput.tokenCount(), "Shared output should have 2 tokens after both fire");
  }

  /**
   * Tests a sequence of transitions where the output of the first transition becomes the input of
   * the second. Ensures that token flow works across chained transitions.
   */
  @Test
  void testChainedTransitions() {
    PlaceComponent p1 = new PlaceComponent();
    PlaceComponent p2 = new PlaceComponent();
    PlaceComponent p3 = new PlaceComponent();

    TransitionComponent t1 = new TransitionComponent();
    TransitionComponent t2 = new TransitionComponent();

    system.addInputArc(t1, p1);
    system.addOutputArc(t1, p2);

    system.addInputArc(t2, p2);
    system.addOutputArc(t2, p3);

    p1.produce();
    //// t1 should fire thant t2; execute two times because we cant make assumption about checking
    // order
    system.execute();
    system.execute();
    assertEquals(0, p1.tokenCount());
    assertEquals(0, p2.tokenCount());
    assertEquals(1, p3.tokenCount());
  }

  /**
   * Tests a transition whose output is the same as its input, creating a loop. Verifies that firing
   * returns the token back to the same place.
   */
  @Test
  void testTransitionWithLoop() {
    PlaceComponent p1 = new PlaceComponent();
    TransitionComponent t = new TransitionComponent();

    system.addInputArc(t, p1);
    system.addOutputArc(t, p1);

    p1.produce();
    system.execute();
    assertEquals(1, p1.tokenCount(), "Token should return to the same place after firing");
  }

  /**
   * Tests that when two transitions share the same input place but only one token is available,
   * only one transition fires. Ensures proper token consumption behavior.
   */
  @Test
  void testTransitionDoesNotFireIfInputConsumedByAnotherTransition() {
    PlaceComponent sharedInput = new PlaceComponent();
    PlaceComponent out1 = new PlaceComponent();
    PlaceComponent out2 = new PlaceComponent();

    TransitionComponent t1 = new TransitionComponent();
    TransitionComponent t2 = new TransitionComponent();

    system.addInputArc(t1, sharedInput);
    system.addInputArc(t2, sharedInput);

    system.addOutputArc(t1, out1);
    system.addOutputArc(t2, out2);

    sharedInput.produce(); // only 1 token

    system.execute(); // only one transition should fire
    assertEquals(0, sharedInput.tokenCount());
    assertTrue(
        (out1.tokenCount() == 1 && out2.tokenCount() == 0)
            || (out1.tokenCount() == 0 && out2.tokenCount() == 1),
        "Only one transition should fire due to limited token");
  }

  /**
   * Ensures that a transition with no tokens in its input places does not fire. The output places
   * should remain unchanged.
   */
  @Test
  void testTransitionWithNoInputTokensDoesNotFire() {
    PlaceComponent p1 = new PlaceComponent();
    PlaceComponent p2 = new PlaceComponent();
    TransitionComponent t = new TransitionComponent();

    system.addInputArc(t, p1);
    system.addOutputArc(t, p2);

    // No tokens added to p1
    system.execute();
    assertEquals(0, p1.tokenCount(), "Input place should remain empty");
    assertEquals(0, p2.tokenCount(), "Output place should not receive any tokens");
  }

  /**
   * Verifies that a transition with no output places does not throw an exception and consumes
   * tokens correctly from its input places.
   */
  @Test
  void testTransitionWithNoOutputPlaces() {
    PlaceComponent p1 = new PlaceComponent();
    TransitionComponent t = new TransitionComponent();

    system.addInputArc(t, p1);
    // No output arcs added

    p1.produce();
    system.execute();
    assertEquals(0, p1.tokenCount(), "Input tokens should be consumed even with no outputs");
  }

  /**
   * Ensures that firing the same transition multiple times in a row behaves independently when
   * input tokens are insufficient for subsequent firings.
   */
  @Test
  void testIdempotentTransition() {
    PlaceComponent p1 = new PlaceComponent();
    PlaceComponent p2 = new PlaceComponent();
    TransitionComponent t = new TransitionComponent();

    system.addInputArc(t, p1);
    system.addOutputArc(t, p2);

    p1.produce(); // Only 1 token
    system.execute(); // First firing
    assertEquals(0, p1.tokenCount());
    assertEquals(1, p2.tokenCount());

    system.execute(); // Second firing should not fire
    assertEquals(0, p1.tokenCount(), "No new input tokens, so transition should not fire again");
    assertEquals(1, p2.tokenCount(), "Output should remain unchanged");
  }

  /** Tests that a transition consumes multiple tokens from an input place based on its weight. */
  @Test
  void testTransitionConsumesWeightedTokens() {
    PlaceComponent input = new PlaceComponent();
    input.produce(3);
    PlaceComponent output = new PlaceComponent();

    TransitionComponent transition = new TransitionComponent();

    system.addInputArc(transition, input, 2);
    system.addOutputArc(transition, output);

    system.execute();

    assertEquals(1, input.tokenCount(), "Input should have 1 token left after consuming 2");
    assertEquals(1, output.tokenCount(), "Output should have 1 token produced");
  }

  /**
   * Tests that a transition does not fire if the input place has fewer tokens than required by
   * weight.
   */
  @Test
  void testTransitionNotEnabledWhenInsufficientTokens() {
    PlaceComponent input = new PlaceComponent();
    input.produce();
    PlaceComponent output = new PlaceComponent();

    TransitionComponent transition = new TransitionComponent();

    system.addInputArc(transition, input, 2);
    system.addOutputArc(transition, output);

    PetriNetSystem system = new PetriNetSystem();
    system.execute();

    assertEquals(1, input.tokenCount(), "Input should still have 1 token (not enough to fire)");
    assertEquals(0, output.tokenCount(), "Output should have 0 tokens (transition not enabled)");
  }

  /** Tests that a transition produces multiple tokens in an output place based on its weight. */
  @Test
  void testTransitionProducesWeightedTokens() {
    PlaceComponent input = new PlaceComponent();
    input.produce(2);
    PlaceComponent output = new PlaceComponent();

    TransitionComponent transition = new TransitionComponent();

    system.addInputArc(transition, input, 2);
    system.addOutputArc(transition, output, 3);

    system.execute();

    assertEquals(0, input.tokenCount(), "Input should have 0 tokens left after consuming 2");
    assertEquals(3, output.tokenCount(), "Output should have 3 tokens produced");
  }

  /** Tests that adding an output arc with a weight of 0 throws an exception. */
  @Test
  void testAddOutputArcWithZeroWeightThrows() {
    TransitionComponent transition = new TransitionComponent();
    PlaceComponent place = new PlaceComponent();
    assertThrows(
        IllegalArgumentException.class,
        () -> system.addOutputArc(transition, place, 0),
        "Weight 0 should not be allowed");
  }

  /** Tests that adding an output arc with a negative weight throws an exception. */
  @Test
  void testAddOutputArcWithNegativeWeightThrows() {
    TransitionComponent transition = new TransitionComponent();
    PlaceComponent place = new PlaceComponent();
    assertThrows(
        IllegalArgumentException.class,
        () -> system.addOutputArc(transition, place, -1),
        "Negative weight should not be allowed");
  }
}
