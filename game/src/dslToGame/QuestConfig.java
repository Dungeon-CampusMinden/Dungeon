package dslToGame;

import graph.Graph;

// TODO: add more fields (entry-point for interpreter, QuestType, etc.)
public record QuestConfig(
        Graph<String> levelGenGraph, String taskDescription, int points, String password) {}
