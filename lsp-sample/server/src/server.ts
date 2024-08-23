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
	DocumentDiagnosticReportKind,
	type DocumentDiagnosticReport,
	TextEdit,
	Range
} from 'vscode-languageserver/node';
import { createCompletionItems } from './completionItems';
import {
	TextDocument
} from 'vscode-languageserver-textdocument';
import { checkForBracketErrors, checkForUnusedVariables, checkForVariableIssues } from './diagnostics';


// Create a connection for the server, using Node's IPC as a transport.
// Also include all preview / proposed LSP features.
const connection = createConnection(ProposedFeatures.all);

// Create a simple text document manager.
const documents: TextDocuments<TextDocument> = new TextDocuments(TextDocument);
//In dieser Liste werden alle deklarierten Variablen und Objekte für die Completions gespeichert
const declaredVariables: Map<string, string> = new Map();
//Alle Typen auf die geprüft wird für die Completions der deklarierten Objekte und Variablen
const typeFields: Map<string, string[]> = new Map([
    ['single_choice_task', [
        'description',
        'answers',
        'correct_answer_index',
        'scenario_builder', 
        'explanation'
    ]],
    ['replacement_task', [
        'description',
        'initial_element_set',
        'elements',
        'rules',
        'answer_sequence',
        'answer_configuration',
        'fn_score'
    ]],
    ['mapping_task', [
        'description',
        'mapping',
        'fn_score'
    ]],
    ['gap_task', [
        'description',
        'gaps',
        'fn_score'
    ]],
    ['multiple_choice_task', [
        'description',
        'answers',
        'correct_answer_indices',
        'fn_score'
    ]],
    ['var', []
    ],
    ['entity_type', [
        'description',
        'answers',
        'correct_answer_indices',
        'fn_score'
    ]],
    // Weitere Typen können hier ergänzt werden
]);

const configTypes = Array.from(typeFields.keys());
//const configTypes = ['var', 'entity_type', 'item_type', 'graph', 'dungeon_config', 'multiple_choice_task', 'single_choice_task', 'replacement_task', 'mapping_task', 'gap_task'];

let hasConfigurationCapability = false;
let hasWorkspaceFolderCapability = false;
let hasDiagnosticRelatedInformationCapability = false;

connection.onInitialize((params: InitializeParams) => {
	const capabilities = params.capabilities;

	// Does the client support the `workspace/configuration` request?
	// If not, we fall back using global settings.
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
			// Tell the client that this server supports code completion.
			completionProvider: {
				resolveProvider: true,
				triggerCharacters: ["."]
			},
			diagnosticProvider: {
				interFileDependencies: false,
				workspaceDiagnostics: false
			}
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
	// Refresh the diagnostics since the `maxNumberOfProblems` could have changed.
	// We could optimize things here and re-fetch the setting first can compare it
	// to the existing setting, but this is out of scope for this example.
	connection.languages.diagnostics.refresh();
});

function getDocumentSettings(resource: string): Thenable<ExampleSettings> {
	if (!hasConfigurationCapability) {
		return Promise.resolve(globalSettings);
	}
	let result = documentSettings.get(resource);
	if (!result) {
		result = connection.workspace.getConfiguration({
			scopeUri: resource,
			section: 'languageServerExample'
		});
		documentSettings.set(resource, result);
	}
	return result;
}

// Only keep settings for open documents
documents.onDidClose(e => {
	documentSettings.delete(e.document.uri);
});


connection.languages.diagnostics.on(async (params) => {
	const document = documents.get(params.textDocument.uri);
	if (document !== undefined) {
		return {
			kind: DocumentDiagnosticReportKind.Full,
			items: await validateTextDocument(document)
		} satisfies DocumentDiagnosticReport;
	} else {
		// We don't know the document. We can either try to read it from disk
		// or we don't report problems for it.
		return {
			kind: DocumentDiagnosticReportKind.Full,
			items: []
		} satisfies DocumentDiagnosticReport;
	}
});

