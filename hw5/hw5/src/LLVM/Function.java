package LLVM;

import java.util.ArrayList;

public class Function extends Value{
    private ArrayList<Param> paramArrayList;
    private ArrayList<BasicBlock> basicBlocks;
    private LLVMType returnType;
    public Function(LLVMType llvmType, String name,LLVMType returnType) {
        super(llvmType, name);
        this.returnType = returnType;
        this.paramArrayList = new ArrayList<>();
        this.basicBlocks = new ArrayList<>();
    }
    
    public void addParam(Param param) {
        paramArrayList.add(param);
    }
    
    public void addBasicBlocks(BasicBlock basicBlock) {
        basicBlocks.add(basicBlock);
    }
    
    public ArrayList<Param> getParamArrayList() {
        return paramArrayList;
    }
    
    public ArrayList<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }
    
    public LLVMType getReturnType() {
        return returnType;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("define dso_local ");
        sb.append(returnType.toString().toLowerCase());
        sb.append(" ");
        sb.append(getName());
        sb.append("(");
        int cnt = 0;
        for (Param param : paramArrayList) {
            //sb.append(param.toString());
            sb.append(param.getTargetType());
            sb.append(" ");
            sb.append(param.getName());
            cnt++;
            if (cnt < paramArrayList.size()) {
                sb.append(", ");
            }
        }
        sb.append(") {\n");
        for (BasicBlock basicBlock : basicBlocks) {
            sb.append(basicBlock.toString());
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
