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
    if (key.equals("E")) hero.interact();
    if (key.equals("I")) hero.openInventory();
    if (key.equals("LMB")) shootFireball();
    if (key.equals("RMB")) checkBerry();
    if (key.equals("F")) destroyItem();
}

private void move(int x, int y) {
    hero.setSpeed(Vector2.of(x, y));
    // or for diagonal movement
    hero.setXSpeed(x);
    hero.setYSpeed(y);
}

private void shootFireball() {
    hero.shootFireball(hero.getMousePosition());
}

// multiple methods for the berry level
private void checkBerry() {
    Berry berry = hero.getBerryAt(hero.getMousePosition());
    // null check or game gives an error when mouse is not over a berry
    if (berry != null) {
        // Mark toxic berries with red color and safe berries with green color
        if (berry.isToxic()) berry.tintColor(0xFF0000FF);
        else berry.tintColor(0x00FF00FF);
        // Destroy the berry if toxic
        if (berry.isToxic()) berry.destroy();
        // Safe berries are now donuts
        if (!berry.isToxic()) berry.changeTexture(Berry.DONUT_TEXTURE);
    }
}
// alternative method to destroy a marked berry
private void destroyItem() {
    hero.destroyItemAt(hero.getMousePosition());
}
```

