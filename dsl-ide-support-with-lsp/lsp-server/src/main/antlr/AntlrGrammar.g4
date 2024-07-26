grammar AntlrGrammar;

start: task* EOF;

task
    : single_choice_task
    | multiple_choice_task
    | assign_task
    | graph
    | dungeon_config
    ;

single_choice_task: 'single_choice_task' id_definition '{' (single_choice_task_attribute ',')* single_choice_task_attribute? '}';

single_choice_task_attribute
    : 'correct_answer_index' ':' INT
    | shared_choice_tasks_attributes
    | shared_task_attribute
    ;

multiple_choice_task: 'multiple_choice_task' id_definition '{' (multiple_choice_task_attribute ',')* multiple_choice_task_attribute? '}';

multiple_choice_task_attribute
    : 'correct_answer_indices' ':' int_list
    | shared_choice_tasks_attributes
    | shared_task_attribute
    ;

shared_choice_tasks_attributes
    : 'answers' ':' string_list
    ;

assign_task: 'assign_task' id_definition '{' (assign_task_attribute ',')* assign_task_attribute? '}';

assign_task_attribute
    : 'solution' ':' set
    | shared_task_attribute
    ;

shared_task_attribute
    : 'description' ':' STRING
    | 'points' ':' decimal_or_int
    | 'points_to_pass' ':' decimal_or_int
    | 'explanation' ':' STRING
    | 'grading_function' ':' ID
    | 'scenario_builder' ':' ID
    ;

graph: 'graph' id_definition '{' task_dependency* '}';

task_dependency: id_usage ('->' id_usage)* '[type=' task_dependency_type '];';

task_dependency_type
    : ('seq'|'sequence')
    | ('st_m'|'subtask_mandatory')
    | ('st_o'|'subtask_optional')
    | ('c_c'|'conditional_correct')
    | ('c_f'|'conditional_false')
    | ('seq_and'|'sequence_and')
    | ('seq_or'|'sequence_or')
    ;

dungeon_config: 'dungeon_config' id_definition_only_used_by_dungeon_system '{' 'dependency_graph' ':' id_usage '}';

string_list: '[' (STRING ',')* STRING? ']';
int_list: '[' (INT ',')* INT? ']';

set: '<' (set_element ',')* set_element? '>';
set_element: '[' (STRING | '_') ',' (STRING | '_') ']';

decimal_or_int
    : INT
    | DECIMAL
    ;

id_definition: ID;
id_usage: ID;
id_definition_only_used_by_dungeon_system: ID;

STRING: '"' ~["]* '"';
ID: [_a-zA-Z][a-zA-Z0-9_]*;
INT : ([0-9]|[1-9][0-9]*);
DECIMAL: [0-9]+'.'[0-9]+;
WHIESPACE: [ \t\r\n]+ -> skip;

COMMENT_LINE: '//' ~[\r\n]* -> skip;
COMMENT_MULTI_LINE: '/*' ~[*/]* '*/' -> skip;
