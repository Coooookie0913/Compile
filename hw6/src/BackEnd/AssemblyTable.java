package BackEnd;

import BackEnd.assembly.Assembly;

import java.util.ArrayList;

public class AssemblyTable {
    private ArrayList<Assembly> dataSegment;
    private ArrayList<Assembly> textSegment;
    
    public AssemblyTable() {
        this.dataSegment = new ArrayList<>();
        this.textSegment = new ArrayList<>();
    }
    
    public void addDataAsm(Assembly assembly) {
        dataSegment.add(assembly);
    }
    
    public void addTextAsm(Assembly assembly) {
        textSegment.add(assembly);
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(".data\n");
        for (Assembly assembly:dataSegment) {
            sb.append(assembly.toString());
            sb.append("\n");
        }
        sb.append("\n\n.text\n");
        for (Assembly assembly:textSegment) {
            sb.append(assembly.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
