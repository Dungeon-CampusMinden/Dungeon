# Morph & Co. KG – Die Wörterfabrik

Dieses Dokument beschreibt den umgesetzten Escape Room. Grundlage sind das Skript
„Morph & Co.“ und das Handout [Morphemtypen und wo sie zu finden sind](Handout_Morphemtypen.pdf)
(angepasst nach Busch & Stenschke 2014, *Germanistische Linguistik*, S. 83).

# 1. Überblick

| | |
| --- | --- |
| Fach | Germanistische Linguistik, Morphologie |
| Inhalte | flektierbare Wortarten, Affixtypen (Präfix, Suffix, Zirkumfix), Wortbildung vs. Flexion, freie Morpheme, Morphemanalyse |
| Spieler | 1 bis 2, allein oder im Multiplayer |
| Dauer | Zielwert 30 bis 45 Minuten, im Playtest zu prüfen |
| Setting | Wörterfabrik der Morph & Co. KG, Kontrollraum und zwei Produktionshallen |
| Start | `rooms.morphco.MorphCo`, Tracking-Raum `morph-co` |
| Gradle | `runMorphCo`, `runMorphCoLevelEditor`, `runMorphCoServer`, `runMorphCoAutoplay`, `buildMorphCoJar`, `jpackageImageMorphCo` |

# 2. Rahmenhandlung

Die Wörterfabrik der Morph & Co. KG stellt seit Jahrhunderten alle deutschen Wörter her.
Heute Morgen spielen die Maschinen verrückt: Präfixe landen an falschen Wörtern, Flexive werden
vertauscht. Die Qualitätskontrolle hat die Produktion gestoppt. Die Spieler sollen die
Produktionslinie reparieren und neu starten.

Nach einem Intro (Schwarzblende) brüllt die Schichtleitung gegen den Alarm an. Mehr als
„ZU LAUT. ALARM!“ ist nicht zu verstehen. Erst muss der Lärm aufhören. Das Ende bleibt offen:
War es Sabotage, ein Materialfehler oder Zufall? „Das muss bis zum nächsten Abenteuer offen
bleiben …“

# 3. Räume

```
+------------------------+------------------+
| F-04 Flexionsstraße    D A-12 Affix-      |
|                        | Sortieranlage    |
+-----D------------------+-------D----------+
|                  Flur                     |
+--D--+--D--+--D--+--------D----------------+
|Küche|Abst.| WC  |      Kontrollraum       |
+-----+-----+-----+-----------D-------------+
                         | Eingang (Start) |
                         +-----------------+
```

| Raum | Zugang | Inhalt |
| --- | --- | --- |
| Eingang | Start | Schichtleitung (Hinweise), Lesegerät, Warnschild |
| Kontrollraum | Karte | Konsole (Alarm), PC (Produktionsleitsystem), Telefon, Sicherungsbox, Tisch, Kaffeetasse, Pflanze, 4 Plakate, Hinweis Videoüberwachung |
| Flur | nach dem PC | Türschilder, Pflanze; Küche und Abstellkammer verschlossen |
| Sanitäranlagen | offen | Waschbecken, Spiegel |
| F-04 | Flur | Flexionsmaschine, Bildschirm, 5 Kisten, Förderband mit 4 Wortformen, 2 Regale, Papierkorb, 2 Plakate, große Pflanze |
| A-12 | Flur oder Verbindungstür | Affix-Sortieranlage mit Hebel, Display, Wartungsklappe und Schloss, Plakat, Murmeltier-Bild, Regal, Zirkumfixmontagestation, Kisten, Krimskrams |

Die Tür „Zur Produktion“ öffnet sich, sobald das Produktionsleitsystem gelesen wurde. Danach ist
die Reihenfolge der Reparaturen frei.

# 4. Ablauf und Lösungen

