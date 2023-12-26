package BackEnd.assembly;

import BackEnd.Register;
import LLVM.Instr;

public class AluAsm extends Assembly{
    public enum Op {
        //calR
        ADDU,
        SUBU,
        AND,
        OR,
        //calI
        ADDI,
        //shift
        SLL
    }
    
    private Op op;
    private Register rd;
    private Register rs;
    private Register rt;
    private Integer imm;
    private int type;
    
    //R
    public AluAsm(Op op,Register rd,Register rs,Register rt) {
        this.op = op;
        this.rd = rd;
        this.rs = rs;
        this.rt = rt;
        this.imm = null;
        type = 0;
    }
    //I
    public AluAsm(Op op,Register rd,Register rs, int imm) {
        this.op = op;
        this.rd = rd;
        this.rs = rs;
        this.rt = null;
        this.imm = imm;
        type = 1;
    }
    
    public Op getOp() {
        return op;
    }
    
    public Register getRs() {
        return rs;
    }
    
    public Register getRd() {
        return rd;
    }
    
    public Register getRt() {
        return rt;
    }
    
    public int getImm() {
        return imm;
    }
    
    public int getType() {
        return type;
    }
    
    public String toString() {
        if (type == 0) {
            return op.toString().toLowerCase() + " " + rd + " " + rs + " " + rt;
        } else {
            return op.toString().toLowerCase() + " " + rd + " " + rs + " " + imm;
        }
    }
}
