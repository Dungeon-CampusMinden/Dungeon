# Lösung für MyPlayerController

# Tastatureingaben verarbeiten

Diese Funktion verarbeitet Tastatureingaben und löst abhängig von der gedrückten Taste Bewegungen, Aktionen oder Interaktionen des Spielers aus.

```java
protected void processKey(String key) {
  if (key.equals("W")) move(0, 5);
  if (key.equals("S")) move(0, -5);
  if (key.equals("A")) move(-5, 0);
  if (key.equals("D")) move(5, 0);
  if (key.equals("Q")) hero.shootSkill();
  if (key.equals("F")) {
    if (Timer.isTimerEnd("F")) {
      hero.nextSkill();
      Timer.startTimer("F", 120);
    }
  }
  if (key.equals("E")) hero.interact(hero.getMousePosition());

}
private void move(int x, int y) {
      if (x != 0) hero.setXSpeed(x);
      if (y != 0) hero.setYSpeed(y);
}
```
