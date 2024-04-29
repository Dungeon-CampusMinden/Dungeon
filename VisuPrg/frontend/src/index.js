import * as Blockly from 'blockly';
import {javascriptGenerator} from 'blockly/javascript';
import {LexicalVariablesPlugin} from '@mit-app-inventor/blockly-block-lexical-variables';
import {toolbox} from "./toolbox";
import varDeclPlugin from './varDeclPlugin';
import '../styles/style.css';


// Inject Blockly into the given div
const blocklyDiv = document.getElementById("blocklyDiv");
const workspace =
    Blockly.inject(blocklyDiv, {
        trashcan: true,
        toolbox: toolbox,
        media: 'media/'
    });

// Adding call of highlightBlock in front of each generated blockly-block
javascriptGenerator.STATEMENT_PREFIX = 'highlightBlock(%1);\n';
javascriptGenerator.addReservedWords('highlightBlock');
javascriptGenerator.addReservedWords('code');

// Init of lexical/local Variable Plugin
LexicalVariablesPlugin.init(workspace);

// Waits for a websocket connection
function waitForSocketConnection(socket, callback){
    setTimeout(
        function () {
            if (socket.readyState === 1){
                console.log("Connection is made");
                if (callback != null){
                    callback();
                }
            } else {
                console.log("waiting for connection...")
                waitForSocketConnection(socket, callback);
            }
        }, 5);
}

// Highlights a block with given id
function highlightBlock(id) {
    workspace.highlightBlock(id);
    highlightPause = true;
}

var websocket;

// Initializes the API for the JS-Interpreter
function initApi(interpreter, globalObject) {
    // Add an API function for highlighting blocks.
    const wrapperHighlight = function (id) {
        id = String(id || '');
        return highlightBlock(id);
    };
    interpreter.setProperty(
        globalObject,
        'highlightBlock',
        interpreter.createNativeFunction(wrapperHighlight)
    );

    // Add an API function for connecting to the websocket endpoint of the server
    const wrapperWebsocketConnect = function (){
        websocket = new WebSocket("ws://localhost:8080/ws");
    }
    interpreter.setProperty(
        globalObject,
        'websocketConnect',
        interpreter.createNativeFunction(wrapperWebsocketConnect)
    );

    // Add an API function for sending a message over websocket
    const wrapperWebsocketSend = function (message) {
        waitForSocketConnection(websocket, function(){
            websocket.send(message);
            console.log('Send Message: ' + message);
        });

    }
    interpreter.setProperty(
        globalObject,
        'websocketSend',
        interpreter.createNativeFunction(wrapperWebsocketSend)
    );
}

var code;
var interpreter;
// Each step will run the interpreter until the highlightPause is true
let highlightPause = false;

// Stops the execution and resets the interpreter
function resetStepUi() {
    workspace.highlightBlock(null);
    highlightPause = false;

    interpreter = null;
}

// Generating JavaScript code and manipulate it for visualization
if (loadBtn) {
    loadBtn.addEventListener("click", async () => {
        // Generating JavaScript-Code
        code = javascriptGenerator.workspaceToCode(workspace);
        window.alert(code);

        // Transforming the code to ES2015
        code = 'websocketConnect();\n' + Babel.transform(code, {
            presets: ['es2015'].reverse()
        }).code;
        console.log(code);

        // Calling babel with given plugin for the addition of visu commands
        code = Babel.transform(code, {
            plugins: [varDeclPlugin]
        }).code;
        window.alert(code);

        // Initializing interpreter
        interpreter = new Interpreter(code, initApi);
    });
}

// Stepping one Blockly-Block at a time and interpreting the given code
if (stepBtn){
    stepBtn.addEventListener("click", async () => {
        highlightPause = false;
        let hasMoreCode;
        do {
            try {
                hasMoreCode = interpreter.step();
            } finally {
                if (!hasMoreCode) {
                    // Program complete, no more code to execute.
                    resetStepUi();

                    // Cool down, to discourage accidentally restarting the program.
                    stepBtn.disabled = 'disabled';
                    setTimeout(function () {
                        stepBtn.disabled = '';
                    }, 2000);

                    return;
                }
            }
            // Keep executing until a highlight statement is reached,
            // or the code completes or errors.
        } while (hasMoreCode && !highlightPause);
    });
}

// Resetting interpretation and ui
if (resetBtn) {
    resetBtn.addEventListener("click", async () => {
        resetStepUi();
    });
}




