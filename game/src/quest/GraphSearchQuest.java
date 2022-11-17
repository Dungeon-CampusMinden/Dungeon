package quest;

import dslToGame.QuestConfig;
import levelgraph.GraphLevelGenerator;

public class GraphSearchQuest extends Quest {
    public GraphSearchQuest(QuestConfig questConfig) {
        super(questConfig);
        generator = new GraphLevelGenerator(questConfig.levelGenGraph());
    }
    // Kisten in Level verteilen
    // Bewertung durchführen
    // UI
}
