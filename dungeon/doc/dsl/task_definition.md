---
title: "Aufgabendefinition"
---

Dieser Teil der Dokumentation beschreibt, wie Aufgaben definiert werden. Es werden konfigurierbare
Eigenschaften und die entsprechenden [Datentypen](TODO: Datentyp-Doku) erläutert.
Zum jetzigen Zeitpunkt sind drei Aufgabentypen verfügbar: Single Choice, Multiple Choice
und Zuweisungsaufgaben.

## Single Choice Aufgaben

Single Choice Aufgaben werden wie folgt definiert (nicht alle Eigenschaften müssen zwingend definiert werden):

```
single_choice_task my_task {
    description: "Dies ist der Aufgabentext",
    answers: ["Antwort1", "Antwort2", "Antwort3"],
    correct_answer_index: 1,
    points: 1,
    points_to_pass: 1,
    explanation: "Dieser Text wird angezeigt, falls die Aufgabe falsch beantwortet wird",
    grading_function: grade_single_choice_task,
    scenario_builder: my_scenario_builder
}
```

### Beschreibung der Eigenschaften

| Name                   | Datentyp                                           | Funktion                                                                                                                                                           | Default-Wert                                                                                              |
|------------------------|----------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| `description`          | `string`                                           | Der Aufgabentext, der im Spiel als Arbeitsanweisung angezeigt wird.                                                                                                | `""` (leerer `string`)                                                                                    |
| `answers`              | `string[]` (Liste von `string`-Werten)             | Die Antwortmöglichkeiten für die Aufgabe                                                                                                                           | `[]` (leere Liste)                                                                                        |
| `correct_answer_index` | `int`                                              | Der Index der korrekten Antwortmöglichkeit aus `answers` (beginnt bei `0`)                                                                                         | `0`                                                                                                       |
| `points`               | `float`                                            | Wie viele Punkte können mit dieser Aufgabe maximal erreicht werden?                                                                                                | `1.0`                                                                                                     |
| `points_to_pass`       | `float`                                            | Wie viele Punkte müssen erreicht werden, um die Aufgabe zu bestehen?                                                                                               | `1.0`                                                                                                     |
| `explanation`          | `string`                                           | Ein optional konfigurierbarer Erklärungstext, der angezeigt wird, falls die Frage falsch beantwortet wird. Falls nicht konfiguriert, wird er auch nicht angezeigt. | `""` (leerer `string`)                                                                                    |
| `grading_function`     | `fn (single_choice_task, task_content<>) -> float` | Eine Referenz auf eine Funktion, die zur Bewertung der Aufgabe verwendet wird.                                                                                     | [`grade_single_choice_task`](TODO: Link auf Doku zu nativer Funktion)                                     |
| `scenario_builder`     | `fn (single_choice_task) -> entity<><>`            | Eine Referenz auf eine Funktion, die aus der Aufgabendefinition ein konkretes Spielszenario erstellt, muss nicht konfiguriert werden.                              | `NONE` (keine Funktionsreferenz, siehe [Szenario-Builder](scenario_builder.md) für weitere Informationen) |

## Multiple Choice Aufgaben

Multiple Choice Aufgaben werden wie folgt definiert (nicht alle Eigenschaften müssen zwingend definiert werden):

```
multiple_choice_task my_task {
    description: "Dies ist der Aufgabentext",
    answers: ["Antwort1", "Antwort2", "Antwort3"],
    correct_answer_indices: [1,2],
    points: 1,
    points_to_pass: 1,
    explanation: "Dieser Text wird angezeigt, falls die Aufgabe falsch beantwortet wird",
    grading_function: grade_multiple_choice_task,
    scenario_builder: my_scenario_builder
}
```

### Beschreibung der Eigenschaften

