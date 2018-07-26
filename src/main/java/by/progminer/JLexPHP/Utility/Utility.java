package by.progminer.JLexPHP.Utility;

public class Utility {
    
    // static final boolean DEBUG = false;
    public static final boolean DEBUG = true;
    
    // static final boolean SLOW_DEBUG = false;
    public static final boolean SLOW_DEBUG = true;
    
    public static final boolean DESCENT_DEBUG = false;
    public static final boolean OLD_DEBUG = false;
    public static final boolean OLD_DUMP_DEBUG = false;
    public static final boolean FOODEBUG = false;
    public static final boolean DO_DEBUG = false;
    
    /**
     * Integer Bounds
     */
    public static final int INT_MAX = 2147483647;
    
    public static final int MAX_SEVEN_BIT   = 127;
    public static final int MAX_EIGHT_BIT   = 255;
    public static final int MAX_SIXTEEN_BIT = 65535;
    
    /**
     * Debugging routine.
     */
    public static void enter(String descent, char lexeme, int token) {
        System.out.println("Entering " + descent + " [lexeme: " + lexeme + "] [token: " + token + "]");
    }
    
    /**
     * Debugging routine.
     */
    public static void leave(String descent, char lexeme, int token) {
        System.out.println("Leaving " + descent + " [lexeme:" + lexeme + "] [token:" + token + "]");
    }
    
    /**
     * Debugging routine.
     */
    public static void ASSERT(boolean expr) {
        // noinspection ConstantConditions
        assert !DEBUG || expr;
    }
    
    public static char[] doubleSize(char oldBuffer[]) {
        char newBuffer[] = new char[2 * oldBuffer.length];
        int elem;
        
        for (elem = 0; elem < oldBuffer.length; ++elem) {
            newBuffer[elem] = oldBuffer[elem];
        }
        
        return newBuffer;
    }
    
    public static byte[] doubleSize(byte oldBuffer[]) {
        byte newBuffer[] = new byte[2 * oldBuffer.length];
        int elem;
        
        for (elem = 0; elem < oldBuffer.length; ++elem) {
            newBuffer[elem] = oldBuffer[elem];
        }
        
        return newBuffer;
    }
    
    public static char hex2bin(char c) {
        if ('0' <= c && '9' >= c) {
            return (char) (c - '0');
        } else if ('a' <= c && 'f' >= c) {
            return (char) (c - 'a' + 10);
        } else if ('A' <= c && 'F' >= c) {
            return (char) (c - 'A' + 10);
        }
        
        Error.msg("Bad hexidecimal digit" + c);
        return 0;
    }
    
    public static boolean isHexDigit(char c) {
        return ('0' <= c && '9' >= c) ||
               ('a' <= c && 'f' >= c) ||
               ('A' <= c && 'F' >= c);
    }
    
    public static char oct2bin(char c) {
        if ('0' <= c && '7' >= c) {
            return (char) (c - '0');
        }
        
        Error.msg("Bad octal digit " + c);
        return 0;
    }
    
    public static boolean isOctDigit(char c) {
        return '0' <= c && '7' >= c;
    }
    
    public static boolean isSpace(char c) {
        return '\b' == c ||
               '\t' == c ||
               '\n' == c ||
               '\f' == c ||
               '\r' == c ||
               ' ' == c;
    }
    
    public static boolean isNewLine(char c) {
        return '\n' == c || '\r' == c;
    }
    
    /**
     * Compares up to n elements of
     * byte array a[] against byte array b[].
     *
     * The first byte comparison is made between
     * a[a_first] and b[b_first]. Comparisons continue
     * until the null terminating byte '\0' is reached
     * or until n bytes are compared.
     *
     * @return Returns 0 if arrays are the
     * same up to and including the null terminating byte
     * or up to and including the first n bytes,
     * whichever comes first.
     */
    public static int bytesCmp(byte a[], int a_first, byte b[], int b_first, int n) {
        int elem;
        
        for (elem = 0; elem < n; ++elem) {
            // System.out.print((char) a[a_first + elem]);
            // System.out.print((char) b[b_first + elem]);
            
            if ('\0' == a[a_first + elem] && '\0' == b[b_first + elem]) {
                // System.out.println("return 0");
                
                return 0;
            }
            if (a[a_first + elem] < b[b_first + elem]) {
                // System.out.println("return 1");
                
                return 1;
            } else if (a[a_first + elem] > b[b_first + elem]) {
                // System.out.println("return -1");
                
                return -1;
            }
        }
        
        // System.out.println("return 0");
        return 0;
    }
    
    public static int charsCmp(char a[], int a_first, char b[], int b_first, int n) {
        int elem;
        
        for (elem = 0; elem < n; ++elem) {
            if ('\0' == a[a_first + elem] && '\0' == b[b_first + elem]) {
                return 0;
            }
            
            if (a[a_first + elem] < b[b_first + elem]) {
                return 1;
            } else if (a[a_first + elem] > b[b_first + elem]) {
                return -1;
            }
        }
        
        return 0;
    }
}
