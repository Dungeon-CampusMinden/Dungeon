package quest;

import controller.ScreenController;
import dslToGame.QuestConfig;

public class QuestFactory {

    public static Quest generateQuestFromConfig(QuestConfig questConfig, ScreenController sc) {
        // TODO replace dummy QuestType with Type in config
        // QuestType questType = questConfig.questType;
        enum QuestType {
            GRAPH_SEARCH_QUEST,
            STATE_MACHINE_BOSS_QUEST,
            DFA_TREASURE_HUNT_QUEST,
            MULTIPLE_CHOICE_QUEST
        }
        QuestType questType = QuestType.GRAPH_SEARCH_QUEST;
        switch (questType) {
            case GRAPH_SEARCH_QUEST -> {
                // todo later
            }
            case STATE_MACHINE_BOSS_QUEST -> {
                // todo later
            }
            case DFA_TREASURE_HUNT_QUEST -> {
                // todo later
            }
            case MULTIPLE_CHOICE_QUEST -> {
                // todo later
            }
        }
        return null;
    }
}
