package newdsl.tasks;

import newdsl.ast.ASTNodes;

import java.util.List;

public class CraftingRuleAnswer extends CraftingAnswer {

    public CraftingRuleAnswer(List<CraftingIngredientAnswer> left, ASTNodes.CraftingStrictness strictness, List<CraftingIngredientAnswer> right) {
        this.left = left;
        this.strictness = strictness;
        this.right = right;
    }

    public CraftingRuleAnswer() {
    }

    private List<CraftingIngredientAnswer> left;
    private ASTNodes.CraftingStrictness strictness;
    private List<CraftingIngredientAnswer> right;

    public List<CraftingIngredientAnswer> getLeft() {
        return left;
    }

    public void setLeft(List<CraftingIngredientAnswer> left) {
        this.left = left;
    }

    public ASTNodes.CraftingStrictness getStrictness() {
        return strictness;
    }

    public void setStrictness(ASTNodes.CraftingStrictness strictness) {
        this.strictness = strictness;
    }

    public List<CraftingIngredientAnswer> getRight() {
        return right;
    }

    public void setRight(List<CraftingIngredientAnswer> right) {
        this.right = right;
    }

}
