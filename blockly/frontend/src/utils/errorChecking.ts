export function hasEmptyWhileLoopHead(code: string): boolean {
  // Regex explanation:
  // while      -> match the keyword
  // \s*        -> allow optional whitespace
  // \(         -> opening parenthesis
  // \s*        -> allow whitespace inside
  // \)         -> closing parenthesis with nothing between
  // \s*        -> optional whitespace
  // \{         -> opening curly brace
  const regex = /while\s*\(\s*\)\s*\{/;

  return regex.test(code);
}

export function hasIfWithMissingCondition(code: string): boolean {
  // Explanation:
  // if         -> match "if"
  // \s*        -> optional whitespace
  // \(         -> opening parenthesis
  // \s*        -> optional whitespace
  // falsch     -> the word "falsch"
  // \s*        -> optional whitespace
  // \)         -> closing parenthesis
  const regex = /if\s*\(\s*falsch\s*\)/;

  return regex.test(code);
}

export function isMissingDirectionInIsNearTile(code: string, tileType: string): boolean {
  const regex = new RegExp(
    `hero\\.isNearTile\\s*\\(\\s*LevelElement\\.${tileType}\\s*(?:,\\s*)?\\)`,
    "s"
  );

  return regex.test(code);
}

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

export function isHeroActiveWithoutParameters(code: string): boolean {
  // Match hero.active() optionally with spaces: hero.active ( )
  const regex = /hero\.active\s*\(\s*\)/s;
  return regex.test(code);
}

export function isHeroInteractWithoutParameters(code: string): boolean {
  // Match hero.active() optionally with spaces: hero.active ( )
  const regex = /hero\.interact\s*\(\s*\)/s;
  return regex.test(code);
}

export function hasIncompleteIfComparison(code: string): boolean {
  // Regex Erklärung:
  // if\s*\(       -> "if" gefolgt von "(" mit optionalem Whitespace
  // \s*([^\s=]*)?\s*  -> linke Seite (optional)
  // ==             -> Vergleichsoperator
  // \s*([^\s)]*)?\s* -> rechte Seite (optional)
  // \)             -> schließende Klammer
  const regex = /if\s*\(\s*([^\s=]*)?\s*==\s*([^\s)]*)?\s*\)/;

  // Teste, ob eine oder beide Seiten leer sind
  const match = code.match(regex);
  if (!match) return false;

  const left = match[1] ?? "";
  const right = match[2] ?? "";

  // true wenn eine oder beide Seiten leer
  return left.trim() === "" || right.trim() === "";

}

export function containsDirection(code: string): boolean {
  return /keine\s+Richtungsangabe/.test(code);
}
