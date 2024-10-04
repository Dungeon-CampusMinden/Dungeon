# Blockly Visualisierungskonzept

## Ziele der Visualisierung im Dungeon

Das Hauptziel der Visualisierung im Dungeon ist es, einen visuellen Debugger zu bieten. Dies soll mit der Lösung von
spielerischen Aufgaben verknüpft werden. Die Aufgaben sollen mit einem Blockly-Programm lösbar sein. Die erste Aufgabe,
welche für den Blockly-Dungeon definiert wurde, ist die Lösung eines Labyrinths. Der Spieler wird mitten in einem
Labyrinth platziert und muss den Ausgang finden.

## Was machen andere Spiele?

Es gibt einige Spiele, welche versuchen die Programmierung mit spielerischen Elementen zu verknüpfen. Dabei ist es jedoch nicht
das Ziel einen visuellen Debugger zu bieten, sondern vielmehr die Lösung eines Rätsels oder einer Aufgabe mithilfe eines
Programms zu finden. Dementsprechend wird in diesen Spielen kein Code im Spiel selber angezeigt. In den folgenden Abschnitten
sind einige Beispiele aufgelistet.

### Robocode

In diesem Spiel programmiert der Spieler einen Panzer in einer Programmiersprache seiner Wahl, welcher gegen andere
Panzer antritt. Der Panzer, welcher als letztes überlebt gewinnt. Vorkenntnisse in der Programmierung vorausgesetzt.

### Codinggame

Auf dieser Website müssen Puzzle oder Challenges gelöst werden (in einer beliebigen Programmiersprache). Vorkenntnisse
in der Programmierung vorausgesetzt.

### Codecombat

Es müssen Rätsel gelöst werden (in Python/ JS). Vorkenntnisse in der Programmierung vorausgesetzt.

### Human Resource Machine

Dieses Spiel kommt den Anforderungen des Dungeons am nächsten. Es handelt sich hierbei um ein Spiel, welches unabhängig
von einer Programmiersprache versucht dem Spieler beizubringen wie ein Programmierer zu denken. Es sind dabei keine
Vorkenntnisse in der Programmierung notwendig. Der Spieler muss alltägliche Aufgaben eines Büroangestellten lösen.
Wie in Blockly hat man bestimmte Blöcke zur Verfügung, welche genutzt werden können, um das Verhalten des
Büroangestellten zu steuern.

Das Spielprinzip funktioniert wie folgt: Es gibt ein Input- und ein Output-Förderband. Über das Input-Förderband kommen
Blöcke mit Werten rein. Mit dem Befehl ``Outbox`` transportiert der Büroangestellte den obersten Block vom
Input-Förderband zum Output-Förderband. Zu Beginn bestehen die Aufgaben z.B. darin, alle Blöcke vom Input-Förderband
zum Output-Förderband zu transportieren. Um so weiter der Spieler im Spiel fortschreitet, desto schwieriger werden die
Aufgabenstellungen und umso mehr Blöcke hat der Spieler zur Verfügung. Z.B. hat der Spieler später Blöcke wie JUMP und IF
zu verfügung. Mit dem Jump-Befehl kann an eine bestimmte Stelle im Code gesprungen werden kann. Dadurch können Schleifen
gebaut werden.

Die Kontrollstrukturen, wie z.B. eine If-Abfrage oder der Jump-Befehl sind jedoch nicht explizit visuell sichtbar,
sondern lassen sich nur indirekt über die ausgeführten Aktionen des Angestellten beobachten.
Beispielsweise gibt es Aufgabenstellungen, welche von dem Angestellten verlangen, dass nur Blöcke mit dem Wert 0 auf das
Förderband gelegt werden sollen. Wenn man in dem Programm mithilfe einer If-Bedingung dafür sorgt, dass der Outbox
Befehl nur bei Blöcken mit dem Wert null ausgeführt wird, werden alle Blöcke mit einem Wert ungleich null von dem
Büroangestellten weggeworfen und nicht zum Output-Förderband transportiert.

Andere Aktionen, wie z.B. eine Addition, sind direkt sichtbar. Bei einer Addition schlägt der Angestellte z.B. zwei
Blöcke über seinem Kopf zusammen und diese ergeben einen neuen Block.

### Fazit andere Spiele

