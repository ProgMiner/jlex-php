package by.progminer.JLexPHP;

class Error extends java.lang.Error {
    
    /**
     * Error codes for parseError().
     */
    static final int E_BADEXPR  = 0;
    static final int E_PAREN    = 1;
    static final int E_LENGTH   = 2;
    static final int E_BRACKET  = 3;
    static final int E_BOL      = 4;
    static final int E_CLOSE    = 5;
    static final int E_NEWLINE  = 6;
    static final int E_BADMAC   = 7;
    static final int E_NOMAC    = 8;
    static final int E_MACDEPTH = 9;
    static final int E_INIT     = 10;
    static final int E_EOF      = 11;
    static final int E_DIRECT   = 12;
    static final int E_INTERNAL = 13;
    static final int E_STATE    = 14;
    static final int E_MACDEF   = 15;
    static final int E_SYNTAX   = 16;
    static final int E_BRACE    = 17;
    static final int E_DASH     = 18;
    static final int E_ZERO     = 19;
    static final int E_BADCTRL  = 20;
    
    /**
     * String messages for parseError();
     */
    private static final String ERRMSGS[] = {
        /* E_BADEXPR  */ "Malformed regular expression.",
        /* E_PAREN    */ "Missing close parenthesis.",
        /* E_LENGTH   */ "Too many regular expressions or expression too long.",
        /* E_BRACKET  */ "Missing [ in character class.",
        /* E_BOL      */ "^ must be at start of expression or after [.",
        /* E_CLOSE    */ "+ ? or * must follow an expression or subexpression.",
        /* E_NEWLINE  */ "Newline in quoted string.",
        /* E_BADMAC   */ "Missing } in macro expansion.",
        /* E_NOMAC    */ "Macro does not exist.",
        /* E_MACDEPTH */ "Macro expansions nested too deeply.",
        /* E_INIT     */ "JLex has not been successfully initialized.",
        /* E_EOF      */ "Unexpected end-of-file found.",
        /* E_DIRECT   */ "Undefined or badly-formed JLex directive.",
        /* E_INTERNAL */ "Internal JLex error.",
        /* E_STATE    */ "Undefined state name.",
        /* E_MACDEF   */ "Badly formed macro definition.",
        /* E_SYNTAX   */ "Syntax error.",
        /* E_BRACE    */ "Missing brace at start of lexical action.",
        /* E_DASH     */ "Special character dash - in character class [...] must be preceded by start-of-range character.",
        /* E_ZERO     */ "Zero-length regular expression.",
        /* E_BADCTRL  */ "Illegal \\^C-style escape sequence (character following caret must be alphabetic)."
    };
    
    Error() {
        super();
    }
    
    private Error(String msg) {
        super(msg);
    }
    
    static void msg(String message) {
        System.out.println("JLex Error: " + message);
    }
    
    static void parseError(int errorCode, int lineNumber) {
        System.out.println("Parse error at line " + lineNumber + ".");
        System.out.println("Description: " + ERRMSGS[errorCode]);
        
        throw new Error("Parse error.");
    }
}
