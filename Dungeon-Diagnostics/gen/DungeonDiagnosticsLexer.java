// Generated from C:/Users/bjarn/VS_Projects/Dungeon/Dungeon-Diagnostics/DungeonDiagnostics.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class DungeonDiagnosticsLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, DOUBLE_LINE=7, ARROW=8, 
		TRUE=9, FALSE=10, ID=11, NUM=12, NUM_DEC=13, WS=14, LINE_COMMENT=15, BLOCK_COMMENT=16, 
		STRING_LITERAL=17;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "DOUBLE_LINE", "ARROW", 
			"TRUE", "FALSE", "ID", "NUM", "NUM_DEC", "WS", "LINE_COMMENT", "BLOCK_COMMENT", 
			"STRING_LITERAL", "STRING_ESCAPE_SEQ"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'single_choice_task '", "'{'", "','", "'}'", "':'", "'\"'", "'--'", 
			"'->'", "'true'", "'false'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, "DOUBLE_LINE", "ARROW", "TRUE", 
			"FALSE", "ID", "NUM", "NUM_DEC", "WS", "LINE_COMMENT", "BLOCK_COMMENT", 
			"STRING_LITERAL"
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


	public DungeonDiagnosticsLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "DungeonDiagnostics.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\u0004\u0000\u0011\u00a7\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002"+
		"\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002"+
		"\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002"+
		"\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002"+
		"\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e"+
		"\u0002\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002"+
		"\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005"+
		"\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\n\u0001\n\u0005\nW\b\n\n\n\f\nZ\t\n\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0005\u000b_\b\u000b\n\u000b\f\u000bb\t\u000b"+
		"\u0003\u000bd\b\u000b\u0001\f\u0004\fg\b\f\u000b\f\f\fh\u0001\f\u0001"+
		"\f\u0004\fm\b\f\u000b\f\f\fn\u0001\r\u0004\rr\b\r\u000b\r\f\rs\u0001\r"+
		"\u0001\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0005\u000e|"+
		"\b\u000e\n\u000e\f\u000e\u007f\t\u000e\u0001\u000e\u0001\u000e\u0001\u000f"+
		"\u0001\u000f\u0001\u000f\u0001\u000f\u0005\u000f\u0087\b\u000f\n\u000f"+
		"\f\u000f\u008a\t\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f"+
		"\u0001\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0005\u0010\u0094\b\u0010"+
		"\n\u0010\f\u0010\u0097\t\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001"+
		"\u0010\u0005\u0010\u009d\b\u0010\n\u0010\f\u0010\u00a0\t\u0010\u0001\u0010"+
		"\u0003\u0010\u00a3\b\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0088"+
		"\u0000\u0012\u0001\u0001\u0003\u0002\u0005\u0003\u0007\u0004\t\u0005\u000b"+
		"\u0006\r\u0007\u000f\b\u0011\t\u0013\n\u0015\u000b\u0017\f\u0019\r\u001b"+
		"\u000e\u001d\u000f\u001f\u0010!\u0011#\u0000\u0001\u0000\b\u0003\u0000"+
		"AZ__az\u0004\u000009AZ__az\u0001\u000009\u0001\u000019\u0003\u0000\t\n"+
		"\r\r  \u0002\u0000\n\n\r\r\u0004\u0000\n\n\f\r\'\'\\\\\u0004\u0000\n\n"+
		"\f\r\"\"\\\\\u00b2\u0000\u0001\u0001\u0000\u0000\u0000\u0000\u0003\u0001"+
		"\u0000\u0000\u0000\u0000\u0005\u0001\u0000\u0000\u0000\u0000\u0007\u0001"+
		"\u0000\u0000\u0000\u0000\t\u0001\u0000\u0000\u0000\u0000\u000b\u0001\u0000"+
		"\u0000\u0000\u0000\r\u0001\u0000\u0000\u0000\u0000\u000f\u0001\u0000\u0000"+
		"\u0000\u0000\u0011\u0001\u0000\u0000\u0000\u0000\u0013\u0001\u0000\u0000"+
		"\u0000\u0000\u0015\u0001\u0000\u0000\u0000\u0000\u0017\u0001\u0000\u0000"+
		"\u0000\u0000\u0019\u0001\u0000\u0000\u0000\u0000\u001b\u0001\u0000\u0000"+
		"\u0000\u0000\u001d\u0001\u0000\u0000\u0000\u0000\u001f\u0001\u0000\u0000"+
		"\u0000\u0000!\u0001\u0000\u0000\u0000\u0001%\u0001\u0000\u0000\u0000\u0003"+
		"9\u0001\u0000\u0000\u0000\u0005;\u0001\u0000\u0000\u0000\u0007=\u0001"+
		"\u0000\u0000\u0000\t?\u0001\u0000\u0000\u0000\u000bA\u0001\u0000\u0000"+
		"\u0000\rC\u0001\u0000\u0000\u0000\u000fF\u0001\u0000\u0000\u0000\u0011"+
		"I\u0001\u0000\u0000\u0000\u0013N\u0001\u0000\u0000\u0000\u0015T\u0001"+
		"\u0000\u0000\u0000\u0017c\u0001\u0000\u0000\u0000\u0019f\u0001\u0000\u0000"+
		"\u0000\u001bq\u0001\u0000\u0000\u0000\u001dw\u0001\u0000\u0000\u0000\u001f"+
		"\u0082\u0001\u0000\u0000\u0000!\u00a2\u0001\u0000\u0000\u0000#\u00a4\u0001"+
		"\u0000\u0000\u0000%&\u0005s\u0000\u0000&\'\u0005i\u0000\u0000\'(\u0005"+
		"n\u0000\u0000()\u0005g\u0000\u0000)*\u0005l\u0000\u0000*+\u0005e\u0000"+
		"\u0000+,\u0005_\u0000\u0000,-\u0005c\u0000\u0000-.\u0005h\u0000\u0000"+
		"./\u0005o\u0000\u0000/0\u0005i\u0000\u000001\u0005c\u0000\u000012\u0005"+
		"e\u0000\u000023\u0005_\u0000\u000034\u0005t\u0000\u000045\u0005a\u0000"+
		"\u000056\u0005s\u0000\u000067\u0005k\u0000\u000078\u0005 \u0000\u0000"+
		"8\u0002\u0001\u0000\u0000\u00009:\u0005{\u0000\u0000:\u0004\u0001\u0000"+
		"\u0000\u0000;<\u0005,\u0000\u0000<\u0006\u0001\u0000\u0000\u0000=>\u0005"+
		"}\u0000\u0000>\b\u0001\u0000\u0000\u0000?@\u0005:\u0000\u0000@\n\u0001"+
		"\u0000\u0000\u0000AB\u0005\"\u0000\u0000B\f\u0001\u0000\u0000\u0000CD"+
		"\u0005-\u0000\u0000DE\u0005-\u0000\u0000E\u000e\u0001\u0000\u0000\u0000"+
		"FG\u0005-\u0000\u0000GH\u0005>\u0000\u0000H\u0010\u0001\u0000\u0000\u0000"+
		"IJ\u0005t\u0000\u0000JK\u0005r\u0000\u0000KL\u0005u\u0000\u0000LM\u0005"+
		"e\u0000\u0000M\u0012\u0001\u0000\u0000\u0000NO\u0005f\u0000\u0000OP\u0005"+
		"a\u0000\u0000PQ\u0005l\u0000\u0000QR\u0005s\u0000\u0000RS\u0005e\u0000"+
		"\u0000S\u0014\u0001\u0000\u0000\u0000TX\u0007\u0000\u0000\u0000UW\u0007"+
		"\u0001\u0000\u0000VU\u0001\u0000\u0000\u0000WZ\u0001\u0000\u0000\u0000"+
		"XV\u0001\u0000\u0000\u0000XY\u0001\u0000\u0000\u0000Y\u0016\u0001\u0000"+
		"\u0000\u0000ZX\u0001\u0000\u0000\u0000[d\u0007\u0002\u0000\u0000\\`\u0007"+
		"\u0003\u0000\u0000]_\u0007\u0002\u0000\u0000^]\u0001\u0000\u0000\u0000"+
		"_b\u0001\u0000\u0000\u0000`^\u0001\u0000\u0000\u0000`a\u0001\u0000\u0000"+
		"\u0000ad\u0001\u0000\u0000\u0000b`\u0001\u0000\u0000\u0000c[\u0001\u0000"+
		"\u0000\u0000c\\\u0001\u0000\u0000\u0000d\u0018\u0001\u0000\u0000\u0000"+
		"eg\u0007\u0002\u0000\u0000fe\u0001\u0000\u0000\u0000gh\u0001\u0000\u0000"+
		"\u0000hf\u0001\u0000\u0000\u0000hi\u0001\u0000\u0000\u0000ij\u0001\u0000"+
		"\u0000\u0000jl\u0005.\u0000\u0000km\u0007\u0002\u0000\u0000lk\u0001\u0000"+
		"\u0000\u0000mn\u0001\u0000\u0000\u0000nl\u0001\u0000\u0000\u0000no\u0001"+
		"\u0000\u0000\u0000o\u001a\u0001\u0000\u0000\u0000pr\u0007\u0004\u0000"+
		"\u0000qp\u0001\u0000\u0000\u0000rs\u0001\u0000\u0000\u0000sq\u0001\u0000"+
		"\u0000\u0000st\u0001\u0000\u0000\u0000tu\u0001\u0000\u0000\u0000uv\u0006"+
		"\r\u0000\u0000v\u001c\u0001\u0000\u0000\u0000wx\u0005/\u0000\u0000xy\u0005"+
		"/\u0000\u0000y}\u0001\u0000\u0000\u0000z|\b\u0005\u0000\u0000{z\u0001"+
		"\u0000\u0000\u0000|\u007f\u0001\u0000\u0000\u0000}{\u0001\u0000\u0000"+
		"\u0000}~\u0001\u0000\u0000\u0000~\u0080\u0001\u0000\u0000\u0000\u007f"+
		"}\u0001\u0000\u0000\u0000\u0080\u0081\u0006\u000e\u0001\u0000\u0081\u001e"+
		"\u0001\u0000\u0000\u0000\u0082\u0083\u0005/\u0000\u0000\u0083\u0084\u0005"+
		"*\u0000\u0000\u0084\u0088\u0001\u0000\u0000\u0000\u0085\u0087\t\u0000"+
		"\u0000\u0000\u0086\u0085\u0001\u0000\u0000\u0000\u0087\u008a\u0001\u0000"+
		"\u0000\u0000\u0088\u0089\u0001\u0000\u0000\u0000\u0088\u0086\u0001\u0000"+
		"\u0000\u0000\u0089\u008b\u0001\u0000\u0000\u0000\u008a\u0088\u0001\u0000"+
		"\u0000\u0000\u008b\u008c\u0005*\u0000\u0000\u008c\u008d\u0005/\u0000\u0000"+
		"\u008d\u008e\u0001\u0000\u0000\u0000\u008e\u008f\u0006\u000f\u0001\u0000"+
		"\u008f \u0001\u0000\u0000\u0000\u0090\u0095\u0005\'\u0000\u0000\u0091"+
		"\u0094\u0003#\u0011\u0000\u0092\u0094\b\u0006\u0000\u0000\u0093\u0091"+
		"\u0001\u0000\u0000\u0000\u0093\u0092\u0001\u0000\u0000\u0000\u0094\u0097"+
		"\u0001\u0000\u0000\u0000\u0095\u0093\u0001\u0000\u0000\u0000\u0095\u0096"+
		"\u0001\u0000\u0000\u0000\u0096\u0098\u0001\u0000\u0000\u0000\u0097\u0095"+
		"\u0001\u0000\u0000\u0000\u0098\u00a3\u0005\'\u0000\u0000\u0099\u009e\u0005"+
		"\"\u0000\u0000\u009a\u009d\u0003#\u0011\u0000\u009b\u009d\b\u0007\u0000"+
		"\u0000\u009c\u009a\u0001\u0000\u0000\u0000\u009c\u009b\u0001\u0000\u0000"+
		"\u0000\u009d\u00a0\u0001\u0000\u0000\u0000\u009e\u009c\u0001\u0000\u0000"+
		"\u0000\u009e\u009f\u0001\u0000\u0000\u0000\u009f\u00a1\u0001\u0000\u0000"+
		"\u0000\u00a0\u009e\u0001\u0000\u0000\u0000\u00a1\u00a3\u0005\"\u0000\u0000"+
		"\u00a2\u0090\u0001\u0000\u0000\u0000\u00a2\u0099\u0001\u0000\u0000\u0000"+
		"\u00a3\"\u0001\u0000\u0000\u0000\u00a4\u00a5\u0005\\\u0000\u0000\u00a5"+
		"\u00a6\t\u0000\u0000\u0000\u00a6$\u0001\u0000\u0000\u0000\u000e\u0000"+
		"X`chns}\u0088\u0093\u0095\u009c\u009e\u00a2\u0002\u0006\u0000\u0000\u0000"+
		"\u0001\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}