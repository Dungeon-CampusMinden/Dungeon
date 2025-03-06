import * as vscode from 'vscode';
import axios from 'axios';

export default async function fetchLanguageConfig(): Promise<vscode.CompletionItem[]> {
    const config = vscode.workspace.getConfiguration('blocklyServer');
    const url: string = config.get('url', 'http://localhost:8080') + '/language';

    console.log(`Requesting language config from: ${url}`);

    try {
        const response = await axios.get(url, { timeout: 5000 }); // Add timeout
        console.log('Server responded with status:', response.status);

        if (!Array.isArray(response.data)) {
            console.error('Invalid response format:', response.data);
            throw new Error("Invalid language response format");
        }

        console.log(`Processing ${response.data.length} language items`);
        return response.data.map((entry: any) => {
            const item = new vscode.CompletionItem(entry.label, vscode.CompletionItemKind.Function);
            item.detail = entry.description;
            item.insertText = new vscode.SnippetString(entry.snippet || entry.label + "()");
            return item;
        });
    } catch (error: any) {
        console.error('Error fetching language config:', error.message);
        vscode.window.showErrorMessage("Failed to load Blockly language: " + error.message);
        return [];
    }
}
