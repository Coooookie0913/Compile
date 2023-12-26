package LLVM.Instruction;

import BackEnd.MipsBuilder;
import BackEnd.Register;
import BackEnd.assembly.GlobalAsm;
import BackEnd.assembly.LaAsm;
import BackEnd.assembly.MemAsm;
import LLVM.*;

public class LoadInstruction extends Instr {
    private Value operand;
    private String targetType;
    public LoadInstruction(LLVMType llvmType, String name, BasicBlock parentBlock, Value operand, String type) {
        super(llvmType, name, parentBlock);
        this.operand = operand;
        this.targetType = type;
        addValue(operand);
    }
    
    public String toString() {
        return this.getName() + " = load " + targetType + ", " + targetType + "* " + operand.getName();
    }
    
    public String getTargetType() {
        return targetType;
    }
    
    public void toAssembly() {
        super.toAssembly();
        Register pointerReg = Register.K0;
        Register targetReg = MipsBuilder.getInstance().getRegByValue(this);
        if (targetReg == null) {
            targetReg = Register.K0;
        }
        
        //pointerReg
        if (operand instanceof GlobalVar) {
            //TODO ?
            new LaAsm(pointerReg,operand.getName().substring(1));
        } else if (MipsBuilder.getInstance().getRegByValue(operand) != null) {
            pointerReg = MipsBuilder.getInstance().getRegByValue(operand);
        } else {
//            //还未放入内存中的局部变量，但其 allocInstr 已经放入内存中
//            if (operand instanceof LocalVar && MipsBuilder.getInstance().getOffsetOfValue(operand.getName()) == Integer.MAX_VALUE) {
//                int offset = MipsBuilder.getInstance().getOffsetOfValue(((LocalVar) operand).getAlloca().getName());
//                //建立localVar和offset的映射
//                MipsBuilder.getInstance().addOffsetOfValue(operand.getName(),offset);
//            }
            int offset = MipsBuilder.getInstance().getOffsetOfValue(operand.getName());
            if (offset == Integer.MAX_VALUE) {
                MipsBuilder.getInstance().subCurOffset(4);
                offset = MipsBuilder.getInstance().getCurOffset();
                MipsBuilder.getInstance().addOffsetOfValue(operand.getName(),offset);
            }
            new MemAsm(MemAsm.Op.LW,pointerReg,Register.SP,offset);
        }
        //将 pointerReg 中保存的地址存到 targetReg 中
        new MemAsm(MemAsm.Op.LW,targetReg,pointerReg,0);
        //存 targetReg 到内存中
        if (MipsBuilder.getInstance().getRegByValue(this) == null) {
            MipsBuilder.getInstance().subCurOffset(4);
            int curOffset = MipsBuilder.getInstance().getCurOffset();
            MipsBuilder.getInstance().addOffsetOfValue(this.getName(),curOffset);
            new MemAsm(MemAsm.Op.SW,targetReg,Register.SP,curOffset);
        }
    }
}
