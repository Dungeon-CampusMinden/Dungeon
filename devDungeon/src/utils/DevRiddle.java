package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DevRiddle {
  private final String question;
  private final List<String> answers;
  private int correctAnswerIndex;

  public DevRiddle(String question, List<String> answers, int correctAnswerIndex) {
    this.question = question;
    this.answers = new ArrayList<>(answers);
    this.correctAnswerIndex = correctAnswerIndex;
  }

  public void shuffleAnswers() {
    String correctAnswer = this.answers.get(this.correctAnswerIndex);
    Collections.shuffle(this.answers);
    this.correctAnswerIndex = this.answers.indexOf(correctAnswer);
  }

  public String question() {
    return this.question;
  }

  public List<String> answers() {
    return this.answers;
  }

  public int correctAnswerIndex() {
    return this.correctAnswerIndex;
  }
}
