---
title: "Daten für Aufgabendefinitionen"
---

## Aufgabendefinition

Im Aufgabenkonzept werden [Aufgabentypen](../tasks/readme.md#aufgabentypen) definiert.
Welche Daten zur Definition einer Aufgabe nötig sind, ist vom Aufgabentyp abhängig. Ziel
dieses Dokuments ist, die erforderlichen Datenstrukturen für alle Aufgabentypen zu
definieren.

## Aufgabentyp “Single Choice”

```
single_choice_task task {
  description: "Bitte wähle die richtige Antwort aus!"
  answers: ["a", "b", "c"],
  correct_answer_index: 0,
  fn_score: score
}
```

Member:

- `description`: Die textuelle Aufgabenbeschreibung
- `answers`: Die Liste der Antwortmöglichkeiten, aus denen ein Element ausgewählt werden
  muss
- `correct_answer_index`: Der Index aus `answers`, er die korrekte Antwort angibt
- `fn_score`: Die [Scoring-Funktion](../control_mechanisms/reporting.md#scoring-funktion)

## Aufgabentyp “Multiple Choice”

```
multiple_choice_task task {
  description: "Bitte wähle die richtigen Antworten aus!"
  answers: ["a", "b", "c", "d"],
  correct_answer_indices: [0, 2], // kann auch leer bleiben
  fn_score: score
}
```

Member:

- `description`: Die textuelle Aufgabenbeschreibung
- `answers`: Die Liste der Antwortmöglichkeiten, aus denen mehrere Elemente ausgewählt
  werden müssen
- `correct_answer_indices`: Die Indizes aus `answers`, welche die korrekten Antworten
  angeben; falls `correct_answer_indices` nicht explizit definiert wird (also ein leeres
  Array bleibt), gilt keine der Antwortmöglichkeiten in `answers` als korrekt
- `fn_score`: Die [Scoring-Funktion](../control_mechanisms/reporting.md#scoring-funktion)

## Aufgabentyp "Ersetzen"

```
replacement_task t {
  description: "Bitte führe Ersetzungen durch!"
  initial_element_set: ["elem1", "elem2", "elem6"]
  elements: {
    n1: ("elem1", "elem2),
    n2: {"elem1", "elem3"},
    n3: {"elem4"},
    n4: {"elem5"},
    n5: {"elem1", "elem2", "elem6"},
    n6: {"verwirrungselement"}
  },
  rules: {
    r1: n1 -> n3,
    r2: n2 -> n4,
    r3: n5 -> n3
  },
  answer_sequence: [rules.r3], // Reihenfolge könnte auch egal sein, alternativen zulassen
  answer_configuration: ["elem4"], // hier auch Alternativen zulassen
  fn_score: score
}
```

TODO: Überprüfung, ob Elemente, die beide "h" heißen, auch beide aufs gleiche Objekt verweisen
TODO: Mengendefinition als `elements`-Feld angeben, `rules` nur für Ersetzungsregeln nutzen

TODO: Aus den Ersetzungsregeln könnte ein Baum erstellt werden, daraus kann theoretisch eine initiale Menge ausgerechnet
werden -> erstmal selbst definieren, FALLS ZEIT können wir das noch testen -> Ticket: later

Member:

- `description`: Die textuelle Aufgabenbeschreibung
- `elements`: Eine Liste, die alle Elemente, welche an der Ersetzungsaufgabe beteiligt sind,
  enthält.
- `initial_element_set`: Eine Liste, welche die initiale Menge der Aufgabenelemente angibt.
- `elements`: Definition der Element-Mengen: definiert, welche Elemente Mengen
  bilden, die durch Anwendung einer Ersetzungsregel durch eine andere Menge ersetzt
  werden können; runde Klammern `(` `)` geben an, dass die Element-Reihenfolge **relevant** ist,
  geschweifte Klammern `{` `}` geben an, dass die Element-Reihenfolge **irrelevant** ist
- `rules`: Definition der Ersetzungsregeln: Definition, welche Element-Mengen durch welche anderen
  Element-Mengen ersetzt werden dürfen; `n1 -> n2` bedeutet, dass die Menge `n1` durch `n2` ersetzt,
  werden kann, allerdings nicht andersherum
- `answer_sequence`: Liste der Ersetzungsregeln aus `rules`, die der Reihe nach ausgeführt
  werden müssen; alternative Lösungen anzugeben ist möglich
- `answer_configuration`: Liste der Elemente, welche nach Fertigstellung der Aufgabe
  vorhanden sein müssen; alternative Lösungen anzugeben ist möglich
- `fn_score`: Die [Scoring-Funktion](../control_mechanisms/reporting.md#scoring-funktion)


## Aufgabentyp "Zuordnen"

```
mapping_task t {
  description: "Bitte ordne Elemente einander zu!"
  elements_A: [elem1, elem2, elem3],
  elements_B: [elem4, elem5, elem6],
  rules: graph {
    // Definition der Elemente-Mengen
    n1[elements=[elements_A[0]]
    n2[elements=[elements_A[1]]
    n3[elements=[elements_A[2]]
    n4[elements=[elements_B[0]]
    n5[elements=[elements_B[2]]

    // Definition Zuordnung
    n1 -> n4
    n2 -> n4
    n3 -> n5
  },
  fn_score: score
}
```

Member:

- `description`: Die textuelle Aufgabenbeschreibung
- `elements_A`: Liste der Elemente, die in Menge $A$ (vgl.
  [Zuordnen](../tasks/readme.md#zuordnen)) enthalten sind
- `elements_B`: Liste der Elemente, die in Menge $B$ enthalten sind
- `rules`: Die Definition der Zuordnung, als `graph` notiert
  - Definition der Element-Mengen: definiert, welche Elemente aus `elements_A` und
    `elements_B` eine Menge bilden, die einer anderen Menge zugeordnet werden kann; als
    Knoten im `graph` notiert
  - Definition der Zuordnung; als Kanten im `graph` notiert
- `fn_score`: Die [Scoring-Funktion](../control_mechanisms/reporting.md#scoring-funktion)

### Zuordnung: Alternative Notation

Als alternative Notation zur Definition der Zuordnung ist folgende Notation vorstellbar:

```
...
  rules: graph {
    // Definition Zuordnung
    elements_A[0] -> elements_B[0]
    elements_A[1] -> elements_B[0]
    elements_A[2] -> elements_B[2]

    // Alternativ: Menge von Tupeln definieren

    ("a", "b")
    ("x", "y")
    ("z", "y")

    ("c", _)
    (_, "w")
    // falls doch schon mal verwendet: Warning ausgeben
  },
...
```

Dies würde allerdings die Erweiterung der eingebetteten Dot-Syntax erfordern, sodass
beliebige Ausdrücke als Knoten in Kantendefinitionen verwendet werden können.

TODO:
- evtl. keine Mengen A und B angeben, sondern direkt Elemente aus Tupel auslesen
- wie Verwirrungselemente einbauen?
  - Tupel mit nur einem Element erlauben? (a,_) (_,a) -> ist wahrscheinlich die einfachste Form
  - leeres Element erfordert dann gesondertes Token
  - diese Tupel haben dann eine Sonderrolle -> semantische Sonderbehandlung
  - einfach noch eine Menge zusätzlicher Elemente angeben, die dann einfach noch in Mengen integriert werden?

## Aufgabentyp “Lücken füllen”

```
gap_task task {
  description: "Bitte fülle die Lücken!"
  gaps_amount: 4, // erzeugt implizit ein Lücken-array mit 4 Elementen
  elements: [elem1, elem2, elem3, elem4],
  rules: graph {
    // Definition der Elemente-Mengen
    n1[elements=[gaps[0]]
    n2[elements=[gaps[1]]
    n3[elements=[gaps[2]]

    n4[elements=[elements[0]]
    n5[elements=[elements[1]]
    n6[elements=[elements[2]]
    n7[elements=[elements[3]]

    // Definition Zuordnung
    n4 -> n1
    n5 -> n2
    n6 -> n2
    n7 -> n3
  },
  fn_score: score
}
```

Member:

- `description`: Die textuelle Aufgabenbeschreibung
- `gaps_amount`: Anzahl der Lücken, die gefüllt werden müssen; hierdurch wird intern ein
  `gaps`-Array erzeugt, welches die Lücken repräsentiert
- `elements`: Liste der Elemente, welche in die Lücken eingesetzt werden müssen
- `rules`: Die Definition der Zuordnung, als `graph` notiert
  - Definition der Element-Mengen: definiert, welche Elemente aus `gaps` und `elements` eine
    Menge bilden, die einer anderen Menge zugeordnet werden kann; als Knoten im `graph`
    notiert
  - Definition der Zuordnung; als Kanten im `graph` notiert
- `fn_score`: Die [Scoring-Funktion](../control_mechanisms/reporting.md#scoring-funktion)


### Lücken füllen: Alternative Notation (vgl. [Zuordnen: alternative Notation](#zuordnung-alternative-notation))

```
rules: graph {
  // Definition Zuordnung
  elements[0] -> gaps[0]
  elements[1] -> gaps[1]
  elements[2] -> gaps[1]
  elements[3] -> gaps[2]
},
```

TODO: linke Seite von Tupel wird zu Regex, rechte Seite bleibt

Für Freitext könnte das auch genutzt werden -> also entweder auch Regex oder LLM-Aufruf
als native Funktion einbauen -> Ticket:Later
