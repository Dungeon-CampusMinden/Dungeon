grammar DSL;

@header{
package newdsl.antlr;
}

start: (import_statement | task | task_composition | task_config)* EOF;

//==========================================================
// Parser
//==========================================================

// Parser Task =============================================
task: task_header task_content;
task_content:
    task_body #taskBody
    | task_variant_body #taskVariantBody
;

task_header: TASK_KEYWORD type=(MULTIPLE_CHOICE_KEYWORD | SINGLE_CHOICE_KEYWORD | FILL_IN_THE_BLANK_KEYWORD | MATCHING_KEYWORD | CRAFTING_KEYWORD | CALCULATION_KEYWORD) id=ID ':';
task_body: desc=TEXT task_answers optional_task_content_list; // Anworten immer unter dem Aufgabentext, dann folgt der optionale Teil
task_answers: answer_list | answer_selection_expression;

answer_list: answer+;
answer:
    choice_answer #choiceAnswer
    | matching_answer #matchingAnswer
    | parameter_answer #parameterAnswer
    | crafting_answer #craftingAnswer
;

// Parser Task Variant =====================================
task_variant_body: task_variant_list USING_KEYWORD ':' optional_task_content_list;
task_variant_list: task_variant+;
task_variant: VARIANT_KEYWORD id=ID ':' task_body;

// Parser Task Crafting ====================================
crafting_answer:
    crafting_ingredient #initialIngredient
    | crafting_rule #rule
    | crafting_solution #solution
;
crafting_ingredient_list: crafting_ingredient ('+' crafting_ingredient)*;
crafting_ingredient: amount=NUMBER? alias_text;
crafting_rule: crafting_ingredient_list strictness=(UNORDERED|ORDERED) crafting_ingredient_list;
crafting_solution: RESULT crafting_ingredient;

// Parser Task Choice ======================================
choice_answer: prefix=(CORRECT | FALSE) answer_text=TEXT;

// Parser Task Matching ====================================
matching_answer:
    left=BLANK MATCH right=alias_text #leftBlank
    | left=alias_text MATCH right=alias_text #bothText
    | left=alias_text MATCH right=BLANK #rightBlank
;

// Parser Task Parameter ===================================
parameter_answer: parameter=CODE value=TEXT;

// Parser Task Optional Content ============================
optional_task_content_list: optional_task_content*;
optional_task_content:
    EXPLANATION_KEYWORD text=TEXT #explain
    | type=(GRADE_KEYWORD | PASS_KEYWORD | SCENARIO_KEYWORD | SOLUTION_KEYWORD) ':' code=CODE #customCode
;

answer_selection_expression:
    answer_selection_expression OR_KEYWORD answer_selection_term #or
    | answer_selection_term #singleTerm
;

answer_selection_term:
    answer_selection_term AND_KEYWORD answer_selection_factor #and
    | answer_selection_factor #singleFactor
;

answer_selection_factor:
    '(' answer_selection_expression ')' #parenthesis
    | SELECT_KEYWORD amount=NUMBER FROM_KEYWORD '{' answer_list '}' #factor
;

// Parser Task Composition =================================
task_composition: TASK_KEYWORD id=ID ':' list=task_composition_content_list;
task_composition_content_list: task_composition_content (',' task_composition_content)*;
task_composition_content:
    id=ID #requiredTask
    | '(' id=ID ')' #optionalTask
;

// Parser Task Sequence ====================================
task_config: CONFIG_KEYWORD config_id=ID ':' config=task_config_content;
task_config_content: id=ID (ORDERED config_branch=task_config_branch)? (ORDERED config_content=task_config_content)?;

task_config_branch:
'{'
    CORRECT ORDERED correctBranch=task_config_content
    FALSE ORDERED falseBranch=task_config_content
'}';

// Parser Import ===========================================
import_statement:
    IMPORT_KEYWORD path=TEXT
;

// Parser Answer ===========================================
alias_text:
    text=TEXT #textAnswer
    | text=TEXT AS_KEYWORD alias=ID #textAliasAnswer
    | alias=ID #aliasAnswer
;

//==========================================================
// Lexer
//==========================================================

// Lexer Keywords ==========================================
IMPORT_KEYWORD: 'import';
TASK_KEYWORD: 'task';
VARIANT_KEYWORD: 'variant';
USING_KEYWORD: 'using';
GRADE_KEYWORD: 'points';
PASS_KEYWORD: 'pass';
SCENARIO_KEYWORD: 'scenario';
SOLUTION_KEYWORD: 'solution';
EXPLANATION_KEYWORD: '(?)';
SELECT_KEYWORD: 'select';
FROM_KEYWORD: 'from';
AND_KEYWORD: 'and';
OR_KEYWORD: 'or';
AS_KEYWORD: 'as';
CONFIG_KEYWORD: 'config';
MULTIPLE_CHOICE_KEYWORD: 'multiple-choice' | 'multiple';
SINGLE_CHOICE_KEYWORD: 'single-choice' | 'single';
FILL_IN_THE_BLANK_KEYWORD: 'fill-in-the-blank' | 'blank';
MATCHING_KEYWORD: 'matching' | 'match';
CRAFTING_KEYWORD: 'crafting' | 'craft';
CALCULATION_KEYWORD: 'calculation' | 'calc';

// Lexer Answers ===========================================
BLANK: '_';
MATCH: '<->';
ORDERED: '->';
UNORDERED: '~>';
RESULT: '=>';
CORRECT: '[' 'X' ']';
FALSE: '[' ' '? ']';

// Lexer Misc ==============================================
ID: [a-zA-Z][a-zA-Z0-9_]*; // Starten mit einem Buchstaben -> Soll sich am Sprachgebrauch orientieren
NUMBER: [0-9]+;
TEXT: '"' (~'"'|'/"')* '"' {setText(String.valueOf(getText().substring(1, getText().length() - 1)));} ;
CODE: '<<' (~'<'|'>')* '>>' {setText(String.valueOf(getText().substring(2, getText().length() - 2)));} ;
WHITESPACE: (' ' | '\t' | '\r' | '\n') -> skip;
COMMENT: '#' ~[\r\n]* -> skip;