Andere Spiele setzten oft vorraus, das bereits Vorkenntnisse in der Programmierung vorhanden sind. Die Spiele zielen
darauf ab, die eigenen Fähigkeiten zu verbessern, anstatt Einsteigern das Programmieren unabhängig von einer Programmiersprache
beizubringen. In den meisten Fällen muss in einer existierenden Programmiersprache programmiert werden. Es gibt jedoch
auch Ausnahmen, wie das Spiel ``Human Resource Machine``. Es ist einsteigerfreundlich und unabhängig von einer
Programmiersprache. Es kommt den Anforderungen des Visualisierungskonzepts im Dungeon am nächsten und kann deshalb eine
grobe Orientierungshilfe bieten.

## Visualisierungskonzept im Dungeon

### Variablen

Monster stellen eine zentrale Rolle in dem Visualisierungskonzept dar. Ähnlich wie die Blöcke bei ``Human Resource Machine``
transportieren sie die Werte in die Oberfläche. Damit Variablen die gesamte Zeit über im Dungeon sichtbar sind, wird ein
gesonderter Bereich für die Monster als HUD dargestellt. Dieser Bereich nutzt die bereits vorhandenen Kacheln für den
Boden und die Wände. Der Bereich wird von dem Rest des Spiels mit den Wand-Kacheln abgegrenzt. Im inneren Bereich des
Variablen-Bereichs sind die Boden-Kacheln. Dort werden die Monster platziert. Über den Monstern wird der eigentliche Wert
der Variable angezeigt. Damit das Monster der Variable aus dem Blockly Programm zugeordnet werden kann, wird ein der
Variablenname unter oder über dem Monster mithilfe eines Namensschildes angezeigt. Damit nicht zu große Werte angezeigt
werden müssen, sollte der maximale Wert einer Variable in Frontend bereits begrenzt werden.

Die Anzahl der Variablen, welche gleichzeitig angezeigt werden können, ist limitiert. Das heißt, dass in dem Bereich
immer die zuletzt verwendeten Variablen angezeigt werden. Es werden maximal bis zu 14 Variablen gleichzeitig angezeigt.
Bei langen Variablennamen werden die Zeichen, welche zu viel sind durch ein ``...`` ersetzt, damit sich die
Variablennamen nicht überschneiden.

Wenn eine neue Variable erstellt wird und damit auch ein neues Monster im HUD angezeigt wird, sollte eine Animation
eingespielt werden. Das könnte z.B. eine Art Teleportation-Animation sein.

### Expressions

Mathematische Expressions, welche mit den Operatoren +, -, / und * durchgeführt werden basieren ebenfalls auf der
Darstellung von Zahlenwerten durch Monster. Sobald eine Expression durchgeführt wird, werden die benötigten Monster aus
dem Variablen HUD in den normalen Dungeon teleportiert. Für die Teleportation wird eine Animation bei den Monstern und
an der Stelle im Dungeon, wo das jeweilige Monster platziert wird, eingespielt. Währenddessen wird eine Animation beim
Zauberer eingespielt, sodass erkenntlich wird, dass der Zauberer die Monster teleportiert. Z.B. könnte er den Zauberstab
während der Teleportation schwingen. Darauf folgen abhängig von der mathematischen Operation unterschiedliche Animationen.
Die unterschiedlichen Animationen sind in den folgenden Abschnitten aufgelistet und erläutert.

#### Addition

Zwei Monster laufen aufeinander zu und fusionieren miteinander. Aus den beiden Monstern entsteht ein neues Monster. Das
neue Monster erhält den addierten Wert der beiden Monster. Das neue Monster ist das Ergebnis-Monster. Die
Fusions-Animation könnte z.B. ein weißer Kreis sein: <https://youtu.be/mJn5LJlX1jQ?si=GcvIf_D_Zf2a38AD&t=155>.

#### Subtraktion (Monster A - Monster B)

Bei einer Subtraktion schießt Monster B einen *blauen* Feuerball auf Monster A. Monster B verschwindet, nachdem es den
Feuerball geworfen hat. Monster A bleibt zurück, wobei der Wert von Monster A um den Wert von Monster B verringert wird.
Es wird die Animation eingespielt, wenn ein Monster Schaden bekommt. Monster A ist das Ergebnis-Monster.

#### Multiplikation (Monster A * Monster B)

Bei einer Multiplikation schießen Monster A und Monster B einen *roten* Feuerball aufeinander und verschwinden danach.
Die Feuerbälle treffen sich in der Mitte und fusionieren zu einem neuen Monster mit dem Ergebnis der Multiplikation.
Das neue Monster ist das Ergebnis-Monster.

#### Division (Monster A / Monster B)

