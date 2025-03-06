import * as vscode from 'vscode';
import sendBlocklyFile from './sendBlocklyFile';
import fetchLanguageConfig from './languageProvider';

export function activate(context: vscode.ExtensionContext) {
    // Register the command
    let commandDisposable = vscode.commands.registerCommand('blockly-code-runner.sendBlocklyFile', sendBlocklyFile);

	// Register the completion provider for multiple languages/extensions
	let completionProvider = vscode.languages.registerCompletionItemProvider(
		{ scheme: 'file', language: 'java' },
		{
			async provideCompletionItems(document: vscode.TextDocument, position: vscode.Position) {
				const linePrefix = document.lineAt(position).text.substring(0, position.character);

				if (!linePrefix.trim()) {
					const gameCompletion = new vscode.CompletionItem('game');
					gameCompletion.kind = vscode.CompletionItemKind.Variable;
					gameCompletion.detail = 'Game context object';
					return [gameCompletion];
				}

				if (linePrefix.trim().endsWith('game.')) {
					try {
						return await fetchLanguageConfig();
					} catch (error) {
						console.error('Error fetching completions:', error);
						return [];
					}
				}

				return undefined;
			}
		},
		'.' // Trigger auf Punkt
	);

    context.subscriptions.push(commandDisposable);
    context.subscriptions.push(completionProvider);
}
