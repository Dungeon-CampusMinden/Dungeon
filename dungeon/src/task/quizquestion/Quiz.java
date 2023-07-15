package task.quizquestion;

import task.Task;
import task.TaskContent;

/**
 * Represents a single quiz question, including the question itself, possible answer choices, and
 * the type of question.
 *
 * <p>A {@link Quiz} can be a Single-Choice, a Multiple-Choice, or a Free-text question. The type is
 * stored as a {@link QuizType}. If the question is asked via the UI, the {@link QuizUI} will
 * configure the UI for the question based on that type. The type can be accessed via {@link
 * #type()}.
 *
 * <p>Add a {@link QuizContent} answer by using the {@link #addAnswer(QuizContentType, String)}
 * method. Use the {@link #contentStream()} method to get the answers as a stream.
 *
 * <p>The question will be stored as {@link QuizContent} and can be accessed via {@link
 * #question()}.
 */
public class Quiz extends Task {

    private final QuizType type;
    private final QuizContent question;

    /**
     * Create a new {@link Quiz} with the given configuration.
     *
     * <p>This will create a new {@link QuizContent} instance as the question reference.
     *
     * <p>The {@link Quiz} will not have any answers, use {@link #addAnswer(QuizContentType,
     * String)} to add possible answers to the question.
     *
     * @param type Type of the question (e.g., single-choice)
     * @param questionContentType What does the question contain? Just text, just a path to an
     *     image, or both text and a path to an image?
     * @param questionText The question itself (can contain a path to images).
     */
    public Quiz(final QuizType type, QuizContentType questionContentType, String questionText) {
        super();
        this.type = type;
        taskText(questionText);
        question = new QuizContent(this, questionContentType, questionText);
    }

    /**
     * Get the type of the question.
     *
     * @return The type of the question.
     */
    public QuizType type() {
        return type;
    }

    /**
     * Create a {@link QuizContent} answer instance with the given configuration as a possible
     * answer for this question.
     *
     * @param type What does the answer contain? Just text, just a path to an image, or both text
     *     and a path to an image?
     * @param content The answer itself (can contain a path to images).
     */
    public void addAnswer(QuizContentType type, String content) {
        addContent(new QuizContent(this, type, content));
    }

    /**
     * Get the question instance.
     *
     * @return The question instance.
     */
    public QuizContent question() {
        return question;
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

    /**
     * Content for a {@link Quiz}-
     *
     * <p>Is used as answer and question for a {@link Quiz}.
     *
     * <p>Stores a String with the question/answer text and {@link QuizContentType} which defines if
     * the String is just Text or contains a path to an image or both. The type is used by the
     * {@link QuizUI} to configure the ui if the question is asked via the ui.
     */
    public static class QuizContent extends TaskContent {

        private final QuizContentType type;
        private final String content;

        /**
         * Creates a new {@link QuizContent}.
         *
         * <p>Use {@link QuizContent#addAnswer(QuizContentType, String)} to create an instance of
         * this.
         *
         * @param task Task to which this content belongs.
         * @param type What does the answer contain? Just text, just a path to an image, or both
         *     text and a path to an image?
         * @param content The answer itself (can contain a path to images).
         */
        private QuizContent(Task task, QuizContentType type, String content) {
            super(task);
            this.type = type;
            this.content = content;
        }

        /**
         * Get the content string.
         *
         * @return The content string.
         */
        public String content() {
            return content;
        }

        /**
         * Get the type of the {@link QuizContent}.
         *
         * @return The type of this content.
         */
        public QuizContentType type() {
            return type;
        }
    }
}
