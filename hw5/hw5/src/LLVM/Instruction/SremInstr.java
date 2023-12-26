package LLVM.Instruction;

import LLVM.BasicBlock;
import LLVM.Instr;
import LLVM.LLVMType;
import LLVM.Value;

public class SremInstr extends Instr {
    private Value operand1;
    private Value operand2;
    
    public SremInstr(LLVMType llvmType, String name, BasicBlock parentBlock, Value operand1, Value operand2) {
        super(llvmType, name, parentBlock);
        this.operand1 = operand1;
        this.operand2 = operand2;
    }
    
    public String toString () {
        return this.getName() + " = srem i32 " + operand1.getName() + ", " + operand2.getName();
    }
}
