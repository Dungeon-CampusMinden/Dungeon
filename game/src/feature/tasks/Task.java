package feature.tasks;

public abstract class Task<T> {
  protected String taskText;

  public abstract boolean isCorrect(T answer);
}
