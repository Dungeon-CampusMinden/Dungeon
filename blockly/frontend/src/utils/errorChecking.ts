
/**
  * Checks if a while loop has an empty head
 * @param code the code to check
 * @returns whether a while loop has an empty head
 */
export function hasEmptyWhileLoopHead(code: string): boolean {
  const regex = /while\s*\(\s*\)\s*\{/;
  return regex.test(code);
}
/**
 * Checks whether an if statement has a missing condition
 * @param code the code to check
 * @returns whether an if statmeent has a missing condition
 */
export function hasIfWithMissingCondition(code: string): boolean {
  const regex = /if\s*\(\s*falsch\s*\)/;

  return regex.test(code);
}
/**
 * Checks whether a tile has no direction statement
 * @param code the code to check
 * @param tileType the tile type to check for in the code
 * @returns whether the tile type is missing a direction
 */
export function isMissingDirectionInIsNearTile(code: string, tileType: string): boolean {
  const regex = new RegExp(
    `hero\\.isNearTile\\s*\\(\\s*LevelElement\\.${tileType}\\s*(?:,\\s*)?\\)`,
    "s"
  );

  return regex.test(code);
}

/**
 * Checks whether a direction is missing in component statement
 * @param code the code to check
 * @param componentName the component type to check for
 * @returns whether the the compnent type is missing a direction
 */
export function isMissingDirectionInIsNearComponent(
  code: string,
  componentName: string
): boolean {
  const regex = new RegExp(
    `hero\\.isNearComponent\\s*\\(\\s*${componentName}\\.class\\s*(?:,\\s*)?\\)`,
    "s"
  );

  return regex.test(code);
}

/**
 * Checks whether a direction is missing in component statement
 * @param code the code to check
 * @param componentName the component type to check for
 * @returns whether the the compnent type is missing a direction
 */
export function isHeroActiveWithoutParameters(code: string): boolean {
  // Match hero.active() optionally with spaces: hero.active ( )
  const regex = /hero\.active\s*\(\s*\)/s;
  return regex.test(code);
}

/**
 * Checks whether a hero interaction is missing a direction
 * @param code the code to check
 * @returns whether the hero interaction is missing a direction
 */
export function isHeroInteractWithoutParameters(code: string): boolean {
  // Match hero.active() optionally with spaces: hero.active ( )
  const regex = /hero\.interact\s*\(\s*\)/s;
  return regex.test(code);
}


/**
 * Checks whether the if comparison is incomplete
 * @param code the code to check
 * @returns whether the if comparison is incomplete
 */
export function hasIncompleteIfComparison(code: string): boolean {
  const regex = /if\s*\(\s*([^\s=]*)?\s*==\s*([^\s)]*)?\s*\)/;

  // Teste, ob eine oder beide Seiten leer sind
  const match = code.match(regex);
  if (!match) return false;

  const left = match[1] ?? "";
  const right = match[2] ?? "";

  // true wenn eine oder beide Seiten leer
  return left.trim() === "" || right.trim() === "";

}

/**
 * Check for the error message that a simple rotation has no direction
 * @param code whether the message is in the code
 */
export function containsDirection(code: string): boolean {
  return /keine\s+Richtungsangabe/.test(code);
}

/**
 *
 * Check if a variable is used but not initialized in the code
 * @param codeLines
 * @returns a message with the variable that is not declared
 */
export const checkIfVariablesAreDeclared = (codeLines : string[]) => {

// Globaler Scope
  const scopes: Set<string>[] = [new Set<string>()];
  let message = "";
  for (let line of codeLines) {
    line = line.trim();

    // Variablen deklarieren (nur let, var, const - C-Typen kannst du hinzufügen)
    const matchDecl = line.match(/(let|var|const|int)\s+([a-zA-Z_][a-zA-Z0-9_]*)/);
    if (matchDecl) {
      const varName = matchDecl[2];
      scopes[scopes.length - 1].add(varName);
      continue;
    }

    // Variablen zuweisen
    const matchAssign = line.match(/^([a-zA-Z_][a-zA-Z0-9_]*)\s*=/);
    if (matchAssign) {
      const varName = matchAssign[1];
      // Prüfen, ob Variable in einem Scope existiert
      const exists = scopes.some(scope => scope.has(varName));
      if (!exists) {
        message = `Fehler: Variable '${varName}' wurde nicht erstellt!`
      }
    }
  }
  return message;
}

/**
 * Checks if a for loop does not have an interation count
 * @param code whether the number of iterations are missing in the for loop
 */
export function hasMissingIterationCount(code: string): boolean {
  /**
   * Erklärung des Regex:
   * - for\s*\(           → for-Schleife mit öffnender Klammer
   * - [^;]*;             → Initialisierungsteil
   * - \s*[^<]*<\s*;     → Vergleich mit "<", aber nichts danach (FEHLER)
   */
  const invalidForRegex =
    /for\s*\(\s*[^;]*;\s*[^<]*<\s*;\s*[^)]*\)/;

  return invalidForRegex.test(code);
}

