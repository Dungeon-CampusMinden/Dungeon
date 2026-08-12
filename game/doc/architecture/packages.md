# Package architecture

The `game` subproject contains the Java runtime, reusable escape-room features, and reference
rooms. Package boundaries express ownership inside this build unit:

- `engine.*` contains the ECS, rendering, level, input, networking, sound, and shared runtime.
- `feature.*` contains reusable gameplay capabilities such as skills, inventory, interaction,
  hints, puzzles, and achievements.
- `escaperoom.foundation.*` contains the room definition and generic escape-room runtime.
- `rooms.*` contains concrete rooms and room-specific behavior. `rooms.lasthour` is the reference
  room.

Rooms may depend on features, the foundation, and the engine. Features and the foundation must not
import room packages. Runtime composition currently lives in `engine.game` and may wire reusable
features; lower-level engine packages should avoid new feature dependencies. Shared input code
belongs in `engine.input`; room-specific interaction code stays in the corresponding `rooms.*`
package.

The Wizard remains a separate build unit because it validates and packages a different artifact.
Its Java runner depends on `:game`; its React/Vite frontend keeps its own npm build.
