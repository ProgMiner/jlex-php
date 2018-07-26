package by.progminer.JLexPHP.Math;

import by.progminer.JLexPHP.Spec;

public class DTrans {
    
    public static final int F = -1;
    
    public int dtrans[];
    public Accept accept;
    public int anchor;
    public int label;
    
    public DTrans(int label, Spec spec) {
        dtrans = new int[spec.dTransNCols];
        accept = null;
        anchor = Spec.NONE;
        this.label = label;
    }
}