| Schritt | Ort | Aufgabe | Lösung |
| --- | --- | --- | --- |
| 1 | Eingang | Sicherheitskarte an das Lesegerät halten | Tür zum Kontrollraum öffnet sich |
| 2 | Kontrollraum | Konsole: Alarm ausschalten | Knopf „Nerviger Alarmton“ (andere Knöpfe geben witzige Rückmeldungen) |
| 3 | Kontrollraum | PC: Bildschirmschoner wegklicken, Status lesen | F-04 (Fehler 133) und A-12 (Fehler 204) gestört; Tür zur Produktion öffnet |
| 4a | F-04 | Regal: Handbuch FL3–10–9001 lesen | flektierbar sind Nomen, Adjektive, Verben, Artikel, Pronomina |
| 4b | F-04 | Kisten: Wortarten sortieren | siehe Tabelle unten |
| 5a | A-12 | Schloss der Wartungsklappe | **8** freie Morphemtypen ohne Portmanteau (Handout Teil 1 in F-04) |
| 5b | A-12 | Wartungsklappe: Wörter zerschneiden und Affixe sortieren | siehe unten |
| 5c | A-12 | Eingabeschächte | Wortbildung: {un-}, {be-}, {-bar}, {-ung}; Flexion: {-ten}, {ge- … -et} |
| 6 | F-04 | Flexionsmaschine prüfen | repariert, sobald 4b und 5c erledigt sind |
| 7 | Kontrollraum | PC: [NEUSTART], Sicherheitsfrage | *Überlegungen* = {über}{leg}{ung}{en} = **4** |
| optional | A-12 | Krimskrams und Zirkumfixmontagestation | {ge}+{e}, {be}+{ig}, {ge}+{en} |

**Wortarten-Kisten** (Wörter ohne passende Kiste bleiben liegen):

| Satz | Wörter |
| --- | --- |
| Ein Hamster kommt selten zu spät | Ein → Artikel, Hamster → Nomen, kommt → Verben, selten/zu → bleiben, spät → Adjektive |
| Grüne Törtchen sind äußerst lecker gewesen und diese schmecken gut | Grüne → Adjektive, Törtchen → Nomen, sind → Verben, äußerst → bleibt, lecker → Adjektive, gewesen → Verben, und → bleibt, diese → Pronomina, schmecken → Verben, gut → Adjektive |

**Affix-Sortierung:** Ein Klick zwischen zwei Buchstaben zerschneidet ein Wort. Die Teile werden
per Drag & Drop oder Klick in Fächer gelegt. Stämme bleiben liegen.

| Wort | Schnitte | Fächer |
| --- | --- | --- |
| unbezahlbar | un · be · zahl · bar | {un-}, {be-} → Präfixe; {-bar} → Suffixe |
| auf | – | ganzes Wort → Kein Affix |
| geregnet | ge · regn · et (auch ge · regne · t) | {ge-}, {-et} → Zirkumfixe |
| Störung | Stör · ung | {-ung} → Suffixe |
| sagten | sag · ten (auch sag · te · n) | {-ten} → Suffixe |
| Schornstein, Hamster | – | ganzes Wort → Kein Affix |

## Gemeinsame Regeln

- Falsche Eingaben kosten nichts. Die Sortier-UIs zeigen sofort, wie viele Wörter noch falsch
  liegen; der Server prüft jede Eingabe erneut und trackt sie.
- Die Schichtleitung gibt jederzeit einen Hinweis zum ersten offenen Schritt. Ein begonnener
  Zweig (A-12 oder F-04) wird dabei zuerst zu Ende geführt.
- Das Handout ist auf zwei Bände verteilt, wie im Skript („Bestseller in zwei Bänden“):
  Teil 1 (freie Morpheme, Portmanteau) steht in F-04, Teil 2 (gebundene Morpheme, Flexions- und
  Derivationsaffixe) in A-12. Die Schlossfrage in A-12 braucht Teil 1 und verbindet so beide Hallen.

# 5. Fortschritt (Petri-Netz)

`MorphCoProgressNet` folgt dem Muster von LastHour: Jeder Meilenstein hat einen Pending- und
einen Complete-Place; Voraussetzungen sind Lesekanten.

```
ALARM_OFF → CAUSES_FOUND ─┬→ HANDBOOK_READ → WORD_CLASSES_SORTED ─┐
                          │                                       ├→ F04_REPAIRED ─┐
                          └→ HATCH_UNLOCKED → AFFIXES_SORTED → A12_REPAIRED ───────┴→ PRODUCTION_RESTARTED
```

