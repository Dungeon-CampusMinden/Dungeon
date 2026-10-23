package feature.survey;

import com.badlogic.gdx.Gdx;
import engine.tracking.Tracking;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import tools.jackson.databind.JsonNode;
import tracking.core.TrackingJson;

/**
 * A room survey that participants answer outside the game, such as on their devices in audience
 * mode.
 *
 * <p>The game hands the {@link #definition()} to the device that renders it and records the
 * returned answers with {@link #record}. Unlike {@link SurveyFeature}, declining consent here only
 * concerns the one participant: the caller records nothing for them.
 */
public final class ExternalSurvey {
  private final SurveyDefinition survey;
  private final JsonNode definition;

  private ExternalSurvey(SurveyDefinition survey, JsonNode definition) {
    this.survey = survey;
    this.definition = definition;
  }

  /**
   * Loads the survey of the tracked room for a phase, chosen like {@link SurveyFeature#show}.
   *
   * @param phase whether the survey comes before or after the game
   * @return the survey, or empty when the room has none, it is invalid, or no room is tracked
   */
  public static Optional<ExternalSurvey> of(SurveyFeature.Phase phase) {
    Optional<String> file =
        Tracking.roomId().flatMap(roomId -> Tracking.runId().map(run -> phase.file(roomId, run)));
    return file.flatMap(
        path ->
            SurveyDefinition.load(path)
                .map(
                    survey ->
                        new ExternalSurvey(
                            survey,
                            TrackingJson.object(
                                Gdx.files
                                    .internal(path)
                                    .readString(StandardCharsets.UTF_8.name())))));
  }

  /**
   * @return the survey document in the format described in {@code game/doc/survey.md}
   */
  public JsonNode definition() {
    return definition.deepCopy();
  }

  /**
   * @param answers JSON object mapping question IDs to answers
   * @return whether the answers select an option that declines consent
   */
  public boolean declined(JsonNode answers) {
    return survey.declined(answers);
  }

  /**
   * Records each answered question for one participant.
   *
   * @param answers JSON object mapping question IDs to answers
   * @param participant tracking participant
   * @throws RuntimeException if the answers are invalid or tracking stopped recording
   */
  public void record(JsonNode answers, UUID participant) {
    SurveyFeature.recordAnswers(survey, participant, answers);
  }
}
