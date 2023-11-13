package task.taskdsltypes;

import dsl.semanticanalysis.types.DSLTypeProperty;
import dsl.semanticanalysis.types.IDSLTypeProperty;

import task.Quiz;

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
