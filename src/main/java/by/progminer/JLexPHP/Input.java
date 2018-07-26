package by.progminer.JLexPHP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

class Input {
    
    /**
     * Return values for getLine().
     */
    private static final boolean EOF = true;
    private static final boolean NOT_EOF = false;
    
    /**
     * Whether EOF has been encountered.
     */
    boolean isEOFReached;
    
    /**
     * Pushback current line?
     */
    boolean pushbackLine;
    
    /**
     * Line buffer.
     */
    char line[];
    
    /**
     * Number of bytes read into line buffer.
     */
    int lineLength;
    
    /**
     * Current index into line buffer.
     */
    int lineIndex;
    
    /**
     * Current line number.
     */
    int lineNumber;
    
    /**
     * JLex specification file.
     */
    private BufferedReader m_input;
    
    Input(Reader input) {
        if (Utility.DEBUG) {
            Utility.ASSERT(null != input);
        }
        
        // Initialize input stream.
        m_input = new BufferedReader(input);
        
        // Initialize buffers and index counters.
        line = null;
        lineLength = 0;
        lineIndex = 0;
        
        // Initialize state variables.
        isEOFReached = false;
        pushbackLine = false;
        lineNumber = 0;
    }
    
    /**
     * Guarantees not to return a blank line, or a line
     * of zero length.
     *
     * @return true on EOF, false otherwise.
     */
    boolean getLine() throws IOException {
        // Has EOF already been reached?
        if (isEOFReached) {
            return EOF;
        }
        
        lineIndex = 0;
        
        // Pushback current line?
        if (pushbackLine) {
            pushbackLine = false;
            
            // Check for empty line.
            boolean empty = true;
            
            for (int elem = 0; elem < lineLength; ++elem) {
                if (!Utility.isSpace(line[elem])) {
                    empty = false;
                    
                    break;
                }
            }
            
            // Not empty?
            if (!empty) {
                return NOT_EOF;
            }
        }
    
        boolean empty = true;
        do {
            String lineStr;
        
            if (null == (lineStr = m_input.readLine())) {
                isEOFReached = true;
                return EOF;
            }
            
            // TODO Replace Unicode chars to \\uXXXX sequences

            line = (lineStr + "\n").toCharArray();
            lineLength = line.length;
            
            ++lineNumber;
        
            // Check for empty line and discard them.
            for (char c: line) {
                if (!Utility.isSpace(c)) {
                    empty = false;
                    break;
                }
            }
        } while (empty);
        
        return NOT_EOF;
    }
}
