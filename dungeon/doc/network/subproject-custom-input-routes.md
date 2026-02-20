# Subproject Guide: Custom Inputs with `InputCommandRouter`

This guide explains how to add or override input behavior in a subproject using:

- `core.network.input.InputCommandRouter`
- `core.network.messages.c2s.InputMessage`

`InputCommandRouter` runs on the authoritative server side and dispatches one handler per route key.

## Key concepts

- Built-in actions (`MOVE`, `CAST_SKILL`, ...) are mapped to fixed core routes like `core:move`.
- Custom actions use `InputMessage.Action.CUSTOM` and route by `commandId` (example: `mymod:hello_dialog`).
- Registering the same route key again overrides the previous handler.

Route key format must match `<namespace>:<action>`.

## Where default handlers come from

Core default input handlers are registered in:

- `dungeon/src/contrib/entities/HeroController.java` (static `registerDefaultInputHandlers()`).

Your subproject can override them by registering the same route later.

## Example 1: Override movement behavior

This replaces the default `core:move` handling.

```java
import contrib.components.CharacterClassComponent;
import contrib.entities.HeroController;
import core.network.input.InputCommandRouter;
import core.network.messages.c2s.InputMessage;

public final class MyInputOverrides {
  private MyInputOverrides() {}

  public static void register() {
    String moveRoute = InputCommandRouter.routeKey(InputMessage.Action.MOVE); // "core:move"

    InputCommandRouter.register(
        moveRoute,
        false, // do not run while paused
        context -> {
          InputMessage.Move move = context.payloadAs(InputMessage.Move.class);

          // Example: 20% speed boost for this subproject mode.
          var player = context.playerEntity();
          var baseSpeed =
              player.fetch(CharacterClassComponent.class).orElseThrow().characterClass().speed();
          var boostedSpeed = baseSpeed.scale(1.2f);

          HeroController.moveHero(player, move.direction().direction(), boostedSpeed);
        });
  }
}
```

Call `MyInputOverrides.register()` during your subproject startup.

## Example 2: Add a new custom input that opens a Hello World dialog

This adds a new route `mymod:hello_dialog`.

### Server-side route registration

```java
import contrib.hud.dialogs.DialogFactory;
import core.network.input.InputCommandRouter;

public final class MyCustomInputRoutes {
  private MyCustomInputRoutes() {}

  public static void register() {
    InputCommandRouter.register(
        "mymod:hello_dialog",
        true, // allow while paused
        context -> {
          int targetEntityId = context.playerEntity().id();
          DialogFactory.showOkDialog(
              "Hello World from mymod!",
              "Greeting",
              () -> {
                // Optional callback on confirm
              },
              targetEntityId);
        });
  }
}
```

### Client-side send input

```java
import core.Game;
import core.network.messages.c2s.InputMessage;

// e.g. key press handler
Game.network().sendInput(InputMessage.custom("mymod:hello_dialog"));
```

That is enough. No new protobuf message is required for this input route because `InputMessage` already has `CustomAction`.

## Optional: Custom payload

If you want parameters:

```java
byte[] payload = "open:hello".getBytes(java.nio.charset.StandardCharsets.UTF_8);
Game.network().sendInput(InputMessage.custom("mymod:hello_dialog", payload, 1));
```

On server:

```java
InputMessage.Custom custom = context.payloadAs(InputMessage.Custom.class);
String data = new String(custom.payload(), java.nio.charset.StandardCharsets.UTF_8);
```

## Safety checklist

- Register routes once during startup.
- Keep handlers short and server-authoritative.
- Use `ignorePause=true` only when really needed.
- Validate custom payload data before applying gameplay effects.

