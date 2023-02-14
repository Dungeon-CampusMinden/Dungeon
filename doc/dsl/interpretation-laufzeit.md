## Überblick: Wie funktioniert die Interpretation allgemein

Der Interpretation sind die Schritte Lexing, Parsing, AST-Konvertierung und
die semantische Analyse vorgelagert.
Die semantische Analyse speichert alle Symbol- und Typinformationen in einer `IEnvironment`-Instanz,
welche den Übergabepunkt zur Interpretation des Programms markiert.

Die Schritte, welche anschließend während der Interpretation vom `DSLInterpreter` ausgeführt
werden, sind im folgenden Diagram dargestellt:

<p align="center">
<img src="img/interpretation_pipeline.png" width=50%>
</p>

Die Interpretations-Pipeline ist in zwei Phasen aufgeteilt, die Laufzeitinitialisierung und die Interpretation.

### Laufzeitinitialisierung

Zuerst lädt der `DSLInterpreter` die Symbol- und Typinformationen aus der übergebenen `IEnvironment`-Instanz
in ein `RuntimeEnvironment`.
Anschließend wird ein globaler `MemorySpace` erzeugt, welcher das Laufzeit-Äquivalent zu einem `Scope` darstellt.
In diesem globalen `MemorySpace` werden globale Definitonen von Funktionen und Objekten (bspw. von `quest_config`)
als `Value` gebunden. Die `Value`-Klasse wird verwendet um alle Werte und Objekte zu verwalten, die vom `DSLInterpreter`
während der Interpretation erzeugt und referenziert werden.
Die Assoziation einer `Value`-Instanz in einem `MemorySpace` mit einem Namen wird als "Binden" (engl.: binding) bezeichnet.
Die `Value`-Instanzen der Objekte haben zu diesem Zeitpunkt einen definierten default-Wert.

### Interpretation

Der erste Schritt der Interpretation (nach der Initialisierung) ist das Erzeugen von Prototypen für
`game_object`-Definitionen.

**Erzeugung von Prototypen**

Ein "Prototyp" ist eine Kombination aus einem `AggregateType` und vom Nutzer
per DSL konfigurierten Defaultwerten. Ein Beispiel für eine `game_object`-Definition:

```
game_object my_obj {
    velocity_component {
        x_velocity: 2.0,
        y_velocity: 3.0
    },
    animation_component {
        idle_left: "path/to/frames",
        idle_right: "path/to/frames"
    }
}
```

Aus dieser Definition erstellt der `DSLInterpreter` einen Prototypen, in dem bspw. die Werte für die konfigurierten Member
`x_velocity` und `y_velocity` gespeichert werden. Aus so einem Prototyp kann wie aus einem Datentyp eine Instanz
erstellt werden. In der Instanz eines Prototyps sind die konfigurierten Defaultwerte gesetzt. Dem folgenden
Objektdiagramm können die beteiligten Instanzen für das obere Beispiel entnommen werden:

![UML: Objektdiagram Prototype](img/prototype_objects.png)

Wie zu erkennen ist, wird für jede Komponenten-Definition auch ein `Prototype` erzeugt,
der jedoch nur im `Prototype` der `GameObjectDefinition` existiert. Der `Prototype` einer
Komponenten-Definition enthält die per DSL konfigurierten Defaultwerte der Komponente.

Die Erzeugung der Prototypen ist im folgenden Sequenzdiagramm dargestellt:

<p align="center">
<img src="img/create_prototype.png" width=50%>
</p>

In den Typdefinitionen, die vom `RuntimeEnvironment` für `getTypes` zurückgegeben werden, sind auch die
`game_object`-Definition enthalten.

Die referenzierte Sequenz `createComponentPrototype` ist im Folgenden dargestellt:

<p align="center">
<img src="img/create_component_prototype.png" width=50%>
</p>

**Evaluierung von Ausdrücken**

Um Prototypen zu erzeugen, müssen die rechtsseitigen Ausdrücke einer Eigenschaftszuweisung (z.B.
`x_velocity: 2.0`) evaluiert werden.
Im obigen Beispiel handelt es sich bei diesen Ausdrücken um triviale Dezimalzahlen, es könnte sich allerdings bei
rechtsseitigen Ausdrücken auch um Funktionsaufrufe, Verweise auf globale Objekte, etc. handeln (vgl. für
gültige Ausdrücke hierzu [Ausdrücke](sprachkonzepte.md#ausdrücke)).

Der `DSLInterpreter` ist ein [Tree-Walk-Interpreter](https://craftinginterpreters.com/a-tree-walk-interpreter.html),
läuft also über den AST und führt für jeden so besuchten Knoten Operationen aus. Für alle Knoten, die Teil eines
Ausdrucks sein können, erzeugt der Interpreter eine `Value`-Instanz, die den Wert des besuchten Knotens enthält.

Für `NumNode` wird bspw. einfach der Wert des AST-Knotens in ein `Value` verpackt:
```java
public Object visit(NumNode node) {
    return new Value(BuiltInType.intType, node.getValue());
}
```

Für einen `GameObjectDefinition`-Knoten, der Teil eines Ausdrucks ist, ist dieses Vorgehen deutlich komplexer und wird
unter [Typinstanziierung](#typinstanziierung) genauer erläutert. Allerdings wird auch für diesen Fall ein `Value`-Objekt
zurückgegeben.

**Anmerkung:**

Die im Folgenden beschriebenen Aspekte bzgl. `quest_config` als zentralem Übergabepunkt von DSL -> Dungeon sind WIP
und können sich daher noch grundlegend ändern (siehe hierzu [Issue #195](https://github.com/Programmiermethoden/Dungeon/issues/195)).

Der zentrale Übergabepunkt zwischen dem DSL Programm und dem Dungeon-Framework wird durch die
`quest_config`-Definition gebildet. Daher sucht der `DSLInterpreter` im nächsten Schritt die erste `quest_config`-Definition
aus einem DSL Programm heraus und evaluiert alle Eigenschaftszuweisungen. Hierdurch werden nur Objekte und Definitionen
evaluiert, die in Eigenschaftszuweisungen dieser `quest_config`-Definition referenziert werden.

Abschließend erzeugt der `DSLInterpreter` eine `QuestConfig`-Instanz und gibt diese an das Dungeon-Framework zurück.

# `MemorySpace`s und `Value`s

# Typinstanziierung

TODO:
- DSLInterpreter-Seite
- TypeInstantiator-Seite
- Was ist die Rolle von `EncapsulatedObject`

# Funktionsaufrufe

TODO:
- wie funktionieren Funktionsaufrufe allgemein?
- wie funktionieren native Funktionen?
- wie werden user defined funktionen behandelt?
- wie funktioniert die Funktionsschnittstelle für Event-Handler DSL-Funktionen, die der Dungeon aufrufen kann
- Wie funktionieren die Builder-Funktionen, um Tasks zu definieren?

# Welche Klassen (neben `DSLInterpreter`) sind beteiligt?

