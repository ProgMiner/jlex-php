package by.progminer.JLexPHP;

class DTrans {
    
    static final int F = -1;
    
    int dtrans[];
    Accept accept;
    int anchor;
    int label;
    
    DTrans(int label, CSpec spec) {
        dtrans = new int[spec.m_dtrans_ncols];
        accept = null;
        anchor = CSpec.NONE;
        this.label = label;
    }
}
