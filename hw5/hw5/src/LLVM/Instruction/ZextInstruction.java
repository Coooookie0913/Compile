package LLVM.Instruction;

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
    
}
