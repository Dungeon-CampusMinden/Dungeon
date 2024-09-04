package newdsl.tasks;

public class ParameterAnswer extends Answer {
    private String parameter;
    private String value;

    public ParameterAnswer(String parameter, String value) {
        this.parameter = parameter;
        this.value = value;
    }

    public ParameterAnswer() {
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
