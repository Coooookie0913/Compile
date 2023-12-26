package LLVM.Instruction;

import LLVM.BasicBlock;
import LLVM.Instr;
import LLVM.LLVMType;

//<result> = alloca <type>
public class AllocaInstruction extends Instr {
    private String targetType;
    public AllocaInstruction(LLVMType llvmType, String name, BasicBlock parentBlock, String targetType) {
        super(llvmType, name, parentBlock);
        this.targetType = targetType;
    }
    
    public String toString() {
        return this.getName() + " = alloca " + targetType;
    }
    
    @Override
    public String getTargetType() {
        return targetType;
    }
}
