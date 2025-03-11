import * as vscode from 'vscode';
import fetchLanguageConfig from './handlers/languageProvider';
import sendBlocklyFile, {stopBlocklyExecution} from './handlers/sendBlocklyFile';

export const BLOCKLY_URL = () => vscode.workspace.getConfiguration('blocklyServer').get('url', 'http://localhost:8080');

export function activate(context: vscode.ExtensionContext) {
    // Create diagnostic collection
    const diagnosticCollection = vscode.languages.createDiagnosticCollection('blockly');
    context.subscriptions.push(diagnosticCollection);

    // Register the command
    const runCommandDisposable = vscode.commands.registerCommand('blockly-code-runner.sendBlocklyFile', () => sendBlocklyFile());

    // Register stop command
    const stopCommandDisposable = vscode.commands.registerCommand('blockly-code-runner.stopBlocklyExecution', () => stopBlocklyExecution());

    // Register the completion provider for multiple languages/extensions
    const completionProvider = vscode.languages.registerCompletionItemProvider(
        {scheme: 'file', language: 'java'},
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
        '.' // Trigger on dot
    );

    context.subscriptions.push(runCommandDisposable);
    context.subscriptions.push(stopCommandDisposable);
    context.subscriptions.push(completionProvider);
}