Bei einer Division schießt Monster B einen *lila* Feuerball auf Monster A und verschwindet danach. Monster A erhält
Schaden und hat jetzt als Wert das Ergebnis der Division. Es wird die Animation eingespielt, wenn ein Monster Schaden
bekommt. Monster A ist das Ergebnis-Monster.

### Variablenzuweisung

Wenn einer Variable ein direkter Wert zugewiesen wird, erscheint das Monster nach einer Animation direkt in dem
Variablenbereich. Wenn die Variable den Wert einer Expression haben soll, wird die Expression wie oben beschrieben
durchgeführt. Am Ende der Animation wird das Ergebnis-Monster in den Variablenbereich teleportiert. Wenn eine neue
Variable erstellt wurde, wird das Ergebnis-Monster als neues Monster in den Variablenbereich teleportiert. Wenn eine
existierende Variable geändert wurde, erscheint die Teleportation-Animation bei dem bereits existierenden Monster und
der Wert wird aktualisiert.

### Arrays

Für Arrays ist wie bei dem Variablenbereich ein extra Bereich vorgesehen, welcher immer an der Seite angezeigt wird. Es
werden immer zwei Bereiche angezeigt. Ein Bereich links und ein Bereich rechts. Dadurch können Aktionen, welche z.B.
die Werte aus zwei Arrays benötigen vernünftig angezeigt werden. Dabei werden immer die beiden zuletzt benötigten
Arrays angezeigt. In dem Feld steht jede Kachel für einen Index des Arrays. Die Größe eines Arrays sollte z.B. auf 10
beschränkt sein. Jede Kachel erhält ein Namensschild mit dem Index der Kachel. Wie bei den Variablen werden die Werte
des Arrays durch Monster dargestellt. Die Monster werden auch hier mit der Teleportation-Animation in das Array
hinein teleportiert, bzw. heraus teleportiert, wenn ein Wert für eine Expression benötigt wird. Die Variable des Arrays
wird als ein Portal dargestellt und nicht als Monster.

### If-Abfragen

Bei If-Abfragen öffnet sich eine Denkblase. In dieser Denkblase werden Animationen angezeigt, welche die Evaluation der
Bedingung visualisieren sollen. Die unterschiedlichen Aktionen werden in den folgenden Abschnitten vorgestellt und
erläutert.

#### Vergleichsoperatoren

In der Denkblase werden zwei Monster mit ihrem Wert über dem Kopf dargestellt. Zwischen den beiden Monstern ist der
Operator. Der Operator wird in keiner speziellen Darstellung angezeigt, da die Darstellung des Gleichheitszeichens und
des kleiner/größer Zeichens leichter zu verstehen ist als eine andere symbolische Darstellung. Wenn ein Monster einen
größeren Wert hat als das andere wird das größere Monster in der Denkblase größer dargestellt. Das kleinere Monster wird
entsprechend kleiner dargestellt. Wenn die beiden Monster gleich groß sind, werden beide Monster gleich groß angezeigt.
Am Ende wird das Ergebnis des Vergleichs mit einem grünen Haken (true) oder einem roten Kreuz dargestellt (false).
Zusätzlich wird das Ergebnis mit einem success oder einem error sound effekt verdeutlicht:

- <https://www.youtube.com/watch?v=ndgsWcd3yUs>
- <https://www.youtube.com/watch?v=FwVRkhy5G04>

#### Logische Operatoren

##### AND

In der Denkblase werden zwei Buttons angezeigt auf denen ``AND`` steht. Die beiden booleschen Werte werden wieder durch
Monster dargestellt. Dieses Mal haben sie jedoch keinen Zahlenwert, sondern entweder einen grünen Haken (true) oder ein
rotes Kreuz (false) über ihrem Kopf schweben. Wenn ein Monster zu True evaluiert läuft es auf einen Button, welcher dann
grün zu leuchten beginnt. Wenn beide Monster zu True evaluieren laufen sie jeweils beide auf einen Button und es wird
der grüne Haken angezeigt. Wenn ein Monster zu False evaluiert, bleibt es stehen und läuft nicht auf den Button. Wenn
mindestens ein Monster stehen bleibt wird ein rotes Kreuz angezeigt.

##### OR

In der Denkblase werden erneut zwei Buttons angezeigt, auf denen dieses Mal jedoch ``OR`` steht. Wie bei ``AND`` läuft
ein Monster los, wenn es zu true evaluiert. Wenn mindestens ein Monster auf einem Button steht, wird der grüne Haken
angezeigt. Andernfalls erscheint das rote Kreuz.

##### NOT

