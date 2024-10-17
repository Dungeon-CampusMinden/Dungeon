package newdsl.foreigncode;

import newdsl.tasks.Answer;
import newdsl.tasks.ChoiceAnswer;
import newdsl.tasks.ParameterAnswer;
import org.python.core.PyBoolean;
import org.python.core.PyFunction;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class PythonHandler {

    private String toChoiceAnswer(ChoiceAnswer ans) {
        return String.format("{\"isCorrect\": %s, \"name\": \"%s\"}", ans.isCorrect(), ans.getText());
    }

    public static boolean handlePassForChoiceAnswer(String code, float points) {
        PythonInterpreter interpreter = new PythonInterpreter();

        interpreter.exec(String.format("grade = %s", code.trim()));
        interpreter.set("points", points);

        PyObject result = interpreter.eval("grade(points)");

        interpreter.close();

        return ((PyBoolean) result).getBooleanValue();
    }

    public static boolean gradeCalculation(String code, List<ParameterAnswer> params, ParameterAnswer answer) {
        PythonInterpreter interpreter = new PythonInterpreter();

        interpreter.exec(String.format("grade = %s", code.trim()));
        params.forEach(param -> interpreter.set(param.getParameter(), Integer.parseInt(param.getValue())));

        String toEval = String.format("grade(%s) == %s", String.join(",", params.stream().map(ParameterAnswer::getParameter).toList()), Integer.parseInt(answer.getValue()));

        PyObject result = interpreter.eval(toEval);

        interpreter.close();

        return ((PyBoolean) result).getBooleanValue();
    }

    public static float handleChoiceScoring(String code, Set<ChoiceAnswer> givenAnswers, Set<ChoiceAnswer> taskAnswers) {
        PythonInterpreter interpreter = new PythonInterpreter();

        interpreter.exec(String.format("score = %s", code.trim()));

        interpreter.set("givenAnswers", givenAnswers.stream().map(Answer::toString).toArray());
        interpreter.set("allAnswers", taskAnswers.stream().map(Answer::toString).toArray());
        interpreter.set("correctAnswers", taskAnswers.stream().filter(ChoiceAnswer::isCorrect).map(Answer::toString).toArray());

        double result = interpreter.eval("score(givenAnswers, allAnswers, correctAnswers)").asDouble();

        interpreter.close();

        float score = Float.parseFloat(result + "");
        return score;
    }

    public static PyObject exec(String code, String functionName, List<PyObject> args) {
        PythonInterpreter interpreter = new PythonInterpreter();
        // Define a Python function and call it from Java
        interpreter.exec(code);
        PyFunction function = interpreter.get(functionName, PyFunction.class);
        PyObject result = function.__call__(new PyInteger(5), new PyInteger(3));

        interpreter.close();

        return result;
    }


}
