package feature.tasks;

public abstract class Task {
  protected String taskDescription;

  public abstract boolean isCorrect(Answer answer);
}
