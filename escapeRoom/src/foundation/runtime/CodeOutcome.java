package foundation.runtime;

/** Non-secret evaluation outcome of one numeric-code command. */
public enum CodeOutcome {
  /** The supplied attempt exactly matched and completed the active riddle. */
  CORRECT,
  /** The supplied attempt did not match; no attempt count is retained. */
  INCORRECT,
  /** Authority state prevented evaluation of the supplied value. */
  NOT_EVALUATED
}
