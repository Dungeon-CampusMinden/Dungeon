# Morph & Co. KG: Die Wörterfabrik

Dieses Dokument beschreibt den umgesetzten Escape Room. Grundlage sind das Skript
„Morph & Co.“ und das Handout [Morphemtypen und wo sie zu finden sind](Handout_Morphemtypen.pdf)
(angepasst nach Busch & Stenschke 2014, *Germanistische Linguistik*, S. 83).

Den kompletten Lösungsweg mit Screenshots, Bedienung, Hilfesystem und Achievements zeigt der
[Walkthrough](Morph%20&%20Co%20Walkthrough/MorphCo.md) ([PDF](Morph%20&%20Co%20Walkthrough/MorphCo.pdf)).

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
| Eingang | Start | Schichtleitung (Hinweise), Spind mit Sicherheitskarte, Lesegerät, Warnschild |
| Kontrollraum | Karte | Konsole (Alarm), PC (Produktionsleitsystem), Telefon mit Ziffernwahl und Technik-Hotline, Sicherungsbox, Tisch mit Schichtplan, Kaffeetasse, Pflanze, 4 Plakate, Hinweis Videoüberwachung |
| Flur | nach der Sicherungsbox | Türschilder, Pflanze, Schichtuhr; Küche und Lager verschlossen |
| Sanitäranlagen | offen | Waschbecken, Spiegel, blaue Tonne als Toilette |
| F-04 | Flur | Flexionsmaschine, Bildschirm, 5 Kisten, Förderband mit 4 Wortformen, Regal, Papierkorb am Bandende, 2 Plakate, große Pflanze |
| A-12 | Flur oder Verbindungstür | Affix-Sortieranlage mit Hebel, Display, Wartungsklappe und Schloss, Plakat, Murmeltier-Bild, Regal, Zirkumfixmontagestation, Kisten, Krimskrams |

Die Tür zum Flur öffnet sich, sobald die Sicherungsbox verkabelt ist. Der PC nennt die kaputten
Maschinen, ist aber keine Bedingung. Danach ist die Reihenfolge der Reparaturen frei.

# 4. Ablauf und Lösungen

| Schritt | Ort | Aufgabe | Lösung |
| --- | --- | --- | --- |
| 1 | Eingang | Sicherheitskarte aus dem Spind holen und durch das Lesegerät ziehen | ohne Karte meldet das Lesegerät, dass sie fehlt; im richtigen Tempo (0,3 bis 1,2 s) öffnet sich die Tür zum Kontrollraum |
| 2 | Kontrollraum | Konsole: Alarm ausschalten | Knopf „Nerviger Alarmton“ (andere Knöpfe geben witzige Rückmeldungen) |
| 3a | Kontrollraum | PC: Bildschirmschoner wegklicken, Status lesen | F-04 (Fehler 133) und A-12 (Fehler 204) gestört, Türsteuerung zum Flur ohne Strom |
| 3b | Kontrollraum | Sicherungsbox: Kabel wieder einstecken | jedes Kabel per Ziehen oder Klicken in die Buchse mit derselben Beschriftung; die grüne Notbeleuchtung geht an. Sind 3a und 3b erledigt, in beliebiger Reihenfolge, öffnet sich die Tür zum Flur |
| 4a | F-04 | Regal: Handbuch FL3-10-9001 lesen | flektierbar sind Nomen, Adjektive, Verben, Artikel, Pronomina |
| 4b | F-04 | Kisten: Wortarten sortieren | siehe Tabelle unten |
| 5a | A-12 | Nahansicht der Anlage: Ziffernblock am Schloss der Wartungsklappe | **8** freie Morphemtypen ohne Portmanteau (Handout Teil 1 in F-04) |
| 5b | A-12 | Wartungsklappe: Wörter zerschneiden und Affixe sortieren | siehe unten |
| 5c | A-12 | Eingabeschächte | Wortbildung: {un-}, {be-}, {-bar}, {-ung}; Flexion: {-ten}, {ge- … -et} |
| 5d | A-12 | Krimskrams und Zirkumfixmontagestation: Zirkumfixe fürs Zirkumfix-Magazin montieren | {ge}+{e}, {be}+{ig}, {ge}+{en}; geht schon vor 5a |
| 5e | A-12 | Hebel: Anlage neu starten | gleichmäßig ganz nach unten ziehen (0,6 bis 2 s); vor 5c und 5d klemmt der Hebel |
| 6 | F-04 | Flexionsmaschine prüfen | repariert, sobald 4b und 5e erledigt sind |
| 7 | Kontrollraum | PC: [NEUSTART], Sicherheitsfrage | *Überlegungen* = {über}{leg}{ung}{en} = **4** |
| optional | F-04 | Papierkorb durchwühlen | Wort beim Überfahren lesen, Zettel wegziehen oder per Klick hinauswerfen, dann die gefaltete Mängelmeldung per Klick entfalten |

