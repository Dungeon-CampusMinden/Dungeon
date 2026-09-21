package feature.tasks;

public record Answer(String answer) {

  public static Answer of(int value) {
    return new Answer(Integer.toString(value));
  }

  public static Answer of(String value) {
    return new Answer(value);
  }

  public String toString() {
    return answer;
  }
}
