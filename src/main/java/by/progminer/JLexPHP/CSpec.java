package by.progminer.JLexPHP;

import java.util.Hashtable;
import java.util.Vector;

class CSpec {
    
    // Lexical States.
    
    static final int NUM_PSEUDO = 2;
    
    // Regular Expression Macros.
    
    /**
     * Constants
     */
    static final int NONE = 0;
    
    // NFA Machine.
    static final int START = 1;
    static final int END = 2;
    
    /**
     * Hashtable taking state indices (Integer)
     * to state name (String).
     */
    Hashtable <String, Integer> m_states;
    
    /**
     * Hashtable taking macro name (String)
     * to corresponding char buffer that
     * holds macro definition.
     */
    Hashtable <String, String> m_macros;
    
    // DFA Machine.
    
    /**
     * Start state of NFA machine.
     */
    NFA m_NFA_start;
    
    /**
     * Vector of states, with index
     * corresponding to label.
     */
    Vector <NFA> m_NFA_states;
    
    // Accept States and Corresponding Anchors.
    
    /**
     * An array of Vectors of Integers.
     * <p>
     * The ith Vector represents the lexical state with index i.
     * The contents of the ithVector are the indices of the NFA start
     * states that can be matched while in he ith lexical state.
     */
    Vector <NFA> m_state_rules[];
    
    int m_state_dtrans[];
    
    // Transition Table.
    
    /**
     * Vector of states, with index
     * corresponding to label.
     */
    Vector <DFA> m_DFA_states;
    
    /**
     * Hashtable taking set of NFA states
     * to corresponding DFA state,
     * if the latter exists.
     */
    Hashtable <SparseBitSet, DFA> m_dfa_sets;
    
    Vector<Accept> m_accept_vector;
    int m_anchor_array[];
    
    // Special pseudo-characters for beginning-of-line and end-of-file.
    
    Vector<DTrans> m_dtrans_vector;
    int m_dtrans_ncols;
    int m_row_map[];
    int m_col_map[];
    
    // Regular expression token variables.
    
    int BOL; // beginning-of-line
    int EOF; // end-of-line
    
    /**
     * NFA character class minimization map.
     */
    int m_ccls_map[];
    
    int m_current_token;
    char m_lexeme;
    
    // JLex directives flags.
    
    boolean m_in_quote;
    boolean m_in_ccl;
    
    /**
     * Verbose execution flag.
     */
    boolean m_verbose;
    
    boolean m_integer_type;
    boolean m_intwrap_type;
    boolean m_yyeof;
    boolean m_count_chars;
    boolean m_count_lines;
    boolean m_cup_compatible;
    boolean m_unix;
    boolean m_public;
    boolean m_ignorecase;
    char m_init_code[];
    int m_init_read;
    char m_init_throw_code[];
    int m_init_throw_read;
    char m_class_code[];
    int m_class_read;
    char m_eof_code[];
    int m_eof_read;
    char m_eof_value_code[];
    int m_eof_value_read;
    char m_eof_throw_code[];
    
    // Class, function, type names.
    
    int m_eof_throw_read;
    char m_yylex_throw_code[];
    int m_yylex_throw_read;
    char m_class_name[] = {'Y', 'y', 'l', 'e', 'x'};
    char m_implements_name[] = {};
    char m_function_name[] = {'y', 'y', 'l', 'e', 'x'};
    char m_type_name[] = {'Y', 'y', 't', 'o', 'k', 'e', 'n'};
    
    /**
     * Lexical Generator.
     */
    private LexGen m_lexGen;
    
    CSpec(LexGen lexGen) {
        m_lexGen = lexGen;
        
        // Initialize regular expression token variables.
        m_current_token = LexGen.EOS;
        m_lexeme = '\0';
        m_in_quote = false;
        m_in_ccl = false;
        
        // Initialize hashtable for lexer states.
        m_states = new Hashtable <String, Integer> ();
        m_states.put("YYINITIAL", m_states.size());
        
        // Initialize hashtable for lexical macros.
        m_macros = new Hashtable <String, String> ();
        
        // Initialize variables for lexer options.
        m_integer_type = false;
        m_intwrap_type = false;
        m_count_lines = false;
        m_count_chars = false;
        m_cup_compatible = false;
        m_unix = true;
        m_public = false;
        m_yyeof = false;
        m_ignorecase = false;
        
        // Initialize variables for JLex runtime options.
        m_verbose = true;
        
        m_NFA_start = null;
        m_NFA_states = new Vector<NFA>();
        
        m_DFA_states = new Vector <DFA> ();
        m_dfa_sets = new Hashtable <SparseBitSet, DFA> ();
        
        m_dtrans_vector = new Vector<DTrans>();
        m_dtrans_ncols = CUtility.MAX_SEVEN_BIT + 1;
        m_row_map = null;
        m_col_map = null;
        
        m_accept_vector = null;
        m_anchor_array = null;
        
        m_init_code = null;
        m_init_read = 0;
        
        m_init_throw_code = null;
        m_init_throw_read = 0;
        
        m_yylex_throw_code = null;
        m_yylex_throw_read = 0;
        
        m_class_code = null;
        m_class_read = 0;
        
        m_eof_code = null;
        m_eof_read = 0;
        
        m_eof_value_code = null;
        m_eof_value_read = 0;
        
        m_eof_throw_code = null;
        m_eof_throw_read = 0;
        
        m_state_dtrans = null;
        
        m_state_rules = null;
    }
}