**Wortarten-Kisten** (Wörter ohne passende Kiste bleiben liegen):

| Satz | Wörter |
| --- | --- |
| Ein Hamster kommt immer zu spät | Ein → Artikel, Hamster → Nomen, kommt → Verben, immer/zu → bleiben, spät → Adjektive |
| Grüne Törtchen sind äußerst lecker gewesen und diese schmecken gut | Grüne → Adjektive, Törtchen → Nomen, sind → Verben, äußerst → bleibt, lecker → Adjektive, gewesen → Verben, und → bleibt, diese → Pronomina, schmecken → Verben, gut → Adjektive |

**Affix-Sortierung:** Ein Klick zwischen zwei Buchstaben zerschneidet ein Wort. Die Teile werden
per Drag & Drop oder Klick in Fächer gelegt. Stämme bleiben liegen.

| Wort | Schnitte | Fächer |
| --- | --- | --- |
| unbezahlbar | un · be · zahl · bar | {un-}, {be-} → Präfixe; {-bar} → Suffixe |
| auf | keine | ganzes Wort → Kein Affix |
| geregnet | ge · regn · et (auch ge · regne · t) | {ge-}, {-et} → Zirkumfixe |
| Störung | Stör · ung | {-ung} → Suffixe |
| sagten | sag · ten (auch sag · te · n) | {-ten} → Suffixe |
| Schornstein, Hamster | keine | ganzes Wort → Kein Affix |

## Gemeinsame Regeln

- Falsche Eingaben kosten nichts. Die Sortier-UIs zeigen sofort, wie viele Wörter noch falsch
  liegen; der Server prüft jede Eingabe erneut und trackt sie.
- Die Schichtleitung gibt jederzeit einen Hinweis zum ersten offenen Schritt. Ein begonnener
  Zweig (A-12 oder F-04) wird dabei zuerst zu Ende geführt.
- Die Technik-Hotline am Telefon ist das Hilfesystem des Raums. Sie gibt zum selben Schritt
  gestufte Hinweise bis zur Lösung (siehe unten).
- Das Handout ist auf zwei Bände verteilt, wie im Skript („Bestseller in zwei Bänden“):
  Teil 1 (freie Morpheme, Portmanteau) steht in F-04, Teil 2 (gebundene Morpheme, Flexions- und
  Derivationsaffixe) in A-12. Die Schlossfrage in A-12 braucht Teil 1 und verbindet so beide Hallen.
  Beide Bände nimmt man aus dem Regal ins Inventar; zieht man dort einen auf den anderen, entsteht
  das Handout „Morphemtypen“ mit beiden Teilen.

## Telefon und Technik-Hotline

Ein Klick auf das Telefon im Kontrollraum öffnet Hörer und Ziffernblock. Daneben klebt der Zettel
„Wichtige Durchwahlen“. Gewählt wird mit dem grünen Hörer oder Enter, der rote Hörer legt auf.
Ansagen und die Rückfrage der Hotline erscheinen auf einem Bildschirm unter dem Apparat; das
Telefon bleibt offen, danach wählt man gleich weiter.

