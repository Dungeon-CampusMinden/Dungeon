package newdsl.tasks;

public class CraftingIngredientAnswer extends CraftingAnswer {

    public CraftingIngredientAnswer(int amount, Alias alias) {
        this.amount = amount;
        this.alias = alias;
    }

    public CraftingIngredientAnswer() {
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
