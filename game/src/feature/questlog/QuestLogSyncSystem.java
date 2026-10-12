package feature.questlog;

import engine.Game;
import engine.System;
import engine.components.PlayerComponent;
import engine.game.PreRunConfiguration;
import engine.network.NetworkUtils;
import engine.network.messages.s2c.QuestLogStateMessage;
import engine.network.server.ClientState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Server-side system that keeps every client's copy of the shared quest log up to date.
 *
 * <p>Whenever the quest log changes, is replaced, or disappears, each client that finished its
 * initial world sync reliably receives the complete quest log it may see. Entries marked only for
 * their creator are sent only to that player. Clients apply the state through {@link
 * QuestLogUtil#applyClientState(QuestLogStateMessage)}.
 */
public final class QuestLogSyncSystem extends System {

  private final Map<Short, SentState> sentStates = new HashMap<>();

  /** Creates the quest log synchronization system. */
  public QuestLogSyncSystem() {
    super(AuthoritativeSide.SERVER);
  }

  @Override
  public void execute() {
    if (!PreRunConfiguration.multiplayerEnabled() || !PreRunConfiguration.isNetworkServer()) {
      return;
    }

    QuestLogComponent questLog = QuestLogUtil.getQuestLogComponent().orElse(null);
    SentState current = new SentState(questLog, questLog == null ? 0 : questLog.revision());
    List<ClientState> clients = NetworkUtils.readyClients();
    Set<Short> clientIds = clients.stream().map(ClientState::clientId).collect(Collectors.toSet());
    sentStates.keySet().retainAll(clientIds);

    for (ClientState client : clients) {
      if (current.equals(sentStates.get(client.clientId()))) continue;
      Game.network().send(client.clientId(), stateFor(questLog, viewerName(client)), true);
      sentStates.put(client.clientId(), current);
    }
  }

  private static QuestLogStateMessage stateFor(QuestLogComponent questLog, String viewerName) {
    if (questLog == null) {
      return new QuestLogStateMessage(false, List.of());
    }

    List<QuestLogStateMessage.Entry> entries = new ArrayList<>();
    for (String tab : questLog.getQuestlogTabs()) {
      for (QuestLogEntry entry : QuestLogUI.visibleEntriesFor(questLog, tab, viewerName)) {
        entries.add(
            new QuestLogStateMessage.Entry(
                tab,
                entry.text(),
                entry.timestamp(),
                entry.userCreated(),
                entry.owner(),
                entry.onlyForCreator(),
                entry.title()));
      }
    }
    return new QuestLogStateMessage(true, entries);
  }

  private static String viewerName(ClientState client) {
    return client
        .playerEntity()
        .flatMap(player -> player.fetch(PlayerComponent.class))
        .map(PlayerComponent::playerName)
        .orElse(client.username());
  }

  /**
   * Quest log instance and revision last sent to a client.
   *
   * @param questLog quest log instance, or {@code null} if no quest log existed
   * @param revision revision of that quest log
   */
  private record SentState(QuestLogComponent questLog, int revision) {}
}
