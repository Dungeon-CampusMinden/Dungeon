import {TextDocument } from 'vscode-languageserver-textdocument';
import { Diagnostic, DiagnosticSeverity } from 'vscode-languageserver/node';

// Funktion zum Überprüfen auf nicht deklarierte oder doppelt deklarierte Variablen
export function checkForVariableIssues(textDocument: TextDocument): Diagnostic[] {
    const diagnostics: Diagnostic[] = [];
    const lines = textDocument.getText().split(/\r?\n/g);

    const variableDeclarations: Set<string> = new Set();
    const usedVariables: Set<string> = new Set();

    for (let i = 0; i < lines.length; i++) {
        const line = lines[i];
        let match: RegExpExecArray | null;

        // Variablendeklarationen überprüfen
        if (match = /\bvar\s+(\w+)/.exec(line)) {
            const variableName = match[1];
            if (variableDeclarations.has(variableName)) {
                diagnostics.push({
                    severity: DiagnosticSeverity.Warning,
                    range: {
                        start: { line: i, character: match.index },
                        end: { line: i, character: match.index + variableName.length }
                    },
                    message: `Variable '${variableName}' ist bereits deklariert.`,
                    source: 'ex'
                });
            } else {
                variableDeclarations.add(variableName);
            }
        }

        // Verwendete Variablen erfassen
        const words = line.split(/\W+/);
        for (const word of words) {
            if (variableDeclarations.has(word)) {
                usedVariables.add(word);
            }
        }

        // Überprüfung auf Verwendung nicht deklarierter Variablen
        if (match = /\bprint\((\w+)\)/.exec(line)) {
            const variableName = match[1];
            if (!variableDeclarations.has(variableName)) {
                diagnostics.push({
                    severity: DiagnosticSeverity.Error,
                    range: {
                        start: { line: i, character: match.index },
                        end: { line: i, character: line.length }
                    },
                    message: `Variable '${variableName}' ist nicht deklariert.`,
                    source: 'ex'
                });
            }
        }
    }

    return diagnostics;
}

// Funktion zum Überprüfen von Syntaxfehlern bei Klammern
export function checkForBracketErrors(textDocument: TextDocument): Diagnostic[] {
    const diagnostics: Diagnostic[] = [];
    const lines = textDocument.getText().split(/\r?\n/g);

    for (let i = 0; i < lines.length; i++) {
        const line = lines[i];
        const openBrackets = (line.match(/\(/g) || []).length;
        const closeBrackets = (line.match(/\)/g) || []).length;

        if (openBrackets > closeBrackets) {
            diagnostics.push({
                severity: DiagnosticSeverity.Error,
                range: {
                    start: { line: i, character: line.indexOf('(') },
                    end: { line: i, character: line.length }
                },
                message: "Nicht geschlossene '(' gefunden.",
                source: 'ex'
            });
        } else if (closeBrackets > openBrackets) {
            diagnostics.push({
                severity: DiagnosticSeverity.Error,
                range: {
                    start: { line: i, character: line.indexOf(')') },
                    end: { line: i, character: line.length }
                },
                message: "Ungepaarte ')' gefunden.",
                source: 'ex'
            });
        }
    }

    return diagnostics;
}

export function checkForUnusedVariables(textDocument: TextDocument): Diagnostic[] {
    const diagnostics: Diagnostic[] = [];
    const lines = textDocument.getText().split(/\r?\n/g);

    const variableDeclarations: Map<string, { line: number, character: number }> = new Map();
    const usedVariables: Set<string> = new Set();

    // Erfassen der deklarierten Variablen
    for (let i = 0; i < lines.length; i++) {
        const line = lines[i];
        const match = /\bvar\s+(\w+)/.exec(line);

        if (match) {
            const variableName = match[1];
            variableDeclarations.set(variableName, { line: i, character: match.index });
        }
    }

    // Erfassen der verwendeten Variablen
    for (let i = 0; i < lines.length; i++) {
        const line = lines[i];
        const words = line.split(/\W+/);

        for (const word of words) {
            if (variableDeclarations.has(word)) {
                usedVariables.add(word);
            }
        }
    }

    // Überprüfen auf unbenutzte Variablen
    for (const [variableName, position] of variableDeclarations.entries()) {
        if (!usedVariables.has(variableName)) {
            diagnostics.push({
                severity: DiagnosticSeverity.Warning,
                range: {
                    start: { line: position.line, character: position.character },
                    end: { line: position.line, character: position.character + variableName.length }
                },
                message: `Variable '${variableName}' ist deklariert, aber unbenutzt.`,
                source: 'ex'
            });
        }
    }

    return diagnostics;
}
// Weitere spezifische Überprüfungen können hier hinzugefügt werden
