

# Was bedeutet Typebuilding im Kontext der DSL?

Das Dungeon Framework verwendet einen komponentenbasierten Ansatz (vgl. [ECS](./../game/ecs.md)), um Entitäten im
Level zu definieren. Die DSL ermöglicht das Definieren von Enitäten in textueller Form (vgl. hierfür [Entitätsdefinition](./sprachkonzepte.md#entitätsdefinition)), indem für einen Entitätstypen festgelegt wird, welche Komponenten in ihm enthalten sein sollen. Hierbei können die Member der Komponenten konfiguriert werden. 

Ein Beispiel für einen Entitätstypen names `my_object`, der ein `PositionComponent` und ein `AnimationComponent` hat, könnte so aussehen:

```
game_object my_object {
    position_component {},
    animation_component {
        idle_animation: "path/to/idle/frames"
    }
}
```

Diese Form der Entitätsdefinition erfordert eine Repräsentation der Komponententypen (welche als Java-Klassen / Java-Records im ECS definiert sind) im DSL Typsystem und eine Verbindung des DSL Typen mit der Java-Klasse, dessen DSL-Equivalent er ist.

TODO: Diagram, das zeigt, wie Java-Klasse und DSL-Datentyp zusammenhängen

Um die DSL Typen, die auf diese Weise benötigt werden, nicht manuell implementieren zu müssen, übernimmt der `TypeBuiler` diese Aufgabe automatisch.
Hierzu wird ein Annotation-basierter Ansatz verfolgt.

# Beispiel(e) aus User-Sicht

Die wesentlichen Annotationen sind:
- `DSLType`: Markierung für die Java-Klasse / den Java-Record, für den ein DSL Typ erzeugt werden soll
- `DSLTypeMember`: Markierung für die Member der Java-Klasse / des Java-Records, welche in dem DSL Typ enthalten sein sollen

Eine Java-Klasse, für die ein DSL Typ generiert werden soll, könnte so aussehen:

```java
@DSLType
public class ComponentClass {
    @DSLTypeMember int member1;
    @DSLTypeMember String member2;
    @DSLTypeMember float member3;
    int member4;
}
```

Aus dieser Klasse kann mit der Methode `TypeBuilder::createTypeFromClass` ein DSL Typ erzeugt werden:

```java
TypeBuilder tp = new TypeBuilder();
AggregateType dslType = tp.createTypeFromClass(Scope.NULL, ComponentClass.class);
```

Der `dslType` enthält drei Symbole, welche `member1`, `member2` und `member3` representieren, die in `ComponentClass` markiert sind.
Die Datentypen sind `BuiltInType.intType`, `BuiltInType.stringType` und `BuiltInType.floatType` respektive.
Der so erzeugte Datentyp kann (nachdem er über ein `IEnvironment` geladen wurde, siehe [Laden von Datentypen](#laden-von-datentypen)) wie folgt in einer Entitätsdefinition verwendet werden:

``` 
game_ojbect my_obj {
    component_class {
        member1: 42,
        member3: 3.14,
        member2: "Hello, World!"
    }
}
```

## Typ- und Membernamen

Standardmäßig konvertiert der `TypeBuilder` die Namen der Java-Klassen in [snake case](https://en.wikipedia.org//wiki/Snake_case), um ein zu den restlichen DSL Keywords konsistentes Namensschema zu verfolgen. Alternativ akzeptieren `DSLType` und `DSLTypeMember` einen `name`-Parameter, der dieses Standardverhalten überschreibt. Für die oben bereits genutzte `ComponentClass` könnte dies entsprechend verwendet werden:

```java
@DSLType(name="my_component")
public class ComponentClass {
    @DSLTypeMember int member1;
    @DSLTypeMember(name="my_member") String member2;
    @DSLTypeMember float member3;
    int member4;
}
```

Der entsprechende Komponententyp könnte wie folgt in einer Entitätsdefinition verwendet werden:

```
game_ojbect my_obj {
    my_component {
        member1: 42,
        member3: 3.14,
        my_member: "Hello, World!"
    }
}
```

## Laden von Datentypen

Der mit `TypeBuilder::createTypeFromClass` erzeugte Datentyp muss in die [DSL Pipeline](./ueberblick.md#dsl-pipeline) integriert werden.
Hierzu muss der DSL Typ über ein `IEnvironment` Objekt geladen werden. Die Standard `IEnvironment`-Implementierung ist das `GameEnvironment` ([GameEnvironment.java](./../../dsl/src/runtime/GameEnvironment.java)), welches 
bereits alle BuiltIn-Datentypen und standardmäßig verfügbaren komplexeren Datentypen (Komponenten-Datentypen) enthält.

```java
// DSL Datentyp erzeugen
TypeBuilder tp = new TypeBuilder();
AggregateType dslType = tp.createTypeFromClass(Scope.NULL, ComponentClass.class);

// DSL Datentyp ins GameEnvironment laden
var env = new GameEnvironment();
env.loadTypes(new IType[] {dslType});

// Vorbereiten des SymbolTableParsers für die semantische Analyse, indem 
// das modifiziertei GameEnvironment geladen wird
SymbolTableParser symbolTableParser = new SymbolTableParser();
symbolTableParser.setup(env);

/*
 *  Parsing und AST Erzeugung aus einem DSL Programm (hier ausgelassen)
 */

// Ausführung der semantischen Analyse; die semantischen Informationen werden im 
// `IEnvironment` gespeichert, mit dem der `SymbolTableParsers` per `setup`-Methode
// vorbereitet wurde
symbolTableParser.walk(ast);

// Initialisierung der Laufzeitumgebung des DSLInterpreters mit dem `IEnvironment`,
// welches nun die semantischen Informationen des Programms enthält 
DSLInterpreter interpreter = new DSLInterpreter();
interpreter.initializeRuntime(env);

// Interpretation des DSL Programms
var questConfig = interpreter.generateQuestConfig(ast);
```

## TODO:

Weitere Annotationen mit speziellerem Anwendungsfall:
- `DSLContextPush`
- `DSLContextMember`

## Einschränkungen

Mit dem oben beschriebenen Mechanismus können DSL Datentypen aus Java-Klassen und Java-Records erstellt werden. Für beide Anwendungsfälle sind folgende Einschränkungen zu beachten:

### Einschränkungen Java-Klasse

Eine Java-Klasse, die mit `@DSLType` markiert wird, muss folgende Kriterien erfüllen:
- sie muss über einen Default-Konstruktor ohne Parameter verfügen
- falls sie über keinen Default-Konstruktor verfügt, muss sie über einen Konstruktor verfügen, dessen Parameter alle mit `@DSLContextMember` (siehe [Typinstanziierung](interpretation-laufzeit.md#typinstanziierung)) markiert sind
- die Datentypen aller Member, die mit `@DSLTypeMember` markiert sind, müssen entweder Datentypen sein, die mit `@DSLType` markiert oder adaptiert sind, oder sich auf die `BuiltIn`-Datentypen zurückführen lassen (siehe [Typsystem](typsystem.md))

Falls diese Kriterien nicht erfüllt sind, kann der `TypeInstantiator` keine Instanzen der Klasse anlegen. Für weitere Details siehe [Typinstanziierung](interpreation-laufzeit.md#typinstanziierung).

### Einschränkungen Java-Record

Ein Java-Record, der mit `DSLType` markiert ist, muss folgende Kriterien erfüllen:
- alle Member des Records müssen mit `DSLTypeMember` markiert sein
- die Datentypen aller Member müssen entweder Datentypen sein, die mit `@DSLType` markiert oder adaptiert sind, oder sich auf die `BuiltIn`-Datentypen zurückführen lassen (siehe [Typsystem](typsystem.md))

Falls diese Kriterien nicht erfüllt sind, kann der `TypeInstantiator` keine Instanzen der des Records anlegen. Für weitere Details (siehe  [Typinstanziierung](interpretation-laufzeit.md#typinstanziierung)). 

# Typadaptierung

Einige Komponenten des ECS (bspw. `AnimationComponent`) verwenden Datentypen für mit `@DSLTypeMember` markierte Member, die nicht direkt mit `@DSLType` markiert werden können. Dies kann unterschiedliche Gründe haben (bspw., dass der betroffene Datentyp in einem externen Projekt definiert ist), die allerdings für diese Dokumentation keine weitere Relevanz haben. 

Ein Beispiel ist im folgenden Snippet zu sehen. Hier verwendet `member2` einen Datentyp aus einer externen Bibliothek.
```java
import some.external.library.ExternalType;

@DSLType
public class Component {
    @DSLTypeMember int member1;
    @DSLTypeMember ExternalType member2;
}
```

Um diese Member trotzdem über die DSL konfigurierbar zu machen, kann ihr Datentyp 'adaptiert' werden. Hierzu kann eine statische Methode mit `@DSLTypeAdapter` markiert werden. Dabei muss über den `t`-Parameter definiert werden, welcher Java-Datentyp über die Methode adaptiert werden soll.
Beispiel:

```java
public class ExternalTypeBuilder {
    @DSLTypeAdapter(t = ExternalType.class)
    public static ExternalType buildExternalType(String param) {
        return new ExternalType(param, otherParameter);
    }
}
```

Die Klasse, in der die so markierte Builder-Methode definiert ist, muss im `TypeBuilder` über die `registerTypeAdapter`-Methode registriert werden.

```java
TypeBuilder tb = new TypeBuilder();
tb.registerTypeAdapter(ExternalTypeBuilder.class, Scope.NULL);
```

Für alle standardmäßig verfügbaren adaptierten Datentypen ist dies bereits im `GameEnvironment` ([GameEnvironment.java](./../../dsl/src/runtime/GameEnvironment.java)) in der Methode `registerDefaultTypeAdapters` implementiert.

Die Konfiguration von Membern mit adaptierten Datentypen unterscheidet zwischen zwei Fällen:
1. die Builder-Methode benötigt nur einen Parameter
2. die Builder-Methode benötigt mehr als einen Parameter

#### 1. Builder-Methode mit einem Parameter 

Für die Konfiguration des Members der Datentyp des Parameters der Builder-Methode verwendet. Für die oben definierte Methode `buildExternalType` ist das `String`. Die Konfiguration per DSL würde wie folgt aussehen:

```
game_object my_obj {
    component {
        member1: 42,
        member2: "Parameter"
    }
}
```

#### 2. Builder-Methode mit mehr als einem Parameter

TODO!

# Implementierung

Der grobe Ablauf des Typebuildings, welches vom GameEnvironment für alle standardmäßig verfügbaren Datentypen ausgeführt wird, ist im folgenden Sequenzdiagramm abgebildet.

![typebuilding](./img/typebuilding.jpg)


TODO:
- Sequenzdiagramm für Typadaptierung

