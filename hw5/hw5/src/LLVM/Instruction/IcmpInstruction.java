package LLVM.Instruction;

import LLVM.BasicBlock;
import LLVM.Instr;
import LLVM.LLVMType;
import LLVM.Value;

//%4 = icmp ne i32 %3, 0
public class IcmpInstruction extends Instr {
    private Value operand1;
    private Value operand2;
    
    private IcmpOp op;
    
    public IcmpInstruction(LLVMType llvmType, String name, BasicBlock parentBlock, Value operand1, Value operand2, IcmpOp op) {
        super(llvmType, name, parentBlock);
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.op = op;
        addValue(operand1);
        addValue(operand2);
    }
    
    public String toString() {
        return this.getName() + " = icmp " + op.toString().toLowerCase() + " i32 " + operand1.getName() + ", " + operand2.getName();
    }
}