F-04 setzt A-12 voraus, weil die Flexionsmaschine erst mit korrekt sortierten Affixen
funktioniert. Beobachtungen ohne Gating (gelesene Plakate, Förderband, Papierkorb, …) sind Flags in
`MorphCoState`.

# 6. Questlog

Das Questlog wird aus dem Zustand abgeleitet (`MorphCoQuestLog.sync`). Erledigte Aufgaben
ersetzen ihren Eintrag und behalten den ursprünglichen Zeitstempel.

| Tab | Einträge |
| --- | --- |
| Auftrag | Alarm, Ursachen feststellen, A-12 reparieren, F-04 reparieren, Produktion neu starten |
| F-04 Flexionsstraße | Handbuch gelesen, Flexionsmaschine überprüfen, Wortarten identifiziert, A-12 inspizieren, repariert |
| A-12 Affix-Sortieranlage | inspiziert, Affixe sortieren, repariert, zur Flexionsmaschine gehen |
| Indizien | fehlerhafte Wortformen, Mängelmeldung, Handbuch, Morphemtypen Teil 1/2 (aufklappbar), Display, Schlossfrage, Zirkumfix-Teile |

# 7. Achievements

| ID | Name | Bedingung |
| --- | --- | --- |
| `morphco_alarm_off` | Wuuuuuuh-o, wuuuuuh-o | Alarm aus |
| `morphco_word_classes` | Kistenweise Wortarten | Wortarten-Kisten gelöst |
| `morphco_marie_kondo` | Marie Kondo wäre stolz auf dich | A-12 repariert |
| `morphco_flexion` | In Form gebracht | F-04 repariert |
| `morphco_combinatorial` | Kombinatorisch 1A! | alle drei Zirkumfixe montiert |
| `morphco_restart` | All systems nominal | Produktion neu gestartet |
| `morphco_first_try` (versteckt) | Schnitt sitzt | Affixe im ersten Versuch richtig |
| `morphco_plants` (versteckt) | Was hast du erwartet? | alle 3 Pflanzen |
| `morphco_lever` (versteckt) | Hnnnnnnnnnng | 5× am Hebel ziehen |
| `morphco_nobody_home` (versteckt) | Niemand zu Hause | Wachschutz und Rosi anrufen |
| `morphco_posters` (versteckt) | Aushangkundig | alle 7 Plakate |
| `morphco_bookworm` (versteckt) | Leseratte | alle 8 Bücher |
| `morphco_platinum` | Mitarbeiter*in des Monats | alle anderen |

Die Bilder sind vorhandene Sprites des Projekts (wie bei Soulweaver).

# 8. Technik

- **Server/Client:** Der Server entscheidet über Fortschritt, Türen, Items und Dialoge. Eigene
  Dialogtypen (`MorphCoDialogs`) werden auf Server und Client registriert. Die UIs prüfen lokal für
  sofortiges Feedback und senden jede Eingabe als `StringValue` an den Server
  (`MorphCoDialogs.SUBMIT`). `MorphCoSnapshotTranslator` überträgt Kollider und
  Interaktionsreichweiten.
- **Lokalisierung:** Alle Texte stehen in `game/assets/language/morphCo/de.json` und `en.json`.
  Der Server verschickt Schlüssel (`MorphCoText.key`), `MorphCoTranslator` löst sie auf dem Client
  auf. Die deutschen Beispielwörter bleiben auch in der englischen Fassung deutsch.
- **Speichern:** `MorphCoSave` schreibt nach jedem Meilenstein `morph-co-save.json` (Meilensteine,
  Flags, montierte Zirkumfixe, Spielzeit, Run-ID, Tracking-Einwilligung). „Fortsetzen“ stellt den
  Stand wieder her; Inventare werden nicht gespeichert, deshalb gibt der Krimskrams nach dem Laden
  die noch nicht montierten Zirkumfix-Teile erneut heraus.
- **Tracking:** Puzzle-Starts, Lösungen, Versuche (`assignment`, `segmentation`, `number`, `choice`)
  und Hinweise der Schichtleitung über `MorphCoTracking`.
