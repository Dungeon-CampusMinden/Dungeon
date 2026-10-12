package feature.tasks;

import feature.hud.dialogs.ChoiceOption;
import feature.hud.dialogs.DialogFactory;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A question whose answer is correct when it selects exactly the correct option values.
 *
 * <p>Show it with {@link DialogFactory#showMultipleChoiceDialog} and submit the confirmed {@link
 * ChoiceOption#value()}s as a set to the {@link TaskComponent} that owns the task.
 */
public class ChoiceTask extends Task<Set<String>> {

  private final List<ChoiceOption> options;
  private final Set<String> correctValues;
  private final boolean multipleSelection;

  /**
   * Creates a choice task.
   *
   * @param taskDescription the question shown above the options
   * @param options the selectable options; their values must be unique
   * @param correctValues the values of all correct options
   * @param multipleSelection whether players may select more than one option
   * @throws IllegalArgumentException if the options or correct values are inconsistent
   */
  public ChoiceTask(
      String taskDescription,
      List<ChoiceOption> options,
      Set<String> correctValues,
      boolean multipleSelection) {
    this.taskDescription = Objects.requireNonNull(taskDescription, "taskDescription");
    this.options = List.copyOf(options);
    this.correctValues = Set.copyOf(correctValues);
    this.multipleSelection = multipleSelection;

    Set<String> values = new HashSet<>();
    for (ChoiceOption option : this.options) {
      if (!values.add(option.value())) {
        throw new IllegalArgumentException("duplicate option value: " + option.value());
      }
    }
    if (this.correctValues.isEmpty() || !values.containsAll(this.correctValues)) {
      throw new IllegalArgumentException("correct values must be a non-empty subset of options");
    }
    if (!multipleSelection && this.correctValues.size() != 1) {
      throw new IllegalArgumentException("single selection requires exactly one correct option");
    }
  }

  @Override
  public boolean isCorrect(Set<String> answer) {
    return correctValues.equals(answer);
  }

  /**
   * Returns the question shown above the options.
   *
   * @return the task description
   */
  public String taskDescription() {
    return taskDescription;
  }

  /**
   * Returns the selectable options in display order.
   *
   * @return the options
   */
  public List<ChoiceOption> options() {
    return options;
  }

  /**
   * Returns whether players may select more than one option.
   *
   * @return {@code true} for multiple selection
   */
  public boolean multipleSelection() {
    return multipleSelection;
  }
}
