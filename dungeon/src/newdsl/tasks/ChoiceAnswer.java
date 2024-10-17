package newdsl.tasks;

public class ChoiceAnswer extends Answer {

    public ChoiceAnswer() {
    }

    public ChoiceAnswer(String text) {
        this.text = text;
    }

    private boolean isCorrect;
    private String text;

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return this.text;
    }
}
