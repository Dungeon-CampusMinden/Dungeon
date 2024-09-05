// Generated from C:/Users/bjarn/VS_Projects/Dungeon/Dungeon-Diagnostics/DungeonDiagnostics.g4 by ANTLR 4.13.1
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
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, DOUBLE_LINE=23, ARROW=24, 
		TRUE=25, FALSE=26, ID=27, NUM=28, NUM_DEC=29, WS=30, LINE_COMMENT=31, 
		BLOCK_COMMENT=32, STRING_LITERAL=33;
	public static final int
		RULE_program = 0, RULE_definition = 1, RULE_singleChoiceTask = 2, RULE_multipleChoiceTask = 3, 
		RULE_assignTask = 4, RULE_field = 5, RULE_descriptionField = 6, RULE_answersField = 7, 
		RULE_correctAnswerIndexField = 8, RULE_correctAnswerIndicesField = 9, 
		RULE_solutionField = 10, RULE_pair = 11, RULE_term = 12, RULE_pointsField = 13, 
		RULE_pointsToPassField = 14, RULE_explanationField = 15, RULE_gradingFunctionField = 16, 
		RULE_scenarioBuilderField = 17;
	private static String[] makeRuleNames() {
		return new String[] {
			"program", "definition", "singleChoiceTask", "multipleChoiceTask", "assignTask", 
			"field", "descriptionField", "answersField", "correctAnswerIndexField", 
			"correctAnswerIndicesField", "solutionField", "pair", "term", "pointsField", 
			"pointsToPassField", "explanationField", "gradingFunctionField", "scenarioBuilderField"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'single_choice_task'", "'{'", "','", "'}'", "'multiple_choice_task'", 
			"'assign_task'", "'description'", "':'", "'answers'", "'['", "']'", "'correct_answer_index'", 
			"'correct_answer_indices'", "'solution'", "'<'", "'>'", "'_'", "'points'", 
			"'points_to_pass'", "'explanation'", "'grading_function'", "'scenario_builder'", 
			"'--'", "'->'", "'true'", "'false'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, "DOUBLE_LINE", 
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
			setState(39);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 98L) != 0)) {
				{
				{
				setState(36);
				definition();
				}
				}
				setState(41);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(42);
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
		public SingleChoiceTaskContext singleChoiceTask() {
			return getRuleContext(SingleChoiceTaskContext.class,0);
		}
		public MultipleChoiceTaskContext multipleChoiceTask() {
			return getRuleContext(MultipleChoiceTaskContext.class,0);
		}
		public AssignTaskContext assignTask() {
			return getRuleContext(AssignTaskContext.class,0);
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
			setState(47);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
				enterOuterAlt(_localctx, 1);
				{
				setState(44);
				singleChoiceTask();
				}
				break;
			case T__4:
				enterOuterAlt(_localctx, 2);
				{
				setState(45);
				multipleChoiceTask();
				}
				break;
			case T__5:
				enterOuterAlt(_localctx, 3);
				{
				setState(46);
				assignTask();
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
		enterRule(_localctx, 4, RULE_singleChoiceTask);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(49);
			match(T__0);
			setState(50);
			match(ID);
			setState(51);
			match(T__1);
			setState(52);
			field();
			setState(57);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__2) {
				{
				{
				setState(53);
				match(T__2);
				setState(54);
				field();
				}
				}
				setState(59);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(60);
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
		enterRule(_localctx, 6, RULE_multipleChoiceTask);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(62);
			match(T__4);
			setState(63);
			match(ID);
			setState(64);
			match(T__1);
			setState(65);
			field();
			setState(70);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__2) {
				{
				{
				setState(66);
				match(T__2);
				setState(67);
				field();
				}
				}
				setState(72);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(73);
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
		enterRule(_localctx, 8, RULE_assignTask);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(75);
			match(T__5);
			setState(76);
			match(ID);
			setState(77);
			match(T__1);
			setState(78);
			field();
			setState(83);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__2) {
				{
				{
				setState(79);
				match(T__2);
				setState(80);
				field();
				}
				}
				setState(85);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(86);
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
		enterRule(_localctx, 10, RULE_field);
		try {
			setState(98);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__6:
				enterOuterAlt(_localctx, 1);
				{
				setState(88);
				descriptionField();
				}
				break;
			case T__8:
				enterOuterAlt(_localctx, 2);
				{
				setState(89);
				answersField();
				}
				break;
			case T__11:
				enterOuterAlt(_localctx, 3);
				{
				setState(90);
				correctAnswerIndexField();
				}
				break;
			case T__12:
				enterOuterAlt(_localctx, 4);
				{
				setState(91);
				correctAnswerIndicesField();
				}
				break;
			case T__13:
				enterOuterAlt(_localctx, 5);
				{
				setState(92);
				solutionField();
				}
				break;
			case T__17:
				enterOuterAlt(_localctx, 6);
				{
				setState(93);
				pointsField();
				}
				break;
			case T__18:
				enterOuterAlt(_localctx, 7);
				{
				setState(94);
				pointsToPassField();
				}
				break;
			case T__19:
				enterOuterAlt(_localctx, 8);
				{
				setState(95);
				explanationField();
				}
				break;
			case T__20:
				enterOuterAlt(_localctx, 9);
				{
				setState(96);
				gradingFunctionField();
				}
				break;
			case T__21:
				enterOuterAlt(_localctx, 10);
				{
				setState(97);
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
		enterRule(_localctx, 12, RULE_descriptionField);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(100);
			match(T__6);
			setState(101);
			match(T__7);
			setState(102);
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
		enterRule(_localctx, 14, RULE_answersField);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(104);
			match(T__8);
			setState(105);
			match(T__7);
			setState(106);
			match(T__9);
			setState(107);
			match(STRING_LITERAL);
			setState(112);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__2) {
				{
				{
				setState(108);
				match(T__2);
				setState(109);
				match(STRING_LITERAL);
				}
				}
				setState(114);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(115);
			match(T__10);
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
		enterRule(_localctx, 16, RULE_correctAnswerIndexField);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(117);
			match(T__11);
			setState(118);
			match(T__7);
			setState(119);
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
		enterRule(_localctx, 18, RULE_correctAnswerIndicesField);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(121);
			match(T__12);
			setState(122);
			match(T__7);
			setState(123);
			match(T__9);
			setState(124);
			match(NUM);
			setState(129);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__2) {
				{
				{
				setState(125);
				match(T__2);
				setState(126);
				match(NUM);
				}
				}
				setState(131);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(132);
			match(T__10);
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
		enterRule(_localctx, 20, RULE_solutionField);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(134);
			match(T__13);
			setState(135);
			match(T__7);
			setState(136);
			match(T__14);
			setState(137);
			pair();
			setState(142);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__2) {
				{
				{
				setState(138);
				match(T__2);
				setState(139);
				pair();
				}
				}
				setState(144);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(145);
			match(T__15);
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
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
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
		enterRule(_localctx, 22, RULE_pair);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(147);
			match(T__9);
			setState(148);
			term();
			setState(149);
			match(T__2);
			setState(150);
			term();
			setState(151);
			match(T__10);
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
	public static class TermContext extends ParserRuleContext {
		public TerminalNode STRING_LITERAL() { return getToken(DungeonDiagnosticsParser.STRING_LITERAL, 0); }
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
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_term);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(153);
			_la = _input.LA(1);
			if ( !(_la==T__16 || _la==STRING_LITERAL) ) {
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
		enterRule(_localctx, 26, RULE_pointsField);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(155);
			match(T__17);
			setState(156);
			match(T__7);
			setState(157);
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
		enterRule(_localctx, 28, RULE_pointsToPassField);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(159);
			match(T__18);
			setState(160);
			match(T__7);
			setState(161);
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
		enterRule(_localctx, 30, RULE_explanationField);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(163);
			match(T__19);
			setState(164);
			match(T__7);
			setState(165);
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
		enterRule(_localctx, 32, RULE_gradingFunctionField);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(167);
			match(T__20);
			setState(168);
			match(T__7);
			setState(169);
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
		enterRule(_localctx, 34, RULE_scenarioBuilderField);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(171);
			match(T__21);
			setState(172);
			match(T__7);
			setState(173);
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

	public static final String _serializedATN =
		"\u0004\u0001!\u00b0\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0001\u0000\u0005\u0000"+
		"&\b\u0000\n\u0000\f\u0000)\t\u0000\u0001\u0000\u0001\u0000\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0003\u00010\b\u0001\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0005\u00028\b\u0002"+
		"\n\u0002\f\u0002;\t\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0005\u0003E\b\u0003"+
		"\n\u0003\f\u0003H\t\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0005\u0004R\b\u0004"+
		"\n\u0004\f\u0004U\t\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0003\u0005c\b\u0005\u0001\u0006\u0001\u0006"+
		"\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0005\u0007o\b\u0007\n\u0007\f\u0007r\t\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0005\t\u0080\b\t\n\t\f\t\u0083\t\t"+
		"\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0005"+
		"\n\u008d\b\n\n\n\f\n\u0090\t\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001"+
		"\r\u0001\r\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e"+
		"\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010"+
		"\u0001\u0010\u0001\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011"+
		"\u0001\u0011\u0000\u0000\u0012\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010"+
		"\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"\u0000\u0001\u0002\u0000"+
		"\u0011\u0011!!\u00af\u0000\'\u0001\u0000\u0000\u0000\u0002/\u0001\u0000"+
		"\u0000\u0000\u00041\u0001\u0000\u0000\u0000\u0006>\u0001\u0000\u0000\u0000"+
		"\bK\u0001\u0000\u0000\u0000\nb\u0001\u0000\u0000\u0000\fd\u0001\u0000"+
		"\u0000\u0000\u000eh\u0001\u0000\u0000\u0000\u0010u\u0001\u0000\u0000\u0000"+
		"\u0012y\u0001\u0000\u0000\u0000\u0014\u0086\u0001\u0000\u0000\u0000\u0016"+
		"\u0093\u0001\u0000\u0000\u0000\u0018\u0099\u0001\u0000\u0000\u0000\u001a"+
		"\u009b\u0001\u0000\u0000\u0000\u001c\u009f\u0001\u0000\u0000\u0000\u001e"+
		"\u00a3\u0001\u0000\u0000\u0000 \u00a7\u0001\u0000\u0000\u0000\"\u00ab"+
		"\u0001\u0000\u0000\u0000$&\u0003\u0002\u0001\u0000%$\u0001\u0000\u0000"+
		"\u0000&)\u0001\u0000\u0000\u0000\'%\u0001\u0000\u0000\u0000\'(\u0001\u0000"+
		"\u0000\u0000(*\u0001\u0000\u0000\u0000)\'\u0001\u0000\u0000\u0000*+\u0005"+
		"\u0000\u0000\u0001+\u0001\u0001\u0000\u0000\u0000,0\u0003\u0004\u0002"+
		"\u0000-0\u0003\u0006\u0003\u0000.0\u0003\b\u0004\u0000/,\u0001\u0000\u0000"+
		"\u0000/-\u0001\u0000\u0000\u0000/.\u0001\u0000\u0000\u00000\u0003\u0001"+
		"\u0000\u0000\u000012\u0005\u0001\u0000\u000023\u0005\u001b\u0000\u0000"+
		"34\u0005\u0002\u0000\u000049\u0003\n\u0005\u000056\u0005\u0003\u0000\u0000"+
		"68\u0003\n\u0005\u000075\u0001\u0000\u0000\u00008;\u0001\u0000\u0000\u0000"+
		"97\u0001\u0000\u0000\u00009:\u0001\u0000\u0000\u0000:<\u0001\u0000\u0000"+
		"\u0000;9\u0001\u0000\u0000\u0000<=\u0005\u0004\u0000\u0000=\u0005\u0001"+
		"\u0000\u0000\u0000>?\u0005\u0005\u0000\u0000?@\u0005\u001b\u0000\u0000"+
		"@A\u0005\u0002\u0000\u0000AF\u0003\n\u0005\u0000BC\u0005\u0003\u0000\u0000"+
		"CE\u0003\n\u0005\u0000DB\u0001\u0000\u0000\u0000EH\u0001\u0000\u0000\u0000"+
		"FD\u0001\u0000\u0000\u0000FG\u0001\u0000\u0000\u0000GI\u0001\u0000\u0000"+
		"\u0000HF\u0001\u0000\u0000\u0000IJ\u0005\u0004\u0000\u0000J\u0007\u0001"+
		"\u0000\u0000\u0000KL\u0005\u0006\u0000\u0000LM\u0005\u001b\u0000\u0000"+
		"MN\u0005\u0002\u0000\u0000NS\u0003\n\u0005\u0000OP\u0005\u0003\u0000\u0000"+
		"PR\u0003\n\u0005\u0000QO\u0001\u0000\u0000\u0000RU\u0001\u0000\u0000\u0000"+
		"SQ\u0001\u0000\u0000\u0000ST\u0001\u0000\u0000\u0000TV\u0001\u0000\u0000"+
		"\u0000US\u0001\u0000\u0000\u0000VW\u0005\u0004\u0000\u0000W\t\u0001\u0000"+
		"\u0000\u0000Xc\u0003\f\u0006\u0000Yc\u0003\u000e\u0007\u0000Zc\u0003\u0010"+
		"\b\u0000[c\u0003\u0012\t\u0000\\c\u0003\u0014\n\u0000]c\u0003\u001a\r"+
		"\u0000^c\u0003\u001c\u000e\u0000_c\u0003\u001e\u000f\u0000`c\u0003 \u0010"+
		"\u0000ac\u0003\"\u0011\u0000bX\u0001\u0000\u0000\u0000bY\u0001\u0000\u0000"+
		"\u0000bZ\u0001\u0000\u0000\u0000b[\u0001\u0000\u0000\u0000b\\\u0001\u0000"+
		"\u0000\u0000b]\u0001\u0000\u0000\u0000b^\u0001\u0000\u0000\u0000b_\u0001"+
		"\u0000\u0000\u0000b`\u0001\u0000\u0000\u0000ba\u0001\u0000\u0000\u0000"+
		"c\u000b\u0001\u0000\u0000\u0000de\u0005\u0007\u0000\u0000ef\u0005\b\u0000"+
		"\u0000fg\u0005!\u0000\u0000g\r\u0001\u0000\u0000\u0000hi\u0005\t\u0000"+
		"\u0000ij\u0005\b\u0000\u0000jk\u0005\n\u0000\u0000kp\u0005!\u0000\u0000"+
		"lm\u0005\u0003\u0000\u0000mo\u0005!\u0000\u0000nl\u0001\u0000\u0000\u0000"+
		"or\u0001\u0000\u0000\u0000pn\u0001\u0000\u0000\u0000pq\u0001\u0000\u0000"+
		"\u0000qs\u0001\u0000\u0000\u0000rp\u0001\u0000\u0000\u0000st\u0005\u000b"+
		"\u0000\u0000t\u000f\u0001\u0000\u0000\u0000uv\u0005\f\u0000\u0000vw\u0005"+
		"\b\u0000\u0000wx\u0005\u001c\u0000\u0000x\u0011\u0001\u0000\u0000\u0000"+
		"yz\u0005\r\u0000\u0000z{\u0005\b\u0000\u0000{|\u0005\n\u0000\u0000|\u0081"+
		"\u0005\u001c\u0000\u0000}~\u0005\u0003\u0000\u0000~\u0080\u0005\u001c"+
		"\u0000\u0000\u007f}\u0001\u0000\u0000\u0000\u0080\u0083\u0001\u0000\u0000"+
		"\u0000\u0081\u007f\u0001\u0000\u0000\u0000\u0081\u0082\u0001\u0000\u0000"+
		"\u0000\u0082\u0084\u0001\u0000\u0000\u0000\u0083\u0081\u0001\u0000\u0000"+
		"\u0000\u0084\u0085\u0005\u000b\u0000\u0000\u0085\u0013\u0001\u0000\u0000"+
		"\u0000\u0086\u0087\u0005\u000e\u0000\u0000\u0087\u0088\u0005\b\u0000\u0000"+
		"\u0088\u0089\u0005\u000f\u0000\u0000\u0089\u008e\u0003\u0016\u000b\u0000"+
		"\u008a\u008b\u0005\u0003\u0000\u0000\u008b\u008d\u0003\u0016\u000b\u0000"+
		"\u008c\u008a\u0001\u0000\u0000\u0000\u008d\u0090\u0001\u0000\u0000\u0000"+
		"\u008e\u008c\u0001\u0000\u0000\u0000\u008e\u008f\u0001\u0000\u0000\u0000"+
		"\u008f\u0091\u0001\u0000\u0000\u0000\u0090\u008e\u0001\u0000\u0000\u0000"+
		"\u0091\u0092\u0005\u0010\u0000\u0000\u0092\u0015\u0001\u0000\u0000\u0000"+
		"\u0093\u0094\u0005\n\u0000\u0000\u0094\u0095\u0003\u0018\f\u0000\u0095"+
		"\u0096\u0005\u0003\u0000\u0000\u0096\u0097\u0003\u0018\f\u0000\u0097\u0098"+
		"\u0005\u000b\u0000\u0000\u0098\u0017\u0001\u0000\u0000\u0000\u0099\u009a"+
		"\u0007\u0000\u0000\u0000\u009a\u0019\u0001\u0000\u0000\u0000\u009b\u009c"+
		"\u0005\u0012\u0000\u0000\u009c\u009d\u0005\b\u0000\u0000\u009d\u009e\u0005"+
		"\u001c\u0000\u0000\u009e\u001b\u0001\u0000\u0000\u0000\u009f\u00a0\u0005"+
		"\u0013\u0000\u0000\u00a0\u00a1\u0005\b\u0000\u0000\u00a1\u00a2\u0005\u001c"+
		"\u0000\u0000\u00a2\u001d\u0001\u0000\u0000\u0000\u00a3\u00a4\u0005\u0014"+
		"\u0000\u0000\u00a4\u00a5\u0005\b\u0000\u0000\u00a5\u00a6\u0005!\u0000"+
		"\u0000\u00a6\u001f\u0001\u0000\u0000\u0000\u00a7\u00a8\u0005\u0015\u0000"+
		"\u0000\u00a8\u00a9\u0005\b\u0000\u0000\u00a9\u00aa\u0005\u001b\u0000\u0000"+
		"\u00aa!\u0001\u0000\u0000\u0000\u00ab\u00ac\u0005\u0016\u0000\u0000\u00ac"+
		"\u00ad\u0005\b\u0000\u0000\u00ad\u00ae\u0005\u001b\u0000\u0000\u00ae#"+
		"\u0001\u0000\u0000\u0000\t\'/9FSbp\u0081\u008e";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}