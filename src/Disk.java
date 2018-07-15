import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class Disk {
    int nBlock, nInodeBlock, blockSize, inodeSize;
    FreeBlock freeBlock;

    Map<Integer, Inode> inodes = new HashMap<>();
    Map<MyFile, Integer> fino = new HashMap<>();
    Map<Integer, Set<MyFile>> inof = new HashMap<>();

    int nino = 0;

    public int nInode() {
        return blockSize/inodeSize*nInodeBlock;
    }
    public int nptpBlock() {
        return blockSize*1024/4;
    }

    public Disk(int nBlock, int nInodeBlock, int blockSize, int inodeSize) throws SpaceNotEnoughException, InodeOverflowException {
        deleteFileWithoutCollection(new File("disk/"));
        this.nBlock = nBlock;
        this.nInodeBlock = nInodeBlock;
        this.blockSize = blockSize;
        this.inodeSize = inodeSize;
        freeBlock = new FreeBlock(nBlock, nInodeBlock);
        try {
            createDirectory(new MyFile("disk/"), "root", "root");
        }
        catch (PermissionDeniedException e) {
            e.printStackTrace();
        }
    }

    public Disk(JSONObject diskObject) throws IOException {
        nBlock = diskObject.getInt("nblock");
        nInodeBlock = diskObject.getInt("ninode-block");
        blockSize = diskObject.getInt("block-size");
        inodeSize = diskObject.getInt("inode-size");
        nino = diskObject.getInt("nino");
        freeBlock = new FreeBlock(diskObject.getJSONObject("free-block"));
        // inodes
        JSONObject inodesObject = diskObject.getJSONObject("inodes");
        for(String key : inodesObject.toMap().keySet()) {
            inodes.put(Integer.parseInt(key), new Inode(inodesObject.getJSONObject(key)));
        }
        // fino
        JSONObject finoObject = diskObject.getJSONObject("fino");
        for (Map.Entry<String, Object> entry : finoObject.toMap().entrySet()) {
            fino.put(new MyFile(entry.getKey()), (int)entry.getValue());
        }
        // inof
        JSONObject inofObject = diskObject.getJSONObject("inof");
        for (Map.Entry<String, Object> entry : inofObject.toMap().entrySet()) {
            Set<MyFile> f = new HashSet<>();
            for (String fp : (Collection<String>)entry.getValue()) {
                f.add(new MyFile(fp));
            }
            inof.put(Integer.parseInt(entry.getKey()), f);
        }
    }

    public JSONObject toJSONObject() {
        JSONObject rst = new JSONObject();
        rst.put("nblock", nBlock);
        rst.put("ninode-block", nInodeBlock);
        rst.put("block-size", blockSize);
        rst.put("inode-size", inodeSize);
        rst.put("nino", nino);
        rst.put("free-block", freeBlock.toJSONObject());
        // inodes
        JSONObject inodesObject = new JSONObject();
        for(Map.Entry<Integer, Inode> entry : inodes.entrySet()) {
            inodesObject.put(entry.getKey().toString(), entry.getValue().toJSONObject());
        }
        rst.put("inodes", inodesObject);
        // fino
        JSONObject finoObject = new JSONObject();
        for (Map.Entry<MyFile, Integer> entry : fino.entrySet()) {
            finoObject.put(entry.getKey().getPath(), entry.getValue().intValue());
        }
        rst.put("fino", finoObject);
        // inof
        JSONObject inofObject = new JSONObject();
        for(Map.Entry<Integer, Set<MyFile>> entry : inof.entrySet()) {
            JSONArray filesArray = new JSONArray();
            for(File file : entry.getValue()) {
                filesArray.put(file.getPath());
            }
            inofObject.put(entry.getKey().toString(), filesArray);
        }
        rst.put("inof", inofObject);
        return rst;
    }

    public static void deleteFileWithoutCollection(File file) {
        if(file.isDirectory()) {
            for(File f : file.listFiles()) {
                deleteFileWithoutCollection(f);
            }
        }
        file.delete();
    }

    public Inode getInode(File file) {
        if(file instanceof MyFile)
            return inodes.get(fino.get(file));
        else
            return inodes.get(fino.get(new MyFile(file)));
    }

    public Set<MyFile> getFiles(int ino) {
        return inof.get(ino);
    }

    public Inode createFile(MyFile file, String uid, String gid) throws InodeOverflowException, IOException, PermissionDeniedException {
        if(!getInode(file.getParentFile()).permitToWrite(uid, gid)) {
            throw new PermissionDeniedException();
        }
        if(inodes.size()>=nInode()) {
            throw new InodeOverflowException();
        }
        Inode inode = new Inode(nino, 0, uid, gid, 0644, new Date(), new Date(), new Date(), 1, new int[15]);
        file.createNewFile();

        inodes.put(nino, inode);
        fino.put(file, nino);
        inof.put(nino, new HashSet<MyFile>(){{add(file);}});
        nino++;
        Main.save();
        return inode;
    }

    public Inode createDirectory(MyFile directory, String uid, String gid) throws InodeOverflowException, SpaceNotEnoughException, PermissionDeniedException {
        if(!directory.equals(new MyFile("disk/")) && !getInode(directory.getParentFile()).permitToWrite(uid, gid)) {
            throw new PermissionDeniedException();
        }
        if(inodes.size()>=nInode()) {
            throw new InodeOverflowException();
        }
        List<Integer> blocks = freeBlock.alloc(1);
        Inode inode = new Inode(nino, blockSize, uid, gid, 0755, new Date(), new Date(), new Date(), 2, new int[15]);
        adjustBlocksToPointers(blocks, inode);
        directory.mkdirs();

        inodes.put(nino, inode);
        fino.put(directory, nino);
        inof.put(nino, new HashSet<MyFile>(){{add(directory);}});
        nino++;
        if(!directory.equals(new MyFile("disk/"))) {
            getInode(directory.getParentFile()).nlink++;
            Main.save();
        }
        return inode;
    }

    public Inode editFile(File file, byte[] content, String uid, String gid) throws IOException, SpaceNotEnoughException, PermissionDeniedException {
        Inode inode = getInode(file);

        if(!inode.permitToWrite(uid, gid))
            throw new PermissionDeniedException();

        freeBlock.free(inode.getAllBlocks());

        int nblock = content.length/(blockSize*1024);
        if(content.length%(blockSize*1024)!=0)
            nblock++;
        List<Integer> blocks = freeBlock.alloc(nblock);
        adjustBlocksToPointers(blocks, inode);
        inode.size = content.length;
        inode.mtime = new Date();
        // 实际写入文件
        for(File p : getFiles(inode.ino)) {
            FileOutputStream fout = new FileOutputStream(p);
            fout.write(content);
            fout.close();
        }
        Main.save();
        return inode;
    }

    private Inode copyFile(MyFile source, MyFile dest) throws InodeOverflowException, SpaceNotEnoughException, IOException {
        if(inodes.size() >= nInode()) {
            throw new InodeOverflowException();
        }
        Inode old = getInode(source);
        int nblock = old.size/(blockSize*1024);
        if(old.size%(blockSize*1024)!=0)
            nblock++;
        List<Integer> blocks = freeBlock.alloc(nblock);
        Inode newInode = new Inode(old.toJSONObject());
        adjustBlocksToPointers(blocks, newInode);
        newInode.ino = nino;
        newInode.nlink = 1;
        newInode.ctime = new Date();
        Files.copy(source.toPath(), dest.toPath());

        inodes.put(nino, newInode);
        fino.put(dest, nino);
        inof.put(nino, new HashSet<MyFile>(){{add(dest);}});
        nino++;
        return newInode;
    }

    public void copy(MyFile source, MyFile dest) throws IOException, InodeOverflowException, SpaceNotEnoughException, PermissionDeniedException {
        if(source.isFile()) {
            copyFile(source, dest);
        }
        else if(source.isDirectory()) {
            createDirectory(dest, getInode(source).uid, getInode(source).gid);
            for(File f : source.listFiles()) {
                copy(new MyFile(f), new MyFile(dest, f.getName()));
            }
        }
        Main.save();
    }

    private void deleteFile(File file) {
        Inode inode = getInode(file);
        if(--inode.nlink<=0) {
            inodes.remove(inode.ino);
            inof.remove(inode.ino);
            fino.remove(file);
            freeBlock.free(inode.getAllBlocks());
        }
        else {
            inode.ctime = new Date();
        }

        file.delete();
    }

    private Inode moveFile(MyFile source, MyFile dest) throws IOException {
        Inode inode = getInode(source);

        fino.remove(source);
        getFiles(inode.ino).remove(source);
        Files.move(source.toPath(), dest.toPath());
        fino.put(dest, inode.ino);
        getFiles(inode.ino).add(dest);
        return inode;
    }

    private void deleteEmptyDirectory(File directory) {
        Inode inode = getInode(directory);
        getInode(directory.getParentFile()).nlink--;
        inodes.remove(inode.ino);
        inof.remove(inode.ino);
        fino.remove(directory);
        freeBlock.free(inode.getAllBlocks());
        directory.delete();
    }

    public void delete(File file) {
        if(file.isFile()) {
            deleteFile(file);
        }
        else if(file.isDirectory()) {
            for(File f : file.listFiles())
                delete(f);
            deleteEmptyDirectory(file);
        }
        Main.save();
    }

    public void move(MyFile source, MyFile dest) throws IOException, SpaceNotEnoughException, InodeOverflowException, PermissionDeniedException {
        if(source.isFile()) {
            moveFile(source, dest);
        }
        else if(source.isDirectory()) {
            createDirectory(dest, getInode(source).uid, getInode(source).gid);
            for(File f : source.listFiles()) {
                move(new MyFile(f), new MyFile(dest, f.getName()));
            }
            deleteEmptyDirectory(source);
        }
        Main.save();
    }

    public void chmod(MyFile file, int mode, String uid, String gid) throws PermissionDeniedException {
        Inode inode = getInode(file);
        if(!uid.equals("root") && !uid.equals(inode.uid))
            throw new PermissionDeniedException();
        inode.mode = mode;
        Main.save();
    }

    public void chown(MyFile file, String uid, String gid, String oldUid, String oldGid) throws PermissionDeniedException {
        Inode inode = getInode(file);
        if(!oldUid.equals("root") && !oldUid.equals(inode.uid))
            throw new PermissionDeniedException();
        inode.uid = uid;
        inode.gid = gid;
        Main.save();
    }

    public void adjustBlocksToPointers(List<Integer> blocks, Inode inode) throws SpaceNotEnoughException {
        inode.ctime = new Date();
        inode.pointer = new int[15];
        inode.pt1 = null; inode.pt2 = null; inode.pt3 = null;
        if(blocks.size()<=12) {
            for(int i=0; i<blocks.size(); i++) {
                inode.pointer[i] = blocks.get(i);
            }
        }
        else {
            for(int i=0; i<12; i++) {
                inode.pointer[i] = blocks.get(i);
            }
            List<Integer> pb = freeBlock.alloc(1);
            inode.pointer[12] = pb.get(0);
            inode.pt1 = new ArrayList<>();
            if(blocks.size()-12 <= nptpBlock()) {
                for(int i=0; i<blocks.size()-12; i++) {
                    inode.pt1.add(blocks.get(12+i));
                }
            }
            else {
                for(int i=0; i<nptpBlock(); i++) {
                    inode.pt1.add(blocks.get(12+i));
                }
                // TODO: 添加二级指针
            }
        }
    }
}
