package rooms.systemRecovery.modules.computer;

import java.util.ArrayList;
import java.util.List;

/** Encodes the editable blanks stored on an unejected USB program item. */
public final class UsbProgramDraft {

  private static final String DELIMITER = String.valueOf((char) 31);
  private static final int MAX_FIELD_LENGTH = 256;

  private UsbProgramDraft() {}

  /**
   * Encodes one or more single-line blank values for item and save-game storage.
   *
   * @param fields input values to encode
   * @return encoded values
   */
  public static String encode(List<String> fields) {
    if (fields == null || fields.isEmpty() || !isValid(fields)) {
      throw new IllegalArgumentException("A valid list of USB program fields is required.");
    }
    return String.join(DELIMITER, fields);
  }

  /**
   * Returns the stored blank values, or empty values when no draft has been saved yet.
   *
   * @param draft stored values
   * @param fieldCount expected number of fill-in fields
   * @return one value per fill-in field
   */
  public static List<String> decode(String draft, int fieldCount) {
    if (fieldCount < 1) throw new IllegalArgumentException("fieldCount must be positive.");
    if (draft == null || draft.isEmpty()) return emptyFields(fieldCount);
    String[] fields = draft.split(DELIMITER, -1);
    if (fields.length != fieldCount) return emptyFields(fieldCount);
    List<String> result = List.of(fields);
    return isValid(result) ? result : emptyFields(fieldCount);
  }

  /**
   * Rejects malformed, multiline or oversized data before the server accepts it as a draft.
   *
   * @param draft encoded values
   * @param fieldCount expected number of fill-in fields
   * @return whether the draft is structurally safe to persist
   */
  public static boolean isValid(String draft, int fieldCount) {
    if (draft == null || fieldCount < 1) return false;
    if (draft.isEmpty()) return fieldCount == 1;
    String[] fields = draft.split(DELIMITER, -1);
    return fields.length == fieldCount && isValid(List.of(fields));
  }

  private static boolean isValid(List<String> fields) {
    return fields != null
        && fields.stream()
            .allMatch(
                field ->
                    field != null
                        && field.length() <= MAX_FIELD_LENGTH
                        && field.indexOf('\n') < 0
                        && field.indexOf('\r') < 0
                        && field.indexOf(DELIMITER.charAt(0)) < 0);
  }

  private static List<String> emptyFields(int fieldCount) {
    List<String> fields = new ArrayList<>(fieldCount);
    for (int index = 0; index < fieldCount; index++) fields.add("");
    return List.copyOf(fields);
  }
}
