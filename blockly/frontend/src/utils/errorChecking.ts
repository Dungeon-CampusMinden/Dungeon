
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
  const incompletePattern = /\(\s*(?:==|!=|>=|<=|>|<)|(?:==|!=|>=|<=|>|<)\s*\)|(?:&&|\|\|)\s*(?:==|!=|>=|<=|>|<)|(?:==|!=|>=|<=|>|<)\s*(?:&&|\|\|)/;

  return incompletePattern.test(code);
}

/**
 * Checks if a certain string is contained in the code
 * @param code the code to check
 * @param searchTerm to look for
 * @returns whether the string is contaiend in the code
 */
export function containsString(code: string, searchTerm: string): boolean {
  return code.includes(searchTerm);
}

/**
 * Check if there is an incomplete logical operation (&& or || operation)
 * @param code the code to check
 * @returns whether there is an incomplete logical operation
 */
export function hasIncompleteLogicalOperator(code: string): boolean {
  const pattern = /(\(\s*&&|\(\s*\|\||&&\s*\)|\|\|\s*\))/;

  return pattern.test(code);
}
/**
 *
 * Check if a variable is used but not initialized in the code
 * @param fullProgram
 @returns a message with the variable that is not declared
 */
export const checkIfVariablesAreDeclared = (fullProgram : string) => {

  const scopes = [new Set()];
  const codeLines = fullProgram.split("\n");

  const keywords = new Set(["if", "else", "while", "for", "return", "let", "var", "const", "int", "true", "false", "hero", "move"]);

  for (let line of codeLines) {
    line = line.trim();
    if (!line) continue;

    const declMatch = line.match(/(?:let|var|const|int)\s+([a-zA-Z_]\w*)/);
    if (declMatch) {
      const varName = declMatch[1];
      scopes[scopes.length - 1].add(varName);
    }

    const potentialVars = line.match(/(?<![.\w])\b[a-zA-Z_]\w*\b(?![.\w])/g) || [];

    for (const word of potentialVars) {
      if (keywords.has(word) || (declMatch && declMatch[1] === word)) {
        continue;
      }

      const exists = scopes.some(scope => scope.has(word));

      if (!exists) {
        return `Fehler: Variable '${word}' wurde nicht erstellt!`;
      }
    }
  }
  return "";
};

/**
 * Checks if a for loop does not have an interation count
 * @param code whether the number of iterations are missing in the for loop
 * @returns whether the for loop has an interation count
 */
export function hasMissingIterationCount(code: string): boolean {
  const invalidForRegex =
    /for\s*\(\s*[^;]*;\s*[^<]*<\s*;\s*[^)]*\)/;

  return invalidForRegex.test(code);
}

/**
 * Checks whether the boss direction command is missing the direction
 * @param code the code to check
 * @returns whether the direction is missing in the code
 */
export function hasMissingBossDirection(code: string): boolean {
  const emptyArgsRegex = /checkBossViewDirection\s*\(\s*\)/;

  return emptyArgsRegex.test(code);
}

/**
 * Checks if there is a not command with a missing condition
 * @param code code to check
 * @returns whether the not command is missing a direction
 */
export function hasEmptyNotCondition(code: string): boolean {
  const emptyNotRegex = /!\s*(?=[)|&])/;

  return emptyNotRegex.test(code);
}

