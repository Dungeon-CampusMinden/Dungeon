# Multiplayer-Architektur: Prinzipien und Alternativen

## Übersicht

Dieses Dokument beschreibt die Architektur zur Implementierung der Multiplayer-Funktionalität im Spiel. Unser Hauptziel ist ein robustes, autoritatives Client-Server-Modell, das gleichzeitig voll kompatibel mit dem Singleplayer-Modus bleibt.

### Synergie: ECS und das Client-Server-Modell

Die Entscheidung für eine ECS-Architektur zu Beginn der Entwicklung erweist sich für die Implementierung des Multiplayers als großer Vorteil. ECS und das autoritative Client-Server-Modell passen außergewöhnlich gut zusammen, was die Umsetzung vereinfacht und robuster macht.

**Warum diese Kombination so gut funktioniert:**

*   **Klare Trennung von Daten und Logik:** Dies ist der entscheidende Punkt. Components sind reine Daten (Position, HP), während Systems die Logik sind, die diese Daten verändert. Über ein Netzwerk werden nur Daten gesendet, keine Logik. Der Server führt die Systems aus, modifiziert die Components und sendet dann nur die reinen, aktualisierten Daten.
*   **Gezielte Synchronisation:** Wir müssen keine komplexen Objekte serialisieren. Stattdessen können wir gezielt nur die Daten aus den Components extrahieren, die der Client wirklich zum Rendern benötigt. Moderne Frameworks wie Unity's Netcode for Entities basieren auf genau diesem Prinzip der Synchronisation von Component-Daten [[Quelle](https://docs.unity3d.com/Packages/com.unity.netcode@1.0/manual/basics/networkworld.html)].
*   **Einfacher Snapshot-Prozess:** Auf dem Server ist es trivial, einen `EntityStateUpdate` zu erstellen. Nach dem Durchlauf aller Gameplay-Systems kann ein weiteres System einfach über die Entities iterieren, die relevanten Daten auslesen und in ein Nachrichten-Objekt packen.
*   **Sauberes Update auf dem Client:** Der Client empfängt den `EntityStateUpdate` und überschreibt die Daten in seinen lokalen Components. Der Client agiert somit als reiner Spiegel des Server-Zustands, was die Logik stark vereinfacht. Dieses Muster der Zustandsanwendung und anschließenden Interpolation ist eine bewährte Methode, die in erfolgreichen Spielen wie *Overwatch* zum Einsatz kommt [[Quelle](https://www.youtube.com/watch?v=W3aieHy3M3w)].

## 1. Netzwerk-Topologie: Autoritative Client-Server

### 1.1. Unser gewählter Ansatz: Autoritative Client-Server

Wir setzen auf ein klassisches Client-Server-Modell. Der Server hat die alleinige Autorität über den Spielzustand (State). Clients senden nur ihre Eingaben (Inputs), der Server simuliert das Ergebnis und sendet den neuen State an alle zurück.

**Warum dieser Ansatz?**

*   **Sicherheit & Konsistenz:** Der Hauptgrund ist die Verhinderung von Cheating. Der Server ist die einzige "Source of Truth" und validiert alle Aktionen. Ein Client kann nicht einfach seine Position oder Gesundheit manipulieren, da der Server dies als ungültig erkennen würde. Das ist der Industriestandard für Actionspiele [[Quelle](https://moldstud.com/articles/p-unreal-engine-client-server-model-comprehensive-guide-for-multiplayer-game-development)]. Der Server agiert als vertrauenswürdiger Schiedsrichter [[Quelle](https://medium.com/@lemapp09/beginning-game-development-client-server-architecture-1b7676d80dea)].
*   **Stabile Grundlage:** Dieser Ansatz wird von Branchenexperten wie Glenn Fiedler [[Quelle](https://gafferongames.com/post/what_every_programmer_needs_to_know_about_game_networking/)] und in Engines wie der Source Engine von Valve [[Quelle](https://developer.valvesoftware.com/wiki/Source_Multiplayer_Networking)] verwendet und hat sich in großem Maßstab bewährt.

### 1.2. Alternative: Peer-to-Peer (P2P) mit Deterministic Lockstep

Hierbei gäbe es keinen zentralen Server. Alle Spieler (Peers) würden sich direkt miteinander verbinden und nur ihre Inputs austauschen.

**Warum nicht P2P?**

*   **Extremer Implementierungsaufwand:** P2P erfordert **strikten Determinismus**. Das bedeutet, jeder Client muss bei identischen Inputs exakt das gleiche Ergebnis produzieren, Bit für Bit. Das ist extrem schwierig zu erreichen, da Dinge wie Fließkomma-Mathematik, Threading oder sogar die Reihenfolge von Iterationen über Hash-Maps auf verschiedenen Maschinen zu unterschiedlichen Ergebnissen führen können [[Quelle](https://hdms.bsz-bw.de/files/7107/DeterministicLockstepInNetworkedGamesPaper.pdf)]. Unsere aktuelle Codebasis ist nicht deterministisch und müsste grundlegend umgeschrieben werden.
*   **Anfällig für Latenz:** Das Spiel müsste im "Lockstep" laufen, also immer auf den langsamsten Spieler warten, bevor die nächste Runde (Turn) simuliert werden kann. Das führt zu spürbarem Input-Lag für alle Spieler [[Quelle](https://en.wikipedia.org/wiki/Lockstep_protocol)].

**Fazit:** Die Umstellung auf P2P wäre ein komplettes Rewrite der Engine. Das **autoritative Client-Server-Modell** ist für uns die einzig realistische und sichere Wahl.

## 2. Synchronisationsstrategie: State Synchronization

### 2.1. Unser gewählter Ansatz: State Synchronization

Der Server sendet in regelmäßigen Abständen "Snapshots" des Spielzustands an die Clients. Die Clients nutzen diese Daten, um die Welt zu rendern, und interpolieren zwischen den Snapshots, um Bewegungen flüssig darzustellen.

**Warum dieser Ansatz?**

*   **Industriestandard für Actionspiele:** Diese Strategie, oft als **"Snapshot Interpolation"** bezeichnet, ist der De-facto-Standard für Echtzeit-Actionspiele. Sie wurde durch Engines wie *Quake* und die *Source Engine* populär gemacht [[Quelle](https://medium.com/my-games-company/unity-realtime-multiplayer-part-7-architectures-in-different-genres-8185e9a3a3ad)].
*   **Flexibilität und Robustheit:** Der Ansatz ist tolerant gegenüber Packet Loss, da der nächste Snapshot den Zustand korrigiert. Er entkoppelt die Render-Rate des Clients von der Tick-Rate des Servers, was eine flüssige Darstellung ermöglicht [[Quelle](https://gafferongames.com/post/state_synchronization/)].
*   **Kein Determinismus nötig:** Da der Server die Wahrheit vorgibt, muss die Client-Simulation nicht perfekt deterministisch sein.

### 2.2. Alternative: Frame Synchronization (P2P-Ansatz)

Hierbei werden nur Inputs gesendet. Dies ist die Methode, die bei P2P-Lockstep zum Einsatz kommt.

**Warum nicht Frame-Sync?**

*   Wie in Abschnitt 1.2 erläutert, erfordert dies einen strikten Determinismus, den unsere Engine nicht bietet. Dieser Ansatz eignet sich gut für Strategiespiele wie *StarCraft*, aber nicht für unser Action-Roguelike [[Quelle](https://hdms.bsz-bw.de/files/7107/DeterministicLockstepInNetworkedGamesPaper.pdf)].

**Fazit:** **State Synchronization** ist für unser Projekt die richtige Wahl.

## 3. Nachrichteninhalt: Zweckgebundene State-Objekte

### 3.1. Unser gewählter Ansatz: State-Objekte (`EntityStateUpdate`)

Wir definieren explizite Nachrichten-Objekte (Records), die nur die Daten enthalten, die für die Synchronisation wirklich notwendig sind. Anstatt ganze Components zu versenden, extrahiert der Server die relevanten Daten (z.B. Position, Animation-Name) und packt sie in ein `EntityStateUpdate`-Objekt.

**Warum dieser Ansatz?**

*   **Stabile "Network API":** Dieser Ansatz schafft eine klare Schnittstelle zwischen Client und Server [[Quelle](https://learn.microsoft.com/en-us/azure/architecture/best-practices/api-design)]. Wir können die interne Implementierung unserer Components ändern, ohne das Netzwerkprotokoll zu brechen, solange wir die Nachrichten-Objekte noch korrekt befüllen können.
*   **Effizienz und Sicherheit:** Wir senden nur, was gebraucht wird, und vermeiden es, interne Server-Daten preiszugeben. Dieser professionelle Ansatz ist vergleichbar mit dem, wie Engines wie die Source Engine ihre Netzwerkdaten mit "Send Tables" strukturieren [[Quelle](https://developer.valvesoftware.com/wiki/Networking_Entities)].

### 3.2. Alternative: Senden von serialisierten Roh-Components

Man könnte auch einfach ganze Component-Objekte serialisieren und versenden.

**Warum nicht dieser "einfache" Weg?**

*   **Extrem fragil:** Jede kleine Änderung an einem Component (z.B. ein neues Feld hinzufügen) würde das Netzwerkprotokoll brechen und Client und Server inkompatibel machen.
*   **Ineffizient:** Es würden viele unnötige Daten gesendet, die der Client gar nicht braucht. Die Komplexität von professionellen Serialisierungs-Systemen zeigt, dass dieser naive Ansatz in der Praxis nicht funktioniert [[Quelle](https://developer.valvesoftware.com/wiki/Data_Descriptions)].

**Fazit:** Die Verwendung von **zweckgebundenen State-Objekten** ist der einzig wartbare und performante Weg.

## 4. API-Verwendung und Code-Struktur

Dieses Kapitel beschreibt, wie die Multiplayer-API in der Praxis verwendet wird und wo die relevanten Klassen und Interfaces im Projekt zu finden sind.

### 4.1. Der zentrale Zugriffspunkt: `Game.network()`

Alle Interaktionen mit der Netzwerkschicht sollen über einen einzigen, zentralen Punkt erfolgen, um die Konsistenz zu wahren und die Kopplung gering zu halten.

*   **Zugriff:** Der `NetworkHandler` wird ausschließlich über die statische Methode `Game.network()` abgerufen.
*   **Verwendung:** Anstatt direkt auf Komponenten zuzugreifen, um eine Aktion auszulösen, wird ein Befehl an den Handler gesendet.

**Beispiel:**

```java
// Falsch (direkte Manipulation):
hero.fetch(VelocityComponent.class).ifPresent(vc -> vc.setCurrentVelocity(...));

// Richtig (Senden eines Befehls über die API):
Game.network().sendHeroMovement(Direction.UP);
```

### 4.2. Senden von Spieler-Befehlen (Client-Logik)

Die Logik zum Senden von Spieler-Inputs befindet sich dort, wo die Eingaben verarbeitet werden, typischerweise in Klassen wie dem `HeroFactory` oder dedizierten Input-Systemen.

*   **Ablauf:** Die Input-Logik erstellt keinen direkten Effekt im Spiel, sondern ruft lediglich die entsprechende `send...`-Methode des `NetworkHandler` auf.
*   **Nachrichten-Objekte:** Die Befehle selbst sind als `record`-Klassen im Paket `core.network.messages` definiert. Wenn neue Befehle benötigt werden, sollten sie dort als Implementierung des `NetworkMessage`-Interfaces angelegt werden.

### 4.3. Empfangen von State-Updates (Client-Logik)

Der Client wird so konzipiert, dass er seinen Zustand nicht selbst simuliert, sondern auf Basis der vom Server gesendeten `EntityStateUpdate`-Nachrichten aktualisiert wird.

*   **Ablauf:** Während der Spielinitialisierung wird ein Listener über `Game.setStateUpdateListener(...)` registriert. Dieser Listener wird immer dann aufgerufen, wenn ein State-Update eintrifft (im Singleplayer-Modus wird dies durch den `LocalNetworkHandler` simuliert).
*   **Verantwortlichkeit:** Die im Listener enthaltene Logik ist dafür verantwortlich, die empfangenen Daten zu verarbeiten. In unserem Fall wird dies der `RenderStateManager` sein, der die Daten für das `DrawSystem` aufbereitet.

### 4.4. Paket- und Klassenübersicht

Hier ist eine Übersicht der wichtigsten Pakete und Klassen der Netzwerk-Architektur:

*   `core.network`
    *   `NetworkHandler`: Das zentrale Interface, das alle Netzwerk-Aktionen definiert (z.B. `sendHeroMovement`).
    *   `RenderStateManager`: Empfängt State-Updates und hält den zu rendernden Zustand vor.
    *   `NetworkException`: Eine spezifische Exception für Netzwerkfehler.
    *   `LocalNetworkHandler`: Die Implementierung für den Singleplayer-Modus. Führt Befehle direkt lokal aus und simuliert den Netzwerk-Flow. Hier wird später auch der `KryoNetNetworkHandler` für den echten Multiplayer liegen.

*   `core.network.messages`
    *   `NetworkMessage`: Das Marker-Interface für alle Nachrichten.
    *   `EntityStateUpdate`: Record für die Zustands-Snapshots, die vom Server gesendet werden.
    *   `NetworkEvent`: Record für einmalige, kritische Ereignisse (z.B. Level-Wechsel).
    *   `HeroMoveCommand`, `UseSkillCommand`, etc.: Records für die Befehle, die vom Client an den Server gesendet werden.

## 5. Nächste Schritte

1.  **Echte Netzwerk-Implementierung:** Erstellung eines `KryoNetNetworkHandler`, der das `NetworkHandler`-Interface implementiert und Nachrichten über TCP/UDP sendet.
2.  **Serverseitiger Game Loop:** Erstellung einer headless (ohne Grafik) Server-Applikation, die den `KryoNetNetworkHandler` im Server-Modus initialisiert, die ECS-Systeme ausführt und State-Updates an die Clients sendet.
3.  **Clientseitige State-Verarbeitung:** Der Game-Client wird so umgebaut, dass er den State, den er vom Server empfängt, rendert. Lokale Inputs werden nur noch an den Server gesendet und beeinflussen den lokalen State nicht mehr direkt.

## 6. Aktuelle Einschränkungen

1.  **Nur Singleplayer:** Aktuell ist alles nur eine lokale Simulation. Es findet kein echter Netzwerkverkehr statt.
2.  **Keine Input Lag Compensation:** Das Design beinhaltet noch keine Client-Side Prediction. Im echten Multiplayer wird es eine spürbare Latenz zwischen Tastendruck und Bewegung geben. Das ist für die erste Version akzeptabel.
3.  **Grundlegende State Synchronization:** Der `EntityStateUpdate` ist noch nicht für Bandbreite optimiert (z.B. durch Delta Compression).
