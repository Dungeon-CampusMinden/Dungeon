/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */
import {
	createConnection,
	TextDocuments,
	Diagnostic,
	DiagnosticSeverity,
	ProposedFeatures,
	InitializeParams,
	DidChangeConfigurationNotification,
	CompletionItem,
	CompletionItemKind,
	TextDocumentPositionParams,
	TextDocumentSyncKind,
	InitializeResult,
	Range,
} from 'vscode-languageserver/node';
import { createCompletionItems } from './completionItems';
import * as path from 'path';
import {
	Position,
	TextDocument
} from 'vscode-languageserver-textdocument';
import { exec } from 'child_process';
import { promisify } from 'util';
import { writeFile, unlink } from 'fs/promises';
import { tmpdir } from 'os';
import { join } from 'path';
// Create a connection for the server, using Node's IPC as a transport.
// Also include all preview / proposed LSP features.
const connection = createConnection(ProposedFeatures.all);
const javaJarPath = path.join(__dirname, '..', 'jars', 'Dungeon-Diagnostics.jar');
const execPromise = promisify(exec);
// Create a simple text document manager.
const documents: TextDocuments<TextDocument> = new TextDocuments(TextDocument);
// In dieser Liste werden alle deklarierten Variablen und Objekte für die Completions gespeichert
const declaredVariables: Map<string, string> = new Map();
// Alle Typen auf die geprüft wird für die Completions der deklarierten Objekte und Variablen
// Im Value steht einer Liste der dazugehörigen Properties
const typeFields: Map<string, string[]> = new Map([
	['single_choice_task', [
		'description',
		'answers',
		'correct_answer_index',
		'points',
		'points_to_pass',
		'explanation',
		'grading_function',
		'scenario_builder'
	]],
	['assign_task', [
		'description',
		'solution',
		'points',
		'points_to_pass',
		'explanation',
		'grading_function',
		'scenario_builder'
	]],
	['multiple_choice_task', [
		'description',
		'answers',
		'correct_answer_indices',
		'points',
		'points_to_pass',
		'explanation',
		'grading_function',
		'scenario_builder'
	]],
	['var', []
	],
	['entity_type', []
	],
	['item_type', []
	],
	['dungeon_config', [
		'dependency_graph'
	]
	],
	// Weitere Typen können hier ergänzt werden
]);
// eine Liste von allen Keys der Liste typeFields
const keywords = Array.from(typeFields.keys());
const primitiveTypes = ['int', 'string', 'boolean', 'entity'];
const functions = ['instantiate'];

let hasConfigurationCapability = false;
let hasWorkspaceFolderCapability = false;
let hasDiagnosticRelatedInformationCapability = false;

connection.onInitialize((params: InitializeParams) => {
	const capabilities = params.capabilities;

	hasConfigurationCapability = !!(
		capabilities.workspace && !!capabilities.workspace.configuration
	);
	hasWorkspaceFolderCapability = !!(
		capabilities.workspace && !!capabilities.workspace.workspaceFolders
	);
	hasDiagnosticRelatedInformationCapability = !!(
		capabilities.textDocument &&
		capabilities.textDocument.publishDiagnostics &&
		capabilities.textDocument.publishDiagnostics.relatedInformation
	);

	const result: InitializeResult = {
		capabilities: {
			textDocumentSync: TextDocumentSyncKind.Incremental,
			// TriggerCharacters sind Sonderzeichen, welche completions auslösen sollen
			completionProvider: {
				resolveProvider: true,
				triggerCharacters: ['=', '.', ':', '(', ',', ' ']
			},
		}
	};
	if (hasWorkspaceFolderCapability) {
		result.capabilities.workspace = {
			workspaceFolders: {
				supported: true
			}
		};
	}
	return result;
});

connection.onInitialized(() => {
	if (hasConfigurationCapability) {
		// Register for all configuration changes.
		connection.client.register(DidChangeConfigurationNotification.type, undefined);
	}
	if (hasWorkspaceFolderCapability) {
		connection.workspace.onDidChangeWorkspaceFolders(_event => {
			connection.console.log('Workspace folder change event received.');
		});
	}
});

// The example settings
interface ExampleSettings {
	maxNumberOfProblems: number;
}

