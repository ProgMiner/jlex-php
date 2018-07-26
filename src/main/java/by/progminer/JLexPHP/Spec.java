package by.progminer.JLexPHP;

import java.util.Hashtable;
import java.util.Vector;

class Spec {
    
    /**
     * Lexical States
     */
    static final int NUM_PSEUDO = 2;
    
    // Regular Expression Macros
    
    /**
     * Constants
     */
    static final int NONE = 0;
    
    // NFA Machine
    static final int START = 1;
    static final int END   = 2;
    
    /**
     * Hashtable taking state indices (Integer)
     * to state name (String).
     */
    Hashtable <String, Integer> states;
    
    /**
     * Hashtable taking macro name (String)
     * to corresponding char buffer that
     * holds macro definition.
     */
    Hashtable <String, String> macros;
    
    // DFA Machine
    
    /**
     * Start state of NFA machine.
     */
    NFA nfaStart;
    
    /**
     * Vector of states, with index
     * corresponding to label.
     */
    Vector <NFA> nfaStates;
    
    // Accept States and Corresponding Anchors
    
    /**
     * An array of Vectors of Integers.
     * <p>
     * The ith Vector represents the lexical state with index i.
     * The contents of the ithVector are the indices of the NFA start
     * states that can be matched while in he ith lexical state.
     */
    Vector <NFA> stateRules[];
    
    int stateDTrans[];
    
    // Transition Table
    
    /**
     * Vector of states, with index
     * corresponding to label.
     */
    Vector <DFA> dfaStates;
    
    /**
     * Hashtable taking set of NFA states
     * to corresponding DFA state,
     * if the latter exists.
     */
    Hashtable <SparseBitSet, DFA> dfaSets;
    
    Vector<Accept> acceptVector;
    int anchorArray[];
    
    // Special pseudo-characters for beginning-of-line and end-of-file
    
    Vector<DTrans> dTransVector;
    int dTransNCols;
    int rowMap[];
    int colMap[];
    
    // Regular expression token variables
    
    int BOL; // beginning-of-line
    int EOF; // end-of-line
    
    /**
     * NFA character class minimization map.
     */
    int cClsMap[];
    
    int currentToken;
    char lexeme;
    
    // JLex directives flags
    
    boolean inQuote;
    boolean inCCl;
    
    /**
     * Verbose execution flag.
     */
    boolean verbose;
    
    boolean integerType;
    boolean intWrapType;
    boolean yyeof;
    boolean countChars;
    boolean countLines;
    boolean cupCompatible;
    boolean unix;
    boolean public_;
    boolean ignoreCase;
    
    char initCode[];
    int initLength;
    
    char initThrowCode[];
    int initThrowLength;
    
    char classCode[];
    int classLength;
    
    char eofCode[];
    int eofLength;
    
    char eofValueCode[];
    int eofValueLength;
    
    char eofThrowCode[];
    int eofThrowLength;
    
    char yylexThrowCode[];
    int yylexThrowLength;
    
    // Class, function, type names
    
    char className[]      = {'Y', 'y', 'l', 'e', 'x'};
    char implementsName[] = {};
    char functionName[]   = {'y', 'y', 'l', 'e', 'x'};
    char typeName[]       = {'Y', 'y', 't', 'o', 'k', 'e', 'n'};
    
    /**
     * Lexical Generator.
     */
    private LexGen lexGen;
    
    Spec(LexGen lexGen) {
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
        dTransNCols = CUtility.MAX_SEVEN_BIT + 1;
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