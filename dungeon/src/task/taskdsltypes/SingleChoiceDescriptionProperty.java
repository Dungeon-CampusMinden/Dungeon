package task.taskdsltypes;

import semanticanalysis.types.DSLTypeProperty;
import semanticanalysis.types.IDSLTypeProperty;

import task.quizquestion.Quiz;

@DSLTypeProperty(name = "description", extendedType = SingleChoiceTask.class)
public class SingleChoiceDescriptionProperty implements IDSLTypeProperty<Quiz, String> {
    public static SingleChoiceDescriptionProperty instance = new SingleChoiceDescriptionProperty();

    private SingleChoiceDescriptionProperty() {}

    @Override
    public void set(Quiz instance, String valueToSet) {
        instance.taskText(valueToSet);
    }

    @Override
    public String get(Quiz instance) {
        return instance.taskText();
    }
}
