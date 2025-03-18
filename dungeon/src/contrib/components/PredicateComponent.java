package contrib.components;

import contrib.utils.ICommand;
import core.Component;
import java.util.function.Supplier;

/**
 * Represents a component that encapsulates a boolean logic operation and an associated action.
 *
 * <p>The {@link PredicateComponent} combines a Boolean-Supplier that evaluates a boolean logic
 * condition with an {@link ICommand} that performs an action. The component allows for executing
 * and undoing the action based on the boolean logic result. The state of the component can be
 * managed separately.
 *
 * @see contrib.utils.PredicateFactory
 * @see contrib.systems.PredicateSystem
 */
public final class PredicateComponent implements Component {

  private final Supplier<Boolean> logic;
  private final ICommand action;
  private boolean state = false;

  /**
   * Constructs a {@link PredicateComponent} with the specified logic and action.
   *
   * @param logic the Boolean-Supplier that provides the boolean logic result
   * @param action the {@link ICommand} to be executed based on the logic result
   */
  public PredicateComponent(Supplier<Boolean> logic, ICommand action) {
    this.logic = logic;
    this.action = action;
  }

  /**
   * Executes the associated action.
   *
   * <p>Calls the {@link ICommand#execute()} method of the action, typically when the logic is true.
   */
  public void execute() {
    action.execute();
  }

  /**
   * Undoes the associated action.
   *
   * <p>Calls the {@link ICommand#undo()} method of the action to revert the action's effects.
   */
  public void undo() {
    action.undo();
  }

  /**
   * Retrieves the result of the logic operation.
   *
   * @return the boolean result of the logic supplied by {@link Supplier#get()}
   */
  public boolean logicResult() {
    return logic.get();
  }

  /**
   * Retrieves the current state of the component.
   *
   * @return the state of the component (true or false)
   */
  public boolean state() {
    return state;
  }

  /**
   * Sets the state of the component.
   *
   * @param state the new state of the component (true or false)
   */
  public void state(boolean state) {
    this.state = state;
  }
}
