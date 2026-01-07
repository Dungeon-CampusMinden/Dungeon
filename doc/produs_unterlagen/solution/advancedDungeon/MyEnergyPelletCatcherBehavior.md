```java
public class MyEnergyPelletCatcherBehavior extends EnergyPelletCatcherBehavior {

  @Override
  public void catchPellet(Entity catcher, Entity pellet) {
    catcher.fetch(ToggleableComponent.class).ifPresent(ToggleableComponent::toggle);
    Game.remove(pellet);
  }
  }
  ```
