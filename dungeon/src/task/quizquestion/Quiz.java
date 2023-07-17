package task.quizquestion;

import task.Task;
import task.TaskContent;

/**
 * Represents a single quiz question, including the question itself, possible answer choices, and
 * the type of question.
 *
 * <p>A {@link Quiz} can be a Single-Choice, a Multiple-Choice, or a Free-text question. The type is
 * stored as a {@link Type}. If the question is asked via the UI, the {@link QuizUI} will configure
 * the UI for the question based on that type. The type can be accessed via {@link #type()}.
 *
 * <p>Add a {@link Content} answer by using the {@link #addAnswer(Quiz.Content)} method. Use the
 * {@link #contentStream()} method to get the answers as a stream.
 *
 * <p>The question will be stored as {@link Content} and can be accessed via {@link #question()}.
 */
public class Quiz extends Task {

    private final Type type;
    private final Content question;

    /**
     * Create a new {@link Quiz} with the given configuration.
     *
     * <p>This will create a new {@link Content} instance as the question reference.
     *
     * <p>The {@link Quiz} will not have any answers, use {@link #addAnswer(Quiz.Content)} to add
     * possible answers to the question.
     *
     * @param type Type of the question (e.g., single-choice)
     * @param questionContentType What does the question contain? Just text, just a path to an
     *     image, or both text and a path to an image?
     * @param questionText The question itself (can contain a path to images).
     */
    public Quiz(final Type type, Content.Type questionContentType, String questionText) {
        super();
        this.type = type;
        taskText(questionText);
        question = new Content(questionContentType, questionText);
        question.task(this);
    }

    /**
     * Get the type of the question.
     *
     * @return The type of the question.
     */
    public Type type() {
        return type;
    }

    /**
     * Add a {@link Content} answer instance as a possible answer for this question.
     *
     * <p>If the answer has no task reference yet, the reference will be set to this task and the
     * answer will be added to this task's content. If the answer already has a task reference, the
     * answer will not be added to this task's content.
     *
     * @param answer The answer (can contain a path to images).
     * @return true if the answer was added to this task's content, false if not.
     */
    public boolean addAnswer(Quiz.Content answer) {
        if (answer.task(this)) {
            addContent(answer);
            return true;
        }
        return false;
    }

    /**
     * Get the question instance.
     *
     * @return The question instance.
     */
    public Content question() {
        return question;
    }

    /**
     * The QuizQuestionType enum represents the different types of quiz questions that can be
     * created. The available types are SINGLE_CHOICE, MULTIPLE_CHOICE, and FREETEXT. SINGLE_CHOICE
     * represents a question with multiple answer choices, where the user is required to select one
     * answer. MULTIPLE_CHOICE represents a question with multiple answer choices, where the user is
     * allowed to select multiple answers. FREETEXT represents a question where the user is required
     * to input their answer as text.
     */
    public enum Type {
        SINGLE_CHOICE,
        MULTIPLE_CHOICE,
        FREETEXT
    }

    /**
     * Content for a {@link Quiz}-
     *
     * <p>Is used as answer and question for a {@link Quiz}.
     *
     * <p>Stores a String with the question/answer text and {@link Type} which defines if the String
     * is just Text or contains a path to an image or both. The type is used by the {@link QuizUI}
     * to configure the ui if the question is asked via the ui.
     */
    public static class Content extends TaskContent {

        private final Type type;
        private final String content;

        /**
         * Creates a new {@link Content}.
         *
         * @param type What does the answer contain? Just text, just a path to an image, or both
         *     text and a path to an image?
         * @param content The answer itself (can contain a path to images).
         */
        public Content(Type type, String content) {
            super();
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
         * Get the type of the {@link Content}.
         *
         * @return The type of this content.
         */
        public Type type() {
            return type;
        }

        /**
         * The QuizQuestionContentType enum represents the different types of content that can be
         * associated with a quiz question. The available types are TEXT, IMAGE, and TEXT_AND_IMAGE.
         * TEXT represents a question or answer choice as text. IMAGE represents a question or
         * answer choice as an image. TEXT_AND_IMAGE represents a question or answer choice as both
         * text and an image.
         */
        public enum Type {
            TEXT,
            IMAGE,
            TEXT_AND_IMAGE
        }
    }
}
