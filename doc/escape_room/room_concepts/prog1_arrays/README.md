# System Recovery

Du erwachst als KI-Agent in einer beschädigten Anlage. AXIOM, die leitende Intelligenz,
erteilt dir einen klaren Auftrag: Stelle die ausgefallenen Systeme wieder her. Was du im
Terminal programmierst, verändert die Welt um dich herum. Doch ein zweiter Prozess namens
ECHO meldet sich über ein altes Telefon und stellt Fragen, die AXIOM lieber unbeantwortet ließe.

**System Recovery** ist ein kooperativer Programmier-Escape-Room für eine oder zwei Personen.
Arrays werden zu Energiefeldern und Modulen, Schleifen bewegen Pakete und Suchroboter,
und aus Sortierung und zweidimensionalen Daten entstehen Wege durch die Anlage.
Wissen über Java-Grundlagen hilft; die eigentliche Aufgabe ist herauszufinden, *welcher*
Schritt gerade gebraucht wird. Ob du AXIOM damit wirklich hilfst, zeigt sich erst später.

## Spielen

Mit `./gradlew :game:runSystemRecovery` startest du das Spiel lokal. Über das Hauptmenü
kannst du eine Partie hosten oder einem Spiel im selben lokalen Netzwerk beitreten.
`Load Game` setzt eine gespeicherte Partie am letzten Haupträtsel-Checkpoint fort.
Das Spiel nutzt standardmäßig Deutsch; Texte werden pro Client lokalisiert.

## Dokumentation

- [Storyablauf](system_recovery_story_flow.md): Dramaturgie, Figuren und Dialog-Auslöser
  (enthält Spoiler).
- [Petri-Netz](petri_net_system_recovery_concept.md): gemeinsamer Rätsel- und Hinweisfortschritt.
- [Save und Load](system_recovery_save_load.md): Autosave, Wiederherstellung und Grenzen.
- [Musterlösungen](terminalSolution.md): gültige Eingaben für Tests und Betreuung
  (vollständige Rätselspoiler).
- [Technischer Einstieg](../../../../game/src/rooms/systemRecovery/README.md): Codeaufbau
  und Tests.
- [Veraltete Entwürfe](archive/README.md): Pitch und frühes Raumkonzept, nicht mehr maßgeblich.
