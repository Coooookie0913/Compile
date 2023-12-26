package BackEnd.assembly;

import BackEnd.Register;

public class MDAsm extends Assembly{
    public enum Op {
        MULT,
        DIV
    }
    
    private Op op;
    private Register rs;
    private Register rt;
    
    public MDAsm(Op op,Register rs,Register rt) {
        this.op = op;
        this.rs = rs;
        this.rt = rt;
    }
    
    public String toString() {
        return op.toString().toLowerCase() + " " + rs + " " + rt;
    }
}
