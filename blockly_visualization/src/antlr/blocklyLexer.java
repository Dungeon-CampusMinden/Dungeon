// Generated from D:/Documents/Forschungsprojekt/Dungeon/blockly_visualization/src/blockly.g4 by ANTLR 4.13.1
package antlr;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class blocklyLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, WS=4, IF=5, BOOLEAN=6, COMPARE_OPERATOR=7, AND=8, 
		OR=9, MULTI=10, DIV=11, PLUS=12, MINUS=13, NOT=14, ID=15, INT=16, STRING=17;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "WS", "IF", "BOOLEAN", "COMPARE_OPERATOR", "AND", 
			"OR", "MULTI", "DIV", "PLUS", "MINUS", "NOT", "ID", "INT", "STRING", 
			"DIGIT", "CHAR", "ASCII"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'('", "')'", "','", null, "'falls'", null, null, "'&&'", "'||'", 
			"'*'", "'/'", "'+'", "'-'", "'not'"
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


	public blocklyLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "blockly.g4"; }

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
		"\u0004\u0000\u0011\u008f\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002"+
		"\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002"+
		"\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002"+
		"\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002"+
		"\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e"+
		"\u0002\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011"+
		"\u0002\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0001\u0000\u0001\u0000"+
		"\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0003\u0004\u0003"+
		"1\b\u0003\u000b\u0003\f\u00032\u0001\u0003\u0001\u0003\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0003\u0005G\b\u0005\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0003\u0006S\b\u0006\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001\n\u0001"+
		"\n\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\r\u0001\r\u0001\r\u0001"+
		"\r\u0001\u000e\u0001\u000e\u0003\u000ei\b\u000e\u0001\u000e\u0001\u000e"+
		"\u0001\u000e\u0005\u000en\b\u000e\n\u000e\f\u000eq\t\u000e\u0001\u000f"+
		"\u0004\u000ft\b\u000f\u000b\u000f\f\u000fu\u0001\u0010\u0001\u0010\u0005"+
		"\u0010z\b\u0010\n\u0010\f\u0010}\t\u0010\u0001\u0010\u0001\u0010\u0001"+
		"\u0010\u0005\u0010\u0082\b\u0010\n\u0010\f\u0010\u0085\t\u0010\u0001\u0010"+
		"\u0003\u0010\u0088\b\u0010\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012"+
		"\u0001\u0013\u0001\u0013\u0002{\u0083\u0000\u0014\u0001\u0001\u0003\u0002"+
		"\u0005\u0003\u0007\u0004\t\u0005\u000b\u0006\r\u0007\u000f\b\u0011\t\u0013"+
		"\n\u0015\u000b\u0017\f\u0019\r\u001b\u000e\u001d\u000f\u001f\u0010!\u0011"+
		"#\u0000%\u0000\'\u0000\u0001\u0000\u0003\u0003\u0000\t\n\r\r  \u0001\u0000"+
		"09\u0002\u0000AZaz\u009a\u0000\u0001\u0001\u0000\u0000\u0000\u0000\u0003"+
		"\u0001\u0000\u0000\u0000\u0000\u0005\u0001\u0000\u0000\u0000\u0000\u0007"+
		"\u0001\u0000\u0000\u0000\u0000\t\u0001\u0000\u0000\u0000\u0000\u000b\u0001"+
		"\u0000\u0000\u0000\u0000\r\u0001\u0000\u0000\u0000\u0000\u000f\u0001\u0000"+
		"\u0000\u0000\u0000\u0011\u0001\u0000\u0000\u0000\u0000\u0013\u0001\u0000"+
		"\u0000\u0000\u0000\u0015\u0001\u0000\u0000\u0000\u0000\u0017\u0001\u0000"+
		"\u0000\u0000\u0000\u0019\u0001\u0000\u0000\u0000\u0000\u001b\u0001\u0000"+
		"\u0000\u0000\u0000\u001d\u0001\u0000\u0000\u0000\u0000\u001f\u0001\u0000"+
		"\u0000\u0000\u0000!\u0001\u0000\u0000\u0000\u0001)\u0001\u0000\u0000\u0000"+
		"\u0003+\u0001\u0000\u0000\u0000\u0005-\u0001\u0000\u0000\u0000\u00070"+
		"\u0001\u0000\u0000\u0000\t6\u0001\u0000\u0000\u0000\u000bF\u0001\u0000"+
		"\u0000\u0000\rR\u0001\u0000\u0000\u0000\u000fT\u0001\u0000\u0000\u0000"+
		"\u0011W\u0001\u0000\u0000\u0000\u0013Z\u0001\u0000\u0000\u0000\u0015\\"+
		"\u0001\u0000\u0000\u0000\u0017^\u0001\u0000\u0000\u0000\u0019`\u0001\u0000"+
		"\u0000\u0000\u001bb\u0001\u0000\u0000\u0000\u001dh\u0001\u0000\u0000\u0000"+
		"\u001fs\u0001\u0000\u0000\u0000!\u0087\u0001\u0000\u0000\u0000#\u0089"+
		"\u0001\u0000\u0000\u0000%\u008b\u0001\u0000\u0000\u0000\'\u008d\u0001"+
		"\u0000\u0000\u0000)*\u0005(\u0000\u0000*\u0002\u0001\u0000\u0000\u0000"+
		"+,\u0005)\u0000\u0000,\u0004\u0001\u0000\u0000\u0000-.\u0005,\u0000\u0000"+
		".\u0006\u0001\u0000\u0000\u0000/1\u0007\u0000\u0000\u00000/\u0001\u0000"+
		"\u0000\u000012\u0001\u0000\u0000\u000020\u0001\u0000\u0000\u000023\u0001"+
		"\u0000\u0000\u000034\u0001\u0000\u0000\u000045\u0006\u0003\u0000\u0000"+
		"5\b\u0001\u0000\u0000\u000067\u0005f\u0000\u000078\u0005a\u0000\u0000"+
		"89\u0005l\u0000\u00009:\u0005l\u0000\u0000:;\u0005s\u0000\u0000;\n\u0001"+
		"\u0000\u0000\u0000<=\u0005w\u0000\u0000=>\u0005a\u0000\u0000>?\u0005h"+
		"\u0000\u0000?G\u0005r\u0000\u0000@A\u0005f\u0000\u0000AB\u0005a\u0000"+
		"\u0000BC\u0005l\u0000\u0000CD\u0005s\u0000\u0000DE\u0005c\u0000\u0000"+
		"EG\u0005h\u0000\u0000F<\u0001\u0000\u0000\u0000F@\u0001\u0000\u0000\u0000"+
		"G\f\u0001\u0000\u0000\u0000HS\u0005<\u0000\u0000IJ\u0005<\u0000\u0000"+
		"JS\u0005=\u0000\u0000KS\u0005>\u0000\u0000LM\u0005>\u0000\u0000MS\u0005"+
		"=\u0000\u0000NO\u0005!\u0000\u0000OS\u0005=\u0000\u0000PQ\u0005=\u0000"+
		"\u0000QS\u0005=\u0000\u0000RH\u0001\u0000\u0000\u0000RI\u0001\u0000\u0000"+
		"\u0000RK\u0001\u0000\u0000\u0000RL\u0001\u0000\u0000\u0000RN\u0001\u0000"+
		"\u0000\u0000RP\u0001\u0000\u0000\u0000S\u000e\u0001\u0000\u0000\u0000"+
		"TU\u0005&\u0000\u0000UV\u0005&\u0000\u0000V\u0010\u0001\u0000\u0000\u0000"+
		"WX\u0005|\u0000\u0000XY\u0005|\u0000\u0000Y\u0012\u0001\u0000\u0000\u0000"+
		"Z[\u0005*\u0000\u0000[\u0014\u0001\u0000\u0000\u0000\\]\u0005/\u0000\u0000"+
		"]\u0016\u0001\u0000\u0000\u0000^_\u0005+\u0000\u0000_\u0018\u0001\u0000"+
		"\u0000\u0000`a\u0005-\u0000\u0000a\u001a\u0001\u0000\u0000\u0000bc\u0005"+
		"n\u0000\u0000cd\u0005o\u0000\u0000de\u0005t\u0000\u0000e\u001c\u0001\u0000"+
		"\u0000\u0000fi\u0003%\u0012\u0000gi\u0005_\u0000\u0000hf\u0001\u0000\u0000"+
		"\u0000hg\u0001\u0000\u0000\u0000io\u0001\u0000\u0000\u0000jn\u0003%\u0012"+
		"\u0000kn\u0003#\u0011\u0000ln\u0005_\u0000\u0000mj\u0001\u0000\u0000\u0000"+
		"mk\u0001\u0000\u0000\u0000ml\u0001\u0000\u0000\u0000nq\u0001\u0000\u0000"+
		"\u0000om\u0001\u0000\u0000\u0000op\u0001\u0000\u0000\u0000p\u001e\u0001"+
		"\u0000\u0000\u0000qo\u0001\u0000\u0000\u0000rt\u0003#\u0011\u0000sr\u0001"+
		"\u0000\u0000\u0000tu\u0001\u0000\u0000\u0000us\u0001\u0000\u0000\u0000"+
		"uv\u0001\u0000\u0000\u0000v \u0001\u0000\u0000\u0000w{\u0005\'\u0000\u0000"+
		"xz\u0003\'\u0013\u0000yx\u0001\u0000\u0000\u0000z}\u0001\u0000\u0000\u0000"+
		"{|\u0001\u0000\u0000\u0000{y\u0001\u0000\u0000\u0000|~\u0001\u0000\u0000"+
		"\u0000}{\u0001\u0000\u0000\u0000~\u0088\u0005\'\u0000\u0000\u007f\u0083"+
		"\u0005\"\u0000\u0000\u0080\u0082\u0003\'\u0013\u0000\u0081\u0080\u0001"+
		"\u0000\u0000\u0000\u0082\u0085\u0001\u0000\u0000\u0000\u0083\u0084\u0001"+
		"\u0000\u0000\u0000\u0083\u0081\u0001\u0000\u0000\u0000\u0084\u0086\u0001"+
		"\u0000\u0000\u0000\u0085\u0083\u0001\u0000\u0000\u0000\u0086\u0088\u0005"+
		"\"\u0000\u0000\u0087w\u0001\u0000\u0000\u0000\u0087\u007f\u0001\u0000"+
		"\u0000\u0000\u0088\"\u0001\u0000\u0000\u0000\u0089\u008a\u0007\u0001\u0000"+
		"\u0000\u008a$\u0001\u0000\u0000\u0000\u008b\u008c\u0007\u0002\u0000\u0000"+
		"\u008c&\u0001\u0000\u0000\u0000\u008d\u008e\u0002\u0000\u00ff\u0000\u008e"+
		"(\u0001\u0000\u0000\u0000\u000b\u00002FRhmou{\u0083\u0087\u0001\u0006"+
		"\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}