| Nummer | Steht auf | Ergebnis |
| --- | --- | --- |
| 99 | Zettel am Telefon | Mailbox: die Nachricht vom Morgen; das Ausrufezeichen am Telefon verschwindet |
| 42 | Zettel am Telefon | Technik-Hotline (Instandhaltung) mit gestuften Hinweisen |
| 11 | Zettel am Telefon | Wachschutz am Eingang: niemand nimmt ab |
| 17 | Schichtplan auf dem Tisch | Rosi: nimmt nicht ab (mit 11 das versteckte Achievement „Niemand zu Hause“) |
| 0172 1717 4711 0815 1717 | Zettel am Telefon | Rosis Handy: nimmt auch nicht ab (zählt wie die 17) |
| 133, 204 | Fehlercodes am PC | Bandansage, die auf die Technik-Hotline verweist |
| jede andere | nirgends | „Kein Anschluss unter dieser Nummer.“ |

Die Hotline hilft bei dem Schritt, zu dem die Schichtleitung gerade etwas sagen würde
(`ShiftLead.hint`), in drei Stufen: erst die Richtung, dann ein konkreter Tipp, zuletzt die
Lösung. Vor der Lösung fragt sie nach („Willst du wirklich die Lösung hören?“); Nein kostet
nichts. Danach wiederholt sie die Lösung. Ist der Schritt gelöst, beginnt der nächste wieder bei
der ersten Stufe. Die Stufen gelten für den ganzen Raum: Im Multiplayer setzt der zweite Spieler
dort fort, wo der erste aufgehört hat. Jede gehörte Stufe steht im Questlog-Tab „Hinweise“. Die
Hotline antwortet schon vor dem Alarm (Hinweise zum Alarm). Im Skript gibt es sie nicht; damit
die Spieler sie als Hilfe erkennen, nennt sie schon der Steuerungsdialog der Schichtleitung zu
Beginn („Kommst du nicht weiter? Frag mich oder ruf die Technik-Hotline an …“). Außerdem weisen der
Zettel am Telefon, die Bandansage zu den Fehlercodes und der Hinweis `hint-repair` der
Schichtleitung auf sie hin.

# 5. Fortschritt (Petri-Netz)

`MorphCoProgressNet` folgt dem Muster von LastHour: Jeder Meilenstein hat einen Pending- und
einen Complete-Place; Voraussetzungen sind Lesekanten.

```
ALARM_OFF → CAUSES_FOUND ─┬→ HANDBOOK_READ → WORD_CLASSES_SORTED ─┐
                          │                                       ├→ F04_REPAIRED ─┐
                          └→ HATCH_UNLOCKED → AFFIXES_SORTED → A12_REPAIRED ───────┴→ PRODUCTION_RESTARTED
```

F-04 setzt A-12 voraus, weil die Flexionsmaschine erst mit korrekt sortierten Affixen
funktioniert. `A12_REPAIRED` wird erst mit dem Hebelzug abgeschlossen; dass die Eingabeschächte
befüllt sind, merkt sich das Flag `shafts-filled` in `MorphCoState`. Beobachtungen ohne Gating
(gelesene Plakate, Förderband, Papierkorb, …) sind Flags in `MorphCoState`.

Auch Sicherungsbox und Zirkumfixe sind keine Meilensteine. Wie die Eingabeschächte sind sie
Bedingungen neben einem Meilenstein und lassen sich in beliebiger Reihenfolge erledigen: Die Tür
zum Flur öffnet sich, sobald `CAUSES_FOUND` erreicht und das Flag `fuse-box-wired` gesetzt ist;
der Hebel schließt `A12_REPAIRED` erst ab, wenn die Schächte befüllt und alle drei Zirkumfixe
montiert sind (`MorphCoState.assembled`, im Speicherstand enthalten).

# 6. Questlog

