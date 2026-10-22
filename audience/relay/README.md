# Publikums-Relay

Das Relay verbindet ein Spiel im Publikumsmodus mit den Geräten des Publikums. Es liefert die
Webseite aus, hält die Sitzungen im Speicher und reicht Nachrichten weiter: den öffentlichen
Spielzustand an die Geräte und deren Eingaben an das Spiel. Es enthält keine Spiellogik und
keine Datenbank. Das Spiel beschreibt [`game/doc/audience.md`](../../game/doc/audience.md).

## Lokal starten

```bash
./gradlew :audience:relay:run
```

Das Relay lauscht auf `127.0.0.1:8090`, die Seite liegt unter `http://127.0.0.1:8090/play/`. Ein
Spiel nutzt dieses lokale Relay, solange nichts anderes eingestellt ist. Die Adresse eines
öffentlichen Relays bekommt ein Spiel-Build über die Umgebungsvariable `DUNGEON_AUDIENCE_RELAY`
beim Bauen, etwa `DUNGEON_AUDIENCE_RELAY=https://example.org/play`, wie der Tracking-Endpoint über
`DUNGEON_TRACKING_ENDPOINT`. Dieselbe Variable oder die Systemeigenschaft `dungeon.audience.relay`
beim Start überschreibt sie.

| Zweck | Umgebungsvariable | Systemeigenschaft | Standardwert |
| --- | --- | --- | --- |
| Bindeadresse | `DUNGEON_RELAY_BIND_HOST` | `dungeon.relay.bindHost` | `127.0.0.1` |
| Port | `DUNGEON_RELAY_PORT` | `dungeon.relay.port` | `8090` |
| Gleichzeitige Sitzungen | `DUNGEON_RELAY_MAX_SESSIONS` | `dungeon.relay.maxSessions` | `50` |
| Geräte pro Sitzung | `DUNGEON_RELAY_MAX_PARTICIPANTS` | `dungeon.relay.maxParticipants` | `300` |
| Minuten ohne Spielkontakt bis zum Löschen | `DUNGEON_RELAY_IDLE_MINUTES` | `dungeon.relay.idleMinutes` | `2` |
| Schlüssel zum Anlegen von Sitzungen | `DUNGEON_RELAY_CREATE_KEY` | `dungeon.relay.createKey` | keiner |

Ein öffentliches Relay braucht einen Schlüssel, sonst kann jeder Sitzungen anlegen, alle Plätze
belegen und eigene Umfragen ausliefern. Das Spiel bekommt denselben Schlüssel beim Start über
`DUNGEON_AUDIENCE_RELAY_KEY` oder die Systemeigenschaft `dungeon.audience.relayKey`, nicht beim
Bauen, weil er sonst im Jar stünde.

Eine Sitzung endet, wenn das Spiel sie schließt, auch beim Beenden des Spiels. Stürzt das Spiel
ab, fragt es keine Eingaben mehr ab; nach der Wartezeit ohne Spielkontakt löscht das Relay die
Sitzung. Das Spiel fragt mindestens alle 25 Sekunden nach, die Wartezeit muss darüber liegen.
In beiden Fällen zeigen die Geräte, dass die Runde vorbei ist.

## API

Alle Pfade liegen unter `/play`. Das Spiel schickt beim Anlegen den Schlüssel des Relays als
`Authorization: Bearer`, danach das Spieltoken aus der Antwort. Ein Gerät authentifiziert sich
mit seinem Token aus `join`.

| Methode und Pfad | Wer | Zweck |
| --- | --- | --- |
| `POST /api/game/sessions` | Spiel | legt eine Sitzung an; Body `{"surveys": {"pre": ..., "post": ...}}`, Antwort `{"code", "gameToken"}` |
| `PUT /api/game/sessions/{code}/state` | Spiel | ersetzt den öffentlichen Zustand |
| `GET /api/game/sessions/{code}/inbox?after=n` | Spiel | wartet bis zu 25 Sekunden auf Eingaben mit höherer Nummer als `n` |
| `DELETE /api/game/sessions/{code}` | Spiel | beendet die Sitzung |
| `POST /api/sessions/{code}/join` | Gerät | tritt bei oder wieder bei; Body `{"token"?}`, Antwort `{"token", "pid", "surveys"}` |
| `GET /api/sessions/{code}/events?token=` | Gerät | Server-Sent Events `state` mit `{"ageMs", "state"}` |
| `POST /api/sessions/{code}/input` | Gerät | `{"token", "type": "vote", "window", "value"}` oder `{"token", "type": "survey", "phase", "answers"}` |
| `GET /api/health` | Monitoring | `{"status": "ok"}` |

Das Spiel sieht nur die öffentliche Teilnehmerkennung `pid`, nie das Token. Ein Gerät darf
höchstens zehn Eingaben pro Sekunde schicken.

## Betrieb

`audience/relay/Dockerfile` baut ein Image, das als UID 10001 auf Port 8090 lauscht. Der
Reverse-Proxy leitet `/play` an das Relay weiter. Für die Event-Streams muss er die Pufferung
abschalten und lange Verbindungen erlauben, etwa in nginx:

```nginx
location /play/ {
    proxy_pass http://127.0.0.1:8090;
    proxy_http_version 1.1;
    proxy_set_header Connection "";
    proxy_buffering off;
    proxy_read_timeout 1h;
}
```

Hinter einem gemeinsamen WLAN teilen sich viele Geräte oft eine öffentliche IP-Adresse. Ein Rate-Limit
pro IP-Adresse im Proxy muss deshalb mehrere Dutzend Anfragen pro Sekunde erlauben. Der Code einer
Sitzung ist öffentlich; damit ein Skript nicht alle Plätze belegt, sollte der Proxy Beitritte
strenger begrenzen, etwa:

```nginx
limit_req_zone $binary_remote_addr zone=audience_join:10m rate=3r/s;

location ~ ^/play/api/sessions/[^/]+/join$ {
    limit_req zone=audience_join burst=100 nodelay;
    proxy_pass http://127.0.0.1:8090;
}
```

Damit treten von einer Adresse sofort 100 Geräte bei und danach drei pro Sekunde.
