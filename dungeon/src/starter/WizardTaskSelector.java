package starter;

import contrib.components.InteractionComponent;

import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.TileLevel;
import core.level.elements.ILevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;

import dungeonFiles.DSLEntryPoint;

import task.Quiz;
import task.Task;
import task.TaskContent;
import task.quizquestion.SingleChoice;
import task.quizquestion.UIAnswerCallback;

import java.io.IOException;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * This class contains static methods to create the Wizard-Level to select the tasks at the start of
 * the game.
 *
 * <p>This class is part of the {@link Starter} and should not be used otherwise. The code is
 * extracted into this class for better code readability.
 */
public class WizardTaskSelector {
    protected static DSLEntryPoint selectedDSLEntryPoint = null;

    protected static ILevel wizardLevel() {
        // default layout is:
        //
        // W W W W W
        // W F F F W
        // W F F F W
        // W F F F W
        // W W W W W
        ILevel level =
                new TileLevel(
                        new LevelElement[][] {
                            new LevelElement[] {
                                LevelElement.WALL,
                                LevelElement.WALL,
                                LevelElement.WALL,
                                LevelElement.WALL,
                                LevelElement.WALL,
                            },
                            new LevelElement[] {
                                LevelElement.WALL,
                                LevelElement.FLOOR,
                                LevelElement.FLOOR,
                                LevelElement.FLOOR,
                                LevelElement.WALL,
                            },
                            new LevelElement[] {
                                LevelElement.WALL,
                                LevelElement.FLOOR,
                                LevelElement.FLOOR,
                                LevelElement.FLOOR,
                                LevelElement.WALL,
                            },
                            new LevelElement[] {
                                LevelElement.WALL,
                                LevelElement.FLOOR,
                                LevelElement.FLOOR,
                                LevelElement.FLOOR,
                                LevelElement.WALL,
                            },
                            new LevelElement[] {
                                LevelElement.WALL,
                                LevelElement.WALL,
                                LevelElement.WALL,
                                LevelElement.WALL,
                                LevelElement.WALL,
                            }
                        },
                        DesignLabel.randomDesign());
        level.changeTileElementType(level.endTile(), LevelElement.FLOOR);
        return level;
    }

    protected static SingleChoice selectTaskQuestion(Set<DSLEntryPoint> entryPoints) {
        SingleChoice question = new SingleChoice("Wähle deine Mission:");
        entryPoints.forEach(ep -> question.addAnswer(new PayloadTaskContent(ep)));
        question.state(Task.TaskState.ACTIVE);
        return question;
    }

    protected static Entity wizard(SingleChoice selectionQuestion) throws IOException {
        Entity wizard = new Entity("Selection Wizard");
        wizard.addComponent(new DrawComponent("character/wizard"));
        wizard.addComponent(new PositionComponent());
        wizard.addComponent(
                new InteractionComponent(
                        1,
                        true,
                        UIAnswerCallback.askOnInteraction(
                                selectionQuestion, setSelectedEntryPoint())));

        return wizard;
    }

    private static BiConsumer<Task, Set<TaskContent>> setSelectedEntryPoint() {
        return (task, taskContents) -> {
            selectedDSLEntryPoint =
                    ((PayloadTaskContent)
                                    taskContents.stream()
                                            .findFirst()
                                            .orElseThrow(
                                                    () ->
                                                            new RuntimeException(
                                                                    "Something went wrong at selecting the DSLEntryPoint")))
                            .payload();
            task.state(Task.TaskState.FINISHED_CORRECT);
        };
    }

    private static class PayloadTaskContent extends Quiz.Content {
        private final DSLEntryPoint payload;

        public PayloadTaskContent(DSLEntryPoint payload) {
            super(payload.displayName());
            this.payload = payload;
        }

        public DSLEntryPoint payload() {
            return payload;
        }
    }
}
