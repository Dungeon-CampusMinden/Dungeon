

// Create the definition.
const definitions = Blockly.common.createBlockDefinitionsFromJsonArray([
    {
        // The type is like the "class name" for your block. It is used to construct
        // new instances. E.g. in the toolbox.
        type: 'my_custom_block',
        // The message defines the basic text of your block, and where inputs or
        // fields will be inserted.
        message0: 'move forward %1',
        args0: [
            // Each arg is associated with a %# in the message.
            // This one gets substituted for %1.
            {
                // The type specifies the kind of input or field to be inserted.
                type: 'field_number',
                // The name allows you to reference the field and get its value.
                name: 'FIELD_NAME',
            }
        ],
        // Adds an untyped previous connection to the top of the block.
        previousStatement: null,
        // Adds an untyped next connection to the bottom of the block.
        nextStatement: null,
    }
]);

const __default =  {
    kind: 'categoryToolbox',
    contents: [
        {
            kind: 'category',
            name: 'Logic',
            categorystyle: 'logic_category',
            contents: [
                {
                    type: 'controls_if',
                    kind: 'block',
                },
                {
                    type: 'logic_compare',
                    kind: 'block',
                    fields: {
                        OP: 'EQ',
                    },
                },
                {
                    type: 'logic_operation',
                    kind: 'block',
                    fields: {
                        OP: 'AND',
                    },
                },
                {
                    type: 'logic_negate',
                    kind: 'block',
                },
                {
                    type: 'logic_boolean',
                    kind: 'block',
                    fields: {
                        BOOL: 'TRUE',
                    },
                },
                {
                    type: 'logic_null',
                    kind: 'block',
                    enabled: false,
                },
                {
                    type: 'logic_ternary',
                    kind: 'block',
                },
            ],
        },
        {
            kind: 'category',
            name: 'Loops',
            categorystyle: 'loop_category',
            contents: [
                {
                    type: 'controls_repeat_ext',
                    kind: 'block',
                    inputs: {
                        TIMES: {
                            shadow: {
                                type: 'math_number',
                                fields: {
                                    NUM: 10,
                                },
                            },
                        },
                    },
                },
                {
                    type: 'controls_repeat',
                    kind: 'block',
                    enabled: false,
                    fields: {
                        TIMES: 10,
                    },
                },
                {
                    type: 'controls_whileUntil',
                    kind: 'block',
                    fields: {
                        MODE: 'WHILE',
                    },
                },
                {
                    type: 'controls_for',
                    kind: 'block',
                    fields: {
                        VAR: {
                            name: 'i',
                        },
                    },
                    inputs: {
                        FROM: {
                            shadow: {
                                type: 'math_number',
                                fields: {
                                    NUM: 1,
                                },
                            },
                        },
                        TO: {
                            shadow: {
                                type: 'math_number',
                                fields: {
                                    NUM: 10,
                                },
                            },
                        },
                        BY: {
                            shadow: {
                                type: 'math_number',
                                fields: {
                                    NUM: 1,
                                },
                            },
                        },
                    },
                },
                {
                    type: 'controls_forEach',
                    kind: 'block',
                    fields: {
                        VAR: {
                            name: 'j',
                        },
                    },
                },
                {
                    type: 'controls_flow_statements',
                    kind: 'block',
                    enabled: false,
                    fields: {
                        FLOW: 'BREAK',
                    },
                },
            ],
        },
        {
            kind: 'category',
            name: 'Math',
            categorystyle: 'math_category',
            contents: [
                {
                    type: 'math_number',
                    kind: 'block',
                    fields: {
                        NUM: 123,
                    },
                },
                {
                    type: 'math_arithmetic',
                    kind: 'block',
                    fields: {
                        OP: 'ADD',
                    },
                    inputs: {
                        A: {
                            shadow: {
                                type: 'math_number',
                                fields: {
                                    NUM: 1,
                                },
                            },
                        },
                        B: {
                            shadow: {
                                type: 'math_number',
                                fields: {
                                    NUM: 1,
                                },
                            },
                        },
                    },
                },
                {
                    type: 'math_single',
                    kind: 'block',
                    fields: {
                        OP: 'ROOT',
                    },
                    inputs: {
                        NUM: {
                            shadow: {
                                type: 'math_number',
                                fields: {
                                    NUM: 9,
                                },
                            },
                        },
                    },
                },
                {
                    type: 'math_trig',
                    kind: 'block',
                    fields: {
                        OP: 'SIN',
                    },
                    inputs: {
                        NUM: {
                            shadow: {
                                type: 'math_number',
                                fields: {
                                    NUM: 45,
                                },
                            },
                        },
                    },
                },
                {
                    type: 'math_constant',
                    kind: 'block',
                    fields: {
                        CONSTANT: 'PI',
                    },
                },
                {
                    type: 'math_number_property',
                    kind: 'block',
                    fields: {
                        PROPERTY: 'EVEN',
                    },
                    inputs: {
                        NUMBER_TO_CHECK: {
                            shadow: {
                                type: 'math_number',
                                fields: {
                                    NUM: 0,
                                },
                            },
                        },
                    },
                },
                {
                    type: 'math_round',
                    kind: 'block',
                    fields: {
                        OP: 'ROUND',
                    },
                    inputs: {
                        NUM: {
                            shadow: {
                                type: 'math_number',
                                fields: {
                                    NUM: 3.1,
                                },
                            },
                        },
                    },
                },
                {
                    type: 'math_on_list',
                    kind: 'block',
                    fields: {
                        OP: 'SUM',
                    },
                },
                {
                    type: 'math_modulo',
                    kind: 'block',
                    inputs: {
                        DIVIDEND: {
                            shadow: {
                                type: 'math_number',
                                fields: {
                                    NUM: 64,
                                },
                            },
                        },
                        DIVISOR: {
                            shadow: {
                                type: 'math_number',
                                fields: {
                                    NUM: 10,
                                },
                            },
                        },
                    },
                },
                {
                    type: 'math_constrain',
                    kind: 'block',
                    inputs: {
                        VALUE: {
                            shadow: {
                                type: 'math_number',
                                fields: {
                                    NUM: 50,
                                },
                            },
                        },
                        LOW: {
                            shadow: {
                                type: 'math_number',
                                fields: {
                                    NUM: 1,
                                },
                            },
                        },
                        HIGH: {
                            shadow: {
                                type: 'math_number',
                                fields: {
                                    NUM: 100,
                                },
                            },
                        },
                    },
                },
                {
                    type: 'math_random_int',
                    kind: 'block',
                    inputs: {
                        FROM: {
                            shadow: {
                                type: 'math_number',
                                fields: {
                                    NUM: 1,
                                },
                            },
                        },
                        TO: {
                            shadow: {
                                type: 'math_number',
                                fields: {
                                    NUM: 100,
                                },
                            },
                        },
                    },
                },
                {
                    type: 'math_random_float',
                    kind: 'block',
                },
                {
                    type: 'math_atan2',
                    kind: 'block',
                    inputs: {
                        X: {
                            shadow: {
                                type: 'math_number',
                                fields: {
                                    NUM: 1,
                                },
                            },
                        },
                        Y: {
                            shadow: {
                                type: 'math_number',
                                fields: {
                                    NUM: 1,
                                },
                            },
                        },
                    },
                },
            ],
        },
        {
            kind: 'category',
            name: 'Text',
            categorystyle: 'text_category',
            contents: [
                {
                    type: 'text',
                    kind: 'block',
                    fields: {
                        TEXT: '',
                    },
                },
                {
                    type: 'text_multiline',
                    kind: 'block',
                    fields: {
                        TEXT: '',
                    },
                },
                {
                    type: 'text_join',
                    kind: 'block',
                },
                {
                    type: 'text_append',
                    kind: 'block',
                    fields: {
                        name: 'item',
                    },
                    inputs: {
                        TEXT: {
                            shadow: {
                                type: 'text',
                                fields: {
                                    TEXT: '',
                                },
                            },
                        },
                    },
                },
                {
                    type: 'text_length',
                    kind: 'block',
                    inputs: {
                        VALUE: {
                            shadow: {
                                type: 'text',
                                fields: {
                                    TEXT: 'abc',
                                },
                            },
                        },
                    },
                },
                {
                    type: 'text_isEmpty',
                    kind: 'block',
                    inputs: {
                        VALUE: {
                            shadow: {
                                type: 'text',
                                fields: {
                                    TEXT: '',
                                },
                            },
                        },
                    },
                },
                {
                    type: 'text_indexOf',
                    kind: 'block',
                    fields: {
                        END: 'FIRST',
                    },
                    inputs: {
                        VALUE: {
                            block: {
                                type: 'variables_get',
                                fields: {
                                    VAR: {
                                        name: 'text',
                                    },
                                },
                            },
                        },
                        FIND: {
                            shadow: {
                                type: 'text',
                                fields: {
                                    TEXT: 'abc',
                                },
                            },
                        },
                    },
                },
                {
                    type: 'text_charAt',
                    kind: 'block',
                    fields: {
                        WHERE: 'FROM_START',
                    },
                    inputs: {
                        VALUE: {
                            block: {
                                type: 'variables_get',
                                fields: {
                                    VAR: {
                                        name: 'text',
                                    },
                                },
                            },
                        },
                    },
                },
                {
                    type: 'text_getSubstring',
                    kind: 'block',
                    fields: {
                        WHERE1: 'FROM_START',
                        WHERE2: 'FROM_START',
                    },
                    inputs: {
                        STRING: {
                            block: {
                                type: 'variables_get',
                                fields: {
                                    VAR: {
                                        name: 'text',
                                    },
                                },
                            },
                        },
                    },
                },
                {
                    type: 'text_changeCase',
                    kind: 'block',
                    fields: {
                        CASE: 'UPPERCASE',
                    },
                    inputs: {
                        TEXT: {
                            shadow: {
                                type: 'text',
                                fields: {
                                    TEXT: 'abc',
                                },
                            },
                        },
                    },
                },
                {
                    type: 'text_trim',
                    kind: 'block',
                    fields: {
                        MODE: 'BOTH',
                    },
                    inputs: {
                        TEXT: {
                            shadow: {
                                type: 'text',
                                fields: {
                                    TEXT: 'abc',
                                },
                            },
                        },
                    },
                },
                {
                    type: 'text_count',
                    kind: 'block',
                    inputs: {
                        SUB: {
                            shadow: {
                                type: 'text',
                                fields: {
                                    TEXT: '',
                                },
                            },
                        },
                        TEXT: {
                            shadow: {
                                type: 'text',
                                fields: {
                                    TEXT: '',
                                },
                            },
                        },
                    },
                },
                {
                    type: 'text_replace',
                    kind: 'block',
                    inputs: {
                        FROM: {
                            shadow: {
                                type: 'text',
                                fields: {
                                    TEXT: '',
                                },
                            },
                        },
                        TO: {
                            shadow: {
                                type: 'text',
                                fields: {
                                    TEXT: '',
                                },
                            },
                        },
                        TEXT: {
                            shadow: {
                                type: 'text',
                                fields: {
                                    TEXT: '',
                                },
                            },
                        },
                    },
                },
                {
                    type: 'text_reverse',
                    kind: 'block',
                    inputs: {
                        TEXT: {
                            shadow: {
                                type: 'text',
                                fields: {
                                    TEXT: '',
                                },
                            },
                        },
                    },
                },

                {
                    type: 'text_print',
                    kind: 'block',
                    inputs: {
                        TEXT: {
                            shadow: {
                                type: 'text',
                                fields: {
                                    TEXT: 'abc',
                                },
                            },
                        },
                    },
                },
                {
                    type: 'text_prompt_ext',
                    kind: 'block',
                    fields: {
                        TYPE: 'TEXT',
                    },
                    inputs: {
                        TEXT: {
                            shadow: {
                                type: 'text',
                                fields: {
                                    TEXT: 'abc',
                                },
                            },
                        },
                    },
                },
            ],
        },
        {
            kind: 'category',
            name: 'Lists',
            categorystyle: 'list_category',
            contents: [
                {
                    type: 'lists_create_empty',
                    kind: 'block',
                },
                {
                    type: 'lists_create_with',
                    kind: 'block',
                },
                {
                    type: 'lists_repeat',
                    kind: 'block',
                    inputs: {
                        NUM: {
                            shadow: {
                                type: 'math_number',
                                fields: {
                                    NUM: 5,
                                },
                            },
                        },
                    },
                },
                {
                    type: 'lists_length',
                    kind: 'block',
                },
                {
                    type: 'lists_isEmpty',
                    kind: 'block',
                },
                {
                    type: 'lists_indexOf',
                    kind: 'block',

                    fields: {
                        END: 'FIRST',
                    },
                    inputs: {
                        VALUE: {
                            block: {
                                type: 'variables_get',
                                fields: {
                                    VAR: {
                                        name: 'list',
                                    },
                                },
                            },
                        },
                    },
                },
                {
                    type: 'lists_getIndex',
                    kind: 'block',
                    fields: {
                        MODE: 'GET',
                        WHERE: 'FROM_START',
                    },
                    inputs: {
                        VALUE: {
                            block: {
                                type: 'variables_get',
                                fields: {
                                    VAR: {
                                        name: 'list',
                                    },
                                },
                            },
                        },
                    },
                },
                {
                    type: 'lists_setIndex',
                    kind: 'block',
                    fields: {
                        MODE: 'SET',
                        WHERE: 'FROM_START',
                    },
                    inputs: {
                        LIST: {
                            block: {
                                type: 'variables_get',
                                fields: {
                                    VAR: {
                                        name: 'list',
                                    },
                                },
                            },
                        },
                    },
                },
                {
                    type: 'lists_getSublist',
                    kind: 'block',
                    fields: {
                        WHERE1: 'FROM_START',
                        WHERE2: 'FROM_START',
                    },
                    inputs: {
                        LIST: {
                            block: {
                                type: 'variables_get',
                                fields: {
                                    VAR: {
                                        name: 'list',
                                    },
                                },
                            },
                        },
                    },
                },
                {
                    type: 'lists_split',
                    kind: 'block',

                    fields: {
                        MODE: 'SPLIT',
                    },
                    inputs: {
                        DELIM: {
                            shadow: {
                                type: 'text',
                                fields: {
                                    TEXT: ',',
                                },
                            },
                        },
                    },
                },
                {
                    type: 'lists_sort',
                    kind: 'block',

                    fields: {
                        TYPE: 'NUMERIC',
                        DIRECTION: '1',
                    },
                },
                {
                    type: 'lists_reverse',
                    kind: 'block',
                },
            ],
        },
        {
            kind: 'category',
            categorystyle: 'colour_category',
            name: 'Colour',
            contents: [
                {
                    type: 'colour_picker',
                    kind: 'block',
                    fields: {
                        COLOUR: '#ff0000',
                    },
                },
                {
                    type: 'colour_random',
                    kind: 'block',
                },
                {
                    type: 'colour_rgb',
                    kind: 'block',
                    inputs: {
                        RED: {
                            shadow: {
                                type: 'math_number',
                                fields: {
                                    NUM: 100,
                                },
                            },
                        },
                        GREEN: {
                            shadow: {
                                type: 'math_number',
                                fields: {
                                    NUM: 50,
                                },
                            },
                        },
                        BLUE: {
                            shadow: {
                                type: 'math_number',
                                fields: {
                                    NUM: 0,
                                },
                            },
                        },
                    },
                },
                {
                    type: 'colour_blend',
                    kind: 'block',
                    inputs: {
                        COLOUR1: {
                            shadow: {
                                type: 'colour_picker',
                                fields: {
                                    COLOUR: '#ff0000',
                                },
                            },
                        },
                        COLOUR2: {
                            shadow: {
                                type: 'colour_picker',
                                fields: {
                                    COLOUR: '#3333ff',
                                },
                            },
                        },
                        RATIO: {
                            shadow: {
                                type: 'math_number',
                                fields: {
                                    NUM: 0.5,
                                },
                            },
                        },
                    },
                },
            ],
        },
        {
            kind: 'sep',
        },
        {
            kind: 'category',
            name: 'Variables',
            custom: 'VARIABLE',
            categorystyle: 'variable_category',
        },
        {
            kind: 'category',
            name: 'Functions',
            custom: 'PROCEDURE',
            categorystyle: 'procedure_category',
        },
    ],
};