Das Questlog wird aus dem Zustand abgeleitet (`MorphCoQuestLog.sync`). Jede Aufgabe hat ein
Kästchen, das ein Häkchen bekommt, sobald sie erledigt ist; ein geänderter Eintrag behält seinen
ursprünglichen Zeitstempel. Die Tabs von F-04 und A-12 enthalten je eine Checkliste: oben die
Reparatur, darunter eingerückt ihre Schritte, so wie das Skript sie als Unterpunkte nennt.

| Tab | Einträge |
| --- | --- |
| Auftrag | Alarm, Ursachen feststellen, Sicherungsbox verkabeln (sobald der PC gelesen ist), A-12 reparieren, F-04 reparieren, Produktion neu starten |
| F-04 Flexionsstraße | F-04 reparieren mit den Unterpunkten Handbuch gelesen, Flexionsmaschine überprüfen (darunter: Wortarten identifiziert), Affix-Sortieranlage inspizieren |
| A-12 Affix-Sortieranlage | A-12 reparieren mit den Unterpunkten Anlage inspizieren, Affixe sortieren, Zirkumfixe montieren (sobald Teile genommen, Zirkumfixe montiert oder Schächte befüllt sind), mit dem Hebel neu starten, zur Flexionsmaschine gehen |
| Indizien | fehlerhafte Wortformen, Mängelmeldung, Handbuch (aufklappbar), Morphemtypen Teil 1/2 und Handout (Verweis aufs Inventar), Display, Schlossfrage, Zirkumfix-Teile |
| Hinweise | jede gehörte Stufe der Technik-Hotline („Hinweis“, „Tipp“, „Lösung“), der neueste oben |

Die Hinweise haben einen eigenen Tab statt eines Eintrags im passenden Aufgaben-Tab: Die anderen
Tabs sind Aufgabenlisten, in denen Kästchen abgehakt werden. Im eigenen Tab steht der
Verlauf der Hotline am Stück, und man sieht auf einen Blick, wie viel Hilfe man schon geholt hat.

# 7. Achievements

| ID | Name | Bedingung |
| --- | --- | --- |
| `morphco_alarm_off` | Wuuuuuuh-o, wuuuuuh-o | Alarm aus |
| `morphco_fuse_box` | Unter Strom | alle Kabel der Sicherungsbox eingesteckt |
| `morphco_word_classes` | Kistenweise Wortarten | Wortarten-Kisten gelöst |
| `morphco_marie_kondo` | Marie Kondo wäre stolz auf dich | A-12 repariert |
| `morphco_flexion` | In Form gebracht | F-04 repariert |
| `morphco_combinatorial` | Kombinatorisch 1A! | alle drei Zirkumfixe montiert |
| `morphco_restart` | All systems nominal | Produktion neu gestartet |
| `morphco_first_try` (versteckt) | Schnitt sitzt | Affixe im ersten Versuch richtig |
| `morphco_plants` (versteckt) | Was hast du erwartet? | alle 3 Pflanzen |
| `morphco_lever` (versteckt) | Hnnnnnnnnnng | 5× am klemmenden Hebel ziehen (Eingabeschächte oder Zirkumfix-Magazin leer) |
| `morphco_nobody_home` (versteckt) | Niemand zu Hause | Wachschutz (11) und Rosi (17 oder Handy) anrufen |
| `morphco_posters` (versteckt) | Aushangkundig | alle 7 Plakate |
| `morphco_bookworm` (versteckt) | Leseratte | alle 8 Bücher |
| `morphco_platinum` | Mitarbeitende des Monats | alle anderen |

Die Bilder sind vorhandene Sprites des Projekts (wie bei Soulweaver).

# 8. Technik

