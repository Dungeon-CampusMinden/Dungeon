package wizard.runner.validation;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import wizard.runner.contract.IssueCode;
import wizard.runner.model.ProjectDefinition;
import wizard.runner.room.RoomDeriver;

/** Production-path integration tests for strict Wizard project validation. */
final class ProjectValidationPipelineTest {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @TempDir Path temporaryDirectory;

  @Test
  void validatesTheCanonicalPublicProjectAndResultContract() throws IOException {
    Path project = materializeCanonicalProject();

    ValidationResult result = new ProjectValidationPipeline().validate(project);
    ValidationResult repeated = new ProjectValidationPipeline().validate(project);
    Path authoringVariant = materializeCanonicalProject("authoring-hash");
    ObjectNode variantDocument =
        (ObjectNode) MAPPER.readTree(authoringVariant.resolve("deer.json").toFile());
    ((ObjectNode) variantDocument.required("assets").required(0).required("source"))
        .put("license", "Changed license text");
    Files.write(authoringVariant.resolve("deer.json"), MAPPER.writeValueAsBytes(variantDocument));
    ValidationResult changedAuthoring = new ProjectValidationPipeline().validate(authoringVariant);
    Path formattingVariant = materializeCanonicalProject("formatting-hash");
    ObjectNode formattingDocument =
        (ObjectNode) MAPPER.readTree(formattingVariant.resolve("deer.json").toFile());
    Files.write(
        formattingVariant.resolve("deer.json"), MAPPER.writeValueAsBytes(formattingDocument));
    ValidationResult changedFormatting =
        new ProjectValidationPipeline().validate(formattingVariant);

    assertTrue(result.valid());
    assertTrue(result.issues().isEmpty());
    assertEquals("wizard_foundation_v0_4", result.model().orElseThrow().metadata().id());
    assertEquals(1, result.assets().size());
    assertEquals(123456789L, result.model().orElseThrow().seed());
    assertEquals(result.hostInputSha256(), repeated.hostInputSha256());
    assertEquals(result.model(), changedAuthoring.model());
    assertNotEquals(result.hostInputSha256(), changedAuthoring.hostInputSha256());
    assertEquals(result.hostInputSha256(), changedFormatting.hostInputSha256());
    assertNotEquals(result.rawDeerSha256(), changedFormatting.rawDeerSha256());
    assertEquals(64, result.hostInputSha256().orElseThrow().length());
    assertEquals(
        "3b50ea522803ed6e067c75c00df584271a6e8fd62896b63eab4d64d618f0d1a9",
        result.assets().get(0).sha256());
    assertArrayEquals(
        Files.readAllBytes(project.resolve(result.assets().get(0).logicalPath())),
        result.assets().get(0).bytes());
    Path invalidProject = materializeCanonicalProject("invalid-result");
    ObjectNode invalidDocument =
        (ObjectNode) MAPPER.readTree(invalidProject.resolve("deer.json").toFile());
    invalidDocument.remove("seed");
    Files.write(invalidProject.resolve("deer.json"), MAPPER.writeValueAsBytes(invalidDocument));
    ValidationResult invalid = new ProjectValidationPipeline().validate(invalidProject);

    assertFalse(invalid.valid());
    assertTrue(invalid.rawDeerSha256().isPresent());
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ValidationResult(
                false,
                invalid.rawDeerSha256(),
                Optional.empty(),
                result.model(),
                List.of(),
                invalid.issues()));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ValidationResult(
                false,
                invalid.rawDeerSha256(),
                result.hostInputSha256(),
                Optional.empty(),
                List.of(),
                invalid.issues()));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ValidationResult(
                false,
                invalid.rawDeerSha256(),
                Optional.empty(),
                Optional.empty(),
                List.of(),
                List.of()));
  }

  @Test
  void acceptsTheStaggeredMandatoryAndDagExample() {
    Path project = Path.of("examples", "the-last-hour-v0.4").toAbsolutePath().normalize();

    ValidationResult result = new ProjectValidationPipeline().validate(project);

    assertTrue(result.valid(), result.issues().toString());
    assertEquals(6, result.model().orElseThrow().riddleGraph().edges().size());
  }

  @Test
  void rejectsThePreviousFormatVersionWithoutCompatibilityFallback() throws IOException {
    Path project = materializeCanonicalProject("old-format");
    ObjectNode deer = (ObjectNode) MAPPER.readTree(project.resolve("deer.json").toFile());
    deer.put("formatVersion", "0.3");
    Files.write(project.resolve("deer.json"), MAPPER.writeValueAsBytes(deer));

    ValidationResult result = new ProjectValidationPipeline().validate(project);

    assertIssue(result, IssueCode.FORMAT_VERSION_UNSUPPORTED);
  }

  @Test
  void reportsGraphEdgeCapacityThroughFeasibilityValidation() throws IOException {
    Path project = materializeCanonicalProject("edge-capacity");
    ObjectNode deer = (ObjectNode) MAPPER.readTree(project.resolve("deer.json").toFile());
    ArrayNode edges = (ArrayNode) deer.required("riddleGraph").required("edges");
    while (edges.size() <= 4_096) {
      ObjectNode edge = edges.addObject();
      edge.put("from", "n_start");
      edge.put("to", "n_exit");
    }
    Files.write(project.resolve("deer.json"), MAPPER.writeValueAsBytes(deer));

    ValidationResult result = new ProjectValidationPipeline().validate(project);

    assertTrue(
        result.issues().stream()
            .anyMatch(
                issue ->
                    issue.code() == IssueCode.RUNNER_CAPACITY_EXCEEDED
                        && issue.path().equals("/riddleGraph/edges")
                        && issue.arguments().get("kind").equals("graph_edges")));
  }

  @Test
  void acceptsHyphenatedIdentifiersAndPortableCustomFilenames() throws IOException {
    Path project = materializeCanonicalProject("relaxed-authoring-names");
    String customPath = "assets/custom/3b50ea522803-Foundation Note (1) + Copy.PNG";
    Files.move(
        project.resolve("assets/custom/3b50ea522803-foundation-note.png"),
        project.resolve(customPath));
    ObjectNode document = (ObjectNode) MAPPER.readTree(project.resolve("deer.json").toFile());
    ((ObjectNode) document.required("metadata")).put("id", "wizard-foundation-v0-4");
    ((ObjectNode) document.required("assets").required(0)).put("path", customPath);
    Files.write(project.resolve("deer.json"), MAPPER.writeValueAsBytes(document));

    ValidationResult result = new ProjectValidationPipeline().validate(project);

    assertTrue(result.valid(), result.issues().toString());
    assertEquals(customPath, result.assets().getFirst().logicalPath());
    assertEquals("wizard-foundation-v0-4", new RoomDeriver().derive(result).definition().id());
  }

  @Test
  void validatesBundledOnlyProjectsByExactInternalPath() throws IOException {
    Path knownProject = materializeBundledProject("bundled-known", "images/open-book.png");
    ValidationResult known = new ProjectValidationPipeline().validate(knownProject);

    Path missingProject = materializeBundledProject("bundled-missing", "items/missing.png");
    ValidationResult missing = new ProjectValidationPipeline().validate(missingProject);

    Path portableMissingProject =
        materializeBundledProject(
            "bundled-portable-missing", "images/Room Plan (Final) + Copy.PNG");
    ValidationResult portableMissing =
        new ProjectValidationPipeline().validate(portableMissingProject);

    Path artificialDirectoryProject =
        materializeBundledProject("bundled-artificial-directory", "assets/bundled/missing.png");
    ValidationResult artificialDirectory =
        new ProjectValidationPipeline().validate(artificialDirectoryProject);

    Path mismatchProject = materializeBundledProject("bundled-mismatch", "emotes/emote_cloud.png");
    ObjectNode mismatchDocument =
        (ObjectNode) MAPPER.readTree(mismatchProject.resolve("deer.json").toFile());
    ((ObjectNode) mismatchDocument.required("assets").required(0)).put("mediaType", "image/jpeg");
    Files.write(mismatchProject.resolve("deer.json"), MAPPER.writeValueAsBytes(mismatchDocument));
    ValidationResult mismatch = new ProjectValidationPipeline().validate(mismatchProject);

    assertTrue(known.valid(), known.issues().toString());
    assertTrue(known.assets().isEmpty());
    assertIssue(missing, IssueCode.ASSET_MISSING);
    assertIssue(portableMissing, IssueCode.ASSET_MISSING);
    assertTrue(
        portableMissing.issues().stream()
            .noneMatch(issue -> issue.code() == IssueCode.SCHEMA_INVALID));
    assertIssue(artificialDirectory, IssueCode.ASSET_MISSING);
    assertTrue(mismatch.valid(), mismatch.issues().toString());
    assertTrue(mismatch.assets().isEmpty());
  }

  @Test
  void validatesAssetAuthoringShape() throws IOException {
    Path emptyAttributionProject = materializeCanonicalProject("empty-attribution");
    ObjectNode emptyAttribution =
        (ObjectNode) MAPPER.readTree(emptyAttributionProject.resolve("deer.json").toFile());
    ((ObjectNode) emptyAttribution.required("assets").required(0).required("source"))
        .put("attribution", "");
    Files.write(
        emptyAttributionProject.resolve("deer.json"), MAPPER.writeValueAsBytes(emptyAttribution));

    Path missingAttributionProject = materializeCanonicalProject("missing-attribution");
    ObjectNode missingAttribution =
        (ObjectNode) MAPPER.readTree(missingAttributionProject.resolve("deer.json").toFile());
    ((ObjectNode) missingAttribution.required("assets").required(0).required("source"))
        .remove("attribution");
    Files.write(
        missingAttributionProject.resolve("deer.json"),
        MAPPER.writeValueAsBytes(missingAttribution));

    Path blankAttributionProject = materializeCanonicalProject("blank-attribution");
    ObjectNode blankAttribution =
        (ObjectNode) MAPPER.readTree(blankAttributionProject.resolve("deer.json").toFile());
    ((ObjectNode) blankAttribution.required("assets").required(0).required("source"))
        .put("attribution", " ");
    Files.write(
        blankAttributionProject.resolve("deer.json"), MAPPER.writeValueAsBytes(blankAttribution));

    Path sourceTypeProject = materializeCanonicalProject("source-type");
    ObjectNode sourceType =
        (ObjectNode) MAPPER.readTree(sourceTypeProject.resolve("deer.json").toFile());
    ((ObjectNode) sourceType.required("assets").required(0).required("source"))
        .put("type", "custom");
    Files.write(sourceTypeProject.resolve("deer.json"), MAPPER.writeValueAsBytes(sourceType));

    Path missingLicenseProject = materializeCanonicalProject("missing-license");
    ObjectNode missingLicense =
        (ObjectNode) MAPPER.readTree(missingLicenseProject.resolve("deer.json").toFile());
    ((ObjectNode) missingLicense.required("assets").required(0).required("source"))
        .remove("license");
    Files.write(
        missingLicenseProject.resolve("deer.json"), MAPPER.writeValueAsBytes(missingLicense));

    Path emptyLicenseProject = materializeCanonicalProject("empty-license");
    ObjectNode emptyLicense =
        (ObjectNode) MAPPER.readTree(emptyLicenseProject.resolve("deer.json").toFile());
    ((ObjectNode) emptyLicense.required("assets").required(0).required("source"))
        .put("license", " ");
    Files.write(emptyLicenseProject.resolve("deer.json"), MAPPER.writeValueAsBytes(emptyLicense));

    ValidationResult emptyAttributionResult =
        new ProjectValidationPipeline().validate(emptyAttributionProject);
    ValidationResult missingAttributionResult =
        new ProjectValidationPipeline().validate(missingAttributionProject);
    ValidationResult blankAttributionResult =
        new ProjectValidationPipeline().validate(blankAttributionProject);

    assertTrue(emptyAttributionResult.valid(), emptyAttributionResult.issues().toString());
    assertTrue(missingAttributionResult.valid(), missingAttributionResult.issues().toString());
    assertTrue(blankAttributionResult.valid(), blankAttributionResult.issues().toString());
    assertEquals(emptyAttributionResult.model(), missingAttributionResult.model());
    assertEquals(blankAttributionResult.model(), missingAttributionResult.model());
    assertOnlySchemaIssues(new ProjectValidationPipeline().validate(sourceTypeProject));
    assertOnlySchemaIssues(new ProjectValidationPipeline().validate(missingLicenseProject));
    assertOnlySchemaIssues(new ProjectValidationPipeline().validate(emptyLicenseProject));

    List<String> unknownBundledPaths =
        List.of(
            "/items/puzzle-piece.png",
            "items\\puzzle-piece.png",
            "https://example.invalid/puzzle.png",
            ".",
            "..",
            "items/../puzzle.png");
    for (int index = 0; index < unknownBundledPaths.size(); index++) {
      Path unknownProject =
          materializeBundledProject(
              "unknown-bundled-path-" + index, unknownBundledPaths.get(index));
      ValidationResult unknown = new ProjectValidationPipeline().validate(unknownProject);
      assertIssue(unknown, IssueCode.ASSET_MISSING);
      assertTrue(
          unknown.issues().stream().noneMatch(issue -> issue.code() == IssueCode.SCHEMA_INVALID));
    }

    Path customTraversalProject = materializeCanonicalProject("unsafe-custom-traversal");
    ObjectNode customTraversal =
        (ObjectNode) MAPPER.readTree(customTraversalProject.resolve("deer.json").toFile());
    ((ObjectNode) customTraversal.required("assets").required(0))
        .put("path", "assets/custom/../foundation-note.png");
    Files.write(
        customTraversalProject.resolve("deer.json"), MAPPER.writeValueAsBytes(customTraversal));
    ValidationResult customTraversalResult =
        new ProjectValidationPipeline().validate(customTraversalProject);
    assertIssue(customTraversalResult, IssueCode.ASSET_PATH_UNSAFE);
    assertTrue(
        customTraversalResult.issues().stream()
            .noneMatch(issue -> issue.code() == IssueCode.SCHEMA_INVALID));
  }

  @Test
  void enforcesSupportedValuesAndSafeIntegerBoundaries() throws IOException {
    Path unknownTypeProject = materializeCanonicalProject("unknown-type");
    ObjectNode unknownType =
        (ObjectNode) MAPPER.readTree(unknownTypeProject.resolve("deer.json").toFile());
    ((ObjectNode) unknownType.required("riddles").required(0)).put("type", "unknown");
    Files.write(unknownTypeProject.resolve("deer.json"), MAPPER.writeValueAsBytes(unknownType));

    Path missingSeedProject = materializeCanonicalProject("missing-seed");
    ObjectNode missingSeed =
        (ObjectNode) MAPPER.readTree(missingSeedProject.resolve("deer.json").toFile());
    missingSeed.remove("seed");
    Files.write(missingSeedProject.resolve("deer.json"), MAPPER.writeValueAsBytes(missingSeed));

    Path negativeSeedProject = materializeCanonicalProject("negative-seed");
    ObjectNode negativeSeed =
        (ObjectNode) MAPPER.readTree(negativeSeedProject.resolve("deer.json").toFile());
    negativeSeed.put("seed", -1);
    Files.write(negativeSeedProject.resolve("deer.json"), MAPPER.writeValueAsBytes(negativeSeed));

    Path maximumSeedProject = materializeCanonicalProject("maximum-seed");
    ObjectNode maximumSeed =
        (ObjectNode) MAPPER.readTree(maximumSeedProject.resolve("deer.json").toFile());
    maximumSeed.put("seed", 9_007_199_254_740_991L);
    Files.write(maximumSeedProject.resolve("deer.json"), MAPPER.writeValueAsBytes(maximumSeed));

    Path excessiveSeedProject = materializeCanonicalProject("excessive-seed");
    ObjectNode excessiveSeed =
        (ObjectNode) MAPPER.readTree(excessiveSeedProject.resolve("deer.json").toFile());
    excessiveSeed.put("seed", 9_007_199_254_740_992L);
    Files.write(excessiveSeedProject.resolve("deer.json"), MAPPER.writeValueAsBytes(excessiveSeed));

    Path maximumEstimatedMinutesProject = materializeCanonicalProject("maximum-estimated-minutes");
    ObjectNode maximumEstimatedMinutes =
        (ObjectNode) MAPPER.readTree(maximumEstimatedMinutesProject.resolve("deer.json").toFile());
    ((ObjectNode) maximumEstimatedMinutes.required("riddles").required(0))
        .put("estimatedMinutes", 9_007_199_254_740_991L);
    Files.write(
        maximumEstimatedMinutesProject.resolve("deer.json"),
        MAPPER.writeValueAsBytes(maximumEstimatedMinutes));

    Path excessiveEstimatedMinutesProject =
        materializeCanonicalProject("excessive-estimated-minutes");
    ObjectNode excessiveEstimatedMinutes =
        (ObjectNode)
            MAPPER.readTree(excessiveEstimatedMinutesProject.resolve("deer.json").toFile());
    ((ObjectNode) excessiveEstimatedMinutes.required("riddles").required(0))
        .put("estimatedMinutes", 9_007_199_254_740_992L);
    Files.write(
        excessiveEstimatedMinutesProject.resolve("deer.json"),
        MAPPER.writeValueAsBytes(excessiveEstimatedMinutes));

    Path textualSeedProject = materializeCanonicalProject("textual-seed");
    ObjectNode textualSeed =
        (ObjectNode) MAPPER.readTree(textualSeedProject.resolve("deer.json").toFile());
    textualSeed.put("seed", "123456789");
    Files.write(textualSeedProject.resolve("deer.json"), MAPPER.writeValueAsBytes(textualSeed));

    assertOnlySchemaIssues(new ProjectValidationPipeline().validate(unknownTypeProject));
    assertOnlySchemaIssues(new ProjectValidationPipeline().validate(missingSeedProject));
    assertOnlySchemaIssues(new ProjectValidationPipeline().validate(negativeSeedProject));
    ValidationResult maximumSeedResult =
        new ProjectValidationPipeline().validate(maximumSeedProject);
    assertTrue(maximumSeedResult.valid(), maximumSeedResult.issues().toString());
    ProjectDefinition maximumSeedModel = maximumSeedResult.model().orElseThrow();
    assertEquals(9_007_199_254_740_991L, maximumSeedModel.seed());
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ProjectDefinition(
                9_007_199_254_740_992L,
                maximumSeedModel.metadata(),
                maximumSeedModel.session(),
                maximumSeedModel.scenario(),
                maximumSeedModel.surfaces(),
                maximumSeedModel.riddleGraph(),
                maximumSeedModel.riddles(),
                maximumSeedModel.assets()));
    assertOnlySchemaIssues(new ProjectValidationPipeline().validate(excessiveSeedProject));
    ValidationResult maximumEstimatedMinutesResult =
        new ProjectValidationPipeline().validate(maximumEstimatedMinutesProject);
    assertTrue(
        maximumEstimatedMinutesResult.valid(), maximumEstimatedMinutesResult.issues().toString());
    assertOnlySchemaIssues(
        new ProjectValidationPipeline().validate(excessiveEstimatedMinutesProject));
    assertOnlySchemaIssues(new ProjectValidationPipeline().validate(textualSeedProject));
  }

  @Test
  void rejectsInvalidGraphEdges() throws IOException {
    List<GraphMutation> mutations =
        List.of(
            new GraphMutation(
                "unknown-endpoint",
                edges -> ((ObjectNode) edges.required(0)).put("to", "n_unknown"),
                IssueCode.REFERENCE_UNKNOWN,
                "/riddleGraph/edges/0/to"),
            new GraphMutation(
                "self-edge",
                edges -> ((ObjectNode) edges.required(0)).put("to", "n_start"),
                IssueCode.GRAPH_EDGE_INVALID,
                "/riddleGraph/edges/0/to"),
            new GraphMutation(
                "duplicate-edge",
                edges -> {
                  ObjectNode edge = (ObjectNode) edges.required(1);
                  edge.put("from", "n_start");
                  edge.put("to", "n_exit_code");
                },
                IssueCode.GRAPH_EDGE_INVALID,
                "/riddleGraph/edges/1/to"),
            new GraphMutation(
                "unreachable-node",
                edges -> ((ObjectNode) edges.required(0)).put("to", "n_exit"),
                IssueCode.GRAPH_NODE_UNREACHABLE,
                "/riddleGraph/nodes/1"),
            new GraphMutation(
                "no-path-to-end",
                edges -> ((ObjectNode) edges.required(1)).put("from", "n_start"),
                IssueCode.GRAPH_NODE_NO_PATH_TO_END,
                "/riddleGraph/nodes/1"),
            new GraphMutation(
                "cycle",
                edges -> {
                  ObjectNode edge = edges.addObject();
                  edge.put("from", "n_exit");
                  edge.put("to", "n_exit_code");
                },
                IssueCode.GRAPH_CYCLE,
                "/riddleGraph"));

    for (GraphMutation mutation : mutations) {
      Path project = materializeCanonicalProject(mutation.directoryName());
      ObjectNode deer = (ObjectNode) MAPPER.readTree(project.resolve("deer.json").toFile());
      ArrayNode edges = (ArrayNode) deer.required("riddleGraph").required("edges");
      mutation.mutate().accept(edges);
      Files.write(project.resolve("deer.json"), MAPPER.writeValueAsBytes(deer));

      ValidationResult result = new ProjectValidationPipeline().validate(project);

      assertFalse(result.valid(), mutation.directoryName() + ": " + result.issues());
      assertTrue(
          result.issues().stream()
              .anyMatch(
                  issue -> issue.code() == mutation.code() && issue.path().equals(mutation.path())),
          mutation.directoryName() + ": " + result.issues());
    }
  }

  @Test
  void rejectsIncomingStartAndOutgoingEndWithStableProfileReasons() throws IOException {
    Path incomingProject = materializeCanonicalProject("start-incoming");
    ObjectNode incoming =
        (ObjectNode) MAPPER.readTree(incomingProject.resolve("deer.json").toFile());
    ArrayNode incomingEdges = (ArrayNode) incoming.required("riddleGraph").required("edges");
    ObjectNode incomingEdge = incomingEdges.addObject();
    incomingEdge.put("from", "n_exit_code");
    incomingEdge.put("to", "n_start");
    Files.write(incomingProject.resolve("deer.json"), MAPPER.writeValueAsBytes(incoming));

    Path outgoingProject = materializeCanonicalProject("end-outgoing");
    ObjectNode outgoing =
        (ObjectNode) MAPPER.readTree(outgoingProject.resolve("deer.json").toFile());
    ArrayNode outgoingEdges = (ArrayNode) outgoing.required("riddleGraph").required("edges");
    ObjectNode outgoingEdge = outgoingEdges.addObject();
    outgoingEdge.put("from", "n_exit");
    outgoingEdge.put("to", "n_exit_code");
    Files.write(outgoingProject.resolve("deer.json"), MAPPER.writeValueAsBytes(outgoing));

    assertGraphProfile(
        new ProjectValidationPipeline().validate(incomingProject), "start_has_incoming");
    assertGraphProfile(
        new ProjectValidationPipeline().validate(outgoingProject), "end_has_outgoing");
  }

  @Test
  void rejectsDuplicateRiddleNodeBinding() throws IOException {
    Path project = materializeCanonicalProject("duplicate-riddle-binding");
    ObjectNode deer = (ObjectNode) MAPPER.readTree(project.resolve("deer.json").toFile());
    ObjectNode graph = (ObjectNode) deer.required("riddleGraph");
    ArrayNode nodes = (ArrayNode) graph.required("nodes");
    ObjectNode duplicateNode = nodes.addObject();
    duplicateNode.put("id", "n_exit_code_duplicate");
    duplicateNode.put("kind", "riddle");
    duplicateNode.put("riddleId", "r_exit_code");
    ArrayNode edges = (ArrayNode) graph.required("edges");
    ObjectNode startEdge = edges.addObject();
    startEdge.put("from", "n_start");
    startEdge.put("to", "n_exit_code_duplicate");
    ObjectNode endEdge = edges.addObject();
    endEdge.put("from", "n_exit_code_duplicate");
    endEdge.put("to", "n_exit");
    Files.write(project.resolve("deer.json"), MAPPER.writeValueAsBytes(deer));

    ValidationResult result = new ProjectValidationPipeline().validate(project);

    assertTrue(
        result.issues().stream()
            .anyMatch(
                issue ->
                    issue.code() == IssueCode.GRAPH_RIDDLE_UNREACHABLE
                        && issue.path().equals("/riddles/0")
                        && issue.arguments().get("count").equals(2)));
  }

  @Test
  void rejectsInvalidSurfaceBindingsOwnershipAndCollectionResources() throws IOException {
    Path incompatibleProject = materializeCanonicalProject("surface-incompatible");
    ObjectNode incompatible =
        (ObjectNode) MAPPER.readTree(incompatibleProject.resolve("deer.json").toFile());
    ((ObjectNode)
            incompatible.required("riddles").required(0).required("informationSources").required(0))
        .put("surfaceId", "s_exit_keypad");
    Files.write(incompatibleProject.resolve("deer.json"), MAPPER.writeValueAsBytes(incompatible));

    Path ownershipProject = materializeCanonicalProject("surface-ownership");
    ObjectNode ownership =
        (ObjectNode) MAPPER.readTree(ownershipProject.resolve("deer.json").toFile());
    ObjectNode unusedKeypad = ((ArrayNode) ownership.required("surfaces")).addObject();
    unusedKeypad.put("id", "s_unused_keypad");
    unusedKeypad.put("kind", "keypad");
    unusedKeypad.put("title", "Ungenutztes Keypad");
    Files.write(ownershipProject.resolve("deer.json"), MAPPER.writeValueAsBytes(ownership));

    Path resourcesProject = materializeCanonicalProject("resource-reference");
    ObjectNode resources =
        (ObjectNode) MAPPER.readTree(resourcesProject.resolve("deer.json").toFile());
    ((ObjectNode)
            resources
                .required("riddles")
                .required(0)
                .required("informationSources")
                .required(0)
                .required("resources")
                .required(1))
        .put("assetId", "asset_unknown");
    Files.write(resourcesProject.resolve("deer.json"), MAPPER.writeValueAsBytes(resources));

    Path endProject = materializeCanonicalProject("end-surface");
    ObjectNode end = (ObjectNode) MAPPER.readTree(endProject.resolve("deer.json").toFile());
    ((ObjectNode) end.required("riddleGraph").required("nodes").required(2))
        .put("surfaceId", "s_world");
    Files.write(endProject.resolve("deer.json"), MAPPER.writeValueAsBytes(end));

    assertIssue(
        new ProjectValidationPipeline().validate(incompatibleProject),
        IssueCode.SURFACE_INCOMPATIBLE);
    assertIssue(
        new ProjectValidationPipeline().validate(ownershipProject),
        IssueCode.SURFACE_OWNERSHIP_INVALID);
    assertIssue(
        new ProjectValidationPipeline().validate(resourcesProject), IssueCode.REFERENCE_UNKNOWN);
    assertIssue(
        new ProjectValidationPipeline().validate(endProject), IssueCode.SURFACE_INCOMPATIBLE);
  }

  private Path materializeCanonicalProject() throws IOException {
    return materializeCanonicalProject("canonical");
  }

  private Path materializeCanonicalProject(final String directoryName) throws IOException {
    Path examples = Path.of("examples", "foundation-v0.4").toAbsolutePath().normalize();
    Path project = Files.createDirectory(temporaryDirectory.resolve(directoryName));
    Path assetDirectory = Files.createDirectories(project.resolve("assets/custom"));
    Files.copy(examples.resolve("deer.json"), project.resolve("deer.json"));
    Files.copy(
        examples.resolve("assets/custom/3b50ea522803-foundation-note.png"),
        assetDirectory.resolve("3b50ea522803-foundation-note.png"));
    return project;
  }

  private Path materializeBundledProject(final String directoryName, final String path)
      throws IOException {
    Path project = materializeCanonicalProject(directoryName);
    ObjectNode document = (ObjectNode) MAPPER.readTree(project.resolve("deer.json").toFile());
    ((ObjectNode) document.required("assets").required(0)).put("path", path);
    Files.write(project.resolve("deer.json"), MAPPER.writeValueAsBytes(document));
    Files.delete(project.resolve("assets/custom/3b50ea522803-foundation-note.png"));
    Files.delete(project.resolve("assets/custom"));
    Files.delete(project.resolve("assets"));
    return project;
  }

  private static void assertOnlySchemaIssues(final ValidationResult result) {
    assertFalse(result.valid());
    assertFalse(result.issues().isEmpty());
    assertTrue(
        result.issues().stream().allMatch(issue -> issue.code() == IssueCode.SCHEMA_INVALID));
  }

  private static void assertIssue(final ValidationResult result, final IssueCode code) {
    assertFalse(result.valid());
    assertTrue(result.issues().stream().anyMatch(issue -> issue.code() == code));
  }

  private static void assertGraphProfile(final ValidationResult result, final String reason) {
    assertFalse(result.valid());
    assertTrue(
        result.issues().stream()
            .anyMatch(
                issue ->
                    issue.code() == IssueCode.GRAPH_PROFILE_INVALID
                        && issue.path().equals("/riddleGraph/nodes")
                        && issue.arguments().get("reason").equals(reason)),
        result.issues().toString());
  }

  private record GraphMutation(
      String directoryName, Consumer<ArrayNode> mutate, IssueCode code, String path) {}
}
