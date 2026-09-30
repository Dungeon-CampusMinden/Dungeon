package feature.tasks;

import engine.Component;

public class AnswerComponent implements Component {

  private Answer answer;

  public AnswerComponent(Answer answer) {
    this.answer = answer;
  }

  public Answer answer() {
    return answer;
  }
}
