import * as vscode from 'vscode';
import axios from 'axios';

export default async function sendBlocklyFile() {
    const editor = vscode.window.activeTextEditor;
    if (!editor) {
        vscode.window.showErrorMessage('No active editor detected!');
        return;
    }

    if (editor.document.languageId !== 'java') {
        vscode.window.showErrorMessage('Please open a Java file!');
        return;
    }

    const config = vscode.workspace.getConfiguration('blocklyServer');
    const url = config.get('url', 'http://localhost:8080') + '/code';
    const code: string = editor.document.getText();

    try {
        await axios.post(url, code, { headers: { 'Content-Type': 'text/plain' } });
        vscode.window.showInformationMessage('Blockly file sent successfully!');
    } catch (error: any) {
        if (error.response.status === 400) {
            vscode.window.showErrorMessage(error.response.data);
        } else {
            vscode.window.showErrorMessage('Failed to send Java file: ' + error.message);
        }
    }
}