grammar DungeonDiagnostics;



/*
 * Lexer rules
 */

DOUBLE_LINE : '--';
ARROW       : '->';

TRUE : 'true';
FALSE: 'false';
ID  : [_a-zA-Z][a-zA-Z0-9_]*;
NUM : ([0-9]|[1-9][0-9]*);
NUM_DEC: [0-9]+'.'[0-9]+;
WS  : [ \t\r\n]+ -> skip;


LINE_COMMENT
        : '//' ~[\r\n]* -> channel(HIDDEN)
        ;

BLOCK_COMMENT
        : '/*' .*? '*/' -> channel(HIDDEN)
        ;


STRING_LITERAL  : '\'' ( STRING_ESCAPE_SEQ | ~[\\\r\n\f'] )* '\''
                | '"' ( STRING_ESCAPE_SEQ | ~[\\\r\n\f"] )* '"'
                ;

/*
 * fragments
 */
fragment STRING_ESCAPE_SEQ
    : '\\' .
    ;

/*
 * Parser rules
 */
program : definition* EOF
        ;

definition
        : singleChoiceTask
        | multipleChoiceTask
        | assignTask
        ;

singleChoiceTask
    : 'single_choice_task' ID '{' field (',' field)* '}'
    ;

multipleChoiceTask
    : 'multiple_choice_task' ID '{' field (',' field)* '}'
    ;

assignTask
    : 'assign_task' ID '{' field (',' field)* '}'
    ;

field
    : descriptionField
    | answersField
    | correctAnswerIndexField
    | correctAnswerIndicesField
    | solutionField
    | pointsField
    | pointsToPassField
    | explanationField
    | gradingFunctionField
    | scenarioBuilderField
    ;

descriptionField
    : 'description' ':' STRING_LITERAL
    ;

answersField
    : 'answers' ':' '[' STRING_LITERAL (',' STRING_LITERAL)* ']'
    ;

correctAnswerIndexField
    : 'correct_answer_index' ':' NUM
    ;

correctAnswerIndicesField
    : 'correct_answer_indices' ':' '[' NUM (',' NUM)* ']'
    ;

solutionField
    : 'solution' ':' '<' pair (',' pair)* '>'
    ;

pair
    : '[' term ',' term ']'
    ;

term
    : STRING_LITERAL
    | '_'
    ;

pointsField
    : 'points' ':' NUM
    ;

pointsToPassField
    : 'points_to_pass' ':' NUM
    ;

explanationField
    : 'explanation' ':' STRING_LITERAL
    ;

gradingFunctionField
    : 'grading_function' ':' ID
    ;

scenarioBuilderField
    : 'scenario_builder' ':' ID
    ;
