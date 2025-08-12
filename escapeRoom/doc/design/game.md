---
title: "Entwicklung des Designs der Implementierung von Escape-Rooms im Spiel"
---

# Rahmenentscheidungen

Unsere Escape Rooms sind für **2–4 Spieler** ausgelegt – je nach Raum kann die genaue Anzahl festgelegt werden.
*   In kleinen Gruppen können sich alle aktiv beteiligen, gemeinsam Lösungen entwickeln und Verantwortung übernehmen. 
*   Gruppengrößen besonders förderlich für Kommunikation, Zusammenarbeit und ein intensives gemeinsames Erlebnis sind (vgl [O’Szabo et al., 2022](../research/sources/the_anatomy_of_social_dynamics_in_escape_rooms.md)). 
*   Coop-Spiele: vor allem Zweierteams; besonders eng zusammenarbeiten; starkes Wir-Gefühl
*   Multiplayer-Spiele: Teams um 5 Leute; starke Rollen/ Aufgabenverteilung; höher Kommunikatiosaufwand; 
*   vergleichbar mit Praktikumsgruppengröße
*   beachtet den Umsetzungsaufwand; Räume für mehr Spieler werden komplexer im Design und Umsetzung

# Multiplayer-Implementierung

Annahme: wir brauchen Netzwerk-Multiplayer

- verteilte Simulation
- Client-Server (geringster Aufwand, inhaltlicher Nutzen von P2P für diesen Einsatzzweck unklar)
- ..
- parallel läuft ein Projekt mit dem Ziel, hier mehr Anhaltspunkte zu finden und einen PoC zu erstellen
