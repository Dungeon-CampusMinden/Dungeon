package task.quizquestion;

import com.badlogic.gdx.scenes.scene2d.ui.Image;

import semanticanalysis.types.DSLType;
import semanticanalysis.types.DSLTypeMember;
import task.Task;
import task.TaskContent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

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
    private final HashSet<Integer> correctAnswerIndices;

    /**
     * Create a new {@link Quiz} with the given configuration.
     *
     * <p>This will create a new {@link Content} instance as the question reference.
     *
     * <p>The {@link Quiz} will not have any answers, use {@link #addAnswer(Quiz.Content)} to add
     * possible answers to the question.
     *
     * @param type Type of the question (e.g., single-choice)
     * @param questionText The question itself (can contain a path to images).
     * @param image The image if this question contains one.
     */
    public Quiz(final Type type, String questionText, Image image) {
        super();
        this.type = type;
        taskText(questionText);
        question = new Content(questionText, image);
        question.task(this);
        this.correctAnswerIndices = new HashSet<>();
    }

    /**
     * Mark an answer in the stored content as correct.
     *
     * @param index the index of the stored content
     * @return false, if the index is out of bounds of the
     * stored content or the corresponding content was already marked as correct
     */
    public boolean addCorrectAnswerIndex(int index) {
        if (this.content.size() < index) {
            return false;
        }
        return this.correctAnswerIndices.add(index);
    }

    /**
     * Remove an index from the correct answer indices.
     *
     * @param index the index to remove
     * @return true, if removing succeeded
     */
    public boolean removeCorrectAnswerIndex(int index) {
        return this.correctAnswerIndices.remove(index);
    }

    /**
     * Create a new {@link Quiz} with the given configuration.
     *
     * <p>This will create a new {@link Content} instance as the question reference.
     *
     * <p>The {@link Quiz} will not have any answers, use {@link #addAnswer(Quiz.Content)} to add
     * possible answers to the question.
     *
     * @param type Type of the question (e.g., single-choice)
     * @param questionText The question itself (can contain a path to images).
     */
    public Quiz(final Type type, String questionText) {
        this(type, questionText, null);
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
     */
    @DSLType
    public static class Content extends TaskContent {

        private final Image image;
        @DSLTypeMember private final String content;

        private final Type type;

        /**
         * Creates a new {@link Content}.
         *
         * @param content The answer itself (can contain a path to images).
         * @param image Image if this content contains one.
         */
        public Content(String content, Image image) {
            super();
            this.image = image;
            this.content = content;
            if (image == null) type = Type.TEXT;
            else if (!content.equals("")) type = Type.TEXT_AND_IMAGE;
            else type = Type.IMAGE;
        }

        /**
         * Creates a new {@link Content}.
         *
         * @param content The answer itself (can contain a path to images).
         */
        public Content(String content) {
            this(content, null);
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
         * Get the image associated with this content, if available.
         *
         * @return an {@link Optional} containing the image, or an empty {@link Optional} if this
         *     task does not contain an image.
         */
        public Optional<Image> image() {
            return Optional.ofNullable(image);
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
