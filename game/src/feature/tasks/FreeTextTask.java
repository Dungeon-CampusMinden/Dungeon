package feature.tasks;

import java.util.List;

public class FreeTextTask extends Task<String> {

  private final List<String> acceptedAnswer;
  private boolean caseSensitive;

  public FreeTextTask(String taskDescription, List<String> acceptedAnswer, boolean caseSensitive) {
    this.taskDescription = taskDescription;
    this.caseSensitive = caseSensitive;
    if (this.caseSensitive) {
      this.acceptedAnswer = acceptedAnswer;
    } else {
      this.acceptedAnswer = acceptedAnswer.stream().map(String::toLowerCase).toList();
    }
  }

  public FreeTextTask(String taskDescription, List<String> acceptedAnswer) {
    this(taskDescription, acceptedAnswer, false);
  }

  public FreeTextTask(List<String> acceptedAnswer) {
    this("", acceptedAnswer);
  }

  @Override
  public boolean isCorrect(String answer) {
    if (this.caseSensitive) {
      return acceptedAnswer.contains(answer);
    }
    return acceptedAnswer.contains(answer.toLowerCase());
  }

  public List<String> getAcceptedAnswer() {
    return acceptedAnswer;
  }

  public String getTaskDescription() {
    return taskDescription;
  }
}
