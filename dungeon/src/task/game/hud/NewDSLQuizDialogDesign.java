package task.game.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogDesign;
import newdsl.ast.ASTNodes;
import newdsl.foreigncode.LaTeXHandler;
import newdsl.tasks.ChoiceAnswer;
import newdsl.tasks.SingleChoiceTask;
import newdsl.tasks.Task;
import task.tasktype.Quiz;

import java.util.List;

public class NewDSLQuizDialogDesign {

    public static final String ANSWERS_GROUP_NAME = "Answers";

    private static final String QUIZ_MESSAGE_TASK = "Aufgabenstellung";
    private static final String QUIZ_MESSAGE_SOLUTION = "Lösung";

    public static VerticalGroup createAnswerButtonsForChoiceTasks(Skin skin, List<ChoiceAnswer> answers, ASTNodes.TaskType taskType) {
        VerticalGroup answerButtons = new VerticalGroup();

        ButtonGroup<CheckBox> btnGroup = new ButtonGroup<>();
        btnGroup.setMinCheckCount(0);
        btnGroup.uncheckAll();
        final CheckBox.CheckBoxStyle style;

        if (taskType == ASTNodes.TaskType.SINGLE_CHOICE) style = skin.get("radio", CheckBox.CheckBoxStyle.class);
        else style = skin.get("default", CheckBox.CheckBoxStyle.class);

        answers.stream().map(answer -> new CheckBox(UIUtils.formatString(answer.getText()), style)).forEach(checkBox -> {
            btnGroup.add(checkBox);
            answerButtons.addActor(checkBox);
            checkBox.left();
        });

        if (taskType == ASTNodes.TaskType.MULTIPLE_CHOICE) btnGroup.setMaxCheckCount(answers.size());
        else if (taskType == ASTNodes.TaskType.SINGLE_CHOICE) btnGroup.setMaxCheckCount(1);

        answerButtons.align(Align.left);
        answerButtons.left();
        answerButtons.space(10);

        return answerButtons;
    }

    public static Group createQuizQuestion(Task<ChoiceAnswer> quizQuestion, Skin skin, String outputMsg) {
        Label labelExercise = new Label(QUIZ_MESSAGE_TASK, skin);
        labelExercise.setColor(Color.YELLOW);
        Label labelSolution = new Label(QUIZ_MESSAGE_SOLUTION, skin);
        labelSolution.setColor(Color.GREEN);
        VerticalGroup vg = new VerticalGroup();
        vg.addActor(labelExercise);
        vg.addActor(visualizeQuestionSection(quizQuestion, skin));
        vg.addActor(labelSolution);
        vg.addActor(visualizeAnswerSectionForChoiceAnswers(quizQuestion.getAnswers(), skin, quizQuestion.getType()));
        vg.grow();
        return vg;
    }

    private static Group visualizeQuestionSection(Task<ChoiceAnswer> questionContent, Skin skin) {
        VerticalGroup vg = new VerticalGroup();
        vg.addActor(DialogDesign.createScrollPane(skin, new Label(UIUtils.formatString(questionContent.getTitle()), skin)));
        vg.grow();
        return vg;
    }

    private static Group visualizeAnswerSectionForChoiceAnswers(List<ChoiceAnswer> answers, Skin skin, ASTNodes.TaskType taskType) {
        VerticalGroup vg = new VerticalGroup();
        VerticalGroup btnGrp = createAnswerButtonsForChoiceTasks(skin, answers, taskType);
        btnGrp.fill();
        btnGrp.left();
        vg.addActor(DialogDesign.createScrollPane(skin, btnGrp));
        vg.grow();
        vg.setName(ANSWERS_GROUP_NAME);
        return vg;
    }
}
