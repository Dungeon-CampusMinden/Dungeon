// Generated from C:/Users/bjarn/VS_Projects/Dungeon/dungeon-diagnostics/DungeonDiagnostics.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class DungeonDiagnosticsParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		T__24=25, T__25=26, T__26=27, T__27=28, T__28=29, T__29=30, T__30=31, 
		T__31=32, T__32=33, T__33=34, T__34=35, T__35=36, T__36=37, T__37=38, 
		T__38=39, T__39=40, T__40=41, T__41=42, T__42=43, T__43=44, T__44=45, 
		T__45=46, T__46=47, T__47=48, T__48=49, T__49=50, T__50=51, T__51=52, 
		T__52=53, T__53=54, T__54=55, T__55=56, T__56=57, T__57=58, T__58=59, 
		T__59=60, T__60=61, T__61=62, T__62=63, T__63=64, T__64=65, T__65=66, 
		T__66=67, T__67=68, T__68=69, DOUBLE_LINE=70, ARROW=71, TRUE=72, FALSE=73, 
		ID=74, NUM=75, NUM_DEC=76, WS=77, LINE_COMMENT=78, BLOCK_COMMENT=79, STRING_LITERAL=80;
	public static final int
		RULE_program = 0, RULE_definition = 1, RULE_fn_def = 2, RULE_stmt = 3, 
		RULE_loop_stmt = 4, RULE_var_decl = 5, RULE_expression = 6, RULE_expression_rhs = 7, 
		RULE_assignment = 8, RULE_assignee = 9, RULE_logic_or = 10, RULE_logic_and = 11, 
		RULE_equality = 12, RULE_comparison = 13, RULE_term = 14, RULE_factor = 15, 
		RULE_unary = 16, RULE_func_call = 17, RULE_stmt_block = 18, RULE_stmt_list = 19, 
		RULE_return_stmt = 20, RULE_conditional_stmt = 21, RULE_else_stmt = 22, 
		RULE_ret_type_def = 23, RULE_param_def = 24, RULE_type_decl = 25, RULE_taskTypes = 26, 
		RULE_param_def_list = 27, RULE_entity_type_def = 28, RULE_item_type_def = 29, 
		RULE_component_def_list = 30, RULE_aggregate_value_def = 31, RULE_object_def = 32, 
		RULE_property_def_list = 33, RULE_property_def = 34, RULE_expression_list = 35, 
		RULE_grouped_expression = 36, RULE_list_definition = 37, RULE_set_definition = 38, 
		RULE_primary = 39, RULE_dot_def = 40, RULE_dot_stmt_list = 41, RULE_dot_stmt = 42, 
		RULE_dot_edge_stmt = 43, RULE_dot_node_list = 44, RULE_dot_edge_RHS = 45, 
		RULE_dot_node_stmt = 46, RULE_dot_attr_list = 47, RULE_dot_attr = 48, 
		RULE_dependency_type = 49, RULE_dungeonConfig = 50, RULE_graph = 51, RULE_taskDependency = 52, 
		RULE_dependencyAttribute = 53, RULE_singleChoiceTask = 54, RULE_multipleChoiceTask = 55, 
		RULE_assignTask = 56, RULE_field = 57, RULE_dependencyGraphField = 58, 
		RULE_descriptionField = 59, RULE_answersField = 60, RULE_correctAnswerIndexField = 61, 
		RULE_correctAnswerIndicesField = 62, RULE_solutionField = 63, RULE_pair = 64, 
		RULE_pairVal = 65, RULE_pointsField = 66, RULE_pointsToPassField = 67, 
		RULE_explanationField = 68, RULE_gradingFunctionField = 69, RULE_scenarioBuilderField = 70, 
		RULE_entity_type = 71, RULE_componentList = 72, RULE_component = 73, RULE_attributeList = 74, 
		RULE_attribute = 75, RULE_value = 76;
	private static String[] makeRuleNames() {
		return new String[] {
			"program", "definition", "fn_def", "stmt", "loop_stmt", "var_decl", "expression", 
			"expression_rhs", "assignment", "assignee", "logic_or", "logic_and", 
			"equality", "comparison", "term", "factor", "unary", "func_call", "stmt_block", 
			"stmt_list", "return_stmt", "conditional_stmt", "else_stmt", "ret_type_def", 
			"param_def", "type_decl", "taskTypes", "param_def_list", "entity_type_def", 
			"item_type_def", "component_def_list", "aggregate_value_def", "object_def", 
			"property_def_list", "property_def", "expression_list", "grouped_expression", 
			"list_definition", "set_definition", "primary", "dot_def", "dot_stmt_list", 
			"dot_stmt", "dot_edge_stmt", "dot_node_list", "dot_edge_RHS", "dot_node_stmt", 
			"dot_attr_list", "dot_attr", "dependency_type", "dungeonConfig", "graph", 
			"taskDependency", "dependencyAttribute", "singleChoiceTask", "multipleChoiceTask", 
			"assignTask", "field", "dependencyGraphField", "descriptionField", "answersField", 
			"correctAnswerIndexField", "correctAnswerIndicesField", "solutionField", 
			"pair", "pairVal", "pointsField", "pointsToPassField", "explanationField", 
			"gradingFunctionField", "scenarioBuilderField", "entity_type", "componentList", 
			"component", "attributeList", "attribute", "value"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'fn'", "'('", "')'", "';'", "'for'", "'in'", "'count'", "'while'", 
			"'var'", "'='", "':'", "'.'", "'or'", "'and'", "'!='", "'=='", "'>'", 
			"'>='", "'<'", "'<='", "'-'", "'+'", "'/'", "'*'", "'!'", "'{'", "'}'", 
			"'return'", "'if'", "'else'", "'<>'", "'[]'", "'['", "']'", "'single_choice_task'", 
			"'multiple_choice_task'", "','", "'entity_type'", "'item_type'", "'graph'", 
			"'type'", "'seq'", "'sequence'", "'st_m'", "'subtask_mandatory'", "'st_o'", 
			"'subtask_optional'", "'c_c'", "'conditional_correct'", "'c_f'", "'conditional_false'", 
			"'seq_and'", "'sequence_and'", "'seq_or'", "'sequence_or'", "'dungeon_config'", 
			"'assign_task'", "'dependency_graph'", "'description'", "'answers'", 
			"'correct_answer_index'", "'correct_answer_indices'", "'solution'", "'_'", 
			"'points'", "'points_to_pass'", "'explanation'", "'grading_function'", 
			"'scenario_builder'", "'--'", "'->'", "'true'", "'false'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, "DOUBLE_LINE", 
			"ARROW", "TRUE", "FALSE", "ID", "NUM", "NUM_DEC", "WS", "LINE_COMMENT", 
			"BLOCK_COMMENT", "STRING_LITERAL"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "DungeonDiagnostics.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public DungeonDiagnosticsParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ProgramContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(DungeonDiagnosticsParser.EOF, 0); }
		public List<DefinitionContext> definition() {
			return getRuleContexts(DefinitionContext.class);
		}
		public DefinitionContext definition(int i) {
			return getRuleContext(DefinitionContext.class,i);
		}
		public ProgramContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_program; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterProgram(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitProgram(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitProgram(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ProgramContext program() throws RecognitionException {
		ProgramContext _localctx = new ProgramContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_program);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(157);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 216174809338347522L) != 0) || _la==ID) {
				{
				{
				setState(154);
				definition();
				}
				}
				setState(159);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(160);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DefinitionContext extends ParserRuleContext {
		public Dot_defContext dot_def() {
			return getRuleContext(Dot_defContext.class,0);
		}
		public Object_defContext object_def() {
			return getRuleContext(Object_defContext.class,0);
		}
		public Entity_type_defContext entity_type_def() {
			return getRuleContext(Entity_type_defContext.class,0);
		}
		public Item_type_defContext item_type_def() {
			return getRuleContext(Item_type_defContext.class,0);
		}
		public Fn_defContext fn_def() {
			return getRuleContext(Fn_defContext.class,0);
		}
		public SingleChoiceTaskContext singleChoiceTask() {
			return getRuleContext(SingleChoiceTaskContext.class,0);
		}
		public MultipleChoiceTaskContext multipleChoiceTask() {
			return getRuleContext(MultipleChoiceTaskContext.class,0);
		}
		public AssignTaskContext assignTask() {
			return getRuleContext(AssignTaskContext.class,0);
		}
		public DungeonConfigContext dungeonConfig() {
			return getRuleContext(DungeonConfigContext.class,0);
		}
		public GraphContext graph() {
			return getRuleContext(GraphContext.class,0);
		}
		public Entity_typeContext entity_type() {
			return getRuleContext(Entity_typeContext.class,0);
		}
		public DefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_definition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DefinitionContext definition() throws RecognitionException {
		DefinitionContext _localctx = new DefinitionContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_definition);
		try {
			setState(173);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(162);
				dot_def();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(163);
				object_def();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(164);
				entity_type_def();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(165);
				item_type_def();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(166);
				fn_def();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(167);
				singleChoiceTask();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(168);
				multipleChoiceTask();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(169);
				assignTask();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(170);
				dungeonConfig();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(171);
				graph();
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(172);
				entity_type();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Fn_defContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public Stmt_blockContext stmt_block() {
			return getRuleContext(Stmt_blockContext.class,0);
		}
		public Param_def_listContext param_def_list() {
			return getRuleContext(Param_def_listContext.class,0);
		}
		public Ret_type_defContext ret_type_def() {
			return getRuleContext(Ret_type_defContext.class,0);
		}
		public Fn_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fn_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterFn_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitFn_def(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitFn_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Fn_defContext fn_def() throws RecognitionException {
		Fn_defContext _localctx = new Fn_defContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_fn_def);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(175);
			match(T__0);
			setState(176);
			match(ID);
			setState(177);
			match(T__1);
			setState(179);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((((_la - 33)) & ~0x3f) == 0 && ((1L << (_la - 33)) & 2199023255565L) != 0)) {
				{
				setState(178);
				param_def_list();
				}
			}

			setState(181);
			match(T__2);
			setState(183);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ARROW) {
				{
				setState(182);
				ret_type_def();
				}
			}

			setState(185);
			stmt_block();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StmtContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Var_declContext var_decl() {
			return getRuleContext(Var_declContext.class,0);
		}
		public Stmt_blockContext stmt_block() {
			return getRuleContext(Stmt_blockContext.class,0);
		}
		public Conditional_stmtContext conditional_stmt() {
			return getRuleContext(Conditional_stmtContext.class,0);
		}
		public Return_stmtContext return_stmt() {
			return getRuleContext(Return_stmtContext.class,0);
		}
		public Loop_stmtContext loop_stmt() {
			return getRuleContext(Loop_stmtContext.class,0);
		}
		public StmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StmtContext stmt() throws RecognitionException {
		StmtContext _localctx = new StmtContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_stmt);
		try {
			setState(195);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__1:
			case T__18:
			case T__20:
			case T__24:
			case T__32:
			case TRUE:
			case FALSE:
			case ID:
			case NUM:
			case NUM_DEC:
			case STRING_LITERAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(187);
				expression();
				setState(188);
				match(T__3);
				}
				break;
			case T__8:
				enterOuterAlt(_localctx, 2);
				{
				setState(190);
				var_decl();
				}
				break;
			case T__25:
				enterOuterAlt(_localctx, 3);
				{
				setState(191);
				stmt_block();
				}
				break;
			case T__28:
				enterOuterAlt(_localctx, 4);
				{
				setState(192);
				conditional_stmt();
				}
				break;
			case T__27:
				enterOuterAlt(_localctx, 5);
				{
				setState(193);
				return_stmt();
				}
				break;
			case T__4:
			case T__7:
				enterOuterAlt(_localctx, 6);
				{
				setState(194);
				loop_stmt();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Loop_stmtContext extends ParserRuleContext {
		public Loop_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_loop_stmt; }
	 
		public Loop_stmtContext() { }
		public void copyFrom(Loop_stmtContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class While_loopContext extends Loop_stmtContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public StmtContext stmt() {
			return getRuleContext(StmtContext.class,0);
		}
		public While_loopContext(Loop_stmtContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterWhile_loop(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitWhile_loop(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitWhile_loop(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class For_loop_countingContext extends Loop_stmtContext {
		public Type_declContext type_id;
		public Token var_id;
		public ExpressionContext iteratable_id;
		public Token counter_id;
		public StmtContext stmt() {
			return getRuleContext(StmtContext.class,0);
		}
		public Type_declContext type_decl() {
			return getRuleContext(Type_declContext.class,0);
		}
		public List<TerminalNode> ID() { return getTokens(DungeonDiagnosticsParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(DungeonDiagnosticsParser.ID, i);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public For_loop_countingContext(Loop_stmtContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterFor_loop_counting(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitFor_loop_counting(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitFor_loop_counting(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class For_loopContext extends Loop_stmtContext {
		public Type_declContext type_id;
		public Token var_id;
		public ExpressionContext iteratable_id;
		public StmtContext stmt() {
			return getRuleContext(StmtContext.class,0);
		}
		public Type_declContext type_decl() {
			return getRuleContext(Type_declContext.class,0);
		}
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public For_loopContext(Loop_stmtContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterFor_loop(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitFor_loop(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitFor_loop(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Loop_stmtContext loop_stmt() throws RecognitionException {
		Loop_stmtContext _localctx = new Loop_stmtContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_loop_stmt);
		try {
			setState(217);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				_localctx = new For_loopContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(197);
				match(T__4);
				setState(198);
				((For_loopContext)_localctx).type_id = type_decl(0);
				setState(199);
				((For_loopContext)_localctx).var_id = match(ID);
				setState(200);
				match(T__5);
				setState(201);
				((For_loopContext)_localctx).iteratable_id = expression();
				setState(202);
				stmt();
				}
				break;
			case 2:
				_localctx = new For_loop_countingContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(204);
				match(T__4);
				setState(205);
				((For_loop_countingContext)_localctx).type_id = type_decl(0);
				setState(206);
				((For_loop_countingContext)_localctx).var_id = match(ID);
				setState(207);
				match(T__5);
				setState(208);
				((For_loop_countingContext)_localctx).iteratable_id = expression();
				setState(209);
				match(T__6);
				setState(210);
				((For_loop_countingContext)_localctx).counter_id = match(ID);
				setState(211);
				stmt();
				}
				break;
			case 3:
				_localctx = new While_loopContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(213);
				match(T__7);
				setState(214);
				expression();
				setState(215);
				stmt();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Var_declContext extends ParserRuleContext {
		public Var_declContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_var_decl; }
	 
		public Var_declContext() { }
		public void copyFrom(Var_declContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Var_decl_assignmentContext extends Var_declContext {
		public Token id;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public Var_decl_assignmentContext(Var_declContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterVar_decl_assignment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitVar_decl_assignment(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitVar_decl_assignment(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Var_decl_type_declContext extends Var_declContext {
		public Token id;
		public Type_declContext type_decl() {
			return getRuleContext(Type_declContext.class,0);
		}
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public Var_decl_type_declContext(Var_declContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterVar_decl_type_decl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitVar_decl_type_decl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitVar_decl_type_decl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Var_declContext var_decl() throws RecognitionException {
		Var_declContext _localctx = new Var_declContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_var_decl);
		try {
			setState(231);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				_localctx = new Var_decl_assignmentContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(219);
				match(T__8);
				setState(220);
				((Var_decl_assignmentContext)_localctx).id = match(ID);
				setState(221);
				match(T__9);
				setState(222);
				expression();
				setState(223);
				match(T__3);
				}
				break;
			case 2:
				_localctx = new Var_decl_type_declContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(225);
				match(T__8);
				setState(226);
				((Var_decl_type_declContext)_localctx).id = match(ID);
				setState(227);
				match(T__10);
				setState(228);
				type_decl(0);
				setState(229);
				match(T__3);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionContext extends ParserRuleContext {
		public AssignmentContext assignment() {
			return getRuleContext(AssignmentContext.class,0);
		}
		public Expression_rhsContext expression_rhs() {
			return getRuleContext(Expression_rhsContext.class,0);
		}
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(233);
			assignment();
			setState(235);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				{
				setState(234);
				expression_rhs();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Expression_rhsContext extends ParserRuleContext {
		public Expression_rhsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression_rhs; }
	 
		public Expression_rhsContext() { }
		public void copyFrom(Expression_rhsContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Method_call_expressionContext extends Expression_rhsContext {
		public Func_callContext func_call() {
			return getRuleContext(Func_callContext.class,0);
		}
		public Expression_rhsContext expression_rhs() {
			return getRuleContext(Expression_rhsContext.class,0);
		}
		public Method_call_expressionContext(Expression_rhsContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterMethod_call_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitMethod_call_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitMethod_call_expression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Member_access_expressionContext extends Expression_rhsContext {
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public Expression_rhsContext expression_rhs() {
			return getRuleContext(Expression_rhsContext.class,0);
		}
		public Member_access_expressionContext(Expression_rhsContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterMember_access_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitMember_access_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitMember_access_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Expression_rhsContext expression_rhs() throws RecognitionException {
		Expression_rhsContext _localctx = new Expression_rhsContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_expression_rhs);
		try {
			setState(247);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				_localctx = new Method_call_expressionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(237);
				match(T__11);
				setState(238);
				func_call();
				setState(240);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
				case 1:
					{
					setState(239);
					expression_rhs();
					}
					break;
				}
				}
				break;
			case 2:
				_localctx = new Member_access_expressionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(242);
				match(T__11);
				setState(243);
				match(ID);
				setState(245);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
				case 1:
					{
					setState(244);
					expression_rhs();
					}
					break;
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AssignmentContext extends ParserRuleContext {
		public AssigneeContext assignee() {
			return getRuleContext(AssigneeContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Logic_orContext logic_or() {
			return getRuleContext(Logic_orContext.class,0);
		}
		public AssignmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterAssignment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitAssignment(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitAssignment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentContext assignment() throws RecognitionException {
		AssignmentContext _localctx = new AssignmentContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_assignment);
		try {
			setState(254);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(249);
				assignee();
				setState(250);
				match(T__9);
				setState(251);
				expression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(253);
				logic_or(0);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AssigneeContext extends ParserRuleContext {
		public AssigneeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignee; }
	 
		public AssigneeContext() { }
		public void copyFrom(AssigneeContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Assignee_func_callContext extends AssigneeContext {
		public Func_callContext func_call() {
			return getRuleContext(Func_callContext.class,0);
		}
		public AssigneeContext assignee() {
			return getRuleContext(AssigneeContext.class,0);
		}
		public Assignee_func_callContext(AssigneeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterAssignee_func_call(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitAssignee_func_call(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitAssignee_func_call(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Assignee_identifierContext extends AssigneeContext {
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public Assignee_identifierContext(AssigneeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterAssignee_identifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitAssignee_identifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitAssignee_identifier(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Assignee_qualified_nameContext extends AssigneeContext {
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public AssigneeContext assignee() {
			return getRuleContext(AssigneeContext.class,0);
		}
		public Assignee_qualified_nameContext(AssigneeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterAssignee_qualified_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitAssignee_qualified_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitAssignee_qualified_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssigneeContext assignee() throws RecognitionException {
		AssigneeContext _localctx = new AssigneeContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_assignee);
		try {
			setState(264);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				_localctx = new Assignee_func_callContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(256);
				func_call();
				setState(257);
				match(T__11);
				setState(258);
				assignee();
				}
				break;
			case 2:
				_localctx = new Assignee_qualified_nameContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(260);
				match(ID);
				setState(261);
				match(T__11);
				setState(262);
				assignee();
				}
				break;
			case 3:
				_localctx = new Assignee_identifierContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(263);
				match(ID);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Logic_orContext extends ParserRuleContext {
		public Token or;
		public Logic_andContext logic_and() {
			return getRuleContext(Logic_andContext.class,0);
		}
		public Logic_orContext logic_or() {
			return getRuleContext(Logic_orContext.class,0);
		}
		public Logic_orContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logic_or; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterLogic_or(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitLogic_or(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitLogic_or(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Logic_orContext logic_or() throws RecognitionException {
		return logic_or(0);
	}

	private Logic_orContext logic_or(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		Logic_orContext _localctx = new Logic_orContext(_ctx, _parentState);
		Logic_orContext _prevctx = _localctx;
		int _startState = 20;
		enterRecursionRule(_localctx, 20, RULE_logic_or, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(267);
			logic_and(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(274);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Logic_orContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_logic_or);
					setState(269);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					{
					setState(270);
					((Logic_orContext)_localctx).or = match(T__12);
					setState(271);
					logic_and(0);
					}
					}
					} 
				}
				setState(276);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Logic_andContext extends ParserRuleContext {
		public Token and;
		public EqualityContext equality() {
			return getRuleContext(EqualityContext.class,0);
		}
		public Logic_andContext logic_and() {
			return getRuleContext(Logic_andContext.class,0);
		}
		public Logic_andContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logic_and; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterLogic_and(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitLogic_and(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitLogic_and(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Logic_andContext logic_and() throws RecognitionException {
		return logic_and(0);
	}

	private Logic_andContext logic_and(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		Logic_andContext _localctx = new Logic_andContext(_ctx, _parentState);
		Logic_andContext _prevctx = _localctx;
		int _startState = 22;
		enterRecursionRule(_localctx, 22, RULE_logic_and, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(278);
			equality(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(285);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Logic_andContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_logic_and);
					setState(280);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					{
					setState(281);
					((Logic_andContext)_localctx).and = match(T__13);
					setState(282);
					equality(0);
					}
					}
					} 
				}
				setState(287);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class EqualityContext extends ParserRuleContext {
		public Token neq;
		public Token eq;
		public ComparisonContext comparison() {
			return getRuleContext(ComparisonContext.class,0);
		}
		public EqualityContext equality() {
			return getRuleContext(EqualityContext.class,0);
		}
		public EqualityContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_equality; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterEquality(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitEquality(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitEquality(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EqualityContext equality() throws RecognitionException {
		return equality(0);
	}

	private EqualityContext equality(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		EqualityContext _localctx = new EqualityContext(_ctx, _parentState);
		EqualityContext _prevctx = _localctx;
		int _startState = 24;
		enterRecursionRule(_localctx, 24, RULE_equality, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(289);
			comparison(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(299);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new EqualityContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_equality);
					setState(291);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					{
					setState(294);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case T__14:
						{
						setState(292);
						((EqualityContext)_localctx).neq = match(T__14);
						}
						break;
					case T__15:
						{
						setState(293);
						((EqualityContext)_localctx).eq = match(T__15);
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(296);
					comparison(0);
					}
					}
					} 
				}
				setState(301);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ComparisonContext extends ParserRuleContext {
		public Token gt;
		public Token geq;
		public Token lt;
		public Token leq;
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public ComparisonContext comparison() {
			return getRuleContext(ComparisonContext.class,0);
		}
		public ComparisonContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparison; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterComparison(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitComparison(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitComparison(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComparisonContext comparison() throws RecognitionException {
		return comparison(0);
	}

	private ComparisonContext comparison(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ComparisonContext _localctx = new ComparisonContext(_ctx, _parentState);
		ComparisonContext _prevctx = _localctx;
		int _startState = 26;
		enterRecursionRule(_localctx, 26, RULE_comparison, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(303);
			term(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(315);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new ComparisonContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_comparison);
					setState(305);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					{
					setState(310);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case T__16:
						{
						setState(306);
						((ComparisonContext)_localctx).gt = match(T__16);
						}
						break;
					case T__17:
						{
						setState(307);
						((ComparisonContext)_localctx).geq = match(T__17);
						}
						break;
					case T__18:
						{
						setState(308);
						((ComparisonContext)_localctx).lt = match(T__18);
						}
						break;
					case T__19:
						{
						setState(309);
						((ComparisonContext)_localctx).leq = match(T__19);
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(312);
					term(0);
					}
					}
					} 
				}
				setState(317);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TermContext extends ParserRuleContext {
		public Token minus;
		public Token plus;
		public FactorContext factor() {
			return getRuleContext(FactorContext.class,0);
		}
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public TermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_term; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitTerm(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitTerm(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		return term(0);
	}

	private TermContext term(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		TermContext _localctx = new TermContext(_ctx, _parentState);
		TermContext _prevctx = _localctx;
		int _startState = 28;
		enterRecursionRule(_localctx, 28, RULE_term, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(319);
			factor(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(329);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new TermContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_term);
					setState(321);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					{
					setState(324);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case T__20:
						{
						setState(322);
						((TermContext)_localctx).minus = match(T__20);
						}
						break;
					case T__21:
						{
						setState(323);
						((TermContext)_localctx).plus = match(T__21);
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(326);
					factor(0);
					}
					}
					} 
				}
				setState(331);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FactorContext extends ParserRuleContext {
		public Token div;
		public Token mult;
		public UnaryContext unary() {
			return getRuleContext(UnaryContext.class,0);
		}
		public FactorContext factor() {
			return getRuleContext(FactorContext.class,0);
		}
		public FactorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_factor; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterFactor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitFactor(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitFactor(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FactorContext factor() throws RecognitionException {
		return factor(0);
	}

	private FactorContext factor(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		FactorContext _localctx = new FactorContext(_ctx, _parentState);
		FactorContext _prevctx = _localctx;
		int _startState = 30;
		enterRecursionRule(_localctx, 30, RULE_factor, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(333);
			unary();
			}
			_ctx.stop = _input.LT(-1);
			setState(343);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,22,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new FactorContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_factor);
					setState(335);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					{
					setState(338);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case T__22:
						{
						setState(336);
						((FactorContext)_localctx).div = match(T__22);
						}
						break;
					case T__23:
						{
						setState(337);
						((FactorContext)_localctx).mult = match(T__23);
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(340);
					unary();
					}
					}
					} 
				}
				setState(345);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,22,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class UnaryContext extends ParserRuleContext {
		public Token bang;
		public Token minus;
		public UnaryContext unary() {
			return getRuleContext(UnaryContext.class,0);
		}
		public PrimaryContext primary() {
			return getRuleContext(PrimaryContext.class,0);
		}
		public UnaryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unary; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterUnary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitUnary(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitUnary(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnaryContext unary() throws RecognitionException {
		UnaryContext _localctx = new UnaryContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_unary);
		try {
			setState(352);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__20:
			case T__24:
				enterOuterAlt(_localctx, 1);
				{
				setState(348);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case T__24:
					{
					setState(346);
					((UnaryContext)_localctx).bang = match(T__24);
					}
					break;
				case T__20:
					{
					setState(347);
					((UnaryContext)_localctx).minus = match(T__20);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(350);
				unary();
				}
				break;
			case T__1:
			case T__18:
			case T__32:
			case TRUE:
			case FALSE:
			case ID:
			case NUM:
			case NUM_DEC:
			case STRING_LITERAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(351);
				primary();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Func_callContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public Expression_listContext expression_list() {
			return getRuleContext(Expression_listContext.class,0);
		}
		public Func_callContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_func_call; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterFunc_call(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitFunc_call(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitFunc_call(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Func_callContext func_call() throws RecognitionException {
		Func_callContext _localctx = new Func_callContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_func_call);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(354);
			match(ID);
			setState(355);
			match(T__1);
			setState(357);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 8626110468L) != 0) || ((((_la - 72)) & ~0x3f) == 0 && ((1L << (_la - 72)) & 287L) != 0)) {
				{
				setState(356);
				expression_list();
				}
			}

			setState(359);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Stmt_blockContext extends ParserRuleContext {
		public Stmt_listContext stmt_list() {
			return getRuleContext(Stmt_listContext.class,0);
		}
		public Stmt_blockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stmt_block; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterStmt_block(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitStmt_block(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitStmt_block(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Stmt_blockContext stmt_block() throws RecognitionException {
		Stmt_blockContext _localctx = new Stmt_blockContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_stmt_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(361);
			match(T__25);
			setState(363);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 9498526500L) != 0) || ((((_la - 72)) & ~0x3f) == 0 && ((1L << (_la - 72)) & 287L) != 0)) {
				{
				setState(362);
				stmt_list();
				}
			}

			setState(365);
			match(T__26);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Stmt_listContext extends ParserRuleContext {
		public StmtContext stmt() {
			return getRuleContext(StmtContext.class,0);
		}
		public Stmt_listContext stmt_list() {
			return getRuleContext(Stmt_listContext.class,0);
		}
		public Stmt_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stmt_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterStmt_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitStmt_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitStmt_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Stmt_listContext stmt_list() throws RecognitionException {
		Stmt_listContext _localctx = new Stmt_listContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_stmt_list);
		try {
			setState(371);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(367);
				stmt();
				setState(368);
				stmt_list();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(370);
				stmt();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Return_stmtContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Return_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_return_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterReturn_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitReturn_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitReturn_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Return_stmtContext return_stmt() throws RecognitionException {
		Return_stmtContext _localctx = new Return_stmtContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_return_stmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(373);
			match(T__27);
			setState(375);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 8626110468L) != 0) || ((((_la - 72)) & ~0x3f) == 0 && ((1L << (_la - 72)) & 287L) != 0)) {
				{
				setState(374);
				expression();
				}
			}

			setState(377);
			match(T__3);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Conditional_stmtContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public StmtContext stmt() {
			return getRuleContext(StmtContext.class,0);
		}
		public Else_stmtContext else_stmt() {
			return getRuleContext(Else_stmtContext.class,0);
		}
		public Conditional_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conditional_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterConditional_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitConditional_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitConditional_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Conditional_stmtContext conditional_stmt() throws RecognitionException {
		Conditional_stmtContext _localctx = new Conditional_stmtContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_conditional_stmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(379);
			match(T__28);
			setState(380);
			expression();
			setState(381);
			stmt();
			setState(383);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,29,_ctx) ) {
			case 1:
				{
				setState(382);
				else_stmt();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Else_stmtContext extends ParserRuleContext {
		public StmtContext stmt() {
			return getRuleContext(StmtContext.class,0);
		}
		public Else_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_else_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterElse_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitElse_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitElse_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Else_stmtContext else_stmt() throws RecognitionException {
		Else_stmtContext _localctx = new Else_stmtContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_else_stmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(385);
			match(T__29);
			setState(386);
			stmt();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Ret_type_defContext extends ParserRuleContext {
		public Type_declContext type_id;
		public TerminalNode ARROW() { return getToken(DungeonDiagnosticsParser.ARROW, 0); }
		public Type_declContext type_decl() {
			return getRuleContext(Type_declContext.class,0);
		}
		public Ret_type_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ret_type_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterRet_type_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitRet_type_def(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitRet_type_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Ret_type_defContext ret_type_def() throws RecognitionException {
		Ret_type_defContext _localctx = new Ret_type_defContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_ret_type_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(388);
			match(ARROW);
			setState(389);
			((Ret_type_defContext)_localctx).type_id = type_decl(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Param_defContext extends ParserRuleContext {
		public Type_declContext type_id;
		public Token param_id;
		public Type_declContext type_decl() {
			return getRuleContext(Type_declContext.class,0);
		}
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public Param_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_param_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterParam_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitParam_def(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitParam_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Param_defContext param_def() throws RecognitionException {
		Param_defContext _localctx = new Param_defContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_param_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(391);
			((Param_defContext)_localctx).type_id = type_decl(0);
			setState(392);
			((Param_defContext)_localctx).param_id = match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Type_declContext extends ParserRuleContext {
		public Type_declContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_type_decl; }
	 
		public Type_declContext() { }
		public void copyFrom(Type_declContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Map_param_typeContext extends Type_declContext {
		public List<Type_declContext> type_decl() {
			return getRuleContexts(Type_declContext.class);
		}
		public Type_declContext type_decl(int i) {
			return getRuleContext(Type_declContext.class,i);
		}
		public TerminalNode ARROW() { return getToken(DungeonDiagnosticsParser.ARROW, 0); }
		public Map_param_typeContext(Type_declContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterMap_param_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitMap_param_type(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitMap_param_type(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Task_typesContext extends Type_declContext {
		public TaskTypesContext taskTypes() {
			return getRuleContext(TaskTypesContext.class,0);
		}
		public Task_typesContext(Type_declContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterTask_types(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitTask_types(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitTask_types(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Id_param_typeContext extends Type_declContext {
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public Id_param_typeContext(Type_declContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterId_param_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitId_param_type(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitId_param_type(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class List_param_typeContext extends Type_declContext {
		public Type_declContext type_decl() {
			return getRuleContext(Type_declContext.class,0);
		}
		public List_param_typeContext(Type_declContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterList_param_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitList_param_type(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitList_param_type(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Set_param_typeContext extends Type_declContext {
		public Type_declContext type_decl() {
			return getRuleContext(Type_declContext.class,0);
		}
		public Set_param_typeContext(Type_declContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterSet_param_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitSet_param_type(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitSet_param_type(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Type_declContext type_decl() throws RecognitionException {
		return type_decl(0);
	}

	private Type_declContext type_decl(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		Type_declContext _localctx = new Type_declContext(_ctx, _parentState);
		Type_declContext _prevctx = _localctx;
		int _startState = 50;
		enterRecursionRule(_localctx, 50, RULE_type_decl, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(403);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__32:
				{
				_localctx = new Map_param_typeContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(395);
				match(T__32);
				setState(396);
				type_decl(0);
				setState(397);
				match(ARROW);
				setState(398);
				type_decl(0);
				setState(399);
				match(T__33);
				}
				break;
			case T__34:
			case T__35:
				{
				_localctx = new Task_typesContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(401);
				taskTypes();
				}
				break;
			case ID:
				{
				_localctx = new Id_param_typeContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(402);
				match(ID);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(411);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,32,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(409);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
					case 1:
						{
						_localctx = new Set_param_typeContext(new Type_declContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_type_decl);
						setState(405);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(406);
						match(T__30);
						}
						break;
					case 2:
						{
						_localctx = new List_param_typeContext(new Type_declContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_type_decl);
						setState(407);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(408);
						match(T__31);
						}
						break;
					}
					} 
				}
				setState(413);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,32,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TaskTypesContext extends ParserRuleContext {
		public TaskTypesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_taskTypes; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterTaskTypes(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitTaskTypes(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitTaskTypes(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TaskTypesContext taskTypes() throws RecognitionException {
		TaskTypesContext _localctx = new TaskTypesContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_taskTypes);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(414);
			_la = _input.LA(1);
			if ( !(_la==T__34 || _la==T__35) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Param_def_listContext extends ParserRuleContext {
		public Param_defContext param_def() {
			return getRuleContext(Param_defContext.class,0);
		}
		public Param_def_listContext param_def_list() {
			return getRuleContext(Param_def_listContext.class,0);
		}
		public Param_def_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_param_def_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterParam_def_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitParam_def_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitParam_def_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Param_def_listContext param_def_list() throws RecognitionException {
		Param_def_listContext _localctx = new Param_def_listContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_param_def_list);
		try {
			setState(421);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,33,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(416);
				param_def();
				setState(417);
				match(T__36);
				setState(418);
				param_def_list();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(420);
				param_def();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Entity_type_defContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public Component_def_listContext component_def_list() {
			return getRuleContext(Component_def_listContext.class,0);
		}
		public Entity_type_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_entity_type_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterEntity_type_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitEntity_type_def(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitEntity_type_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Entity_type_defContext entity_type_def() throws RecognitionException {
		Entity_type_defContext _localctx = new Entity_type_defContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_entity_type_def);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(423);
			match(T__37);
			setState(424);
			match(ID);
			setState(425);
			match(T__25);
			setState(427);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(426);
				component_def_list();
				}
			}

			setState(429);
			match(T__26);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Item_type_defContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public Property_def_listContext property_def_list() {
			return getRuleContext(Property_def_listContext.class,0);
		}
		public Item_type_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_item_type_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterItem_type_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitItem_type_def(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitItem_type_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Item_type_defContext item_type_def() throws RecognitionException {
		Item_type_defContext _localctx = new Item_type_defContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_item_type_def);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(431);
			match(T__38);
			setState(432);
			match(ID);
			setState(433);
			match(T__25);
			setState(435);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(434);
				property_def_list();
				}
			}

			setState(437);
			match(T__26);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Component_def_listContext extends ParserRuleContext {
		public Aggregate_value_defContext aggregate_value_def() {
			return getRuleContext(Aggregate_value_defContext.class,0);
		}
		public Component_def_listContext component_def_list() {
			return getRuleContext(Component_def_listContext.class,0);
		}
		public Component_def_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_component_def_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterComponent_def_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitComponent_def_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitComponent_def_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Component_def_listContext component_def_list() throws RecognitionException {
		Component_def_listContext _localctx = new Component_def_listContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_component_def_list);
		try {
			setState(444);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,36,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(439);
				aggregate_value_def();
				setState(440);
				match(T__36);
				setState(441);
				component_def_list();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(443);
				aggregate_value_def();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Aggregate_value_defContext extends ParserRuleContext {
		public Token type_id;
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public Property_def_listContext property_def_list() {
			return getRuleContext(Property_def_listContext.class,0);
		}
		public Aggregate_value_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aggregate_value_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterAggregate_value_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitAggregate_value_def(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitAggregate_value_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Aggregate_value_defContext aggregate_value_def() throws RecognitionException {
		Aggregate_value_defContext _localctx = new Aggregate_value_defContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_aggregate_value_def);
		int _la;
		try {
			setState(453);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,38,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(446);
				((Aggregate_value_defContext)_localctx).type_id = match(ID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(447);
				((Aggregate_value_defContext)_localctx).type_id = match(ID);
				setState(448);
				match(T__25);
				setState(450);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(449);
					property_def_list();
					}
				}

				setState(452);
				match(T__26);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Object_defContext extends ParserRuleContext {
		public Token type_id;
		public Token object_id;
		public List<TerminalNode> ID() { return getTokens(DungeonDiagnosticsParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(DungeonDiagnosticsParser.ID, i);
		}
		public Property_def_listContext property_def_list() {
			return getRuleContext(Property_def_listContext.class,0);
		}
		public Object_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_object_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterObject_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitObject_def(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitObject_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Object_defContext object_def() throws RecognitionException {
		Object_defContext _localctx = new Object_defContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_object_def);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(455);
			((Object_defContext)_localctx).type_id = match(ID);
			setState(456);
			((Object_defContext)_localctx).object_id = match(ID);
			setState(457);
			match(T__25);
			setState(459);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(458);
				property_def_list();
				}
			}

			setState(461);
			match(T__26);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Property_def_listContext extends ParserRuleContext {
		public Property_defContext property_def() {
			return getRuleContext(Property_defContext.class,0);
		}
		public Property_def_listContext property_def_list() {
			return getRuleContext(Property_def_listContext.class,0);
		}
		public Property_def_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_property_def_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterProperty_def_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitProperty_def_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitProperty_def_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Property_def_listContext property_def_list() throws RecognitionException {
		Property_def_listContext _localctx = new Property_def_listContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_property_def_list);
		try {
			setState(468);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,40,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(463);
				property_def();
				setState(464);
				match(T__36);
				setState(465);
				property_def_list();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(467);
				property_def();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Property_defContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Property_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_property_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterProperty_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitProperty_def(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitProperty_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Property_defContext property_def() throws RecognitionException {
		Property_defContext _localctx = new Property_defContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_property_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(470);
			match(ID);
			setState(471);
			match(T__10);
			setState(472);
			expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Expression_listContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Expression_listContext expression_list() {
			return getRuleContext(Expression_listContext.class,0);
		}
		public Expression_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterExpression_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitExpression_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitExpression_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Expression_listContext expression_list() throws RecognitionException {
		Expression_listContext _localctx = new Expression_listContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_expression_list);
		try {
			setState(479);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,41,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(474);
				expression();
				setState(475);
				match(T__36);
				setState(476);
				expression_list();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(478);
				expression();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Grouped_expressionContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Grouped_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_grouped_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterGrouped_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitGrouped_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitGrouped_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Grouped_expressionContext grouped_expression() throws RecognitionException {
		Grouped_expressionContext _localctx = new Grouped_expressionContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_grouped_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(481);
			match(T__1);
			setState(482);
			expression();
			setState(483);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class List_definitionContext extends ParserRuleContext {
		public Expression_listContext expression_list() {
			return getRuleContext(Expression_listContext.class,0);
		}
		public List_definitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_list_definition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterList_definition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitList_definition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitList_definition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final List_definitionContext list_definition() throws RecognitionException {
		List_definitionContext _localctx = new List_definitionContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_list_definition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(485);
			match(T__32);
			setState(487);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 8626110468L) != 0) || ((((_la - 72)) & ~0x3f) == 0 && ((1L << (_la - 72)) & 287L) != 0)) {
				{
				setState(486);
				expression_list();
				}
			}

			setState(489);
			match(T__33);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Set_definitionContext extends ParserRuleContext {
		public Expression_listContext expression_list() {
			return getRuleContext(Expression_listContext.class,0);
		}
		public Set_definitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_set_definition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterSet_definition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitSet_definition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitSet_definition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Set_definitionContext set_definition() throws RecognitionException {
		Set_definitionContext _localctx = new Set_definitionContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_set_definition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(491);
			match(T__18);
			setState(493);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 8626110468L) != 0) || ((((_la - 72)) & ~0x3f) == 0 && ((1L << (_la - 72)) & 287L) != 0)) {
				{
				setState(492);
				expression_list();
				}
			}

			setState(495);
			match(T__16);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PrimaryContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public TerminalNode STRING_LITERAL() { return getToken(DungeonDiagnosticsParser.STRING_LITERAL, 0); }
		public TerminalNode TRUE() { return getToken(DungeonDiagnosticsParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(DungeonDiagnosticsParser.FALSE, 0); }
		public TerminalNode NUM() { return getToken(DungeonDiagnosticsParser.NUM, 0); }
		public TerminalNode NUM_DEC() { return getToken(DungeonDiagnosticsParser.NUM_DEC, 0); }
		public Aggregate_value_defContext aggregate_value_def() {
			return getRuleContext(Aggregate_value_defContext.class,0);
		}
		public Set_definitionContext set_definition() {
			return getRuleContext(Set_definitionContext.class,0);
		}
		public Grouped_expressionContext grouped_expression() {
			return getRuleContext(Grouped_expressionContext.class,0);
		}
		public Func_callContext func_call() {
			return getRuleContext(Func_callContext.class,0);
		}
		public List_definitionContext list_definition() {
			return getRuleContext(List_definitionContext.class,0);
		}
		public PrimaryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primary; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterPrimary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitPrimary(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitPrimary(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimaryContext primary() throws RecognitionException {
		PrimaryContext _localctx = new PrimaryContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_primary);
		try {
			setState(508);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,44,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(497);
				match(ID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(498);
				match(STRING_LITERAL);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(499);
				match(TRUE);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(500);
				match(FALSE);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(501);
				match(NUM);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(502);
				match(NUM_DEC);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(503);
				aggregate_value_def();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(504);
				set_definition();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(505);
				grouped_expression();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(506);
				func_call();
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(507);
				list_definition();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Dot_defContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public Dot_stmt_listContext dot_stmt_list() {
			return getRuleContext(Dot_stmt_listContext.class,0);
		}
		public Dot_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dot_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterDot_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitDot_def(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitDot_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Dot_defContext dot_def() throws RecognitionException {
		Dot_defContext _localctx = new Dot_defContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_dot_def);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(510);
			match(T__39);
			setState(511);
			match(ID);
			setState(512);
			match(T__25);
			setState(514);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(513);
				dot_stmt_list();
				}
			}

			setState(516);
			match(T__26);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Dot_stmt_listContext extends ParserRuleContext {
		public Dot_stmtContext dot_stmt() {
			return getRuleContext(Dot_stmtContext.class,0);
		}
		public Dot_stmt_listContext dot_stmt_list() {
			return getRuleContext(Dot_stmt_listContext.class,0);
		}
		public Dot_stmt_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dot_stmt_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterDot_stmt_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitDot_stmt_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitDot_stmt_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Dot_stmt_listContext dot_stmt_list() throws RecognitionException {
		Dot_stmt_listContext _localctx = new Dot_stmt_listContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_dot_stmt_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(518);
			dot_stmt();
			setState(520);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__3) {
				{
				setState(519);
				match(T__3);
				}
			}

			setState(523);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(522);
				dot_stmt_list();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Dot_stmtContext extends ParserRuleContext {
		public Dot_node_stmtContext dot_node_stmt() {
			return getRuleContext(Dot_node_stmtContext.class,0);
		}
		public Dot_edge_stmtContext dot_edge_stmt() {
			return getRuleContext(Dot_edge_stmtContext.class,0);
		}
		public Dot_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dot_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterDot_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitDot_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitDot_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Dot_stmtContext dot_stmt() throws RecognitionException {
		Dot_stmtContext _localctx = new Dot_stmtContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_dot_stmt);
		try {
			setState(527);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,48,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(525);
				dot_node_stmt();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(526);
				dot_edge_stmt();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Dot_edge_stmtContext extends ParserRuleContext {
		public Dot_node_listContext dot_node_list() {
			return getRuleContext(Dot_node_listContext.class,0);
		}
		public List<Dot_edge_RHSContext> dot_edge_RHS() {
			return getRuleContexts(Dot_edge_RHSContext.class);
		}
		public Dot_edge_RHSContext dot_edge_RHS(int i) {
			return getRuleContext(Dot_edge_RHSContext.class,i);
		}
		public Dot_attr_listContext dot_attr_list() {
			return getRuleContext(Dot_attr_listContext.class,0);
		}
		public Dot_edge_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dot_edge_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterDot_edge_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitDot_edge_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitDot_edge_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Dot_edge_stmtContext dot_edge_stmt() throws RecognitionException {
		Dot_edge_stmtContext _localctx = new Dot_edge_stmtContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_dot_edge_stmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(529);
			dot_node_list();
			setState(531); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(530);
				dot_edge_RHS();
				}
				}
				setState(533); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==ARROW );
			setState(536);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__32) {
				{
				setState(535);
				dot_attr_list();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Dot_node_listContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public Dot_node_listContext dot_node_list() {
			return getRuleContext(Dot_node_listContext.class,0);
		}
		public Dot_node_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dot_node_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterDot_node_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitDot_node_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitDot_node_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Dot_node_listContext dot_node_list() throws RecognitionException {
		Dot_node_listContext _localctx = new Dot_node_listContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_dot_node_list);
		try {
			setState(542);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,51,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(538);
				match(ID);
				setState(539);
				match(T__36);
				setState(540);
				dot_node_list();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(541);
				match(ID);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Dot_edge_RHSContext extends ParserRuleContext {
		public TerminalNode ARROW() { return getToken(DungeonDiagnosticsParser.ARROW, 0); }
		public Dot_node_listContext dot_node_list() {
			return getRuleContext(Dot_node_listContext.class,0);
		}
		public Dot_edge_RHSContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dot_edge_RHS; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterDot_edge_RHS(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitDot_edge_RHS(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitDot_edge_RHS(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Dot_edge_RHSContext dot_edge_RHS() throws RecognitionException {
		Dot_edge_RHSContext _localctx = new Dot_edge_RHSContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_dot_edge_RHS);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(544);
			match(ARROW);
			setState(545);
			dot_node_list();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Dot_node_stmtContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public Dot_attr_listContext dot_attr_list() {
			return getRuleContext(Dot_attr_listContext.class,0);
		}
		public Dot_node_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dot_node_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterDot_node_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitDot_node_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitDot_node_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Dot_node_stmtContext dot_node_stmt() throws RecognitionException {
		Dot_node_stmtContext _localctx = new Dot_node_stmtContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_dot_node_stmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(547);
			match(ID);
			setState(549);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__32) {
				{
				setState(548);
				dot_attr_list();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Dot_attr_listContext extends ParserRuleContext {
		public List<Dot_attrContext> dot_attr() {
			return getRuleContexts(Dot_attrContext.class);
		}
		public Dot_attrContext dot_attr(int i) {
			return getRuleContext(Dot_attrContext.class,i);
		}
		public Dot_attr_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dot_attr_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterDot_attr_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitDot_attr_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitDot_attr_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Dot_attr_listContext dot_attr_list() throws RecognitionException {
		Dot_attr_listContext _localctx = new Dot_attr_listContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_dot_attr_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(551);
			match(T__32);
			setState(553); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(552);
				dot_attr();
				}
				}
				setState(555); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==T__40 || _la==ID );
			setState(557);
			match(T__33);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Dot_attrContext extends ParserRuleContext {
		public Dot_attrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dot_attr; }
	 
		public Dot_attrContext() { }
		public void copyFrom(Dot_attrContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Dot_attr_idContext extends Dot_attrContext {
		public List<TerminalNode> ID() { return getTokens(DungeonDiagnosticsParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(DungeonDiagnosticsParser.ID, i);
		}
		public Dot_attr_idContext(Dot_attrContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterDot_attr_id(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitDot_attr_id(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitDot_attr_id(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Dot_attr_dependency_typeContext extends Dot_attrContext {
		public Dependency_typeContext dependency_type() {
			return getRuleContext(Dependency_typeContext.class,0);
		}
		public Dot_attr_dependency_typeContext(Dot_attrContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterDot_attr_dependency_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitDot_attr_dependency_type(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitDot_attr_dependency_type(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Dot_attrContext dot_attr() throws RecognitionException {
		Dot_attrContext _localctx = new Dot_attrContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_dot_attr);
		int _la;
		try {
			setState(571);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ID:
				_localctx = new Dot_attr_idContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(559);
				match(ID);
				setState(560);
				match(T__9);
				setState(561);
				match(ID);
				setState(563);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__3 || _la==T__36) {
					{
					setState(562);
					_la = _input.LA(1);
					if ( !(_la==T__3 || _la==T__36) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				}
				break;
			case T__40:
				_localctx = new Dot_attr_dependency_typeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(565);
				match(T__40);
				setState(566);
				match(T__9);
				setState(567);
				dependency_type();
				setState(569);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__3 || _la==T__36) {
					{
					setState(568);
					_la = _input.LA(1);
					if ( !(_la==T__3 || _la==T__36) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Dependency_typeContext extends ParserRuleContext {
		public Dependency_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dependency_type; }
	 
		public Dependency_typeContext() { }
		public void copyFrom(Dependency_typeContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Dt_sequenceContext extends Dependency_typeContext {
		public Dt_sequenceContext(Dependency_typeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterDt_sequence(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitDt_sequence(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitDt_sequence(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Dt_subtask_mandatoryContext extends Dependency_typeContext {
		public Dt_subtask_mandatoryContext(Dependency_typeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterDt_subtask_mandatory(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitDt_subtask_mandatory(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitDt_subtask_mandatory(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Dt_sequence_andContext extends Dependency_typeContext {
		public Dt_sequence_andContext(Dependency_typeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterDt_sequence_and(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitDt_sequence_and(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitDt_sequence_and(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Dt_conditional_falseContext extends Dependency_typeContext {
		public Dt_conditional_falseContext(Dependency_typeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterDt_conditional_false(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitDt_conditional_false(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitDt_conditional_false(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Dt_subtask_optionalContext extends Dependency_typeContext {
		public Dt_subtask_optionalContext(Dependency_typeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterDt_subtask_optional(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitDt_subtask_optional(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitDt_subtask_optional(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Dt_conditional_correctContext extends Dependency_typeContext {
		public Dt_conditional_correctContext(Dependency_typeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterDt_conditional_correct(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitDt_conditional_correct(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitDt_conditional_correct(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Dt_sequence_orContext extends Dependency_typeContext {
		public Dt_sequence_orContext(Dependency_typeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterDt_sequence_or(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitDt_sequence_or(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitDt_sequence_or(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Dependency_typeContext dependency_type() throws RecognitionException {
		Dependency_typeContext _localctx = new Dependency_typeContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_dependency_type);
		int _la;
		try {
			setState(580);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__41:
			case T__42:
				_localctx = new Dt_sequenceContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(573);
				_la = _input.LA(1);
				if ( !(_la==T__41 || _la==T__42) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case T__43:
			case T__44:
				_localctx = new Dt_subtask_mandatoryContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(574);
				_la = _input.LA(1);
				if ( !(_la==T__43 || _la==T__44) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case T__45:
			case T__46:
				_localctx = new Dt_subtask_optionalContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(575);
				_la = _input.LA(1);
				if ( !(_la==T__45 || _la==T__46) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case T__47:
			case T__48:
				_localctx = new Dt_conditional_correctContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(576);
				_la = _input.LA(1);
				if ( !(_la==T__47 || _la==T__48) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case T__49:
			case T__50:
				_localctx = new Dt_conditional_falseContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(577);
				_la = _input.LA(1);
				if ( !(_la==T__49 || _la==T__50) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case T__51:
			case T__52:
				_localctx = new Dt_sequence_andContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(578);
				_la = _input.LA(1);
				if ( !(_la==T__51 || _la==T__52) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case T__53:
			case T__54:
				_localctx = new Dt_sequence_orContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(579);
				_la = _input.LA(1);
				if ( !(_la==T__53 || _la==T__54) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DungeonConfigContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public DependencyGraphFieldContext dependencyGraphField() {
			return getRuleContext(DependencyGraphFieldContext.class,0);
		}
		public DungeonConfigContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dungeonConfig; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterDungeonConfig(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitDungeonConfig(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitDungeonConfig(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DungeonConfigContext dungeonConfig() throws RecognitionException {
		DungeonConfigContext _localctx = new DungeonConfigContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_dungeonConfig);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(582);
			match(T__55);
			setState(583);
			match(ID);
			setState(584);
			match(T__25);
			setState(585);
			dependencyGraphField();
			setState(586);
			match(T__26);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class GraphContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public TaskDependencyContext taskDependency() {
			return getRuleContext(TaskDependencyContext.class,0);
		}
		public GraphContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_graph; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterGraph(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitGraph(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitGraph(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GraphContext graph() throws RecognitionException {
		GraphContext _localctx = new GraphContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_graph);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(588);
			match(T__39);
			setState(589);
			match(ID);
			setState(590);
			match(T__25);
			setState(591);
			taskDependency();
			setState(592);
			match(T__26);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TaskDependencyContext extends ParserRuleContext {
		public List<TerminalNode> ID() { return getTokens(DungeonDiagnosticsParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(DungeonDiagnosticsParser.ID, i);
		}
		public DependencyAttributeContext dependencyAttribute() {
			return getRuleContext(DependencyAttributeContext.class,0);
		}
		public List<TerminalNode> ARROW() { return getTokens(DungeonDiagnosticsParser.ARROW); }
		public TerminalNode ARROW(int i) {
			return getToken(DungeonDiagnosticsParser.ARROW, i);
		}
		public TaskDependencyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_taskDependency; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterTaskDependency(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitTaskDependency(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitTaskDependency(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TaskDependencyContext taskDependency() throws RecognitionException {
		TaskDependencyContext _localctx = new TaskDependencyContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_taskDependency);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(594);
			match(ID);
			setState(599);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==ARROW) {
				{
				{
				setState(595);
				match(ARROW);
				setState(596);
				match(ID);
				}
				}
				setState(601);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(602);
			match(T__32);
			setState(603);
			dependencyAttribute();
			setState(604);
			match(T__33);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DependencyAttributeContext extends ParserRuleContext {
		public Dependency_typeContext dependency_type() {
			return getRuleContext(Dependency_typeContext.class,0);
		}
		public DependencyAttributeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dependencyAttribute; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterDependencyAttribute(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitDependencyAttribute(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitDependencyAttribute(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DependencyAttributeContext dependencyAttribute() throws RecognitionException {
		DependencyAttributeContext _localctx = new DependencyAttributeContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_dependencyAttribute);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(606);
			match(T__40);
			setState(607);
			match(T__9);
			setState(608);
			dependency_type();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SingleChoiceTaskContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public List<FieldContext> field() {
			return getRuleContexts(FieldContext.class);
		}
		public FieldContext field(int i) {
			return getRuleContext(FieldContext.class,i);
		}
		public SingleChoiceTaskContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleChoiceTask; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterSingleChoiceTask(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitSingleChoiceTask(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitSingleChoiceTask(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleChoiceTaskContext singleChoiceTask() throws RecognitionException {
		SingleChoiceTaskContext _localctx = new SingleChoiceTaskContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_singleChoiceTask);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(610);
			match(T__34);
			setState(611);
			match(ID);
			setState(612);
			match(T__25);
			setState(613);
			field();
			setState(618);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__36) {
				{
				{
				setState(614);
				match(T__36);
				setState(615);
				field();
				}
				}
				setState(620);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(621);
			match(T__26);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MultipleChoiceTaskContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public List<FieldContext> field() {
			return getRuleContexts(FieldContext.class);
		}
		public FieldContext field(int i) {
			return getRuleContext(FieldContext.class,i);
		}
		public MultipleChoiceTaskContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_multipleChoiceTask; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterMultipleChoiceTask(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitMultipleChoiceTask(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitMultipleChoiceTask(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultipleChoiceTaskContext multipleChoiceTask() throws RecognitionException {
		MultipleChoiceTaskContext _localctx = new MultipleChoiceTaskContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_multipleChoiceTask);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(623);
			match(T__35);
			setState(624);
			match(ID);
			setState(625);
			match(T__25);
			setState(626);
			field();
			setState(631);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__36) {
				{
				{
				setState(627);
				match(T__36);
				setState(628);
				field();
				}
				}
				setState(633);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(634);
			match(T__26);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AssignTaskContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public List<FieldContext> field() {
			return getRuleContexts(FieldContext.class);
		}
		public FieldContext field(int i) {
			return getRuleContext(FieldContext.class,i);
		}
		public AssignTaskContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignTask; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterAssignTask(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitAssignTask(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitAssignTask(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignTaskContext assignTask() throws RecognitionException {
		AssignTaskContext _localctx = new AssignTaskContext(_ctx, getState());
		enterRule(_localctx, 112, RULE_assignTask);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(636);
			match(T__56);
			setState(637);
			match(ID);
			setState(638);
			match(T__25);
			setState(639);
			field();
			setState(644);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__36) {
				{
				{
				setState(640);
				match(T__36);
				setState(641);
				field();
				}
				}
				setState(646);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(647);
			match(T__26);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FieldContext extends ParserRuleContext {
		public DescriptionFieldContext descriptionField() {
			return getRuleContext(DescriptionFieldContext.class,0);
		}
		public AnswersFieldContext answersField() {
			return getRuleContext(AnswersFieldContext.class,0);
		}
		public CorrectAnswerIndexFieldContext correctAnswerIndexField() {
			return getRuleContext(CorrectAnswerIndexFieldContext.class,0);
		}
		public CorrectAnswerIndicesFieldContext correctAnswerIndicesField() {
			return getRuleContext(CorrectAnswerIndicesFieldContext.class,0);
		}
		public SolutionFieldContext solutionField() {
			return getRuleContext(SolutionFieldContext.class,0);
		}
		public PointsFieldContext pointsField() {
			return getRuleContext(PointsFieldContext.class,0);
		}
		public PointsToPassFieldContext pointsToPassField() {
			return getRuleContext(PointsToPassFieldContext.class,0);
		}
		public ExplanationFieldContext explanationField() {
			return getRuleContext(ExplanationFieldContext.class,0);
		}
		public GradingFunctionFieldContext gradingFunctionField() {
			return getRuleContext(GradingFunctionFieldContext.class,0);
		}
		public ScenarioBuilderFieldContext scenarioBuilderField() {
			return getRuleContext(ScenarioBuilderFieldContext.class,0);
		}
		public FieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_field; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldContext field() throws RecognitionException {
		FieldContext _localctx = new FieldContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_field);
		try {
			setState(659);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__58:
				enterOuterAlt(_localctx, 1);
				{
				setState(649);
				descriptionField();
				}
				break;
			case T__59:
				enterOuterAlt(_localctx, 2);
				{
				setState(650);
				answersField();
				}
				break;
			case T__60:
				enterOuterAlt(_localctx, 3);
				{
				setState(651);
				correctAnswerIndexField();
				}
				break;
			case T__61:
				enterOuterAlt(_localctx, 4);
				{
				setState(652);
				correctAnswerIndicesField();
				}
				break;
			case T__62:
				enterOuterAlt(_localctx, 5);
				{
				setState(653);
				solutionField();
				}
				break;
			case T__64:
				enterOuterAlt(_localctx, 6);
				{
				setState(654);
				pointsField();
				}
				break;
			case T__65:
				enterOuterAlt(_localctx, 7);
				{
				setState(655);
				pointsToPassField();
				}
				break;
			case T__66:
				enterOuterAlt(_localctx, 8);
				{
				setState(656);
				explanationField();
				}
				break;
			case T__67:
				enterOuterAlt(_localctx, 9);
				{
				setState(657);
				gradingFunctionField();
				}
				break;
			case T__68:
				enterOuterAlt(_localctx, 10);
				{
				setState(658);
				scenarioBuilderField();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DependencyGraphFieldContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public DependencyGraphFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dependencyGraphField; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterDependencyGraphField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitDependencyGraphField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitDependencyGraphField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DependencyGraphFieldContext dependencyGraphField() throws RecognitionException {
		DependencyGraphFieldContext _localctx = new DependencyGraphFieldContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_dependencyGraphField);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(661);
			match(T__57);
			setState(662);
			match(T__10);
			setState(663);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DescriptionFieldContext extends ParserRuleContext {
		public TerminalNode STRING_LITERAL() { return getToken(DungeonDiagnosticsParser.STRING_LITERAL, 0); }
		public DescriptionFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_descriptionField; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterDescriptionField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitDescriptionField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitDescriptionField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DescriptionFieldContext descriptionField() throws RecognitionException {
		DescriptionFieldContext _localctx = new DescriptionFieldContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_descriptionField);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(665);
			match(T__58);
			setState(666);
			match(T__10);
			setState(667);
			match(STRING_LITERAL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AnswersFieldContext extends ParserRuleContext {
		public List<TerminalNode> STRING_LITERAL() { return getTokens(DungeonDiagnosticsParser.STRING_LITERAL); }
		public TerminalNode STRING_LITERAL(int i) {
			return getToken(DungeonDiagnosticsParser.STRING_LITERAL, i);
		}
		public AnswersFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_answersField; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterAnswersField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitAnswersField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitAnswersField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnswersFieldContext answersField() throws RecognitionException {
		AnswersFieldContext _localctx = new AnswersFieldContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_answersField);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(669);
			match(T__59);
			setState(670);
			match(T__10);
			setState(671);
			match(T__32);
			setState(672);
			match(STRING_LITERAL);
			setState(677);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__36) {
				{
				{
				setState(673);
				match(T__36);
				setState(674);
				match(STRING_LITERAL);
				}
				}
				setState(679);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(680);
			match(T__33);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CorrectAnswerIndexFieldContext extends ParserRuleContext {
		public TerminalNode NUM() { return getToken(DungeonDiagnosticsParser.NUM, 0); }
		public CorrectAnswerIndexFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_correctAnswerIndexField; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterCorrectAnswerIndexField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitCorrectAnswerIndexField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitCorrectAnswerIndexField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CorrectAnswerIndexFieldContext correctAnswerIndexField() throws RecognitionException {
		CorrectAnswerIndexFieldContext _localctx = new CorrectAnswerIndexFieldContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_correctAnswerIndexField);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(682);
			match(T__60);
			setState(683);
			match(T__10);
			setState(684);
			match(NUM);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CorrectAnswerIndicesFieldContext extends ParserRuleContext {
		public List<TerminalNode> NUM() { return getTokens(DungeonDiagnosticsParser.NUM); }
		public TerminalNode NUM(int i) {
			return getToken(DungeonDiagnosticsParser.NUM, i);
		}
		public CorrectAnswerIndicesFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_correctAnswerIndicesField; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterCorrectAnswerIndicesField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitCorrectAnswerIndicesField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitCorrectAnswerIndicesField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CorrectAnswerIndicesFieldContext correctAnswerIndicesField() throws RecognitionException {
		CorrectAnswerIndicesFieldContext _localctx = new CorrectAnswerIndicesFieldContext(_ctx, getState());
		enterRule(_localctx, 124, RULE_correctAnswerIndicesField);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(686);
			match(T__61);
			setState(687);
			match(T__10);
			setState(688);
			match(T__32);
			setState(689);
			match(NUM);
			setState(694);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__36) {
				{
				{
				setState(690);
				match(T__36);
				setState(691);
				match(NUM);
				}
				}
				setState(696);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(697);
			match(T__33);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SolutionFieldContext extends ParserRuleContext {
		public List<PairContext> pair() {
			return getRuleContexts(PairContext.class);
		}
		public PairContext pair(int i) {
			return getRuleContext(PairContext.class,i);
		}
		public SolutionFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_solutionField; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterSolutionField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitSolutionField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitSolutionField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SolutionFieldContext solutionField() throws RecognitionException {
		SolutionFieldContext _localctx = new SolutionFieldContext(_ctx, getState());
		enterRule(_localctx, 126, RULE_solutionField);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(699);
			match(T__62);
			setState(700);
			match(T__10);
			setState(701);
			match(T__18);
			setState(702);
			pair();
			setState(707);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__36) {
				{
				{
				setState(703);
				match(T__36);
				setState(704);
				pair();
				}
				}
				setState(709);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(710);
			match(T__16);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PairContext extends ParserRuleContext {
		public List<PairValContext> pairVal() {
			return getRuleContexts(PairValContext.class);
		}
		public PairValContext pairVal(int i) {
			return getRuleContext(PairValContext.class,i);
		}
		public PairContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pair; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterPair(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitPair(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitPair(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PairContext pair() throws RecognitionException {
		PairContext _localctx = new PairContext(_ctx, getState());
		enterRule(_localctx, 128, RULE_pair);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(712);
			match(T__32);
			setState(713);
			pairVal();
			setState(714);
			match(T__36);
			setState(715);
			pairVal();
			setState(716);
			match(T__33);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PairValContext extends ParserRuleContext {
		public TerminalNode STRING_LITERAL() { return getToken(DungeonDiagnosticsParser.STRING_LITERAL, 0); }
		public PairValContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pairVal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterPairVal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitPairVal(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitPairVal(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PairValContext pairVal() throws RecognitionException {
		PairValContext _localctx = new PairValContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_pairVal);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(718);
			_la = _input.LA(1);
			if ( !(_la==T__63 || _la==STRING_LITERAL) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PointsFieldContext extends ParserRuleContext {
		public TerminalNode NUM() { return getToken(DungeonDiagnosticsParser.NUM, 0); }
		public PointsFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pointsField; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterPointsField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitPointsField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitPointsField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PointsFieldContext pointsField() throws RecognitionException {
		PointsFieldContext _localctx = new PointsFieldContext(_ctx, getState());
		enterRule(_localctx, 132, RULE_pointsField);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(720);
			match(T__64);
			setState(721);
			match(T__10);
			setState(722);
			match(NUM);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PointsToPassFieldContext extends ParserRuleContext {
		public TerminalNode NUM() { return getToken(DungeonDiagnosticsParser.NUM, 0); }
		public PointsToPassFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pointsToPassField; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterPointsToPassField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitPointsToPassField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitPointsToPassField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PointsToPassFieldContext pointsToPassField() throws RecognitionException {
		PointsToPassFieldContext _localctx = new PointsToPassFieldContext(_ctx, getState());
		enterRule(_localctx, 134, RULE_pointsToPassField);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(724);
			match(T__65);
			setState(725);
			match(T__10);
			setState(726);
			match(NUM);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExplanationFieldContext extends ParserRuleContext {
		public TerminalNode STRING_LITERAL() { return getToken(DungeonDiagnosticsParser.STRING_LITERAL, 0); }
		public ExplanationFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_explanationField; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterExplanationField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitExplanationField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitExplanationField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExplanationFieldContext explanationField() throws RecognitionException {
		ExplanationFieldContext _localctx = new ExplanationFieldContext(_ctx, getState());
		enterRule(_localctx, 136, RULE_explanationField);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(728);
			match(T__66);
			setState(729);
			match(T__10);
			setState(730);
			match(STRING_LITERAL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class GradingFunctionFieldContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public GradingFunctionFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_gradingFunctionField; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterGradingFunctionField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitGradingFunctionField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitGradingFunctionField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GradingFunctionFieldContext gradingFunctionField() throws RecognitionException {
		GradingFunctionFieldContext _localctx = new GradingFunctionFieldContext(_ctx, getState());
		enterRule(_localctx, 138, RULE_gradingFunctionField);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(732);
			match(T__67);
			setState(733);
			match(T__10);
			setState(734);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ScenarioBuilderFieldContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public ScenarioBuilderFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_scenarioBuilderField; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterScenarioBuilderField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitScenarioBuilderField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitScenarioBuilderField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ScenarioBuilderFieldContext scenarioBuilderField() throws RecognitionException {
		ScenarioBuilderFieldContext _localctx = new ScenarioBuilderFieldContext(_ctx, getState());
		enterRule(_localctx, 140, RULE_scenarioBuilderField);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(736);
			match(T__68);
			setState(737);
			match(T__10);
			setState(738);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Entity_typeContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public ComponentListContext componentList() {
			return getRuleContext(ComponentListContext.class,0);
		}
		public Entity_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_entity_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterEntity_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitEntity_type(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitEntity_type(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Entity_typeContext entity_type() throws RecognitionException {
		Entity_typeContext _localctx = new Entity_typeContext(_ctx, getState());
		enterRule(_localctx, 142, RULE_entity_type);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(740);
			match(T__37);
			setState(741);
			match(ID);
			setState(742);
			match(T__25);
			setState(743);
			componentList();
			setState(744);
			match(T__26);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ComponentListContext extends ParserRuleContext {
		public List<ComponentContext> component() {
			return getRuleContexts(ComponentContext.class);
		}
		public ComponentContext component(int i) {
			return getRuleContext(ComponentContext.class,i);
		}
		public ComponentListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_componentList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterComponentList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitComponentList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitComponentList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComponentListContext componentList() throws RecognitionException {
		ComponentListContext _localctx = new ComponentListContext(_ctx, getState());
		enterRule(_localctx, 144, RULE_componentList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(746);
			component();
			setState(751);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__36) {
				{
				{
				setState(747);
				match(T__36);
				setState(748);
				component();
				}
				}
				setState(753);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ComponentContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public AttributeListContext attributeList() {
			return getRuleContext(AttributeListContext.class,0);
		}
		public ComponentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_component; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterComponent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitComponent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitComponent(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComponentContext component() throws RecognitionException {
		ComponentContext _localctx = new ComponentContext(_ctx, getState());
		enterRule(_localctx, 146, RULE_component);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(754);
			match(ID);
			setState(755);
			match(T__25);
			setState(757);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(756);
				attributeList();
				}
			}

			setState(759);
			match(T__26);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AttributeListContext extends ParserRuleContext {
		public List<AttributeContext> attribute() {
			return getRuleContexts(AttributeContext.class);
		}
		public AttributeContext attribute(int i) {
			return getRuleContext(AttributeContext.class,i);
		}
		public AttributeListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attributeList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterAttributeList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitAttributeList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitAttributeList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttributeListContext attributeList() throws RecognitionException {
		AttributeListContext _localctx = new AttributeListContext(_ctx, getState());
		enterRule(_localctx, 148, RULE_attributeList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(761);
			attribute();
			setState(766);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__36) {
				{
				{
				setState(762);
				match(T__36);
				setState(763);
				attribute();
				}
				}
				setState(768);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AttributeContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public AttributeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attribute; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterAttribute(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitAttribute(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitAttribute(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttributeContext attribute() throws RecognitionException {
		AttributeContext _localctx = new AttributeContext(_ctx, getState());
		enterRule(_localctx, 150, RULE_attribute);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(769);
			match(ID);
			setState(770);
			match(T__10);
			setState(771);
			value();
			setState(773);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,69,_ctx) ) {
			case 1:
				{
				setState(772);
				match(T__36);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ValueContext extends ParserRuleContext {
		public TerminalNode STRING_LITERAL() { return getToken(DungeonDiagnosticsParser.STRING_LITERAL, 0); }
		public TerminalNode NUM_DEC() { return getToken(DungeonDiagnosticsParser.NUM_DEC, 0); }
		public TerminalNode NUM() { return getToken(DungeonDiagnosticsParser.NUM, 0); }
		public TerminalNode ID() { return getToken(DungeonDiagnosticsParser.ID, 0); }
		public ValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).enterValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DungeonDiagnosticsListener ) ((DungeonDiagnosticsListener)listener).exitValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDiagnosticsVisitor ) return ((DungeonDiagnosticsVisitor<? extends T>)visitor).visitValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValueContext value() throws RecognitionException {
		ValueContext _localctx = new ValueContext(_ctx, getState());
		enterRule(_localctx, 152, RULE_value);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(775);
			_la = _input.LA(1);
			if ( !(((((_la - 74)) & ~0x3f) == 0 && ((1L << (_la - 74)) & 71L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 10:
			return logic_or_sempred((Logic_orContext)_localctx, predIndex);
		case 11:
			return logic_and_sempred((Logic_andContext)_localctx, predIndex);
		case 12:
			return equality_sempred((EqualityContext)_localctx, predIndex);
		case 13:
			return comparison_sempred((ComparisonContext)_localctx, predIndex);
		case 14:
			return term_sempred((TermContext)_localctx, predIndex);
		case 15:
			return factor_sempred((FactorContext)_localctx, predIndex);
		case 25:
			return type_decl_sempred((Type_declContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean logic_or_sempred(Logic_orContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean logic_and_sempred(Logic_andContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean equality_sempred(EqualityContext _localctx, int predIndex) {
		switch (predIndex) {
		case 2:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean comparison_sempred(ComparisonContext _localctx, int predIndex) {
		switch (predIndex) {
		case 3:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean term_sempred(TermContext _localctx, int predIndex) {
		switch (predIndex) {
		case 4:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean factor_sempred(FactorContext _localctx, int predIndex) {
		switch (predIndex) {
		case 5:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean type_decl_sempred(Type_declContext _localctx, int predIndex) {
		switch (predIndex) {
		case 6:
			return precpred(_ctx, 5);
		case 7:
			return precpred(_ctx, 4);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001P\u030a\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b"+
		"\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e"+
		"\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0002\"\u0007\"\u0002"+
		"#\u0007#\u0002$\u0007$\u0002%\u0007%\u0002&\u0007&\u0002\'\u0007\'\u0002"+
		"(\u0007(\u0002)\u0007)\u0002*\u0007*\u0002+\u0007+\u0002,\u0007,\u0002"+
		"-\u0007-\u0002.\u0007.\u0002/\u0007/\u00020\u00070\u00021\u00071\u0002"+
		"2\u00072\u00023\u00073\u00024\u00074\u00025\u00075\u00026\u00076\u0002"+
		"7\u00077\u00028\u00078\u00029\u00079\u0002:\u0007:\u0002;\u0007;\u0002"+
		"<\u0007<\u0002=\u0007=\u0002>\u0007>\u0002?\u0007?\u0002@\u0007@\u0002"+
		"A\u0007A\u0002B\u0007B\u0002C\u0007C\u0002D\u0007D\u0002E\u0007E\u0002"+
		"F\u0007F\u0002G\u0007G\u0002H\u0007H\u0002I\u0007I\u0002J\u0007J\u0002"+
		"K\u0007K\u0002L\u0007L\u0001\u0000\u0005\u0000\u009c\b\u0000\n\u0000\f"+
		"\u0000\u009f\t\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0003\u0001\u00ae\b\u0001\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0003\u0002\u00b4\b\u0002\u0001\u0002\u0001"+
		"\u0002\u0003\u0002\u00b8\b\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0003\u0003\u00c4\b\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004\u00da"+
		"\b\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0003\u0005\u00e8\b\u0005\u0001\u0006\u0001\u0006\u0003\u0006\u00ec"+
		"\b\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u00f1\b\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u00f6\b\u0007\u0003\u0007"+
		"\u00f8\b\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0003\b\u00ff\b"+
		"\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003"+
		"\t\u0109\b\t\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0005\n\u0111"+
		"\b\n\n\n\f\n\u0114\t\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0005\u000b\u011c\b\u000b\n\u000b\f\u000b\u011f"+
		"\t\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u0127"+
		"\b\f\u0001\f\u0005\f\u012a\b\f\n\f\f\f\u012d\t\f\u0001\r\u0001\r\u0001"+
		"\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0003\r\u0137\b\r\u0001\r\u0005"+
		"\r\u013a\b\r\n\r\f\r\u013d\t\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001"+
		"\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u0145\b\u000e\u0001\u000e\u0005"+
		"\u000e\u0148\b\u000e\n\u000e\f\u000e\u014b\t\u000e\u0001\u000f\u0001\u000f"+
		"\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0003\u000f\u0153\b\u000f"+
		"\u0001\u000f\u0005\u000f\u0156\b\u000f\n\u000f\f\u000f\u0159\t\u000f\u0001"+
		"\u0010\u0001\u0010\u0003\u0010\u015d\b\u0010\u0001\u0010\u0001\u0010\u0003"+
		"\u0010\u0161\b\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0003\u0011\u0166"+
		"\b\u0011\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0003\u0012\u016c"+
		"\b\u0012\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0001"+
		"\u0013\u0003\u0013\u0174\b\u0013\u0001\u0014\u0001\u0014\u0003\u0014\u0178"+
		"\b\u0014\u0001\u0014\u0001\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0003\u0015\u0180\b\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0017\u0001\u0017\u0001\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001"+
		"\u0019\u0001\u0019\u0001\u0019\u0003\u0019\u0194\b\u0019\u0001\u0019\u0001"+
		"\u0019\u0001\u0019\u0001\u0019\u0005\u0019\u019a\b\u0019\n\u0019\f\u0019"+
		"\u019d\t\u0019\u0001\u001a\u0001\u001a\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0003\u001b\u01a6\b\u001b\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0003\u001c\u01ac\b\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0003\u001d\u01b4\b\u001d"+
		"\u0001\u001d\u0001\u001d\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e"+
		"\u0001\u001e\u0003\u001e\u01bd\b\u001e\u0001\u001f\u0001\u001f\u0001\u001f"+
		"\u0001\u001f\u0003\u001f\u01c3\b\u001f\u0001\u001f\u0003\u001f\u01c6\b"+
		"\u001f\u0001 \u0001 \u0001 \u0001 \u0003 \u01cc\b \u0001 \u0001 \u0001"+
		"!\u0001!\u0001!\u0001!\u0001!\u0003!\u01d5\b!\u0001\"\u0001\"\u0001\""+
		"\u0001\"\u0001#\u0001#\u0001#\u0001#\u0001#\u0003#\u01e0\b#\u0001$\u0001"+
		"$\u0001$\u0001$\u0001%\u0001%\u0003%\u01e8\b%\u0001%\u0001%\u0001&\u0001"+
		"&\u0003&\u01ee\b&\u0001&\u0001&\u0001\'\u0001\'\u0001\'\u0001\'\u0001"+
		"\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0003\'\u01fd\b\'\u0001"+
		"(\u0001(\u0001(\u0001(\u0003(\u0203\b(\u0001(\u0001(\u0001)\u0001)\u0003"+
		")\u0209\b)\u0001)\u0003)\u020c\b)\u0001*\u0001*\u0003*\u0210\b*\u0001"+
		"+\u0001+\u0004+\u0214\b+\u000b+\f+\u0215\u0001+\u0003+\u0219\b+\u0001"+
		",\u0001,\u0001,\u0001,\u0003,\u021f\b,\u0001-\u0001-\u0001-\u0001.\u0001"+
		".\u0003.\u0226\b.\u0001/\u0001/\u0004/\u022a\b/\u000b/\f/\u022b\u0001"+
		"/\u0001/\u00010\u00010\u00010\u00010\u00030\u0234\b0\u00010\u00010\u0001"+
		"0\u00010\u00030\u023a\b0\u00030\u023c\b0\u00011\u00011\u00011\u00011\u0001"+
		"1\u00011\u00011\u00031\u0245\b1\u00012\u00012\u00012\u00012\u00012\u0001"+
		"2\u00013\u00013\u00013\u00013\u00013\u00013\u00014\u00014\u00014\u0005"+
		"4\u0256\b4\n4\f4\u0259\t4\u00014\u00014\u00014\u00014\u00015\u00015\u0001"+
		"5\u00015\u00016\u00016\u00016\u00016\u00016\u00016\u00056\u0269\b6\n6"+
		"\f6\u026c\t6\u00016\u00016\u00017\u00017\u00017\u00017\u00017\u00017\u0005"+
		"7\u0276\b7\n7\f7\u0279\t7\u00017\u00017\u00018\u00018\u00018\u00018\u0001"+
		"8\u00018\u00058\u0283\b8\n8\f8\u0286\t8\u00018\u00018\u00019\u00019\u0001"+
		"9\u00019\u00019\u00019\u00019\u00019\u00019\u00019\u00039\u0294\b9\u0001"+
		":\u0001:\u0001:\u0001:\u0001;\u0001;\u0001;\u0001;\u0001<\u0001<\u0001"+
		"<\u0001<\u0001<\u0001<\u0005<\u02a4\b<\n<\f<\u02a7\t<\u0001<\u0001<\u0001"+
		"=\u0001=\u0001=\u0001=\u0001>\u0001>\u0001>\u0001>\u0001>\u0001>\u0005"+
		">\u02b5\b>\n>\f>\u02b8\t>\u0001>\u0001>\u0001?\u0001?\u0001?\u0001?\u0001"+
		"?\u0001?\u0005?\u02c2\b?\n?\f?\u02c5\t?\u0001?\u0001?\u0001@\u0001@\u0001"+
		"@\u0001@\u0001@\u0001@\u0001A\u0001A\u0001B\u0001B\u0001B\u0001B\u0001"+
		"C\u0001C\u0001C\u0001C\u0001D\u0001D\u0001D\u0001D\u0001E\u0001E\u0001"+
		"E\u0001E\u0001F\u0001F\u0001F\u0001F\u0001G\u0001G\u0001G\u0001G\u0001"+
		"G\u0001G\u0001H\u0001H\u0001H\u0005H\u02ee\bH\nH\fH\u02f1\tH\u0001I\u0001"+
		"I\u0001I\u0003I\u02f6\bI\u0001I\u0001I\u0001J\u0001J\u0001J\u0005J\u02fd"+
		"\bJ\nJ\fJ\u0300\tJ\u0001K\u0001K\u0001K\u0001K\u0003K\u0306\bK\u0001L"+
		"\u0001L\u0001L\u0000\u0007\u0014\u0016\u0018\u001a\u001c\u001e2M\u0000"+
		"\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c"+
		"\u001e \"$&(*,.02468:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprtvxz|~\u0080\u0082\u0084"+
		"\u0086\u0088\u008a\u008c\u008e\u0090\u0092\u0094\u0096\u0098\u0000\u000b"+
		"\u0001\u0000#$\u0002\u0000\u0004\u0004%%\u0001\u0000*+\u0001\u0000,-\u0001"+
		"\u0000./\u0001\u000001\u0001\u000023\u0001\u000045\u0001\u000067\u0002"+
		"\u0000@@PP\u0002\u0000JLPP\u032a\u0000\u009d\u0001\u0000\u0000\u0000\u0002"+
		"\u00ad\u0001\u0000\u0000\u0000\u0004\u00af\u0001\u0000\u0000\u0000\u0006"+
		"\u00c3\u0001\u0000\u0000\u0000\b\u00d9\u0001\u0000\u0000\u0000\n\u00e7"+
		"\u0001\u0000\u0000\u0000\f\u00e9\u0001\u0000\u0000\u0000\u000e\u00f7\u0001"+
		"\u0000\u0000\u0000\u0010\u00fe\u0001\u0000\u0000\u0000\u0012\u0108\u0001"+
		"\u0000\u0000\u0000\u0014\u010a\u0001\u0000\u0000\u0000\u0016\u0115\u0001"+
		"\u0000\u0000\u0000\u0018\u0120\u0001\u0000\u0000\u0000\u001a\u012e\u0001"+
		"\u0000\u0000\u0000\u001c\u013e\u0001\u0000\u0000\u0000\u001e\u014c\u0001"+
		"\u0000\u0000\u0000 \u0160\u0001\u0000\u0000\u0000\"\u0162\u0001\u0000"+
		"\u0000\u0000$\u0169\u0001\u0000\u0000\u0000&\u0173\u0001\u0000\u0000\u0000"+
		"(\u0175\u0001\u0000\u0000\u0000*\u017b\u0001\u0000\u0000\u0000,\u0181"+
		"\u0001\u0000\u0000\u0000.\u0184\u0001\u0000\u0000\u00000\u0187\u0001\u0000"+
		"\u0000\u00002\u0193\u0001\u0000\u0000\u00004\u019e\u0001\u0000\u0000\u0000"+
		"6\u01a5\u0001\u0000\u0000\u00008\u01a7\u0001\u0000\u0000\u0000:\u01af"+
		"\u0001\u0000\u0000\u0000<\u01bc\u0001\u0000\u0000\u0000>\u01c5\u0001\u0000"+
		"\u0000\u0000@\u01c7\u0001\u0000\u0000\u0000B\u01d4\u0001\u0000\u0000\u0000"+
		"D\u01d6\u0001\u0000\u0000\u0000F\u01df\u0001\u0000\u0000\u0000H\u01e1"+
		"\u0001\u0000\u0000\u0000J\u01e5\u0001\u0000\u0000\u0000L\u01eb\u0001\u0000"+
		"\u0000\u0000N\u01fc\u0001\u0000\u0000\u0000P\u01fe\u0001\u0000\u0000\u0000"+
		"R\u0206\u0001\u0000\u0000\u0000T\u020f\u0001\u0000\u0000\u0000V\u0211"+
		"\u0001\u0000\u0000\u0000X\u021e\u0001\u0000\u0000\u0000Z\u0220\u0001\u0000"+
		"\u0000\u0000\\\u0223\u0001\u0000\u0000\u0000^\u0227\u0001\u0000\u0000"+
		"\u0000`\u023b\u0001\u0000\u0000\u0000b\u0244\u0001\u0000\u0000\u0000d"+
		"\u0246\u0001\u0000\u0000\u0000f\u024c\u0001\u0000\u0000\u0000h\u0252\u0001"+
		"\u0000\u0000\u0000j\u025e\u0001\u0000\u0000\u0000l\u0262\u0001\u0000\u0000"+
		"\u0000n\u026f\u0001\u0000\u0000\u0000p\u027c\u0001\u0000\u0000\u0000r"+
		"\u0293\u0001\u0000\u0000\u0000t\u0295\u0001\u0000\u0000\u0000v\u0299\u0001"+
		"\u0000\u0000\u0000x\u029d\u0001\u0000\u0000\u0000z\u02aa\u0001\u0000\u0000"+
		"\u0000|\u02ae\u0001\u0000\u0000\u0000~\u02bb\u0001\u0000\u0000\u0000\u0080"+
		"\u02c8\u0001\u0000\u0000\u0000\u0082\u02ce\u0001\u0000\u0000\u0000\u0084"+
		"\u02d0\u0001\u0000\u0000\u0000\u0086\u02d4\u0001\u0000\u0000\u0000\u0088"+
		"\u02d8\u0001\u0000\u0000\u0000\u008a\u02dc\u0001\u0000\u0000\u0000\u008c"+
		"\u02e0\u0001\u0000\u0000\u0000\u008e\u02e4\u0001\u0000\u0000\u0000\u0090"+
		"\u02ea\u0001\u0000\u0000\u0000\u0092\u02f2\u0001\u0000\u0000\u0000\u0094"+
		"\u02f9\u0001\u0000\u0000\u0000\u0096\u0301\u0001\u0000\u0000\u0000\u0098"+
		"\u0307\u0001\u0000\u0000\u0000\u009a\u009c\u0003\u0002\u0001\u0000\u009b"+
		"\u009a\u0001\u0000\u0000\u0000\u009c\u009f\u0001\u0000\u0000\u0000\u009d"+
		"\u009b\u0001\u0000\u0000\u0000\u009d\u009e\u0001\u0000\u0000\u0000\u009e"+
		"\u00a0\u0001\u0000\u0000\u0000\u009f\u009d\u0001\u0000\u0000\u0000\u00a0"+
		"\u00a1\u0005\u0000\u0000\u0001\u00a1\u0001\u0001\u0000\u0000\u0000\u00a2"+
		"\u00ae\u0003P(\u0000\u00a3\u00ae\u0003@ \u0000\u00a4\u00ae\u00038\u001c"+
		"\u0000\u00a5\u00ae\u0003:\u001d\u0000\u00a6\u00ae\u0003\u0004\u0002\u0000"+
		"\u00a7\u00ae\u0003l6\u0000\u00a8\u00ae\u0003n7\u0000\u00a9\u00ae\u0003"+
		"p8\u0000\u00aa\u00ae\u0003d2\u0000\u00ab\u00ae\u0003f3\u0000\u00ac\u00ae"+
		"\u0003\u008eG\u0000\u00ad\u00a2\u0001\u0000\u0000\u0000\u00ad\u00a3\u0001"+
		"\u0000\u0000\u0000\u00ad\u00a4\u0001\u0000\u0000\u0000\u00ad\u00a5\u0001"+
		"\u0000\u0000\u0000\u00ad\u00a6\u0001\u0000\u0000\u0000\u00ad\u00a7\u0001"+
		"\u0000\u0000\u0000\u00ad\u00a8\u0001\u0000\u0000\u0000\u00ad\u00a9\u0001"+
		"\u0000\u0000\u0000\u00ad\u00aa\u0001\u0000\u0000\u0000\u00ad\u00ab\u0001"+
		"\u0000\u0000\u0000\u00ad\u00ac\u0001\u0000\u0000\u0000\u00ae\u0003\u0001"+
		"\u0000\u0000\u0000\u00af\u00b0\u0005\u0001\u0000\u0000\u00b0\u00b1\u0005"+
		"J\u0000\u0000\u00b1\u00b3\u0005\u0002\u0000\u0000\u00b2\u00b4\u00036\u001b"+
		"\u0000\u00b3\u00b2\u0001\u0000\u0000\u0000\u00b3\u00b4\u0001\u0000\u0000"+
		"\u0000\u00b4\u00b5\u0001\u0000\u0000\u0000\u00b5\u00b7\u0005\u0003\u0000"+
		"\u0000\u00b6\u00b8\u0003.\u0017\u0000\u00b7\u00b6\u0001\u0000\u0000\u0000"+
		"\u00b7\u00b8\u0001\u0000\u0000\u0000\u00b8\u00b9\u0001\u0000\u0000\u0000"+
		"\u00b9\u00ba\u0003$\u0012\u0000\u00ba\u0005\u0001\u0000\u0000\u0000\u00bb"+
		"\u00bc\u0003\f\u0006\u0000\u00bc\u00bd\u0005\u0004\u0000\u0000\u00bd\u00c4"+
		"\u0001\u0000\u0000\u0000\u00be\u00c4\u0003\n\u0005\u0000\u00bf\u00c4\u0003"+
		"$\u0012\u0000\u00c0\u00c4\u0003*\u0015\u0000\u00c1\u00c4\u0003(\u0014"+
		"\u0000\u00c2\u00c4\u0003\b\u0004\u0000\u00c3\u00bb\u0001\u0000\u0000\u0000"+
		"\u00c3\u00be\u0001\u0000\u0000\u0000\u00c3\u00bf\u0001\u0000\u0000\u0000"+
		"\u00c3\u00c0\u0001\u0000\u0000\u0000\u00c3\u00c1\u0001\u0000\u0000\u0000"+
		"\u00c3\u00c2\u0001\u0000\u0000\u0000\u00c4\u0007\u0001\u0000\u0000\u0000"+
		"\u00c5\u00c6\u0005\u0005\u0000\u0000\u00c6\u00c7\u00032\u0019\u0000\u00c7"+
		"\u00c8\u0005J\u0000\u0000\u00c8\u00c9\u0005\u0006\u0000\u0000\u00c9\u00ca"+
		"\u0003\f\u0006\u0000\u00ca\u00cb\u0003\u0006\u0003\u0000\u00cb\u00da\u0001"+
		"\u0000\u0000\u0000\u00cc\u00cd\u0005\u0005\u0000\u0000\u00cd\u00ce\u0003"+
		"2\u0019\u0000\u00ce\u00cf\u0005J\u0000\u0000\u00cf\u00d0\u0005\u0006\u0000"+
		"\u0000\u00d0\u00d1\u0003\f\u0006\u0000\u00d1\u00d2\u0005\u0007\u0000\u0000"+
		"\u00d2\u00d3\u0005J\u0000\u0000\u00d3\u00d4\u0003\u0006\u0003\u0000\u00d4"+
		"\u00da\u0001\u0000\u0000\u0000\u00d5\u00d6\u0005\b\u0000\u0000\u00d6\u00d7"+
		"\u0003\f\u0006\u0000\u00d7\u00d8\u0003\u0006\u0003\u0000\u00d8\u00da\u0001"+
		"\u0000\u0000\u0000\u00d9\u00c5\u0001\u0000\u0000\u0000\u00d9\u00cc\u0001"+
		"\u0000\u0000\u0000\u00d9\u00d5\u0001\u0000\u0000\u0000\u00da\t\u0001\u0000"+
		"\u0000\u0000\u00db\u00dc\u0005\t\u0000\u0000\u00dc\u00dd\u0005J\u0000"+
		"\u0000\u00dd\u00de\u0005\n\u0000\u0000\u00de\u00df\u0003\f\u0006\u0000"+
		"\u00df\u00e0\u0005\u0004\u0000\u0000\u00e0\u00e8\u0001\u0000\u0000\u0000"+
		"\u00e1\u00e2\u0005\t\u0000\u0000\u00e2\u00e3\u0005J\u0000\u0000\u00e3"+
		"\u00e4\u0005\u000b\u0000\u0000\u00e4\u00e5\u00032\u0019\u0000\u00e5\u00e6"+
		"\u0005\u0004\u0000\u0000\u00e6\u00e8\u0001\u0000\u0000\u0000\u00e7\u00db"+
		"\u0001\u0000\u0000\u0000\u00e7\u00e1\u0001\u0000\u0000\u0000\u00e8\u000b"+
		"\u0001\u0000\u0000\u0000\u00e9\u00eb\u0003\u0010\b\u0000\u00ea\u00ec\u0003"+
		"\u000e\u0007\u0000\u00eb\u00ea\u0001\u0000\u0000\u0000\u00eb\u00ec\u0001"+
		"\u0000\u0000\u0000\u00ec\r\u0001\u0000\u0000\u0000\u00ed\u00ee\u0005\f"+
		"\u0000\u0000\u00ee\u00f0\u0003\"\u0011\u0000\u00ef\u00f1\u0003\u000e\u0007"+
		"\u0000\u00f0\u00ef\u0001\u0000\u0000\u0000\u00f0\u00f1\u0001\u0000\u0000"+
		"\u0000\u00f1\u00f8\u0001\u0000\u0000\u0000\u00f2\u00f3\u0005\f\u0000\u0000"+
		"\u00f3\u00f5\u0005J\u0000\u0000\u00f4\u00f6\u0003\u000e\u0007\u0000\u00f5"+
		"\u00f4\u0001\u0000\u0000\u0000\u00f5\u00f6\u0001\u0000\u0000\u0000\u00f6"+
		"\u00f8\u0001\u0000\u0000\u0000\u00f7\u00ed\u0001\u0000\u0000\u0000\u00f7"+
		"\u00f2\u0001\u0000\u0000\u0000\u00f8\u000f\u0001\u0000\u0000\u0000\u00f9"+
		"\u00fa\u0003\u0012\t\u0000\u00fa\u00fb\u0005\n\u0000\u0000\u00fb\u00fc"+
		"\u0003\f\u0006\u0000\u00fc\u00ff\u0001\u0000\u0000\u0000\u00fd\u00ff\u0003"+
		"\u0014\n\u0000\u00fe\u00f9\u0001\u0000\u0000\u0000\u00fe\u00fd\u0001\u0000"+
		"\u0000\u0000\u00ff\u0011\u0001\u0000\u0000\u0000\u0100\u0101\u0003\"\u0011"+
		"\u0000\u0101\u0102\u0005\f\u0000\u0000\u0102\u0103\u0003\u0012\t\u0000"+
		"\u0103\u0109\u0001\u0000\u0000\u0000\u0104\u0105\u0005J\u0000\u0000\u0105"+
		"\u0106\u0005\f\u0000\u0000\u0106\u0109\u0003\u0012\t\u0000\u0107\u0109"+
		"\u0005J\u0000\u0000\u0108\u0100\u0001\u0000\u0000\u0000\u0108\u0104\u0001"+
		"\u0000\u0000\u0000\u0108\u0107\u0001\u0000\u0000\u0000\u0109\u0013\u0001"+
		"\u0000\u0000\u0000\u010a\u010b\u0006\n\uffff\uffff\u0000\u010b\u010c\u0003"+
		"\u0016\u000b\u0000\u010c\u0112\u0001\u0000\u0000\u0000\u010d\u010e\n\u0002"+
		"\u0000\u0000\u010e\u010f\u0005\r\u0000\u0000\u010f\u0111\u0003\u0016\u000b"+
		"\u0000\u0110\u010d\u0001\u0000\u0000\u0000\u0111\u0114\u0001\u0000\u0000"+
		"\u0000\u0112\u0110\u0001\u0000\u0000\u0000\u0112\u0113\u0001\u0000\u0000"+
		"\u0000\u0113\u0015\u0001\u0000\u0000\u0000\u0114\u0112\u0001\u0000\u0000"+
		"\u0000\u0115\u0116\u0006\u000b\uffff\uffff\u0000\u0116\u0117\u0003\u0018"+
		"\f\u0000\u0117\u011d\u0001\u0000\u0000\u0000\u0118\u0119\n\u0002\u0000"+
		"\u0000\u0119\u011a\u0005\u000e\u0000\u0000\u011a\u011c\u0003\u0018\f\u0000"+
		"\u011b\u0118\u0001\u0000\u0000\u0000\u011c\u011f\u0001\u0000\u0000\u0000"+
		"\u011d\u011b\u0001\u0000\u0000\u0000\u011d\u011e\u0001\u0000\u0000\u0000"+
		"\u011e\u0017\u0001\u0000\u0000\u0000\u011f\u011d\u0001\u0000\u0000\u0000"+
		"\u0120\u0121\u0006\f\uffff\uffff\u0000\u0121\u0122\u0003\u001a\r\u0000"+
		"\u0122\u012b\u0001\u0000\u0000\u0000\u0123\u0126\n\u0002\u0000\u0000\u0124"+
		"\u0127\u0005\u000f\u0000\u0000\u0125\u0127\u0005\u0010\u0000\u0000\u0126"+
		"\u0124\u0001\u0000\u0000\u0000\u0126\u0125\u0001\u0000\u0000\u0000\u0127"+
		"\u0128\u0001\u0000\u0000\u0000\u0128\u012a\u0003\u001a\r\u0000\u0129\u0123"+
		"\u0001\u0000\u0000\u0000\u012a\u012d\u0001\u0000\u0000\u0000\u012b\u0129"+
		"\u0001\u0000\u0000\u0000\u012b\u012c\u0001\u0000\u0000\u0000\u012c\u0019"+
		"\u0001\u0000\u0000\u0000\u012d\u012b\u0001\u0000\u0000\u0000\u012e\u012f"+
		"\u0006\r\uffff\uffff\u0000\u012f\u0130\u0003\u001c\u000e\u0000\u0130\u013b"+
		"\u0001\u0000\u0000\u0000\u0131\u0136\n\u0002\u0000\u0000\u0132\u0137\u0005"+
		"\u0011\u0000\u0000\u0133\u0137\u0005\u0012\u0000\u0000\u0134\u0137\u0005"+
		"\u0013\u0000\u0000\u0135\u0137\u0005\u0014\u0000\u0000\u0136\u0132\u0001"+
		"\u0000\u0000\u0000\u0136\u0133\u0001\u0000\u0000\u0000\u0136\u0134\u0001"+
		"\u0000\u0000\u0000\u0136\u0135\u0001\u0000\u0000\u0000\u0137\u0138\u0001"+
		"\u0000\u0000\u0000\u0138\u013a\u0003\u001c\u000e\u0000\u0139\u0131\u0001"+
		"\u0000\u0000\u0000\u013a\u013d\u0001\u0000\u0000\u0000\u013b\u0139\u0001"+
		"\u0000\u0000\u0000\u013b\u013c\u0001\u0000\u0000\u0000\u013c\u001b\u0001"+
		"\u0000\u0000\u0000\u013d\u013b\u0001\u0000\u0000\u0000\u013e\u013f\u0006"+
		"\u000e\uffff\uffff\u0000\u013f\u0140\u0003\u001e\u000f\u0000\u0140\u0149"+
		"\u0001\u0000\u0000\u0000\u0141\u0144\n\u0002\u0000\u0000\u0142\u0145\u0005"+
		"\u0015\u0000\u0000\u0143\u0145\u0005\u0016\u0000\u0000\u0144\u0142\u0001"+
		"\u0000\u0000\u0000\u0144\u0143\u0001\u0000\u0000\u0000\u0145\u0146\u0001"+
		"\u0000\u0000\u0000\u0146\u0148\u0003\u001e\u000f\u0000\u0147\u0141\u0001"+
		"\u0000\u0000\u0000\u0148\u014b\u0001\u0000\u0000\u0000\u0149\u0147\u0001"+
		"\u0000\u0000\u0000\u0149\u014a\u0001\u0000\u0000\u0000\u014a\u001d\u0001"+
		"\u0000\u0000\u0000\u014b\u0149\u0001\u0000\u0000\u0000\u014c\u014d\u0006"+
		"\u000f\uffff\uffff\u0000\u014d\u014e\u0003 \u0010\u0000\u014e\u0157\u0001"+
		"\u0000\u0000\u0000\u014f\u0152\n\u0002\u0000\u0000\u0150\u0153\u0005\u0017"+
		"\u0000\u0000\u0151\u0153\u0005\u0018\u0000\u0000\u0152\u0150\u0001\u0000"+
		"\u0000\u0000\u0152\u0151\u0001\u0000\u0000\u0000\u0153\u0154\u0001\u0000"+
		"\u0000\u0000\u0154\u0156\u0003 \u0010\u0000\u0155\u014f\u0001\u0000\u0000"+
		"\u0000\u0156\u0159\u0001\u0000\u0000\u0000\u0157\u0155\u0001\u0000\u0000"+
		"\u0000\u0157\u0158\u0001\u0000\u0000\u0000\u0158\u001f\u0001\u0000\u0000"+
		"\u0000\u0159\u0157\u0001\u0000\u0000\u0000\u015a\u015d\u0005\u0019\u0000"+
		"\u0000\u015b\u015d\u0005\u0015\u0000\u0000\u015c\u015a\u0001\u0000\u0000"+
		"\u0000\u015c\u015b\u0001\u0000\u0000\u0000\u015d\u015e\u0001\u0000\u0000"+
		"\u0000\u015e\u0161\u0003 \u0010\u0000\u015f\u0161\u0003N\'\u0000\u0160"+
		"\u015c\u0001\u0000\u0000\u0000\u0160\u015f\u0001\u0000\u0000\u0000\u0161"+
		"!\u0001\u0000\u0000\u0000\u0162\u0163\u0005J\u0000\u0000\u0163\u0165\u0005"+
		"\u0002\u0000\u0000\u0164\u0166\u0003F#\u0000\u0165\u0164\u0001\u0000\u0000"+
		"\u0000\u0165\u0166\u0001\u0000\u0000\u0000\u0166\u0167\u0001\u0000\u0000"+
		"\u0000\u0167\u0168\u0005\u0003\u0000\u0000\u0168#\u0001\u0000\u0000\u0000"+
		"\u0169\u016b\u0005\u001a\u0000\u0000\u016a\u016c\u0003&\u0013\u0000\u016b"+
		"\u016a\u0001\u0000\u0000\u0000\u016b\u016c\u0001\u0000\u0000\u0000\u016c"+
		"\u016d\u0001\u0000\u0000\u0000\u016d\u016e\u0005\u001b\u0000\u0000\u016e"+
		"%\u0001\u0000\u0000\u0000\u016f\u0170\u0003\u0006\u0003\u0000\u0170\u0171"+
		"\u0003&\u0013\u0000\u0171\u0174\u0001\u0000\u0000\u0000\u0172\u0174\u0003"+
		"\u0006\u0003\u0000\u0173\u016f\u0001\u0000\u0000\u0000\u0173\u0172\u0001"+
		"\u0000\u0000\u0000\u0174\'\u0001\u0000\u0000\u0000\u0175\u0177\u0005\u001c"+
		"\u0000\u0000\u0176\u0178\u0003\f\u0006\u0000\u0177\u0176\u0001\u0000\u0000"+
		"\u0000\u0177\u0178\u0001\u0000\u0000\u0000\u0178\u0179\u0001\u0000\u0000"+
		"\u0000\u0179\u017a\u0005\u0004\u0000\u0000\u017a)\u0001\u0000\u0000\u0000"+
		"\u017b\u017c\u0005\u001d\u0000\u0000\u017c\u017d\u0003\f\u0006\u0000\u017d"+
		"\u017f\u0003\u0006\u0003\u0000\u017e\u0180\u0003,\u0016\u0000\u017f\u017e"+
		"\u0001\u0000\u0000\u0000\u017f\u0180\u0001\u0000\u0000\u0000\u0180+\u0001"+
		"\u0000\u0000\u0000\u0181\u0182\u0005\u001e\u0000\u0000\u0182\u0183\u0003"+
		"\u0006\u0003\u0000\u0183-\u0001\u0000\u0000\u0000\u0184\u0185\u0005G\u0000"+
		"\u0000\u0185\u0186\u00032\u0019\u0000\u0186/\u0001\u0000\u0000\u0000\u0187"+
		"\u0188\u00032\u0019\u0000\u0188\u0189\u0005J\u0000\u0000\u01891\u0001"+
		"\u0000\u0000\u0000\u018a\u018b\u0006\u0019\uffff\uffff\u0000\u018b\u018c"+
		"\u0005!\u0000\u0000\u018c\u018d\u00032\u0019\u0000\u018d\u018e\u0005G"+
		"\u0000\u0000\u018e\u018f\u00032\u0019\u0000\u018f\u0190\u0005\"\u0000"+
		"\u0000\u0190\u0194\u0001\u0000\u0000\u0000\u0191\u0194\u00034\u001a\u0000"+
		"\u0192\u0194\u0005J\u0000\u0000\u0193\u018a\u0001\u0000\u0000\u0000\u0193"+
		"\u0191\u0001\u0000\u0000\u0000\u0193\u0192\u0001\u0000\u0000\u0000\u0194"+
		"\u019b\u0001\u0000\u0000\u0000\u0195\u0196\n\u0005\u0000\u0000\u0196\u019a"+
		"\u0005\u001f\u0000\u0000\u0197\u0198\n\u0004\u0000\u0000\u0198\u019a\u0005"+
		" \u0000\u0000\u0199\u0195\u0001\u0000\u0000\u0000\u0199\u0197\u0001\u0000"+
		"\u0000\u0000\u019a\u019d\u0001\u0000\u0000\u0000\u019b\u0199\u0001\u0000"+
		"\u0000\u0000\u019b\u019c\u0001\u0000\u0000\u0000\u019c3\u0001\u0000\u0000"+
		"\u0000\u019d\u019b\u0001\u0000\u0000\u0000\u019e\u019f\u0007\u0000\u0000"+
		"\u0000\u019f5\u0001\u0000\u0000\u0000\u01a0\u01a1\u00030\u0018\u0000\u01a1"+
		"\u01a2\u0005%\u0000\u0000\u01a2\u01a3\u00036\u001b\u0000\u01a3\u01a6\u0001"+
		"\u0000\u0000\u0000\u01a4\u01a6\u00030\u0018\u0000\u01a5\u01a0\u0001\u0000"+
		"\u0000\u0000\u01a5\u01a4\u0001\u0000\u0000\u0000\u01a67\u0001\u0000\u0000"+
		"\u0000\u01a7\u01a8\u0005&\u0000\u0000\u01a8\u01a9\u0005J\u0000\u0000\u01a9"+
		"\u01ab\u0005\u001a\u0000\u0000\u01aa\u01ac\u0003<\u001e\u0000\u01ab\u01aa"+
		"\u0001\u0000\u0000\u0000\u01ab\u01ac\u0001\u0000\u0000\u0000\u01ac\u01ad"+
		"\u0001\u0000\u0000\u0000\u01ad\u01ae\u0005\u001b\u0000\u0000\u01ae9\u0001"+
		"\u0000\u0000\u0000\u01af\u01b0\u0005\'\u0000\u0000\u01b0\u01b1\u0005J"+
		"\u0000\u0000\u01b1\u01b3\u0005\u001a\u0000\u0000\u01b2\u01b4\u0003B!\u0000"+
		"\u01b3\u01b2\u0001\u0000\u0000\u0000\u01b3\u01b4\u0001\u0000\u0000\u0000"+
		"\u01b4\u01b5\u0001\u0000\u0000\u0000\u01b5\u01b6\u0005\u001b\u0000\u0000"+
		"\u01b6;\u0001\u0000\u0000\u0000\u01b7\u01b8\u0003>\u001f\u0000\u01b8\u01b9"+
		"\u0005%\u0000\u0000\u01b9\u01ba\u0003<\u001e\u0000\u01ba\u01bd\u0001\u0000"+
		"\u0000\u0000\u01bb\u01bd\u0003>\u001f\u0000\u01bc\u01b7\u0001\u0000\u0000"+
		"\u0000\u01bc\u01bb\u0001\u0000\u0000\u0000\u01bd=\u0001\u0000\u0000\u0000"+
		"\u01be\u01c6\u0005J\u0000\u0000\u01bf\u01c0\u0005J\u0000\u0000\u01c0\u01c2"+
		"\u0005\u001a\u0000\u0000\u01c1\u01c3\u0003B!\u0000\u01c2\u01c1\u0001\u0000"+
		"\u0000\u0000\u01c2\u01c3\u0001\u0000\u0000\u0000\u01c3\u01c4\u0001\u0000"+
		"\u0000\u0000\u01c4\u01c6\u0005\u001b\u0000\u0000\u01c5\u01be\u0001\u0000"+
		"\u0000\u0000\u01c5\u01bf\u0001\u0000\u0000\u0000\u01c6?\u0001\u0000\u0000"+
		"\u0000\u01c7\u01c8\u0005J\u0000\u0000\u01c8\u01c9\u0005J\u0000\u0000\u01c9"+
		"\u01cb\u0005\u001a\u0000\u0000\u01ca\u01cc\u0003B!\u0000\u01cb\u01ca\u0001"+
		"\u0000\u0000\u0000\u01cb\u01cc\u0001\u0000\u0000\u0000\u01cc\u01cd\u0001"+
		"\u0000\u0000\u0000\u01cd\u01ce\u0005\u001b\u0000\u0000\u01ceA\u0001\u0000"+
		"\u0000\u0000\u01cf\u01d0\u0003D\"\u0000\u01d0\u01d1\u0005%\u0000\u0000"+
		"\u01d1\u01d2\u0003B!\u0000\u01d2\u01d5\u0001\u0000\u0000\u0000\u01d3\u01d5"+
		"\u0003D\"\u0000\u01d4\u01cf\u0001\u0000\u0000\u0000\u01d4\u01d3\u0001"+
		"\u0000\u0000\u0000\u01d5C\u0001\u0000\u0000\u0000\u01d6\u01d7\u0005J\u0000"+
		"\u0000\u01d7\u01d8\u0005\u000b\u0000\u0000\u01d8\u01d9\u0003\f\u0006\u0000"+
		"\u01d9E\u0001\u0000\u0000\u0000\u01da\u01db\u0003\f\u0006\u0000\u01db"+
		"\u01dc\u0005%\u0000\u0000\u01dc\u01dd\u0003F#\u0000\u01dd\u01e0\u0001"+
		"\u0000\u0000\u0000\u01de\u01e0\u0003\f\u0006\u0000\u01df\u01da\u0001\u0000"+
		"\u0000\u0000\u01df\u01de\u0001\u0000\u0000\u0000\u01e0G\u0001\u0000\u0000"+
		"\u0000\u01e1\u01e2\u0005\u0002\u0000\u0000\u01e2\u01e3\u0003\f\u0006\u0000"+
		"\u01e3\u01e4\u0005\u0003\u0000\u0000\u01e4I\u0001\u0000\u0000\u0000\u01e5"+
		"\u01e7\u0005!\u0000\u0000\u01e6\u01e8\u0003F#\u0000\u01e7\u01e6\u0001"+
		"\u0000\u0000\u0000\u01e7\u01e8\u0001\u0000\u0000\u0000\u01e8\u01e9\u0001"+
		"\u0000\u0000\u0000\u01e9\u01ea\u0005\"\u0000\u0000\u01eaK\u0001\u0000"+
		"\u0000\u0000\u01eb\u01ed\u0005\u0013\u0000\u0000\u01ec\u01ee\u0003F#\u0000"+
		"\u01ed\u01ec\u0001\u0000\u0000\u0000\u01ed\u01ee\u0001\u0000\u0000\u0000"+
		"\u01ee\u01ef\u0001\u0000\u0000\u0000\u01ef\u01f0\u0005\u0011\u0000\u0000"+
		"\u01f0M\u0001\u0000\u0000\u0000\u01f1\u01fd\u0005J\u0000\u0000\u01f2\u01fd"+
		"\u0005P\u0000\u0000\u01f3\u01fd\u0005H\u0000\u0000\u01f4\u01fd\u0005I"+
		"\u0000\u0000\u01f5\u01fd\u0005K\u0000\u0000\u01f6\u01fd\u0005L\u0000\u0000"+
		"\u01f7\u01fd\u0003>\u001f\u0000\u01f8\u01fd\u0003L&\u0000\u01f9\u01fd"+
		"\u0003H$\u0000\u01fa\u01fd\u0003\"\u0011\u0000\u01fb\u01fd\u0003J%\u0000"+
		"\u01fc\u01f1\u0001\u0000\u0000\u0000\u01fc\u01f2\u0001\u0000\u0000\u0000"+
		"\u01fc\u01f3\u0001\u0000\u0000\u0000\u01fc\u01f4\u0001\u0000\u0000\u0000"+
		"\u01fc\u01f5\u0001\u0000\u0000\u0000\u01fc\u01f6\u0001\u0000\u0000\u0000"+
		"\u01fc\u01f7\u0001\u0000\u0000\u0000\u01fc\u01f8\u0001\u0000\u0000\u0000"+
		"\u01fc\u01f9\u0001\u0000\u0000\u0000\u01fc\u01fa\u0001\u0000\u0000\u0000"+
		"\u01fc\u01fb\u0001\u0000\u0000\u0000\u01fdO\u0001\u0000\u0000\u0000\u01fe"+
		"\u01ff\u0005(\u0000\u0000\u01ff\u0200\u0005J\u0000\u0000\u0200\u0202\u0005"+
		"\u001a\u0000\u0000\u0201\u0203\u0003R)\u0000\u0202\u0201\u0001\u0000\u0000"+
		"\u0000\u0202\u0203\u0001\u0000\u0000\u0000\u0203\u0204\u0001\u0000\u0000"+
		"\u0000\u0204\u0205\u0005\u001b\u0000\u0000\u0205Q\u0001\u0000\u0000\u0000"+
		"\u0206\u0208\u0003T*\u0000\u0207\u0209\u0005\u0004\u0000\u0000\u0208\u0207"+
		"\u0001\u0000\u0000\u0000\u0208\u0209\u0001\u0000\u0000\u0000\u0209\u020b"+
		"\u0001\u0000\u0000\u0000\u020a\u020c\u0003R)\u0000\u020b\u020a\u0001\u0000"+
		"\u0000\u0000\u020b\u020c\u0001\u0000\u0000\u0000\u020cS\u0001\u0000\u0000"+
		"\u0000\u020d\u0210\u0003\\.\u0000\u020e\u0210\u0003V+\u0000\u020f\u020d"+
		"\u0001\u0000\u0000\u0000\u020f\u020e\u0001\u0000\u0000\u0000\u0210U\u0001"+
		"\u0000\u0000\u0000\u0211\u0213\u0003X,\u0000\u0212\u0214\u0003Z-\u0000"+
		"\u0213\u0212\u0001\u0000\u0000\u0000\u0214\u0215\u0001\u0000\u0000\u0000"+
		"\u0215\u0213\u0001\u0000\u0000\u0000\u0215\u0216\u0001\u0000\u0000\u0000"+
		"\u0216\u0218\u0001\u0000\u0000\u0000\u0217\u0219\u0003^/\u0000\u0218\u0217"+
		"\u0001\u0000\u0000\u0000\u0218\u0219\u0001\u0000\u0000\u0000\u0219W\u0001"+
		"\u0000\u0000\u0000\u021a\u021b\u0005J\u0000\u0000\u021b\u021c\u0005%\u0000"+
		"\u0000\u021c\u021f\u0003X,\u0000\u021d\u021f\u0005J\u0000\u0000\u021e"+
		"\u021a\u0001\u0000\u0000\u0000\u021e\u021d\u0001\u0000\u0000\u0000\u021f"+
		"Y\u0001\u0000\u0000\u0000\u0220\u0221\u0005G\u0000\u0000\u0221\u0222\u0003"+
		"X,\u0000\u0222[\u0001\u0000\u0000\u0000\u0223\u0225\u0005J\u0000\u0000"+
		"\u0224\u0226\u0003^/\u0000\u0225\u0224\u0001\u0000\u0000\u0000\u0225\u0226"+
		"\u0001\u0000\u0000\u0000\u0226]\u0001\u0000\u0000\u0000\u0227\u0229\u0005"+
		"!\u0000\u0000\u0228\u022a\u0003`0\u0000\u0229\u0228\u0001\u0000\u0000"+
		"\u0000\u022a\u022b\u0001\u0000\u0000\u0000\u022b\u0229\u0001\u0000\u0000"+
		"\u0000\u022b\u022c\u0001\u0000\u0000\u0000\u022c\u022d\u0001\u0000\u0000"+
		"\u0000\u022d\u022e\u0005\"\u0000\u0000\u022e_\u0001\u0000\u0000\u0000"+
		"\u022f\u0230\u0005J\u0000\u0000\u0230\u0231\u0005\n\u0000\u0000\u0231"+
		"\u0233\u0005J\u0000\u0000\u0232\u0234\u0007\u0001\u0000\u0000\u0233\u0232"+
		"\u0001\u0000\u0000\u0000\u0233\u0234\u0001\u0000\u0000\u0000\u0234\u023c"+
		"\u0001\u0000\u0000\u0000\u0235\u0236\u0005)\u0000\u0000\u0236\u0237\u0005"+
		"\n\u0000\u0000\u0237\u0239\u0003b1\u0000\u0238\u023a\u0007\u0001\u0000"+
		"\u0000\u0239\u0238\u0001\u0000\u0000\u0000\u0239\u023a\u0001\u0000\u0000"+
		"\u0000\u023a\u023c\u0001\u0000\u0000\u0000\u023b\u022f\u0001\u0000\u0000"+
		"\u0000\u023b\u0235\u0001\u0000\u0000\u0000\u023ca\u0001\u0000\u0000\u0000"+
		"\u023d\u0245\u0007\u0002\u0000\u0000\u023e\u0245\u0007\u0003\u0000\u0000"+
		"\u023f\u0245\u0007\u0004\u0000\u0000\u0240\u0245\u0007\u0005\u0000\u0000"+
		"\u0241\u0245\u0007\u0006\u0000\u0000\u0242\u0245\u0007\u0007\u0000\u0000"+
		"\u0243\u0245\u0007\b\u0000\u0000\u0244\u023d\u0001\u0000\u0000\u0000\u0244"+
		"\u023e\u0001\u0000\u0000\u0000\u0244\u023f\u0001\u0000\u0000\u0000\u0244"+
		"\u0240\u0001\u0000\u0000\u0000\u0244\u0241\u0001\u0000\u0000\u0000\u0244"+
		"\u0242\u0001\u0000\u0000\u0000\u0244\u0243\u0001\u0000\u0000\u0000\u0245"+
		"c\u0001\u0000\u0000\u0000\u0246\u0247\u00058\u0000\u0000\u0247\u0248\u0005"+
		"J\u0000\u0000\u0248\u0249\u0005\u001a\u0000\u0000\u0249\u024a\u0003t:"+
		"\u0000\u024a\u024b\u0005\u001b\u0000\u0000\u024be\u0001\u0000\u0000\u0000"+
		"\u024c\u024d\u0005(\u0000\u0000\u024d\u024e\u0005J\u0000\u0000\u024e\u024f"+
		"\u0005\u001a\u0000\u0000\u024f\u0250\u0003h4\u0000\u0250\u0251\u0005\u001b"+
		"\u0000\u0000\u0251g\u0001\u0000\u0000\u0000\u0252\u0257\u0005J\u0000\u0000"+
		"\u0253\u0254\u0005G\u0000\u0000\u0254\u0256\u0005J\u0000\u0000\u0255\u0253"+
		"\u0001\u0000\u0000\u0000\u0256\u0259\u0001\u0000\u0000\u0000\u0257\u0255"+
		"\u0001\u0000\u0000\u0000\u0257\u0258\u0001\u0000\u0000\u0000\u0258\u025a"+
		"\u0001\u0000\u0000\u0000\u0259\u0257\u0001\u0000\u0000\u0000\u025a\u025b"+
		"\u0005!\u0000\u0000\u025b\u025c\u0003j5\u0000\u025c\u025d\u0005\"\u0000"+
		"\u0000\u025di\u0001\u0000\u0000\u0000\u025e\u025f\u0005)\u0000\u0000\u025f"+
		"\u0260\u0005\n\u0000\u0000\u0260\u0261\u0003b1\u0000\u0261k\u0001\u0000"+
		"\u0000\u0000\u0262\u0263\u0005#\u0000\u0000\u0263\u0264\u0005J\u0000\u0000"+
		"\u0264\u0265\u0005\u001a\u0000\u0000\u0265\u026a\u0003r9\u0000\u0266\u0267"+
		"\u0005%\u0000\u0000\u0267\u0269\u0003r9\u0000\u0268\u0266\u0001\u0000"+
		"\u0000\u0000\u0269\u026c\u0001\u0000\u0000\u0000\u026a\u0268\u0001\u0000"+
		"\u0000\u0000\u026a\u026b\u0001\u0000\u0000\u0000\u026b\u026d\u0001\u0000"+
		"\u0000\u0000\u026c\u026a\u0001\u0000\u0000\u0000\u026d\u026e\u0005\u001b"+
		"\u0000\u0000\u026em\u0001\u0000\u0000\u0000\u026f\u0270\u0005$\u0000\u0000"+
		"\u0270\u0271\u0005J\u0000\u0000\u0271\u0272\u0005\u001a\u0000\u0000\u0272"+
		"\u0277\u0003r9\u0000\u0273\u0274\u0005%\u0000\u0000\u0274\u0276\u0003"+
		"r9\u0000\u0275\u0273\u0001\u0000\u0000\u0000\u0276\u0279\u0001\u0000\u0000"+
		"\u0000\u0277\u0275\u0001\u0000\u0000\u0000\u0277\u0278\u0001\u0000\u0000"+
		"\u0000\u0278\u027a\u0001\u0000\u0000\u0000\u0279\u0277\u0001\u0000\u0000"+
		"\u0000\u027a\u027b\u0005\u001b\u0000\u0000\u027bo\u0001\u0000\u0000\u0000"+
		"\u027c\u027d\u00059\u0000\u0000\u027d\u027e\u0005J\u0000\u0000\u027e\u027f"+
		"\u0005\u001a\u0000\u0000\u027f\u0284\u0003r9\u0000\u0280\u0281\u0005%"+
		"\u0000\u0000\u0281\u0283\u0003r9\u0000\u0282\u0280\u0001\u0000\u0000\u0000"+
		"\u0283\u0286\u0001\u0000\u0000\u0000\u0284\u0282\u0001\u0000\u0000\u0000"+
		"\u0284\u0285\u0001\u0000\u0000\u0000\u0285\u0287\u0001\u0000\u0000\u0000"+
		"\u0286\u0284\u0001\u0000\u0000\u0000\u0287\u0288\u0005\u001b\u0000\u0000"+
		"\u0288q\u0001\u0000\u0000\u0000\u0289\u0294\u0003v;\u0000\u028a\u0294"+
		"\u0003x<\u0000\u028b\u0294\u0003z=\u0000\u028c\u0294\u0003|>\u0000\u028d"+
		"\u0294\u0003~?\u0000\u028e\u0294\u0003\u0084B\u0000\u028f\u0294\u0003"+
		"\u0086C\u0000\u0290\u0294\u0003\u0088D\u0000\u0291\u0294\u0003\u008aE"+
		"\u0000\u0292\u0294\u0003\u008cF\u0000\u0293\u0289\u0001\u0000\u0000\u0000"+
		"\u0293\u028a\u0001\u0000\u0000\u0000\u0293\u028b\u0001\u0000\u0000\u0000"+
		"\u0293\u028c\u0001\u0000\u0000\u0000\u0293\u028d\u0001\u0000\u0000\u0000"+
		"\u0293\u028e\u0001\u0000\u0000\u0000\u0293\u028f\u0001\u0000\u0000\u0000"+
		"\u0293\u0290\u0001\u0000\u0000\u0000\u0293\u0291\u0001\u0000\u0000\u0000"+
		"\u0293\u0292\u0001\u0000\u0000\u0000\u0294s\u0001\u0000\u0000\u0000\u0295"+
		"\u0296\u0005:\u0000\u0000\u0296\u0297\u0005\u000b\u0000\u0000\u0297\u0298"+
		"\u0005J\u0000\u0000\u0298u\u0001\u0000\u0000\u0000\u0299\u029a\u0005;"+
		"\u0000\u0000\u029a\u029b\u0005\u000b\u0000\u0000\u029b\u029c\u0005P\u0000"+
		"\u0000\u029cw\u0001\u0000\u0000\u0000\u029d\u029e\u0005<\u0000\u0000\u029e"+
		"\u029f\u0005\u000b\u0000\u0000\u029f\u02a0\u0005!\u0000\u0000\u02a0\u02a5"+
		"\u0005P\u0000\u0000\u02a1\u02a2\u0005%\u0000\u0000\u02a2\u02a4\u0005P"+
		"\u0000\u0000\u02a3\u02a1\u0001\u0000\u0000\u0000\u02a4\u02a7\u0001\u0000"+
		"\u0000\u0000\u02a5\u02a3\u0001\u0000\u0000\u0000\u02a5\u02a6\u0001\u0000"+
		"\u0000\u0000\u02a6\u02a8\u0001\u0000\u0000\u0000\u02a7\u02a5\u0001\u0000"+
		"\u0000\u0000\u02a8\u02a9\u0005\"\u0000\u0000\u02a9y\u0001\u0000\u0000"+
		"\u0000\u02aa\u02ab\u0005=\u0000\u0000\u02ab\u02ac\u0005\u000b\u0000\u0000"+
		"\u02ac\u02ad\u0005K\u0000\u0000\u02ad{\u0001\u0000\u0000\u0000\u02ae\u02af"+
		"\u0005>\u0000\u0000\u02af\u02b0\u0005\u000b\u0000\u0000\u02b0\u02b1\u0005"+
		"!\u0000\u0000\u02b1\u02b6\u0005K\u0000\u0000\u02b2\u02b3\u0005%\u0000"+
		"\u0000\u02b3\u02b5\u0005K\u0000\u0000\u02b4\u02b2\u0001\u0000\u0000\u0000"+
		"\u02b5\u02b8\u0001\u0000\u0000\u0000\u02b6\u02b4\u0001\u0000\u0000\u0000"+
		"\u02b6\u02b7\u0001\u0000\u0000\u0000\u02b7\u02b9\u0001\u0000\u0000\u0000"+
		"\u02b8\u02b6\u0001\u0000\u0000\u0000\u02b9\u02ba\u0005\"\u0000\u0000\u02ba"+
		"}\u0001\u0000\u0000\u0000\u02bb\u02bc\u0005?\u0000\u0000\u02bc\u02bd\u0005"+
		"\u000b\u0000\u0000\u02bd\u02be\u0005\u0013\u0000\u0000\u02be\u02c3\u0003"+
		"\u0080@\u0000\u02bf\u02c0\u0005%\u0000\u0000\u02c0\u02c2\u0003\u0080@"+
		"\u0000\u02c1\u02bf\u0001\u0000\u0000\u0000\u02c2\u02c5\u0001\u0000\u0000"+
		"\u0000\u02c3\u02c1\u0001\u0000\u0000\u0000\u02c3\u02c4\u0001\u0000\u0000"+
		"\u0000\u02c4\u02c6\u0001\u0000\u0000\u0000\u02c5\u02c3\u0001\u0000\u0000"+
		"\u0000\u02c6\u02c7\u0005\u0011\u0000\u0000\u02c7\u007f\u0001\u0000\u0000"+
		"\u0000\u02c8\u02c9\u0005!\u0000\u0000\u02c9\u02ca\u0003\u0082A\u0000\u02ca"+
		"\u02cb\u0005%\u0000\u0000\u02cb\u02cc\u0003\u0082A\u0000\u02cc\u02cd\u0005"+
		"\"\u0000\u0000\u02cd\u0081\u0001\u0000\u0000\u0000\u02ce\u02cf\u0007\t"+
		"\u0000\u0000\u02cf\u0083\u0001\u0000\u0000\u0000\u02d0\u02d1\u0005A\u0000"+
		"\u0000\u02d1\u02d2\u0005\u000b\u0000\u0000\u02d2\u02d3\u0005K\u0000\u0000"+
		"\u02d3\u0085\u0001\u0000\u0000\u0000\u02d4\u02d5\u0005B\u0000\u0000\u02d5"+
		"\u02d6\u0005\u000b\u0000\u0000\u02d6\u02d7\u0005K\u0000\u0000\u02d7\u0087"+
		"\u0001\u0000\u0000\u0000\u02d8\u02d9\u0005C\u0000\u0000\u02d9\u02da\u0005"+
		"\u000b\u0000\u0000\u02da\u02db\u0005P\u0000\u0000\u02db\u0089\u0001\u0000"+
		"\u0000\u0000\u02dc\u02dd\u0005D\u0000\u0000\u02dd\u02de\u0005\u000b\u0000"+
		"\u0000\u02de\u02df\u0005J\u0000\u0000\u02df\u008b\u0001\u0000\u0000\u0000"+
		"\u02e0\u02e1\u0005E\u0000\u0000\u02e1\u02e2\u0005\u000b\u0000\u0000\u02e2"+
		"\u02e3\u0005J\u0000\u0000\u02e3\u008d\u0001\u0000\u0000\u0000\u02e4\u02e5"+
		"\u0005&\u0000\u0000\u02e5\u02e6\u0005J\u0000\u0000\u02e6\u02e7\u0005\u001a"+
		"\u0000\u0000\u02e7\u02e8\u0003\u0090H\u0000\u02e8\u02e9\u0005\u001b\u0000"+
		"\u0000\u02e9\u008f\u0001\u0000\u0000\u0000\u02ea\u02ef\u0003\u0092I\u0000"+
		"\u02eb\u02ec\u0005%\u0000\u0000\u02ec\u02ee\u0003\u0092I\u0000\u02ed\u02eb"+
		"\u0001\u0000\u0000\u0000\u02ee\u02f1\u0001\u0000\u0000\u0000\u02ef\u02ed"+
		"\u0001\u0000\u0000\u0000\u02ef\u02f0\u0001\u0000\u0000\u0000\u02f0\u0091"+
		"\u0001\u0000\u0000\u0000\u02f1\u02ef\u0001\u0000\u0000\u0000\u02f2\u02f3"+
		"\u0005J\u0000\u0000\u02f3\u02f5\u0005\u001a\u0000\u0000\u02f4\u02f6\u0003"+
		"\u0094J\u0000\u02f5\u02f4\u0001\u0000\u0000\u0000\u02f5\u02f6\u0001\u0000"+
		"\u0000\u0000\u02f6\u02f7\u0001\u0000\u0000\u0000\u02f7\u02f8\u0005\u001b"+
		"\u0000\u0000\u02f8\u0093\u0001\u0000\u0000\u0000\u02f9\u02fe\u0003\u0096"+
		"K\u0000\u02fa\u02fb\u0005%\u0000\u0000\u02fb\u02fd\u0003\u0096K\u0000"+
		"\u02fc\u02fa\u0001\u0000\u0000\u0000\u02fd\u0300\u0001\u0000\u0000\u0000"+
		"\u02fe\u02fc\u0001\u0000\u0000\u0000\u02fe\u02ff\u0001\u0000\u0000\u0000"+
		"\u02ff\u0095\u0001\u0000\u0000\u0000\u0300\u02fe\u0001\u0000\u0000\u0000"+
		"\u0301\u0302\u0005J\u0000\u0000\u0302\u0303\u0005\u000b\u0000\u0000\u0303"+
		"\u0305\u0003\u0098L\u0000\u0304\u0306\u0005%\u0000\u0000\u0305\u0304\u0001"+
		"\u0000\u0000\u0000\u0305\u0306\u0001\u0000\u0000\u0000\u0306\u0097\u0001"+
		"\u0000\u0000\u0000\u0307\u0308\u0007\n\u0000\u0000\u0308\u0099\u0001\u0000"+
		"\u0000\u0000F\u009d\u00ad\u00b3\u00b7\u00c3\u00d9\u00e7\u00eb\u00f0\u00f5"+
		"\u00f7\u00fe\u0108\u0112\u011d\u0126\u012b\u0136\u013b\u0144\u0149\u0152"+
		"\u0157\u015c\u0160\u0165\u016b\u0173\u0177\u017f\u0193\u0199\u019b\u01a5"+
		"\u01ab\u01b3\u01bc\u01c2\u01c5\u01cb\u01d4\u01df\u01e7\u01ed\u01fc\u0202"+
		"\u0208\u020b\u020f\u0215\u0218\u021e\u0225\u022b\u0233\u0239\u023b\u0244"+
		"\u0257\u026a\u0277\u0284\u0293\u02a5\u02b6\u02c3\u02ef\u02f5\u02fe\u0305";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}