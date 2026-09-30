package feature.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MultipleChoiceTask extends Task<Set<Answer>> {

  private List<Set<Answer>> submittedAnswers;
  private Set<Answer> correctAnswers;

  public MultipleChoiceTask(Set<Answer> correctAnswers) {
    this.submittedAnswers = new ArrayList<>();
    this.correctAnswers = correctAnswers;
  }

  @Override
  public boolean isCorrect(Set<Answer> answer) {
    if (answer == null || answer.isEmpty()) return false;
    submittedAnswers.add(answer);
    return correctAnswers.equals(answer);
  }
}
