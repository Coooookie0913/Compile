package BackEnd.assembly;

import BackEnd.Register;
import LLVM.Param;

public class BranchAsm extends Assembly{
    public enum Op {
        BEQ,
        BNE
    }
    
    private Op op;
    private Register rs;
    private Register rt;
    private String label;
    
    public BranchAsm(Op op,Register rs,Register rt,String label) {
        this.op = op;
        this.rs = rs;
        this.rt = rt;
        this.label = label;
    }
    
    public String toString() {
        return op.toString().toLowerCase() + " " + rs + " " + rt + " " + label;
    }
}
