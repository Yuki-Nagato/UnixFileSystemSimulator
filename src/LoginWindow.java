import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LoginWindow extends JFrame {
    private JTextField userText;
    private JPanel panel1;
    private JButton 登录Button;
    private JTextField groupText;
    private JButton 磁盘空间Button;
    private JButton 格式化磁盘Button;

    public LoginWindow() {
        登录Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new FileSystemWindow(userText.getText(), groupText.getText());
            }
        });
        // action listener finished
        setTitle("用户登录");
        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Main.save();
            }
        });
        磁盘空间Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new DiskGraph();
            }
        });
        格式化磁盘Button.addActionListener(e -> {
            FormatDialog dialog = new FormatDialog(this);
            dialog.setVisible(true);
            if(!dialog.accepted) return;
            try {
                Main.disk = new Disk(dialog.getDiskSize()/dialog.getBlockSize(),dialog.getInodeSize()/dialog.getBlockSize(),dialog.getBlockSize(),1);
            }
            catch (SpaceNotEnoughException e1) {
                e1.printStackTrace();
            }
            catch (InodeOverflowException e1) {
                e1.printStackTrace();
            }
        });
    }
}
