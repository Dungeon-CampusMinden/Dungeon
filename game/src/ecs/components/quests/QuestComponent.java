package ecs.components.quests;

import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Logger;

import ecs.components.Component;
import ecs.entities.Entity;
import logging.CustomLogLevel;

public class QuestComponent extends Component {

    private transient final Logger questLogger = Logger.getLogger(this.getClass().getName());
    private ArrayList<Quest> questLog = new ArrayList<>();
    private Quest pinnedQuest;

    /**
     * Creates a new QuestComponent
     * 
     * @param entity the entity that owns this QuestComponent
     */
    public QuestComponent(Entity entity) {
        super(entity);
    }

    /**
     * Creates a new QuestComponent with the given quests
     * 
     * @param entity   entity that owns this QuestComponent
     * @param questLog Already populated QuestLog
     */
    public QuestComponent(Entity entity, ArrayList<Quest> questLog) {
        this(entity);
        this.questLog = questLog;
    }

    /**
     * 
     * @param quest adds quest to the questlog
     */
    public void addQuest(Quest quest) {
        questLog.add(0, quest);
        questLogger.info("New quest " + quest.getName() + " added to questLog");
    }

    /**
     * If the quest is already part of the questLog then the quest will be pinned
     * otherwise the quest will be added to the questlog and then pinned
     * 
     * @param quest
     */
    public void pinQuest(Quest quest) {
        if (!questLog.contains(quest)) {
            addQuest(quest);
        }
        pinnedQuest = quest;
        questLogger.info("Quest " + quest.getName() + " pinned to questLog");
    }

    /**
     * 
     * @return returns Optional of the pinned field
     */
    public Optional<Quest> pinnedQuest() {
        return Optional.ofNullable(pinnedQuest);
    }

    /**
     * 
     * @return returns Optional of null if the questlog is empty
     *         or if the latest quest is null
     *         <p>
     *         otherwise returns Optional of the last added quest
     */
    public Optional<Quest> latestQuest() {
        questLogger.log(CustomLogLevel.DEBUG, "Questlog is empty: " + questLog.isEmpty());
        if (questLog.isEmpty())
            return Optional.empty();
        return Optional.ofNullable(questLog.get(0));
    }

    /**
     * Updates unfinished quests and removes finished quests
     */
    public void update() {
        questLog.stream()
                // considers only non-null values
                .filter(q -> q != null)
                // updates all quests
                .forEach(q -> q.update());
        questLogger.log(CustomLogLevel.DEBUG,
                "Quests updated for " + entity.getClass().getSimpleName());
        removeQuest();
        questLogger.log(CustomLogLevel.DEBUG, "Quests removed for " + entity.getClass().getSimpleName());
    }

    private void removeQuest() {
        // Removes all empty slots and finished quests
        int i = 0;
        while (i < questLog.size()) {
            if (questLog.isEmpty())
                break;
            if (questLog.get(i) == null) {
                questLog.remove(i);
                continue;
            }
            if (questLog.get(i).getTask().isCompleted()) {
                if (pinnedQuest != null && questLog.get(i).equals(pinnedQuest) && !questLog.get(i).equals(pinnedQuest))
                    pinnedQuest = latestQuest().get();
                questLog.remove(i);
                i++;
                continue;
            }
            i++;
        }
    }

    public ArrayList<Quest> getQuestLog() {
        return questLog;
    }

}
