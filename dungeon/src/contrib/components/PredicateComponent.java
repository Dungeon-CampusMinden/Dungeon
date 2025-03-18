package contrib.components;

import contrib.utils.ICommand;
import contrib.utils.Predicate;
import core.Component;

import java.util.function.Supplier;

public final class PredicateComponent implements Component {
  private final Supplier<Boolean> logic;
  private final ICommand action;
  private boolean state = false;

  public PredicateComponent(Supplier<Boolean> logic, ICommand action) {
    this.logic = logic;
    this.action = action;
  }

  public void execute() {
    action.execute();
  }

  public void undo() {
    action.undo();
  }

  public boolean logicResult() {
    return logic.get();
  }

  public boolean state() {
    return state;
  }

  public void state(boolean state) {
    this.state = state;
  }
}
