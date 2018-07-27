package by.progminer.JLexPHP.Utility;

public class Error extends java.lang.Error {
    
    /**
     * Error codes for parseError().
     */
    public static final int E_BADEXPR  = 0;
    public static final int E_PAREN    = 1;
    public static final int E_LENGTH   = 2;
    public static final int E_BRACKET  = 3;
    public static final int E_BOL      = 4;
    public static final int E_CLOSE    = 5;
    public static final int E_NEWLINE  = 6;
    public static final int E_BADMAC   = 7;
    public static final int E_NOMAC    = 8;
    public static final int E_MACDEPTH = 9;
    public static final int E_INIT     = 10;
    public static final int E_EOF      = 11;
    public static final int E_DIRECT   = 12;
    public static final int E_INTERNAL = 13;
    public static final int E_STATE    = 14;
    public static final int E_MACDEF   = 15;
    public static final int E_SYNTAX   = 16;
    public static final int E_BRACE    = 17;
    public static final int E_DASH     = 18;
    public static final int E_ZERO     = 19;
    public static final int E_BADCTRL  = 20;
    
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
    
    public Error() {
        super();
    }
    
    private Error(String msg) {
        super(msg);
    }
    
    public static void msg(String message) {
        System.err.println("JLex Error: " + message);
    }
    
    public static void parseError(int errorCode, int lineNumber) {
        System.err.println("Parse error at line " + lineNumber + ".");
        System.err.println("Description: " + ERRMSGS[errorCode]);
        
        throw new Error("Parse error.");
    }
}
