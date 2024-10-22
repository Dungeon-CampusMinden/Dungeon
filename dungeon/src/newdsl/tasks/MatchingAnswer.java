package newdsl.tasks;

public class MatchingAnswer extends Answer {

    public MatchingAnswer(Alias left, Alias right) {
        this.left = left;
        this.right = right;
    }

    public MatchingAnswer() {
    }

    private Alias left;
    private Alias right;

    public Alias getLeft() {
        return left;
    }

    public void setLeft(Alias left) {
        this.left = left;
    }

    public Alias getRight() {
        return right;
    }

    public void setRight(Alias right) {
        this.right = right;
    }

}
