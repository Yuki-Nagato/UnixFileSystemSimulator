import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PermissionDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JCheckBox ur;
    private JCheckBox uw;
    private JCheckBox ux;
    private JCheckBox gr;
    private JCheckBox gw;
    private JCheckBox gx;
    private JCheckBox or;
    private JCheckBox ow;
    private JCheckBox ox;

    public boolean accepted = false;

    public PermissionDialog(Frame owner) {
        super(owner, "修改权限", true);
        setContentPane(contentPane);
        pack();
        setLocationRelativeTo(owner);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        accepted = true;
        setVisible(false);
    }

    private void onCancel() {
        accepted = false;
        setVisible(false);
    }

    public int getMode() {
        int rst = 0;
        if(ur.isSelected()) rst|=0400;
        if(uw.isSelected()) rst|=0200;
        if(ux.isSelected()) rst|=0100;
        if(gr.isSelected()) rst|=040;
        if(gw.isSelected()) rst|=020;
        if(gx.isSelected()) rst|=010;
        if(or.isSelected()) rst|=04;
        if(ow.isSelected()) rst|=02;
        if(ox.isSelected()) rst|=01;
        return rst;
    }

    public void setCheckBoxes(int mode) {
        ur.setSelected((mode&0400)!=0);
        uw.setSelected((mode&0200)!=0);
        ux.setSelected((mode&0100)!=0);
        gr.setSelected((mode&040)!=0);
        gw.setSelected((mode&020)!=0);
        gx.setSelected((mode&010)!=0);
        or.setSelected((mode&04)!=0);
        ow.setSelected((mode&02)!=0);
        ox.setSelected((mode&01)!=0);
    }


}
