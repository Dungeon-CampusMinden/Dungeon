package feature.interaction.phone;

/**
 * A line a phone reaches under its number, see {@link PhoneComponent#addLine}.
 *
 * @param number digits that reach the line
 * @param name what the phone's note of important extensions calls the line
 * @param listed whether the note lists the line; players have to find an unlisted number elsewhere
 */
public record PhoneLine(String number, String name, boolean listed) {}
