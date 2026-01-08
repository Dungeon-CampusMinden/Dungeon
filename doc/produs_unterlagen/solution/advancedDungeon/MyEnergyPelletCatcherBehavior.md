```java
  public void catchPellet(Entity catcher, Entity pellet) {
    Tools.getToggleComponent(catcher).toggle();
    Game.remove(pellet);
  }
  ```