| Name                     | Datentyp                                             | Funktion                                                                                                                                                           | Default-Wert                                                                                              |
|--------------------------|------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| `description`            | `string`                                             | Der Aufgabentext, der im Spiel als Arbeitsanweisung angezeigt wird.                                                                                                | `""` (leerer `string`)                                                                                    |
| `answers`                | `string[]` (Liste von `string`-Werten)               | Die Antwortmöglichkeiten für die Aufgabe                                                                                                                           | `[]` (leere Liste)                                                                                        |
| `correct_answer_indices` | `int[]` (Liste von `int`-Werten)                     | Die Indizes der korrekten Antwortmöglichkeiten aus `answers` (beginnt bei `0`)                                                                                     | `[]` (leere Liste                                                                                         |
| `points`                 | `float`                                              | Wie viele Punkte können mit dieser Aufgabe maximal erreicht werden?                                                                                                | `1.0`                                                                                                     |
| `points_to_pass`         | `float`                                              | Wie viele Punkte müssen erreicht werden, um die Aufgabe zu bestehen?                                                                                               | `1.0`                                                                                                     |
| `explanation`            | `string`                                             | Ein optional konfigurierbarer Erklärungstext, der angezeigt wird, falls die Frage falsch beantwortet wird. Falls nicht konfiguriert, wird er auch nicht angezeigt. | `""` (leerer `string`)                                                                                    |
| `grading_function`       | `fn (multiple_choice_task, task_content<>) -> float` | Eine Referenz auf eine Funktion, die zur Bewertung der Aufgabe verwendet wird.                                                                                     | [`grade_multiple_choice_task`](TODO: Link auf Doku zu nativer Funktion)                                   |
| `scenario_builder`       | `fn (multiple_choice_task) -> entity<><>`            | Eine Referenz auf eine Funktion, die aus der Aufgabendefinition ein konkretes Spielszenario erstellt, muss nicht konfiguriert werden.                              | `NONE` (keine Funktionsreferenz, siehe [Szenario-Builder](scenario_builder.md) für weitere Informationen) |

## Zuordnungsaufgaben

Zuordnungsaufgaben werden wie folgt definiert (nicht alle Eigenschaften müssen zwingend definiert werden):

```
assign_task my_task {
    description: "Dies ist der Aufgabentext",
    solution: <
        ["Term1", "Definition1"],
        ["Term1", "Definition2"],
        ["Term2", "Definition3"],
        [_, "Definition4"],
        ["Term3", _]
        >,
    points: 1,
    points_to_pass: 1,
    explanation: "Dieser Text wird angezeigt, falls die Aufgabe falsch beantwortet wird",
    grading_function: grade_assign_task_easy,
    scenario_builder: my_scenario_builder
}
```

### Beschreibung der Eigenschaften

| Name               | Datentyp                                             | Funktion                                                                                                                                                           | Default-Wert                                                                                              |
|--------------------|------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| `description`      | `string`                                             | Der Aufgabentext, der im Spiel als Arbeitsanweisung angezeigt wird.                                                                                                | `""` (leerer `string`)                                                                                    |
| `solution`         | `string[]<>`                                         | Die Lösung der Zuordnungsaufgabe (siehe Erläuterung unter der Tabelle)                                                                                             | `<>` (leeres Set)                                                                                         |
| `points`           | `float`                                              | Wie viele Punkte können mit dieser Aufgabe maximal erreicht werden?                                                                                                | `1.0`                                                                                                     |
| `points_to_pass`   | `float`                                              | Wie viele Punkte müssen erreicht werden, um die Aufgabe zu bestehen?                                                                                               | `1.0`                                                                                                     |
| `explanation`      | `string`                                             | Ein optional konfigurierbarer Erklärungstext, der angezeigt wird, falls die Frage falsch beantwortet wird. Falls nicht konfiguriert, wird er auch nicht angezeigt. | `""` (leerer `string`)                                                                                    |
| `grading_function` | `fn (multiple_choice_task, task_content<>) -> float` | Eine Referenz auf eine Funktion, die zur Bewertung der Aufgabe verwendet wird.                                                                                     | [`grade_multiple_choice_task`](TODO: Link auf Doku zu nativer Funktion)                                   |
| `scenario_builder` | `fn (multiple_choice_task) -> entity<><>`            | Eine Referenz auf eine Funktion, die aus der Aufgabendefinition ein konkretes Spielszenario erstellt, muss nicht konfiguriert werden.                              | `NONE` (keine Funktionsreferenz, siehe [Szenario-Builder](scenario_builder.md) für weitere Informationen) |

### Definition der Lösung

In einer Zuordnungsaufgabe werden Elementen aus einer Menge $A$ (z.B.
Antwortmöglichkeiten) die Elemente aus einer anderen Menge $B$ zuzuordnen.
Die Zuordnung muss dabei nach der Form
einer partiellen Funktion $f: A \rightharpoonup B$ erfolgen.

Das heißt:

- Jeder Antwortmöglichkeit $a \in A$ kann maximal einem Element $b \in B$ zugeordnet werden.
- Jedem Element $b \in B$ können $n$ Elemente $a \in A$ zugeordnet werden, wobei
  $0 \leq n \leq |A|$ ist

Die Lösung einer Zuordnungsaufgabe wird als Menge `<>` von `string`-Listen (`string[]`) definiert,
wobei jede `string`-Liste nur zwei Elemente beinhaltet (alle weiteren Elemente werden ignoriert).

Die Interpretation ist dabei wie folgt:
- das erste Element $b$ einer Liste gibt ein Element aus Menge $B$ an
- das zweite Element $a$ einer Liste gibt ein Element aus der Menge $A$ an
- durch die Nennung beider Elemente in einer Liste wird definiert, dass die Zuordnung von
  $a$ zu $b$ Teil der Lösung ist
- ist das erste Element `_`, wird das zweite Element zu $A$ hinzugefügt, wird allerdings
  in der Lösung keinem Element aus $B$ zugeordnet
- ist das zweite Element `_`, wird das erste Element zu $B$ hinzugefügt, wird allerdings
  in der Lösung keinem Element aus $A$ zugeordnet

Bildlich am Beispiel eines Szenarios ausgedrückt:
Die Menge $B$ stellt eine Menge an Truhen dar, denen die Schriftrollen aus Menge $A$ zugeordnet werden
müssen.
Gegeben sei folgende Lösungsdefinition:

```
...
  solution: <
      ["Elementaroperation", "anzahl = 0"],
      ["Elementaroperation", "sinus(30)"],
      ["Kontrollstruktur", "if"],
      ["Kontrollstruktur", "if - else"],
      ["Kontrollstruktur", "while"],
      ["Basisanweisung", _],
      [_, "Öffne die Tür."]
  >
...
```

Hieraus werden Truhen mit den folgenden Namen erstellt, denen Items aus $A$ zugeordnet werden müssen:
- "Elementaroperation"
- "Kontrollstrukturen"
- "Basisanweisung"

Aus der Definition werden Items mit folgenden Namen erstellt:
- "anzahl = 0"
- "sinus(30)"
- "if"
- "if - else"
- "while"
- "Öffne die Tür"

Folgende Zuordnung wir als korrekt interpretiert:
- Inhalt der Truhe "Elementaroperation":
  - "anzahl = 0"
  - "sinus(30"
- Inhalt der Truhe "Kontrollstrukturen":
  - "if"
  - "if - else"
  - "while"
- Die Truhe "Basisanweisung" ist leer
- Das Item "Öffne die Tür" ist keiner Truhe zugeordnet
