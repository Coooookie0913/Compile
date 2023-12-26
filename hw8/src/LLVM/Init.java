package LLVM;

import java.util.ArrayList;

public class Init extends Value{
    private ArrayList<Value> initValList;
    private boolean isArray;
    public Init(LLVMType llvmType, String name, ArrayList<Value> initValList,boolean isArray) {
        super(llvmType, name);
        this.initValList = initValList;
        this.isArray = isArray;
        setInitName();
    }
    
    public ArrayList<Value> getInitValList() {
        return initValList;
    }
    
    public void setInitName() {
        StringBuilder sb = new StringBuilder();
        if (!isArray) {
            setName(initValList.get(0).getName());
        }
        //数组
        else {
            sb.append("[");
            int cnt = 0;
            for (Value value : initValList) {
                sb.append("i32 ");
                sb.append(value.getName());
                cnt++;
                if (cnt < initValList.size()) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            setName(sb.toString());
        }
    }
}
