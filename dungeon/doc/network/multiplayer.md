# Multiplayer-Architektur: Prinzipien, Entscheidungen und Struktur

Dieses Dokument beschreibt die aktuelle Multiplayer-Architektur, die wesentlichen Designentscheidungen und Alternativen inklusive Quellen zu etablierten Verfahren aus Industrie und Forschung. Ziel ist ein robustes, autoritatives Client-Server-Modell, das mit unserer ECS-Architektur harmoniert und Singleplayer vollständig unterstützt.

Inhalt
- 1. ECS und Autorität: Warum das so gut zusammenpasst
- 2. Netzwerktopologie: Autoritativer Client-Server statt P2P
- 3. Synchronisationsstrategie: Snapshots mit Interpolation (State Sync)
- 4. Transport- und Protokollebene (TCP/UDP, Framing, NAT-Registrierung)
- 5. Nachrichtenformat und „Network API“
- 6. Serverlaufzeit: Tick, Verarbeitung, Broadcast
- 7. Clientlaufzeit: Handshake, Eingaben, Empfang, Threading
- 8. Projektstruktur: Module und Verantwortlichkeiten
- 9. Sicherheit, Robustheit und Grenzen
- 10. Roadmap: Was als Nächstes kommt
- 11. Referenzen

-------------------------------------------------------------------------------

## 1. ECS und Autorität: Warum das so gut zusammenpasst

- Strikte Trennung von Daten und Logik
  - Components enthalten reine Daten (z. B. Position, HP), Systems verändern diese Daten.
  - Über das Netzwerk werden nur Daten synchronisiert, keine Logik. Der Server führt die Systems aus, Clients spiegeln Zustände und rendern.
- Gezielte Synchronisation
  - Wir versenden nur die Daten, die der Client zum Rendern braucht (Positions-/Animationszustände etc.), nicht komplette Objekte oder gesamte Components.
- Schnapper für Snapshots
  - Serverseitig ist es trivial, nach einer Tick-Simulation über Entities zu iterieren, relevante Felder zu extrahieren und als Snapshot zu versenden.
- Auf dem Client: Übernahme + Interpolation
  - Clients übernehmen Zustände und „glätten“ die Anzeige (Interpolation/Buffering). Dieses Muster ist in Actionspielen Standard (vgl. Valve/Source, Snapshot-Interpolationsansatz und Interpolationspuffer).

Belegt durch:
- Snapshot Interpolation und State Synchronization in Gaffer on Games [1][2][3].
- Source-Engine-Dokumentation zu Entity-Interpolation, Prediction und Lag Compensation [8][10].

-------------------------------------------------------------------------------

## 2. Netzwerktopologie: Autoritativer Client-Server statt P2P

Gewählte Architektur: Autoritativer Client-Server
- Clients senden nur Eingaben (Inputs).
- Server simuliert und hat die alleinige Autorität.
- Server broadcastet den resultierenden Zustand (Snapshots/Ereignisse).

Vorteile
- Sicherheit und Konsistenz: Der Server ist „Source of Truth“, verhindert Cheating und Divergenzen.
- Skalierbarkeit und späte Beitritte: Kein globaler Determinismus nötig.

Alternativen, die wir bewusst NICHT wählen
- Peer-to-Peer mit deterministischem Lockstep (RTS-klassisch):
  - Erfordert starken Determinismus (Bitgleichheit), leidet unter „Warten auf den langsamsten Peer“.
  - Für unsere (nicht deterministische) Action/ECS-Engine ungeeignet.
  - Gaffer: Lockstep-Ansatz und Grenzen [4].

Praxisbezug
- Die Source-Engine (Valve) setzt ebenfalls autoritativen Client-Server mit Prediction, Interpolation und Lag Compensation ein [8].

-------------------------------------------------------------------------------

## 3. Synchronisationsstrategie: Snapshots mit Interpolation (State Sync)

Gewählter Ansatz: State Synchronization mit Snapshot Interpolation
- Server versendet in festen Abständen Zustands-Snapshots für die relevanten Objekte.
- Clients halten einen kleinen Interpolationspuffer und rendern zwischen Snapshots (glatt).
- Inputs laufen unabhängig; verlorene Snapshot-Pakete werden nicht nachgefordert, sondern durch nachfolgende Snapshots „korrigiert“.

Warum das für uns passt
- Industriestandard für schnelle Actionspiele (Quake/Source-Familie) [2][3][8].
- Tolerant gegen Paketverlust: nächster Snapshot korrigiert den Zustand.
- Entkoppelt Rendering-Rate (Client) von Tick-Rate (Server).

