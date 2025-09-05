package core.utils.components.draw.state;

import com.badlogic.gdx.graphics.g2d.Sprite;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.animation.AnimationConfig;
import core.utils.components.draw.animation.SpritesheetConfig;
import core.utils.components.path.IPath;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A finite state machine implementation for handling animations and state transitions.
 *
 * <p>Supports named states, regular transitions triggered by signals, and epsilon transitions that
 * are evaluated each update cycle.
 */
public class StateMachine {

  /** Default name for the idle state. */
  public static final String IDLE_STATE = "idle";

  private State currentState;
  private final List<State> states;
  private final Map<State, List<Transition>> transitions = new HashMap<>();
  private final Map<State, List<EpsilonTransition>> epsilonTransitions = new HashMap<>();

  /**
   * Constructs a StateMachine from a path and a spritesheet configuration.
   *
   * @param path the path to the spritesheet
   * @param config the configuration for animations
   */
  public StateMachine(IPath path, SpritesheetConfig config) {
    this(path, new AnimationConfig(config));
  }

  /**
   * Constructs a StateMachine with a single state based on an animation.
   *
   * @param animation the animation for the default state
   */
  public StateMachine(Animation animation) {
    states = new ArrayList<>();
    states.add(new State(IDLE_STATE, animation));
    setInitialState();
  }

  /**
   * Constructs a StateMachine from a path using the default animation configuration.
   *
   * @param path the path to the spritesheet
   */
  public StateMachine(IPath path) {
    this(path, new AnimationConfig());
  }

  /**
   * Constructs a StateMachine from a path using the default animation configuration.
   *
   * @param path the path to the spritesheet
   * @param defaultStateName name of the state to be used as default
   */
  public StateMachine(IPath path, String defaultStateName) {
    this(path, new AnimationConfig(), defaultStateName);
  }

  /**
   * Constructs a StateMachine from a path and a custom animation configuration.
   *
   * @param path the path to the spritesheet
   * @param config the animation configuration
   */
  public StateMachine(IPath path, AnimationConfig config) {
    states = new ArrayList<>();

    Map<String, Animation> map = Animation.loadAnimationSpritesheet(path);
    if (map != null) {
      map.keySet().forEach(s -> states.add(new State(s, map.get(s))));
    } else {
      states.add(new State(IDLE_STATE, path, config));
    }

    setInitialState();
  }

  /**
   * Constructs a StateMachine from a path and a custom animation configuration.
   *
   * @param path the path to the spritesheet
   * @param config the animation configuration
   * @param defaultStateName name of the state to be used as default
   */
  public StateMachine(IPath path, AnimationConfig config, String defaultStateName) {
    this(path, config);
    setInitialState(defaultStateName);
  }

  /**
   * Constructs a StateMachine with a list of pre-defined states.
   *
   * @param states the list of states
   * @throws IllegalArgumentException if the list of states is empty
   */
  public StateMachine(List<State> states) {
    if (states.size() == 0) throw new IllegalArgumentException("State list can't be empty");
    this.states = states;
    setInitialState();
  }

  /**
   * Constructs a StateMachine with a list of pre-defined states and a specific default state.
   *
   * @param states the list of states
   * @param defaultState the default state
   * @throws IllegalArgumentException if the list of states is empty or the default state is not in
   *     the state list
   */
  public StateMachine(List<State> states, State defaultState) {
    this(states);
    if (defaultState == null) {
      setInitialState();
      return;
    }
    if (!states.contains(defaultState))
      throw new IllegalArgumentException("Default state has to be in the state list");
    this.currentState = defaultState;
  }

  private void setInitialState() {
    setInitialState(null);
  }

  private void setInitialState(String name) {
    String targetName = name == null ? IDLE_STATE : name;
    states.stream()
        .filter(s -> targetName.equals(s.name))
        .findFirst()
        .ifPresentOrElse(
            s -> currentState = s,
            () -> {
              if (IDLE_STATE.equals(targetName)) {
                currentState = states.get(0);
              } else {
                throw new IllegalArgumentException("Default state name not found in states list");
              }
            });
  }

  /**
   * Returns the current active state.
   *
   * @return the current state
   */
  public State getCurrentState() {
    return currentState;
  }

  /**
   * Returns the name of the current active state.
   *
   * @return the current state's name
   */
  public String getCurrentStateName() {
    return currentState.name;
  }

  /**
   * Retrieves a state by name.
   *
   * @param name the name of the state
   * @return the state with the given name, or null if not found
   * @throws IllegalArgumentException if the name is null
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
   * @return the replaced state if it existed, otherwise null
   */
  public State addState(State state) {
    State existing = getState(state.name);
    if (existing != null) removeState(existing);
    addState(state);
    return existing;
  }

  /**
   * Removes a state by its name.
   *
   * @param name the name of the state to remove
   * @return the removed state, or null if no state with that name exists
   */
  public State removeState(String name) {
    State existing = getState(name);
    if (existing != null) removeState(existing);
    return existing;
  }

  /**
   * Removes a state.
   *
   * @param state the state to remove
   * @return true if the state was removed, false otherwise
   */
  public boolean removeState(State state) {
    removeAllTransitions(state);
    return states.remove(state);
  }

