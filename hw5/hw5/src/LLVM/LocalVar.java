package LLVM;

import java.util.ArrayList;

//局部常量 局部变量 变量 数组（在genIR的时候就全部化成一维数组）
public class LocalVar extends Value{
    //形如 [n * i32]
    private String targetType;
    private Boolean isArray;
    private ArrayList<Value> valueArrayList;
    //记录压成一维前的 dims（0维变量size是0）
    private ArrayList<Integer> dims;
    public LocalVar(LLVMType llvmType, String name, Boolean isArray, ArrayList<Value> valueArrayList,String targetType,ArrayList<Integer> dims) {
        super(llvmType, name);
        this.isArray = isArray;
        this.targetType = targetType;
        this.valueArrayList = valueArrayList;
        this.dims = dims;
    }
    
    public void setValueArrayList(ArrayList<Value> valueArrayList) {
        this.valueArrayList = valueArrayList;
    }
    
    public ArrayList<Integer> getDims() {
        return dims;
    }
    
    public Value getConstInteger(ArrayList<Integer> offDims) {
        int i,j;
        int offset = 0;
        for (i = 0; i < offDims.size(); i++) {
            int subOffset = offDims.get(i);
            for (j = i + 1; j < dims.size(); j++) {
                subOffset *= dims.get(j);
            }
            offset += subOffset;
        }
        return valueArrayList.get(offset);
    }
    
    @Override
    public String getTargetType() {
        return targetType;
    }
    
    public ArrayList<Value> getValueArrayList() {
        return valueArrayList;
    }
    
    //public String toString() {
    
    //}
}
