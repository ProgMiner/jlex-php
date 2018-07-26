package by.progminer.JLexPHP;


import java.io.*;
import java.util.*;

class LexGen {
    
    static final int EOS          = 1;
    static final int ANY          = 2;
    static final int AT_BOL       = 3;
    static final int AT_EOL       = 4;
    static final int CCL_END      = 5;
    static final int CCL_START    = 6;
    static final int CLOSE_CURLY  = 7;
    static final int CLOSE_PAREN  = 8;
    static final int CLOSURE      = 9;
    static final int DASH         = 10;
    static final int END_OF_INPUT = 11;
    static final int L            = 12;
    static final int OPEN_CURLY   = 13;
    static final int OPEN_PAREN   = 14;
    static final int OPTIONAL     = 15;
    static final int OR           = 16;
    static final int PLUS_CLOSE   = 17;
    
    /**
     * Return values for expandMacro().
     */
    private static final boolean ERROR     = false;
    private static final boolean NOT_ERROR = true;
    
    private static final int BUFFER_SIZE = 1024;
    
    /**
     * Specified codes for packCode().
     */
    private final int CLASS_CODE = 0;
    private final int INIT_CODE = 1;
    private final int EOF_CODE = 2;
    private final int INIT_THROW_CODE = 3;
    private final int YYLEX_THROW_CODE = 4;
    private final int EOF_THROW_CODE = 5;
    private final int EOF_VALUE_CODE = 6;
    
    /**
     * JLex specification file.
     */
    private Reader in;
    
    /**
     * Lexical analyzer source file.
     */
    private PrintWriter out;
    
    /**
     * Input buffer class.
     */
    private Input input;
    
    /**
     * Hashtable that maps characters to their
     * corresponding lexical code for
     * the msg lexical analyzer.
     */
    private Hashtable <Character, Integer> tokens;
    
    /**
     * Spec class holds information
     * about the generated lexer.
     */
    private CSpec spec;
    
    /**
     * Flag set to true only upon
     * successful initialization.
     */
    private boolean initFlag;
    
    /**
     * NFA machine generator module.
     */
    private MakeNFA makeNFA;
    
    /**
     * NFA to DFA machine (transition table)
     * conversion module.
     */
    private Nfa2DFA nfa2DFA;
    
    /**
     * Transition table compressor.
     */
    private Minimize minimize;
    
    /**
     * NFA simplifier using char classes.
     */
    private CSimplifyNfa simplifyNFA;
    
    /**
     * Cache for getStates().
     */
    private SparseBitSet all_states = null;
    
    /**
     * Output module that emits source code
     * into the generated lexer file.
     */
    private Emit emit;
    
    /**
     * JLex directives.
     */
    private char stateDir[]             = {'%', 's', 't', 'a', 't', 'e', '\0'};
    private char charDir[]              = {'%', 'c', 'h', 'a', 'r', '\0'};
    private char lineDir[]              = {'%', 'l', 'i', 'n', 'e', '\0'};
    private char cupDir[]               = {'%', 'c', 'u', 'p', '\0'};
    private char classDir[]             = {'%', 'c', 'l', 'a', 's', 's', '\0'};
    private char implementsDir[]        = {'%', 'i', 'm', 'p', 'l', 'e', 'm', 'e', 'n', 't', 's', '\0'};
    private char functionDir[]          = {'%', 'f', 'u', 'n', 'c', 't', 'i', 'o', 'n', '\0'};
    private char typeDir[]              = {'%', 't', 'y', 'p', 'e', '\0'};
    private char integerDir[]           = {'%', 'i', 'n', 't', 'e', 'g', 'e', 'r', '\0'};
    private char intwrapDir[]           = {'%', 'i', 'n', 't', 'w', 'r', 'a', 'p', '\0'};
    private char fullDir[]              = {'%', 'f', 'u', 'l', 'l', '\0'};
    private char unicodeDir[]           = {'%', 'u', 'n', 'i', 'c', 'o', 'd', 'e', '\0'};
    private char ignorecaseDir[]        = {'%', 'i', 'g', 'n', 'o', 'r', 'e', 'c', 'a', 's', 'e', '\0'};
    private char notunixDir[]           = {'%', 'n', 'o', 't', 'u', 'n', 'i', 'x', '\0'};
    private char initCodeDir[]          = {'%', 'i', 'n', 'i', 't', '{', '\0'};
    private char initCodeEndDir[]       = {'%', 'i', 'n', 'i', 't', '}', '\0'};
    private char initThrowCodeDir[]     = {'%', 'i', 'n', 'i', 't', 't', 'h', 'r', 'o', 'w', '{', '\0'};
    private char initThrowCodeEndDir[]  = {'%', 'i', 'n', 'i', 't', 't', 'h', 'r', 'o', 'w', '}', '\0'};
    private char yylexThrowCodeDir[]    = {'%', 'y', 'y', 'l', 'e', 'x', 't', 'h', 'r', 'o', 'w', '{', '\0'};
    private char yylexThrowCodeEndDir[] = {'%', 'y', 'y', 'l', 'e', 'x', 't', 'h', 'r', 'o', 'w', '}', '\0'};
    private char eofCodeDir[]           = {'%', 'e', 'o', 'f', '{', '\0'};
    private char eofCodeEndDir[]        = {'%', 'e', 'o', 'f', '}', '\0'};
    private char eofValueCodeDir[]      = {'%', 'e', 'o', 'f', 'v', 'a', 'l', '{', '\0'};
    private char eofValueCodeEndDir[]   = {'%', 'e', 'o', 'f', 'v', 'a', 'l', '}', '\0'};
    private char eofThrowCodeDir[]      = {'%', 'e', 'o', 'f', 't', 'h', 'r', 'o', 'w', '{', '\0'};
    private char eofThrowCodeEndDir[]   = {'%', 'e', 'o', 'f', 't', 'h', 'r', 'o', 'w', '}', '\0'};
    private char classCodeDir[]         = {'%', '{', '\0'};
    private char classCodeEndDir[]      = {'%', '}', '\0'};
    private char yyeofDir[]             = {'%', 'y', 'y', 'e', 'o', 'f', '\0'};
    private char publicDir[]            = {'%', 'p', 'u', 'b', 'l', 'i', 'c', '\0'};
    
    /**
     * Flag for advance().
     */
    private boolean advanceStop = false;
    
