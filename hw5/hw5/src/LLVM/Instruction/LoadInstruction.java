package LLVM.Instruction;

import LLVM.BasicBlock;
import LLVM.Instr;
import LLVM.LLVMType;
import LLVM.Value;

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
}
