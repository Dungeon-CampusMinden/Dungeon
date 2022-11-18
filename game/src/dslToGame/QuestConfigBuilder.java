package dslToGame;

import graph.Graph;

public class QuestConfigBuilder {
    private graph.Graph<String> levelGenGraph;
    private String questDescription;
    private int points;

    public QuestConfigBuilder() {
        levelGenGraph = new Graph<>(null, null);
        questDescription = "";
    }

    public QuestConfigBuilder setGraph(graph.Graph<String> graph) {
        this.levelGenGraph = graph;
        return this;
    }

    public QuestConfigBuilder setDescription(String description) {
        this.questDescription = description;
        return this;
    }

    public QuestConfigBuilder setPoints(int points) {
        this.points = points;
        return this;
    }

    public QuestConfig build() {
        return new QuestConfig(this.levelGenGraph, this.questDescription, this.points);
    }
}
