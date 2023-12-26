package LLVM.Instruction;

import LLVM.BasicBlock;
import LLVM.Instr;
import LLVM.LLVMType;
import LLVM.Value;

import java.util.Objects;

// 在 genIR 的时候全部化成一维数组
//<result> = getelementptr <ty>, <ty>* <ptrval>, {<ty> <index>}*
public class GetElementPtrInstruction extends Instr {
    //形如 [n * i32]
    private String targetType;
    //genIR 的时候就生成一个整数
    //private int offset;
    private Value offset;
    private Value pointer;
    public GetElementPtrInstruction(LLVMType llvmType, String name, BasicBlock parentBlock, Value pointer, String targetType, Value offset) {
        super(llvmType, name, parentBlock);
        //就没用 user value了
        this.targetType = targetType;
        this.offset = offset;
        this.pointer = pointer;
    }
    
    public String getTargetType() {
        return targetType;
    }
    
    public String toString() {
        //pointer
        //TODO 没搞明白
        //if (pointer instanceof LoadInstruction && ((LoadInstruction)pointer).getTargetType() == LLVMType.INT32_POINTER) {
            //return this.getName() + " = getelementptr " + targetType + ", " + targetType + "* " + pointer.getName() +
            //        ", i32 " + offset;
        //}
        //else {
            //return this.getName() + " = getelementptr " + targetType + ", " + targetType + "* " + pointer.getName() +
            //        ", i32 0, i32 " + offset;
        //}
        
        //Symbol symbol = IRBuilder.getInstance().getTableStack().getSymbol(pointer.getName());
        if (Objects.equals(targetType, "i32*")) {
            //return this.getName() + " = getelementptr " + targetType + ", " + targetType + "* " + pointer.getName() +
            //        ", i32 " + offset;
            return this.getName() + " = getelementptr " + "i32, i32* " + pointer.getName() +
                    ", i32 " + offset.getName();
        }
        else {
            return this.getName() + " = getelementptr " + targetType + ", " + targetType + "* " + pointer.getName() +
                    ", i32 0, i32 " + offset.getName();
        }
    }
}