const toolbox = {
    // There are two kinds of toolboxes. The simpler one is a flyout toolbox.
    kind: 'flyoutToolbox',
    // The contents is the blocks and other items that exist in your toolbox.
    contents: [
        {
            kind: 'block',
            type: 'controls_if'
        },
        {
            kind: 'block',
            type: 'controls_whileUntil'
        },
        {
            kind: 'block',
            type: 'logic_boolean'
        },
        {
            kind: 'block',
            type: 'math_number'
        },
        {
            kind: 'block',
            type: 'controls_for'
        },
        {
            kind: 'block',
            type: 'math_arithmetic'
        },
        {
            kind: 'block',
            type: 'my_custom_block'
        }
    ]
};

// Register the definition.
Blockly.common.defineBlocks(definitions);


// Overwrite finish function, to add websocket connection
const originalFinish = javascript.javascriptGenerator.finish;

// Todo - Only add line, when not empty
javascript.javascriptGenerator.finish = function (code) {
    var variables = javascript.javascriptGenerator.definitions_['variables'];
    return 'websocketConnect();\n'
        + 'websocketSend(\'' + variables.substring(0, variables.length - 1) + '\'); \n'
        + originalFinish.call(this,code);
}

// Inject Blockly into the given div
const blocklyDiv = document.getElementById("blocklyDiv");
const workspace =
    Blockly.inject(blocklyDiv, {
        trashcan: true,
        toolbox: __default
    });

