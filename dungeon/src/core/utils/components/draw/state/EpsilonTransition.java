package core.utils.components.draw.state;

import java.util.function.Function;
import java.util.function.Supplier;

public record EpsilonTransition(Function<State, Boolean> function, State targetState, Supplier<Object> data) {}
