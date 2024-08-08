
import * as path from 'path';
import {
	ExtensionContext,
	OutputChannel,
	window,
} from 'vscode';

import {
	LanguageClient,
	LanguageClientOptions,
	State,
	StreamInfo,
	integer,
	ServerOptions,
	TransportKind,
} from 'vscode-languageclient/node';
import * as net from 'net';
import { ChildProcess, spawn } from 'node:child_process';

let client: LanguageClient;

function createServerOptions(context: ExtensionContext, outputChannel: OutputChannel) {
	const pathToJavaExe = path.join(String(process.env.JAVA_HOME), "bin", "java");
	outputChannel.appendLine("pathToJavaExe: " + pathToJavaExe);

	const pathToFolderOfServerJar = context.asAbsolutePath(path.join('.', '*'));
	outputChannel.appendLine("pathToFolderOfServerJar: " + pathToFolderOfServerJar);

	const qualifiedMainClass = "lsp.DslLanguageServerLauncher";

	const args: string[] = ["-cp", pathToFolderOfServerJar, qualifiedMainClass];
	const isDebug: boolean = process.env.LSDEBUG === "true";
	if (isDebug) {
		args.unshift('-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005');
		args.push('debug');
	}

	const stdioServerOptions: ServerOptions = {
		command: pathToJavaExe,
		transport: TransportKind.stdio,
		args: args,
		options: {}
	};

	const tcpServerOptions = () =>
		new Promise<ChildProcess | StreamInfo>((resolve, _) => {
			const server = net.createServer(socket => {
				outputChannel.appendLine('Language Server Process Disconnected');
				socket.on('end', () => {
					outputChannel.appendLine('Language Server Process Disconnected');
				});
				server.close();
				resolve({ reader: socket, writer: socket });
			});
			server.listen(9925, '127.0.0.1', () => {
				const childProcess = spawn(pathToJavaExe, [...args, qualifiedMainClass]);
				childProcess.stderr.on('data', (chunk: Buffer) => {
					const str = chunk.toString();
					outputChannel.appendLine('Language Server:' + str);
				});
				childProcess.stdout.on('data', (chunk: Buffer) => {
					outputChannel.appendLine('Language Server:' + chunk);
				});
				childProcess.on('exit', (code: integer, signal: integer) => {
					outputChannel.appendLine(
						`Language server exited ` + (signal ? `from signal ${signal}` : `with exit code ${code}`)
					);
					if (code !== 0) {
						outputChannel.show();
					}
				});
				return childProcess;
			});
		});

	return isDebug ? tcpServerOptions : stdioServerOptions;
}

export function activate(context: ExtensionContext) {
	const outputChannel = window.createOutputChannel("Dungeon-DSL Extension");

	const clientOptions: LanguageClientOptions = {
		// Register the server for dungeon-dsl language defined in package.json
		documentSelector: [{ scheme: 'file', language: 'dungeon-dsl' }],
		outputChannel: outputChannel,
	};

	client = new LanguageClient(
		'dungeon-dsl-extension',
		'Dungeon DSL Extension',
		createServerOptions(context, outputChannel),
		clientOptions
	);

	const disposeDidChange = client.onDidChangeState(
		(stateChangeEvent) => {
			if (stateChangeEvent.newState === State.Stopped) {
				window.showErrorMessage(
					"Failed to initialize the extension"
				);
			} else if (stateChangeEvent.newState === State.Running) {
				window.showInformationMessage(
					"Extension initialized successfully!"
				);
			}
		}
	);

	// Start the client. This will also launch the server
	client.start().then(() => {
		disposeDidChange.dispose();
	});
}

export function deactivate(): Thenable<void> | undefined {
	if (!client) {
		return undefined;
	}
	return client.stop();
}