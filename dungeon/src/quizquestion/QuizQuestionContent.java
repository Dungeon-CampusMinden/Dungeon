package quizquestion;

/**
 * The QuizQuestionContent class represents the content of a quiz question, such as the question
 * text or an image associated with the question.
 *
 * @param type the QuizQuestionContentType representing the type of content, such as TEXT, IMAGE, or
 *     TEXT_AND_IMAGE
 * @param content a String representing the actual content, such as the text of a question or the
 *     path to an image file or a String with text and a path to an image file.
 */
public record QuizQuestionContent(QuizQuestionContentType type, String content) {
    /**
     * The QuizQuestionContentType enum represents the different types of content that can be
     * associated with a quiz question. The available types are TEXT, IMAGE, and TEXT_AND_IMAGE.
     * TEXT represents a question or answer choice as text. IMAGE represents a question or answer
     * choice as an image. TEXT_AND_IMAGE represents a question or answer choice as both text and an
     * image.
     */
    public enum QuizQuestionContentType {
        TEXT,
        IMAGE,
        TEXT_AND_IMAGE
    }
}
