import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class FormatDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JSpinner blockSizeSpinner;
    private JSpinner diskSizeSpinner;
    private JSpinner inodeSizeSpinner;

    public boolean accepted = false;

    public FormatDialog(Frame owner) {
        super(owner, "格式化磁盘", true);
        blockSizeSpinner.setValue(4);
        diskSizeSpinner.setValue(40960);
        inodeSizeSpinner.setValue(4096);
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

    public int getBlockSize() {
        return (int) blockSizeSpinner.getValue();
    }

    public int getDiskSize() {
        return (int)diskSizeSpinner.getValue();
    }

    public int getInodeSize() {
        return  (int)inodeSizeSpinner.getValue();
    }

    private void onOK() {
        if(getDiskSize()%getBlockSize()!=0) {
            JOptionPane.showMessageDialog(this,"磁盘大小应为块大小的倍数","参数不合法",JOptionPane.ERROR_MESSAGE);
            return;
        }
        if(getInodeSize()%getBlockSize()!=0) {
            JOptionPane.showMessageDialog(this,"Inode空间大小应为块大小的倍数","参数不合法",JOptionPane.ERROR_MESSAGE);
            return;
        }
        accepted = true;
        setVisible(false);
    }

    private void onCancel() {
        accepted = false;
        setVisible(false);
    }
}
