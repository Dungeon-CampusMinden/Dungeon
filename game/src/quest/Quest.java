package quest;

import dslToGame.QuestConfig;
import level.generator.IGenerator;

public abstract class Quest {
    protected QuestConfig questConfig;
    protected IGenerator generator;

    public Quest(QuestConfig questConfig) {
        this.questConfig = questConfig;
    }

    public IGenerator getGenerator() {
        return generator;
    }
}
