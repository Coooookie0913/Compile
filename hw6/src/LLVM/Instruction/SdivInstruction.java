package LLVM.Instruction;

import BackEnd.MipsBuilder;
import BackEnd.Register;
import BackEnd.assembly.HiLoAsm;
import BackEnd.assembly.LiAsm;
import BackEnd.assembly.MDAsm;
import BackEnd.assembly.MemAsm;
import LLVM.*;

public class SdivInstruction extends Instr {
    private Value operand1;
    private Value operand2;
    public SdivInstruction(LLVMType llvmType, String name, BasicBlock parentBlock, Value operand1, Value operand2) {
        super(llvmType, name, parentBlock);
        this.operand1 = operand1;
        this.operand2 = operand2;
        addValue(operand1);
        addValue(operand2);
    }
    
    public String toString() {
        return this.getName() + " = sdiv i32 " + operand1.getName() + ", " + operand2.getName();
    }
    
    public void toAssembly() {
        super.toAssembly();
        Register reg1 = Register.K0;
        Register reg2 = Register.K1;
        Register targetReg = MipsBuilder.getInstance().getRegByValue(this);
        if (targetReg == null) {
            targetReg = Register.K0;
        }
        
        if (operand1 instanceof Constant) {
            new LiAsm(reg1,Integer.parseInt(operand1.getName()));
        } else if (MipsBuilder.getInstance().getRegByValue(operand1) != null) {
            reg1 = MipsBuilder.getInstance().getRegByValue(operand1);
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
        } else if (MipsBuilder.getInstance().getRegByValue(operand2) != null) {
            reg2 = MipsBuilder.getInstance().getRegByValue(operand2);
        } else {
            int offset2 = MipsBuilder.getInstance().getOffsetOfValue(operand2.getName());
            if (offset2 == Integer.MAX_VALUE) {
                MipsBuilder.getInstance().subCurOffset(4);
                offset2 = MipsBuilder.getInstance().getCurOffset();
                MipsBuilder.getInstance().addOffsetOfValue(operand2.getName(),offset2);
            }
            new MemAsm(MemAsm.Op.LW,reg2,Register.SP,offset2);
        }
        
        new MDAsm(MDAsm.Op.DIV,reg1,reg2);
        new HiLoAsm(HiLoAsm.Op.MFLO,targetReg);
        
        if (MipsBuilder.getInstance().getRegByValue(this) == null) {
            MipsBuilder.getInstance().subCurOffset(4);
            int curOffset = MipsBuilder.getInstance().getCurOffset();
            MipsBuilder.getInstance().addOffsetOfValue(this.getName(),curOffset);
            new MemAsm(MemAsm.Op.SW,targetReg,Register.SP,curOffset);
        }
    }
}