- **Server/Client:** Der Server entscheidet über Fortschritt, Türen, Items und Dialoge. Eigene
  Dialogtypen (`MorphCoDialogs`) werden auf Server und Client registriert. Die UIs prüfen lokal für
  sofortiges Feedback und senden jede Eingabe als `StringValue` an den Server
  (`MorphCoDialogs.SUBMIT`). Der Kartenleser am Eingang ist der Kartenleser der Engine
  (`feature.interaction.cardreader`): Der Raum registriert für dessen Dialogtyp `CARD_READER` sein
  eigenes Aussehen; Messung und Bewertung des Durchziehens kommen aus der Engine. Er schickt jedes
  Durchziehen über `CardReaderFactory.SWIPE` statt über `MorphCoDialogs.SUBMIT`. Die Sicherungsbox
  ist das Kabel-Rätsel der Engine (`feature.interaction.cables`) im Aussehen des Raums:
  `MorphCoDialogs` registriert `FuseBoxUI` für den Dialogtyp `CABLES`, der Server prüft die fertige
  Verkabelung und schließt danach die Boxen der anderen Spieler.
  `MorphCoSnapshotTranslator` überträgt Kollider und Interaktionsreichweiten.
- **Zuordnen:** Wortarten-Kisten, Affix-Fächer, Eingabeschächte und Zirkumfixstation sortieren auf
  dem Zuordnungs-Board des Engine-Features `feature.interaction.sorting` (`SortBoard`, im Aussehen
  der Halle `MorphCoSortBoard`). Rätselmodelle, Prüfungen und Antworten bleiben im Raum, deshalb
  behalten die vier Dialoge ihre eigenen Dialogtypen.
- **Lokalisierung:** Alle Texte stehen in `game/assets/language/morphCo/de.json` und `en.json`.
  Der Server verschickt Schlüssel (`MorphCoText.key`), `MorphCoTranslator` löst sie auf dem Client
  auf. Die deutschen Beispielwörter bleiben auch in der englischen Fassung deutsch.
- **Speichern:** `MorphCoSave` schreibt nach jedem Meilenstein, nach den Eingabeschächten, nach der
  Sicherungsbox und nach jedem montierten Zirkumfix `morph-co-save.json` (Meilensteine, Flags,
  montierte Zirkumfixe, Spielzeit, Run-ID, Tracking-Einwilligung). Gehörte Hotline-Stufen sind Flags
  `hint:<schritt>:<stufe>` und werden nach jedem Hinweis gespeichert; ein Stand ohne solche Flags
  beginnt bei Stufe 1. „Fortsetzen“ stellt den Stand wieder her; Inventare werden nicht gespeichert.
  Der Krimskrams enthält alle Zirkumfix-Teile, die weder montiert sind noch von einem Spieler
  getragen werden, etwa nach dem Laden oder wenn ein Spieler das Spiel verlassen hat.
  Zirkumfix-Teile lassen sich deshalb nicht ablegen. Wer wieder beitritt, bekommt seine Teile nicht
  zurück; sie liegen im Krimskrams, solange niemand sie genommen hat. Die Bände „Morphemtypen“ und
  das Handout merkt sich der Zustand als Flags `carried:<dokument>:<spieler>`; wer nach dem Laden
  mit demselben Namen beitritt, bekommt sie zurück (das Handout ersetzt beide Bände).
- **Tracking:** Puzzle-Starts, Lösungen, Versuche (`assignment`, `segmentation`, `number`, `choice`,
  `pull` für den Hebel, `plug` und `wiring` für die Sicherungsbox) und Hinweise über
  `MorphCoTracking`: die der Schichtleitung als Schritt (`hint-alarm`), die der Technik-Hotline mit
  Stufe (`hint-alarm:direction`, `:tip`, `:solution`).
- **Telefon:** Das Telefon nutzt das Engine-Feature `feature.interaction.phone`. `ControlRoom`
  legt seine Leitungen an und hängt an jede die Reaktion des Raums, an die 42 die Technik-Hotline.
  Der Raum registriert für den Dialogtyp `PHONE` sein eigenes Aussehen (`PhoneUI` mit
  Mailbox-Lampe, Ziffernblock `KeypadNode` im Aussehen `PHONE`); der Dialog zeigt „Kein Anschluss“
  sofort. Jede Nummer geht an den Server; er schlägt sie erneut nach. Bei einer bekannten Nummer
  antwortet die Leitung über `PhoneCall` im offenen Telefon: `say` für Ansagen, `ask` für die
  Rückfrage der Hotline, deren Ja an den Server zurückgeht. Die Antwort geht nur an den Client des
  Anrufers und erscheint dort unter dem Apparat. Hinweistexte kommen als Schlüssel beim Client an.
