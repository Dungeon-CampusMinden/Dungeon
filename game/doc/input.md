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
GameInput.released(EditorActions.PLACE);       // Loslassen erreicht diesen Frame
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

Räume definieren Actions in ihrem Namespace und legen beim Start einen Kontext auf:

``` java
ActionId jump = new ActionId("myroom", "jump");
GameInput.bindings().define(jump, "key:SPACE");
GameInput.push(
    new InputContext(ContextSpec.of("myroom", ContextLayer.ROOM, false, jump))
        .onPress(jump, this::jump));
```

## Bindings

Bindings sind lesbare Strings: `key:E`, `key:Ctrl+C`, `key:F3+A`, `mouse:RIGHT`, `mouse:WHEEL`,
`pad:A`, `pad:LSTICK`. Eine Kombi braucht bereits gehaltene Präfixtasten und eine neue Druckkante der
letzten Taste. Eine Taste, die selbst Action und Präfix ist (F3), feuert beim Loslassen.

Spieler-Overrides stehen in `input_bindings.json` neben dem Spiel. Eine leere Liste heißt bewusst
unbelegt, ein fehlender Eintrag Default. `InputResolver#rebind` lehnt Konflikte ab; Konflikte der
Defaults erscheinen beim Start im Log.

In Texten zeigt `[action id=core:interact]` die aktuelle Belegung für das zuletzt genutzte Gerät.
