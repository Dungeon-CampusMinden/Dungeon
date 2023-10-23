# Fehlerbehebung

## Inhaltsverzeichnis

- [Fehlerbehebung](#fehlerbehebung)
  - [Inhaltsverzeichnis](#inhaltsverzeichnis)
  - [Fehleranalyse](#fehleranalyse)
    - [Generierter Code](#generierter-code)
    - [Server Response](#server-response)

## Fehleranalyse

Alle Fehler, die während der Laufzeit auftreten können, werden in der Konsole des Browsers ausgegeben. Falls während der Benutzung oder Entwicklung der Anwendung Fehler auftreten, sollte hier zuerst nachgesehen werden.

Fehler, die während der Kommunikation mit dem Server auftreten, als auch Rückmeldungen nach erfolgreichen Anfragen werden ebenfalls in der Konsole angezeigt.

### Generierter Code

Falls die Anwendung nicht wie erwartet funktioniert, kann es sein, dass der generierte Code nicht korrekt ist. Dieser wird in einem separaten Fenster angezeigt.

Das Fenster, in dem der generierte angezeigt wird. Kann über die `config.ts` an und ausgeschaltet werden. Während der Entwicklung bietet es sich an, das Fenster zu aktivieren, um den generierten Code direkt überprüfen zu können.

```ts
export const config: Config = {
  HIDE_GENERATED_CODE: false, // <- Hier kann das Fenster eingeblendet oder ausgeblendet werden
};
```

### Server Response

Der Server gibt bei jeder Anfrage eine Rückmeldung, welche den HTTP-Statuscode und die aktuelle Position des Charakters enthält. Um diese Informationen direkt im Browser anzuzeigen, kann die `config.ts` angepasst werden.

```ts
export const config: Config = {
  HIDE_RESPONSE_INFO: true, // <- Hier kann das Fenster eingeblendet oder ausgeblendet werden
};
```
