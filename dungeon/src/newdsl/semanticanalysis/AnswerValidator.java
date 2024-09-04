package newdsl.semanticanalysis;

import newdsl.ast.ASTNodes;

import java.util.Optional;

public class AnswerValidator {

    private static Optional<String> exactlyOneCorrectAnswer(ASTNodes.AnswerListNode answers) {
        boolean passValidation = true;

        boolean hasCorrect = false;

        for (ASTNodes.AnswerNode answer : answers.answers) {
            if (answer instanceof ASTNodes.ChoiceAnswerNode) { // Should only occur for single-choice
                boolean isCorrect = ((ASTNodes.ChoiceAnswerNode) answer).isCorrect;
                if (isCorrect) {
                    if (hasCorrect) {
                        passValidation = false;
                        break;
                    }
                    hasCorrect = true;
                }
            }
        }

        return passValidation ? Optional.empty() : Optional.of("Please provide exactly one correct answer");
    }

    private static Optional<String> exactlyOneCorrectAndAtLeastOneIncorrectAnswer(ASTNodes.AnswerListNode answers) {
        boolean passValidation = true;

        boolean hasCorrect = false;
        boolean hasIncorrect = false;

        for (ASTNodes.AnswerNode answer : answers.answers) {
            if (answer instanceof ASTNodes.ChoiceAnswerNode) { // Should only occur for single-choice
                boolean isCorrect = ((ASTNodes.ChoiceAnswerNode) answer).isCorrect;
                if (isCorrect) {
                    if (hasCorrect) {
                        passValidation = false;
                        break;
                    }
                    hasCorrect = true;
                } else {
                    hasIncorrect = true;
                }
            }
        }

        if (!(hasCorrect && hasIncorrect)) {
            passValidation = false;
        }

        return passValidation ? Optional.empty() : Optional.of("Please provide exactly one correct and at last one incorrect answer");
    }

    private static Optional<String> atLeastOneCorrectAndOneIncorrectAnswer(ASTNodes.AnswerListNode answers) {
        boolean passValidation = true;

        boolean hasCorrect = false;
        boolean hasIncorrect = false;

        for (ASTNodes.AnswerNode answer : answers.answers) {
            if (answer instanceof ASTNodes.ChoiceAnswerNode) { // Should only occur for single-choice
                boolean isCorrect = ((ASTNodes.ChoiceAnswerNode) answer).isCorrect;
                if (isCorrect) {
                    hasCorrect = true;
                } else {
                    hasIncorrect = true;
                }
            }
        }

        if (!(hasCorrect && hasIncorrect)) {
            passValidation = false;
        }

        return passValidation ? Optional.empty() : Optional.of("Please provide at least one correct and at least one incorrect answer");
    }



    private static Optional<String> atLeastOneMatchingPair(ASTNodes.AnswerListNode answers) {
        boolean passValidation = false;

        for (ASTNodes.AnswerNode answer : answers.answers) {
            if (answer instanceof ASTNodes.MatchingAnswerNode) { // Should only occur for matching answers
                if (((ASTNodes.MatchingAnswerNode) answer).left != null && ((ASTNodes.MatchingAnswerNode) answer).right != null) {
                    passValidation = true;
                    break;
                }
            }
        }
        return passValidation ? Optional.empty() : Optional.of("Please provide at least one matching pair that is not empty on either side");
    }

    public static Optional<String> isValid(ASTNodes.TaskType taskType, ASTNodes.AnswerListNode answers) {
        switch (taskType) {
            case SINGLE_CHOICE -> {
                return exactlyOneCorrectAndAtLeastOneIncorrectAnswer(answers);
            }
            case MULTIPLE_CHOICE -> {
                return atLeastOneCorrectAndOneIncorrectAnswer(answers);
            }
            case FILL_IN_THE_BLANK -> {
                return exactlyOneCorrectAnswer(answers);
            }
            case MATCHING -> {
                return atLeastOneMatchingPair(answers);
            }
            case CALCULATION -> {
                return Optional.empty(); // can be anything
            }
            case CRAFTING -> {
                return Optional.empty(); // can be anything
            }
        }
        return Optional.empty();
    }

}
