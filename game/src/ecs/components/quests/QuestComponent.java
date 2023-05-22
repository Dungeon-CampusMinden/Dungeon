package ecs.components.quests;

import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Logger;

import ecs.components.Component;
import ecs.entities.Entity;

public class QuestComponent extends Component {

    private transient final Logger questLogger = Logger.getLogger(this.getClass().getName());
    private ArrayList<Optional<Quest>> questLog = new ArrayList<>();
    private Quest pinnedQuest;

    public QuestComponent(Entity entity) {
        super(entity);
    }

    /**
     * 
     * @param quest adds quest to the questlog
     */
    public void addQuest(Quest quest) {
        questLog.add(0, Optional.ofNullable(quest));
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
        if (questLog.isEmpty())
            return Optional.empty();
        return questLog.get(0);
    }

    /**
     * 
     */
    public void update() {
        questLog.stream()
                // considers only non-null values
                .flatMap(o -> o.stream())
                // updates all quests
                .forEach(q -> q.update());
        // Removes all empty slots and finished quests
        int i = 0;
        while (i < questLog.size()) {
            if (!questLog.get(i).isPresent()) {
                questLog.remove(i);
                continue;
            }
            if (questLog.get(i).get().getTask().isCompleted()) {
                questLog.remove(i);
                continue;
            }
            i++;
        }
    }

    public ArrayList<Optional<Quest>> getQuestLog() {
        return questLog;
    }

}