- **Level:** `game/assets/levels/morphCo/morphco_1.level` (Design `MORPH_CO`). Alle Objekte sind
  Prefab-Instanzen `morphco-object` und werden im Level-Editor (Prefab-Modus) platziert; die
  Controller hängen ihr Verhalten per Instanzname an. Benannte Punkte gibt es nur für Türen und
  Auslöseflächen. `MorphCoLevelFileTest` prüft, dass jedes vom Code genutzte Objekt im Level steht,
  jede Objektart bekannt ist und alle Bilder existieren.

## Abweichungen vom Skript

- Der PC lässt sich erst nach dem Alarm sinnvoll lesen. Die Tür zum Flur öffnet sich mit der
  verkabelten Sicherungsbox, auch wenn der PC noch nicht gelesen ist.
- Die vier Aufgaben stehen wie im Skript direkt nach dem PC im Questlog, dazu „Sicherungsbox
  verkabeln“; „Produktion neu starten“ wird konkret, sobald F-04 und A-12 laufen.
- Die letzte Zeile der Wortarten-Tabelle („und diese schmecken gut“) wird als Fortsetzung von
  Satz 2 gezeigt.
- Neben der Skriptlösung werden die feineren Segmentierungen *sag·te·n* und *ge·regne·t*
  akzeptiert.
- Zusätzliche, nicht versteckte Achievements: Kistenweise Wortarten, In Form gebracht, All systems
  nominal, Unter Strom.
- Die Statuszeilen des Neustarts tippt der PC nacheinander als Terminal; die Schwarzblende danach
  zeigt sie noch einmal.
- Die Sicherheitskarte liegt im Spind neben dem Lesegerät. Sie ist kein Inventargegenstand: Der
  Raum merkt sich, dass die Karte geholt ist, und die Tür bleibt nach dem ersten Durchziehen für
  alle offen. Das Lesegerät öffnet die Tür nach einem Swipe-Card-Minigame (wie in Among Us): zu
  schnell, zu langsam oder nicht ganz durchgezogen heißt nochmal.
- Im Papierkorb in F-04 wühlt man sich durch zerknüllte Zettel; jeder zeigt beim Überfahren mit
  der Maus ein Ausschusswort, ein Klick wirft ihn hinaus. Darunter liegt die gefaltete
  Mängelmeldung. Sie bleibt ein optionales Indiz im Questlog.
- Die Konsole öffnet eine Nahansicht mit Knöpfen, Reglern und Alarmlampe; neben „Nerviger
  Alarmton“ gibt es einige Scherz-Knöpfe. Ihre Rückmeldungen stehen im Display der Konsole.
- Am Telefon wählt man eine Nummer, statt eine Option aus einer Liste zu nehmen. Neu sind die
  Technik-Hotline mit gestuften Hinweisen, Rosis Durchwahl auf dem Schichtplan, ihre absurd lange
  Handynummer auf dem Zettel und die Bandansage zu den Fehlercodes.
- Die Sicherungsbox im Kontrollraum ist ein zusätzliches Pflicht-Minigame: Jemand hat die Kabel
  herausgezogen, deshalb hat die Türsteuerung zum Flur keinen Strom. Konsole, PC und Telefon laufen
  auf der Notstrombatterie. Der PC nennt die Sicherungsbox im Anlagenstatus. Notbeleuchtung und
  Achievement gibt es für beide Spieler.
- Die Zirkumfixmontagestation ist Pflicht: Fehler 204 hat die Zirkumfixe von A-12 zerlegt, ihre
  Teile liegen im Krimskrams. Erst wenn alle drei Zirkumfixe montiert im Zirkumfix-Magazin liegen,
  lässt sich A-12 mit dem Hebel neu starten.
