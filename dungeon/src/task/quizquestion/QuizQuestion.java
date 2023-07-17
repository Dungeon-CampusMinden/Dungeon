package task.quizquestion;

import task.Task;
import task.TaskContent;

/**
 * Represents a single quiz question, including the question itself, possible answer choices, and
 * the type of question.
 *
 * <p>A{@link QuizQuestion} can be a Single-Choice, a Multiple-Choice or a Freetext questuin. The
 * type is stored as a {@link QuizType}. If the question is ask via tha UI, the {@link
 * QuizQuestionUI} will configurate the UI fpr the question based on that type.
 */
public class QuizQuestion extends Task {

    private final QuizType type;
    private final QuizContent question;

    public QuizQuestion(
            final QuizType type, QuizContentType questionContentType, String questionText) {
        super();
        this.type = type;
        taskText(questionText);
        question = new QuizContent(this, questionContentType, questionText);
        // addContent(question); // should i do that?
    }

    public QuizType type() {
        return type;
    }

    public void addAnswer(QuizContentType type, String content) {
        addContent(new QuizContent(this, type, content));
    }

    public QuizContent question() {
        return question;
    }

    public class QuizContent extends TaskContent {

        private final QuizContentType type;
        private final String content;
        /**
         * Creates a new TaskContent.
         *
         * @param task Task to which this content belongs.
         */
        public QuizContent(Task task, QuizContentType type, String content) {
            super(task);
            this.type = type;
            this.content = content;
        }

        public String content() {
            return content;
        }

        public QuizContentType type() {
            return type;
        }
    }

    /**
     * The QuizQuestionContentType enum represents the different types of content that can be
     * associated with a quiz question. The available types are TEXT, IMAGE, and TEXT_AND_IMAGE.
     * TEXT represents a question or answer choice as text. IMAGE represents a question or answer
     * choice as an image. TEXT_AND_IMAGE represents a question or answer choice as both text and an
     * image.
     */
    public enum QuizContentType {
        TEXT,
        IMAGE,
        TEXT_AND_IMAGE
    }

    /**
     * The QuizQuestionType enum represents the different types of quiz questions that can be
     * created. The available types are SINGLE_CHOICE, MULTIPLE_CHOICE, and FREETEXT. SINGLE_CHOICE
     * represents a question with multiple answer choices, where the user is required to select one
     * answer. MULTIPLE_CHOICE represents a question with multiple answer choices, where the user is
     * allowed to select multiple answers. FREETEXT represents a question where the user is required
     * to input their answer as text.
     */
    public enum QuizType {
        SINGLE_CHOICE,
        MULTIPLE_CHOICE,
        FREETEXT
    }
}
