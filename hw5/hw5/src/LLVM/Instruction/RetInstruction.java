package LLVM.Instruction;

import LLVM.BasicBlock;
import LLVM.Instr;
import LLVM.LLVMType;
import LLVM.Value;

public class RetInstruction extends Instr {
    private LLVMType returnType;
    private Value returnValue;
    //ret void 的话 returnValue 是 null
    public RetInstruction(LLVMType llvmType, String name, BasicBlock parentBlock, LLVMType returnType, Value returnValue) {
        super(llvmType, name, parentBlock);
        this.returnType = returnType;
        this.returnValue = returnValue;
        addValue(returnValue);
    }
    
    public String toString() {
        if (returnType != LLVMType.VOID) {
            return "ret " + returnType.toString().toLowerCase() + " " + returnValue.getName();
        } else {
            return "ret void";
        }
    }
}
