---
title: "Reporting-Konzept"
---

## Ziel

Das Ziel dieses Dokuments ist es, die Konzepte zum **Reporting** zentral zu sammeln.

**Reporting** bezeichnet den Prozess des Erfassens, Bewertens und Speicherns einer Antwort
auf eine Frage im Dungeon-Kontext.

## Beteiligte Komponenten

### Task-Definition

Die Task-Definition definiert die Aufgabe.

Beispiel:

```
single_choice_task my_task {
  answers: ["a", "b", "c"],   // die Antwortmöglichkeiten
  correct_answer: answers[0], // die korrekte Antwortmöglichkeit
  fn_score: score             // die Scoring-Funktion/Bewertungsfunktion
}
```

### Scoring-Funktion

Die **Scoring-Funktion** implementiert die Art, wie eine Aufgabe bewertet wird. Eine
beispielhafte DSL-Implementierung könnte so aussehen:

```
fn score(single_choice_task t, string a) -> float {
  if t.correct_answer == a
    return 1.0;
  else
    return 0.0;
}
```
Die **Scoring-Funktion** nimmt zwei Parameter entgegen: das Aufgabenobjekt (welches die
**Task-Definition** repräsentiert), und die gegebene Antwort (hier als `string`). Sie gibt
einen Fließkommawert zurück, der die Bewertung der Antwort repräsentiert: 1.0 = richtig, 0.0
= falsch.

Note: Der **Datentyp** der übergebenen Antwort (hier wird `string` verwendet) ist **noch
nicht final festgelegt**. Prinzipiell können mit `string` alle Antworten abgebildet werden
(da `int` und `float` in `string` konvertiert werden können), allerdings ist das für die
Überprüfung von Zahlwerten nicht optimal. Es ist auch ein `answer`-Datentyp vorstellbar, der
dieses Problem kapselt, wie das technisch genau funktionieren soll, ist allerdings noch
unklar.

### Szenario-Definition

Die **Szenario-Definition** ist dafür zuständig, die Verbindung zwischen der abstrakten
Aufgaben-Definition und der konkreten Umsetzung im Dungeon herzustellen. Hierzu zählt auch
Verknüpfung von Event-Handler-Funktionen mit Events im Kontext von Entitäten, um auf
bestimmte Ereignisse im Dungeon zu reagieren. Da die “Abgabe” einer Aufgabe im
Dungeon-Kontext durch viele szenario-abhängige Ereignisse abgebildet werden kann, muss auch
der Aufruf der **Scoring-Funktion** und weiterer Reporting-Logik in die Szenario-Definition
integriert werden.

```
fn handler_function(entity e) {
  // andere Event-Handler Logik

  report(e.properties.task, e.properties.task_answer_value)

  // andere Event-Handler Logik
}

fn report(task t, string a) -> float {
  // Aufruf der Scoring-Funktion, die mit der Aufgaben-Definition vom Parameter "t" verknüpft ist
  score = t.fn_score(t, a);

  // Speichern der Antwort mit Bewertungswert
  log(t, a, score);
}
```

Im obigen Listing werden zwei Funktionen definiert. die `handler_function` ist eine
Event-Handler-Funktion, die als Callback-Funktion für ein Ereignis im Kontext einer Entität
(z.B. ein Kollisionsereignis) verknüpft werden kann. Die `report`-Funktion wird als Teil der
Standard-Bibliothek oder als native Funktion des DSL-Interpreters umgesetzt, ruft die
**Scoring-Funktion** des `task`-Objekts auf und speichert das Ergebnis per `log`-Funktion
(welche ebenfalls als native Funktion implementiert wird).

Die obige Implementierung setzt voraus, dass die Entität, die der Handler-Funktion übergeben
wird, zwei Referenzen enthält:

1.  Eine Referenz auf die **Aufgabendefinition**, an deren konkreter Umsetzung die Entität
    beteiligt ist
2.  Eine Referenz auf die **Antwortmöglichkeit**, welche durch die Entität repräsentiert
    wird

Note: Um diese Referenzen zu halten, könnte ein `TaskReferenceComponent` (o.ä.) genutzt
werden, welches sowohl eine Referenz auf ein `Task`-Objekt und eine Referenz auf ein
`Answer`-Objekt hält.
