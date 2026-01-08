```java
public class MyEnergyPelletCatcherBehavior extends EnergyPelletCatcherBehavior {

  @Override
  public void catchPellet(Entity catcher, Entity pellet) {
    Tools.getToggleComponent(catcher).toggle();
    Game.remove(pellet);
  }
  }
  ```
