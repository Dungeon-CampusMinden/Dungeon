package wizard.runner.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import wizard.runner.contract.IssueCode;
import wizard.runner.contract.IssueCollector;
import wizard.runner.contract.ValidationIssue;
import wizard.runner.contract.ValidationPhase;
import wizard.runner.contract.ValidationSeverity;

/** Validates parsed DEER trees against the packaged normative schema. */
final class DeerSchemaValidator {
  private static final String RESOURCE = "/wizard/contract/deer.schema.json";

  private final Schema schema;

  DeerSchemaValidator() {
    try (InputStream input = DeerSchemaValidator.class.getResourceAsStream(RESOURCE)) {
      if (input == null) {
        throw new IllegalStateException("Missing DEER schema resource: " + RESOURCE);
      }
      schema =
          SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12).getSchema(input);
    } catch (Exception exception) {
      throw new IllegalStateException("Cannot load DEER schema resource", exception);
    }
  }

  void validate(final JsonNode document, final IssueCollector issues) {
    List<Error> errors = schema.validate(document);
    for (Error error : errors) {
      String keyword = error.getKeyword() == null ? "unknown" : error.getKeyword();
      issues.add(
          new ValidationIssue(
              ValidationSeverity.ERROR,
              ValidationPhase.SCHEMA,
              IssueCode.SCHEMA_INVALID,
              "validation.schema.invalid",
              Map.of("keyword", keyword),
              pointer(error),
              Optional.empty(),
              List.of()));
    }
  }

  private static String pointer(final Error error) {
    String path = error.getInstanceLocation().toString();
    String base;
    if (path.isEmpty() || path.equals("$") || path.equals("#")) {
      base = "";
    } else if (path.startsWith("#/")) {
      base = path.substring(1);
    } else if (path.startsWith("/")) {
      base = path;
    } else if (path.startsWith("$.")) {
      base = "/" + path.substring(2).replace(".", "/");
    } else {
      base = "";
    }
    if ("required".equals(error.getKeyword()) && error.getProperty() != null) {
      return base + "/" + escape(error.getProperty());
    }
    return base;
  }

  private static String escape(final String token) {
    return token.replace("~", "~0").replace("/", "~1");
  }
}
