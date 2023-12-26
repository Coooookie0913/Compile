package LLVM.Instruction;

import LLVM.BasicBlock;
import LLVM.Instr;
import LLVM.LLVMType;
import LLVM.Value;

import java.util.ArrayList;
import java.util.HashMap;

public class PhiInstruction extends Instr {
    private HashMap<BasicBlock,Value> preBlockValue;
    public PhiInstruction(LLVMType llvmType, String name, BasicBlock parentBlock, ArrayList<BasicBlock> preBlocks) {
        super(llvmType, name, parentBlock);
        for (BasicBlock basicBlock : preBlocks) {
            this.preBlockValue.put(basicBlock,null);
        }
    }
    
    public void setPreBlockValue(BasicBlock basicBlock, Value value) {
        preBlockValue.replace(basicBlock,value);
        value.addUse(this);
    }
    
    //<result> = phi <ty> [ <val0>, <label0>], ...
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append(" = phi i32 ");
        int cnt = 0;
        for (BasicBlock basicBlock : preBlockValue.keySet()) {
            cnt++;
            sb.append("[ ");
            sb.append(basicBlock.getName());
            sb.append(", ");
            sb.append(preBlockValue.get(basicBlock).getName());
            sb.append("]");
            if (cnt < preBlockValue.size()) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
}
