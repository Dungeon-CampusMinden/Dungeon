import * as vscode from 'vscode';
import axios, {AxiosError} from 'axios';
import { BLOCKLY_URL } from '../extension';

export interface BlocklyCompletionItem {
    label: string;
    detail?: string;
    insertText?: string;
    parameters?: string[]; // Array of parameter names
    documentation?: string; // Optional documentation
}

// Function to fetch language config and return both raw data and completion items
export async function fetchLanguageConfig(objectToFetch: string): Promise<{
    rawItems: BlocklyCompletionItem[],
    completionItems: vscode.CompletionItem[]
}> {
    const url: string = BLOCKLY_URL() + '/language?object=' + objectToFetch;

    try {
        const response = await axios.get(url, {timeout: 5000}); // Add timeout

        if (!Array.isArray(response.data)) {
            throw new Error("Invalid language response format");
        }
        
        const rawItems: BlocklyCompletionItem[] = response.data;
        const completionItems = rawItems.map(createCompletionItem);

        return { rawItems, completionItems };
    } catch (error: unknown) {
        if (!(error instanceof AxiosError))
            throw error; // rethrow if not an AxiosError

        vscode.window.showErrorMessage("Failed to load Blockly language: " + error.message);
        return { rawItems: [], completionItems: [] };
    }
}

// Function to create a completion item from a raw item
function createCompletionItem(entry: BlocklyCompletionItem): vscode.CompletionItem {
    const item = new vscode.CompletionItem(entry.label, vscode.CompletionItemKind.Function);
    
    item.detail = entry.detail;
    
    // Create snippet with parameter placeholders if available
    if (entry.parameters && entry.parameters.length > 0) {
        const snippetParams = entry.parameters
            .map((param, index) => `\${${index + 1}:${param}}`)
            .join(', ');
        item.insertText = new vscode.SnippetString(`${entry.label}(${snippetParams})`);
    } else {
        item.insertText = entry.insertText 
            ? new vscode.SnippetString(entry.insertText)
            : new vscode.SnippetString(`${entry.label}()`);
    }
    
    // documentation
    if (entry.documentation) {
        const md = new vscode.MarkdownString();
        md.supportHtml = true;
        md.isTrusted = true;
        
        // Use the documentation directly as it's already formatted correctly
        md.appendMarkdown(entry.documentation.replace(/\\n/g, '\n'));
        
        item.documentation = md;
    }
    
    return item;
}
