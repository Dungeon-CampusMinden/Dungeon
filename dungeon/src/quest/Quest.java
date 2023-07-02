package quest;

import core.level.elements.ILevel;
import core.level.generator.IGenerator;

import dslToGame.QuestConfig;

public abstract class Quest {
    protected QuestConfig questConfig;
    protected IGenerator generator;
    protected ILevel root;
    protected String questText;

    protected int maxscore;

    public Quest(QuestConfig questConfig) {
        this.questConfig = questConfig;
        this.maxscore = questConfig.questPoints();
        this.questText = questConfig.questDesc();
    }

    public IGenerator generator() {
        return generator;
    }

    public void rootLevel(ILevel root) {
        this.root = root;
    }

    public abstract void addQuestObjectsToLevels();

    public abstract void addQuestUIElements();

    public abstract int evaluateUserPerformance();

    public abstract void onLevelLoad(ILevel currentLevel);
}
