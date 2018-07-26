package by.progminer.JLexPHP.Math;

public class Accept {
    
    public char action[];
    public int actionLength;
    public int lineNumber;
    
    public Accept(char action[], int actionLength, int lineNumber) {
        this.actionLength = actionLength;
        
        this.action = new char[actionLength];
        System.arraycopy(action, 0, this.action, 0, actionLength);
    
        this.lineNumber = lineNumber;
    }
    
    public Accept(Accept accept) {
        this(accept.action, accept.actionLength, accept.lineNumber);
    }
    
    public void mimic(Accept accept) {
        actionLength = accept.actionLength;
        
        action = new char[actionLength];
        System.arraycopy(accept.action, 0, action, 0, actionLength);
    }
}
