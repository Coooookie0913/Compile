package LLVM;

import BackEnd.MipsBuilder;
import BackEnd.Register;
import BackEnd.assembly.LabelAsm;

import java.util.ArrayList;
import java.util.HashMap;

public class Function extends Value{
    private ArrayList<Param> paramArrayList;
    private ArrayList<BasicBlock> basicBlocks;
    private LLVMType returnType;
    //reg分配
    private HashMap<String, Register> var2Reg;
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
    
    public HashMap<String,Register> getVar2Reg() {
        return var2Reg;
    }
    //TODO
    public void setVar2Reg(HashMap<String,Register> var2Reg) {
        this.var2Reg = var2Reg;
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
    
    public void toAssembly() {
        new LabelAsm(getName().substring(1));
        MipsBuilder.getInstance().enterFunc(this);
        //建立形参和offset的映射关系，为前三个参数分配a1-a3寄存器，但仍在栈中为其预留空间
        int i;
        for (i = 0; i < paramArrayList.size(); i++) {
            //if (i < 3) {
            //    MipsBuilder.getInstance().allocRegForParam(paramArrayList.get(i),Register.getRegByIndex(Register.A0.ordinal() + i + 1));
            //}
            MipsBuilder.getInstance().subCurOffset(4);
            int curOffset = MipsBuilder.getInstance().getCurOffset();
            MipsBuilder.getInstance().addOffsetOfValue(paramArrayList.get(i).getName(),curOffset);
        }
        
        for (BasicBlock block : basicBlocks) {
            block.toAssembly();
        }
    }
}
