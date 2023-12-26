package BackEnd.assembly;

import BackEnd.MipsBuilder;

import javax.swing.*;

public class Assembly {
    public Assembly() {
        //如果是全局变量
        if (this instanceof GlobalAsm) {
            MipsBuilder.getInstance().addDataAsm(this);
        } else {
            MipsBuilder.getInstance().addTextAsm(this);
        }
    }
}
