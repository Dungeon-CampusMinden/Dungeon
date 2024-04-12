Bitte die Klasse `ClsToRefactor.java` auf folgende Punkte prüfen:

- Nicht genutzte Parameter und Variablen
- Zu kurze Parameter-, Variablen- und Methodennamen
- Zu kurze bzw. schlechte Kommentare (Was passiert wie?)
- Zu lange Methodennamen
- Nicht aussagekräftige String-Inhalte
- Schlechte Fehlermeldungen
- Code-Duplizitäten: Konstante Strings und Funktionen können zu Variablen oder Klassenkonstanten gemacht werden
- Die Methode `ge()` tut zu viel und kann aufgeteilt werden in kleinere Methoden
- Unübersichtliche Konstrukte: Manche Stream- und Lambda-Schreibweisen sind schwer zu lesen (aufteilen in kleinere Methoden)
- Falsche Sichtbarkeiten: Manche Attribute sollten `private` sein
- Manche Attribute/Konstanten sollten `final` sein
