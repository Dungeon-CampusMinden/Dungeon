import * as vscode from 'vscode';
import {CompletionItem} from 'vscode';
import axios, {AxiosError} from 'axios';

export default async function fetchLanguageConfig(): Promise<vscode.CompletionItem[]> {
    const config = vscode.workspace.getConfiguration('blocklyServer');
    const url: string = config.get('url', 'http://localhost:8080') + '/language';

    console.log(`Requesting language config from: ${url}`);

    try {
        const response = await axios.get(url, {timeout: 5000}); // Add timeout
        console.log('Server responded with status:', response.status);

        if (!Array.isArray(response.data)) {
            console.error('Invalid response format:', response.data);
            throw new Error("Invalid language response format");
        }

        return response.data.map((entry: CompletionItem) => {
            const item = new vscode.CompletionItem(entry.label, vscode.CompletionItemKind.Function);
            item.detail = entry.detail;
            item.insertText = new vscode.SnippetString(entry.insertText as string || entry.label + "()");
            return item;
        });
    } catch (error: unknown) {
        if (!(error instanceof AxiosError))
            throw error; // rethrow if not an AxiosError

        console.error('Error fetching language config:', error.message);
        vscode.window.showErrorMessage("Failed to load Blockly language: " + error.message);
        return [];
    }
}
