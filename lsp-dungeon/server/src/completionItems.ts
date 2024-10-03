import { CompletionItem, CompletionItemKind, Range, TextEdit } from 'vscode-languageserver';
// Datei zum auslagern der Snippets

export interface SuggestionData {
    label: string;
    documentation: string;
    snippet: string; 
}

export const suggestionDefinitions: SuggestionData[] = [
    {
        label: 'single-choice-task',
        documentation: 'Einfügen eines single-choice-tasks',
        snippet: `single_choice_task \${1:name} {
    description: "\${2:description}",
    answers: [
    "\${3:answer}",
    "\${4:answer}",
    "\${5:answer}"
    ],
    correct_answer_index: \${6|1,2,3|}
}`
    },
    {
        label: 'multiple-choice-task',
        documentation: 'Einfügen eines multiple-choice-tasks',
        snippet: `multiple_choice_task \${1:name} {
    description: "\${2:description}",
    answers: [
    "\${3:answer}",
    "\${4:answer}",
    "\${5:answer}"
    ],
    correct_answer_indices: [\${6|1,2,3|},\${7|1,2,3|}],
    explanation: "\${8:yourExplanation}"
}`
    },
    {
        label: 'for-loop',
        documentation: 'Einfügen einer for-Schleife',
        snippet: `for \${1:entry_type} \${2:entry} in \${3:my_list} {
    \${4://TODO: Implement logic}
}`
    },
    {
        label: 'while-loop',
        documentation: 'Einfügen einer while-Schleife',
        snippet: `while \${1:condition} {
  \${2://TODO: Implement logic}
}`
    },
    {
        label: 'function',
        documentation: 'Einfügen einer Funktion',
        snippet: `fn \${1:function_name}(\${2:possible_params}) {
  \${3://TODO: Implement logic}
}`
    },
    {
        label: 'function',
        documentation: 'Einfügen einer Funktion',
        snippet: `fn \${1:function_name}(\${2:possible_params}) {
  \${3://TODO: Implement logic}
}`
    },
    {
        label: 'if',
        documentation: 'If Statement',
        snippet: `if(\${1:condition}) {
        
}`
    },
    {
        label: 'ifelse',
        documentation: 'If-Else Statement',
        snippet: `if(\${1:condition}) {
        
} else {
     
}`
    }

    // Weitere Suggestionen hier hinzufügen
];

export function createCompletionItems(range: Range): CompletionItem[] {
    return suggestionDefinitions.map((suggestion: SuggestionData) => {
        return {
            label: suggestion.label,
            kind: CompletionItemKind.Snippet,
            documentation: suggestion.documentation,
            textEdit: TextEdit.replace(range, suggestion.snippet),
            insertTextFormat: 2 // Dies gibt an, dass es sich um einen Snippet-Text handelt
        };
    });
}