    // TODO IOStreams receiving
    LexGen(String filename) throws IOException {
        // Successful initialization flag
        initFlag = false;
        
        try {
            // Open input stream
            in = new FileReader(filename);
        } catch (IOException ex) {
            System.out.println("Error: Unable to open input file " + filename + ".");
            
            throw ex;
        }
        
        try {
            // Open output stream
            out = new PrintWriter(new BufferedWriter(new FileWriter(filename + ".php")));
        } catch (IOException ex) {
            System.out.println("Error: Unable to open output file " + filename + ".php.");
    
            throw ex;
        }
        
        // Create input buffer class
        input = new Input(in);
        
        // Initialize character hash table
        tokens = new Hashtable <Character, Integer> ();
        tokens.put('$', AT_EOL);
        tokens.put('(', OPEN_PAREN);
        tokens.put(')', CLOSE_PAREN);
        tokens.put('*', CLOSURE);
        tokens.put('+', PLUS_CLOSE);
        tokens.put('-', DASH);
        tokens.put('.', ANY);
        tokens.put('?', OPTIONAL);
        tokens.put('[', CCL_START);
        tokens.put(']', CCL_END);
        tokens.put('^', AT_BOL);
        tokens.put('{', OPEN_CURLY);
        tokens.put('|', OR);
        tokens.put('}', CLOSE_CURLY);
        
        // Initialize spec structure
        spec = new CSpec(this);
        
        // NFA to dfa converter
        nfa2DFA = new Nfa2DFA();
        minimize = new Minimize();
        makeNFA = new MakeNFA();
        simplifyNFA = new CSimplifyNfa();
        
        emit = new Emit();
        
        // Successful initialization flag
        initFlag = true;
    }
    
