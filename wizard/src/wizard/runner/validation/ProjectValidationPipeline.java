package wizard.runner.validation;

import com.fasterxml.jackson.databind.JsonNode;
import foundation.room.model.VerifiedAsset;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import wizard.runner.asset.AssetVerifier;
import wizard.runner.canonical.HostInputIdentity;
import wizard.runner.contract.IssueCode;
import wizard.runner.contract.IssueCollector;
import wizard.runner.contract.ValidationIssue;
import wizard.runner.contract.ValidationPhase;
import wizard.runner.contract.ValidationSeverity;
import wizard.runner.input.InputSnapshot;
import wizard.runner.input.ProjectInputReader;
import wizard.runner.model.ProjectDefinition;
import wizard.runner.model.ProjectDefinitionMapper;

/** Strict shared production pipeline from immutable Wizard project input to validated room data. */
public final class ProjectValidationPipeline {
  private final ProjectInputReader inputReader;
  private final DeerSchemaValidator schemaValidator;
  private final ProjectDefinitionMapper mapper;
  private final SemanticValidator semanticValidator;
  private final AssetVerifier assetVerifier;

  /** Creates a pipeline using the compiled V0.3 capabilities and packaged schema. */
  public ProjectValidationPipeline() {
    inputReader = new ProjectInputReader();
    schemaValidator = new DeerSchemaValidator();
    mapper = new ProjectDefinitionMapper();
    semanticValidator = new SemanticValidator();
    assetVerifier = new AssetVerifier();
  }

  /**
   * Validates one finalized project without mutating or later reopening its inputs.
   *
   * @param project finalized Wizard project directory
   * @return immutable deterministic validation result
   */
  public ValidationResult validate(final Path project) {
    InputSnapshot input = inputReader.read(Objects.requireNonNull(project, "project"));
    IssueCollector issues = new IssueCollector();
    issues.addAll(input.issues());
    if (input.document().isEmpty() || input.realProjectRoot().isEmpty() || issues.hasErrors()) {
      return invalid(input.rawDeerSha256(), issues);
    }

    JsonNode document = input.document().orElseThrow();
    schemaValidator.validate(document, issues);
    if (issues.hasErrors()) {
      return invalid(input.rawDeerSha256(), issues);
    }

    try {
      ProjectDefinition model = mapper.map(document);
      semanticValidator.validate(model, issues);
      List<VerifiedAsset> assets =
          assetVerifier.verify(model, input.realProjectRoot().orElseThrow(), issues);
      if (issues.hasErrors()) {
        return invalid(input.rawDeerSha256(), issues);
      }
      String hostInputSha256 = HostInputIdentity.sha256(document);
      return new ValidationResult(
          true,
          input.rawDeerSha256(),
          Optional.of(hostInputSha256),
          Optional.of(model),
          assets,
          issues.issues());
    } catch (RuntimeException exception) {
      IssueCollector internalIssues = new IssueCollector();
      internalIssues.add(
          new ValidationIssue(
              ValidationSeverity.ERROR,
              ValidationPhase.FEASIBILITY,
              IssueCode.INTERNAL_ERROR,
              "validation.internal_error",
              Map.of(),
              "",
              Optional.empty(),
              List.of()));
      return invalid(input.rawDeerSha256(), internalIssues);
    }
  }

  private static ValidationResult invalid(
      final Optional<String> rawDeerSha256, final IssueCollector issues) {
    return new ValidationResult(
        false, rawDeerSha256, Optional.empty(), Optional.empty(), List.of(), issues.issues());
  }
}
