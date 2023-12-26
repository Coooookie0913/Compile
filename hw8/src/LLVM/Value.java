package LLVM;

import java.util.ArrayList;
import java.util.Iterator;

public class Value {
    private ArrayList<Use> useList;
    private String name;
    private LLVMType llvmType;
    private String targetType;
    
    public Value(LLVMType llvmType, String name) {
        this.useList = new ArrayList<>();
        this.name = name;
        this.llvmType = llvmType;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public LLVMType getLlvmType() {
        return llvmType;
    }
    
    public ArrayList<Use> getUseList() {
        return useList;
    }
    
    public void addUse(User user) {
        Use use = new Use(user,this);
        useList.add(use);
    }
    
    public void delUser(User user) {
        Iterator<Use> iterator = useList.iterator();
        while (iterator.hasNext()) {
            Use use = iterator.next();
            if (use.getUser() == user) {
                iterator.remove();
            }
        }
    }
    
    public String getTargetType() {
        return targetType;
    }
    
    public void toAssembly() {
    
    }
}