// The content of a text document has changed. This event is emitted
// when the text document first opened or when its content has changed.
documents.onDidChangeContent(change => {
	validateTextDocument(change.document);
});

async function validateTextDocument(textDocument: TextDocument): Promise<Diagnostic[]> {
	const diagnostics: Diagnostic[] = [];
	const text = textDocument.getText();
    const lines = text.split(/\r?\n/g);
	
	declaredVariables.clear();

    // Durchsuche das Dokument nach Deklarationen
    for (const line of lines) {
        for (const type of configTypes) {
            const regex = new RegExp(`\\b${type}\\s+(\\w+)`);
            const match = regex.exec(line);
            if (match) {
                declaredVariables.set(match[1], type);
            }
        }
    }
    // Rufe die spezifischen Diagnosefunktionen auf
    diagnostics.push(...checkForVariableIssues(textDocument));
    diagnostics.push(...checkForBracketErrors(textDocument));
    diagnostics.push(...checkForUnusedVariables(textDocument));
    return diagnostics;
}

connection.onDidChangeWatchedFiles(_change => {
	// Monitored files have change in VSCode
	connection.console.log('We received a file change event');
});

// This handler provides the initial list of the completion items.
connection.onCompletion(
	(_textDocumentPosition: TextDocumentPositionParams): CompletionItem[] => {
		const document = documents.get(_textDocumentPosition.textDocument.uri);

        if (!document) {
            return [];
        }

        const position = _textDocumentPosition.position;
		const textBeforeCursor = document.getText({
            start: { line: position.line, character: 0 },
            end: position
        }).trim();
        const completionStart = { line: position.line, character: 0 };
        const completionEnd = { line: position.line, character: position.character };
        const range = Range.create(completionStart, completionEnd);
		// The pass parameter contains the position of the text document in
		// which code complete got requested. For the example we ignore this
		// info and always provide the same completion items.
		const suggestions: CompletionItem[] = createCompletionItems(range);
		// Prüfen, ob der Benutzer auf ein bekanntes Typ-Objekt zugreift
        const objectAccessRegex = /(\w+)\.\s*$/;
        const match = objectAccessRegex.exec(textBeforeCursor);

        if (match) {
            const variableName = match[1];
            const variableType = declaredVariables.get(variableName);

            if (variableType && typeFields.has(variableType)) {
                // Vorschlagen der Felder für den spezifischen Typ
                const fields = typeFields.get(variableType) || [];
                for (const field of fields) {
                    suggestions.push({
                        label: field,
                        kind: CompletionItemKind.Field,
                        documentation: `Feld von ${variableType}: ${field}`,
                        insertText: field
                    });
                }
                return suggestions;
            }
        } else {
            // Vorschläge für Typen selbst (z.B. 'single_choice_task', 'replacement_task')
            for (const [type] of typeFields) {
                suggestions.push({
                    label: type,
                    kind: CompletionItemKind.Class,
                    documentation: `Definiert ein neues Objekt vom Typ ${type}.`,
                    insertText: type
                });
            }

            // Vorschlag für alle gefundenen Variablen und Instanzen
            declaredVariables.forEach((type, name) => {
                suggestions.push({
                    label: name,
                    kind: CompletionItemKind.Variable,
                    documentation: `Typ: ${type}`,
                    insertText: name
                });
            });
        }
		return suggestions;
	}
	
);


// This handler resolves additional information for the item selected in
// the completion list.
connection.onCompletionResolve(
	(item: CompletionItem): CompletionItem => {
		if (item.data === 1) {
			item.detail = 'Dungeon var';
			item.documentation = 'Dungeon var documentation';
		} else if (item.data === 2) {
			item.detail = 'entity_type details';
			item.documentation = 'entity_type documentation';
		}
		return item;
	}
);

// Make the text document manager listen on the connection
// for open, change and close text document events
documents.listen(connection);

// Listen on the connection
connection.listen();
