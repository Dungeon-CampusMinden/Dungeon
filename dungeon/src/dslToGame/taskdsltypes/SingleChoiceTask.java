package dslToGame.taskdsltypes;

import semanticanalysis.types.DSLType;
import semanticanalysis.types.DSLTypeMember;
import task.quizquestion.Quiz;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

@DSLType
public class SingleChoiceTask {
    @DSLTypeMember String description;
    @DSLTypeMember ArrayList<Quiz.Content> answers;
    @DSLTypeMember int correctAnswerIndex;
    @DSLTypeMember BiFunction<SingleChoiceTask, List<Quiz.Content>, Float> scoreFunction;
}
