package LLVM.Instruction;

import BackEnd.MipsBuilder;
import BackEnd.Register;
import BackEnd.assembly.MemAsm;
import LLVM.BasicBlock;
import LLVM.Instr;
import LLVM.LLVMType;
import LLVM.Value;

public class ZextInstruction extends Instr {
    private Value targetValue;
    private LLVMType fromType;
    private LLVMType toType;
    public ZextInstruction(LLVMType llvmType, String name, BasicBlock parentBlock, Value targetValue, LLVMType fromType, LLVMType toType) {
        super(llvmType, name, parentBlock);
        this.targetValue = targetValue;
        this.fromType = fromType;
        this.toType = toType;
        addValue(targetValue);
    }
    
    public String toString() {
        return getName() + " = zext " + fromType.toString().toLowerCase() + " " +
                targetValue.getName() + " to " + toType.toString().toLowerCase();
    }
    
    //其实就是改变映射
    public void toAssembly() {
        if (MipsBuilder.getInstance().getRegByValue(targetValue) != null) {
            Register targetReg = MipsBuilder.getInstance().getRegByValue(targetValue);
            MipsBuilder.getInstance().subCurOffset(4);
            int curOffset = MipsBuilder.getInstance().getCurOffset();
            MipsBuilder.getInstance().addOffsetOfValue(this.getName(),curOffset);
            new MemAsm(MemAsm.Op.SW,targetReg,Register.SP,curOffset);
        }
        else {
            int targetOffset = MipsBuilder.getInstance().getOffsetOfValue(targetValue.getName());
            MipsBuilder.getInstance().addOffsetOfValue(this.getName(),targetOffset);
        }
    }
}
