package feature.tasks;

import java.util.List;

public class FreeTextTask extends Task<String> {

  private final List<String> acceptedAnswer;
  private boolean caseSensitive;

  public FreeTextTask(String taskText, List<String> acceptedAnswer, boolean caseSensitive) {
    this.taskText = taskText;
    this.caseSensitive = caseSensitive;
    if (this.caseSensitive) {
      this.acceptedAnswer = acceptedAnswer;
    } else {
      this.acceptedAnswer = acceptedAnswer.stream().map(String::toLowerCase).toList();
    }
  }

  public FreeTextTask(String taskText, List<String> acceptedAnswer) {
    this(taskText, acceptedAnswer, false);
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

  public String getTaskText() {
    return taskText;
  }
}
