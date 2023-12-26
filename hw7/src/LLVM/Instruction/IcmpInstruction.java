package LLVM.Instruction;

import BackEnd.MipsBuilder;
import BackEnd.Register;
import BackEnd.assembly.AluAsm;
import BackEnd.assembly.CmpAsm;
import BackEnd.assembly.LiAsm;
import BackEnd.assembly.MemAsm;
import LLVM.*;

import java.awt.color.CMMException;

//%4 = icmp ne i32 %3, 0
public class IcmpInstruction extends Instr {
    private Value operand1;
    private Value operand2;
    
    private IcmpOp op;
    
    public IcmpInstruction(LLVMType llvmType, String name, BasicBlock parentBlock, Value operand1, Value operand2, IcmpOp op) {
        super(llvmType, name, parentBlock);
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.op = op;
        addValue(operand1);
        addValue(operand2);
    }
    
    public String toString() {
        return this.getName() + " = icmp " + op.toString().toLowerCase() + " i32 " + operand1.getName() + ", " + operand2.getName();
    }
    
    public void toAssembly() {
        super.toAssembly();
        Register reg1 = Register.K0;
        Register reg2 = Register.K1;
        Register targetReg = MipsBuilder.getInstance().getRegByValue(this.getName());
        if (targetReg == null) {
            targetReg = Register.K0;
        }
        
        if (operand1 instanceof Constant) {
            new LiAsm(reg1,Integer.parseInt(operand1.getName()));
        } else if (MipsBuilder.getInstance().getRegByValue(operand1.getName()) != null) {
            reg1 = MipsBuilder.getInstance().getRegByValue(operand1.getName());
        } else {
            int offset1 = MipsBuilder.getInstance().getOffsetOfValue(operand1.getName());
            if (offset1 == Integer.MAX_VALUE) {
                MipsBuilder.getInstance().subCurOffset(4);
                offset1 = MipsBuilder.getInstance().getCurOffset();
                MipsBuilder.getInstance().addOffsetOfValue(operand1.getName(),offset1);
            }
            new MemAsm(MemAsm.Op.LW,reg1,Register.SP,offset1);
        }
        
        if (operand2 instanceof Constant) {
            new LiAsm(reg2,Integer.parseInt(operand2.getName()));
        } else if (MipsBuilder.getInstance().getRegByValue(operand2.getName()) != null) {
            reg2 = MipsBuilder.getInstance().getRegByValue(operand2.getName());
        } else {
            int offset2 = MipsBuilder.getInstance().getOffsetOfValue(operand2.getName());
            if (offset2 == Integer.MAX_VALUE) {
                MipsBuilder.getInstance().subCurOffset(4);
                offset2 = MipsBuilder.getInstance().getCurOffset();
                MipsBuilder.getInstance().addOffsetOfValue(operand2.getName(),offset2);
            }
            new MemAsm(MemAsm.Op.LW,reg2,Register.SP,offset2);
        }
        
        if (op == IcmpOp.EQ) {
            new CmpAsm(CmpAsm.Op.SEQ,targetReg,reg1,reg2);
        } else if (op == IcmpOp.NE) {
            new CmpAsm(CmpAsm.Op.SNE,targetReg,reg1,reg2);
        } else if (op == IcmpOp.SGT) {
            new CmpAsm(CmpAsm.Op.SGT,targetReg,reg1,reg2);
        } else if (op == IcmpOp.SGE) {
            new CmpAsm(CmpAsm.Op.SGE,targetReg,reg1,reg2);
        } else if (op == IcmpOp.SLT) {
            new CmpAsm(CmpAsm.Op.SLT,targetReg,reg1,reg2);
        } else {
            new CmpAsm(CmpAsm.Op.SLE,targetReg,reg1,reg2);
        }
    
        if (MipsBuilder.getInstance().getRegByValue(this.getName()) == null) {
            MipsBuilder.getInstance().subCurOffset(4);
            int curOffset = MipsBuilder.getInstance().getCurOffset();
            MipsBuilder.getInstance().addOffsetOfValue(this.getName(),curOffset);
            new MemAsm(MemAsm.Op.SW,targetReg,Register.SP,curOffset);
        }
    }
}