Details und Best Practices
- Interpolationspuffer vs. Jitter: Snapshots kommen selten genau gleichmäßig an ein Buffer (z. B. ~100 ms wie in Source als Default) glättet die Darstellung [9].
- Delta/Kompression: Später (Roadmap) Reduktion der Bandbreite durch Deltas und Quantisierung [3].

-------------------------------------------------------------------------------

## 4. Transport- und Protokollebene (TCP/UDP, Framing, NAT-Registrierung)

Transportwahl
- TCP (zuverlässig, in-Order): Handshake/Steuerkanal und zuverlässige Events, z. B.:
  - ConnectRequest/ConnectAck, LevelChangeEvent, EntitySpawnEvent.
- UDP (unzuverlässig, reihenfolgefrei, ohne Head-of-Line-Blocking): Zeitkritisches
  - Eingaben (InputMessage), kontinuierliche Zustände/Snapshots.

Begründung
- Snapshots und Inputs sollen nicht von Retransmits blockiert werden; verlorene Pakete werden toleriert (UDP), s. Gaffer (Snapshots über UDP) [2][3].
- Steuer-/Handshake-Nachrichten sind selten und wichtig → TCP ist einfacher/robuster.

Framing und Limits
- TCP: 4-Byte Length-Field (Big-Endian) + Payload; Netty LengthFieldBasedFrameDecoder.
- Schutzlimits:
  - MAX_TCP_OBJECT_SIZE ~ 1 MiB (Schutz vor zu großen Frames).
  - SAFE_UDP_MTU ~ konservativ (wir nutzen 12001400 B, um Fragmentierung zu vermeiden).
- Hinweis: Source sendet Snapshots typischerweise 2030 pps; Interpolation mit ~100 ms Default [8][9].

NAT/UDP-Registrierung
- Client sendet nach TCP-Handshake mehrmals ein kleines UDP-Registrierungspaket (RegisterUdp(clientId)) an den Server.
- Retransmit (wenige Versuche, kurzer Intervall).
- Abbruch der Retries, sobald der Client sein erstes UDP-Paket vom Server empfängt („Pfad steht“).
- Zweck: Sicherstellen, dass der Server die korrekte UDP-Quelladresse/NAT-Bindung „lernt“.

-------------------------------------------------------------------------------

## 5. Nachrichtenformat und „Network API“

Designprinzip: Zweckgebundene DTOs statt Roh-Components
- Protokollobjekte (Serializable Records/POJOs) enthalten nur die Felder, die für Anzeige/Interaktion nötig sind (z. B. EntityState).
- Interne Server-Representation (Components) bleibt gekapselt; Änderungen an internen Datenstrukturen brechen das Protokoll nicht.

Beispiele (Auszug)
- C2S (Client → Server): ConnectRequest, InputMessage, RegisterUdp, RequestEntitySpawn
- S2C (Server → Client): ConnectAck, LevelChangeEvent, EntitySpawnEvent, SnapshotMessage

Begründung
- Stabile „Network API“: Trennschicht wie bei „Send Tables“/Networking Entities in der Source-Engine (Bandbreitenoptimierung/Interessefilterung) [11][12].
- Effizienz: Wir versenden nur notwendige Felder (Position, Richtung, Animation-State, Health-Auszug …).

-------------------------------------------------------------------------------

## 6. Serverlaufzeit: Tick, Verarbeitung, Broadcast

AuthoritativeServerLoop
- Tick-Rate: z. B. 20 Hz (konfigurierbar).
- Ablauf je Tick
  1) Neue/wegfallende TCP-Clients mit Entities synchronisieren (pro Client ein Hero-Entity).
  2) Eingangsqueue (InputMessage) leeren; Eingaben anwenden (HeroController).
  3) ECS-Frame simulieren (Systems laufen).
- Snapshot-Sender: getrennte feste Rate (z. B. 20 Hz) → SnapshotMessage an alle registrierten UDP-Endpunkte.
- Levelwechsel/GameOver: als zuverlässige Events über TCP.

Belegt durch:
- „Server simuliert, Clients interpolieren“ (Valve/Source) [8].
- Interpolationspuffer (Source-Defaults) [9].

-------------------------------------------------------------------------------

## 7. Clientlaufzeit: Handshake, Eingaben, Empfang, Threading

