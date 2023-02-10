@malte-r

# Überblick über Idee und Ziele die mit der DSL verfolgt werden

TODO

# DSL Pipeline

- Lexing & Parsing: ANTLR
- Konvertierung in AST
- Erzeugen von Typumgebung (IEnvironment)
- Semantische Analyse
    - Typebinding
    - Variablebinding
    - Resolver-Walk
    - Functions-Calls resolven
- Initialisierung der Laufzeit
    - Laden von IEnvironment in RuntimeEnvironment (enthält zusätzlich
      Prototypen)
    - Funktionsdefinitionen in runtime environment binden
      (als FuncCallValue, temporäres Design, da fehlen noch
      Typinformationen zu den Funktionssignaturen)
    - globale definitionen binden (als Value)
- generateQuestConfig
    - Prototypen erzeugen -> AggregateValues mit Defaultwerten
    - Evaluierung der `quest_config`-Definition -> nur das, was auch
      referenziert wird, wird evaluiert
    - Instanziierung des QuestConfig-Objekts

[Typebuilding](https://github.com/Programmiermethoden/Dungeon/wiki/Typebuilding)
