package feature.tasks;

public abstract class Task<T> {
  protected String taskDescription;

  public abstract boolean isCorrect(T answer);
}