ClientNetwork
- TCP-Handshake:
  - Nach Verbindungsaufbau ConnectRequest(username) → Server antwortet mit ConnectAck(clientId).
- UDP-Registrierung:
  - RegisterUdp(clientId) unverzüglich + wenige Retries.
  - Abbruch der Retries, sobald erstes UDP-Paket vom Server empfangen wurde.
- Senden:
  - Reliable (TCP) für Steuer-/Event-Nachrichten.
  - Unreliable (UDP) für InputMessage (ggf. clientId anreichern) und Snapshots.
- Empfang/Threading:
  - IO-Threads (Netty) deserialisieren vollständig und legen in einer Thread-sicheren Queue ab.
  - Game-Thread ruft pollAndDispatch() auf, um Messages deterministisch zu verarbeiten (Dispatcher oder optionaler Roh-Consumer).
- Verbindungs-Lifecycle:
  - ConnectionListener (onConnected/onDisconnected) werden auf dem Game-Thread aufgerufen (via Lifecycle-Queue).

-------------------------------------------------------------------------------

## 8. Projektstruktur: Module und Verantwortlichkeiten

Zentrale Schnittstelle (unverändert nutzbar via Game.network)
- core.network.handler.INetworkHandler
  - Einheitliche API für Singleplayer (LocalNetworkHandler) und Remote (NettyNetworkHandler).
- core.network.handler.LocalNetworkHandler
  - Singleplayer: Inputs werden lokal angewendet; kann SnapshotMessage simulieren.
- core.network.handler.NettyNetworkHandler
  - Fassade für Remote: Implementiert INetworkHandler; delegiert im Client-Modus an ClientNetwork, im Server-Modus an ServerRuntime.

Transport/Server/Client
- core.network.client.ClientNetwork
  - TCP/UDP-Client-Transport (Netty), Handshake, UDP-Registrierung mit Retry+Cancel, Inbound-Queues, Lifecycle.
- core.network.server.ServerTransport
  - TCP/UDP-Server (Netty), Client-IDs, UDP-Endpunkte lernen, Input-Queue verwalten, UDP-Senden.
- core.network.server.AuthoritativeServerLoop
  - ECS-Tick, Eingaben anwenden, Entities pro Client, Snapshots broadcasten.
- core.network.server.ServerRuntime
  - Bootstrap für Transport + Loop, Start/Stopp.

Codec/Config
- core.network.codec.NetworkCodec
  - Zentrale Serialisierung/Deserialisierung.
- core.network.config.NetworkConfig
  - Konfiguration/Schranken (z. B. MAX_TCP_OBJECT_SIZE, SAFE_UDP_MTU, Retry-Intervalle, Length-Field-Parameter).

Gemeinsam
- core.network.MessageDispatcher, ConnectionListener, SnapshotTranslator (+ Default), messages.*

Diese Struktur entfernt Duplikate (z. B. Serialisierung, Konstanten), trennt Transport von Spiel-/Protokoll-Logik sauber und macht beide Modi (lokal/remote) über dieselbe Schnittstelle verwendbar.

-------------------------------------------------------------------------------

## 9. Sicherheit, Robustheit und Grenzen

Robustheit
- Größenlimits: TCP-Objektgröße begrenzt; UDP-MTU konservativ, um Fragmentierung zu vermeiden.
- Entkoppelte Raten: Tick-Rate vs. Snapshot-Rate konfigurierbar.
- NAT/UDP-Retries: wenige, kurze Versuche; Abbruch bei Erfolg oder Kanal-Schließung.
- Threading: Jegliche Spiel- und Dispatch-Logik auf dem Game-Thread (Determinismus, Debugbarkeit).

Sicherheit (heute)
- „Authority“ beim Server: Cheating auf Client-Seite kann Zustand nicht direkt manipulieren.
- Eingangsvalidierung (z. B. simple Playername-Regeln).
- Hinweis: Java-Objektserialisierung ist praktisch, aber langfristig zu ersetzen (s. Roadmap).

Grenzen (bewusst)
- Noch keine Client-Side Prediction, Reconciliation oder Lag Compensation bewusst als späterer Schritt (vgl. Source/Valve) [8].
- Keine Delta-/Interessenbasierte Kompression in Snapshots folgt in Roadmap (s. Gaffer) [3].

-------------------------------------------------------------------------------

## 10. Roadmap: Was als Nächstes kommt

