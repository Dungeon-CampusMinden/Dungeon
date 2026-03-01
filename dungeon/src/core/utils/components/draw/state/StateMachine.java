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
 * A finite state machine implementation for handling animations and state transitions.
 *
 * <p>Core API is engine-agnostic: exposes {@link AnimationFrame} instead of libGDX Sprite.
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

  /** Default name for the idle state. */
  public static final String IDLE_STATE = "idle";

  private State currentState;
  private final List<State> states;
  private final Map<State, List<Transition>> transitions = new HashMap<>();
  private final Map<State, List<EpsilonTransition>> epsilonTransitions = new HashMap<>();

  public StateMachine(IPath path, SpritesheetConfig config) {
    this(path, new AnimationConfig(config));
  }

  public StateMachine(Animation animation) {
    states = new ArrayList<>();
    states.add(new State(IDLE_STATE, animation));
    setInitialState();
  }

  public StateMachine(IPath path) {
    this(path, new AnimationConfig());
  }

  public StateMachine(IPath path, String defaultStateName) {
    this(path, new AnimationConfig(), defaultStateName);
  }

  public StateMachine(IPath path, AnimationConfig config) {
    this(path, config, IDLE_STATE);
  }

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

  public StateMachine(List<State> states) {
    this(states, states != null && !states.isEmpty() ? states.get(0) : null);
  }

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

  public List<State> states() {
    return states;
  }

  public State getCurrentState() {
    return currentState;
  }

  public String getCurrentStateName() {
    return currentState.name;
  }

  public State getState(String name) {
    if (name == null) throw new IllegalArgumentException("name can't be empty");
    return states.stream().filter(s -> s.name.equals(name)).findFirst().orElse(null);
  }

  /**
   * Adds a new state to the state machine. If a state with the same name exists, it will be
   * replaced.
   */
  public State addState(State state) {
    if (state == null) return null;
    State existing = getState(state.name);
    if (existing != null) removeState(existing);
    states.add(state);
    return existing;
  }

  public State removeState(String name) {
    State existing = getState(name);
    if (existing != null) removeState(existing);
    return existing;
  }

  public boolean removeState(State state) {
    if (state == null) return false;
    removeAllTransitions(state);
    return states.remove(state);
  }

  public Transition addTransition(String from, String signal, String to) {
    State stFrom = getState(from);
    State stTo = getState(to);
    if (stFrom == null) throw new IllegalArgumentException("State '" + from + "' doesn't exist");
    if (stTo == null) throw new IllegalArgumentException("State '" + to + "' doesn't exist");
    return addTransition(stFrom, signal, stTo);
  }

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
   */
  public Transition addTransition(Transition transition) {
    if (transition == null) return null;
    return addTransition(currentState, transition.signal(), transition.targetState());
  }

  public EpsilonTransition addEpsilonTransition(
    State from, Function<State, Boolean> function, State to, Supplier<Object> dataSupplier) {
    List<EpsilonTransition> fromTransitions = getEpsilonTransitionList(from);
    EpsilonTransition existing =
      fromTransitions.stream().filter(t -> t.targetState() == to).findFirst().orElse(null);
    if (existing != null) fromTransitions.remove(existing);
    fromTransitions.add(new EpsilonTransition(function, to, dataSupplier));
    return existing;
  }

  public EpsilonTransition addEpsilonTransition(State from, Function<State, Boolean> function, State to) {
    return addEpsilonTransition(from, function, to, null);
  }

  private void removeAllTransitions(State state) {
    transitions.remove(state);
    epsilonTransitions.remove(state);

    transitions.values().forEach(list -> list.removeIf(t -> t.targetState() == state));
    epsilonTransitions.values().forEach(list -> list.removeIf(t -> t.targetState() == state));
  }

  public boolean removeTransition(String from, String signal) {
    State stFrom = getState(from);
    if (stFrom == null) throw new IllegalArgumentException("State '" + from + "' doesn't exist");
    return removeTransition(stFrom, signal);
  }

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

  public void sendSignal(Signal signal) {
    if (signal == null) return;

    Transition t = findTransition(currentState, signal.signal);
    if (t == null) return;

    changeState(t.targetState(), signal.data);
  }

  public void update() {
    currentState.update();
    checkEpsilonTransitions();
  }

  public void reset() {
    changeState(states.get(0), null);
  }

  /** Engine-agnostic: return current frame instead of Sprite. */
  public AnimationFrame getFrame() {
    return currentState.getFrame();
  }

  public float getWidth() {
    return currentState.getWidth();
  }

  public float getHeight() {
    return currentState.getHeight();
  }

  public float getSpriteWidth() {
    return currentState.getSpriteWidth();
  }

  public float getSpriteHeight() {
    return currentState.getSpriteHeight();
  }

  public boolean isAnimationFinished() {
    return currentState.isAnimationFinished();
  }

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

  public static void setResetFrame(boolean value) {
    resetFrame = value;
  }

  public void setState(String stateName, Object data) {
    State state = getState(stateName);
    if (state != null) {
      changeState(state, data);
    } else {
      throw new IllegalArgumentException("State '" + stateName + "' doesn't exist");
    }
  }

  private void changeState(State newState, Object data) {
    if (newState == null) return;
    if (newState == currentState) return;

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

  public static void resetFrameOnStateChange(boolean value) {
    resetFrame = value;
  }
}
