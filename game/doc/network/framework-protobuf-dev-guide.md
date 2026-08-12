# Framework Dev Guide: Protobuf and Network Message Architecture

This document is for developers working on the `dungeon` framework itself.

It explains:

- how protobuf/network conversion is structured today,
- where protobuf-related files are,
- what to change when core data structures evolve (for example `PositionComponent`),
- and how to add a new framework-level synced element.

## 1. Runtime architecture (current)

### Serialization path

`NetworkCodec.serialize(NetworkMessage)` -> `ConverterRegistry.global().toProto(...)` -> `ConverterRegistry.global().typeId(...)` -> wire bytes.

### Deserialization path

wire bytes -> `ConverterRegistry.global().parse(typeId, payload)` -> `ConverterRegistry.global().fromProto(...)` -> `NetworkMessage`.

### Important consequence

There is no central switch anymore. Dispatch is fully modular through `MessageConverter` implementations registered in `CoreConverters`.

## 2. Where to find what (protobuf/network)

### Core codec and registry

- `game/src/engine/network/codec/NetworkCodec.java`
- `game/src/engine/network/codec/ConverterRegistry.java`
- `game/src/engine/network/codec/MessageConverter.java`
- `game/src/engine/network/codec/CoreConverters.java`
- `game/src/engine/network/codec/CommonProtoConverters.java`

### Built-in converters

- `game/src/engine/network/codec/converters/c2s/*`
- `game/src/engine/network/codec/converters/s2c/*`

### Domain message types (Java)

- `game/src/engine/network/messages/c2s/*`
- `game/src/engine/network/messages/s2c/*`

### Protobuf schemas (`.proto`)

- `game/proto/common/common_types.proto`
- `game/proto/c2s/*.proto`
- `game/proto/s2c/*.proto`

### Generated Java protobuf classes (do not edit manually)

- `game/build/generated/source/proto/main/java/core/network/proto/**`

### Message handlers / runtime integration

- Client handlers: `game/src/engine/game/GameLoop.java` (`setupMessageHandlers`)
- Server handlers: `game/src/engine/network/server/ServerTransport.java` (`setupDispatchers`)
- Snapshot mapping: `game/src/engine/network/DefaultSnapshotTranslator.java`
- Custom input routing (server-authoritative): `game/src/engine/network/input/InputCommandRouter.java`

## 3. Protobuf change rules

When changing `.proto` files:

- Do not renumber existing field tags.
- Prefer adding new fields with new tag numbers.
- Keep old fields readable during migration when possible.
- Regenerate code via Gradle (`:game:generateProto` or normal compile tasks).

## 4. If `PositionComponent` changes: exact checklist

Example scenario: `PositionComponent` changes shape (field added/replaced).

### Must-update locations

1. Domain component:
- `game/src/engine/components/PositionComponent.java`

2. Shared protobuf schema:
- `game/proto/common/common_types.proto` (`message PositionInfo`)

3. Shared converter helper:
- `game/src/engine/network/codec/CommonProtoConverters.java`
  - `toProto(PositionComponent)`
  - `fromProto(PositionInfo)`

4. Converters that reference `PositionInfo`:
- `game/src/engine/network/codec/converters/s2c/EntitySpawnEventConverter.java` (delegates to `CommonProtoConverters`; usually covered by step 3, but verify if the domain message signature changes)
- `game/src/engine/network/codec/converters/s2c/EntityStateConverter.java` (builds `PositionInfo` directly inline -> always needs a manual update)

5. Snapshot translator mapping (server build + client apply):
- `game/src/engine/network/DefaultSnapshotTranslator.java`

6. Tests:
- `game/test/engine/network/codec/CommonProtoConvertersTest.java`
- `game/test/engine/network/codec/S2CConverterTest.java`
- `game/test/engine/network/codec/NetworkCodecTest.java`

If the change affects input payloads (for example move/interact target structures), also check:

- `game/proto/c2s/input.proto`
- `game/src/engine/network/codec/converters/c2s/InputMessageConverter.java`

## 5. Framework example: add a new synced element

Example: Add stamina fields to framework snapshots (`EntityState`).

### Step 1: Extend protobuf schema

In `game/proto/s2c/entity.proto`, add new optional fields to `EntityState`:

```proto
optional float current_stamina = 12;
optional float max_stamina = 13;
```

### Step 2: Extend domain message

Update `game/src/engine/network/messages/s2c/EntityState.java` builder/getters with stamina optionals.

### Step 3: Extend converter

Update `game/src/engine/network/codec/converters/s2c/EntityStateConverter.java`:

- write stamina in `toProto(...)`,
- read stamina in `fromProto(...)`.

### Step 4: Connect to ECS snapshot pipeline

Update `game/src/engine/network/DefaultSnapshotTranslator.java`:

- server side `translateToSnapshot(...)`: read from your stamina component and set builder fields,
- client side `applySnapshot(...)`: apply to existing component or create one if needed.

### Step 5: Tests

- Add/adjust unit tests for converter roundtrip and snapshot application.
- Run `:game:test`.

## 6. Add a new framework message type (not just a new field)

If the framework needs a brand new message class:

1. Add domain message (`engine.network.messages.c2s` or `s2c`).
2. Add schema in `game/proto/...`.
3. Add converter class in `game/src/engine/network/codec/converters/...`.
4. Assign a unique core wire id in that converter.
5. Register converter in `game/src/engine/network/codec/CoreConverters.java`.
6. Register runtime handler:
- server: `ServerTransport.setupDispatchers()`
- client: `GameLoop.setupMessageHandlers()`
7. Add tests (converter + network roundtrip + handler behavior).

## 7. Build/test commands for protobuf work

From repo root:

```powershell
./gradlew :game:generateProto
./gradlew :game:compileJava
./gradlew :game:test
```

For formatting and style:

```powershell
./gradlew spotlessApply
./gradlew :game:checkstyleMain
```

## 8. Relation to custom input commands

Custom player inputs already travel through `c2s/input.proto` (`CustomAction`) and are dispatched by `InputCommandRouter`.

You only need a new protobuf message when:

- input payload in `CustomAction` is not enough, or
- you need a new explicit network message type with dedicated converter/parser/wire id.

