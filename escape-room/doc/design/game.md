---
title: "Entwicklung des Designs der Implementierung von Escape-Rooms im Spiel"
---

# Rahmenentscheidungen

*   Escape Rooms sind für 2–4 Spieler konzipiert. Die genaue Anzahl der Spieler kann für jeden Escape Room individuell festgelegt werden. Die Spieleranzahl hilft uns dabei, die Escape Rooms so zu gestalten, dass alle Teilnehmenden aktiv eingebunden sind und sinnvoll zur Lösung beitragen können.

# Multiplayer-Implementierung

Annahme: wir brauchen Netzwerk-Multiplayer

- verteilte Simulation
- Client-Server (geringster Aufwand, inhaltlicher Nutzen von P2P für diesen Einsatzzweck unklar)
- ..
- parallel läuft ein Projekt mit dem Ziel, hier mehr Anhaltspunkte zu finden und einen PoC zu erstellen