// The global settings, used when the `workspace/configuration` request is not supported by the client.
// Please note that this is not the case when using this server with the client provided in this example
// but could happen with other clients.
const defaultSettings: ExampleSettings = { maxNumberOfProblems: 1000 };
let globalSettings: ExampleSettings = defaultSettings;

// Cache the settings of all open documents
const documentSettings: Map<string, Thenable<ExampleSettings>> = new Map();

connection.onDidChangeConfiguration(change => {
	if (hasConfigurationCapability) {
		// Reset all cached document settings
		documentSettings.clear();
	} else {
		globalSettings = <ExampleSettings>(
			(change.settings.languageServerExample || defaultSettings)
		);
	}
	connection.languages.diagnostics.refresh();
});



// Document Einstellungen beim schließen löschen
documents.onDidClose(e => {
	documentSettings.delete(e.document.uri);
});

// Der Inhalt eines Textdokuments hat sich geändert. Dieses Ereignis wird ausgelöst, 
// wenn das Textdokument erstmals geöffnet wird oder wenn sich dessen Inhalt geändert hat.
documents.onDidChangeContent(change => {
	validateTextDocument(change.document);
});

async function validateTextDocument(textDocument: TextDocument): Promise<void> {
	const diagnostics: Diagnostic[] = [];
	const text = textDocument.getText();
	const lines = text.split(/\r?\n/g);
	declaredVariables.clear();

	//Durchsuche das Dokument nach Deklarationen
	for (const line of lines) {
		for (const type of keywords) {
			const regex = new RegExp(`\\b${type}\\s+(\\w+)`);
			const match = regex.exec(line);
			if (match) {
				declaredVariables.set(match[1], type);
			}
		}
	}


	try {
		// Erstellen eines temporären Dateipfads
		const tempFilePath = join(tmpdir(), `temp-${Date.now()}.dng`);
		// Schreiben des aktuellen Inhalts in die temporäre Datei
		// Notwendiger workaround, da das JAR sonst auf einer veralteten Datei
		// analysiert und falsche Diagnostics ausgegeben werden
		await writeFile(tempFilePath, text, 'utf8');

		// Ausführen des Java-Prozesses mit der temporären Datei
		const { stdout, stderr } = await execPromise(`java -jar ${javaJarPath} ${tempFilePath}`);

		if (stderr) {
			console.error(`Fehler im Java-Programm: ${stderr}`);
		}

		// Verarbeitung des Outputs und Auffüllen von diagnostics
		try {
			const output = JSON.parse(stdout);
			if (output.errors && Array.isArray(output.errors)) {
				output.errors.forEach((err: any) => {
					diagnostics.push({
						severity: DiagnosticSeverity.Error,
						range: {
							start: { line: err.line, character: err.column },
							end: { line: err.line, character: err.column }
						},
						message: err.message,
						source: 'ex'
					});
				});
			}
		} catch (parseError) {
			console.error('Fehler beim Parsen des JSON-Outputs:', parseError);
			console.error('Raw stdout:', stdout);
		}

		// Löschen der temporären Datei
		await unlink(tempFilePath);
	} catch (error) {
		console.error(`Fehler beim Ausführen des Java-Prozesses: ${error}`);
	}

	// Senden der Diagnosen an den Client
	connection.sendDiagnostics({ uri: textDocument.uri, diagnostics });
}

connection.onDidChangeWatchedFiles(_change => {
	// Monitored files have change in VSCode
	connection.console.log('We received a file change event');
});

// Funktion, um den umgebenden Typ zu ermitteln
function getEnclosingTypeAtLine(document: TextDocument, position: Position): string | null {
	// Gesamter Text bis zur Cursorposition
	const offset = document.offsetAt(position);
	const textBeforeCursor = document.getText().substring(0, offset);

	let braceStack = 0;
	let i = offset - 1;

	while (i >= 0) {
		const char = textBeforeCursor[i];
		if (char === '}') {
			braceStack--;
		} else if (char === '{') {
			braceStack++;
			if (braceStack > 0) {
				// Öffnende Klammer gefunden
				// Typ vor der Klammer ermitteln
				const beforeBrace = textBeforeCursor.substring(0, i).trimEnd();

				// Muster: <type> <name> {
				const typeMatch = /(\w+)\s+\w+\s*$/.exec(beforeBrace);
				if (typeMatch) {
					const type = typeMatch[1];
					return type;
				} else {
					return null;
				}
			}
		}
		i--;
	}
	return null;
}

