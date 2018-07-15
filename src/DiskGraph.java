import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.util.Collection;
import java.util.HashSet;

public class DiskGraph extends JFrame {

    private JPanel panel1;
    private JScrollPane scrollPane;
    private JPanel paintPanel;
    private JTextArea textArea1;

    int line;

    HashSet<Integer> emptyBlocks = new HashSet<>();

    public DiskGraph() {
        for(java.util.List<Integer> list : Main.disk.freeBlock.stacks.values()) {
            for(int i=1; i<list.size(); i++) {
                emptyBlocks.add(list.get(i));
            }
        }

        setTitle("磁盘空间分配");
        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String info = String.format("磁盘大小: %d KB  已用空间: %d KB  剩余空间: %d KB\n磁盘块数: %d  Inode预留块数: %d  成组链接法占用块数: %d  文件内容占用块数: %d  空闲块数: %d\n",
                Main.disk.nBlock*Main.disk.blockSize, (Main.disk.nBlock-emptyBlocks.size())*Main.disk.blockSize, emptyBlocks.size()*Main.disk.blockSize,
                Main.disk.nBlock, Main.disk.nInodeBlock, Main.disk.freeBlock.usedForGroup.size(), Main.disk.nBlock-Main.disk.nInodeBlock-Main.disk.freeBlock.usedForGroup.size()-emptyBlocks.size(), emptyBlocks.size());
        textArea1.setText(info);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        paintPanel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                int nblock = Main.disk.nBlock, ninodeBlock = Main.disk.nInodeBlock;
                int inodePBlock = Main.disk.blockSize / Main.disk.inodeSize, ninode = Main.disk.inodes.size();
                int usedInodeBlock = ninode/inodePBlock;
                if(ninode%inodePBlock!=0) usedInodeBlock++;
                int templine = line;
                line = 0;
                int col = 0;
                for(int i=0; i<nblock; i++) {
                    if(i<usedInodeBlock) { // 已用inode
                        g.setColor(Color.red);
                    }
                    else if(i<ninodeBlock) { // 未用inode
                        g.setColor(Color.yellow);
                    }
                    else if(Main.disk.freeBlock.usedForGroup.contains(i)) { // 存储空闲块组
                        g.setColor(Color.blue);
                    }
                    else if(emptyBlocks.contains(i)) { // 空闲块
                        g.setColor(Color.gray);
                    }
                    else { // 文件块
                        g.setColor(Color.green);
                    }
                    g.fillRect(col*20, line*20, 20,20);
                    g.setColor(Color.black);
                    g.drawRect(col*20, line*20, 20,20);

                    if(++col>=50) {
                        col = 0;
                        ++line;
                    }
                }
                for(int i=0; i<nblock; i+=50) {
                    g.drawString(String.valueOf(i), 0, i/50*20+20);
                }
                if(line != templine) {
                    setPreferredSize(new Dimension(1000,(line+1)*20));
                    updateUI();
                }
            }
        };
    }
}
