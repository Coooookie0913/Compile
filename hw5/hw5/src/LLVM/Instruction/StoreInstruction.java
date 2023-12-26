package LLVM.Instruction;

import LLVM.BasicBlock;
import LLVM.Instr;
import LLVM.LLVMType;
import LLVM.Value;

//store <ty> <value>, <ty>* <pointer>
public class StoreInstruction extends Instr {
    private Value from;
    private Value to;
    //private LLVMType targetType;
    private String targetType;
    public StoreInstruction(LLVMType llvmType,String name,BasicBlock parentBlock, Value from, Value to, String targetType) {
        super(llvmType, name, parentBlock);
        this.from = from;
        this.to = to;
        this.targetType = targetType;
        addValue(from);
        addValue(to);
    }
    
    public String toString() {
        return "store " + targetType.toString().toLowerCase() + " " + from.getName() + ", "
                + targetType.toString().toLowerCase() + "* " + to.getName();
    }
}
