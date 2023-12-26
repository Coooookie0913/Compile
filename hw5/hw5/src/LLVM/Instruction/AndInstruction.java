package LLVM.Instruction;

import LLVM.BasicBlock;
import LLVM.Instr;
import LLVM.LLVMType;
import LLVM.Value;

public class AndInstruction extends Instr {
    private Value operand1;
    private Value operand2;
    public AndInstruction(LLVMType llvmType, String name, BasicBlock parentBlock, Value operand1, Value operand2) {
        super(llvmType, name, parentBlock);
        this.operand1 = operand1;
        this.operand2 = operand2;
        addValue(operand1);
        addValue(operand2);
    }
    
    public String toString() {
        return this.getName() + " = and i32 " + operand1.getName() + ", " + operand2.getName();
    }
}
