package BackEnd.assembly;

import BackEnd.Register;

public class CmpAsm extends Assembly{
    public enum Op {
        //<
        SLT,
        //<=
        SLE,
        //>
        SGT,
        //>=
        SGE,
        //==
        SEQ,
        //!=
        SNE
    }
    
    private Op op;
    private Register rd;
    private Register rs;
    private Register rt;
    
    public CmpAsm(Op op,Register rd,Register rs,Register rt) {
        this.op = op;
        this.rd = rd;
        this.rs = rs;
        this.rt = rt;
    }
    
    public String toString() {
        return op.toString().toLowerCase() + " " + rd + " " + rs + " " + rt;
    }
}
