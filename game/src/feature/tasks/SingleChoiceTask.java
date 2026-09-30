package feature.tasks;

import java.util.ArrayList;
import java.util.List;

public class SingleChoiceTask extends Task<Answer> {

  private List<Answer> submittedAnswers;
  private Answer correctAnswers;

  public SingleChoiceTask(Answer correctAnswers) {
    this.submittedAnswers = new ArrayList<>();
    this.correctAnswers = correctAnswers;
  }

  @Override
  public boolean isCorrect(Answer answer) {
    if (answer == null) return false;
    submittedAnswers.add(answer);
    return correctAnswers.equals(answer);
  }
}