  /**
   * Adds a transition between two states triggered by a signal.
   *
   * @param from the name of the source state
   * @param signal the signal triggering the transition
   * @param to the name of the target state
   * @return the replaced transition if it existed, otherwise null
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
   * @param signal the signal triggering the transition
   * @param to the target state
   * @return the replaced transition if it existed, otherwise null
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
   * Adds an epsilon transition between states. Epsilon transitions are evaluated on each update and
   * automatically trigger if their function returns true.
   *
   * @param from the source state
   * @param function the condition function evaluated for the current state
   * @param to the target state
   * @param dataSupplier supplier of the data to pass to the target state
   * @return the replaced epsilon transition if it existed, otherwise null
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
   * Adds an epsilon transition without a data supplier.
   *
   * @param from the source state
   * @param function the condition function
   * @param to the target state
   * @return the replaced epsilon transition if it existed, otherwise null
   */
  public EpsilonTransition addEpsilonTransition(
      State from, Function<State, Boolean> function, State to) {
    return addEpsilonTransition(from, function, to, null);
  }

  /**
   * Removes all transitions to and from a given state.
   *
   * @param state the state whose transitions will be removed
   */
  private void removeAllTransitions(State state) {
    transitions.remove(state);
    epsilonTransitions.remove(state);

    transitions.values().forEach(list -> list.removeIf(t -> t.targetState() == state));

    epsilonTransitions.values().forEach(list -> list.removeIf(t -> t.targetState() == state));
  }

  /**
   * Removes a transition by state name and signal.
   *
   * @param from the source state's name
   * @param signal the signal triggering the transition
   * @return true if a transition was removed, false otherwise
   * @throws IllegalArgumentException if the state doesn't exist
   */
  public boolean removeTransition(String from, String signal) {
    State stFrom = getState(from);
    if (stFrom == null) throw new IllegalArgumentException("State '" + from + "' doesn't exist");
    return removeTransition(stFrom, signal);
  }

  /**
   * Removes a transition from a state.
   *
   * @param from the source state
   * @param signal the signal triggering the transition
   * @return true if a transition was removed, false otherwise
   */
  public boolean removeTransition(State from, String signal) {
    List<Transition> fromTransitions = getTransitionList(from);
    Transition transition =
        fromTransitions.stream().filter(t -> t.signal().equals(signal)).findFirst().orElse(null);
    if (transition != null) return fromTransitions.remove(transition);
    return false;
  }

  private List<EpsilonTransition> getEpsilonTransitionList(State state) {
    if (!epsilonTransitions.containsKey(state)) {
      epsilonTransitions.put(state, new ArrayList<>());
    }
    return epsilonTransitions.get(state);
  }

  private List<Transition> getTransitionList(State state) {
    if (!transitions.containsKey(state)) {
      transitions.put(state, new ArrayList<>());
    }
    return transitions.get(state);
  }

  /**
   * Sends a signal to the current state to trigger a transition.
   *
   * @param signal the signal to send
   */
  public void sendSignal(Signal signal) {
    Transition transition =
        getTransitionList(currentState).stream()
            .filter(t -> t.signal().equals(signal.signal))
            .findFirst()
            .orElse(null);
    if (transition == null) return;
    State newState = transition.targetState();
    changeState(newState, signal.data);
  }

  private void changeState(State newState, Object data) {
    newState.setData(data);
    if (newState != currentState) {
      newState.frameCount(0);
      currentState = newState;
    }
  }

  /** Updates the current state and evaluates epsilon transitions. */
  public void update() {
    currentState.update();
    List<EpsilonTransition> epsilonTransitions = getEpsilonTransitionList(currentState);
    for (int i = 0; i < epsilonTransitions.size(); i++) {
      EpsilonTransition transition = epsilonTransitions.get(i);
      if (transition.function().apply(currentState)) {
        Object data = transition.data() != null ? transition.data().get() : null;
        changeState(transition.targetState(), data);
      }
    }
  }

  /** Resets the state machine to the default state (first state in the list). */
  public void reset() {
    changeState(states.get(0), null);
  }

  /**
   * Retrieves the current state's sprite.
   *
   * @return the {@link Sprite} of the current state
   */
  public Sprite getSprite() {
    return currentState.getSprite();
  }

  /**
   * Retrieves the width of the current state's sprite in world coordinates.
   *
   * @return the width of the current state's sprite in world units
   */
  public float getWidth() {
    return currentState.getWidth();
  }

  /**
   * Retrieves the height of the current state's sprite in world coordinates.
   *
   * @return the height of the current state's sprite in world units
   */
  public float getHeight() {
    return currentState.getHeight();
  }

  /**
   * Retrieves the width of the sprite's texture region.
   *
   * @return the width of the sprite's texture region
   */
  public float getSpriteWidth() {
    return currentState.getSpriteWidth();
  }

  /**
   * Retrieves the height of the sprite's texture region.
   *
   * @return the height of the sprite's texture region
   */
  public float getSpriteHeight() {
    return currentState.getSpriteHeight();
  }

  /**
   * Checks whether the current animation has finished playing.
   *
   * @return true if the animation has finished, false otherwise
   */
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
        sb.append("-- \"")
            .append(t.signal())
            .append("\" -> ")
            .append(t.targetState().name)
            .append("\n");
      }
    }
    return sb.toString();
  }
}
