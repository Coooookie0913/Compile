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
    
    private Register target;
    
    public MDAsm(Op op,Register rs,Register rt,Register target) {
        this.op = op;
        this.rs = rs;
        this.rt = rt;
        this.target = target;
    }
    
    public Op getOp() {
        return op;
    }
    
    public Register getRs() {
        return rs;
    }
    
    public Register getRt() {
        return rt;
    }
    
    public Register getTarget() {
        return target;
    }
    
    public String toString() {
        return op.toString().toLowerCase() + " " + rs + " " + rt;
    }
}
