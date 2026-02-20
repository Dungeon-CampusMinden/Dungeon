# Subproject Guide: Add a Custom Network Message (Component Sync Example)

This guide shows how a subproject can add a new protobuf-backed message without changing `dungeon` core code.

The example synchronizes a custom `ShieldComponent` from server to client.

## When to use this

Use this approach when:

- Your subproject has data that is not part of core framework messages.
- You want to send a specific update (event or state) over the network.
- You want to keep the extension local to your subproject.

## End-to-end flow

1. Define a domain message (`NetworkMessage`) in your subproject.
2. Define a `.proto` message for the wire format.
3. Implement a `MessageConverter<D, P>`.
4. Register the converter in `ConverterRegistry.global()`.
5. Register message handlers in `MessageDispatcher`.
6. Send and receive the message with `Game.network().send(...)` / `broadcast(...)`.

## 1. Define the domain message

Example (server -> client):

```java
package mymod.network.messages.s2c;

import core.network.messages.NetworkMessage;

/**
 * Synchronizes shield values for one entity.
 */
public record ShieldSyncMessage(
    int entityId,
    int currentShield,
    int maxShield) implements NetworkMessage {}
```

## 2. Define protobuf schema

Create a `.proto` file in your subproject, for example:

```proto
syntax = "proto3";
package mymod.s2c;

option java_multiple_files = true;
option java_package = "mymod.network.proto.s2c";

message ShieldSyncMessage {
  int32 entity_id = 1;
  int32 current_shield = 2;
  int32 max_shield = 3;
}
```

Build once so Java classes are generated.

## 3. Implement converter

Use a wire id outside core range (`1..63` are core). Example uses `128`.

```java
package mymod.network.codec;

import com.google.protobuf.Parser;
import core.network.codec.MessageConverter;
import mymod.network.messages.s2c.ShieldSyncMessage;
import mymod.network.proto.s2c.ShieldSyncMessageOuterClass;

public final class ShieldSyncConverter
    implements MessageConverter<ShieldSyncMessage, ShieldSyncMessageOuterClass.ShieldSyncMessage> {

  private static final byte WIRE_TYPE_ID = (byte) 128;

  @Override
  public ShieldSyncMessageOuterClass.ShieldSyncMessage toProto(ShieldSyncMessage message) {
    return ShieldSyncMessageOuterClass.ShieldSyncMessage.newBuilder()
        .setEntityId(message.entityId())
        .setCurrentShield(message.currentShield())
        .setMaxShield(message.maxShield())
        .build();
  }

  @Override
  public ShieldSyncMessage fromProto(ShieldSyncMessageOuterClass.ShieldSyncMessage proto) {
    return new ShieldSyncMessage(proto.getEntityId(), proto.getCurrentShield(), proto.getMaxShield());
  }

  @Override
  public Class<ShieldSyncMessage> domainType() {
    return ShieldSyncMessage.class;
  }

  @Override
  public Class<ShieldSyncMessageOuterClass.ShieldSyncMessage> protoType() {
    return ShieldSyncMessageOuterClass.ShieldSyncMessage.class;
  }

  @Override
  public Parser<ShieldSyncMessageOuterClass.ShieldSyncMessage> parser() {
    return ShieldSyncMessageOuterClass.ShieldSyncMessage.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
```

## 4. Register converter at startup

Register once during subproject initialization:

```java
ConverterRegistry.global().register(new ShieldSyncConverter());
```

If the same converter is registered twice, `ConverterRegistry` throws.

## 5. Register handlers

Use `MessageDispatcher` on both sides where relevant.

### Client-side apply to component

```java
Game.network().messageDispatcher().registerHandler(
    ShieldSyncMessage.class,
    (session, msg) -> {
      Game.findEntityById(msg.entityId()).ifPresent(entity -> {
        ShieldComponent shield =
            entity.fetch(ShieldComponent.class).orElseGet(() -> {
              ShieldComponent sc = new ShieldComponent(msg.maxShield());
              entity.add(sc);
              return sc;
            });
        shield.current(msg.currentShield());
        shield.max(msg.maxShield());
      });
    });
```

### Server-side send updates

```java
ShieldSyncMessage message = new ShieldSyncMessage(entity.id(), shield.current(), shield.max());
Game.network().broadcast(message, true);
```

## 6. Where this integrates in framework internals

- Serialization uses `NetworkCodec` -> `ConverterRegistry` -> your converter.
- Deserialization uses wire id -> `ConverterRegistry.parse(...)` -> your parser -> your converter.
- Dispatch still goes through `MessageDispatcher`.

No switch/case extension in core is required.

## Testing checklist

- Converter roundtrip test (`toProto` -> `fromProto`).
- Registry registration test (id conflict should fail).
- End-to-end network test (send from server, apply on client).
- Reconnect test if message affects persistent runtime state.

