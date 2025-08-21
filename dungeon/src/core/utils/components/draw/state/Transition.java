package core.utils.components.draw.state;

/**
 * Represents a transition from one state to another in a {@link StateMachine}, triggered by a
 * specific signal.
 *
 * @param signal the signal that triggers this transition
 * @param targetState the state to transition to when the signal is received
 */
public record Transition(String signal, State targetState) {}
