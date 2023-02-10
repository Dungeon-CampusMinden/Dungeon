In diesem Eintrag wird beschrieben, wie wir Updates im laufenden Semester verteilen. 

Im Master-Branch befindet sich die `aktuelle` Version des Dungeons. Dies ist die Version, mit der die Studierenden arbeiten. 
Im `aktuellen`-Branch werden im laufenden Semester keine `MAJOR`-Updates veröffentlicht. `MINOR`-Updates und `PATCH`-Updates sind erlaubt. 

Um auch im laufenden Semester das Dungeon strukturell erweitern zu können, wird ein `develop`-Branch angelegt. 
In diesem Branch wird die neue Version gepflegt. Er kann als Master-Branch für den `develop`-Zyklus verstanden werden. 
Für den `develop`-Branch gelten dieselben Regeln wie für den eigentlichen Master-Branch.

Nachdem das Semester beendet ist, wird der `develop`-Branch zum neuen Master-Branch.

Alle Änderungen die in der `aktuellen`-Version des Dungeons getätigt werden, müssen auch im `develop`-Branch eingeplfegt werden.
Sollten sich Änderungen nicht 1 zu 1 übernehmen lassen, sind diese für den `develop`-Branch entsprechend anzupassen.

Dafür kann zuerst ein PR in den Master-Branch gestellt werden und nachdem dieser gemerged wurde, ein neuer PR (ggf. mit Anpassungen) in den `develop`-Branch.
*Bitte löscht regelmäßig alle Branches die nicht mehr benötigt werden.*

Der Quickstartguide ist immer aktuell zu halten. Für Änderungen am Quickstart, die mit `develop` in Zusammenhang stehen, einen entsprechenden `develop-quickstart`-Branch anlegen und am Ende des Semesters mergen. 
