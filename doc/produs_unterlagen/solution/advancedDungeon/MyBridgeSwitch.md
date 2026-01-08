# Lösung für MyBridgeSwitch

# Bridge Switch

```java
public class MyBridgeSwitch extends BridgeSwitch {
  public void activate(Entity emitter) {
    LightBridgeFactory.activate(emitter);
  }

  public void deactivate(Entity emitter) {
    LightBridgeFactory.deactivate(emitter);
  }
}
```


