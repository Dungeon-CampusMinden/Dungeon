package newdsl.tasks;

public class CraftingSolutionAnswer extends CraftingAnswer {

    public CraftingSolutionAnswer(int amount, Alias alias) {
        this.amount = amount;
        this.alias = alias;
    }

    public CraftingSolutionAnswer() {
    }

    private int amount;
    private Alias alias;

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Alias getAlias() {
        return alias;
    }

    public void setAlias(Alias alias) {
        this.alias = alias;
    }

}
