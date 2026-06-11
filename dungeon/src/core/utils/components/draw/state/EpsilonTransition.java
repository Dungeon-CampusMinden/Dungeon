package core.utils.components.draw.state;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a conditional transition between states without an external trigger.
 *
 * <p>An {@code EpsilonTransition} checks a condition on a source {@link State} using a function. If
 * the function returns true, the transition moves to the {@link #targetState} and optionally sets
 * {@link #data}.
 *
 * @param function a function that receives the current state and returns true if the transition
 *     should occur
 * @param targetState the state to transition to when the function returns true
 * @param data a supplier of optional data to attach to the target state upon transition
 */
public record EpsilonTransition(
    Function<State, Boolean> function, State targetState, Supplier<Object> data) {}
