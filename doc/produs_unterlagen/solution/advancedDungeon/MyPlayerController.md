---
title: "AdvancedDungeon: Lösung Steuerrung und Beeren Level"
---


### Solution for `MyPlayerController`

```java
protected void processKey(String key) {
    if (key.equals("W")) move(0, 5);
    if (key.equals("S")) move(0, -5);
    if (key.equals("A")) move(-5, 0);
    if (key.equals("D")) move(5, 0);
    if (key.equals("E")) hero.interact(hero.getMousePosition());
    if (key.equals("LMB")) hero.shootSkill();
    if (key.equals("Q")) hero.nextSkill();
}

private void move(int x, int y) {
    hero.setSpeed(Vector2.of(x, y));
    // or for diagonal movement
    hero.setXSpeed(x);
    hero.setYSpeed(y);
}
```

