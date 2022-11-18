package quest;

import controller.EntityController;
import controller.ScreenController;
import dslToGame.QuestConfig;
import level.elements.ILevel;
import level.generator.IGenerator;
import minimap.IMinimap;

public abstract class Quest {
    protected QuestConfig questConfig;
    protected IGenerator generator;
    protected ILevel root;
    protected IMinimap minimap;

    protected String questText;

    protected int maxscore;

    public Quest(QuestConfig questConfig) {
        this.questConfig = questConfig;
        this.maxscore = questConfig.points();
        this.questText = questConfig.taskDescription();
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

    public abstract void addQuestUIElements(ScreenController sc);

    public abstract int evaluateUserPerformance();

    public abstract void onLevelLoad(ILevel currentLevel, EntityController entityController);
}
