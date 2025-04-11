import * as vscode from 'vscode';
import { fetchLanguageConfig, BlocklyCompletionItem } from './handlers/languageProvider';
import sendBlocklyFile, {stopBlocklyExecution} from './handlers/sendBlocklyFile';

export const BLOCKLY_URL = () => vscode.workspace.getConfiguration('blocklyServer').get('url', 'http://localhost:8080');
const codeObjects = ['hero', 'level'];

// Store API data for reuse
let cachedApiData: BlocklyCompletionItem[] = [];

// Function to get API data - fetches it if needed
async function getApiData(): Promise<BlocklyCompletionItem[]> {
    if (cachedApiData.length === 0) {
        for (const object of codeObjects) {
            try {
                const result = await fetchLanguageConfig(object);
                cachedApiData = result.rawItems;
            } catch (ignored) {}
        }
    }
    return cachedApiData;
}

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
                    const heroComp = new vscode.CompletionItem('hero');
                    heroComp.kind = vscode.CompletionItemKind.Variable;
                    heroComp.detail = 'The hero Character that you control';
                    heroComp.insertText = 'hero';
                    const levelComp = new vscode.CompletionItem('level');
                    levelComp.kind = vscode.CompletionItemKind.Variable;
                    levelComp.detail = 'The current level of the game';
                    levelComp.insertText = 'level';
                    return [heroComp, levelComp];
                }

                // check if the line endswith codeObjects + '.'
                if (codeObjects.some(object => linePrefix.trim().endsWith(object + '.'))) {
                    const objectToFetch = codeObjects.find(object => linePrefix.trim().endsWith(object + '.'));
                    if (objectToFetch) {
                        try {
                            const result = await fetchLanguageConfig(objectToFetch);
                            cachedApiData = result.rawItems; // Update cached data
                            return result.completionItems;
                        } catch (error) {
                            return [];
                        }
                    }
                }

                return undefined;
            }
        },
        '.' // Trigger on dot
    );
    
    // Register the hover provider
    const hoverProvider = vscode.languages.registerHoverProvider(
        { scheme: 'file', language: 'java' },
        {
            async provideHover(document: vscode.TextDocument, position: vscode.Position) {
                const wordRange = document.getWordRangeAtPosition(position);
                if (!wordRange) {
                    return null;
                }
                
                const word = document.getText(wordRange);
                
                const apiData = await getApiData();
                const apiItem = apiData.find(item => item.label === word);
                console.log('API Item:', apiItem);
                
                if (apiItem?.documentation) {
                    const md = new vscode.MarkdownString();
                    md.supportHtml = true;
                    md.isTrusted = true;
                    
                    // Append the method signature as a code block
                    if (apiItem.detail) {
                        md.appendCodeblock(apiItem.detail, 'java');
                        md.appendMarkdown('\n\n');
                    }
                    
                    // Extract and append the documentation text (without any potential method signature part)
                    let docText = apiItem.documentation.replace(/\\n/g, '\n');
                    
                    // If the documentation starts with the method name, it might include the signature
                    // We want to skip that part since we've already added it as a code block
                    if (docText.startsWith(apiItem.label)) {
                        const endOfFirstLine = docText.indexOf('\n');
                        if (endOfFirstLine !== -1) {
                            docText = docText.substring(endOfFirstLine + 1).trim();
                        }
                    }
                    
                    md.appendMarkdown(docText);
                    
                    return new vscode.Hover(md, wordRange);
                }
                
                return null;
            }
        }
    );

    context.subscriptions.push(runCommandDisposable);
    context.subscriptions.push(stopCommandDisposable);
    context.subscriptions.push(completionProvider);
    context.subscriptions.push(hoverProvider);
}
