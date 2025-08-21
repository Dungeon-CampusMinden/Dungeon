package core.utils.components.draw.state;

import com.badlogic.gdx.graphics.g2d.Sprite;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.animation.AnimationConfig;
import core.utils.components.draw.animation.SpritesheetConfig;
import core.utils.components.path.IPath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class StateMachine {

  private State currentState;
  private final List<State> states;
  private final Map<State, List<Transition>> transitions = new HashMap<>();
  private final Map<State, List<EpsilonTransition>> epsilonTransitions = new HashMap<>();

  public StateMachine(IPath path, SpritesheetConfig config) {
    this(path, new AnimationConfig(config));
  }

  public StateMachine(Animation animation) {
    states = new ArrayList<>();
    states.add(new State("idle", animation));
    currentState = states.get(0);
  }

  public StateMachine(IPath path) {
    this(path, new AnimationConfig());
  }

  public StateMachine(IPath path, AnimationConfig config) {
    states = new ArrayList<>();

    Map<String, Animation> map = Animation.loadAnimationSpritesheet(path);
    if (map != null) {
      // If the path leads to a spritesheet with an animation map, load all states via the map
      map.keySet()
          .forEach(
              s -> {
                states.add(new State(s, map.get(s)));
              });
    } else {
      states.add(new State("idle", path, config));
    }

    currentState = states.get(0);
  }

  public StateMachine(List<State> states) {
    if (states.size() == 0) throw new IllegalArgumentException("State list can't be empty");
    this.states = states;
    currentState = states.get(0);
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

  public State addState(State state) {
    State existing = getState(state.name);
    if (existing != null) removeState(existing);
    addState(state);
    return existing;
  }

  public State removeState(String name) {
    State existing = getState(name);
    if (existing != null) removeState(existing);
    return existing;
  }

  public boolean removeState(State state) {
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

  public EpsilonTransition addEpsilonTransition(
      State from, Function<State, Boolean> function, State to, Supplier<Object> dataSupplier) {
    List<EpsilonTransition> fromTransitions = getEpsilonTransitionList(from);
    EpsilonTransition existing =
        fromTransitions.stream().filter(t -> t.targetState() == to).findFirst().orElse(null);
    if (existing != null) fromTransitions.remove(existing);
    fromTransitions.add(new EpsilonTransition(function, to, dataSupplier));
    return existing;
  }

  public EpsilonTransition addEpsilonTransition(
      State from, Function<State, Boolean> function, State to) {
    return addEpsilonTransition(from, function, to, null);
  }

  private void removeAllTransitions(State state) {
    // Remove the state from the transitions
    transitions.remove(state);
    epsilonTransitions.remove(state);
    // Also remove any transition targeting the state
    transitions
        .values()
        .forEach(
            transitionList -> {
              List<Transition> toRemove =
                  transitionList.stream()
                      .filter(t -> t.targetState() == state)
                      .collect(Collectors.toList());
              toRemove.forEach(transitionList::remove);
            });
    epsilonTransitions
        .values()
        .forEach(
            transitionList -> {
              List<EpsilonTransition> toRemove =
                  transitionList.stream()
                      .filter(t -> t.targetState() == state)
                      .collect(Collectors.toList());
              toRemove.forEach(transitionList::remove);
            });
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

  public void update() {
    currentState.update();
    List<EpsilonTransition> epsilonTransitions = getEpsilonTransitionList(currentState);
    for (int i = 0; i < epsilonTransitions.size(); i++) {
      EpsilonTransition transition = epsilonTransitions.get(i);
      if (transition.function().apply(currentState)) {
        changeState(transition.targetState(), transition.data().get());
      }
    }
  }

  /** Resets the state to the default state (first state in the list) */
  public void reset() {
    changeState(states.get(0), null);
  }

  public Sprite getSprite() {
    return currentState.getSprite();
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
