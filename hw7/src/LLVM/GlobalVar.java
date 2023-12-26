package LLVM;

import BackEnd.Register;
import BackEnd.assembly.*;

import java.util.ArrayList;

// 全局常量 全局变量 零维 一维 二维
// 全部化成一维数组
public class GlobalVar extends Value{
    //形如 [n * i32]
    private String targetType;
    private Boolean isArray;
    private ArrayList<Value> valueArrayList;
    //记录压成一维前的维数(0维变量size是0)
    private ArrayList<Integer> dims;
    // 全局常量的value是确定的
    //targetType -> [n x i32]
    public GlobalVar(LLVMType llvmType, String name, Boolean isArray, ArrayList<Value> valueArrayList, String targetType,ArrayList<Integer> dims) {
        super(llvmType, name);
        this.isArray = isArray;
        this.valueArrayList = valueArrayList;
        this.targetType = targetType;
        this.dims = dims;
    }
    
    public ArrayList<Integer> getDims() {
        return dims;
    }
    
    public ArrayList<Value> getValueArrayList() {
        return valueArrayList;
    }
    
    public void setValueArrayList(ArrayList<Value> valueArrayList) {
        this.valueArrayList = valueArrayList;
    }
    
    public String getTargetType() {
        return targetType;
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
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append(" = global ");
        if (valueArrayList.size() != 0) {
            if (!isArray) {
                sb.append("i32 ");
                sb.append(valueArrayList.get(0).getName());
            } else {
                int cnt = 0;
                //targetType -> [n * i32]
                sb.append(targetType);
                sb.append(" [");
                for (Value value : valueArrayList) {
                    sb.append("i32 ");
                    sb.append(value.getName());
                    cnt++;
                    if (cnt < valueArrayList.size()) {
                        sb.append(", ");
                    }
                }
                sb.append("]");
            }
        } else {
            if (!isArray) {
                sb.append("i32 0");
            } else {
                sb.append(targetType);
                sb.append(" zeroinitializer");
            }
        }
        return sb.toString();
    }
    
    public void toAssembly() {
        //0维变量
        if (!isArray) {
            //无初始值
            if (valueArrayList.size() == 0) {
                new Word(getName().substring(1),0);
            }
            //有初始值
            else {
                new Word(getName().substring(1),Integer.parseInt(valueArrayList.get(0).getName()));
            }
        }
        //数组
        else {
            //形如 [n * i32]
            StringBuilder sb = new StringBuilder();
            int i;
            for (i = 1; i < targetType.length(); i++) {
                if (!Character.isDigit(targetType.charAt(i))) {
                    break;
                }
                sb.append(targetType.charAt(i));
            }
            //无初始值
            new Space(getName().substring(1),Integer.parseInt(sb.toString())*4);
            //有初始值
            int offset = 0;
            for (Value value : valueArrayList) {
                int num = Integer.parseInt(value.getName());
                new LiAsm(Register.T0,num);
                new MemAsm(MemAsm.Op.SW,Register.T0,getName().substring(1),offset);
                offset += 4;
            }
        }
    }
}