Es wird ein Button angezeigt auf dem ``NOT`` steht. Der Button leuchtet grün, wenn kein Monster auf dem Button steht.
Wenn das Monster zu true evaluiert läuft es wie bisher auf den Button. Dann leuchtet der Button aber nicht mehr und es
wird das rote Kreuz angezeigt. Wenn das Monster zu false evaluiert, bleibt es stehen. Der Button bleibt am Leuchten und
es erscheint der grüne Haken.

### Schleifen

#### For-Schleifen

Bei einer Zählschleife könnte eine Münze über dem Kopf des Helden erscheinen. In der Münze steht eine Zahl (aktueller
Wert der Zählvariable). Bei jedem Durchlauf hebt der Held seinen Zauberstab nach oben und die Zahl erhöht sich. Dabei
wird ein Soundeffekt eingespielt: <https://www.youtube.com/watch?v=5v20ztxGvQ0>. Für jede weitere Schleife wird eine
eigene Münze angezeigt, immer rechts von der übergeordneten Schleife. Dabei zeigt man aber immer nur zwei/drei Münzen
gleichzeitig an (immer die Münzen der drei innersten Schleifen).

#### While-Schleifen

Wie bei einer If-Abfrage öffnet sich bei jedem Schleifen Durchlauf zu Beginn eine Denkblase, um die Bedingung zu
visualisieren. Das Visualisierungsprinzip ist dabei das gleiche, wie bei If-Abfragen. Außer der Visualisierung der
Bedingung wird nichts weiter visualisiert.

### Funktionen

Eine Funktion wird über ein neues Item ``To-do-Liste`` abgebildet. Zu Beginn einer Funktion wird das Item ``To-do-Liste``
ausgelöst. Es erscheint eine kurze Meldung am oberen Rand des Bildschirms (wie Toasts in der Frontend-Entwicklung):
``<Icon TODO-Liste>`` mit dem Text: ``To-do-Liste: 'Funktionsname' wird bearbeitet``. Wenn die Funktion beendet ist, wird
eine Erfolgsmeldung angezeigt, dass die To-do-Liste vollständig bearbeitet wurde, z.B.:
``To-do-Liste: 'Funktionsname' abgeschlossen``.

### Anzeige für aktuell ausgeführte Codezeile

Da Inhalte von Variablen etc. im Dungeon sichtbar sein sollen, sollte für den Benutzer auch in irgendeiner Art und Weise
sichtbar sein, welche Codezeile aktuell ausgeführt wird. Dafür gibt es zwei Möglichkeiten:

1. Die aktuelle Codezeile wird in einem Textfeld im Dungeon angezeigt, welche die letzten drei Codezeilen anzeigt, wobei
die aktuelle Codezeile mit einem grünen Pfeil markiert wird. Das Textfeld könnte am oben Rand des Bildschirms angezeigt
werden.
2. Der aktuell ausgeführte Block wird im Blockly-Frontend hervorgehoben, sodass der Benutzer nachvollziehen kann, welcher
Block aktuell im Dungeon bearbeitet wird.

### Fazit des Konzepts

Mit den obigen Ideen kann man Variablen, Arrays, Expressions, Funktionen, IF-Abfragen, etc. darstellen. Allerdings ist
die Ausführung des Programms dadurch auch deutlich langsamer, weil jede Operation irgendwie dargestellt wird. Das
funktioniert gut um Algorithmen darstellen zu können, z.B. wenn ein Array sortiert wird.

Allerdings wird durch die langsame Ausführung der spielerische Aspekt reduziert. Wenn man lieber ein Spiel entwickeln
möchte, sollte man eher eine minimale Visualisierung nutzen, z.B. die Anzeige der aktuell ausgeführten Codezeile. Man
müsste sich Aufgaben überlegen, ähnlich wie die anderen Spiele, z.B. Suche den Ausgang aus diesem Labyrinth oder löse
das Rätsel dieses Raumes und der Spieler muss zwei Truhen finden und einen Schlüssel daraus holen, damit er die Tür
öffnen kann.

Man könnte die Ausführungsgeschwindigkeit erhöhen, indem man es konfigurierbar macht, ob eine Animation mit weniger
Frames oder mehr Frames angezeigt werden soll. Denkbar wäre auch, die zeitaufwändigen Animationen nur anzuzeigen, wenn
der Benutzer z.B. über einen Schritt-für-Schritt Button das Programm schrittweise durchläuft. Wenn er jedoch den
Start-Button drückt, um das gesamte Programm auszuführen werden die Animationen nicht angezeigt oder nur in einer
minimalen Form mit weniger Frames.
