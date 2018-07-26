package by.progminer.JLexPHP;

import by.progminer.JLexPHP.Math.*;
import by.progminer.JLexPHP.Utility.Utility;

import java.util.Hashtable;
import java.util.Vector;

public class Spec {
    
    /**
     * Lexical States
     */
    public static final int NUM_PSEUDO = 2;
    
    // Regular Expression Macros
    
    /**
     * Constants
     */
    public static final int NONE = 0;
    
    // NFA Machine
    public static final int START = 1;
    public static final int END   = 2;
    
    /**
     * Hashtable taking state indices (Integer)
     * to state name (String).
     */
    public Hashtable <String, Integer> states;
    
    /**
     * Hashtable taking macro name (String)
     * to corresponding char buffer that
     * holds macro definition.
     */
    public Hashtable <String, String> macros;
    
    // DFA Machine
    
    /**
     * Start state of NFA machine.
     */
    public NFA nfaStart;
    
    /**
     * Vector of states, with index
     * corresponding to label.
     */
    public Vector <NFA> nfaStates;
    
    // Accept States and Corresponding Anchors
    
    /**
     * An array of Vectors of Integers.
     * <p>
     * The ith Vector represents the lexical state with index i.
     * The contents of the ithVector are the indices of the NFA start
     * states that can be matched while in he ith lexical state.
     */
    public Vector <NFA> stateRules[];
    
    public int stateDTrans[];
    
    // Transition Table
    
    /**
     * Vector of states, with index
     * corresponding to label.
     */
    public Vector <DFA> dfaStates;
    
    /**
     * Hashtable taking set of NFA states
     * to corresponding DFA state,
     * if the latter exists.
     */
    public Hashtable <SparseBitSet, DFA> dfaSets;
    
    public Vector<Accept> acceptVector;
    public int anchorArray[];
    
    // Special pseudo-characters for beginning-of-line and end-of-file
    
    public Vector<DTrans> dTransVector;
    public int dTransNCols;
    public int rowMap[];
    public int colMap[];
    
    // Regular expression token variables
    
    public int BOL; // beginning-of-line
    public int EOF; // end-of-line
    
    /**
     * NFA character class minimization map.
     */
    public int cClsMap[];
    
    public int currentToken;
    public char lexeme;
    
    // JLex directives flags
    
    public boolean inQuote;
    public boolean inCCl;
    
    /**
     * Verbose execution flag.
     */
    public boolean verbose;
    
    public boolean integerType;
    public boolean intWrapType;
    public boolean yyeof;
    public boolean countChars;
    public boolean countLines;
    public boolean cupCompatible;
    public boolean unix;
    public boolean public_;
    public boolean ignoreCase;
    
    public char initCode[];
    public int initLength;
    
    public char initThrowCode[];
    public int initThrowLength;
    
    public char classCode[];
    public int classLength;
    
    public char eofCode[];
    public int eofLength;
    
    public char eofValueCode[];
    public int eofValueLength;
    
    public char eofThrowCode[];
    public int eofThrowLength;
    
    public char yylexThrowCode[];
    public int yylexThrowLength;
    
    // Class, function, type names
    
    public char className[]      = {'Y', 'y', 'l', 'e', 'x'};
    public char implementsName[] = {};
    public char functionName[]   = {'y', 'y', 'l', 'e', 'x'};
    public char typeName[]       = {'Y', 'y', 't', 'o', 'k', 'e', 'n'};
    
    /**
     * Lexical Generator.
     */
    private LexGen lexGen;
    
    public Spec(LexGen lexGen) {
        this.lexGen = lexGen;
        
        // Initialize regular expression token variables
        currentToken = LexGen.EOS;
        lexeme = '\0';
        inQuote = false;
        inCCl = false;
        
        // Initialize hashtable for lexer states
        states = new Hashtable <String, Integer> ();
        states.put("YYINITIAL", states.size());
        
        // Initialize hashtable for lexical macros
        macros = new Hashtable <String, String> ();
        
        // Initialize variables for lexer options
        integerType = false;
        intWrapType = false;
        countLines = false;
        countChars = false;
        cupCompatible = false;
        unix = true;
        public_ = false;
        yyeof = false;
        ignoreCase = false;
        
        // Initialize variables for JLex runtime options
        verbose = true;
        
        nfaStart = null;
        nfaStates = new Vector<NFA>();
        
        dfaStates = new Vector <DFA> ();
        dfaSets = new Hashtable <SparseBitSet, DFA> ();
        
        dTransVector = new Vector<DTrans>();
        dTransNCols = Utility.MAX_SEVEN_BIT + 1;
        rowMap = null;
        colMap = null;
        
        acceptVector = null;
        anchorArray = null;
        
        initCode = null;
        initLength = 0;
        
        initThrowCode = null;
        initThrowLength = 0;
        
        yylexThrowCode = null;
        yylexThrowLength = 0;
        
        classCode = null;
        classLength = 0;
        
        eofCode = null;
        eofLength = 0;
        
        eofValueCode = null;
        eofValueLength = 0;
        
        eofThrowCode = null;
        eofThrowLength = 0;
        
        stateDTrans = null;
        
        stateRules = null;
    }
}