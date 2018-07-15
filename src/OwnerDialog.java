import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class OwnerDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField userText;
    private JTextField groupText;

    public boolean accepted = false;

    public OwnerDialog(Frame owner) {
        super(owner,"修改所有者",true);
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

    public void setText(String user, String group) {
        userText.setText(user);
        groupText.setText(group);
    }

    public String getUser() {
        return userText.getText();
    }

    public String getGroup() {
        return groupText.getText();
    }
}
