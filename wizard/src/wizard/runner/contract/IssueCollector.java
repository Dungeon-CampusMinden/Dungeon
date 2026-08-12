package wizard.runner.contract;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import wizard.runner.canonical.CanonicalJson;
import wizard.runner.contract.ValidationIssue.Entity;

/** Mutable issue sink that exposes immutable issues in the normative deterministic order. */
public final class IssueCollector {
  private final Map<IssueIdentity, ValidationIssue> issues = new LinkedHashMap<>();

  /** Creates an empty collector using enum declaration order as the normative issue order. */
  public IssueCollector() {}

  /**
   * Adds an issue, merging related paths for the same normative issue identity.
   *
   * @param issue issue to collect
   */
  public void add(final ValidationIssue issue) {
    Objects.requireNonNull(issue, "issue");
    IssueIdentity identity = IssueIdentity.from(issue);
    ValidationIssue existing = issues.get(identity);
    if (existing == null) {
      issues.put(identity, issue);
      return;
    }
    if (!existing.messageKey().equals(issue.messageKey())) {
      throw new IllegalStateException("One issue identity has conflicting message keys");
    }
    TreeSet<String> related = new TreeSet<>(ValidationIssue::compareCodePoints);
    related.addAll(existing.relatedPaths());
    related.addAll(issue.relatedPaths());
    issues.put(
        identity,
        new ValidationIssue(
            existing.severity(),
            existing.phase(),
            existing.code(),
            existing.messageKey(),
            existing.arguments(),
            existing.path(),
            existing.entity(),
            List.copyOf(related)));
  }

  /**
   * Adds each supplied issue.
   *
   * @param additions issues to collect
   */
  public void addAll(final Iterable<ValidationIssue> additions) {
    Objects.requireNonNull(additions, "additions").forEach(this::add);
  }

  /**
   * Returns a detached immutable list in normative issue order.
   *
   * @return sorted immutable issue list
   */
  public List<ValidationIssue> issues() {
    List<Map.Entry<IssueIdentity, ValidationIssue>> entries = new ArrayList<>(issues.entrySet());
    entries.sort(Map.Entry.comparingByKey(identityComparator()));
    return entries.stream().map(Map.Entry::getValue).toList();
  }

  /**
   * Reports whether any collected issue is blocking.
   *
   * @return true when at least one error exists
   */
  public boolean hasErrors() {
    return issues.values().stream().anyMatch(issue -> issue.severity() == ValidationSeverity.ERROR);
  }

  private Comparator<IssueIdentity> identityComparator() {
    Comparator<String> strings = ValidationIssue::compareCodePoints;
    return Comparator.comparingInt((IssueIdentity identity) -> identity.severity().ordinal())
        .thenComparingInt(identity -> identity.phase().ordinal())
        .thenComparing(IssueIdentity::path, strings)
        .thenComparing(identity -> identity.code().name(), strings)
        .thenComparingInt(identity -> identity.entity().isPresent() ? 1 : 0)
        .thenComparing(identity -> identity.entity().map(Entity::kind).orElse(""), strings)
        .thenComparing(identity -> identity.entity().map(Entity::id).orElse(""), strings)
        .thenComparing(IssueIdentity::canonicalArguments, strings);
  }

  private record IssueIdentity(
      ValidationSeverity severity,
      ValidationPhase phase,
      IssueCode code,
      String path,
      Optional<Entity> entity,
      String canonicalArguments) {
    private static IssueIdentity from(final ValidationIssue issue) {
      return new IssueIdentity(
          issue.severity(),
          issue.phase(),
          issue.code(),
          issue.path(),
          issue.entity(),
          CanonicalJson.encode(issue.arguments()));
    }
  }
}