// Dieser Handler stellt die Liste der Completions bereit
connection.onCompletion(
	(_textDocumentPosition: TextDocumentPositionParams): CompletionItem[] => {
		const document = documents.get(_textDocumentPosition.textDocument.uri);

		if (!document) {
			return [];
		}
		const text = document.getText({
			start: { line: _textDocumentPosition.position.line, character: 0 },
			end: _textDocumentPosition.position
		});

		const trimmedText = text.trim();
		const suggestions: CompletionItem[] = [];
		const position = _textDocumentPosition.position;
		const completionStart = { line: position.line, character: 0 };
		const completionEnd = { line: position.line, character: position.character };
		const range = Range.create(completionStart, completionEnd);

		const isConfigType = keywords.some(keyword => {
			const regex = new RegExp(`^\s*${keyword}\s*[^:{]*$`);
			return regex.test(trimmedText);
		});

		if (isConfigType) {
			// Keine Vorschläge nach Begriffen wie var, entity_type oder anderen TopLevel begriffen, dass ein freier Name vergeben werden kann
			return [];
		}

		// Umgebenden Typ ermitteln
		const enclosingType = getEnclosingTypeAtLine(document, position);

		if (enclosingType && typeFields.has(enclosingType)) {
			// Überprüfen, ob der Cursor am Zeilenanfang nach Whitespace steht
			const isAtLineStart = /^\s*\w+$/.test(text);
			if (!isAtLineStart) {
				// Nicht am Zeilenanfang, keine Vorschläge machen
				return [];
			}
			const fields = typeFields.get(enclosingType) || [];
			const suggestions: CompletionItem[] = fields.map(field => ({
				label: field,
				kind: CompletionItemKind.Field,
				documentation: `Field of ${enclosingType}: ${field}`
			}));
			return suggestions;
		}

		// Zeilenanfang-Kontext
		if (/^\s*$/.test(text) || /^[a-zA-Z_]\w*\s*$/.test(trimmedText)) {
			keywords.forEach(keyword => {
				suggestions.push({
					label: keyword,
					kind: CompletionItemKind.Keyword,
					documentation: `Top-level keyword: ${keyword}`
				});
			});
			const snippets = createCompletionItems(range);
			snippets.forEach(snippet => {
				suggestions.push(snippet);
			});
			return suggestions;
		}

		// Typdefinition-Kontext (nach ':', nach '(', nach ',')
		if (/:$/.test(trimmedText) || /\($/.test(trimmedText) || /,$/.test(trimmedText)) {
			primitiveTypes.forEach(type => {
				suggestions.push({
					label: type,
					kind: CompletionItemKind.TypeParameter,
					documentation: `Primitive type: ${type}`
				});
			});
			return suggestions;
		}

		// Zuweisung-Kontext (nach '=')
		if (/=\s*$/.test(trimmedText)) {
			declaredVariables.forEach((type, name) => {
				suggestions.push({
					label: name,
					kind: CompletionItemKind.Variable,
					documentation: `Variable of type ${type}: ${name}`
				});
			});

			functions.forEach(fn => {
				suggestions.push({
					label: `${fn}()`,
					kind: CompletionItemKind.Function,
					documentation: `Function: ${fn}`
				});
			});
			return suggestions;
		}

		// Kontext nach einem '.' (Objektzugriff)
		const objectAccessMatch = /(\w+)\.\s*$/.exec(trimmedText);
		if (objectAccessMatch) {
			const objectName = objectAccessMatch[1];
			const objectType = declaredVariables.get(objectName); // Hier wird der Typ des Objekts abgerufen

			if (objectType && typeFields.has(objectType)) {
				const fields = typeFields.get(objectType) || [];
				fields.forEach(field => {
					suggestions.push({
						label: field,
						kind: CompletionItemKind.Field,
						documentation: `Field of ${objectType}: ${field}`
					});
				});
			}
			return suggestions;
		}
		// Allgemeiner Kontext (Überall sonst)
		declaredVariables.forEach((type, name) => {
			suggestions.push({
				label: name,
				kind: CompletionItemKind.Variable,
				documentation: `Variable of type ${type}: ${name}`
			});
		});
		return suggestions;
	}

);

// Make the text document manager listen on the connection
// for open, change and close text document events
documents.listen(connection);

// Listen on the connection
connection.listen();
