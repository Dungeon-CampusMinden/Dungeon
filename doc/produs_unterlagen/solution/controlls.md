Controls

```java
protected void processKey(String key) {
    if (key.equals("W")) move(0, 5);
    if (key.equals("S")) move(0, -5);
    if (key.equals("A")) move(-5, 0);
    if (key.equals("D")) move(5, 0);
    if (key.eqials("Q")) shootFireball();
}

private void move(int x, int y) {
    hero.setSpeed(Vector2.of(x, y));
}

private void shootFireball() {
    hero.shootFireball();
}

private void killBerry() {
    Berry berry = hero.getBerryAt(hero.getMousePosition());
    if (berry.isToxic()) {
        berry.tintColor(0xff0000ff);
        berry.changeTexture(Berry.DONUT_TEXTURE);
        berry.destroy();
    }
}
```


