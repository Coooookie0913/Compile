package BackEnd.Optimize;

import BackEnd.AssemblyTable;
import BackEnd.Register;
import BackEnd.assembly.*;

import java.lang.management.MemoryNotificationInfo;
import java.util.ArrayList;
import java.util.Objects;

public class PeepholeOptimize {
    ArrayList<Assembly> retTextSegment;
    public PeepholeOptimize(ArrayList<Assembly> textSegment) {
        ArrayList<Assembly> text1 = delLwAfterSw(textSegment);
        ArrayList<Assembly> text2 = addSubZero(text1);
        ArrayList<Assembly> text3 = moveSameDst(text2);
        ArrayList<Assembly> text4 = moveOverlap(text3);
        retTextSegment = text4;
    }
    
    //Instr.java 里去掉CommentAsm方便优化
    
    //优化连续的两个对同一地址的sw lw
    public ArrayList<Assembly> delLwAfterSw(ArrayList<Assembly> textSegment) {
        ArrayList<Assembly> instructions = new ArrayList<>();
        int i;
        int length = textSegment.size();
        for (i = 0; i < length; i++) {
            Assembly cur = textSegment.get(i);
            if (i == length - 1) {
                instructions.add(cur);
                break;
            }
            Assembly next = textSegment.get(i + 1);
            if (cur instanceof MemAsm && next instanceof MemAsm && Objects.equals(((MemAsm) cur).getMemAddr(), ((MemAsm) next).getMemAddr())
                    && ((MemAsm) cur).getOp() == MemAsm.Op.SW && ((MemAsm) next).getOp() == MemAsm.Op.LW) {
                i++;
                instructions.add(cur);
                if (((MemAsm) cur).getReg() != ((MemAsm) next).getReg()) {
                    MoveAsm moveAsm = new MoveAsm(((MemAsm) next).getReg(),((MemAsm) cur).getReg());
                    instructions.add(moveAsm);
                }
            } else {
                instructions.add(cur);
            }
        }
        return instructions;
    }
    
    public ArrayList<Assembly> moveSameDst(ArrayList<Assembly> textSegment) {
        ArrayList<Assembly> instructions = new ArrayList<>();
        int i;
        int length = textSegment.size();
        for (i = 0; i < length; i++) {
            Assembly cur = textSegment.get(i);
            if (cur instanceof MoveAsm && (((MoveAsm) cur).getDst() == ((MoveAsm) cur).getSrc())) {
                //
            }
            else {
                instructions.add(cur);
            }
        }
        return instructions;
    }
    
    public ArrayList<Assembly> moveOverlap(ArrayList<Assembly> textSegment) {
        ArrayList<Assembly> instructions = new ArrayList<>();
        int i;
        int length = textSegment.size();
        for (i = 0; i < length; i++) {
            Assembly cur = textSegment.get(i);
            if (i == length - 1) {
                instructions.add(cur);
                break;
            }
            Assembly next = textSegment.get(i + 1);
            if (cur instanceof MoveAsm && next instanceof MoveAsm && (((MoveAsm) cur).getDst() == ((MoveAsm) next).getDst()) && (!(((MoveAsm) next).getSrc() != ((MoveAsm) cur).getDst()))) {
                //skip cur
            }
            else {
                instructions.add(cur);
            }
        }
        return instructions;
    }
    
    //优化操作数为0的add和sub
    //addu rd,rs,rt
    //add r0,r0,0 -> null
    //add r0,r1,$0 -> move r0,r1
    public ArrayList<Assembly> addSubZero(ArrayList<Assembly> textSegment) {
        ArrayList<Assembly> instructions = new ArrayList<>();
        int i;
        int length = textSegment.size();
        //第0条指令别忘了加
        instructions.add(textSegment.get(0));
        for (i = 1; i < length; i++) {
            Assembly cur = textSegment.get(i);
            Assembly pre = textSegment.get(i-1);
            if (cur instanceof AluAsm && (((AluAsm) cur).getOp() == AluAsm.Op.ADDI || ((AluAsm) cur).getOp() == AluAsm.Op.ADDU || ((AluAsm) cur).getOp() == AluAsm.Op.SUBU) ) {
                AluAsm.Op op = ((AluAsm) cur).getOp();
                //addi 0
                if (op == AluAsm.Op.ADDI) {
                    if (((AluAsm) cur).getImm() == 0) {
                        //如果rs与rd不相等
                        if (((AluAsm) cur).getRs() != ((AluAsm) cur).getRd()) {
                            MoveAsm moveAsm = new MoveAsm(((AluAsm) cur).getRd(),((AluAsm) cur).getRs());
                            instructions.add(moveAsm);
                        }
                        //如果相等 skip cur
                    }
                    else {
                        instructions.add(cur);
                    }
                }
                if (op == AluAsm.Op.ADDU || op == AluAsm.Op.SUBU) {
                    if (((AluAsm) cur).getRt() == Register.ZERO ||
                            (pre instanceof LiAsm && (((LiAsm) pre).getImm() == 0) && ((AluAsm) cur).getRt() == ((LiAsm) pre).getRd())) {
                        if (((AluAsm) cur).getRs() != ((AluAsm) cur).getRd()) {
                            MoveAsm moveAsm = new MoveAsm(((AluAsm) cur).getRd(),((AluAsm) cur).getRs());
                            instructions.add(moveAsm);
                        }
                        // li r2,0也可以去掉
                        if (pre instanceof LiAsm && (((LiAsm) pre).getImm() == 0) && ((AluAsm) cur).getRt() == ((LiAsm) pre).getRd()) {
                            instructions.remove(pre);
                        }
                    }
                    else if (((AluAsm) cur).getRs() == Register.ZERO ||
                            (pre instanceof LiAsm && (((LiAsm) pre).getImm() == 0) && ((AluAsm) cur).getRs() == ((LiAsm) pre).getRd())) {
                        if (((AluAsm) cur).getRt() != ((AluAsm) cur).getRd()) {
                            MoveAsm moveAsm = new MoveAsm(((AluAsm) cur).getRd(),((AluAsm) cur).getRt());
                            instructions.add(moveAsm);
                        }
                        if (pre instanceof LiAsm && (((LiAsm) pre).getImm() == 0) && ((AluAsm) cur).getRs() == ((LiAsm) pre).getRd()) {
                            instructions.remove(pre);
                        }
                    }
                    else {
                        instructions.add(cur);
                    }
                }
            }
            else {
                instructions.add(cur);
            }
        }
        return instructions;
    }
    
    public ArrayList<Assembly> getRetTextSegment() {
        return retTextSegment;
    }
}
