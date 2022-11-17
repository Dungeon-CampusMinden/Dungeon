package quest;

import controller.EntityController;
import dslToGame.QuestConfig;
import level.elements.ILevel;
import level.generator.IGenerator;
import minimap.IMinimap;

public abstract class Quest {
    protected QuestConfig questConfig;
    protected IGenerator generator;
    protected ILevel root;
    protected IMinimap minimap;

    public Quest(QuestConfig questConfig) {
        this.questConfig = questConfig;
    }

    public IGenerator getGenerator() {
        return generator;
    }

    public void setRootLevel(ILevel root) {
        this.root = root;
    }

    public void setMinimap(IMinimap minimap) {
        this.minimap = minimap;
    }

    public abstract void addQuestObjectsToLevels();

    public abstract void addQuestUIElements();

    public abstract void evaluateUserPerformance();

    public abstract void onLevelLoad(ILevel currentLevel, EntityController entityController);
}
