package BackEnd.Optimize;

import BackEnd.Register;
import BackEnd.assembly.*;

import java.util.ArrayList;

public class MulDivOptimize {
    ArrayList<Assembly> retTextSegment;
    Register mulOperand1 = null;
    Register mulOperand2 = null;
    public MulDivOptimize(ArrayList<Assembly> textSegment) {
        ArrayList<Assembly> text1 = MulOptimize(textSegment);
        //ArrayList<Assembly> text2 = DivOptimize(text1);
        retTextSegment = text1;
    }
    
    //mult rs,rt
    public ArrayList<Assembly> MulOptimize(ArrayList<Assembly> textSegment) {
        ArrayList<Assembly> instructions = new ArrayList<>();
        int i;
        int length = textSegment.size();
        instructions.add(textSegment.get(0));
        
        for (i = 1; i < length; i++) {
            Assembly cur = textSegment.get(i);
            Assembly pre = textSegment.get(i-1);
            if (cur instanceof MDAsm && (((MDAsm) cur).getOp() == MDAsm.Op.MULT) && pre instanceof LiAsm && (((LiAsm) pre).getRd() == ((MDAsm) cur).getRt())) {
                int imm = ((LiAsm) pre).getImm();
                //2的幂次
                int powerFlag = 0;
                int j;
                for (j = 1; j < 32; j++) {
                    int power = (int) Math.pow(2,j);
                    //imm = 2^j
                    if (imm == power) {
                        powerFlag = 1;
                        ShiftAsm shiftAsm = new ShiftAsm(ShiftAsm.Op.SLL, ((MDAsm) cur).getTarget(), ((MDAsm) cur).getRs(),j);
                        instructions.add(shiftAsm);
                        //skip li
                        instructions.remove(pre);
                        //skip mflo
                        i++;
                        break;
                    }
                }
                //乘其他常数
                if (powerFlag == 0) {
                    if (imm == 0) {
                        MoveAsm moveAsm = new MoveAsm(((MDAsm) cur).getTarget(),Register.ZERO);
                        instructions.add(moveAsm);
                        //skip mflo
                        i++;
                    }
                    else if (imm == 1) {
                        //skip li
                        instructions.remove(pre);
                        //skip cur & mflo
                        i++;
                    }
                    //imm <= 5优化才有意义
                    else if (imm <= 5) {
                        int k;
                        for (k = 0; k < imm-1; k++) {
                            AluAsm aluAsm;
                            if (((MDAsm) cur).getTarget() != ((MDAsm) cur).getRs()) {
                                if (k == 0) {
                                    aluAsm = new AluAsm(AluAsm.Op.ADDU,((MDAsm) cur).getTarget(),((MDAsm) cur).getRs(),((MDAsm) cur).getRs());
                                }
                                else {
                                    aluAsm = new AluAsm(AluAsm.Op.ADDU,((MDAsm) cur).getTarget(),((MDAsm) cur).getTarget(),((MDAsm) cur).getRs());
                                }
                            }
                            else {
                                if (k == 0) {
                                    aluAsm = new AluAsm(AluAsm.Op.ADDU,Register.T2,((MDAsm) cur).getRs(),((MDAsm) cur).getRs());
                                }
                                else if (k < imm - 2){
                                    aluAsm = new AluAsm(AluAsm.Op.ADDU,Register.T2,Register.T2,((MDAsm) cur).getRs());
                                }
                                else {
                                    aluAsm = new AluAsm(AluAsm.Op.ADDU,((MDAsm) cur).getTarget(),Register.T2,((MDAsm) cur).getRs());
                                }
                            }
                            instructions.add(aluAsm);
                        }
//                        HiLoAsm hiLoAsm = new HiLoAsm(HiLoAsm.Op.MTLO,Register.T2);
//                        instructions.add(hiLoAsm);
                        //skip li
                        instructions.remove(pre);
                        //skip mflo
                        i++;
                    }
                    else {
                        instructions.add(cur);
                    }
                }
            }
            else if (cur instanceof MDAsm && (((MDAsm) cur).getOp() == MDAsm.Op.MULT) && pre instanceof LiAsm && (((LiAsm) pre).getRd() == ((MDAsm) cur).getRs())) {
                int imm = ((LiAsm) pre).getImm();
                //2的幂次
                int powerFlag = 0;
                int j;
                for (j = 1; j < 32; j++) {
                    int power = (int) Math.pow(2,j);
                    //imm = 2^j
                    if (imm == power) {
                        powerFlag = 1;
                        ShiftAsm shiftAsm = new ShiftAsm(ShiftAsm.Op.SLL, ((MDAsm) cur).getTarget(), ((MDAsm) cur).getRt(),j);
                        instructions.add(shiftAsm);
//                        HiLoAsm hiLoAsm = new HiLoAsm(HiLoAsm.Op.MTLO,Register.K0);
//                        instructions.add(hiLoAsm)
                        //skip li
                        instructions.remove(pre);
                        //skip mflo
                        i++;
                    }
                }
                //乘其他常数
                if (powerFlag == 0) {
                    if (imm == 1) {
                        //skip li
                        instructions.remove(pre);
                        //skip cur & mflo
                        i++;
                    }
                    //imm < 5优化才有意义
                    else if (imm <= 5) {
                        int k;
                        for (k = 0; k < imm-1; k++) {
                            AluAsm aluAsm;
                            if (((MDAsm) cur).getTarget() != ((MDAsm) cur).getRt()) {
                                if (k == 0) {
                                    aluAsm = new AluAsm(AluAsm.Op.ADDU,((MDAsm) cur).getTarget(),((MDAsm) cur).getRt(),((MDAsm) cur).getRt());
                                }
                                else {
                                    aluAsm = new AluAsm(AluAsm.Op.ADDU,((MDAsm) cur).getTarget(),((MDAsm) cur).getTarget(),((MDAsm) cur).getRt());
                                }
                            }
                            else {
                                if (k == 0) {
                                    aluAsm = new AluAsm(AluAsm.Op.ADDU,Register.T2,((MDAsm) cur).getRt(),((MDAsm) cur).getRt());
                                }
                                else if (k < imm - 2){
                                    aluAsm = new AluAsm(AluAsm.Op.ADDU,Register.T2,Register.T2,((MDAsm) cur).getRt());
                                }
                                else {
                                    aluAsm = new AluAsm(AluAsm.Op.ADDU,((MDAsm) cur).getTarget(),Register.T2,((MDAsm) cur).getRt());
                                }
                            }
                            instructions.add(aluAsm);
                        }
//                        HiLoAsm hiLoAsm = new HiLoAsm(HiLoAsm.Op.MTLO,Register.T2);
//                        instructions.add(hiLoAsm);
                        //skip li
                        instructions.remove(pre);
                        //skip mflo
                        i++;
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
    
    public ArrayList<Assembly> DivOptimize(ArrayList<Assembly> textSegment) {
        ArrayList<Assembly> instructions = new ArrayList<>();
        int i;
        int length = textSegment.size();
        instructions.add(textSegment.get(0));
        for (i = 1; i < length; i++) {
            Assembly cur = textSegment.get(i);
            Assembly pre = textSegment.get(i-1);
            if (cur instanceof MDAsm && (((MDAsm) cur).getOp() == MDAsm.Op.DIV) && pre instanceof LiAsm && (((LiAsm) pre).getRd() == ((MDAsm) cur).getRt())) {
                int imm = ((LiAsm) pre).getImm();
                //2的幂次
                int powerFlag = 0;
                int j;
                for (j = 1; j < 32; j++) {
                    int power = (int) Math.pow(2,j);
                    //imm = 2^j
                    if (imm == power) {
                        powerFlag = 1;
                        ShiftAsm shiftAsm = new ShiftAsm(ShiftAsm.Op.SRL, ((MDAsm) cur).getTarget(), ((MDAsm) cur).getRs(),j);
                        instructions.add(shiftAsm);
                        //skip li
                        instructions.remove(pre);
                        //skip mflo
                        i++;
                        break;
                    }
                }
                //除其他常数
                if (powerFlag == 0) {
                    instructions.add(cur);
                }
            }
            else if (cur instanceof MDAsm && (((MDAsm) cur).getOp() == MDAsm.Op.DIV) && pre instanceof LiAsm && (((LiAsm) pre).getRd() == ((MDAsm) cur).getRs())) {
                int imm = ((LiAsm) pre).getImm();
                //2的幂次
                int powerFlag = 0;
                int j;
                for (j = 1; j < 32; j++) {
                    int power = (int) Math.pow(2, j);
                    //imm = 2^j
                    if (imm == power) {
                        powerFlag = 1;
                        ShiftAsm shiftAsm = new ShiftAsm(ShiftAsm.Op.SRL, ((MDAsm) cur).getTarget(), ((MDAsm) cur).getRt(), j);
                        instructions.add(shiftAsm);
                        //skip li
                        instructions.remove(pre);
                        //skip mflo
                        i++;
                        break;
                    }
                }
                //除其他常数
                if (powerFlag == 0) {
                    instructions.add(cur);
                }
            }
            else {
                instructions.add(cur);
            }
        }
        return instructions;
    }
    
//    //TODO 常量传播
//    public ArrayList<Assembly> sameMulExp(ArrayList<Assembly> textSegment) {
//        ArrayList<Assembly> instructions = new ArrayList<>();
//        int i;
//        int length = textSegment.size();
//        for (i = 0; i < length; i++) {
//            Assembly cur = textSegment.get(0);
//            if (cur instanceof MDAsm && (((MDAsm) cur).getOp() == MDAsm.Op.MULT)) {
//                if (mulOperand1 == null) {
//                    mulOperand1 = ((MDAsm) cur).getRs();
//                }
//            }
//        }
//    }
    
    public ArrayList<Assembly> getRetTextSegment() {
        return retTextSegment;
    }
}