- **Level:** `game/assets/levels/morphCo/morphco_1.level` (Design `MORPH_CO`). Alle Objekte sind
  Prefab-Instanzen `morphco-object` und werden im Level-Editor (Prefab-Modus) platziert; die
  Controller hängen ihr Verhalten per Instanzname an. Benannte Punkte gibt es nur für Türen und
  Auslöseflächen. `MorphCoLevelFileTest` prüft, dass jedes vom Code genutzte Objekt im Level steht,
  jede Objektart bekannt ist und alle Bilder existieren.

## Abweichungen vom Skript

- Der PC lässt sich erst nach dem Alarm sinnvoll lesen, und die Tür „Zur Produktion“ öffnet
  sich erst, wenn das Produktionsleitsystem gelesen wurde (so bleibt die Reihenfolge des Skripts
  erhalten).
- Die vier Aufgaben stehen wie im Skript direkt nach dem PC im Questlog; „Produktion neu starten“
  wird konkret, sobald F-04 und A-12 laufen.
- Die letzte Zeile der Wortarten-Tabelle („und diese schmecken gut“) wird als Fortsetzung von
  Satz 2 gezeigt.
- Neben der Skriptlösung werden die feineren Segmentierungen *sag·te·n* und *ge·regne·t*
  akzeptiert.
- Zusätzliche, nicht versteckte Achievements: Kistenweise Wortarten, In Form gebracht, All systems
  nominal.
- Die Statuszeilen des Neustarts erscheinen auf einer Schwarzblende statt nacheinander am PC.

- Die Sicherheitskarte ist kein Inventargegenstand; das Lesegerät öffnet die Tür direkt.
- Die Konsole bietet neben „Nerviger Alarmton“ einige Scherz-Knöpfe.
- Zusätzliche Kleinigkeiten: Spiegel im WC, Kisten in A-12, Hinweise der Schichtleitung,
  versteckte Achievements.
- Der Alarm ist eine Sirene, die beim Ausschalten verstummt; das Leiserwerden beim Schließen der
  Tür entfällt.

# 9. Offene fachliche Fragen an die Autorin

Ein fachliches Review hat folgende Punkte im Skript bzw. Handout markiert. Sie sind **unverändert
umgesetzt**, weil sie den Inhalt der Vorlage betreffen:

1. Das Handbuch sagt „Präfixe und Zirkumfixe gehören daher nicht in die Flexionsmaschine!“, die
   Eingabeschächte verlangen aber {ge- ... -et} unter Flexion (und das Handout führt {ge-}+{-t}
   als Flexionsaffix). Vorschlag: „Wortbildungspräfixe und -zirkumfixe …; Ausnahme: das Zirkumfix
   des Partizips II.“
2. *selten* bleibt liegen, *spät* kommt zu den Adjektiven – beide sind hier adverbial gebrauchte
   Adjektive (seltener, später). Vorschlag: ein echtes Adverb, z. B. „Ein Hamster kommt nie zu spät“.
3. Handout: {nimm} ist selbst eine Imperativform; gemeint ist vermutlich {nehm}. {-thek} ist eher
   ein Konfix (Bibliothek, Videothek) als unikal. {be-}+{-t} (*berühmt*) ist umstritten
   (*beherzt*, *bebrillt* wären klarer).
4. {be- ... -ig} (*begierig*) wird meist als *Begier* + {-ig} analysiert; *gehen – ging –
   gegangen* ist eher Suppletion als ein reines Ablautbeispiel (*singen – sang – gesungen*).
5. Die Schlossfrage nennt 8 als allgemeine Zahl; sie gilt für die Klassifikation des Handouts.

# 10. Autoplay

`./gradlew :game:runMorphCoAutoplay` startet den Raum ohne Menü und spielt ihn mit der ersten
Figur einmal durch (`MorphCoAutoplay`): Die Figur tritt vor jedes Objekt, löst die echte
Interaktion mit Reichweitenprüfung aus und beantwortet die Dialoge über dieselben Server-Callbacks
wie ein Client. Das Log nennt jeden Schritt und die gemessene Distanz. Das Spiel endet regulär mit
Abspann.

# 11. Tests

```sh
./gradlew :game:test --tests 'rooms.morphco.*'
```

Geprüft werden die Rätselmodelle, das Petri-Netz, Speicherstände, Sprachdateien (Schlüssel- und
Platzhaltergleichheit, alle im Code genutzten Schlüssel), Achievements und die Level-Datei.
