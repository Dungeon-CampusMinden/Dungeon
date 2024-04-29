// toolbox with local variables
export var toolbox = {
    "kind": "categoryToolbox",
    "contents": [
        {
            "kind": "category",
            "name": "Logic",
            "colour": "210",
            "contents": [
                {
                    "kind": "block",
                    "type": "controls_if"
                },
                {
                    "kind": "block",
                    "type": "logic_compare"
                },
                {
                    "kind": "block",
                    "type": "logic_operation"
                },
                {
                    "kind": "block",
                    "type": "logic_negate"
                },
                {
                    "kind": "block",
                    "type": "logic_boolean"
                },
                {
                    "kind": "block",
                    "type": "logic_null"
                },
                {
                    "kind": "block",
                    "type": "logic_ternary"
                },
            ]
        },
        {
            "kind": "category",
            "name": "Loops",
            "colour": "120",
            "contents": [
                {
                    "kind": "block",
                    "type": "controls_repeat_ext",
                    "inputs": {
                        "TIMES": {
                            "shadow": {
                                "type": "math_number",
                                "fields": {
                                    "NUM": 10
                                }
                            }
                        }
                    }
                },
                {
                    "kind": "block",
                    "type": "controls_whileUntil"
                },
                {
                    "kind": "block",
                    "type": "controls_for",
                    "inputs": {
                        "START": {
                            "shadow": {
                                "type": "math_number",
                                "fields": {
                                    "NUM": 1
                                }
                            }
                        },
                        "END": {
                            "shadow": {
                                "type": "math_number",
                                "fields": {
                                    "NUM": 10
                                }
                            }
                        },
                        "STEP": {
                            "shadow": {
                                "type": "math_number",
                                "fields": {
                                    "NUM": 1
                                }
                            }
                        }
                    }
                },
                {
                    "kind": "block",
                    "type": "controls_forEach"
                },
                {
                    "kind": "block",
                    "type": "controls_flow_statements"
                }
            ]
        },
        {
            "kind": "category",
            "name": "Math",
            "colour": "230",
            "contents": [
                {
                    "kind": "block",
                    "type": "math_number"
                },
                {
                    "kind": "block",
                    "type": "math_arithmetic",
                    "inputs": {
                        "A": {
                            "shadow": {
                                "type": "math_number",
                                "fields": {
                                    "NUM": 1
                                }
                            }
                        },
                        "B": {
                            "shadow": {
                                "type": "math_number",
                                "fields": {
                                    "NUM": 10
                                }
                            }
                        }
                    }
                },
                {
                    "kind": "block",
                    "type": "math_single",
                    "inputs": {
                        "NUM": {
                            "shadow": {
                                "type": "math_number",
                                "fields": {
                                    "NUM": 9
                                }
                            }
                        }
                    }
                },
                {
                    "kind": "block",
                    "type": "math_trig",
                    "inputs": {
                        "NUM": {
                            "shadow": {
                                "type": "math_number",
                                "fields": {
                                    "NUM": 45
                                }
                            }
                        }
                    }
                },
                {
                    "kind": "block",
                    "type": "math_constant"
                },
                {
                    "kind": "block",
                    "type": "math_number_property",
                    "inputs": {
                        "NUMBER_TO_CHECK": {
                            "shadow": {
                                "type": "math_number",
                                "fields": {
                                    "NUM": 0
                                }
                            }
                        }
                    }
                },
                {
                    "kind": "block",
                    "type": "math_round",
                    "inputs": {
                        "NUM": {
                            "shadow": {
                                "type": "math_number",
                                "fields": {
                                    "NUM": 3.1
                                }
                            }
                        }
                    }
                },
                {
                    "kind": "block",
                    "type": "math_on_list"
                },
                {
                    "kind": "block",
                    "type": "math_modulo",
                    "inputs": {
                        "DIVIDEND": {
                            "shadow": {
                                "type": "math_number",
                                "fields": {
                                    "NUM": 64
                                }
                            }
                        },
                        "DIVISOR": {
                            "shadow": {
                                "type": "math_number",
                                "fields": {
                                    "NUM": 10
                                }
                            }
                        }
                    }
                },
                {
                    "kind": "block",
                    "type": "math_constrain",
                    "inputs": {
                        "VALUE": {
                            "shadow": {
                                "type": "math_number",
                                "fields": {
                                    "NUM": 50
                                }
                            }
                        },
                        "LOW": {
                            "shadow": {
                                "type": "math_number",
                                "fields": {
                                    "NUM": 1
                                }
                            }
                        },
                        "HIGH": {
                            "shadow": {
                                "type": "math_number",
                                "fields": {
                                    "NUM": 100
                                }
                            }
                        }
                    }
                },
                {
                    "kind": "block",
                    "type": "math_random_int",
                    "inputs": {
                        "FROM": {
                            "shadow": {
                                "type": "math_number",
                                "fields": {
                                    "NUM": 1
                                }
                            }
                        },
                        "TO": {
                            "shadow": {
                                "type": "math_number",
                                "fields": {
                                    "NUM": 100
                                }
                            }
                        }
                    }
                },
                {
                    "kind": "block",
                    "type": "math_random_float"
                },
            ]
        },
        {
            "kind": "category",
            "name": "Lists",
            "colour": "260",
            "contents": [
                {
                    "kind": "block",
                    "type": "lists_create_with",
                    "extraState": {
                        "itemCount": 1
                    }
                },
                {
                    "kind": "block",
                    "type": "lists_create_with"
                },
                {
                    "kind": "block",
                    "type": "lists_repeat",
                    "inputs": {
                        "NUM": {
                            "shadow": {
                                "type": "math_number",
                                "fields": {
                                    "NUM": 5
                                }
                            }
                        }
                    }
                },
                {
                    "kind": "block",
                    "type": "lists_length"
                },
                {
                    "kind": "block",
                    "type": "lists_isEmpty"
                },
                {
                    "kind": "block",
                    "type": "lists_indexOf",
                    "inputs": {
                        "VALUE": {
                            "block": {
                                "type": "lexical_variable_get",
                                "field": "VAR"
                            }
                        }
                    }
                },
                {
                    "kind": "block",
                    "type": "lists_getIndex",
                    "inputs": {
                        "VALUE": {
                            "block": {
                                "type": "lexical_variable_get",
                                "field": "VAR"
                            }
                        }
                    }
                },
                {
                    "kind": "block",
                    "type": "lists_setIndex",
                    "inputs": {
                        "LIST": {
                            "block": {
                                "type": "lexical_variable_get",
                                "field": "VAR"
                            }
                        }
                    }
                },
                {
                    "kind": "block",
                    "type": "lists_getSublist",
                    "inputs": {
                        "LIST": {
                            "block": {
                                "type": "lexical_variable_get",
                                "field": "VAR"
                            }
                        }
                    }
                },
                {
                    "kind": "block",
                    "type": "lists_split",
                    "inputs": {
                        "DELIM": {
                            "shadow": {
                                "type": "text",
                                "fields": {
                                    "TEXT": ","
                                }
                            }
                        }
                    }
                },
                {
                    "kind": "block",
                    "type": "lists_sort"
                },
            ]
        },
        {
            "kind": "sep"
        },
        {
            "kind": "category",
            "name": "Variables",
            "colour": "330",
            "contents": [
                {
                    "kind": "block",
                    "type": "global_declaration"
                },
                {
                    "kind": "block",
                    "type": "local_declaration_statement"
                },
                {
                    "kind": "block",
                    "type": "lexical_variable_get"
                },
                {
                    "kind": "block",
                    "type": "lexical_variable_set"
                }
            ]
        },
        {
            "kind": "category",
            "name": "Functions",
            "colour": "290",
            "contents": [
                {
                    "kind": "block",
                    "type": "procedures_defnoreturn"
                },
                {
                    "kind": "block",
                    "type": "procedures_defreturn"
                },
                {
                    "kind": "block",
                    "type": "procedures_callnoreturn"
                },
                {
                    "kind": "block",
                    "type": "procedures_callreturn"
                }
            ]
        }
    ]
};