var code;
var interpreter;
var websocket;

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

javascript.javascriptGenerator.STATEMENT_PREFIX = 'highlightBlock(%1);\n';
javascript.javascriptGenerator.addReservedWords('highlightBlock');

javascript.javascriptGenerator.addReservedWords('code');

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

    // Add an API function for connecting to Websocket
    const wrapperWebsocketConnect = function (){
        websocket = new WebSocket("ws://localhost:8080/ws");
    }
    interpreter.setProperty(
        globalObject,
        'websocketConnect',
        interpreter.createNativeFunction(wrapperWebsocketConnect)
    );

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

// Each step will run the interpreter until the highlightPause is true.
let highlightPause = false;

function highlightBlock(id) {
    workspace.highlightBlock(id);
    highlightPause = true;
}

function resetStepUi(clearOutput) {
    workspace.highlightBlock(null);
    highlightPause = false;

    interpreter = null;
}

if (loadBtn) {
    loadBtn.addEventListener("click", async () => {
        code = javascript.javascriptGenerator.workspaceToCode(workspace);
        window.alert(code);
        interpreter = new Interpreter(code, initApi);
    });
}

// Todo - cleanup
if (stepBtn){
    stepBtn.addEventListener("click", async () => {
        highlightPause = false;
        let hasMoreCode;
        do {
            try {
                hasMoreCode = interpreter.step();
                console.log(interpreter.value);
            } finally {
                if (!hasMoreCode) {
                    // Program complete, no more code to execute.
                    resetStepUi(false);

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

if (resetBtn) {
    resetBtn.addEventListener("click", async () => {
        resetStepUi(false);
    });
}




