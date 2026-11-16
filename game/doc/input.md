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

Diese Abfragen gelten für den aktuellen Frame und gehören deshalb in Code, der pro Frame läuft
(Kontext-Handler, Systeme der Client-Seite). Die Spielwelt läuft in festen Welt-Ticks, von denen
auf einen Frame keiner oder mehrere fallen können. Gameplay-Eingaben gehen deshalb als
`InputMessage` an die Simulation (`Game.network().sendInput`): Im Einzelspieler wendet der nächste
Tick sie genau einmal an, im Multiplayer der Server.

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

`.blocking()` macht einen Kontext blockierend. `.closedBy(...)` nennt weitere Actions, die eine UI
schließen. Eine UI listet dort auch die Action, die sie öffnet, damit dieselbe Eingabe sie wieder
schließt. `.padOnly(...)` nimmt eine Action nur über ihre Pad-Belegung an: START öffnet die Pause
über jeder UI, Esc schließt dort. `UiActions.modal(name, actions...)` fasst das für eine modale
UI zusammen: blockierend, `ui:close` schließt, START öffnet die Pause darüber.

Widgets rufen in `touchDown` auf etwas Ziehbarem `GameInput.startDrag()` auf. Der Drag beginnt dann
schon mit dem Druck statt mit der ersten Bewegung. Am Pad legt das nächste A ab, ohne dass die Taste
gehalten wird.

## Bindings

Im Code entstehen Bindings über die Factories von `Binding`: `key(Keys.E)`, `key(Keys.F3, Keys.A)`,
`key(Keys.CONTROL_LEFT, Keys.C)`, `mouse(Buttons.RIGHT)`, `wheel()`, `pad(Pad.A)`, `pad(PadAxisId.LSTICK)`. Bei
Kombis löst die letzte Taste aus, die davor müssen schon gehalten sein. Eine Taste, die selbst Action
und Präfix ist (F3), feuert beim Loslassen.

Spieler-Overrides stehen in `input_bindings.json` neben dem Spiel, dort als lesbare Strings wie
`key:Ctrl+C`, `mouse:RIGHT` oder `pad:A`. Eine leere Liste heißt bewusst unbelegt, ein fehlender
Eintrag Default. `InputResolver#rebind` lehnt Konflikte ab; Konflikte der
Defaults erscheinen beim Anmelden eines Kontexts im Log.

In Texten zeigt `[action id=core:interact]` die aktuelle Belegung für das zuletzt genutzte Gerät;
`[action id=core:interact all]` zeigt alle Belegungen dieses Geräts.
