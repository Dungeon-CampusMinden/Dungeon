// Generated from c:/Users/bjarn/VS_Projects/Dungeon/dungeon/src/dsl/antlr/DungeonDSL.g4 by ANTLR 4.13.1

    package antlr.main;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class DungeonDSLParser extends Parser {
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
		T__52=53, DOUBLE_LINE=54, ARROW=55, TRUE=56, FALSE=57, ID=58, NUM=59, 
		NUM_DEC=60, WS=61, LINE_COMMENT=62, BLOCK_COMMENT=63, STRING_LITERAL=64;
	public static final int
		RULE_program = 0, RULE_definition = 1, RULE_fn_def = 2, RULE_stmt = 3, 
		RULE_loop_stmt = 4, RULE_var_decl = 5, RULE_expression = 6, RULE_expression_rhs = 7, 
		RULE_assignment = 8, RULE_assignee = 9, RULE_logic_or = 10, RULE_logic_and = 11, 
		RULE_equality = 12, RULE_comparison = 13, RULE_term = 14, RULE_factor = 15, 
		RULE_unary = 16, RULE_func_call = 17, RULE_stmt_block = 18, RULE_stmt_list = 19, 
		RULE_return_stmt = 20, RULE_conditional_stmt = 21, RULE_else_stmt = 22, 
		RULE_ret_type_def = 23, RULE_param_def = 24, RULE_type_decl = 25, RULE_param_def_list = 26, 
		RULE_entity_type_def = 27, RULE_item_type_def = 28, RULE_component_def_list = 29, 
		RULE_aggregate_value_def = 30, RULE_object_def = 31, RULE_property_def_list = 32, 
		RULE_property_def = 33, RULE_expression_list = 34, RULE_grouped_expression = 35, 
		RULE_list_definition = 36, RULE_set_definition = 37, RULE_primary = 38, 
		RULE_dot_def = 39, RULE_dot_stmt_list = 40, RULE_dot_stmt = 41, RULE_dot_edge_stmt = 42, 
		RULE_dot_node_list = 43, RULE_dot_edge_RHS = 44, RULE_dot_node_stmt = 45, 
		RULE_dot_attr_list = 46, RULE_dot_attr = 47, RULE_dependency_type = 48;
	private static String[] makeRuleNames() {
		return new String[] {
			"program", "definition", "fn_def", "stmt", "loop_stmt", "var_decl", "expression", 
			"expression_rhs", "assignment", "assignee", "logic_or", "logic_and", 
			"equality", "comparison", "term", "factor", "unary", "func_call", "stmt_block", 
			"stmt_list", "return_stmt", "conditional_stmt", "else_stmt", "ret_type_def", 
			"param_def", "type_decl", "param_def_list", "entity_type_def", "item_type_def", 
			"component_def_list", "aggregate_value_def", "object_def", "property_def_list", 
			"property_def", "expression_list", "grouped_expression", "list_definition", 
			"set_definition", "primary", "dot_def", "dot_stmt_list", "dot_stmt", 
			"dot_edge_stmt", "dot_node_list", "dot_edge_RHS", "dot_node_stmt", "dot_attr_list", 
			"dot_attr", "dependency_type"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'fn'", "'('", "')'", "';'", "'for'", "'in'", "'count'", "'while'", 
			"'var'", "'='", "':'", "'.'", "'or'", "'and'", "'!='", "'=='", "'>'", 
			"'>='", "'<'", "'<='", "'-'", "'+'", "'/'", "'*'", "'!'", "'{'", "'}'", 
			"'return'", "'if'", "'else'", "'<>'", "'[]'", "'['", "']'", "','", "'entity_type'", 
			"'item_type'", "'graph'", "'type'", "'seq'", "'sequence'", "'st_m'", 
			"'subtask_mandatory'", "'st_o'", "'subtask_optional'", "'c_c'", "'conditional_correct'", 
			"'c_f'", "'conditional_false'", "'seq_and'", "'sequence_and'", "'seq_or'", 
			"'sequence_or'", "'--'", "'->'", "'true'", "'false'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, "DOUBLE_LINE", "ARROW", "TRUE", "FALSE", 
			"ID", "NUM", "NUM_DEC", "WS", "LINE_COMMENT", "BLOCK_COMMENT", "STRING_LITERAL"
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
	public String getGrammarFileName() { return "DungeonDSL.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public DungeonDSLParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ProgramContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(DungeonDSLParser.EOF, 0); }
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitProgram(this);
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
			setState(101);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 288230857188048898L) != 0)) {
				{
				{
				setState(98);
				definition();
				}
				}
				setState(103);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(104);
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
		public DefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_definition; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DefinitionContext definition() throws RecognitionException {
		DefinitionContext _localctx = new DefinitionContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_definition);
		try {
			setState(111);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__37:
				enterOuterAlt(_localctx, 1);
				{
				setState(106);
				dot_def();
				}
				break;
			case ID:
				enterOuterAlt(_localctx, 2);
				{
				setState(107);
				object_def();
				}
				break;
			case T__35:
				enterOuterAlt(_localctx, 3);
				{
				setState(108);
				entity_type_def();
				}
				break;
			case T__36:
				enterOuterAlt(_localctx, 4);
				{
				setState(109);
				item_type_def();
				}
				break;
			case T__0:
				enterOuterAlt(_localctx, 5);
				{
				setState(110);
				fn_def();
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
	public static class Fn_defContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(DungeonDSLParser.ID, 0); }
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitFn_def(this);
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
			setState(113);
			match(T__0);
			setState(114);
			match(ID);
			setState(115);
			match(T__1);
			setState(117);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__32 || _la==ID) {
				{
				setState(116);
				param_def_list();
				}
			}

			setState(119);
			match(T__2);
			setState(121);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ARROW) {
				{
				setState(120);
				ret_type_def();
				}
			}

			setState(123);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StmtContext stmt() throws RecognitionException {
		StmtContext _localctx = new StmtContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_stmt);
		try {
			setState(133);
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
				setState(125);
				expression();
				setState(126);
				match(T__3);
				}
				break;
			case T__8:
				enterOuterAlt(_localctx, 2);
				{
				setState(128);
				var_decl();
				}
				break;
			case T__25:
				enterOuterAlt(_localctx, 3);
				{
				setState(129);
				stmt_block();
				}
				break;
			case T__28:
				enterOuterAlt(_localctx, 4);
				{
				setState(130);
				conditional_stmt();
				}
				break;
			case T__27:
				enterOuterAlt(_localctx, 5);
				{
				setState(131);
				return_stmt();
				}
				break;
			case T__4:
			case T__7:
				enterOuterAlt(_localctx, 6);
				{
				setState(132);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitWhile_loop(this);
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
		public List<TerminalNode> ID() { return getTokens(DungeonDSLParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(DungeonDSLParser.ID, i);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public For_loop_countingContext(Loop_stmtContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitFor_loop_counting(this);
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
		public TerminalNode ID() { return getToken(DungeonDSLParser.ID, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public For_loopContext(Loop_stmtContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitFor_loop(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Loop_stmtContext loop_stmt() throws RecognitionException {
		Loop_stmtContext _localctx = new Loop_stmtContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_loop_stmt);
		try {
			setState(155);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				_localctx = new For_loopContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(135);
				match(T__4);
				setState(136);
				((For_loopContext)_localctx).type_id = type_decl(0);
				setState(137);
				((For_loopContext)_localctx).var_id = match(ID);
				setState(138);
				match(T__5);
				setState(139);
				((For_loopContext)_localctx).iteratable_id = expression();
				setState(140);
				stmt();
				}
				break;
			case 2:
				_localctx = new For_loop_countingContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(142);
				match(T__4);
				setState(143);
				((For_loop_countingContext)_localctx).type_id = type_decl(0);
				setState(144);
				((For_loop_countingContext)_localctx).var_id = match(ID);
				setState(145);
				match(T__5);
				setState(146);
				((For_loop_countingContext)_localctx).iteratable_id = expression();
				setState(147);
				match(T__6);
				setState(148);
				((For_loop_countingContext)_localctx).counter_id = match(ID);
				setState(149);
				stmt();
				}
				break;
			case 3:
				_localctx = new While_loopContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(151);
				match(T__7);
				setState(152);
				expression();
				setState(153);
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
		public TerminalNode ID() { return getToken(DungeonDSLParser.ID, 0); }
		public Var_decl_assignmentContext(Var_declContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitVar_decl_assignment(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Var_decl_type_declContext extends Var_declContext {
		public Token id;
		public Type_declContext type_decl() {
			return getRuleContext(Type_declContext.class,0);
		}
		public TerminalNode ID() { return getToken(DungeonDSLParser.ID, 0); }
		public Var_decl_type_declContext(Var_declContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitVar_decl_type_decl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Var_declContext var_decl() throws RecognitionException {
		Var_declContext _localctx = new Var_declContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_var_decl);
		try {
			setState(169);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				_localctx = new Var_decl_assignmentContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(157);
				match(T__8);
				setState(158);
				((Var_decl_assignmentContext)_localctx).id = match(ID);
				setState(159);
				match(T__9);
				setState(160);
				expression();
				setState(161);
				match(T__3);
				}
				break;
			case 2:
				_localctx = new Var_decl_type_declContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(163);
				match(T__8);
				setState(164);
				((Var_decl_type_declContext)_localctx).id = match(ID);
				setState(165);
				match(T__10);
				setState(166);
				type_decl(0);
				setState(167);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(171);
			assignment();
			setState(173);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				{
				setState(172);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitMethod_call_expression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Member_access_expressionContext extends Expression_rhsContext {
		public TerminalNode ID() { return getToken(DungeonDSLParser.ID, 0); }
		public Expression_rhsContext expression_rhs() {
			return getRuleContext(Expression_rhsContext.class,0);
		}
		public Member_access_expressionContext(Expression_rhsContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitMember_access_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Expression_rhsContext expression_rhs() throws RecognitionException {
		Expression_rhsContext _localctx = new Expression_rhsContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_expression_rhs);
		try {
			setState(185);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				_localctx = new Method_call_expressionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(175);
				match(T__11);
				setState(176);
				func_call();
				setState(178);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
				case 1:
					{
					setState(177);
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
				setState(180);
				match(T__11);
				setState(181);
				match(ID);
				setState(183);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
				case 1:
					{
					setState(182);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitAssignment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentContext assignment() throws RecognitionException {
		AssignmentContext _localctx = new AssignmentContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_assignment);
		try {
			setState(192);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(187);
				assignee();
				setState(188);
				match(T__9);
				setState(189);
				expression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(191);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitAssignee_func_call(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Assignee_identifierContext extends AssigneeContext {
		public TerminalNode ID() { return getToken(DungeonDSLParser.ID, 0); }
		public Assignee_identifierContext(AssigneeContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitAssignee_identifier(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Assignee_qualified_nameContext extends AssigneeContext {
		public TerminalNode ID() { return getToken(DungeonDSLParser.ID, 0); }
		public AssigneeContext assignee() {
			return getRuleContext(AssigneeContext.class,0);
		}
		public Assignee_qualified_nameContext(AssigneeContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitAssignee_qualified_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssigneeContext assignee() throws RecognitionException {
		AssigneeContext _localctx = new AssigneeContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_assignee);
		try {
			setState(202);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				_localctx = new Assignee_func_callContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(194);
				func_call();
				setState(195);
				match(T__11);
				setState(196);
				assignee();
				}
				break;
			case 2:
				_localctx = new Assignee_qualified_nameContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(198);
				match(ID);
				setState(199);
				match(T__11);
				setState(200);
				assignee();
				}
				break;
			case 3:
				_localctx = new Assignee_identifierContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(201);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitLogic_or(this);
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
			setState(205);
			logic_and(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(212);
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
					setState(207);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					{
					setState(208);
					((Logic_orContext)_localctx).or = match(T__12);
					setState(209);
					logic_and(0);
					}
					}
					} 
				}
				setState(214);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitLogic_and(this);
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
			setState(216);
			equality(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(223);
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
					setState(218);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					{
					setState(219);
					((Logic_andContext)_localctx).and = match(T__13);
					setState(220);
					equality(0);
					}
					}
					} 
				}
				setState(225);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitEquality(this);
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
			setState(227);
			comparison(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(237);
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
					setState(229);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					{
					setState(232);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case T__14:
						{
						setState(230);
						((EqualityContext)_localctx).neq = match(T__14);
						}
						break;
					case T__15:
						{
						setState(231);
						((EqualityContext)_localctx).eq = match(T__15);
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(234);
					comparison(0);
					}
					}
					} 
				}
				setState(239);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitComparison(this);
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
			setState(241);
			term(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(253);
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
					setState(243);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					{
					setState(248);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case T__16:
						{
						setState(244);
						((ComparisonContext)_localctx).gt = match(T__16);
						}
						break;
					case T__17:
						{
						setState(245);
						((ComparisonContext)_localctx).geq = match(T__17);
						}
						break;
					case T__18:
						{
						setState(246);
						((ComparisonContext)_localctx).lt = match(T__18);
						}
						break;
					case T__19:
						{
						setState(247);
						((ComparisonContext)_localctx).leq = match(T__19);
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(250);
					term(0);
					}
					}
					} 
				}
				setState(255);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitTerm(this);
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
			setState(257);
			factor(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(267);
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
					setState(259);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					{
					setState(262);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case T__20:
						{
						setState(260);
						((TermContext)_localctx).minus = match(T__20);
						}
						break;
					case T__21:
						{
						setState(261);
						((TermContext)_localctx).plus = match(T__21);
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(264);
					factor(0);
					}
					}
					} 
				}
				setState(269);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitFactor(this);
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
			setState(271);
			unary();
			}
			_ctx.stop = _input.LT(-1);
			setState(281);
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
					setState(273);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					{
					setState(276);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case T__22:
						{
						setState(274);
						((FactorContext)_localctx).div = match(T__22);
						}
						break;
					case T__23:
						{
						setState(275);
						((FactorContext)_localctx).mult = match(T__23);
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(278);
					unary();
					}
					}
					} 
				}
				setState(283);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitUnary(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnaryContext unary() throws RecognitionException {
		UnaryContext _localctx = new UnaryContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_unary);
		try {
			setState(290);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__20:
			case T__24:
				enterOuterAlt(_localctx, 1);
				{
				setState(286);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case T__24:
					{
					setState(284);
					((UnaryContext)_localctx).bang = match(T__24);
					}
					break;
				case T__20:
					{
					setState(285);
					((UnaryContext)_localctx).minus = match(T__20);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(288);
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
				setState(289);
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
		public TerminalNode ID() { return getToken(DungeonDSLParser.ID, 0); }
		public Expression_listContext expression_list() {
			return getRuleContext(Expression_listContext.class,0);
		}
		public Func_callContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_func_call; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitFunc_call(this);
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
			setState(292);
			match(ID);
			setState(293);
			match(T__1);
			setState(295);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((((_la - 2)) & ~0x3f) == 0 && ((1L << (_la - 2)) & 5170132374377857025L) != 0)) {
				{
				setState(294);
				expression_list();
				}
			}

			setState(297);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitStmt_block(this);
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
			setState(299);
			match(T__25);
			setState(301);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((((_la - 2)) & ~0x3f) == 0 && ((1L << (_la - 2)) & 5170132374595961033L) != 0)) {
				{
				setState(300);
				stmt_list();
				}
			}

			setState(303);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitStmt_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Stmt_listContext stmt_list() throws RecognitionException {
		Stmt_listContext _localctx = new Stmt_listContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_stmt_list);
		try {
			setState(309);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(305);
				stmt();
				setState(306);
				stmt_list();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(308);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitReturn_stmt(this);
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
			setState(311);
			match(T__27);
			setState(313);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((((_la - 2)) & ~0x3f) == 0 && ((1L << (_la - 2)) & 5170132374377857025L) != 0)) {
				{
				setState(312);
				expression();
				}
			}

			setState(315);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitConditional_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Conditional_stmtContext conditional_stmt() throws RecognitionException {
		Conditional_stmtContext _localctx = new Conditional_stmtContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_conditional_stmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(317);
			match(T__28);
			setState(318);
			expression();
			setState(319);
			stmt();
			setState(321);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,29,_ctx) ) {
			case 1:
				{
				setState(320);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitElse_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Else_stmtContext else_stmt() throws RecognitionException {
		Else_stmtContext _localctx = new Else_stmtContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_else_stmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(323);
			match(T__29);
			setState(324);
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
		public TerminalNode ARROW() { return getToken(DungeonDSLParser.ARROW, 0); }
		public Type_declContext type_decl() {
			return getRuleContext(Type_declContext.class,0);
		}
		public Ret_type_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ret_type_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitRet_type_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Ret_type_defContext ret_type_def() throws RecognitionException {
		Ret_type_defContext _localctx = new Ret_type_defContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_ret_type_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(326);
			match(ARROW);
			setState(327);
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
		public TerminalNode ID() { return getToken(DungeonDSLParser.ID, 0); }
		public Param_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_param_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitParam_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Param_defContext param_def() throws RecognitionException {
		Param_defContext _localctx = new Param_defContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_param_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(329);
			((Param_defContext)_localctx).type_id = type_decl(0);
			setState(330);
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
		public TerminalNode ARROW() { return getToken(DungeonDSLParser.ARROW, 0); }
		public Map_param_typeContext(Type_declContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitMap_param_type(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Id_param_typeContext extends Type_declContext {
		public TerminalNode ID() { return getToken(DungeonDSLParser.ID, 0); }
		public Id_param_typeContext(Type_declContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitId_param_type(this);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitList_param_type(this);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitSet_param_type(this);
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
			setState(340);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__32:
				{
				_localctx = new Map_param_typeContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(333);
				match(T__32);
				setState(334);
				type_decl(0);
				setState(335);
				match(ARROW);
				setState(336);
				type_decl(0);
				setState(337);
				match(T__33);
				}
				break;
			case ID:
				{
				_localctx = new Id_param_typeContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(339);
				match(ID);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(348);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,32,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(346);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
					case 1:
						{
						_localctx = new Set_param_typeContext(new Type_declContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_type_decl);
						setState(342);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(343);
						match(T__30);
						}
						break;
					case 2:
						{
						_localctx = new List_param_typeContext(new Type_declContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_type_decl);
						setState(344);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(345);
						match(T__31);
						}
						break;
					}
					} 
				}
				setState(350);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitParam_def_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Param_def_listContext param_def_list() throws RecognitionException {
		Param_def_listContext _localctx = new Param_def_listContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_param_def_list);
		try {
			setState(356);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,33,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(351);
				param_def();
				setState(352);
				match(T__34);
				setState(353);
				param_def_list();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(355);
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
		public TerminalNode ID() { return getToken(DungeonDSLParser.ID, 0); }
		public Component_def_listContext component_def_list() {
			return getRuleContext(Component_def_listContext.class,0);
		}
		public Entity_type_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_entity_type_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitEntity_type_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Entity_type_defContext entity_type_def() throws RecognitionException {
		Entity_type_defContext _localctx = new Entity_type_defContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_entity_type_def);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(358);
			match(T__35);
			setState(359);
			match(ID);
			setState(360);
			match(T__25);
			setState(362);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(361);
				component_def_list();
				}
			}

			setState(364);
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
		public TerminalNode ID() { return getToken(DungeonDSLParser.ID, 0); }
		public Property_def_listContext property_def_list() {
			return getRuleContext(Property_def_listContext.class,0);
		}
		public Item_type_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_item_type_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitItem_type_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Item_type_defContext item_type_def() throws RecognitionException {
		Item_type_defContext _localctx = new Item_type_defContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_item_type_def);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(366);
			match(T__36);
			setState(367);
			match(ID);
			setState(368);
			match(T__25);
			setState(370);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(369);
				property_def_list();
				}
			}

			setState(372);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitComponent_def_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Component_def_listContext component_def_list() throws RecognitionException {
		Component_def_listContext _localctx = new Component_def_listContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_component_def_list);
		try {
			setState(379);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,36,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(374);
				aggregate_value_def();
				setState(375);
				match(T__34);
				setState(376);
				component_def_list();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(378);
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
		public TerminalNode ID() { return getToken(DungeonDSLParser.ID, 0); }
		public Property_def_listContext property_def_list() {
			return getRuleContext(Property_def_listContext.class,0);
		}
		public Aggregate_value_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aggregate_value_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitAggregate_value_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Aggregate_value_defContext aggregate_value_def() throws RecognitionException {
		Aggregate_value_defContext _localctx = new Aggregate_value_defContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_aggregate_value_def);
		int _la;
		try {
			setState(388);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,38,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(381);
				((Aggregate_value_defContext)_localctx).type_id = match(ID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(382);
				((Aggregate_value_defContext)_localctx).type_id = match(ID);
				setState(383);
				match(T__25);
				setState(385);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(384);
					property_def_list();
					}
				}

				setState(387);
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
		public List<TerminalNode> ID() { return getTokens(DungeonDSLParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(DungeonDSLParser.ID, i);
		}
		public Property_def_listContext property_def_list() {
			return getRuleContext(Property_def_listContext.class,0);
		}
		public Object_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_object_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitObject_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Object_defContext object_def() throws RecognitionException {
		Object_defContext _localctx = new Object_defContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_object_def);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(390);
			((Object_defContext)_localctx).type_id = match(ID);
			setState(391);
			((Object_defContext)_localctx).object_id = match(ID);
			setState(392);
			match(T__25);
			setState(394);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(393);
				property_def_list();
				}
			}

			setState(396);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitProperty_def_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Property_def_listContext property_def_list() throws RecognitionException {
		Property_def_listContext _localctx = new Property_def_listContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_property_def_list);
		try {
			setState(403);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,40,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(398);
				property_def();
				setState(399);
				match(T__34);
				setState(400);
				property_def_list();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(402);
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
		public TerminalNode ID() { return getToken(DungeonDSLParser.ID, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Property_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_property_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitProperty_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Property_defContext property_def() throws RecognitionException {
		Property_defContext _localctx = new Property_defContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_property_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(405);
			match(ID);
			setState(406);
			match(T__10);
			setState(407);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitExpression_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Expression_listContext expression_list() throws RecognitionException {
		Expression_listContext _localctx = new Expression_listContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_expression_list);
		try {
			setState(414);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,41,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(409);
				expression();
				setState(410);
				match(T__34);
				setState(411);
				expression_list();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(413);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitGrouped_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Grouped_expressionContext grouped_expression() throws RecognitionException {
		Grouped_expressionContext _localctx = new Grouped_expressionContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_grouped_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(416);
			match(T__1);
			setState(417);
			expression();
			setState(418);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitList_definition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final List_definitionContext list_definition() throws RecognitionException {
		List_definitionContext _localctx = new List_definitionContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_list_definition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(420);
			match(T__32);
			setState(422);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((((_la - 2)) & ~0x3f) == 0 && ((1L << (_la - 2)) & 5170132374377857025L) != 0)) {
				{
				setState(421);
				expression_list();
				}
			}

			setState(424);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitSet_definition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Set_definitionContext set_definition() throws RecognitionException {
		Set_definitionContext _localctx = new Set_definitionContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_set_definition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(426);
			match(T__18);
			setState(428);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((((_la - 2)) & ~0x3f) == 0 && ((1L << (_la - 2)) & 5170132374377857025L) != 0)) {
				{
				setState(427);
				expression_list();
				}
			}

			setState(430);
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
		public TerminalNode ID() { return getToken(DungeonDSLParser.ID, 0); }
		public TerminalNode STRING_LITERAL() { return getToken(DungeonDSLParser.STRING_LITERAL, 0); }
		public TerminalNode TRUE() { return getToken(DungeonDSLParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(DungeonDSLParser.FALSE, 0); }
		public TerminalNode NUM() { return getToken(DungeonDSLParser.NUM, 0); }
		public TerminalNode NUM_DEC() { return getToken(DungeonDSLParser.NUM_DEC, 0); }
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitPrimary(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimaryContext primary() throws RecognitionException {
		PrimaryContext _localctx = new PrimaryContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_primary);
		try {
			setState(443);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,44,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(432);
				match(ID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(433);
				match(STRING_LITERAL);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(434);
				match(TRUE);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(435);
				match(FALSE);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(436);
				match(NUM);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(437);
				match(NUM_DEC);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(438);
				aggregate_value_def();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(439);
				set_definition();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(440);
				grouped_expression();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(441);
				func_call();
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(442);
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
		public TerminalNode ID() { return getToken(DungeonDSLParser.ID, 0); }
		public Dot_stmt_listContext dot_stmt_list() {
			return getRuleContext(Dot_stmt_listContext.class,0);
		}
		public Dot_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dot_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitDot_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Dot_defContext dot_def() throws RecognitionException {
		Dot_defContext _localctx = new Dot_defContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_dot_def);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(445);
			match(T__37);
			setState(446);
			match(ID);
			setState(447);
			match(T__25);
			setState(449);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(448);
				dot_stmt_list();
				}
			}

			setState(451);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitDot_stmt_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Dot_stmt_listContext dot_stmt_list() throws RecognitionException {
		Dot_stmt_listContext _localctx = new Dot_stmt_listContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_dot_stmt_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(453);
			dot_stmt();
			setState(455);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__3) {
				{
				setState(454);
				match(T__3);
				}
			}

			setState(458);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(457);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitDot_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Dot_stmtContext dot_stmt() throws RecognitionException {
		Dot_stmtContext _localctx = new Dot_stmtContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_dot_stmt);
		try {
			setState(462);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,48,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(460);
				dot_node_stmt();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(461);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitDot_edge_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Dot_edge_stmtContext dot_edge_stmt() throws RecognitionException {
		Dot_edge_stmtContext _localctx = new Dot_edge_stmtContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_dot_edge_stmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(464);
			dot_node_list();
			setState(466); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(465);
				dot_edge_RHS();
				}
				}
				setState(468); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==ARROW );
			setState(471);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__32) {
				{
				setState(470);
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
		public TerminalNode ID() { return getToken(DungeonDSLParser.ID, 0); }
		public Dot_node_listContext dot_node_list() {
			return getRuleContext(Dot_node_listContext.class,0);
		}
		public Dot_node_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dot_node_list; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitDot_node_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Dot_node_listContext dot_node_list() throws RecognitionException {
		Dot_node_listContext _localctx = new Dot_node_listContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_dot_node_list);
		try {
			setState(477);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,51,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(473);
				match(ID);
				setState(474);
				match(T__34);
				setState(475);
				dot_node_list();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(476);
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
		public TerminalNode ARROW() { return getToken(DungeonDSLParser.ARROW, 0); }
		public Dot_node_listContext dot_node_list() {
			return getRuleContext(Dot_node_listContext.class,0);
		}
		public Dot_edge_RHSContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dot_edge_RHS; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitDot_edge_RHS(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Dot_edge_RHSContext dot_edge_RHS() throws RecognitionException {
		Dot_edge_RHSContext _localctx = new Dot_edge_RHSContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_dot_edge_RHS);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(479);
			match(ARROW);
			setState(480);
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
		public TerminalNode ID() { return getToken(DungeonDSLParser.ID, 0); }
		public Dot_attr_listContext dot_attr_list() {
			return getRuleContext(Dot_attr_listContext.class,0);
		}
		public Dot_node_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dot_node_stmt; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitDot_node_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Dot_node_stmtContext dot_node_stmt() throws RecognitionException {
		Dot_node_stmtContext _localctx = new Dot_node_stmtContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_dot_node_stmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(482);
			match(ID);
			setState(484);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__32) {
				{
				setState(483);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitDot_attr_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Dot_attr_listContext dot_attr_list() throws RecognitionException {
		Dot_attr_listContext _localctx = new Dot_attr_listContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_dot_attr_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(486);
			match(T__32);
			setState(488); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(487);
				dot_attr();
				}
				}
				setState(490); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==T__38 || _la==ID );
			setState(492);
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
		public List<TerminalNode> ID() { return getTokens(DungeonDSLParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(DungeonDSLParser.ID, i);
		}
		public Dot_attr_idContext(Dot_attrContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitDot_attr_id(this);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitDot_attr_dependency_type(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Dot_attrContext dot_attr() throws RecognitionException {
		Dot_attrContext _localctx = new Dot_attrContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_dot_attr);
		int _la;
		try {
			setState(506);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ID:
				_localctx = new Dot_attr_idContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(494);
				match(ID);
				setState(495);
				match(T__9);
				setState(496);
				match(ID);
				setState(498);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__3 || _la==T__34) {
					{
					setState(497);
					_la = _input.LA(1);
					if ( !(_la==T__3 || _la==T__34) ) {
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
			case T__38:
				_localctx = new Dot_attr_dependency_typeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(500);
				match(T__38);
				setState(501);
				match(T__9);
				setState(502);
				dependency_type();
				setState(504);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__3 || _la==T__34) {
					{
					setState(503);
					_la = _input.LA(1);
					if ( !(_la==T__3 || _la==T__34) ) {
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitDt_sequence(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Dt_subtask_mandatoryContext extends Dependency_typeContext {
		public Dt_subtask_mandatoryContext(Dependency_typeContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitDt_subtask_mandatory(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Dt_sequence_andContext extends Dependency_typeContext {
		public Dt_sequence_andContext(Dependency_typeContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitDt_sequence_and(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Dt_conditional_falseContext extends Dependency_typeContext {
		public Dt_conditional_falseContext(Dependency_typeContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitDt_conditional_false(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Dt_subtask_optionalContext extends Dependency_typeContext {
		public Dt_subtask_optionalContext(Dependency_typeContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitDt_subtask_optional(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Dt_conditional_correctContext extends Dependency_typeContext {
		public Dt_conditional_correctContext(Dependency_typeContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitDt_conditional_correct(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Dt_sequence_orContext extends Dependency_typeContext {
		public Dt_sequence_orContext(Dependency_typeContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DungeonDSLVisitor ) return ((DungeonDSLVisitor<? extends T>)visitor).visitDt_sequence_or(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Dependency_typeContext dependency_type() throws RecognitionException {
		Dependency_typeContext _localctx = new Dependency_typeContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_dependency_type);
		int _la;
		try {
			setState(515);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__39:
			case T__40:
				_localctx = new Dt_sequenceContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(508);
				_la = _input.LA(1);
				if ( !(_la==T__39 || _la==T__40) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case T__41:
			case T__42:
				_localctx = new Dt_subtask_mandatoryContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(509);
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
				_localctx = new Dt_subtask_optionalContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(510);
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
				_localctx = new Dt_conditional_correctContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(511);
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
				_localctx = new Dt_conditional_falseContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(512);
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
				_localctx = new Dt_sequence_andContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(513);
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
				_localctx = new Dt_sequence_orContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(514);
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
			return precpred(_ctx, 4);
		case 7:
			return precpred(_ctx, 3);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001@\u0206\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
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
		"-\u0007-\u0002.\u0007.\u0002/\u0007/\u00020\u00070\u0001\u0000\u0005\u0000"+
		"d\b\u0000\n\u0000\f\u0000g\t\u0000\u0001\u0000\u0001\u0000\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001p\b\u0001"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u0002v\b\u0002"+
		"\u0001\u0002\u0001\u0002\u0003\u0002z\b\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0003\u0003\u0086\b\u0003\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0003\u0004\u009c\b\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0003\u0005\u00aa\b\u0005\u0001\u0006\u0001\u0006"+
		"\u0003\u0006\u00ae\b\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007"+
		"\u00b3\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u00b8\b"+
		"\u0007\u0003\u0007\u00ba\b\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0001"+
		"\b\u0003\b\u00c1\b\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0003\t\u00cb\b\t\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0005\n\u00d3\b\n\n\n\f\n\u00d6\t\n\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0005\u000b\u00de\b\u000b\n\u000b"+
		"\f\u000b\u00e1\t\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f"+
		"\u0003\f\u00e9\b\f\u0001\f\u0005\f\u00ec\b\f\n\f\f\f\u00ef\t\f\u0001\r"+
		"\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0003\r\u00f9"+
		"\b\r\u0001\r\u0005\r\u00fc\b\r\n\r\f\r\u00ff\t\r\u0001\u000e\u0001\u000e"+
		"\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u0107\b\u000e"+
		"\u0001\u000e\u0005\u000e\u010a\b\u000e\n\u000e\f\u000e\u010d\t\u000e\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0003"+
		"\u000f\u0115\b\u000f\u0001\u000f\u0005\u000f\u0118\b\u000f\n\u000f\f\u000f"+
		"\u011b\t\u000f\u0001\u0010\u0001\u0010\u0003\u0010\u011f\b\u0010\u0001"+
		"\u0010\u0001\u0010\u0003\u0010\u0123\b\u0010\u0001\u0011\u0001\u0011\u0001"+
		"\u0011\u0003\u0011\u0128\b\u0011\u0001\u0011\u0001\u0011\u0001\u0012\u0001"+
		"\u0012\u0003\u0012\u012e\b\u0012\u0001\u0012\u0001\u0012\u0001\u0013\u0001"+
		"\u0013\u0001\u0013\u0001\u0013\u0003\u0013\u0136\b\u0013\u0001\u0014\u0001"+
		"\u0014\u0003\u0014\u013a\b\u0014\u0001\u0014\u0001\u0014\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0003\u0015\u0142\b\u0015\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0018\u0001"+
		"\u0018\u0001\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001"+
		"\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0003\u0019\u0155\b\u0019\u0001"+
		"\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0005\u0019\u015b\b\u0019\n"+
		"\u0019\f\u0019\u015e\t\u0019\u0001\u001a\u0001\u001a\u0001\u001a\u0001"+
		"\u001a\u0001\u001a\u0003\u001a\u0165\b\u001a\u0001\u001b\u0001\u001b\u0001"+
		"\u001b\u0001\u001b\u0003\u001b\u016b\b\u001b\u0001\u001b\u0001\u001b\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0003\u001c\u0173\b\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001"+
		"\u001d\u0003\u001d\u017c\b\u001d\u0001\u001e\u0001\u001e\u0001\u001e\u0001"+
		"\u001e\u0003\u001e\u0182\b\u001e\u0001\u001e\u0003\u001e\u0185\b\u001e"+
		"\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0003\u001f\u018b\b\u001f"+
		"\u0001\u001f\u0001\u001f\u0001 \u0001 \u0001 \u0001 \u0001 \u0003 \u0194"+
		"\b \u0001!\u0001!\u0001!\u0001!\u0001\"\u0001\"\u0001\"\u0001\"\u0001"+
		"\"\u0003\"\u019f\b\"\u0001#\u0001#\u0001#\u0001#\u0001$\u0001$\u0003$"+
		"\u01a7\b$\u0001$\u0001$\u0001%\u0001%\u0003%\u01ad\b%\u0001%\u0001%\u0001"+
		"&\u0001&\u0001&\u0001&\u0001&\u0001&\u0001&\u0001&\u0001&\u0001&\u0001"+
		"&\u0003&\u01bc\b&\u0001\'\u0001\'\u0001\'\u0001\'\u0003\'\u01c2\b\'\u0001"+
		"\'\u0001\'\u0001(\u0001(\u0003(\u01c8\b(\u0001(\u0003(\u01cb\b(\u0001"+
		")\u0001)\u0003)\u01cf\b)\u0001*\u0001*\u0004*\u01d3\b*\u000b*\f*\u01d4"+
		"\u0001*\u0003*\u01d8\b*\u0001+\u0001+\u0001+\u0001+\u0003+\u01de\b+\u0001"+
		",\u0001,\u0001,\u0001-\u0001-\u0003-\u01e5\b-\u0001.\u0001.\u0004.\u01e9"+
		"\b.\u000b.\f.\u01ea\u0001.\u0001.\u0001/\u0001/\u0001/\u0001/\u0003/\u01f3"+
		"\b/\u0001/\u0001/\u0001/\u0001/\u0003/\u01f9\b/\u0003/\u01fb\b/\u0001"+
		"0\u00010\u00010\u00010\u00010\u00010\u00010\u00030\u0204\b0\u00010\u0000"+
		"\u0007\u0014\u0016\u0018\u001a\u001c\u001e21\u0000\u0002\u0004\u0006\b"+
		"\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02"+
		"468:<>@BDFHJLNPRTVXZ\\^`\u0000\b\u0002\u0000\u0004\u0004##\u0001\u0000"+
		"()\u0001\u0000*+\u0001\u0000,-\u0001\u0000./\u0001\u000001\u0001\u0000"+
		"23\u0001\u000045\u0227\u0000e\u0001\u0000\u0000\u0000\u0002o\u0001\u0000"+
		"\u0000\u0000\u0004q\u0001\u0000\u0000\u0000\u0006\u0085\u0001\u0000\u0000"+
		"\u0000\b\u009b\u0001\u0000\u0000\u0000\n\u00a9\u0001\u0000\u0000\u0000"+
		"\f\u00ab\u0001\u0000\u0000\u0000\u000e\u00b9\u0001\u0000\u0000\u0000\u0010"+
		"\u00c0\u0001\u0000\u0000\u0000\u0012\u00ca\u0001\u0000\u0000\u0000\u0014"+
		"\u00cc\u0001\u0000\u0000\u0000\u0016\u00d7\u0001\u0000\u0000\u0000\u0018"+
		"\u00e2\u0001\u0000\u0000\u0000\u001a\u00f0\u0001\u0000\u0000\u0000\u001c"+
		"\u0100\u0001\u0000\u0000\u0000\u001e\u010e\u0001\u0000\u0000\u0000 \u0122"+
		"\u0001\u0000\u0000\u0000\"\u0124\u0001\u0000\u0000\u0000$\u012b\u0001"+
		"\u0000\u0000\u0000&\u0135\u0001\u0000\u0000\u0000(\u0137\u0001\u0000\u0000"+
		"\u0000*\u013d\u0001\u0000\u0000\u0000,\u0143\u0001\u0000\u0000\u0000."+
		"\u0146\u0001\u0000\u0000\u00000\u0149\u0001\u0000\u0000\u00002\u0154\u0001"+
		"\u0000\u0000\u00004\u0164\u0001\u0000\u0000\u00006\u0166\u0001\u0000\u0000"+
		"\u00008\u016e\u0001\u0000\u0000\u0000:\u017b\u0001\u0000\u0000\u0000<"+
		"\u0184\u0001\u0000\u0000\u0000>\u0186\u0001\u0000\u0000\u0000@\u0193\u0001"+
		"\u0000\u0000\u0000B\u0195\u0001\u0000\u0000\u0000D\u019e\u0001\u0000\u0000"+
		"\u0000F\u01a0\u0001\u0000\u0000\u0000H\u01a4\u0001\u0000\u0000\u0000J"+
		"\u01aa\u0001\u0000\u0000\u0000L\u01bb\u0001\u0000\u0000\u0000N\u01bd\u0001"+
		"\u0000\u0000\u0000P\u01c5\u0001\u0000\u0000\u0000R\u01ce\u0001\u0000\u0000"+
		"\u0000T\u01d0\u0001\u0000\u0000\u0000V\u01dd\u0001\u0000\u0000\u0000X"+
		"\u01df\u0001\u0000\u0000\u0000Z\u01e2\u0001\u0000\u0000\u0000\\\u01e6"+
		"\u0001\u0000\u0000\u0000^\u01fa\u0001\u0000\u0000\u0000`\u0203\u0001\u0000"+
		"\u0000\u0000bd\u0003\u0002\u0001\u0000cb\u0001\u0000\u0000\u0000dg\u0001"+
		"\u0000\u0000\u0000ec\u0001\u0000\u0000\u0000ef\u0001\u0000\u0000\u0000"+
		"fh\u0001\u0000\u0000\u0000ge\u0001\u0000\u0000\u0000hi\u0005\u0000\u0000"+
		"\u0001i\u0001\u0001\u0000\u0000\u0000jp\u0003N\'\u0000kp\u0003>\u001f"+
		"\u0000lp\u00036\u001b\u0000mp\u00038\u001c\u0000np\u0003\u0004\u0002\u0000"+
		"oj\u0001\u0000\u0000\u0000ok\u0001\u0000\u0000\u0000ol\u0001\u0000\u0000"+
		"\u0000om\u0001\u0000\u0000\u0000on\u0001\u0000\u0000\u0000p\u0003\u0001"+
		"\u0000\u0000\u0000qr\u0005\u0001\u0000\u0000rs\u0005:\u0000\u0000su\u0005"+
		"\u0002\u0000\u0000tv\u00034\u001a\u0000ut\u0001\u0000\u0000\u0000uv\u0001"+
		"\u0000\u0000\u0000vw\u0001\u0000\u0000\u0000wy\u0005\u0003\u0000\u0000"+
		"xz\u0003.\u0017\u0000yx\u0001\u0000\u0000\u0000yz\u0001\u0000\u0000\u0000"+
		"z{\u0001\u0000\u0000\u0000{|\u0003$\u0012\u0000|\u0005\u0001\u0000\u0000"+
		"\u0000}~\u0003\f\u0006\u0000~\u007f\u0005\u0004\u0000\u0000\u007f\u0086"+
		"\u0001\u0000\u0000\u0000\u0080\u0086\u0003\n\u0005\u0000\u0081\u0086\u0003"+
		"$\u0012\u0000\u0082\u0086\u0003*\u0015\u0000\u0083\u0086\u0003(\u0014"+
		"\u0000\u0084\u0086\u0003\b\u0004\u0000\u0085}\u0001\u0000\u0000\u0000"+
		"\u0085\u0080\u0001\u0000\u0000\u0000\u0085\u0081\u0001\u0000\u0000\u0000"+
		"\u0085\u0082\u0001\u0000\u0000\u0000\u0085\u0083\u0001\u0000\u0000\u0000"+
		"\u0085\u0084\u0001\u0000\u0000\u0000\u0086\u0007\u0001\u0000\u0000\u0000"+
		"\u0087\u0088\u0005\u0005\u0000\u0000\u0088\u0089\u00032\u0019\u0000\u0089"+
		"\u008a\u0005:\u0000\u0000\u008a\u008b\u0005\u0006\u0000\u0000\u008b\u008c"+
		"\u0003\f\u0006\u0000\u008c\u008d\u0003\u0006\u0003\u0000\u008d\u009c\u0001"+
		"\u0000\u0000\u0000\u008e\u008f\u0005\u0005\u0000\u0000\u008f\u0090\u0003"+
		"2\u0019\u0000\u0090\u0091\u0005:\u0000\u0000\u0091\u0092\u0005\u0006\u0000"+
		"\u0000\u0092\u0093\u0003\f\u0006\u0000\u0093\u0094\u0005\u0007\u0000\u0000"+
		"\u0094\u0095\u0005:\u0000\u0000\u0095\u0096\u0003\u0006\u0003\u0000\u0096"+
		"\u009c\u0001\u0000\u0000\u0000\u0097\u0098\u0005\b\u0000\u0000\u0098\u0099"+
		"\u0003\f\u0006\u0000\u0099\u009a\u0003\u0006\u0003\u0000\u009a\u009c\u0001"+
		"\u0000\u0000\u0000\u009b\u0087\u0001\u0000\u0000\u0000\u009b\u008e\u0001"+
		"\u0000\u0000\u0000\u009b\u0097\u0001\u0000\u0000\u0000\u009c\t\u0001\u0000"+
		"\u0000\u0000\u009d\u009e\u0005\t\u0000\u0000\u009e\u009f\u0005:\u0000"+
		"\u0000\u009f\u00a0\u0005\n\u0000\u0000\u00a0\u00a1\u0003\f\u0006\u0000"+
		"\u00a1\u00a2\u0005\u0004\u0000\u0000\u00a2\u00aa\u0001\u0000\u0000\u0000"+
		"\u00a3\u00a4\u0005\t\u0000\u0000\u00a4\u00a5\u0005:\u0000\u0000\u00a5"+
		"\u00a6\u0005\u000b\u0000\u0000\u00a6\u00a7\u00032\u0019\u0000\u00a7\u00a8"+
		"\u0005\u0004\u0000\u0000\u00a8\u00aa\u0001\u0000\u0000\u0000\u00a9\u009d"+
		"\u0001\u0000\u0000\u0000\u00a9\u00a3\u0001\u0000\u0000\u0000\u00aa\u000b"+
		"\u0001\u0000\u0000\u0000\u00ab\u00ad\u0003\u0010\b\u0000\u00ac\u00ae\u0003"+
		"\u000e\u0007\u0000\u00ad\u00ac\u0001\u0000\u0000\u0000\u00ad\u00ae\u0001"+
		"\u0000\u0000\u0000\u00ae\r\u0001\u0000\u0000\u0000\u00af\u00b0\u0005\f"+
		"\u0000\u0000\u00b0\u00b2\u0003\"\u0011\u0000\u00b1\u00b3\u0003\u000e\u0007"+
		"\u0000\u00b2\u00b1\u0001\u0000\u0000\u0000\u00b2\u00b3\u0001\u0000\u0000"+
		"\u0000\u00b3\u00ba\u0001\u0000\u0000\u0000\u00b4\u00b5\u0005\f\u0000\u0000"+
		"\u00b5\u00b7\u0005:\u0000\u0000\u00b6\u00b8\u0003\u000e\u0007\u0000\u00b7"+
		"\u00b6\u0001\u0000\u0000\u0000\u00b7\u00b8\u0001\u0000\u0000\u0000\u00b8"+
		"\u00ba\u0001\u0000\u0000\u0000\u00b9\u00af\u0001\u0000\u0000\u0000\u00b9"+
		"\u00b4\u0001\u0000\u0000\u0000\u00ba\u000f\u0001\u0000\u0000\u0000\u00bb"+
		"\u00bc\u0003\u0012\t\u0000\u00bc\u00bd\u0005\n\u0000\u0000\u00bd\u00be"+
		"\u0003\f\u0006\u0000\u00be\u00c1\u0001\u0000\u0000\u0000\u00bf\u00c1\u0003"+
		"\u0014\n\u0000\u00c0\u00bb\u0001\u0000\u0000\u0000\u00c0\u00bf\u0001\u0000"+
		"\u0000\u0000\u00c1\u0011\u0001\u0000\u0000\u0000\u00c2\u00c3\u0003\"\u0011"+
		"\u0000\u00c3\u00c4\u0005\f\u0000\u0000\u00c4\u00c5\u0003\u0012\t\u0000"+
		"\u00c5\u00cb\u0001\u0000\u0000\u0000\u00c6\u00c7\u0005:\u0000\u0000\u00c7"+
		"\u00c8\u0005\f\u0000\u0000\u00c8\u00cb\u0003\u0012\t\u0000\u00c9\u00cb"+
		"\u0005:\u0000\u0000\u00ca\u00c2\u0001\u0000\u0000\u0000\u00ca\u00c6\u0001"+
		"\u0000\u0000\u0000\u00ca\u00c9\u0001\u0000\u0000\u0000\u00cb\u0013\u0001"+
		"\u0000\u0000\u0000\u00cc\u00cd\u0006\n\uffff\uffff\u0000\u00cd\u00ce\u0003"+
		"\u0016\u000b\u0000\u00ce\u00d4\u0001\u0000\u0000\u0000\u00cf\u00d0\n\u0002"+
		"\u0000\u0000\u00d0\u00d1\u0005\r\u0000\u0000\u00d1\u00d3\u0003\u0016\u000b"+
		"\u0000\u00d2\u00cf\u0001\u0000\u0000\u0000\u00d3\u00d6\u0001\u0000\u0000"+
		"\u0000\u00d4\u00d2\u0001\u0000\u0000\u0000\u00d4\u00d5\u0001\u0000\u0000"+
		"\u0000\u00d5\u0015\u0001\u0000\u0000\u0000\u00d6\u00d4\u0001\u0000\u0000"+
		"\u0000\u00d7\u00d8\u0006\u000b\uffff\uffff\u0000\u00d8\u00d9\u0003\u0018"+
		"\f\u0000\u00d9\u00df\u0001\u0000\u0000\u0000\u00da\u00db\n\u0002\u0000"+
		"\u0000\u00db\u00dc\u0005\u000e\u0000\u0000\u00dc\u00de\u0003\u0018\f\u0000"+
		"\u00dd\u00da\u0001\u0000\u0000\u0000\u00de\u00e1\u0001\u0000\u0000\u0000"+
		"\u00df\u00dd\u0001\u0000\u0000\u0000\u00df\u00e0\u0001\u0000\u0000\u0000"+
		"\u00e0\u0017\u0001\u0000\u0000\u0000\u00e1\u00df\u0001\u0000\u0000\u0000"+
		"\u00e2\u00e3\u0006\f\uffff\uffff\u0000\u00e3\u00e4\u0003\u001a\r\u0000"+
		"\u00e4\u00ed\u0001\u0000\u0000\u0000\u00e5\u00e8\n\u0002\u0000\u0000\u00e6"+
		"\u00e9\u0005\u000f\u0000\u0000\u00e7\u00e9\u0005\u0010\u0000\u0000\u00e8"+
		"\u00e6\u0001\u0000\u0000\u0000\u00e8\u00e7\u0001\u0000\u0000\u0000\u00e9"+
		"\u00ea\u0001\u0000\u0000\u0000\u00ea\u00ec\u0003\u001a\r\u0000\u00eb\u00e5"+
		"\u0001\u0000\u0000\u0000\u00ec\u00ef\u0001\u0000\u0000\u0000\u00ed\u00eb"+
		"\u0001\u0000\u0000\u0000\u00ed\u00ee\u0001\u0000\u0000\u0000\u00ee\u0019"+
		"\u0001\u0000\u0000\u0000\u00ef\u00ed\u0001\u0000\u0000\u0000\u00f0\u00f1"+
		"\u0006\r\uffff\uffff\u0000\u00f1\u00f2\u0003\u001c\u000e\u0000\u00f2\u00fd"+
		"\u0001\u0000\u0000\u0000\u00f3\u00f8\n\u0002\u0000\u0000\u00f4\u00f9\u0005"+
		"\u0011\u0000\u0000\u00f5\u00f9\u0005\u0012\u0000\u0000\u00f6\u00f9\u0005"+
		"\u0013\u0000\u0000\u00f7\u00f9\u0005\u0014\u0000\u0000\u00f8\u00f4\u0001"+
		"\u0000\u0000\u0000\u00f8\u00f5\u0001\u0000\u0000\u0000\u00f8\u00f6\u0001"+
		"\u0000\u0000\u0000\u00f8\u00f7\u0001\u0000\u0000\u0000\u00f9\u00fa\u0001"+
		"\u0000\u0000\u0000\u00fa\u00fc\u0003\u001c\u000e\u0000\u00fb\u00f3\u0001"+
		"\u0000\u0000\u0000\u00fc\u00ff\u0001\u0000\u0000\u0000\u00fd\u00fb\u0001"+
		"\u0000\u0000\u0000\u00fd\u00fe\u0001\u0000\u0000\u0000\u00fe\u001b\u0001"+
		"\u0000\u0000\u0000\u00ff\u00fd\u0001\u0000\u0000\u0000\u0100\u0101\u0006"+
		"\u000e\uffff\uffff\u0000\u0101\u0102\u0003\u001e\u000f\u0000\u0102\u010b"+
		"\u0001\u0000\u0000\u0000\u0103\u0106\n\u0002\u0000\u0000\u0104\u0107\u0005"+
		"\u0015\u0000\u0000\u0105\u0107\u0005\u0016\u0000\u0000\u0106\u0104\u0001"+
		"\u0000\u0000\u0000\u0106\u0105\u0001\u0000\u0000\u0000\u0107\u0108\u0001"+
		"\u0000\u0000\u0000\u0108\u010a\u0003\u001e\u000f\u0000\u0109\u0103\u0001"+
		"\u0000\u0000\u0000\u010a\u010d\u0001\u0000\u0000\u0000\u010b\u0109\u0001"+
		"\u0000\u0000\u0000\u010b\u010c\u0001\u0000\u0000\u0000\u010c\u001d\u0001"+
		"\u0000\u0000\u0000\u010d\u010b\u0001\u0000\u0000\u0000\u010e\u010f\u0006"+
		"\u000f\uffff\uffff\u0000\u010f\u0110\u0003 \u0010\u0000\u0110\u0119\u0001"+
		"\u0000\u0000\u0000\u0111\u0114\n\u0002\u0000\u0000\u0112\u0115\u0005\u0017"+
		"\u0000\u0000\u0113\u0115\u0005\u0018\u0000\u0000\u0114\u0112\u0001\u0000"+
		"\u0000\u0000\u0114\u0113\u0001\u0000\u0000\u0000\u0115\u0116\u0001\u0000"+
		"\u0000\u0000\u0116\u0118\u0003 \u0010\u0000\u0117\u0111\u0001\u0000\u0000"+
		"\u0000\u0118\u011b\u0001\u0000\u0000\u0000\u0119\u0117\u0001\u0000\u0000"+
		"\u0000\u0119\u011a\u0001\u0000\u0000\u0000\u011a\u001f\u0001\u0000\u0000"+
		"\u0000\u011b\u0119\u0001\u0000\u0000\u0000\u011c\u011f\u0005\u0019\u0000"+
		"\u0000\u011d\u011f\u0005\u0015\u0000\u0000\u011e\u011c\u0001\u0000\u0000"+
		"\u0000\u011e\u011d\u0001\u0000\u0000\u0000\u011f\u0120\u0001\u0000\u0000"+
		"\u0000\u0120\u0123\u0003 \u0010\u0000\u0121\u0123\u0003L&\u0000\u0122"+
		"\u011e\u0001\u0000\u0000\u0000\u0122\u0121\u0001\u0000\u0000\u0000\u0123"+
		"!\u0001\u0000\u0000\u0000\u0124\u0125\u0005:\u0000\u0000\u0125\u0127\u0005"+
		"\u0002\u0000\u0000\u0126\u0128\u0003D\"\u0000\u0127\u0126\u0001\u0000"+
		"\u0000\u0000\u0127\u0128\u0001\u0000\u0000\u0000\u0128\u0129\u0001\u0000"+
		"\u0000\u0000\u0129\u012a\u0005\u0003\u0000\u0000\u012a#\u0001\u0000\u0000"+
		"\u0000\u012b\u012d\u0005\u001a\u0000\u0000\u012c\u012e\u0003&\u0013\u0000"+
		"\u012d\u012c\u0001\u0000\u0000\u0000\u012d\u012e\u0001\u0000\u0000\u0000"+
		"\u012e\u012f\u0001\u0000\u0000\u0000\u012f\u0130\u0005\u001b\u0000\u0000"+
		"\u0130%\u0001\u0000\u0000\u0000\u0131\u0132\u0003\u0006\u0003\u0000\u0132"+
		"\u0133\u0003&\u0013\u0000\u0133\u0136\u0001\u0000\u0000\u0000\u0134\u0136"+
		"\u0003\u0006\u0003\u0000\u0135\u0131\u0001\u0000\u0000\u0000\u0135\u0134"+
		"\u0001\u0000\u0000\u0000\u0136\'\u0001\u0000\u0000\u0000\u0137\u0139\u0005"+
		"\u001c\u0000\u0000\u0138\u013a\u0003\f\u0006\u0000\u0139\u0138\u0001\u0000"+
		"\u0000\u0000\u0139\u013a\u0001\u0000\u0000\u0000\u013a\u013b\u0001\u0000"+
		"\u0000\u0000\u013b\u013c\u0005\u0004\u0000\u0000\u013c)\u0001\u0000\u0000"+
		"\u0000\u013d\u013e\u0005\u001d\u0000\u0000\u013e\u013f\u0003\f\u0006\u0000"+
		"\u013f\u0141\u0003\u0006\u0003\u0000\u0140\u0142\u0003,\u0016\u0000\u0141"+
		"\u0140\u0001\u0000\u0000\u0000\u0141\u0142\u0001\u0000\u0000\u0000\u0142"+
		"+\u0001\u0000\u0000\u0000\u0143\u0144\u0005\u001e\u0000\u0000\u0144\u0145"+
		"\u0003\u0006\u0003\u0000\u0145-\u0001\u0000\u0000\u0000\u0146\u0147\u0005"+
		"7\u0000\u0000\u0147\u0148\u00032\u0019\u0000\u0148/\u0001\u0000\u0000"+
		"\u0000\u0149\u014a\u00032\u0019\u0000\u014a\u014b\u0005:\u0000\u0000\u014b"+
		"1\u0001\u0000\u0000\u0000\u014c\u014d\u0006\u0019\uffff\uffff\u0000\u014d"+
		"\u014e\u0005!\u0000\u0000\u014e\u014f\u00032\u0019\u0000\u014f\u0150\u0005"+
		"7\u0000\u0000\u0150\u0151\u00032\u0019\u0000\u0151\u0152\u0005\"\u0000"+
		"\u0000\u0152\u0155\u0001\u0000\u0000\u0000\u0153\u0155\u0005:\u0000\u0000"+
		"\u0154\u014c\u0001\u0000\u0000\u0000\u0154\u0153\u0001\u0000\u0000\u0000"+
		"\u0155\u015c\u0001\u0000\u0000\u0000\u0156\u0157\n\u0004\u0000\u0000\u0157"+
		"\u015b\u0005\u001f\u0000\u0000\u0158\u0159\n\u0003\u0000\u0000\u0159\u015b"+
		"\u0005 \u0000\u0000\u015a\u0156\u0001\u0000\u0000\u0000\u015a\u0158\u0001"+
		"\u0000\u0000\u0000\u015b\u015e\u0001\u0000\u0000\u0000\u015c\u015a\u0001"+
		"\u0000\u0000\u0000\u015c\u015d\u0001\u0000\u0000\u0000\u015d3\u0001\u0000"+
		"\u0000\u0000\u015e\u015c\u0001\u0000\u0000\u0000\u015f\u0160\u00030\u0018"+
		"\u0000\u0160\u0161\u0005#\u0000\u0000\u0161\u0162\u00034\u001a\u0000\u0162"+
		"\u0165\u0001\u0000\u0000\u0000\u0163\u0165\u00030\u0018\u0000\u0164\u015f"+
		"\u0001\u0000\u0000\u0000\u0164\u0163\u0001\u0000\u0000\u0000\u01655\u0001"+
		"\u0000\u0000\u0000\u0166\u0167\u0005$\u0000\u0000\u0167\u0168\u0005:\u0000"+
		"\u0000\u0168\u016a\u0005\u001a\u0000\u0000\u0169\u016b\u0003:\u001d\u0000"+
		"\u016a\u0169\u0001\u0000\u0000\u0000\u016a\u016b\u0001\u0000\u0000\u0000"+
		"\u016b\u016c\u0001\u0000\u0000\u0000\u016c\u016d\u0005\u001b\u0000\u0000"+
		"\u016d7\u0001\u0000\u0000\u0000\u016e\u016f\u0005%\u0000\u0000\u016f\u0170"+
		"\u0005:\u0000\u0000\u0170\u0172\u0005\u001a\u0000\u0000\u0171\u0173\u0003"+
		"@ \u0000\u0172\u0171\u0001\u0000\u0000\u0000\u0172\u0173\u0001\u0000\u0000"+
		"\u0000\u0173\u0174\u0001\u0000\u0000\u0000\u0174\u0175\u0005\u001b\u0000"+
		"\u0000\u01759\u0001\u0000\u0000\u0000\u0176\u0177\u0003<\u001e\u0000\u0177"+
		"\u0178\u0005#\u0000\u0000\u0178\u0179\u0003:\u001d\u0000\u0179\u017c\u0001"+
		"\u0000\u0000\u0000\u017a\u017c\u0003<\u001e\u0000\u017b\u0176\u0001\u0000"+
		"\u0000\u0000\u017b\u017a\u0001\u0000\u0000\u0000\u017c;\u0001\u0000\u0000"+
		"\u0000\u017d\u0185\u0005:\u0000\u0000\u017e\u017f\u0005:\u0000\u0000\u017f"+
		"\u0181\u0005\u001a\u0000\u0000\u0180\u0182\u0003@ \u0000\u0181\u0180\u0001"+
		"\u0000\u0000\u0000\u0181\u0182\u0001\u0000\u0000\u0000\u0182\u0183\u0001"+
		"\u0000\u0000\u0000\u0183\u0185\u0005\u001b\u0000\u0000\u0184\u017d\u0001"+
		"\u0000\u0000\u0000\u0184\u017e\u0001\u0000\u0000\u0000\u0185=\u0001\u0000"+
		"\u0000\u0000\u0186\u0187\u0005:\u0000\u0000\u0187\u0188\u0005:\u0000\u0000"+
		"\u0188\u018a\u0005\u001a\u0000\u0000\u0189\u018b\u0003@ \u0000\u018a\u0189"+
		"\u0001\u0000\u0000\u0000\u018a\u018b\u0001\u0000\u0000\u0000\u018b\u018c"+
		"\u0001\u0000\u0000\u0000\u018c\u018d\u0005\u001b\u0000\u0000\u018d?\u0001"+
		"\u0000\u0000\u0000\u018e\u018f\u0003B!\u0000\u018f\u0190\u0005#\u0000"+
		"\u0000\u0190\u0191\u0003@ \u0000\u0191\u0194\u0001\u0000\u0000\u0000\u0192"+
		"\u0194\u0003B!\u0000\u0193\u018e\u0001\u0000\u0000\u0000\u0193\u0192\u0001"+
		"\u0000\u0000\u0000\u0194A\u0001\u0000\u0000\u0000\u0195\u0196\u0005:\u0000"+
		"\u0000\u0196\u0197\u0005\u000b\u0000\u0000\u0197\u0198\u0003\f\u0006\u0000"+
		"\u0198C\u0001\u0000\u0000\u0000\u0199\u019a\u0003\f\u0006\u0000\u019a"+
		"\u019b\u0005#\u0000\u0000\u019b\u019c\u0003D\"\u0000\u019c\u019f\u0001"+
		"\u0000\u0000\u0000\u019d\u019f\u0003\f\u0006\u0000\u019e\u0199\u0001\u0000"+
		"\u0000\u0000\u019e\u019d\u0001\u0000\u0000\u0000\u019fE\u0001\u0000\u0000"+
		"\u0000\u01a0\u01a1\u0005\u0002\u0000\u0000\u01a1\u01a2\u0003\f\u0006\u0000"+
		"\u01a2\u01a3\u0005\u0003\u0000\u0000\u01a3G\u0001\u0000\u0000\u0000\u01a4"+
		"\u01a6\u0005!\u0000\u0000\u01a5\u01a7\u0003D\"\u0000\u01a6\u01a5\u0001"+
		"\u0000\u0000\u0000\u01a6\u01a7\u0001\u0000\u0000\u0000\u01a7\u01a8\u0001"+
		"\u0000\u0000\u0000\u01a8\u01a9\u0005\"\u0000\u0000\u01a9I\u0001\u0000"+
		"\u0000\u0000\u01aa\u01ac\u0005\u0013\u0000\u0000\u01ab\u01ad\u0003D\""+
		"\u0000\u01ac\u01ab\u0001\u0000\u0000\u0000\u01ac\u01ad\u0001\u0000\u0000"+
		"\u0000\u01ad\u01ae\u0001\u0000\u0000\u0000\u01ae\u01af\u0005\u0011\u0000"+
		"\u0000\u01afK\u0001\u0000\u0000\u0000\u01b0\u01bc\u0005:\u0000\u0000\u01b1"+
		"\u01bc\u0005@\u0000\u0000\u01b2\u01bc\u00058\u0000\u0000\u01b3\u01bc\u0005"+
		"9\u0000\u0000\u01b4\u01bc\u0005;\u0000\u0000\u01b5\u01bc\u0005<\u0000"+
		"\u0000\u01b6\u01bc\u0003<\u001e\u0000\u01b7\u01bc\u0003J%\u0000\u01b8"+
		"\u01bc\u0003F#\u0000\u01b9\u01bc\u0003\"\u0011\u0000\u01ba\u01bc\u0003"+
		"H$\u0000\u01bb\u01b0\u0001\u0000\u0000\u0000\u01bb\u01b1\u0001\u0000\u0000"+
		"\u0000\u01bb\u01b2\u0001\u0000\u0000\u0000\u01bb\u01b3\u0001\u0000\u0000"+
		"\u0000\u01bb\u01b4\u0001\u0000\u0000\u0000\u01bb\u01b5\u0001\u0000\u0000"+
		"\u0000\u01bb\u01b6\u0001\u0000\u0000\u0000\u01bb\u01b7\u0001\u0000\u0000"+
		"\u0000\u01bb\u01b8\u0001\u0000\u0000\u0000\u01bb\u01b9\u0001\u0000\u0000"+
		"\u0000\u01bb\u01ba\u0001\u0000\u0000\u0000\u01bcM\u0001\u0000\u0000\u0000"+
		"\u01bd\u01be\u0005&\u0000\u0000\u01be\u01bf\u0005:\u0000\u0000\u01bf\u01c1"+
		"\u0005\u001a\u0000\u0000\u01c0\u01c2\u0003P(\u0000\u01c1\u01c0\u0001\u0000"+
		"\u0000\u0000\u01c1\u01c2\u0001\u0000\u0000\u0000\u01c2\u01c3\u0001\u0000"+
		"\u0000\u0000\u01c3\u01c4\u0005\u001b\u0000\u0000\u01c4O\u0001\u0000\u0000"+
		"\u0000\u01c5\u01c7\u0003R)\u0000\u01c6\u01c8\u0005\u0004\u0000\u0000\u01c7"+
		"\u01c6\u0001\u0000\u0000\u0000\u01c7\u01c8\u0001\u0000\u0000\u0000\u01c8"+
		"\u01ca\u0001\u0000\u0000\u0000\u01c9\u01cb\u0003P(\u0000\u01ca\u01c9\u0001"+
		"\u0000\u0000\u0000\u01ca\u01cb\u0001\u0000\u0000\u0000\u01cbQ\u0001\u0000"+
		"\u0000\u0000\u01cc\u01cf\u0003Z-\u0000\u01cd\u01cf\u0003T*\u0000\u01ce"+
		"\u01cc\u0001\u0000\u0000\u0000\u01ce\u01cd\u0001\u0000\u0000\u0000\u01cf"+
		"S\u0001\u0000\u0000\u0000\u01d0\u01d2\u0003V+\u0000\u01d1\u01d3\u0003"+
		"X,\u0000\u01d2\u01d1\u0001\u0000\u0000\u0000\u01d3\u01d4\u0001\u0000\u0000"+
		"\u0000\u01d4\u01d2\u0001\u0000\u0000\u0000\u01d4\u01d5\u0001\u0000\u0000"+
		"\u0000\u01d5\u01d7\u0001\u0000\u0000\u0000\u01d6\u01d8\u0003\\.\u0000"+
		"\u01d7\u01d6\u0001\u0000\u0000\u0000\u01d7\u01d8\u0001\u0000\u0000\u0000"+
		"\u01d8U\u0001\u0000\u0000\u0000\u01d9\u01da\u0005:\u0000\u0000\u01da\u01db"+
		"\u0005#\u0000\u0000\u01db\u01de\u0003V+\u0000\u01dc\u01de\u0005:\u0000"+
		"\u0000\u01dd\u01d9\u0001\u0000\u0000\u0000\u01dd\u01dc\u0001\u0000\u0000"+
		"\u0000\u01deW\u0001\u0000\u0000\u0000\u01df\u01e0\u00057\u0000\u0000\u01e0"+
		"\u01e1\u0003V+\u0000\u01e1Y\u0001\u0000\u0000\u0000\u01e2\u01e4\u0005"+
		":\u0000\u0000\u01e3\u01e5\u0003\\.\u0000\u01e4\u01e3\u0001\u0000\u0000"+
		"\u0000\u01e4\u01e5\u0001\u0000\u0000\u0000\u01e5[\u0001\u0000\u0000\u0000"+
		"\u01e6\u01e8\u0005!\u0000\u0000\u01e7\u01e9\u0003^/\u0000\u01e8\u01e7"+
		"\u0001\u0000\u0000\u0000\u01e9\u01ea\u0001\u0000\u0000\u0000\u01ea\u01e8"+
		"\u0001\u0000\u0000\u0000\u01ea\u01eb\u0001\u0000\u0000\u0000\u01eb\u01ec"+
		"\u0001\u0000\u0000\u0000\u01ec\u01ed\u0005\"\u0000\u0000\u01ed]\u0001"+
		"\u0000\u0000\u0000\u01ee\u01ef\u0005:\u0000\u0000\u01ef\u01f0\u0005\n"+
		"\u0000\u0000\u01f0\u01f2\u0005:\u0000\u0000\u01f1\u01f3\u0007\u0000\u0000"+
		"\u0000\u01f2\u01f1\u0001\u0000\u0000\u0000\u01f2\u01f3\u0001\u0000\u0000"+
		"\u0000\u01f3\u01fb\u0001\u0000\u0000\u0000\u01f4\u01f5\u0005\'\u0000\u0000"+
		"\u01f5\u01f6\u0005\n\u0000\u0000\u01f6\u01f8\u0003`0\u0000\u01f7\u01f9"+
		"\u0007\u0000\u0000\u0000\u01f8\u01f7\u0001\u0000\u0000\u0000\u01f8\u01f9"+
		"\u0001\u0000\u0000\u0000\u01f9\u01fb\u0001\u0000\u0000\u0000\u01fa\u01ee"+
		"\u0001\u0000\u0000\u0000\u01fa\u01f4\u0001\u0000\u0000\u0000\u01fb_\u0001"+
		"\u0000\u0000\u0000\u01fc\u0204\u0007\u0001\u0000\u0000\u01fd\u0204\u0007"+
		"\u0002\u0000\u0000\u01fe\u0204\u0007\u0003\u0000\u0000\u01ff\u0204\u0007"+
		"\u0004\u0000\u0000\u0200\u0204\u0007\u0005\u0000\u0000\u0201\u0204\u0007"+
		"\u0006\u0000\u0000\u0202\u0204\u0007\u0007\u0000\u0000\u0203\u01fc\u0001"+
		"\u0000\u0000\u0000\u0203\u01fd\u0001\u0000\u0000\u0000\u0203\u01fe\u0001"+
		"\u0000\u0000\u0000\u0203\u01ff\u0001\u0000\u0000\u0000\u0203\u0200\u0001"+
		"\u0000\u0000\u0000\u0203\u0201\u0001\u0000\u0000\u0000\u0203\u0202\u0001"+
		"\u0000\u0000\u0000\u0204a\u0001\u0000\u0000\u0000:eouy\u0085\u009b\u00a9"+
		"\u00ad\u00b2\u00b7\u00b9\u00c0\u00ca\u00d4\u00df\u00e8\u00ed\u00f8\u00fd"+
		"\u0106\u010b\u0114\u0119\u011e\u0122\u0127\u012d\u0135\u0139\u0141\u0154"+
		"\u015a\u015c\u0164\u016a\u0172\u017b\u0181\u0184\u018a\u0193\u019e\u01a6"+
		"\u01ac\u01bb\u01c1\u01c7\u01ca\u01ce\u01d4\u01d7\u01dd\u01e4\u01ea\u01f2"+
		"\u01f8\u01fa\u0203";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}