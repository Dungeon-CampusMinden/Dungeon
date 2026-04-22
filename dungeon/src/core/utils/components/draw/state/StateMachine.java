package core.utils.components.draw.state;

import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.animation.AnimationConfig;
import core.utils.components.draw.animation.AnimationFrame;
import core.utils.components.draw.animation.SpritesheetConfig;
import core.utils.components.path.IPath;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a finite state machine that can manage states, transitions, and animations tied to those states.
 *
 * <p>Supports signal-based transitions, automatic epsilon transitions, and customizable configurations.
 */
public class StateMachine implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  /**
   * Defines globally whether the frame counter should be reset when the animation state changes.
   *
   * <p>If set to {@code true}, the animation will restart from the first frame whenever the state
   * changes. If set to {@code false}, the animation continues counting frames without resetting.
   */
  private static boolean resetFrame = true;

  /**
   * Default name for the idle state.
   */
  public static final String IDLE_STATE = "idle";

  private State currentState;
  private final List<State> states;
  private final Map<State, List<Transition>> transitions = new HashMap<>();
  private final Map<State, List<EpsilonTransition>> epsilonTransitions = new HashMap<>();

  /**
   * Constructs a StateMachine with an animation loaded from the specified path and spritesheet configuration.
   *
   * @param path the resource path to load the animation from
   * @param config the spritesheet configuration for the animation
   */
  public StateMachine(IPath path, SpritesheetConfig config) {
    this(path, new AnimationConfig(config));
  }

  /**
   * Constructs a StateMachine with a single animation state.
   *
   * @param animation the animation to use for the idle state
   */
  public StateMachine(Animation animation) {
    states = new ArrayList<>();
    states.add(new State(IDLE_STATE, animation));
    setInitialState();
  }

  /**
   * Constructs a StateMachine by loading an animation from the specified path with default configuration.
   *
   * @param path the resource path to load the animation from
   */
  public StateMachine(IPath path) {
    this(path, new AnimationConfig());
  }

  /**
   * Constructs a StateMachine with an animation loaded from the specified path,
   * using a custom default state name.
   *
   * @param path the resource path to load the animation from
   * @param defaultStateName the name of the default state to set as initial
   */
  public StateMachine(IPath path, String defaultStateName) {
    this(path, new AnimationConfig(), defaultStateName);
  }

  /**
   * Constructs a StateMachine with an animation loaded from the specified path and animation configuration.
   *
   * @param path the resource path to load the animation from
   * @param config the animation configuration
   */
  public StateMachine(IPath path, AnimationConfig config) {
    this(path, config, IDLE_STATE);
  }

  /**
   * Constructs a StateMachine with an animation loaded from the specified path,
   * with custom animation configuration and default state name.
   *
   * @param path the resource path to load the animation from
   * @param config the animation configuration
   * @param defaultStateName the name of the default state to set as initial
   */
  public StateMachine(IPath path, AnimationConfig config, String defaultStateName) {
    states = new ArrayList<>();

    Map<String, Animation> map = Animation.loadAnimationSpritesheet(path);
    if (map != null) {
      map.keySet().forEach(s -> states.add(State.fromMap(map, s)));
    } else {
      states.add(new State(IDLE_STATE, new Animation(path, config)));
    }

    State defaultState =
      states.stream().filter(s -> s.name.equals(defaultStateName)).findFirst().orElse(states.get(0));
    currentState = defaultState;
    currentState.onEnter();
  }

  /**
   * Constructs a StateMachine with the specified states and uses the first state as the default.
   *
   * @param states the list of states for this state machine
   * @throws IllegalArgumentException if states is null or empty
   */
  public StateMachine(List<State> states) {
    this(states, states != null && !states.isEmpty() ? states.get(0) : null);
  }

  /**
   * Constructs a StateMachine with the specified states and default state.
   *
   * @param states the list of states for this state machine
   * @param defaultState the state to set as the initial current state; if null, the first state is used
   * @throws IllegalArgumentException if states is null or empty
   */
  public StateMachine(List<State> states, State defaultState) {
    if (states == null || states.isEmpty()) {
      throw new IllegalArgumentException("states can't be null/empty");
    }
    this.states = new ArrayList<>(states);
    this.currentState = defaultState == null ? this.states.get(0) : defaultState;
    this.currentState.onEnter();
  }

  private void setInitialState() {
    currentState = states.get(0);
    currentState.onEnter();
  }

  /**
   * Returns the list of all states in this state machine.
   *
   * @return a list of states
   */
  public List<State> states() {
    return states;
  }

  /**
   * Returns the current state of this state machine.
   *
   * @return the current state
   */
  public State getCurrentState() {
    return currentState;
  }

  /**
   * Returns the name of the current state.
   *
   * @return the name of the current state
   */
  public String getCurrentStateName() {
    return currentState.name;
  }

  /**
   * Retrieves a state by its name.
   *
   * @param name the name of the state to retrieve
   * @return the state with the specified name, or null if not found
   * @throws IllegalArgumentException if name is null
   */
  public State getState(String name) {
    if (name == null) throw new IllegalArgumentException("name can't be empty");
    return states.stream().filter(s -> s.name.equals(name)).findFirst().orElse(null);
  }

  /**
   * Adds a new state to the state machine. If a state with the same name exists, it will be
   * replaced.
   *
   * @param state the state to add
   * @return the previously existing state with the same name, or null if none existed
   */
  public State addState(State state) {
    if (state == null) return null;
    State existing = getState(state.name);
    if (existing != null) removeState(existing);
    states.add(state);
    return existing;
  }

  /**
   * Removes a state by its name.
   *
   * @param name the name of the state to remove
   * @return the removed state, or null if no state with that name existed
   */
  public State removeState(String name) {
    State existing = getState(name);
    if (existing != null) removeState(existing);
    return existing;
  }

  /**
   * Removes the specified state from this state machine.
   *
   * @param state the state to remove
   * @return true if the state was successfully removed, false otherwise
   */
  public boolean removeState(State state) {
    if (state == null) return false;
    removeAllTransitions(state);
    return states.remove(state);
  }

  /**
   * Adds a transition between two states identified by their names.
   *
   * @param from the name of the source state
   * @param signal the signal that triggers this transition
   * @param to the name of the target state
   * @return the previously existing transition for this signal from the source state, or null if none existed
   * @throws IllegalArgumentException if either state doesn't exist
   */
  public Transition addTransition(String from, String signal, String to) {
    State stFrom = getState(from);
    State stTo = getState(to);
    if (stFrom == null) throw new IllegalArgumentException("State '" + from + "' doesn't exist");
    if (stTo == null) throw new IllegalArgumentException("State '" + to + "' doesn't exist");
    return addTransition(stFrom, signal, stTo);
  }

  /**
   * Adds a transition between two states.
   *
   * @param from the source state
   * @param signal the signal that triggers this transition
   * @param to the target state
   * @return the previously existing transition for this signal from the source state, or null if none existed
   */
  public Transition addTransition(State from, String signal, State to) {
    List<Transition> fromTransitions = getTransitionList(from);
    Transition existing =
      fromTransitions.stream().filter(t -> t.signal().equals(signal)).findFirst().orElse(null);
    if (existing != null) fromTransitions.remove(existing);
    fromTransitions.add(new Transition(signal, to));
    return existing;
  }

  /**
   * Compatibility helper: allows DrawComponent to call addTransition(Transition).
   * Uses the current state as source.
   *
   * @param transition the transition to add from the current state
   * @return the previously existing transition, or null if none existed or the transition parameter was null
   */
  public Transition addTransition(Transition transition) {
    if (transition == null) return null;
    return addTransition(currentState, transition.signal(), transition.targetState());
  }

  /**
   * Adds an epsilon transition (automatic transition) with a condition function and optional data supplier.
   *
   * @param from the source state
   * @param function the condition function that determines when the transition should occur
   * @param to the target state
   * @param dataSupplier optional supplier that provides data to pass when the transition occurs
   * @return the previously existing epsilon transition to the target state, or null if none existed
   */
  public EpsilonTransition addEpsilonTransition(
    State from, Function<State, Boolean> function, State to, Supplier<Object> dataSupplier) {
    List<EpsilonTransition> fromTransitions = getEpsilonTransitionList(from);
    EpsilonTransition existing =
      fromTransitions.stream().filter(t -> t.targetState() == to).findFirst().orElse(null);
    if (existing != null) fromTransitions.remove(existing);
    fromTransitions.add(new EpsilonTransition(function, to, dataSupplier));
    return existing;
  }

  /**
   * Adds an epsilon transition (automatic transition) with a condition function.
   *
   * @param from the source state
   * @param function the condition function that determines when the transition should occur
   * @param to the target state
   * @return the previously existing epsilon transition to the target state, or null if none existed
   */
  public EpsilonTransition addEpsilonTransition(State from, Function<State, Boolean> function, State to) {
    return addEpsilonTransition(from, function, to, null);
  }

  private void removeAllTransitions(State state) {
    transitions.remove(state);
    epsilonTransitions.remove(state);

    transitions.values().forEach(list -> list.removeIf(t -> t.targetState() == state));
    epsilonTransitions.values().forEach(list -> list.removeIf(t -> t.targetState() == state));
  }

  /**
   * Removes a transition from a state by signal name.
   *
   * @param from the name of the source state
   * @param signal the signal identifying the transition to remove
   * @return true if the transition was successfully removed, false otherwise
   * @throws IllegalArgumentException if the source state doesn't exist
   */
  public boolean removeTransition(String from, String signal) {
    State stFrom = getState(from);
    if (stFrom == null) throw new IllegalArgumentException("State '" + from + "' doesn't exist");
    return removeTransition(stFrom, signal);
  }

  /**
   * Removes a transition from a state by signal.
   *
   * @param from the source state
   * @param signal the signal identifying the transition to remove
   * @return true if the transition was successfully removed, false otherwise
   */
  public boolean removeTransition(State from, String signal) {
    List<Transition> fromTransitions = getTransitionList(from);
    Transition transition =
      fromTransitions.stream().filter(t -> t.signal().equals(signal)).findFirst().orElse(null);
    if (transition != null) return fromTransitions.remove(transition);
    return false;
  }

  private List<EpsilonTransition> getEpsilonTransitionList(State state) {
    epsilonTransitions.computeIfAbsent(state, k -> new ArrayList<>());
    return epsilonTransitions.get(state);
  }

  private List<Transition> getTransitionList(State state) {
    transitions.computeIfAbsent(state, k -> new ArrayList<>());
    return transitions.get(state);
  }

  /**
   * Sends a signal to the state machine, triggering any matching transition from the current state.
   *
   * @param signal the signal to send (can be null, which is ignored)
   */
  public void sendSignal(Signal signal) {
    if (signal == null) return;

    Transition t = findTransition(currentState, signal.signal);
    if (t == null) return;

    changeState(t.targetState(), signal.data);
  }

  /**
   * Updates the current state and checks for automatic (epsilon) transitions.
   */
  public void update() {
    currentState.update();
    checkEpsilonTransitions();
  }

  /**
   * Resets the state machine to the first state.
   */
  public void reset() {
    changeState(states.get(0), null);
  }

  /**
   * Returns the current frame of the animation in the current state.
   * Engine-agnostic: returns current frame instead of a platform-specific Sprite.
   *
   * @return the current animation frame
   */
  public AnimationFrame getFrame() {
    return currentState.getFrame();
  }

  /**
   * Returns the width of the current animation frame.
   *
   * @return the width of the current frame
   */
  public float getWidth() {
    return currentState.getWidth();
  }

  /**
   * Returns the height of the current animation frame.
   *
   * @return the height of the current frame
   */
  public float getHeight() {
    return currentState.getHeight();
  }

  /**
   * Returns the width of the spritesheet for the current state's animation.
   *
   * @return the spritesheet width
   */
  public float getSpriteWidth() {
    return currentState.getSpriteWidth();
  }

  /**
   * Returns the height of the spritesheet for the current state's animation.
   *
   * @return the spritesheet height
   */
  public float getSpriteHeight() {
    return currentState.getSpriteHeight();
  }

  /**
   * Checks if the current state's animation has finished playing.
   *
   * @return true if the animation is finished, false otherwise
   */
  public boolean isAnimationFinished() {
    return currentState.isAnimationFinished();
  }

  /**
   * Returns a string representation of this state machine with all states and transitions.
   *
   * @return a detailed string describing the state machine structure
   */
  @Override
  public String toString() {
    StringBuilder sb =
      new StringBuilder("StateMachine{" + "currentState=" + currentState.name + "}\n");
    sb.append("States:\n");
    for (State state : states) {
      sb.append("- ").append(state.name).append(" (object: ").append(state).append(")\n");
      for (Transition t : getTransitionList(state)) {
        sb.append("-- \"").append(t.signal()).append("\" -> ").append(t.targetState().name).append("\n");
      }
    }
    return sb.toString();
  }

  /**
   * Sets the global configuration for whether frame counters should be reset when animation states change.
   *
   * @param value true to reset frame counters on state change, false to continue from current frame count
   */
  public static void setResetFrame(boolean value) {
    resetFrame = value;
  }

  /**
   * Changes the current state of the state machine to the named state.
   *
   * @param stateName the name of the state to transition to
   * @param data optional data to pass to the new state
   * @throws IllegalArgumentException if no state with the specified name exists
   */
  public void setState(String stateName, Object data) {
    State state = getState(stateName);
    if (state != null) {
      changeState(state, data);
    } else {
      throw new IllegalArgumentException("State '" + stateName + "' doesn't exist");
    }
  }

  /**
   * Sets the global configuration for whether frame counters should be reset when animation states change.
   * This is an alias for {@link #setResetFrame(boolean)}.
   *
   * @param value true to reset frame counters on state change, false to continue from current frame count
   */
  public static void resetFrameOnStateChange(boolean value) {
    resetFrame = value;
  }

  private void changeState(State newState, Object data) {
    if (newState == null) return;
    if (newState == currentState) {
      currentState.setData(data);
      return;
    }

    currentState.onExit();

    currentState = newState;
    currentState.setData(data);
    currentState.onEnter();

    if (resetFrame) {
      currentState.frameCount(0);
    }
  }

  private void checkEpsilonTransitions() {
    List<EpsilonTransition> eps = epsilonTransitions.getOrDefault(currentState, List.of());
    for (EpsilonTransition et : eps) {
      if (et == null) continue;

      Function<State, Boolean> fn = et.function();
      if (fn != null && Boolean.TRUE.equals(fn.apply(currentState))) {
        Supplier<Object> dataSupplier = et.data();
        Object data = dataSupplier != null ? dataSupplier.get() : null;
        changeState(et.targetState(), data);
        return;
      }
    }
  }

  private Transition findTransition(State from, String signal) {
    for (Transition t : transitions.getOrDefault(from, List.of())) {
      if (t != null && Objects.equals(t.signal(), signal)) return t;
    }
    return null;
  }
}
