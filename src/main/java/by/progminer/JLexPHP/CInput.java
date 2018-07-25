package by.progminer.JLexPHP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

class CInput {
    
    static final boolean EOF = true;
    static final boolean NOT_EOF = false;
    
    /**
     * Whether EOF has been encountered.
     */
    boolean m_eof_reached;
    boolean m_pushback_line;
    
    /**
     * Line buffer.
     */
    char m_line[];
    
    /**
     * Number of bytes read into line buffer.
     */
    int m_line_read;
    
    /**
     * Current index into line buffer.
     */
    int m_line_index;
    
    /**
     * Current line number.
     */
    int m_line_number;
    
    /**
     * JLex specification file.
     */
    private BufferedReader m_input;
    
    CInput(Reader input) {
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != input);
        }
        
        // Initialize input stream.
        m_input = new java.io.BufferedReader(input);
        
        // Initialize buffers and index counters.
        m_line = null;
        m_line_read = 0;
        m_line_index = 0;
        
        // Initialize state variables.
        m_eof_reached = false;
        m_line_number = 0;
        m_pushback_line = false;
    }
    
    /**
     * Returns true on EOF, false otherwise.
     * Guarantees not to return a blank line, or a line
     * of zero length.
     */
    boolean getLine() throws IOException {
        int elem;
        
        // Has EOF already been reached?
        if (m_eof_reached) {
            return EOF;
        }
        
        // Pushback current line?
        if (m_pushback_line) {
            m_pushback_line = false;
            
            // Check for empty line.
            for (elem = 0; elem < m_line_read; ++elem) {
                if (!CUtility.isspace(m_line[elem])) {
                    break;
                }
            }
            
            // Nonempty?
            if (elem < m_line_read) {
                m_line_index = 0;
                
                return NOT_EOF;
            }
        }
    
        do {
            String lineStr;
        
            if (null == (lineStr = m_input.readLine())) {
                m_eof_reached = true;
                m_line_index = 0;
                
                return EOF;
            }
            
            m_line = (lineStr + "\n").toCharArray();
            m_line_read = m_line.length;
            
            ++m_line_number;
        
            // Check for empty lines and discard them.
            
            elem = 0;
            while (CUtility.isspace(m_line[elem])) {
                ++elem;
                
                if (elem == m_line_read) {
                    break;
                }
            }
        } while (elem >= m_line_read);
        
        m_line_index = 0;
        return NOT_EOF;
    }
}
