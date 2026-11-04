---
title: Eingaben
---

Spielcode kennt keine Tasten, sondern benannte Actions wie `core:interact`. Tastatur, Maus und Pad
speisen Actions über Bindings ein; welcher Empfänger einen Druck bekommt, entscheidet der
Kontext-Stack. Einstiegspunkt ist `engine.input.GameInput`.

## Actions lesen

``` java
GameInput.pressed(CoreActions.SKILL_MAIN);     // gehalten
GameInput.justPressed(EditorActions.PICK);     // Druck erreicht diesen Frame
GameInput.justReleased(EditorActions.PLACE);   // Loslassen erreicht diesen Frame
GameInput.axis(EditorActions.ZOOM);            // 1D-Achse dieses Frames
CoreActions.move();                            // Bewegungsvektor, Länge höchstens 1
GameInput.pointer();                           // Zeiger 0: Maus oder Pad-Cursor
```

Pro Frame erreicht je Action höchstens eine Kante den Aufrufer. Mehrere Drücke innerhalb eines
Frames gehen nicht verloren, sondern kommen in den nächsten Frames an.

## Kontexte

Ein `InputContext` deklariert seine Actions über eine `ContextSpec`, liegt auf einer `ContextLayer`
und kann darunterliegende Kontexte blockieren. Ein Druck gehört dem obersten Kontext, der ihn annimmt:
über eine passende Action oder weil er blockiert. Das Loslassen geht an denselben Besitzer. Danach
sieht die Stage das Ereignis; behandelt ein Widget es, entsteht keine Action.

Ein blockierender Kontext über dem Besitzer, Fokusverlust und das Entfernen des Besitzers brechen
laufende Eingaben ohne Release ab. Sie bleiben bis zum echten Loslassen gesperrt.

Dialoge bekommen ihren Kontext vom `HudSystem`. Dialogtypen mit eigener Belegung melden sie mit
`DialogInput.register` an; eigene Reaktionen im Dialog stehen in `UIUtils.onUiAction`.

Räume definieren Actions in ihrem Namespace und aktivieren beim Start einen Kontext. Die
Default-Belegung steht direkt an der Action, das Aktivieren meldet beides an:

``` java
ActionId jump = new ActionId("myroom", "jump", key(Keys.SPACE));
GameInput.activate(
    new InputContext(ContextSpec.of("myroom", ContextLayer.ROOM, jump))
        .onPress(jump, this::jump));
```

`.blocking()` macht einen Kontext blockierend. `.closedBy(...)` nennt die Actions, die eine UI
schließen. Eine UI listet dort auch die Action, die sie öffnet, damit dieselbe Eingabe sie wieder
schließt. `GameInput.register(spec)` meldet Actions an, die
angezeigt werden sollen, bevor ihr Kontext aktiv ist.

Widgets rufen in `touchDown` auf etwas Ziehbarem `GameInput.keepPadPress()` auf. Am Pad startet A
dann den Drag und das nächste A legt ab, ohne dass die Taste gehalten wird.

## Bindings

Im Code entstehen Bindings über die Factories von `Binding`: `key(Keys.E)`, `key(Keys.F3, Keys.A)`,
`key(CTRL, Keys.C)`, `mouse(Buttons.RIGHT)`, `wheel()`, `pad(Pad.A)`, `pad(PadAxisId.LSTICK)`. Bei
Kombis löst die letzte Taste aus, die davor müssen schon gehalten sein. Eine Taste, die selbst Action
und Präfix ist (F3), feuert beim Loslassen.

Spieler-Overrides stehen in `input_bindings.json` neben dem Spiel, dort als lesbare Strings wie
`key:Ctrl+C`, `mouse:RIGHT` oder `pad:A`. Eine leere Liste heißt bewusst unbelegt, ein fehlender
Eintrag Default. `InputResolver#rebind` lehnt Konflikte ab; Konflikte der
Defaults erscheinen beim Anmelden eines Kontexts im Log.

In Texten zeigt `[action id=core:interact]` die aktuelle Belegung für das zuletzt genutzte Gerät;
`[action id=core:interact all]` zeigt alle Belegungen dieses Geräts.