- Die Affix-Sortieranlage hat eine Nahansicht mit Display, Hebel, Wartungsklappe und Schloss. Jede
  ihrer Klickflächen in der Welt öffnet sie und hebt den angeklickten Teil kurz hervor. Der kleine
  Bildschirm daneben zeigt dieselbe Statuszeile wie der PC. Das Schloss
  ist ein Ziffernblock statt eines Eingabefelds.
- Nach Eingabeschächten und Zirkumfixen läuft A-12 erst nach einem gleichmäßigen Zug am Hebel
  wieder (Minigame wie beim Kartenleser: zu schnell, zu langsam oder nicht ganz unten heißt
  nochmal). Vorher klemmt der Hebel wie im Skript.
- Das Hamsterbuch im Regal von A-12 enthält alle 100 Witze in sechs Kapiteln und die
  Hamsterweisheit des Tages. Es schlägt bei jedem Öffnen einen zufälligen Witz auf, der sich erst
  wiederholt, wenn alle gezeigt wurden, und lässt sich ganz durchblättern.
- Zusätzliche Kleinigkeiten: Spiegel im WC, Kisten in A-12, Hinweise der Schichtleitung,
  versteckte Achievements.
- Der Alarm ist eine Sirene, die beim Ausschalten verstummt. Die Tür zum Kontrollraum bleibt offen;
  hinter ihr klingt die Sirene trotzdem nur noch etwa halb so laut.

# 9. Fachliche Fragen und Entscheidungen der Autorin

Ein fachliches Review hat folgende Punkte im Skript bzw. Handout markiert. Die Autorin hat dazu
Folgendes entschieden:

1. **Frage:** Das Handbuch schloss neben Präfixen auch Zirkumfixe von der Flexionsmaschine aus,
   die Eingabeschächte verlangen aber {ge- ... -et} unter Flexion (und das Handout führt
   {ge-}+{-t} als Flexionsaffix). **Entscheidung:** Der Satz ist gekürzt auf
   „Präfixe gehören daher nicht in die Flexionsmaschine!“.
2. **Frage:** *selten* blieb liegen, *spät* kommt zu den Adjektiven; beide sind hier adverbial
   gebrauchte Adjektive (seltener, später). **Entscheidung:** *selten* ist durch das Adverb *immer*
   ersetzt: „Ein Hamster kommt immer zu spät“. *immer* bleibt liegen, *spät* bleibt Adjektiv.
3. **Frage:** Handout: {nimm} ist selbst eine Imperativform; gemeint ist vermutlich {nehm}.
   {-thek} ist eher ein Konfix (Bibliothek, Videothek) als unikal. {be-}+{-t} (*berühmt*) ist
   umstritten (*beherzt*, *bebrillt* wären klarer). **Entscheidung:** {nimm}, {-thek} und
   *berühmt* bleiben.
4. **Frage:** {be- ... -ig} (*begierig*) wird meist als *Begier* + {-ig} analysiert; *gehen, ging,
   gegangen* ist eher Suppletion als ein reines Ablautbeispiel (*singen, sang, gesungen*).
   **Entscheidung:** *begierig* und *gehen, ging, gegangen* bleiben.
5. **Frage:** Die Schlossfrage nennt 8 als allgemeine Zahl; sie gilt für die Klassifikation des
   Handouts. **Entscheidung:** Die Antwort bleibt 8, die Zahl stammt aus dem zugrunde liegenden
   Seminar und dem Handout. Die Frage am Schloss weist darauf hin, dass das passende Handout
   irgendwo in der Fabrik zu finden ist.

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

Geprüft werden die Rätselmodelle, das Petri-Netz, Speicherstände, die Stufen der Technik-Hotline,
Sprachdateien (Schlüssel- und Platzhaltergleichheit, alle im Code genutzten Schlüssel),
Achievements und die Level-Datei.
