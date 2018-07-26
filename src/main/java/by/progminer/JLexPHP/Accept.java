package by.progminer.JLexPHP;

class Accept {
    
    char action[];
    int actionLength;
    int lineNumber;
    
    Accept(char action[], int actionLength, int lineNumber) {
        this.actionLength = actionLength;
        
        this.action = new char[actionLength];
        System.arraycopy(action, 0, this.action, 0, actionLength);
    
        this.lineNumber = lineNumber;
    }
    
    Accept(Accept accept) {
        this(accept.action, accept.actionLength, accept.lineNumber);
    }
    
    void mimic(Accept accept) {
        actionLength = accept.actionLength;
        
        action = new char[actionLength];
        System.arraycopy(accept.action, 0, action, 0, actionLength);
    }
}
