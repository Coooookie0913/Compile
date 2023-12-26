package BackEnd;

import BackEnd.Optimize.MulDivOptimize;
import BackEnd.Optimize.PeepholeOptimize;
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
    
    public ArrayList<Assembly> getTextSegment() {
        return textSegment;
    }
    
    public void setTextSegment(ArrayList<Assembly> textSegment1) {
        textSegment = textSegment1;
    }
    
    public void openPeepholeOptimize() {
        PeepholeOptimize peepholeOptimize = new PeepholeOptimize(textSegment);
        textSegment = peepholeOptimize.getRetTextSegment();
    }
    
    public void openMulDivOptimize() {
        MulDivOptimize mulDivOptimize = new MulDivOptimize(textSegment);
        textSegment = mulDivOptimize.getRetTextSegment();
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        openPeepholeOptimize();
        openMulDivOptimize();
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