    void generate() throws IOException {
        if (!initFlag) {
            Error.parseError(Error.E_INIT, 0);
        }
        
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != out);
            CUtility.ASSERT(null != input);
            CUtility.ASSERT(null != tokens);
            CUtility.ASSERT(null != spec);
            CUtility.ASSERT(initFlag);
        }
        
        if (spec.m_verbose) {
            System.out.println("Processing first section -- user code.");
        }
        
        userCode();
        
        if (input.isEOFReached) {
            Error.parseError(Error.E_EOF, input.lineNumber);
        }
        
        if (spec.m_verbose) {
            System.out.println("Processing second section -- JLex declarations.");
        }
        
        userDeclare();
        
        if (input.isEOFReached) {
            Error.parseError(Error.E_EOF, input.lineNumber);
        }
        
        if (spec.m_verbose) {
            System.out.println("Processing third section -- lexical rules.");
        }
        
        userRules();
        
        if (CUtility.DO_DEBUG) {
            printHeader();
        }
        
        if (spec.m_verbose) {
            System.out.println("Outputting lexical analyzer code.");
        }
        
        emit.all(spec, out);
        
        if (spec.m_verbose && CUtility.OLD_DUMP_DEBUG) {
            details();
        }
        
        out.close();
    }
    
    /**
     * Process first section of specification,
     * echoing it into output file.
     */
    private void userCode() throws IOException {
        if (!initFlag) {
            Error.parseError(Error.E_INIT, 0);
        }
        
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != out);
            CUtility.ASSERT(null != input);
            CUtility.ASSERT(null != tokens);
            CUtility.ASSERT(null != spec);
        }
        
        if (input.isEOFReached) {
            Error.parseError(Error.E_EOF, 0);
        }
        
        while (true) {
            if (input.getLine()) {
                // Eof reached
                Error.parseError(Error.E_EOF, 0);
            }
            
            if (
                2 <= input.lineLength &&
                '%' == input.line[0] &&
                '%' == input.line[1]
            ) {
                // Discard remainder of line
                input.lineIndex = input.lineLength;
                
                return;
            }
            
            out.print(new String(input.line, 0, input.lineLength));
        }
    }
    
    private char[] getName() {
        // Skip white space
        while (
            input.lineIndex < input.lineLength &&
            CUtility.isspace(input.line[input.lineIndex])
        ) {
            ++input.lineIndex;
        }
        
        // No name?
        if (input.lineIndex >= input.lineLength) {
            Error.parseError(Error.E_DIRECT, 0);
        }
        
        // Determine length
        int elem = input.lineIndex;
        while (
            elem < input.lineLength &&
            !CUtility.isnewline(input.line[elem])
        ) {
            ++elem;
        }
        
        // Allocate non-terminated buffer of exact length
        char buffer[] = new char[elem - input.lineIndex];
        
        // Copy
        
        elem = 0;
        while (
            input.lineIndex < input.lineLength &&
            !CUtility.isnewline(input.line[input.lineIndex])
        ) {
            buffer[elem] = input.line[input.lineIndex];
            
            ++elem;
            ++input.lineIndex;
        }
        
        return buffer;
    }
    
    private char[] packCode(
        char startDir[],
        char endDir[],
        char prevCode[],
        int prevCodeLength,
        int specified
    ) throws java.io.IOException {
        if (CUtility.DEBUG) {
            CUtility.ASSERT(
                INIT_CODE == specified ||
                CLASS_CODE == specified ||
                EOF_CODE == specified ||
                EOF_VALUE_CODE == specified ||
                INIT_THROW_CODE == specified ||
                YYLEX_THROW_CODE == specified ||
                EOF_THROW_CODE == specified
            );
        }
        
        if (0 != CUtility.charncmp(input.line, 0, startDir,0,startDir.length - 1)) {
            Error.parseError(Error.E_INTERNAL, 0);
        }
        
        if (null == prevCode) {
            prevCode = new char[BUFFER_SIZE];
            prevCodeLength = 0;
        }
        
        if (prevCodeLength >= prevCode.length) {
            prevCode = CUtility.doubleSize(prevCode);
        }
        
        input.lineIndex = startDir.length - 1;
        while (true) {
            while (input.lineIndex >= input.lineLength) {
                if (input.getLine()) {
                    Error.parseError(Error.E_EOF, input.lineNumber);
                }
                
                if (0 == CUtility.charncmp(input.line, 0, endDir, 0, endDir.length - 1)) {
                    input.lineIndex = endDir.length - 1;
                    
                    switch (specified) {
                    case CLASS_CODE:
                        spec.m_class_read = prevCodeLength;
                        break;
                    
                    case INIT_CODE:
                        spec.m_init_read = prevCodeLength;
                        break;
                    
                    case EOF_CODE:
                        spec.m_eof_read = prevCodeLength;
                        break;
                    
                    case EOF_VALUE_CODE:
                        spec.m_eof_value_read = prevCodeLength;
                        break;
                    
                    case INIT_THROW_CODE:
                        spec.m_init_throw_read = prevCodeLength;
                        break;
                    
                    case YYLEX_THROW_CODE:
                        spec.m_yylex_throw_read = prevCodeLength;
                        break;
                    
                    case EOF_THROW_CODE:
                        spec.m_eof_throw_read = prevCodeLength;
                        break;
                    
                    default:
                        Error.parseError(Error.E_INTERNAL, input.lineNumber);
                        break;
                    }
                    
                    return prevCode;
                }
            }
            
            while (input.lineIndex < input.lineLength) {
                prevCode[prevCodeLength] = input.line[input.lineIndex];
                ++prevCodeLength;
                ++input.lineIndex;
                
                if (prevCodeLength >= prevCode.length) {
                    prevCode = CUtility.doubleSize(prevCode);
                }
            }
        }
    }
    
    private void userDeclare() throws IOException {
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != out);
            CUtility.ASSERT(null != input);
            CUtility.ASSERT(null != tokens);
            CUtility.ASSERT(null != spec);
        }
        
        if (input.isEOFReached) {
            // End-of-file
            Error.parseError(Error.E_EOF, input.lineNumber);
        }
        
        while (!input.getLine()) {
            // Look for double percent
            
            if (
                input.lineLength >= 2 &&
                '%' == input.line[0] &&
                '%' == input.line[1]
            ) {
                // Mess around with line
                
                input.lineLength -= 2;
                System.arraycopy(input.line, 2, input.line, 0, input.lineLength);
                
                input.pushbackLine = true;
                return;
            }
            
            if (0 == input.lineLength) {
                continue;
            }
            
            if ('%' == input.line[0]) {
                // Special lex declarations
                
                if (1 >= input.lineLength) {
                    Error.parseError(Error.E_DIRECT, input.lineNumber);
                    continue;
                }
                
                switch (input.line[1]) {
                case '{':
                    if (0 == CUtility.charncmp(input.line, 0, classCodeDir, 0, classCodeDir.length - 1)) {
                        spec.m_class_code = packCode(
                            classCodeDir,
                            classCodeEndDir,
                            spec.m_class_code,
                            spec.m_class_read,
                            CLASS_CODE
                        );
                        
                        break;
                    }
    
                    // Bad directive
                    Error.parseError(Error.E_DIRECT, input.lineNumber);
                    break;
                
                case 'c':
                    if (0 == CUtility.charncmp(input.line, 0, charDir, 0, charDir.length - 1)) {
                        // Set line counting to ON
                        
                        input.lineIndex = charDir.length;
                        spec.m_count_chars = true;
                        
                        break;
                    } else if (0 == CUtility.charncmp(input.line, 0, classDir, 0, classDir.length - 1)) {
                        input.lineIndex = classDir.length;
                        spec.m_class_name = getName();
                        
                        break;
                    } else if (0 == CUtility.charncmp(input.line, 0, cupDir, 0, cupDir.length - 1)) {
                        // Set Java CUP compatibility to ON
                        
                        input.lineIndex = cupDir.length;
                        spec.m_cup_compatible = true;
                        
                        // this is what %cup does: [CSA, 27-Jul-1999]
                        spec.m_implements_name = "java_cup.runtime.Scanner".toCharArray();
                        spec.m_function_name = "next_token".toCharArray();
                        spec.m_type_name = "java_cup.runtime.Symbol".toCharArray();
                        
                        break;
                    }
                    
                    // Bad directive
                    Error.parseError(Error.E_DIRECT, input.lineNumber);
                    break;
                
                case 'e':
                    if (0 == CUtility.charncmp(input.line, 0, eofCodeDir, 0, eofCodeDir.length - 1)) {
                        spec.m_eof_code = packCode(
                            eofCodeDir,
                            eofCodeEndDir,
                            spec.m_eof_code,
                            spec.m_eof_read,
                            EOF_CODE
                        );
                        
                        break;
                    } else if (0 == CUtility.charncmp(input.line, 0, eofValueCodeDir, 0, eofValueCodeDir.length - 1)) {
                        spec.m_eof_value_code = packCode(
                            eofValueCodeDir,
                            eofValueCodeEndDir,
                            spec.m_eof_value_code,
                            spec.m_eof_value_read,
                            EOF_VALUE_CODE
                        );
                        
                        break;
                    } else if (0 == CUtility.charncmp(input.line, 0, eofThrowCodeDir, 0, eofThrowCodeDir.length - 1)) {
                        spec.m_eof_throw_code = packCode(
                            eofThrowCodeDir,
                            eofThrowCodeEndDir,
                            spec.m_eof_throw_code,
                            spec.m_eof_throw_read,
                            EOF_THROW_CODE
                        );
                        
                        break;
                    }
                    
                    // Bad directive
                    Error.parseError(Error.E_DIRECT, input.lineNumber);
                    break;
                
                case 'f':
                    if (0 == CUtility.charncmp(input.line, 0, functionDir, 0, functionDir.length - 1)) {
                        // Set line counting to ON
                        
                        input.lineIndex = functionDir.length;
                        spec.m_function_name = getName();
                        
                        break;
                    } else if (0 == CUtility.charncmp(input.line, 0, fullDir, 0, fullDir.length - 1)) {
                        input.lineIndex = fullDir.length;
                        spec.m_dtrans_ncols = CUtility.MAX_EIGHT_BIT + 1;
                        
                        break;
                    }
                    
                    // Bad directive
                    Error.parseError(Error.E_DIRECT, input.lineNumber);
                    break;
                
                case 'i':
                    if (0 == CUtility.charncmp(input.line, 0, integerDir, 0, integerDir.length - 1)) {
                        // Set line counting to ON
                        
                        input.lineIndex = integerDir.length;
                        spec.m_integer_type = true;
                        
                        break;
                    } else if (0 == CUtility.charncmp(input.line, 0, intwrapDir, 0, intwrapDir.length - 1)) {
                        // Set line counting to ON
                        
                        input.lineIndex = integerDir.length;
                        spec.m_intwrap_type = true;
                        
                        break;
                    } else if (0 == CUtility.charncmp(input.line, 0, initCodeDir, 0, initCodeDir.length - 1)) {
                        spec.m_init_code = packCode(
                            initCodeDir,
                            initCodeEndDir,
                            spec.m_init_code,
                            spec.m_init_read,
                            INIT_CODE
                        );
                        
                        break;
                    } else if (0 == CUtility.charncmp(input.line, 0, initThrowCodeDir, 0, initThrowCodeDir.length - 1)) {
                        spec.m_init_throw_code = packCode(
                            initThrowCodeDir,
                            initThrowCodeEndDir,
                            spec.m_init_throw_code,
                            spec.m_init_throw_read,
                            INIT_THROW_CODE
                        );
                        
                        break;
                    } else if (0 == CUtility.charncmp(input.line, 0, implementsDir, 0, implementsDir.length - 1)) {
                        input.lineIndex = implementsDir.length;
                        spec.m_implements_name = getName();
                        
                        break;
                    } else if (0 == CUtility.charncmp(input.line, 0, ignorecaseDir, 0, ignorecaseDir.length - 1)) {
                        // Set m_ignorecase to ON
                        
                        input.lineIndex = ignorecaseDir.length;
                        spec.m_ignorecase = true;
                        
                        break;
                    }
                    
                    // Bad directive
                    Error.parseError(Error.E_DIRECT, input.lineNumber);
                    break;
                
                case 'l':
                    if (0 == CUtility.charncmp(input.line, 0, lineDir, 0, lineDir.length - 1)) {
                        // Set line counting to ON
                        
                        input.lineIndex = lineDir.length;
                        spec.m_count_lines = true;
                        
                        break;
                    }
                    
                    // Bad directive
                    Error.parseError(Error.E_DIRECT, input.lineNumber);
                    break;
                
                case 'n':
                    if (0 == CUtility.charncmp(input.line, 0, notunixDir, 0, notunixDir.length - 1)) {
                        // Set line counting to ON
                        
                        input.lineIndex = notunixDir.length;
                        spec.m_unix = false;
                        
                        break;
                    }
                    
                    // Bad directive
                    Error.parseError(Error.E_DIRECT, input.lineNumber);
                    break;
                
                case 'p':
                    if (0 == CUtility.charncmp(input.line, 0, publicDir, 0, publicDir.length - 1)) {
                        // Set public flag
                        
                        input.lineIndex = publicDir.length;
                        spec.m_public = true;
                        
                        break;
                    }
                    
                    // Bad directive
                    Error.parseError(Error.E_DIRECT, input.lineNumber);
                    break;
                
                case 's':
                    if (0 == CUtility.charncmp(input.line, 0, stateDir, 0, stateDir.length - 1)) {
                        // Recognize state list
                        
                        input.lineIndex = stateDir.length;
                        saveStates();
                        
                        break;
                    }
                    
                    // Undefined directive
                    Error.parseError(Error.E_DIRECT, input.lineNumber);
                    break;
                
                case 't':
                    if (0 == CUtility.charncmp(input.line, 0, typeDir, 0, typeDir.length - 1)) {
                        // Set Java CUP compatibility to ON
                        
                        input.lineIndex = typeDir.length;
                        spec.m_type_name = getName();
                        
                        break;
                    }
                    
                    // Undefined directive
                    Error.parseError(Error.E_DIRECT, input.lineNumber);
                    break;
                
                case 'u':
                    if (0 == CUtility.charncmp(input.line, 0, unicodeDir, 0, unicodeDir.length - 1)) {
                        input.lineIndex = unicodeDir.length;
                        spec.m_dtrans_ncols = CUtility.MAX_SIXTEEN_BIT + 1;
                        
                        break;
                    }
                    
                    // Bad directive
                    Error.parseError(Error.E_DIRECT, input.lineNumber);
                    break;
                
                case 'y':
                    if (0 == CUtility.charncmp(input.line, 0, yyeofDir, 0, yyeofDir.length - 1)) {
                        input.lineIndex = yyeofDir.length;
                        spec.m_yyeof = true;
                        
                        break;
                    } else if (0 == CUtility.charncmp(input.line, 0, yylexThrowCodeDir, 0, yylexThrowCodeDir.length - 1)) {
                        spec.m_yylex_throw_code = packCode(
                            yylexThrowCodeDir,
                            yylexThrowCodeEndDir,
                            spec.m_yylex_throw_code,
                            spec.m_yylex_throw_read,
                            YYLEX_THROW_CODE
                        );
                        
                        break;
                    }
                    
                    // Bad directive
                    Error.parseError(Error.E_DIRECT, input.lineNumber);
                    break;
                
                default:
                    // Undefined directive.
                    Error.parseError(Error.E_DIRECT, input.lineNumber);
                    
                    break;
                }
            } else {
                // Regular expression macro
                input.lineIndex = 0;
                
                saveMacro();
            }
            
            if (CUtility.OLD_DEBUG) {
                System.out.println("Line number " + input.lineNumber + ":");
                System.out.print(new String(input.line, 0, input.lineLength));
            }
        }
    }
    
    /**
     * Processes third section of JLex
     * specification and creates minimized transition table.
     */
    private void userRules() throws java.io.IOException {
        if (!initFlag) {
            Error.parseError(Error.E_INIT, 0);
        }
        
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != out);
            CUtility.ASSERT(null != input);
            CUtility.ASSERT(null != tokens);
            CUtility.ASSERT(null != spec);
        }
        
        // TODO: UNDONE: Need to handle states preceding rules
        
        if (spec.m_verbose) {
            System.out.println("Creating NFA machine representation.");
        }
        
        makeNFA.allocateBolEof(spec);
        makeNFA.thompson(this, spec, input);
        
        simplifyNFA.simplify(spec);
        
        // printNFA();
        
        if (CUtility.DEBUG) {
            CUtility.ASSERT(END_OF_INPUT == spec.m_current_token);
        }
        
        if (spec.m_verbose) {
            System.out.println("Creating DFA transition table.");
        }
        
        nfa2DFA.makeDFA(this, spec);
        
        if (CUtility.FOODEBUG) {
            printHeader();
        }
        
        if (spec.m_verbose) {
            System.out.println("Minimizing DFA transition table.");
        }
        
        minimize.minDFA(spec);
    }
    
    /**
     * Debugging routine that outputs readable form
     * of character class.
     */
    private void printCCl(Set set) {
        System.out.print(" [");
        
        for (int i = 0; i < spec.m_dtrans_ncols; ++i) {
            if (set.contains(i)) {
                System.out.print(interpretInt(i));
            }
        }
        
        System.out.print(']');
    }
    
    private String stateLabel(NFA state) {
        if (null == state) {
            return ("--");
        }
        
        return Integer.toString(spec.m_NFA_states.indexOf(state));
    }
    
    private String interpretInt(int i) {
        char c = (char) i;
        
        switch (c) {
        case '\b':
            return "\\b";
        
        case '\t':
            return "\\t";
        
        case '\n':
            return "\\n";
        
        case '\f':
            return "\\f";
        
        case '\r':
            return "\\r";
        
        case ' ':
            return "\\ ";
        
        default:
            return Character.toString(c);
        }
    }
    
    void printNFA() {
        System.out.println("--------------------- NFA -----------------------");
        
        for (NFA nfa: spec.m_NFA_states) {
            System.out.print("nfa state " + stateLabel(nfa) + ": ");
            
            if (null == nfa.next) {
                System.out.print("(TERMINAL)");
            } else {
                System.out.print(" --> " + stateLabel(nfa.next));
                System.out.print(" --> " + stateLabel(nfa.next2));
                
                switch (nfa.edge) {
                case NFA.CCL:
                    printCCl(nfa.set);
                    break;
                
                case NFA.EPSILON:
                    System.out.print(" EPSILON ");
                    break;
                
                default:
                    System.out.print(" " + interpretInt(nfa.edge));
                    break;
                }
            }
            
            if (0 == spec.m_NFA_states.indexOf(nfa)) {
                System.out.print(" (START STATE)");
            }
            
            if (null != nfa.accept) {
                System.out.print(
                    " accepting " +
                    ((0 != (nfa.anchor & CSpec.START))? "^": "") +
                    "<" + new String(nfa.accept.action, 0, nfa.accept.actionLength) + ">" +
                    ((0 != (nfa.anchor & CSpec.END))? "$": "")
                );
            }
            
            System.out.println();
        }
        
        for (Map.Entry <String, Integer> entry: spec.m_states.entrySet()) {
            String state = entry.getKey();
            int i = entry.getValue();
            
            System.out.println("State \"" + state + "\" has identifying index " + i + ".");
            System.out.print("\tStart states of matching rules: ");
    
            for (NFA nfa: spec.m_state_rules[i]) {
                System.out.print(spec.m_NFA_states.indexOf(nfa) + " ");
            }
    
            System.out.println();
        }
        
        System.out.println("-------------------- NFA ----------------------");
    }
    
    /**
     * Parses the state area of a rule,
     * from the beginning of a line.
     *
     * < state1, state2 ... > regular_expression { action }
     *
     * Returns null on only EOF. Returns all_states,
     * initialied properly to correspond to all states,
     * if no states are found.
     *
     * Special Notes: This function treats commas as optional
     * and permits states to be spread over multiple line.
     */
    SparseBitSet getStates() throws IOException {
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != out);
            CUtility.ASSERT(null != input);
            CUtility.ASSERT(null != tokens);
            CUtility.ASSERT(null != spec);
        }
        
        // Skip white space
        while (CUtility.isspace(input.line[input.lineIndex])) {
            ++input.lineIndex;
            
            while (input.lineIndex >= input.lineLength) {
                // Must just be an empty line
                if (input.getLine()) {
                    // EOF found
                    return null;
                }
            }
        }
        
        // Look for states
        if ('<' == input.line[input.lineIndex]) {
            ++input.lineIndex;
            
            SparseBitSet states = new SparseBitSet();
            
            // Parse states
            while (true) {
                // We may have reached the end of the line
                while (input.lineIndex >= input.lineLength) {
                    if (input.getLine()) {
                        // EOF found
                        
                        Error.parseError(Error.E_EOF, input.lineNumber);
                        return states;
                    }
                }
                
                while (true) {
                    // Skip white space
                    while (CUtility.isspace(input.line[input.lineIndex])) {
                        ++input.lineIndex;
                        
                        while (input.lineIndex >= input.lineLength) {
                            if (input.getLine()) {
                                // EOF found
                                
                                Error.parseError(Error.E_EOF, input.lineNumber);
                                return states;
                            }
                        }
                    }
                    
                    if (',' != input.line[input.lineIndex]) {
                        break;
                    }
                    
                    ++input.lineIndex;
                }
                
                if ('>' == input.line[input.lineIndex]) {
                    ++input.lineIndex;
                    
                    if (input.lineIndex < input.lineLength) {
                        advanceStop = true;
                    }
                    
                    return states;
                }
                
                // Read in state name
                int startState = input.lineIndex;
                while (
                    !CUtility.isspace(input.line[input.lineIndex]) &&
                    ',' != input.line[input.lineIndex] &&
                    '>' != input.line[input.lineIndex]
                ) {
                    ++input.lineIndex;
                    
                    if (input.lineIndex >= input.lineLength) {
                        // End of line means end of state name
                        break;
                    }
                }
                
                int countState = input.lineIndex - startState;
                
                // Save name after checking definition
                String name = new String(input.line, startState, countState);
                Integer index = spec.m_states.get(name);
                
                if (null == index) {
                    // Uninitialized state
                    
                    System.out.println("Uninitialized State Name: " + name);
                    Error.parseError(Error.E_STATE, input.lineNumber);
                    
                    // For IDE
                    throw new Error();
                }
                
                states.set(index);
            }
        }
        
        if (null == all_states) {
            all_states = new SparseBitSet();
            
            for (int i = 0; i < spec.m_states.size(); ++i) {
                all_states.set(i);
            }
        }
        
        if (input.lineIndex < input.lineLength) {
            advanceStop = true;
        }
        
        return all_states;
    }
    
    /**
     * @return false on error, true otherwise.
     */
    private boolean expandMacro() {
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != out);
            CUtility.ASSERT(null != input);
            CUtility.ASSERT(null != tokens);
            CUtility.ASSERT(null != spec);
        }
        
        // Check for macro
        if ('{' != input.line[input.lineIndex]) {
            Error.parseError(Error.E_INTERNAL, input.lineNumber);
            return ERROR;
        }
        
        int macroStart = input.lineIndex;
        
        int elem = input.lineIndex + 1;
        if (elem >= input.lineLength) {
            Error.msg("Unfinished macro name");
            return ERROR;
        }
        
        // Get macro name
        int startName = elem;
        while ('}' != input.line[elem]) {
            ++elem;
            
            if (elem >= input.lineLength) {
                Error.msg("Unfinished macro name at line " + input.lineNumber);
                return ERROR;
            }
        }
        
        int nameLength = elem - startName;
        int macroEnd = elem;
        
        // Check macro name
        if (0 == nameLength) {
            Error.msg("Empty macro name");
            return ERROR;
        }
        
        // Debug checks
        if (CUtility.DEBUG) {
            CUtility.ASSERT(0 < nameLength);
        }
        
        // Get macro definition
        String name = new String(input.line, startName, nameLength);
        String def = spec.m_macros.get(name);
        
        if (null == def) {
            // Error.msg("Undefined macro \"" + name + "\".")
            System.out.println("Error: Undefined macro \"" + name + "\".");
            
            Error.parseError(Error.E_NOMAC, input.lineNumber);
            return ERROR;
        }
        
        if (CUtility.OLD_DUMP_DEBUG) {
            System.out.println("expanded escape: " + def);
        }
		
        // Replace macro in new buffer,
        // beginning by copying first part of line buffer.
        
        char replace[] = new char[input.line.length];
        System.arraycopy(input.line, 0, replace, 0, macroStart);
        
        int cursor = macroStart;
        for (int defElem = 0; defElem < def.length(); ++defElem, ++cursor) {
            if (cursor >= replace.length) {
                replace = CUtility.doubleSize(replace);
            }
            
            replace[cursor] = def.charAt(defElem);
        }
        
        // Copy last part of line
        for (elem = macroEnd + 1; elem < input.lineLength; ++elem, ++cursor) {
            if (cursor >= replace.length) {
                replace = CUtility.doubleSize(replace);
            }
            
            replace[cursor] = input.line[elem];
        }
        
        // Replace buffer
        input.line = replace;
        input.lineLength = cursor;
        
        if (CUtility.OLD_DEBUG) {
            System.out.println(new String(input.line, 0, input.lineLength));
        }
        
        return NOT_ERROR;
    }
    
    /**
     * Saves macro definition of form:
     * macroName = macroDefinition
     */
    private void saveMacro() {
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != out);
            CUtility.ASSERT(null != input);
            CUtility.ASSERT(null != tokens);
            CUtility.ASSERT(null != spec);
        }

        // Macro declarations are of the following form:
        // macroName = macroDefinition
        
        // Skip white space preceding macro name
        while (CUtility.isspace(input.line[input.lineIndex])) {
            ++input.lineIndex;
            
            if (input.lineIndex >= input.lineLength) {
                // End of line has been reached,
                // and line was found to be empty.
                return;
            }
        }
        
        // Read macro name
        int startName = input.lineIndex;
        while (!CUtility.isspace(input.line[input.lineIndex]) && '=' != input.line[input.lineIndex]) {
            ++input.lineIndex;
            
            if (input.lineIndex >= input.lineLength) {
                // Macro has no associated definition
                Error.parseError(Error.E_MACDEF, input.lineNumber);
            }
        }
        
        int nameLength = input.lineIndex - startName;
        
        // Check macro name
        if (0 == nameLength) {
            // Empty macro name
            Error.parseError(Error.E_MACDEF, input.lineNumber);
        }
        
        // Skip white space after name
        while (CUtility.isspace(input.line[input.lineIndex])) {
            ++input.lineIndex;
            
            if (input.lineIndex >= input.lineLength) {
                // Macro has no associated definition
                Error.parseError(Error.E_MACDEF, input.lineNumber);
            }
        }
        
        if ('=' == input.line[input.lineIndex]) {
            ++input.lineIndex;
            
            if (input.lineIndex >= input.lineLength) {
                // Macro has no associated definition
                Error.parseError(Error.E_MACDEF, input.lineNumber);
            }
        } else {
            // Macro definition without =
            Error.parseError(Error.E_MACDEF, input.lineNumber);
        }
        
        // Skip white space before definition
        while (CUtility.isspace(input.line[input.lineIndex])) {
            ++input.lineIndex;
            
            if (input.lineIndex >= input.lineLength) {
                // Macro name but no associated definition
                Error.parseError(Error.E_MACDEF, input.lineNumber);
            }
        }
        
        // Read macro definition
        int startDef = input.lineIndex;
        
        boolean inQuote   = false; // Between " and "
        boolean inCCl     = false; // Between [ and ]
        boolean isEscaped = false; // After \
        while (!CUtility.isspace(input.line[input.lineIndex]) || inQuote || inCCl || isEscaped) {
            // If current char is " and it is not escaped trigger inQuote
            if ('"' == input.line[input.lineIndex] && !isEscaped) {
                inQuote = !inQuote;
            }
    
            // If current char is \ and it is not escaped switch isEscaped on
            isEscaped = ('\\' == input.line[input.lineIndex] && !isEscaped);
            
            // If current char is not escaped and is not in quote...
            if (!isEscaped && !inQuote) {
                // CSA, 24-jul-99
                
                // and it is [ switch inCCl on
                if ('[' == input.line[input.lineIndex] && !inCCl) {
                    inCCl = true;
                }
    
                // and it is ] switch inCCl off
                if (']' == input.line[input.lineIndex] && inCCl) {
                    inCCl = false;
                }
            }
    
            ++input.lineIndex;
            if (input.lineIndex >= input.lineLength) {
                // End of line
                break;
            }
        }
        
        int defLength = input.lineIndex - startDef;
        
        // Check macro definition
        if (0 == defLength) {
            // Empty macro definition
            Error.parseError(Error.E_MACDEF, input.lineNumber);
        }
        
        // Debug checks
        if (CUtility.DEBUG) {
            CUtility.ASSERT(0 < defLength);
            CUtility.ASSERT(0 < nameLength);
            CUtility.ASSERT(null != spec.m_macros);
        }
        
        if (CUtility.OLD_DEBUG) {
            System.out.println("macro name \"" + new String(input.line, startName, nameLength) + "\".");
            System.out.println("macro definition \"" + new String(input.line, startDef, defLength) + "\".");
        }
        
        // Add macro name and definition to table
        spec.m_macros.put(
            new String(input.line, startName, nameLength),
            new String(input.line, startDef, defLength)
        );
    }
    
    /**
     * Takes state declaration and makes entries
     * for them in state hashtable in CSpec structure.
     *
     * State declaration should be of the form:
     * %state name0[, name1, name2 ...]
     *
     * (But commas are actually optional as long as there is
     * white space in between them.)
     */
    private void saveStates() {
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != out);
            CUtility.ASSERT(null != input);
            CUtility.ASSERT(null != tokens);
            CUtility.ASSERT(null != spec);
        }
        
        // EOF found?
        if (input.isEOFReached) {
            return;
        }
        
        // Debug checks
        if (CUtility.DEBUG) {
            CUtility.ASSERT('%' == input.line[0]);
            CUtility.ASSERT('s' == input.line[1]);
            CUtility.ASSERT(input.lineIndex <= input.lineLength);
            CUtility.ASSERT(0 <= input.lineIndex);
            CUtility.ASSERT(0 <= input.lineLength);
        }
        
        // Blank line? No states?
        if (input.lineIndex >= input.lineLength) {
            return;
        }
        
        while (input.lineIndex < input.lineLength) {
            if (CUtility.OLD_DEBUG) {
                System.out.println("line read " + input.lineLength + "\tline index = " + input.lineIndex);
            }
            
            // Skip white space
            while (CUtility.isspace(input.line[input.lineIndex])) {
                ++input.lineIndex;
                
                if (input.lineIndex >= input.lineLength) {
                    // No more states to be found
                    return;
                }
            }
            
            // Look for state name
            int stateStart = input.lineIndex;
            while (!CUtility.isspace(input.line[input.lineIndex]) && ',' != input.line[input.lineIndex]) {
                ++input.lineIndex;
                
                if (input.lineIndex >= input.lineLength) {
                    // End of line and end of state name
                    break;
                }
            }
            
            int stateLength = input.lineIndex - stateStart;
            
            if (CUtility.OLD_DEBUG) {
                System.out.println("State name \"" + new String(input.line, stateStart, stateLength) + "\".");
                System.out.println("Integer index \"" + spec.m_states.size() + "\".");
            }
            
            // Enter new state name, along with unique index
            spec.m_states.put(new String(input.line, stateStart, stateLength), spec.m_states.size());
            
            // Skip comma
            if (',' == input.line[input.lineIndex]) {
                ++input.lineIndex;
                
                if (input.lineIndex >= input.lineLength) {
                    // End of line
                    return;
                }
            }
        }
    }
    
    /**
     * Takes escape sequence and returns
     * corresponding character code.
     */
    private char expandEscape() {
        // Debug checks
        if (CUtility.DEBUG) {
            CUtility.ASSERT(input.lineIndex < input.lineLength);
            CUtility.ASSERT(0 < input.lineLength);
            CUtility.ASSERT(0 <= input.lineIndex);
        }
        
        if ('\\' != input.line[input.lineIndex]) {
            ++input.lineIndex;
            
            return input.line[input.lineIndex - 1];
        } else {
            boolean unicodeEscape = false;
            char r;
            
            ++input.lineIndex;
            switch (input.line[input.lineIndex]) {
            case 'b':
                ++input.lineIndex;
                return '\b';
            
            case 't':
                ++input.lineIndex;
                return '\t';
            
            case 'n':
                ++input.lineIndex;
                return '\n';
            
            case 'f':
                ++input.lineIndex;
                return '\f';
            
            case 'r':
                ++input.lineIndex;
                return '\r';
            
            case '^':
                ++input.lineIndex;
                
                r = Character.toUpperCase(input.line[input.lineIndex]);
                
                // Check for char is in @..Z range
                if (r < '@' || r > 'Z') { // non-fatal
                    Error.parseError(Error.E_BADCTRL, input.lineNumber);
                }
                
                ++input.lineIndex;
                return (char) (r - '@');
            
            case 'u':
                unicodeEscape = true;
                
            case 'x':
                ++input.lineIndex;
                
                r = 0;
                for (int i = 0; i < (unicodeEscape? 4: 2); i++) {
                    if (CUtility.ishexdigit(input.line[input.lineIndex])) {
                        r = (char) (r << 4);
                        r = (char) (r | CUtility.hex2bin(input.line[input.lineIndex]));
                        
                        ++input.lineIndex;
                    } else {
                        break;
                    }
                }
                
                return r;
            
            default:
                if (!CUtility.isoctdigit(input.line[input.lineIndex])) {
                    r = input.line[input.lineIndex];
                    ++input.lineIndex;
                } else {
                    r = 0;
                    
                    for (int i = 0; i < 3; i++) {
                        if (CUtility.isoctdigit(input.line[input.lineIndex])) {
                            r = (char) (r << 3);
                            r = (char) (r | CUtility.oct2bin(input.line[input.lineIndex]));
                            
                            ++input.lineIndex;
                        } else {
                            break;
                        }
                    }
                }
                
                return r;
            }
        }
    }
    
    /**
     * Packages and returns Accept
     * for action next in input stream.
     */
    Accept packAccept() throws IOException {
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != out);
            CUtility.ASSERT(null != input);
            CUtility.ASSERT(null != tokens);
            CUtility.ASSERT(null != spec);
        }
        
        // Get a new line, if needed
        while (input.lineIndex >= input.lineLength) {
            if (input.getLine()) {
                Error.parseError(Error.E_EOF, input.lineNumber);
                return null;
            }
        }
        
        // Look for beginning of action
        while (CUtility.isspace(input.line[input.lineIndex])) {
            ++input.lineIndex;
            
            // Get a new line, if needed
            while (input.lineIndex >= input.lineLength) {
                if (input.getLine()) {
                    Error.parseError(Error.E_EOF, input.lineNumber);
                    return null;
                }
            }
        }
        
        // Look for brackets
        if ('{' != input.line[input.lineIndex]) {
            Error.parseError(Error.E_BRACE, input.lineNumber);
        }
    
        char action[] = new char[BUFFER_SIZE];
        int actionIndex = 0;
    
        // Copy new line into action buffer
        int brackets = 0;
        
        boolean inSingleQuotes = false;
        boolean inDoubleQuotes = false;
        boolean inSlashComment = false;
        boolean inStarComment  = false;
        boolean isEscaped      = false;
        boolean isSlashed      = false;
        while (true) {
            // Double the buffer size, if needed
            if (actionIndex >= action.length) {
                action = CUtility.doubleSize(action);
            }
            
            action[actionIndex] = input.line[input.lineIndex];
            ++actionIndex;
            
            // Flag for reduce conditions complexity. See git-blame for this lines
            boolean triggerQuoting = true;
            if (inSingleQuotes || inDoubleQuotes) {
                if (isEscaped) {
                    
                    isEscaped = false; // only protects one char, but this is enough.
                    triggerQuoting = false;
                } else if ('\\' == input.line[input.lineIndex]) {
                    
                    isEscaped = true;
                    triggerQuoting = false;
                }
            }
            
            if (triggerQuoting && !inSlashComment && !inStarComment) {
                if (!inSingleQuotes && '"' == input.line[input.lineIndex]) {
                    
                    inDoubleQuotes = !inDoubleQuotes; // unescaped double quote.
                } else if (!inDoubleQuotes && '\'' == input.line[input.lineIndex]) {
    
                    inSingleQuotes = !inSingleQuotes; // unescaped single quote.
                }
            }
            
            // Look for comments
            if (inStarComment) { // inside "/*" comment; look for "*/"
                if (isSlashed && '/' == input.line[input.lineIndex]) {
                    
                    inStarComment = isSlashed = false;
                } else { // note that inside a star comment, isSlashed means starred
                    
                    isSlashed = ('*' == input.line[input.lineIndex]);
                }
            } else if (!inSlashComment && !inSingleQuotes && !inDoubleQuotes) {
                // not in comment, look for /* or //
                
                inSlashComment = (isSlashed && '/' == input.line[input.lineIndex]);
                inStarComment  = (isSlashed && '*' == input.line[input.lineIndex]);
                
                isSlashed = ('/' == input.line[input.lineIndex]);
            }
            
            // Look for brackets
            if (!inSingleQuotes && !inDoubleQuotes && !inStarComment && !inSlashComment) {
                if ('{' == input.line[input.lineIndex]) {
                    
                    ++brackets;
                } else if ('}' == input.line[input.lineIndex]) {
                    --brackets;
                    
                    if (0 == brackets) {
                        break;
                    }
                }
            }
            
            ++input.lineIndex;
            
            // Get a new line, if needed
            while (input.lineIndex >= input.lineLength) {
                inSlashComment = isSlashed = false;
                
                if (inSingleQuotes || inDoubleQuotes) { // non-fatal
                    Error.parseError(Error.E_NEWLINE, input.lineNumber);
                    inSingleQuotes = inDoubleQuotes = false;
                }
                
                if (input.getLine()) {
                    Error.parseError(Error.E_SYNTAX, input.lineNumber);
                    return null;
                }
            }
        }
    
        ++input.lineIndex;
        
        Accept accept = new Accept(action, actionIndex, input.lineNumber);
        
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != accept);
        }
        
        if (CUtility.DESCENT_DEBUG) {
            System.out.print("Accepting action:");
            System.out.println(new String(accept.action, 0, accept.actionLength));
        }
        
        return accept;
    }
    
    /**
     * Returns code for next token.
     */
    int advance() throws java.io.IOException {
        if (input.isEOFReached) {
	        // EOF has already been reached,
	        // so return appropriate code.
            
            spec.m_current_token = END_OF_INPUT;
            spec.m_lexeme = '\0';
            
            return spec.m_current_token;
        }

	    // End of previous regular expression?
	    // Refill line buffer?
        if (EOS == spec.m_current_token || input.lineIndex >= input.lineLength) {
            if (spec.m_in_quote) {
                Error.parseError(Error.E_SYNTAX, input.lineNumber);
            }
    
            do {
                if (!advanceStop || input.lineIndex >= input.lineLength) {
                    if (input.getLine()) {
                        /* EOF has already been reached,
                           so return appropriate code. */
                
                        spec.m_current_token = END_OF_INPUT;
                        spec.m_lexeme = '\0';
                        
                        return spec.m_current_token;
                    }
                    
                    input.lineIndex = 0;
                } else {
                    advanceStop = false;
                }
        
                while (
                    input.lineIndex < input.lineLength &&
                    CUtility.isspace(input.line[input.lineIndex])
                ) {
                    ++input.lineIndex;
                }
            } while (input.lineIndex >= input.lineLength);
        }
        
        if (CUtility.DEBUG) {
            CUtility.ASSERT(input.lineIndex <= input.lineLength);
        }
        
        while (true) {
            if (!spec.m_in_quote && '{' == input.line[input.lineIndex]) {
                if (!expandMacro()) {
                    break;
                }
                
                if (input.lineIndex >= input.lineLength) {
                    spec.m_current_token = EOS;
                    spec.m_lexeme = '\0';
                    
                    return spec.m_current_token;
                }
            } else if ('\"' == input.line[input.lineIndex]) {
                spec.m_in_quote = !spec.m_in_quote;
                ++input.lineIndex;
                
                if (input.lineIndex >= input.lineLength) {
                    spec.m_current_token = EOS;
                    spec.m_lexeme = '\0';
                    
                    return spec.m_current_token;
                }
            } else {
                break;
            }
        }
        
        if (input.lineIndex > input.lineLength) {
            System.out.println("input.lineIndex = " + input.lineIndex);
            System.out.println("input.lineLength = " + input.lineLength);
            
            CUtility.ASSERT(input.lineIndex <= input.lineLength);
        }

        /* Look for backslash, and corresponding
           escape sequence. */
        boolean saw_escape = ('\\' == input.line[input.lineIndex]);
        
        if (!spec.m_in_quote) {
            if (!spec.m_in_ccl && CUtility.isspace(input.line[input.lineIndex])) {
                /* White space means the end of
                   the current regular expression. */
                
                spec.m_current_token = EOS;
                spec.m_lexeme = '\0';
                
                return spec.m_current_token;
            }
            
            // Process escape sequence, if needed
            if (saw_escape) {
                spec.m_lexeme = expandEscape();
            } else {
                spec.m_lexeme = input.line[input.lineIndex];
                ++input.lineIndex;
            }
        } else {
            if (
                saw_escape &&
                (input.lineIndex + 1) < input.lineLength &&
                '\"' == input.line[input.lineIndex + 1]
            ) {
                spec.m_lexeme = '\"';
                input.lineIndex = input.lineIndex + 2;
            } else {
                spec.m_lexeme = input.line[input.lineIndex];
                ++input.lineIndex;
            }
        }
    
        Integer code = tokens.get(spec.m_lexeme);
        if (spec.m_in_quote || saw_escape) {
            spec.m_current_token = L;
        } else {
            if (null == code) {
                spec.m_current_token = L;
            } else {
                spec.m_current_token = code;
            }
        }
        
        if (CCL_START == spec.m_current_token) {
            spec.m_in_ccl = true;
        }
        
        if (CCL_END == spec.m_current_token) {
            spec.m_in_ccl = false;
        }
        
        if (CUtility.FOODEBUG) {
            System.out.println(
                "Lexeme: " + spec.m_lexeme +
                "\tToken: " + spec.m_current_token +
                "\tIndex: " + input.lineIndex
            );
        }
        
        return spec.m_current_token;
    }
    
    /**
     * High level debugging routine.
     */
    private void details() {
        System.out.println();
        System.out.println("\t** Macros **");
        
        for (Map.Entry <String, String> entry: spec.m_macros.entrySet()) {
            String name = entry.getKey();
            String def = entry.getValue();
    
            if (CUtility.DEBUG) {
                CUtility.ASSERT(null != def);
            }
    
            System.out.println("Macro name \"" + name + "\" has definition \"" + def + "\".");
        }
        
        System.out.println();
        System.out.println("\t** States **");
    
        for (Map.Entry <String, Integer> entry: spec.m_states.entrySet()) {
            System.out.println("State \"" + entry.getValue() + "\" has identifying index " + entry.getKey() + ".");
        }
        
        System.out.println();
        System.out.println("\t** Character Counting **");
        
        if (!spec.m_count_chars) {
            System.out.println("Character counting is off.");
        } else {
            if (CUtility.DEBUG) {
                CUtility.ASSERT(spec.m_count_lines);
            }
            
            System.out.println("Character counting is on.");
        }
        
        System.out.println();
        System.out.println("\t** Line Counting **");
        
        if (!spec.m_count_lines) {
            System.out.println("Line counting is off.");
        } else {
            System.out.println("Line counting is on.");
        }
        
        System.out.println();
        System.out.println("\t** Operating System Specificity **");
        
        if (!spec.m_unix) {
            System.out.println("Not generating UNIX-specific code.");
            System.out.println("(This means that \"\\r\\n\" is a newline, rather than \"\\n\".)");
        } else {
            System.out.println("Generating UNIX-specific code.");
            System.out.println("(This means that \"\\n\" is a newline, rather than \"\\r\\n\".)");
        }
        
        System.out.println();
        System.out.println("\t** Java CUP Compatibility **");
        
        if (!spec.m_cup_compatible) {
            System.out.println("Generating CUP compatible code.");
            System.out.println("(Scanner implements java_cup.runtime.Scanner.)");
        } else {
            System.out.println("Not generating CUP compatible code.");
        }
        
        if (CUtility.FOODEBUG) {
            if (null != spec.m_NFA_states && null != spec.m_NFA_start) {
                System.out.println();
                System.out.println("\t** NFA machine **");
                
                printNFA();
            }
        }
        
        if (null != spec.m_dtrans_vector) {
            System.out.println();
            System.out.println("\t** DFA transition table **");
            
            // printHeader();
        }
    }
    
    void printSet(Vector <NFA> nfaSet) {
        int size = nfaSet.size();
        
        if (0 == size) {
            System.out.print("empty ");
        }
        
        for (NFA nfa: nfaSet) {
            // System.out.print(spec.m_NFA_states.indexOf(nfa) + " ");
            System.out.print(nfa.label + " ");
        }
    }
    
    private void printHeader() {
        System.out.println("/*---------------------- DFA -----------------------");
    
        for (Map.Entry <String, Integer> entry: spec.m_states.entrySet()) {
            String state = entry.getKey();
            int i = entry.getValue();
    
            System.out.println("State \"" + state + "\" has identifying index " + i + ".");
    
            if (DTrans.F != spec.m_state_dtrans[i]) {
                System.out.println("\tStart index in transition table: " + spec.m_state_dtrans[i]);
            } else {
                System.out.println("\tNo associated transition states.");
            }
        }
        
        for (int i = 0; i < spec.m_dtrans_vector.size(); ++i) {
            DTrans dTrans = spec.m_dtrans_vector.elementAt(i);
            
            if (null == spec.m_accept_vector && null == spec.m_anchor_array) {
                if (null == dTrans.accept) {
                    System.out.print(" * State " + i + " [nonaccepting]");
                } else {
                    System.out.print(
                        " * State " + i +
                        " [accepting, line " + dTrans.accept.lineNumber +
                        " <" + new String(dTrans.accept.action, 0, dTrans.accept.actionLength) + ">]"
                    );
                    
                    if (CSpec.NONE != dTrans.anchor) {
                        System.out.print(
                            " Anchor: " +
                            ((0 != (dTrans.anchor & CSpec.START))? "start ": "") +
                            ((0 != (dTrans.anchor & CSpec.END))? "end ": "")
                        );
                    }
                }
            } else {
                Accept accept = spec.m_accept_vector.elementAt(i);
                
                if (null == accept) {
                    System.out.print(" * State " + i + " [nonaccepting]");
                } else {
                    System.out.print(
                        " * State " + i +
                        " [accepting, line " + accept.lineNumber +
                        " <" + new String(accept.action, 0, accept.actionLength) + ">]"
                    );
                    
                    if (CSpec.NONE != spec.m_anchor_array[i]) {
                        System.out.print(
                            " Anchor: " +
                            ((0 != (spec.m_anchor_array[i] & CSpec.START))? "start ": "") +
                            ((0 != (spec.m_anchor_array[i] & CSpec.END))? "end ": "")
                        );
                    }
                }
            }
            
            int lastTransition = -1;
            for (int j = 0; j < spec.m_dtrans_ncols; ++j) {
                if (DTrans.F != dTrans.dtrans[j]) {
                    int chars_printed = 0;
                    
                    if (lastTransition != dTrans.dtrans[j]) {
                        System.out.println();
                        System.out.print(" *    goto " + dTrans.dtrans[j] + " on ");
                    }
                    
                    String str = interpretInt(j);
                    System.out.print(str);
                    
                    chars_printed = chars_printed + str.length();
                    if (56 < chars_printed) {
                        System.out.println();
                        System.out.print(" *             ");
                    }
                    
                    lastTransition = dTrans.dtrans[j];
                }
            }
            
            System.out.println();
        }
        
        System.out.println(" */");
        System.out.println();
    }
}
