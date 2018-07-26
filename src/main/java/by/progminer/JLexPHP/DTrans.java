package by.progminer.JLexPHP;

class DTrans {
    
    static final int F = -1;
    
    int dtrans[];
    Accept accept;
    int anchor;
    int label;
    
    DTrans(int label, Spec spec) {
        dtrans = new int[spec.dTransNCols];
        accept = null;
        anchor = Spec.NONE;
        this.label = label;
    }
}
