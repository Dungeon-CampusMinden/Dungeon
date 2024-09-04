// Generated from D:/Documents/Forschungsprojekt/Dungeon/blockly_visualization/src/blockly.g4 by ANTLR 4.13.1
package antlr;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class blocklyParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, WS=4, IF=5, BOOLEAN=6, COMPARE_OPERATOR=7, AND=8, 
		OR=9, MULTI=10, DIV=11, PLUS=12, MINUS=13, NOT=14, ID=15, INT=16, STRING=17;
	public static final int
		RULE_start = 0, RULE_expr = 1, RULE_right_value = 2, RULE_func_call = 3, 
		RULE_var = 4, RULE_value = 5, RULE_arguments = 6;
	private static String[] makeRuleNames() {
		return new String[] {
			"start", "expr", "right_value", "func_call", "var", "value", "arguments"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'('", "')'", "','", null, "'falls'", null, null, "'&&'", "'||'", 
			"'*'", "'/'", "'+'", "'-'", "'nicht'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, "WS", "IF", "BOOLEAN", "COMPARE_OPERATOR", "AND", 
			"OR", "MULTI", "DIV", "PLUS", "MINUS", "NOT", "ID", "INT", "STRING"
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
	public String getGrammarFileName() { return "blockly.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public blocklyParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StartContext extends ParserRuleContext {
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public StartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_start; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof blocklyListener ) ((blocklyListener)listener).enterStart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof blocklyListener ) ((blocklyListener)listener).exitStart(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof blocklyVisitor ) return ((blocklyVisitor<? extends T>)visitor).visitStart(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StartContext start() throws RecognitionException {
		StartContext _localctx = new StartContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_start);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(21);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 114754L) != 0)) {
				{
				{
				setState(15); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(14);
						expr(0);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(17); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				}
				setState(23);
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
	public static class ExprContext extends ParserRuleContext {
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
	 
		public ExprContext() { }
		public void copyFrom(ExprContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Parenthese_ExprContext extends ExprContext {
		public ExprContext ex;
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public Parenthese_ExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof blocklyListener ) ((blocklyListener)listener).enterParenthese_Expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof blocklyListener ) ((blocklyListener)listener).exitParenthese_Expr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof blocklyVisitor ) return ((blocklyVisitor<? extends T>)visitor).visitParenthese_Expr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Compare_ExprContext extends ExprContext {
		public ExprContext left;
		public Token op;
		public ExprContext right;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode COMPARE_OPERATOR() { return getToken(blocklyParser.COMPARE_OPERATOR, 0); }
		public Compare_ExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof blocklyListener ) ((blocklyListener)listener).enterCompare_Expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof blocklyListener ) ((blocklyListener)listener).exitCompare_Expr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof blocklyVisitor ) return ((blocklyVisitor<? extends T>)visitor).visitCompare_Expr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Logic_ExprContext extends ExprContext {
		public ExprContext left;
		public Token op;
		public ExprContext right;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode AND() { return getToken(blocklyParser.AND, 0); }
		public TerminalNode OR() { return getToken(blocklyParser.OR, 0); }
		public Logic_ExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof blocklyListener ) ((blocklyListener)listener).enterLogic_Expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof blocklyListener ) ((blocklyListener)listener).exitLogic_Expr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof blocklyVisitor ) return ((blocklyVisitor<? extends T>)visitor).visitLogic_Expr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Atom_ExprContext extends ExprContext {
		public Right_valueContext right_value() {
			return getRuleContext(Right_valueContext.class,0);
		}
		public Atom_ExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof blocklyListener ) ((blocklyListener)listener).enterAtom_Expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof blocklyListener ) ((blocklyListener)listener).exitAtom_Expr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof blocklyVisitor ) return ((blocklyVisitor<? extends T>)visitor).visitAtom_Expr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Not_ExprContext extends ExprContext {
		public ExprContext right;
		public TerminalNode NOT() { return getToken(blocklyParser.NOT, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public Not_ExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof blocklyListener ) ((blocklyListener)listener).enterNot_Expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof blocklyListener ) ((blocklyListener)listener).exitNot_Expr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof blocklyVisitor ) return ((blocklyVisitor<? extends T>)visitor).visitNot_Expr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		return expr(0);
	}

	private ExprContext expr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExprContext _localctx = new ExprContext(_ctx, _parentState);
		ExprContext _prevctx = _localctx;
		int _startState = 2;
		enterRecursionRule(_localctx, 2, RULE_expr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(32);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
				{
				_localctx = new Parenthese_ExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(25);
				match(T__0);
				setState(26);
				((Parenthese_ExprContext)_localctx).ex = expr(0);
				setState(27);
				match(T__1);
				}
				break;
			case NOT:
				{
				_localctx = new Not_ExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(29);
				match(NOT);
				setState(30);
				((Not_ExprContext)_localctx).right = expr(3);
				}
				break;
			case BOOLEAN:
			case ID:
			case INT:
				{
				_localctx = new Atom_ExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(31);
				right_value();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(42);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(40);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
					case 1:
						{
						_localctx = new Compare_ExprContext(new ExprContext(_parentctx, _parentState));
						((Compare_ExprContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(34);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(35);
						((Compare_ExprContext)_localctx).op = match(COMPARE_OPERATOR);
						setState(36);
						((Compare_ExprContext)_localctx).right = expr(5);
						}
						break;
					case 2:
						{
						_localctx = new Logic_ExprContext(new ExprContext(_parentctx, _parentState));
						((Logic_ExprContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(37);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(38);
						((Logic_ExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==AND || _la==OR) ) {
							((Logic_ExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(39);
						((Logic_ExprContext)_localctx).right = expr(3);
						}
						break;
					}
					} 
				}
				setState(44);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
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
	public static class Right_valueContext extends ParserRuleContext {
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public VarContext var() {
			return getRuleContext(VarContext.class,0);
		}
		public Func_callContext func_call() {
			return getRuleContext(Func_callContext.class,0);
		}
		public Right_valueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_right_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof blocklyListener ) ((blocklyListener)listener).enterRight_value(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof blocklyListener ) ((blocklyListener)listener).exitRight_value(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof blocklyVisitor ) return ((blocklyVisitor<? extends T>)visitor).visitRight_value(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Right_valueContext right_value() throws RecognitionException {
		Right_valueContext _localctx = new Right_valueContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_right_value);
		try {
			setState(48);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(45);
				value();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(46);
				var();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(47);
				func_call();
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
	public static class Func_callContext extends ParserRuleContext {
		public Token id;
		public ArgumentsContext args;
		public TerminalNode ID() { return getToken(blocklyParser.ID, 0); }
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public Func_callContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_func_call; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof blocklyListener ) ((blocklyListener)listener).enterFunc_call(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof blocklyListener ) ((blocklyListener)listener).exitFunc_call(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof blocklyVisitor ) return ((blocklyVisitor<? extends T>)visitor).visitFunc_call(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Func_callContext func_call() throws RecognitionException {
		Func_callContext _localctx = new Func_callContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_func_call);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(50);
			((Func_callContext)_localctx).id = match(ID);
			setState(51);
			match(T__0);
			setState(53);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 114754L) != 0)) {
				{
				setState(52);
				((Func_callContext)_localctx).args = arguments();
				}
			}

			setState(55);
			match(T__1);
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
	public static class VarContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(blocklyParser.ID, 0); }
		public VarContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_var; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof blocklyListener ) ((blocklyListener)listener).enterVar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof blocklyListener ) ((blocklyListener)listener).exitVar(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof blocklyVisitor ) return ((blocklyVisitor<? extends T>)visitor).visitVar(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VarContext var() throws RecognitionException {
		VarContext _localctx = new VarContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_var);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(57);
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
	public static class ValueContext extends ParserRuleContext {
		public TerminalNode BOOLEAN() { return getToken(blocklyParser.BOOLEAN, 0); }
		public TerminalNode INT() { return getToken(blocklyParser.INT, 0); }
		public ValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof blocklyListener ) ((blocklyListener)listener).enterValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof blocklyListener ) ((blocklyListener)listener).exitValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof blocklyVisitor ) return ((blocklyVisitor<? extends T>)visitor).visitValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValueContext value() throws RecognitionException {
		ValueContext _localctx = new ValueContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_value);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(59);
			_la = _input.LA(1);
			if ( !(_la==BOOLEAN || _la==INT) ) {
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
	public static class ArgumentsContext extends ParserRuleContext {
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public ArgumentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arguments; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof blocklyListener ) ((blocklyListener)listener).enterArguments(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof blocklyListener ) ((blocklyListener)listener).exitArguments(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof blocklyVisitor ) return ((blocklyVisitor<? extends T>)visitor).visitArguments(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArgumentsContext arguments() throws RecognitionException {
		ArgumentsContext _localctx = new ArgumentsContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_arguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(65); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(61);
				expr(0);
				setState(63);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__2) {
					{
					setState(62);
					match(T__2);
					}
				}

				}
				}
				setState(67); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 114754L) != 0) );
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
		case 1:
			return expr_sempred((ExprContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expr_sempred(ExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 4);
		case 1:
			return precpred(_ctx, 2);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u0011F\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0001\u0000\u0004\u0000\u0010"+
		"\b\u0000\u000b\u0000\f\u0000\u0011\u0005\u0000\u0014\b\u0000\n\u0000\f"+
		"\u0000\u0017\t\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001!\b\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0005"+
		"\u0001)\b\u0001\n\u0001\f\u0001,\t\u0001\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0003\u00021\b\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0003"+
		"\u00036\b\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001"+
		"\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0003\u0006@\b\u0006\u0004"+
		"\u0006B\b\u0006\u000b\u0006\f\u0006C\u0001\u0006\u0000\u0001\u0002\u0007"+
		"\u0000\u0002\u0004\u0006\b\n\f\u0000\u0002\u0001\u0000\b\t\u0002\u0000"+
		"\u0006\u0006\u0010\u0010I\u0000\u0015\u0001\u0000\u0000\u0000\u0002 \u0001"+
		"\u0000\u0000\u0000\u00040\u0001\u0000\u0000\u0000\u00062\u0001\u0000\u0000"+
		"\u0000\b9\u0001\u0000\u0000\u0000\n;\u0001\u0000\u0000\u0000\fA\u0001"+
		"\u0000\u0000\u0000\u000e\u0010\u0003\u0002\u0001\u0000\u000f\u000e\u0001"+
		"\u0000\u0000\u0000\u0010\u0011\u0001\u0000\u0000\u0000\u0011\u000f\u0001"+
		"\u0000\u0000\u0000\u0011\u0012\u0001\u0000\u0000\u0000\u0012\u0014\u0001"+
		"\u0000\u0000\u0000\u0013\u000f\u0001\u0000\u0000\u0000\u0014\u0017\u0001"+
		"\u0000\u0000\u0000\u0015\u0013\u0001\u0000\u0000\u0000\u0015\u0016\u0001"+
		"\u0000\u0000\u0000\u0016\u0001\u0001\u0000\u0000\u0000\u0017\u0015\u0001"+
		"\u0000\u0000\u0000\u0018\u0019\u0006\u0001\uffff\uffff\u0000\u0019\u001a"+
		"\u0005\u0001\u0000\u0000\u001a\u001b\u0003\u0002\u0001\u0000\u001b\u001c"+
		"\u0005\u0002\u0000\u0000\u001c!\u0001\u0000\u0000\u0000\u001d\u001e\u0005"+
		"\u000e\u0000\u0000\u001e!\u0003\u0002\u0001\u0003\u001f!\u0003\u0004\u0002"+
		"\u0000 \u0018\u0001\u0000\u0000\u0000 \u001d\u0001\u0000\u0000\u0000 "+
		"\u001f\u0001\u0000\u0000\u0000!*\u0001\u0000\u0000\u0000\"#\n\u0004\u0000"+
		"\u0000#$\u0005\u0007\u0000\u0000$)\u0003\u0002\u0001\u0005%&\n\u0002\u0000"+
		"\u0000&\'\u0007\u0000\u0000\u0000\')\u0003\u0002\u0001\u0003(\"\u0001"+
		"\u0000\u0000\u0000(%\u0001\u0000\u0000\u0000),\u0001\u0000\u0000\u0000"+
		"*(\u0001\u0000\u0000\u0000*+\u0001\u0000\u0000\u0000+\u0003\u0001\u0000"+
		"\u0000\u0000,*\u0001\u0000\u0000\u0000-1\u0003\n\u0005\u0000.1\u0003\b"+
		"\u0004\u0000/1\u0003\u0006\u0003\u00000-\u0001\u0000\u0000\u00000.\u0001"+
		"\u0000\u0000\u00000/\u0001\u0000\u0000\u00001\u0005\u0001\u0000\u0000"+
		"\u000023\u0005\u000f\u0000\u000035\u0005\u0001\u0000\u000046\u0003\f\u0006"+
		"\u000054\u0001\u0000\u0000\u000056\u0001\u0000\u0000\u000067\u0001\u0000"+
		"\u0000\u000078\u0005\u0002\u0000\u00008\u0007\u0001\u0000\u0000\u0000"+
		"9:\u0005\u000f\u0000\u0000:\t\u0001\u0000\u0000\u0000;<\u0007\u0001\u0000"+
		"\u0000<\u000b\u0001\u0000\u0000\u0000=?\u0003\u0002\u0001\u0000>@\u0005"+
		"\u0003\u0000\u0000?>\u0001\u0000\u0000\u0000?@\u0001\u0000\u0000\u0000"+
		"@B\u0001\u0000\u0000\u0000A=\u0001\u0000\u0000\u0000BC\u0001\u0000\u0000"+
		"\u0000CA\u0001\u0000\u0000\u0000CD\u0001\u0000\u0000\u0000D\r\u0001\u0000"+
		"\u0000\u0000\t\u0011\u0015 (*05?C";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}