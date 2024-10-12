// Generated from C:/Users/bjarn/VS_Projects/Dungeon/dungeon-diagnostics/SemanticAnalysis.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class SemanticAnalysisParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, DOUBLE_LINE=11, ARROW=12, TRUE=13, FALSE=14, VAR=15, ID=16, NUM=17, 
		NUM_DEC=18, WS=19, LINE_COMMENT=20, BLOCK_COMMENT=21, STRING_LITERAL=22, 
		LBRACE=23, RBRACE=24, COMMA=25, DOT=26, LPAREN=27, RPAREN=28;
	public static final int
		RULE_program = 0, RULE_statement = 1, RULE_var_decl = 2, RULE_expression = 3, 
		RULE_type_decl = 4, RULE_taskTypes = 5, RULE_otherCode = 6;
	private static String[] makeRuleNames() {
		return new String[] {
			"program", "statement", "var_decl", "expression", "type_decl", "taskTypes", 
			"otherCode"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'='", "';'", "':'", "'<>'", "'[]'", "'['", "']'", "'single_choice_task'", 
			"'multiple_choice_task'", "'\\n'", "'--'", "'->'", "'true'", "'false'", 
			"'var'", null, null, null, null, null, null, null, "'{'", "'}'", "','", 
			"'.'", "'('", "')'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, "DOUBLE_LINE", 
			"ARROW", "TRUE", "FALSE", "VAR", "ID", "NUM", "NUM_DEC", "WS", "LINE_COMMENT", 
			"BLOCK_COMMENT", "STRING_LITERAL", "LBRACE", "RBRACE", "COMMA", "DOT", 
			"LPAREN", "RPAREN"
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
	public String getGrammarFileName() { return "SemanticAnalysis.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }


	    private boolean isVariableDeclaration() {
	        return _input.LT(1).getType() == VAR;
	    }

	public SemanticAnalysisParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ProgramContext extends ParserRuleContext {
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public ProgramContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_program; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticAnalysisListener ) ((SemanticAnalysisListener)listener).enterProgram(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticAnalysisListener ) ((SemanticAnalysisListener)listener).exitProgram(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticAnalysisVisitor ) return ((SemanticAnalysisVisitor<? extends T>)visitor).visitProgram(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ProgramContext program() throws RecognitionException {
		ProgramContext _localctx = new ProgramContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_program);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(17);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(14);
					statement();
					}
					} 
				}
				setState(19);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
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
	public static class StatementContext extends ParserRuleContext {
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
	 
		public StatementContext() { }
		public void copyFrom(StatementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class OtherCodeStmtContext extends StatementContext {
		public OtherCodeContext otherCode() {
			return getRuleContext(OtherCodeContext.class,0);
		}
		public OtherCodeStmtContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticAnalysisListener ) ((SemanticAnalysisListener)listener).enterOtherCodeStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticAnalysisListener ) ((SemanticAnalysisListener)listener).exitOtherCodeStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticAnalysisVisitor ) return ((SemanticAnalysisVisitor<? extends T>)visitor).visitOtherCodeStmt(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class VarDeclStmtContext extends StatementContext {
		public Var_declContext var_decl() {
			return getRuleContext(Var_declContext.class,0);
		}
		public VarDeclStmtContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticAnalysisListener ) ((SemanticAnalysisListener)listener).enterVarDeclStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticAnalysisListener ) ((SemanticAnalysisListener)listener).exitVarDeclStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticAnalysisVisitor ) return ((SemanticAnalysisVisitor<? extends T>)visitor).visitVarDeclStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_statement);
		try {
			setState(22);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				_localctx = new VarDeclStmtContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(20);
				var_decl();
				}
				break;
			case 2:
				_localctx = new OtherCodeStmtContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(21);
				otherCode();
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
		public TerminalNode VAR() { return getToken(SemanticAnalysisParser.VAR, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode ID() { return getToken(SemanticAnalysisParser.ID, 0); }
		public Var_decl_assignmentContext(Var_declContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticAnalysisListener ) ((SemanticAnalysisListener)listener).enterVar_decl_assignment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticAnalysisListener ) ((SemanticAnalysisListener)listener).exitVar_decl_assignment(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticAnalysisVisitor ) return ((SemanticAnalysisVisitor<? extends T>)visitor).visitVar_decl_assignment(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Var_decl_type_declContext extends Var_declContext {
		public Token id;
		public TerminalNode VAR() { return getToken(SemanticAnalysisParser.VAR, 0); }
		public Type_declContext type_decl() {
			return getRuleContext(Type_declContext.class,0);
		}
		public TerminalNode ID() { return getToken(SemanticAnalysisParser.ID, 0); }
		public Var_decl_type_declContext(Var_declContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticAnalysisListener ) ((SemanticAnalysisListener)listener).enterVar_decl_type_decl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticAnalysisListener ) ((SemanticAnalysisListener)listener).exitVar_decl_type_decl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticAnalysisVisitor ) return ((SemanticAnalysisVisitor<? extends T>)visitor).visitVar_decl_type_decl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Var_declContext var_decl() throws RecognitionException {
		Var_declContext _localctx = new Var_declContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_var_decl);
		try {
			setState(36);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				_localctx = new Var_decl_assignmentContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(24);
				match(VAR);
				setState(25);
				((Var_decl_assignmentContext)_localctx).id = match(ID);
				setState(26);
				match(T__0);
				setState(27);
				expression();
				setState(28);
				match(T__1);
				}
				break;
			case 2:
				_localctx = new Var_decl_type_declContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(30);
				match(VAR);
				setState(31);
				((Var_decl_type_declContext)_localctx).id = match(ID);
				setState(32);
				match(T__2);
				setState(33);
				type_decl(0);
				setState(34);
				match(T__1);
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
		public TerminalNode ID() { return getToken(SemanticAnalysisParser.ID, 0); }
		public TerminalNode NUM() { return getToken(SemanticAnalysisParser.NUM, 0); }
		public TerminalNode NUM_DEC() { return getToken(SemanticAnalysisParser.NUM_DEC, 0); }
		public TerminalNode STRING_LITERAL() { return getToken(SemanticAnalysisParser.STRING_LITERAL, 0); }
		public TerminalNode TRUE() { return getToken(SemanticAnalysisParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(SemanticAnalysisParser.FALSE, 0); }
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticAnalysisListener ) ((SemanticAnalysisListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticAnalysisListener ) ((SemanticAnalysisListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticAnalysisVisitor ) return ((SemanticAnalysisVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_expression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(38);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 4677632L) != 0)) ) {
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
		public TerminalNode ARROW() { return getToken(SemanticAnalysisParser.ARROW, 0); }
		public Map_param_typeContext(Type_declContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticAnalysisListener ) ((SemanticAnalysisListener)listener).enterMap_param_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticAnalysisListener ) ((SemanticAnalysisListener)listener).exitMap_param_type(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticAnalysisVisitor ) return ((SemanticAnalysisVisitor<? extends T>)visitor).visitMap_param_type(this);
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
			if ( listener instanceof SemanticAnalysisListener ) ((SemanticAnalysisListener)listener).enterTask_types(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticAnalysisListener ) ((SemanticAnalysisListener)listener).exitTask_types(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticAnalysisVisitor ) return ((SemanticAnalysisVisitor<? extends T>)visitor).visitTask_types(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Id_param_typeContext extends Type_declContext {
		public TerminalNode ID() { return getToken(SemanticAnalysisParser.ID, 0); }
		public Id_param_typeContext(Type_declContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticAnalysisListener ) ((SemanticAnalysisListener)listener).enterId_param_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticAnalysisListener ) ((SemanticAnalysisListener)listener).exitId_param_type(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticAnalysisVisitor ) return ((SemanticAnalysisVisitor<? extends T>)visitor).visitId_param_type(this);
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
			if ( listener instanceof SemanticAnalysisListener ) ((SemanticAnalysisListener)listener).enterList_param_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticAnalysisListener ) ((SemanticAnalysisListener)listener).exitList_param_type(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticAnalysisVisitor ) return ((SemanticAnalysisVisitor<? extends T>)visitor).visitList_param_type(this);
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
			if ( listener instanceof SemanticAnalysisListener ) ((SemanticAnalysisListener)listener).enterSet_param_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticAnalysisListener ) ((SemanticAnalysisListener)listener).exitSet_param_type(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticAnalysisVisitor ) return ((SemanticAnalysisVisitor<? extends T>)visitor).visitSet_param_type(this);
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
		int _startState = 8;
		enterRecursionRule(_localctx, 8, RULE_type_decl, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(49);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__5:
				{
				_localctx = new Map_param_typeContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(41);
				match(T__5);
				setState(42);
				type_decl(0);
				setState(43);
				match(ARROW);
				setState(44);
				type_decl(0);
				setState(45);
				match(T__6);
				}
				break;
			case T__7:
			case T__8:
				{
				_localctx = new Task_typesContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(47);
				taskTypes();
				}
				break;
			case ID:
				{
				_localctx = new Id_param_typeContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(48);
				match(ID);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(57);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(55);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
					case 1:
						{
						_localctx = new Set_param_typeContext(new Type_declContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_type_decl);
						setState(51);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(52);
						match(T__3);
						}
						break;
					case 2:
						{
						_localctx = new List_param_typeContext(new Type_declContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_type_decl);
						setState(53);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(54);
						match(T__4);
						}
						break;
					}
					} 
				}
				setState(59);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
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
			if ( listener instanceof SemanticAnalysisListener ) ((SemanticAnalysisListener)listener).enterTaskTypes(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticAnalysisListener ) ((SemanticAnalysisListener)listener).exitTaskTypes(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticAnalysisVisitor ) return ((SemanticAnalysisVisitor<? extends T>)visitor).visitTaskTypes(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TaskTypesContext taskTypes() throws RecognitionException {
		TaskTypesContext _localctx = new TaskTypesContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_taskTypes);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(60);
			_la = _input.LA(1);
			if ( !(_la==T__7 || _la==T__8) ) {
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
	public static class OtherCodeContext extends ParserRuleContext {
		public List<TerminalNode> LBRACE() { return getTokens(SemanticAnalysisParser.LBRACE); }
		public TerminalNode LBRACE(int i) {
			return getToken(SemanticAnalysisParser.LBRACE, i);
		}
		public List<TerminalNode> RBRACE() { return getTokens(SemanticAnalysisParser.RBRACE); }
		public TerminalNode RBRACE(int i) {
			return getToken(SemanticAnalysisParser.RBRACE, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SemanticAnalysisParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SemanticAnalysisParser.COMMA, i);
		}
		public List<TerminalNode> DOT() { return getTokens(SemanticAnalysisParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(SemanticAnalysisParser.DOT, i);
		}
		public List<TerminalNode> LPAREN() { return getTokens(SemanticAnalysisParser.LPAREN); }
		public TerminalNode LPAREN(int i) {
			return getToken(SemanticAnalysisParser.LPAREN, i);
		}
		public List<TerminalNode> RPAREN() { return getTokens(SemanticAnalysisParser.RPAREN); }
		public TerminalNode RPAREN(int i) {
			return getToken(SemanticAnalysisParser.RPAREN, i);
		}
		public OtherCodeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_otherCode; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticAnalysisListener ) ((SemanticAnalysisListener)listener).enterOtherCode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticAnalysisListener ) ((SemanticAnalysisListener)listener).exitOtherCode(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticAnalysisVisitor ) return ((SemanticAnalysisVisitor<? extends T>)visitor).visitOtherCode(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OtherCodeContext otherCode() throws RecognitionException {
		OtherCodeContext _localctx = new OtherCodeContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_otherCode);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(62);
			if (!( !isVariableDeclaration() )) throw new FailedPredicateException(this, " !isVariableDeclaration() ");
			setState(72);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
			while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1+1 ) {
					{
					setState(70);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
					case 1:
						{
						setState(63);
						match(LBRACE);
						}
						break;
					case 2:
						{
						setState(64);
						match(RBRACE);
						}
						break;
					case 3:
						{
						setState(65);
						match(COMMA);
						}
						break;
					case 4:
						{
						setState(66);
						match(DOT);
						}
						break;
					case 5:
						{
						setState(67);
						match(LPAREN);
						}
						break;
					case 6:
						{
						setState(68);
						match(RPAREN);
						}
						break;
					case 7:
						{
						setState(69);
						matchWildcard();
						}
						break;
					}
					} 
				}
				setState(74);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
			}
			setState(75);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 25166852L) != 0)) ) {
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
		case 4:
			return type_decl_sempred((Type_declContext)_localctx, predIndex);
		case 6:
			return otherCode_sempred((OtherCodeContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean type_decl_sempred(Type_declContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 5);
		case 1:
			return precpred(_ctx, 4);
		}
		return true;
	}
	private boolean otherCode_sempred(OtherCodeContext _localctx, int predIndex) {
		switch (predIndex) {
		case 2:
			return  !isVariableDeclaration() ;
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u001cN\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0001\u0000\u0005\u0000\u0010"+
		"\b\u0000\n\u0000\f\u0000\u0013\t\u0000\u0001\u0001\u0001\u0001\u0003\u0001"+
		"\u0017\b\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0003\u0002%\b\u0002\u0001\u0003\u0001\u0003\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0003\u00042\b\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0005\u00048\b\u0004\n\u0004\f\u0004;\t\u0004"+
		"\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006"+
		"\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0005\u0006G\b\u0006"+
		"\n\u0006\f\u0006J\t\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001H\u0001"+
		"\b\u0007\u0000\u0002\u0004\u0006\b\n\f\u0000\u0003\u0003\u0000\r\u000e"+
		"\u0010\u0012\u0016\u0016\u0001\u0000\b\t\u0003\u0000\u0002\u0002\n\n\u0017"+
		"\u0018T\u0000\u0011\u0001\u0000\u0000\u0000\u0002\u0016\u0001\u0000\u0000"+
		"\u0000\u0004$\u0001\u0000\u0000\u0000\u0006&\u0001\u0000\u0000\u0000\b"+
		"1\u0001\u0000\u0000\u0000\n<\u0001\u0000\u0000\u0000\f>\u0001\u0000\u0000"+
		"\u0000\u000e\u0010\u0003\u0002\u0001\u0000\u000f\u000e\u0001\u0000\u0000"+
		"\u0000\u0010\u0013\u0001\u0000\u0000\u0000\u0011\u000f\u0001\u0000\u0000"+
		"\u0000\u0011\u0012\u0001\u0000\u0000\u0000\u0012\u0001\u0001\u0000\u0000"+
		"\u0000\u0013\u0011\u0001\u0000\u0000\u0000\u0014\u0017\u0003\u0004\u0002"+
		"\u0000\u0015\u0017\u0003\f\u0006\u0000\u0016\u0014\u0001\u0000\u0000\u0000"+
		"\u0016\u0015\u0001\u0000\u0000\u0000\u0017\u0003\u0001\u0000\u0000\u0000"+
		"\u0018\u0019\u0005\u000f\u0000\u0000\u0019\u001a\u0005\u0010\u0000\u0000"+
		"\u001a\u001b\u0005\u0001\u0000\u0000\u001b\u001c\u0003\u0006\u0003\u0000"+
		"\u001c\u001d\u0005\u0002\u0000\u0000\u001d%\u0001\u0000\u0000\u0000\u001e"+
		"\u001f\u0005\u000f\u0000\u0000\u001f \u0005\u0010\u0000\u0000 !\u0005"+
		"\u0003\u0000\u0000!\"\u0003\b\u0004\u0000\"#\u0005\u0002\u0000\u0000#"+
		"%\u0001\u0000\u0000\u0000$\u0018\u0001\u0000\u0000\u0000$\u001e\u0001"+
		"\u0000\u0000\u0000%\u0005\u0001\u0000\u0000\u0000&\'\u0007\u0000\u0000"+
		"\u0000\'\u0007\u0001\u0000\u0000\u0000()\u0006\u0004\uffff\uffff\u0000"+
		")*\u0005\u0006\u0000\u0000*+\u0003\b\u0004\u0000+,\u0005\f\u0000\u0000"+
		",-\u0003\b\u0004\u0000-.\u0005\u0007\u0000\u0000.2\u0001\u0000\u0000\u0000"+
		"/2\u0003\n\u0005\u000002\u0005\u0010\u0000\u00001(\u0001\u0000\u0000\u0000"+
		"1/\u0001\u0000\u0000\u000010\u0001\u0000\u0000\u000029\u0001\u0000\u0000"+
		"\u000034\n\u0005\u0000\u000048\u0005\u0004\u0000\u000056\n\u0004\u0000"+
		"\u000068\u0005\u0005\u0000\u000073\u0001\u0000\u0000\u000075\u0001\u0000"+
		"\u0000\u00008;\u0001\u0000\u0000\u000097\u0001\u0000\u0000\u00009:\u0001"+
		"\u0000\u0000\u0000:\t\u0001\u0000\u0000\u0000;9\u0001\u0000\u0000\u0000"+
		"<=\u0007\u0001\u0000\u0000=\u000b\u0001\u0000\u0000\u0000>H\u0004\u0006"+
		"\u0002\u0000?G\u0005\u0017\u0000\u0000@G\u0005\u0018\u0000\u0000AG\u0005"+
		"\u0019\u0000\u0000BG\u0005\u001a\u0000\u0000CG\u0005\u001b\u0000\u0000"+
		"DG\u0005\u001c\u0000\u0000EG\t\u0000\u0000\u0000F?\u0001\u0000\u0000\u0000"+
		"F@\u0001\u0000\u0000\u0000FA\u0001\u0000\u0000\u0000FB\u0001\u0000\u0000"+
		"\u0000FC\u0001\u0000\u0000\u0000FD\u0001\u0000\u0000\u0000FE\u0001\u0000"+
		"\u0000\u0000GJ\u0001\u0000\u0000\u0000HI\u0001\u0000\u0000\u0000HF\u0001"+
		"\u0000\u0000\u0000IK\u0001\u0000\u0000\u0000JH\u0001\u0000\u0000\u0000"+
		"KL\u0007\u0002\u0000\u0000L\r\u0001\u0000\u0000\u0000\b\u0011\u0016$1"+
		"79FH";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}