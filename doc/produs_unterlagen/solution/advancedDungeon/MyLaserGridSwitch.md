```java
  @Override
  public void activate(Entity[] grid) {
    for (Entity laser : grid) Tools.getLaserGridComponent(laser).activate();
  }

  @Override
  public void deactivate(Entity[] grid) {
    for (Entity laser : grid) Tools.getLaserGridComponent(laser).deactivate();
  }

```
