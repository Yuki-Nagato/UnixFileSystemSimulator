import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;

public class FileSystemWindow extends JFrame {
    public String user, group;
    public FileTreeModel treeModel;
    private JPanel panel1;
    private JTree fileTree;
    private JButton 保存Button;
    private JButton 取消Button;
    private JButton 修改所有者Button;
    private JButton 修改权限Button;
    private JButton 查看详细Button;
    private JTextArea inodeTextArea;
    private JTextArea fileEditTextArea;

    public FileSystemWindow(String user, String group) {
        fileTree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F5) {
                    loadFiles();
                }
            }
        });

        fileTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = fileTree.getMinSelectionRow();
                    if (row == -1) {
                        JPopupMenu menu = new JPopupMenu();
                        JMenuItem 刷新 = new JMenuItem("刷新");
                        刷新.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                loadFiles();
                            }
                        });
                        menu.add(刷新);
                        menu.show(fileTree, e.getX(), e.getY());
                    }
                    else {
                        JPopupMenu menu = new JPopupMenu();
                        JMenuItem 新建文件 = new JMenuItem("新建文件");
                        新建文件.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                String name = JOptionPane.showInputDialog(panel1, "文件名");
                                if (name.equals(""))
                                    return;
                                File parent = (File) fileTree.getSelectionPath().getLastPathComponent();
                                if (parent.isFile())
                                    parent = parent.getParentFile();
                                File file = new File(parent, name);
                                try {
                                    Main.disk.createFile(new MyFile(file), user, group);
                                }
                                catch (InodeOverflowException e1) {
                                    JOptionPane.showMessageDialog(panel1,"没有剩余的Inode空间","Inode空间不足",JOptionPane.ERROR_MESSAGE);
                                    e1.printStackTrace();
                                }
                                catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                                catch (PermissionDeniedException e1) {
                                    JOptionPane.showMessageDialog(panel1, e1.getMessage(), "权限不足", JOptionPane.ERROR_MESSAGE);
                                    e1.printStackTrace();
                                }
                                loadFiles();
                            }

                        });
                        menu.add(新建文件);
                        JMenuItem 新建文件夹 = new JMenuItem("新建文件夹");
                        新建文件夹.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                String name = JOptionPane.showInputDialog(panel1, "文件夹名");
                                if (name==null || name.isEmpty())
                                    return;
                                File parent = (File) fileTree.getSelectionPath().getLastPathComponent();
                                if (parent.isFile())
                                    parent = parent.getParentFile();
                                File file = new File(parent, name);
                                try {
                                    Main.disk.createDirectory(new MyFile(file), user, group);
                                }
                                catch (InodeOverflowException e1) {
                                    JOptionPane.showMessageDialog(panel1,"没有剩余的Inode空间","Inode空间不足",JOptionPane.ERROR_MESSAGE);
                                    e1.printStackTrace();
                                }
                                catch (SpaceNotEnoughException e1) {
                                    JOptionPane.showMessageDialog(panel1, e1.getMessage(), "空间不足", JOptionPane.ERROR_MESSAGE);
                                    e1.printStackTrace();
                                }
                                catch (PermissionDeniedException e1) {
                                    JOptionPane.showMessageDialog(panel1, e1.getMessage(), "权限不足", JOptionPane.ERROR_MESSAGE);
                                    e1.printStackTrace();
                                }
                                loadFiles();
                            }

                        });
                        menu.add(新建文件夹);
                        if(row>0) {
                            JMenuItem 复制 = new JMenuItem("复制");
                            复制.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    File source = (File) fileTree.getSelectionPath().getLastPathComponent();
                                    JFileChooser dirChooser = new JFileChooser();
                                    dirChooser.setCurrentDirectory(source.getParentFile());
                                    dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                                    if (dirChooser.showDialog(panel1, "选择目录") != JFileChooser.APPROVE_OPTION)
                                        return;
                                    File dest = dirChooser.getSelectedFile();
                                    String newname = JOptionPane.showInputDialog("新名称", source.getName());
                                    if(newname==null || newname.isEmpty())
                                        return;
                                    try {
                                        Main.disk.copy(new MyFile(source), new MyFile(dest, newname));
                                    }
                                    catch (SpaceNotEnoughException e1) {
                                        JOptionPane.showMessageDialog(panel1, e1.getMessage(), "空间不足", JOptionPane.ERROR_MESSAGE);
                                        e1.printStackTrace();
                                    }
                                    catch (InodeOverflowException e1) {
                                        JOptionPane.showMessageDialog(panel1,"没有剩余的Inode空间","Inode空间不足",JOptionPane.ERROR_MESSAGE);
                                        e1.printStackTrace();
                                    }
                                    catch (IOException e1) {
                                        e1.printStackTrace();
                                    }
                                    catch (PermissionDeniedException e1) {
                                        JOptionPane.showMessageDialog(panel1, e1.getMessage(), "权限不足", JOptionPane.ERROR_MESSAGE);
                                        e1.printStackTrace();
                                    }
                                    loadFiles();
                                }

                            });
                            menu.add(复制);
                            JMenuItem 移动 = new JMenuItem("移动");
                            移动.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    File source = (File) fileTree.getSelectionPath().getLastPathComponent();
                                    fileTree.setSelectionRow(-1);
                                    JFileChooser dirChooser = new JFileChooser();
                                    dirChooser.setCurrentDirectory(source.getParentFile());
                                    dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                                    if (dirChooser.showDialog(panel1, "选择目录") != JFileChooser.APPROVE_OPTION)
                                        return;
                                    File dest = dirChooser.getSelectedFile();
                                    String newname = JOptionPane.showInputDialog("新名称", source.getName());
                                    if(newname==null || newname.isEmpty())
                                        return;
                                    try {
                                        Main.disk.move(new MyFile(source), new MyFile(dest, newname));
                                    }
                                    catch (SpaceNotEnoughException e1) {
                                        JOptionPane.showMessageDialog(panel1, e1.getMessage(), "空间不足", JOptionPane.ERROR_MESSAGE);
                                        e1.printStackTrace();
                                    }
                                    catch (InodeOverflowException e1) {
                                        JOptionPane.showMessageDialog(panel1,"没有剩余的Inode空间","Inode空间不足",JOptionPane.ERROR_MESSAGE);
                                        e1.printStackTrace();
                                    }
                                    catch (IOException e1) {
                                        e1.printStackTrace();
                                    }
                                    catch (PermissionDeniedException e1) {
                                        JOptionPane.showMessageDialog(panel1, e1.getMessage(), "权限不足", JOptionPane.ERROR_MESSAGE);
                                        e1.printStackTrace();
                                    }
                                    loadFiles();
                                }

                            });
                            menu.add(移动);
                            JMenuItem 删除 = new JMenuItem("删除");
                            删除.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    File source = (File) fileTree.getSelectionPath().getLastPathComponent();
                                    fileTree.setSelectionRow(-1);
                                    Main.disk.delete(source);
                                    loadFiles();
                                }
                            });
                            menu.add(删除);
                        }
                        menu.show(fileTree, e.getX(), e.getY());
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                int row = fileTree.getRowForLocation(e.getX(), e.getY());
                fileTree.setSelectionRow(row);
            }
        });

        fileTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                // atime
                for (int i = 0; i < e.getPath().getPathCount(); i++) {
                    File p = (File) e.getPath().getPathComponent(i);
                    Inode inode = Main.disk.getInode(p);
                    inode.atime = new Date();
                }
                // inode
                refreshInodeTextArea();
                // content
                refreshEditTextArea();
                // enabled
                resetButtonStatus();
            }
        });

        保存Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File file = (File) fileTree.getSelectionPath().getLastPathComponent();
                try {
                    Main.disk.editFile(file, fileEditTextArea.getText().getBytes(), user, group);
                    refreshInodeTextArea();
                }
                catch (IOException e1) {
                    e1.printStackTrace();
                }
                catch (PermissionDeniedException e1) {
                    JOptionPane.showMessageDialog(panel1, e1.getMessage(), "权限不足", JOptionPane.ERROR_MESSAGE);
                    e1.printStackTrace();
                }
                catch (SpaceNotEnoughException e1) {
                    JOptionPane.showMessageDialog(panel1, e1.getMessage(), "空间不足", JOptionPane.ERROR_MESSAGE);
                    e1.printStackTrace();
                }
            }
        });

        修改所有者Button.addActionListener(e -> {
            MyFile file = (MyFile) fileTree.getSelectionPath().getLastPathComponent();
            OwnerDialog dialog = new OwnerDialog(this);
            dialog.setText(Main.disk.getInode(file).uid, Main.disk.getInode(file).gid);
            dialog.setVisible(true);
            if(!dialog.accepted) return;
            try {
                Main.disk.chown(file, dialog.getUser(), dialog.getGroup(), user, group);
            }
            catch (PermissionDeniedException e1) {
                JOptionPane.showMessageDialog(panel1, e1.getMessage(), "权限不足", JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }
            refreshInodeTextArea();
        });
        修改权限Button.addActionListener(e -> {
            MyFile file = (MyFile) fileTree.getSelectionPath().getLastPathComponent();
            PermissionDialog dialog = new PermissionDialog(this);
            dialog.setCheckBoxes(Main.disk.getInode(file).mode);
            dialog.setVisible(true);
            if(!dialog.accepted) return;
            int mode = dialog.getMode();
            System.out.println("new mode="+mode);
            try {
                Main.disk.chmod(file, mode, user, group);
            }
            catch (PermissionDeniedException e1) {
                JOptionPane.showMessageDialog(panel1, e1.getMessage(), "权限不足", JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }
            refreshInodeTextArea();
        });
        取消Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    fileEditTextArea.setText(new String(Main.readAllFile((MyFile) fileTree.getSelectionPath().getLastPathComponent())));
                }
                catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        // add listeners finished

        this.user = user;
        this.group = group;
        treeModel = new FileTreeModel(new MyFile("disk/"), user, group);
        fileTree.setModel(treeModel);
        setTitle("文件系统：" + user + ":" + group);
        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);


        查看详细Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Inode inode = Main.disk.getInode((File) fileTree.getSelectionPath().getLastPathComponent());
                StringBuilder sb = new StringBuilder();
                sb.append("Inode ID: ").append(inode.ino).append('\n');
                sb.append("文件大小: ").append(inode.size).append(" B\n");
                sb.append("拥有者: ").append(inode.uid).append('\n');
                sb.append("拥有组: ").append(inode.gid).append('\n');
                sb.append("权限: ").append(inode.modeStr()).append('\n');
                sb.append("上次访问时间: ").append(inode.atime).append('\n');
                sb.append("上次修改时间: ").append(inode.mtime).append('\n');
                sb.append("上次Inode修改时间: ").append(inode.ctime).append('\n');
                sb.append("链接数: ").append(inode.nlink).append('\n');
                int nblock = inode.size/(Main.disk.blockSize*1024);
                if(inode.size%(Main.disk.blockSize*1024)!=0)
                    nblock++;
                if(nblock>12)
                    nblock++;
                sb.append("文件占用块（含间接指针块）: ").append(nblock).append('\n');
                if(nblock<=12) {
                    for(int i=0; i<nblock; i++) {
                        sb.append("直接指针").append(i).append(": ").append(inode.pointer[i]).append('\n');
                    }
                    for(int i=nblock; i<12; i++) {
                        sb.append("直接指针").append(i).append(": NULL\n");
                    }
                    sb.append("一级间接指针块: NULL\n");
                    sb.append("二级间接指针块: NULL\n");
                    sb.append("三级间接指针块: NULL");
                }
                else {
                    for(int i=0; i<12; i++) {
                        sb.append("直接指针").append(i).append(": ").append(inode.pointer[i]).append('\n');
                    }
                    sb.append("一级间接指针块: ").append(inode.pointer[12]).append('\n');
                    sb.append("一级间接指针内容: \n");
                    sb.append(inode.pt1).append('\n');
                    sb.append("二级间接指针块: NULL\n");
                    sb.append("三级间接指针块: NULL");
                    // TODO: 二级指针
                }
                JOptionPane.showMessageDialog(panel1, sb.toString());
            }
        });
    }

    // 根据磁盘上的文件，重新填充fileTree
    void loadFiles() {
        treeModel.reload();
    }

    // inodeTextArea
    void refreshInodeTextArea() {
        if (fileTree.isSelectionEmpty()) {
            inodeTextArea.setText("");
        }
        else {
            //inodeTextArea.setText(Main.disk.getInode((File) fileTree.getSelectionPath().getLastPathComponent()).toJSONObject().toString());
            Inode inode = Main.disk.getInode((File) fileTree.getSelectionPath().getLastPathComponent());
            StringBuilder sb = new StringBuilder();
            sb.append("Inode ID: ").append(inode.ino).append('\n');
            sb.append("文件大小: ").append(inode.size).append(" B\n");
            sb.append("拥有者: ").append(inode.uid).append('\n');
            sb.append("拥有组: ").append(inode.gid).append('\n');
            sb.append("权限: ").append(inode.modeStr()).append('\n');
            sb.append("上次访问时间: ").append(inode.atime).append('\n');
            sb.append("上次修改时间: ").append(inode.mtime).append('\n');
            sb.append("上次Inode修改时间: ").append(inode.ctime).append('\n');
            sb.append("链接数: ").append(inode.nlink).append('\n');
            inodeTextArea.setText(sb.toString());
        }
    }

    void refreshEditTextArea() {
        if(fileTree.isSelectionEmpty()) {
            fileEditTextArea.setText("");
            return;
        }
        File file = (File) fileTree.getSelectionPath().getLastPathComponent();
        if (file.isFile()) {
            try {
                if(Main.disk.getInode(file).permitToRead(user, group))
                    fileEditTextArea.setText(new String(Main.readAllFile(file)));
                else
                    fileEditTextArea.setText("（权限不足，无法查看）");
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        else {
            fileEditTextArea.setText("");
        }
    }

    void resetButtonStatus() {
        if(fileTree.isSelectionEmpty()) {
            保存Button.setEnabled(false);
            取消Button.setEnabled(false);
            修改所有者Button.setEnabled(false);
            修改权限Button.setEnabled(false);
            查看详细Button.setEnabled(false);
            fileEditTextArea.setEnabled(false);
        }
        else {
            File file = (File) fileTree.getSelectionPath().getLastPathComponent();
            if(file.isFile()) {
                保存Button.setEnabled(true);
                取消Button.setEnabled(true);
                修改所有者Button.setEnabled(true);
                修改权限Button.setEnabled(true);
                查看详细Button.setEnabled(true);
                fileEditTextArea.setEnabled(true);
            }
            else if(file.isDirectory()) {
                保存Button.setEnabled(false);
                取消Button.setEnabled(false);
                修改所有者Button.setEnabled(true);
                修改权限Button.setEnabled(true);
                查看详细Button.setEnabled(true);
                fileEditTextArea.setEnabled(false);
            }
        }
    }
}

class FileTreeModel implements TreeModel {
    private final ArrayList<TreeModelListener> mListeners = new ArrayList<>();
    private final MyFile mFile;
    String user, group;

    public FileTreeModel(final MyFile pFile, String user, String group) {
        mFile = pFile;
        this.user = user;
        this.group = group;
    }

    @Override
    public Object getRoot() {
        return mFile;
    }

    @Override
    public Object getChild(final Object pParent, final int pIndex) {
        if(!Main.disk.getInode((MyFile)pParent).permitToRead(user, group))
            return null;
        return ((MyFile) pParent).listFiles()[pIndex];
    }

    @Override
    public int getChildCount(final Object pParent) {
        if(!Main.disk.getInode((MyFile)pParent).permitToRead(user, group))
            return 0;
        return ((MyFile) pParent).listFiles().length;
    }

    @Override
    public boolean isLeaf(final Object pNode) {
        return !((MyFile) pNode).isDirectory();
    }

    @Override
    public void valueForPathChanged(final TreePath pPath, final Object pNewValue) {
        final MyFile oldTmp = (MyFile) pPath.getLastPathComponent();
        final File oldFile = oldTmp;
        final String newName = (String) pNewValue;
        final File newFile = new File(oldFile.getParentFile(), newName);
        oldFile.renameTo(newFile);
        System.out.println("Renamed '" + oldFile + "' to '" + newFile + "'.");
        reload();
    }

    @Override
    public int getIndexOfChild(final Object pParent, final Object pChild) {
        final MyFile[] files = ((MyFile) pParent).listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].equals(pChild)) return i;
        }
        return -1;
    }

    @Override
    public void addTreeModelListener(final TreeModelListener pL) {
        mListeners.add(pL);
    }

    @Override
    public void removeTreeModelListener(final TreeModelListener pL) {
        mListeners.remove(pL);
    }

    public void reload() {
        // Need to duplicate the code because the root can formally be
        // no an instance of the TreeNode.
        final int n = getChildCount(getRoot());
        final int[] childIdx = new int[n];
        final Object[] children = new Object[n];

        for (int i = 0; i < n; i++) {
            childIdx[i] = i;
            children[i] = getChild(getRoot(), i);
        }

        fireTreeStructureChanged(this, new Object[]{getRoot()}, childIdx, children);
    }

    protected void fireTreeStructureChanged(final Object source, final Object[] path, final int[] childIndices, final Object[] children) {
        final TreeModelEvent event = new TreeModelEvent(source, path, childIndices, children);
        for (final TreeModelListener l : mListeners) {
            l.treeStructureChanged(event);
        }
    }
}

class MyFile extends File {

    public MyFile(File file) {
        super(file.getPath());
    }

    public MyFile(String pathname) {
        super(pathname);
    }

    public MyFile(File parent, String child) {
        super(parent, child);
    }

    public MyFile[] listFiles() {
        final File[] files = super.listFiles();
        if (files == null) return null;
        if (files.length < 1) return new MyFile[0];

        final MyFile[] ret = new MyFile[files.length];
        for (int i = 0; i < ret.length; i++) {
            final File f = files[i];
            ret[i] = new MyFile(f);
        }
        return ret;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int hashCode() {
        return getAbsolutePath().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return getAbsolutePath().equals(((MyFile) obj).getAbsolutePath());
    }
}