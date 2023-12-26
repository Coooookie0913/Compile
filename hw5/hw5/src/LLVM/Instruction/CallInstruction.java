package LLVM.Instruction;

import LLVM.*;

import java.util.ArrayList;
//call void @a1(i32 %9)
//%14 = call i32 @a2(i32 %12, i32* %13)
public class CallInstruction extends Instr {
    private Function function;
    private ArrayList<Value> paramArrayList;
    public CallInstruction(LLVMType llvmType, String name, BasicBlock parentBlock, Function function, ArrayList<Value> paramArrayList) {
        super(llvmType, name, parentBlock);
        this.function = function;
        this.paramArrayList = paramArrayList;
        addValue(function);
        for (Value param : paramArrayList) {
            addValue(param);
        }
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (function.getReturnType() == LLVMType.VOID) {
            sb.append("call void ");
        } else {
            sb.append(getName());
            sb.append(" = call i32 ");
        }
        sb.append(function.getName());
        sb.append("(");
        int cnt = 0;
        int i;
        for (i = 0; i < paramArrayList.size(); i++) {
            Value param = paramArrayList.get(i);
            //TODO
            //sb.append("i32");
            //用函数定义信息判断
            if (function.getParamArrayList().size() > 0 && ((Param)function.getParamArrayList().get(i)).getParamDims().size() > 0) {
                sb.append("i32*");
            }
            else {
                sb.append("i32");
            }
            sb.append(" ");
            sb.append(param.getName());
            cnt++;
            if (cnt < paramArrayList.size()) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }
}