Kurzfristig
- Snapshot-Optimierung:
  - Delta-Kompression (Baseline/Acks), Quantisierung/Bitpacking vgl. Gaffer „Snapshot Compression“ [3].
  - Interest Management (nur relevante Entities pro Client) vgl. Valve „Networking Entities“ [11].
- Transport/Codec:
  - Austausch Java-Serialisierung gegen kompaktes Binärformat (Kryo/FlatBuffers/Protobuf o. ä.).
  - Versionierung der Nachrichten (Abwärtskompatibilität).
- Metriken/Telemetrie:
  - Messung von RTT/Jitter/Packet Loss und verknüpfte Adaptionsstrategien.

Mittelfristig
- Client-Side Prediction & Server Reconciliation:
  - Input-Pipelining auf dem Client, Korrektur beim Snapshot (Quake-/Source-Ansatz) [8], Gaffer zu Prediction/Lag [1].
- Lag Compensation (Hit-Scan/Server-Rewind) vgl. Valve [8].
- Zirkulare Interpolationspuffer, adaptive Interpolationszeit abhängig von Netzwerkbedingungen [9].

-------------------------------------------------------------------------------

## 11. Referenzen

[1] Gaffer on Games What Every Programmer Needs To Know About Game Networking (Client/Server, Prediction)
https://gafferongames.com/post/what_every_programmer_needs_to_know_about_game_networking/

[2] Gaffer on Games Snapshot Interpolation (Puffer/Interpolation, UDP, Jitter)
https://gafferongames.com/post/snapshot_interpolation/

[3] Gaffer on Games State Synchronization & Snapshot Compression (Kompression/Delta/Priorisierung)
https://gafferongames.com/post/state_synchronization/
https://gafferongames.com/post/snapshot_compression/

[4] Gaffer on Games Deterministic Lockstep (Determinismus, Grenzen)
https://gafferongames.com/post/deterministic_lockstep/

[8] Valve Developer Community Source Multiplayer Networking (Client/Server, Snapshots, Prediction, Lag Compensation)
https://developer.valvesoftware.com/wiki/Source_Multiplayer_Networking
(Archivversion: https://web.archive.org/web/20200910022723/developer.valvesoftware.com/wiki/Source_Multiplayer_Networking)

[9] Valve Developer Community Source Multiplayer Networking (Entity Interpolation, Interpolation Delay ~100 ms)
Chinesische Lokalisierung mit Interpolationsdetails:
https://developer.valvesoftware.com/wiki/Source_Multiplayer_Networking:zh-cn

[11] Valve Developer Community Networking Entities (Send Tables, Bandbreitenoptimierung, Transmission Filters)
https://developer.valvesoftware.com/wiki/Networking_Entities

[12] Valve Developer Community Networking Events & Messages (Abgrenzung Entities vs. Events/User Messages)
https://developer.valvesoftware.com/wiki/Networking_Events_%26_Messages

Weitere aktuelle Artikel von Glenn Fiedler (Más Bandwidth) zu Modellen und Latenz/Netzqualität:
https://mas-bandwidth.com/choosing-the-right-network-model-for-your-multiplayer-game/
https://mas-bandwidth.com/the-case-for-network-acceleration-for-multiplayer-games/

-------------------------------------------------------------------------------

## Anhang: Kurzübersicht der implementierten Klassen/Module

- core.network.handler.INetworkHandler: Einheitliche API (Singleplayer/Remote).
- core.network.handler.LocalNetworkHandler: Singleplayer-Mock, lokale Anwendung der Inputs, optionaler Snapshot-Emit.
- core.network.handler.NettyNetworkHandler: Fassade, die ClientNetwork bzw. ServerRuntime startet.
- core.network.client.ClientNetwork: Netty-TCP/UDP-Client, Handshake, UDP-Registrierung mit Retry+Cancel, Inbound-Queue, Lifecycle, pollAndDispatch.
- core.network.server.ServerTransport: Netty-TCP/UDP-Server, ClientId-Zuteilung, UDP-Endpunkte lernen, Input-Queue, UDP-Senden.
- core.network.server.AuthoritativeServerLoop: ECS-Simulation (Tick), Input-Anwendung, Snapshot-Broadcast.
- core.network.server.ServerRuntime: Orchestriert Transport + Loop, Start/Stopp.
- core.network.codec.NetworkCodec: Zentrale (De-)Serialisierung.
- core.network.config.NetworkConfig: Konstanten/Limits (MTU, Retries, Frame-Decoder-Konfig).
- core.network.messages.*: Schlanke, zweckgebundene DTOs (C2S/S2C).
