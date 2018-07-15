import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Inode {
    int ino;
    int size;
    String uid;
    String gid;
    int mode;
    Date atime; // 打开时间
    Date mtime; // 内容变动时间
    Date ctime; // inode变动时间
    int nlink;
    int[] pointer; // int[15]
    List<Integer> pt1;
    List<List<Integer>> pt2;
    List<List<List<Integer>>> pt3;

    public Inode(int ino, int size, String uid, String gid, int mode, Date atime, Date mtime, Date ctime, int nlink, int[] pointer) {
        this.ino = ino;
        this.size = size;
        this.uid = uid;
        this.gid = gid;
        this.mode = mode;
        this.atime = atime;
        this.mtime = mtime;
        this.ctime = ctime;
        this.nlink = nlink;
        this.pointer = pointer;
    }

    public Inode(JSONObject inodeObject) {
        ino = inodeObject.getInt("ino");
        size = inodeObject.getInt("size");
        uid = inodeObject.getString("uid");
        gid = inodeObject.getString("gid");
        mode = inodeObject.getInt("mode");
        atime = new Date(inodeObject.getLong("atime"));
        mtime = new Date(inodeObject.getLong("mtime"));
        ctime = new Date(inodeObject.getLong("ctime"));
        nlink = inodeObject.getInt("nlink");
        pointer = new int[15];
        for(int i=0; i<pointer.length; i++) {
            pointer[i] = inodeObject.getJSONArray("pointer").getInt(i);
        }
        try {
            JSONArray pt1Array = inodeObject.getJSONArray("pt1");
            pt1 = new ArrayList<>(pt1Array.length());
            for(int i=0; i<pt1Array.length(); i++) {
                pt1.add(pt1Array.getInt(i));
            }
        }
        catch (JSONException e) {
            System.out.println("Inode"+ino+"没有一级指针");
        }
        // TODO: 二级指针
    }

    public JSONObject toJSONObject() {
        JSONObject rst = new JSONObject();
        rst.put("ino", ino);
        rst.put("size", size);
        rst.put("uid", uid);
        rst.put("gid", gid);
        rst.put("mode", mode);
        rst.put("atime",atime.getTime());
        rst.put("mtime",mtime.getTime());
        rst.put("ctime",ctime.getTime());
        rst.put("nlink",nlink);
        rst.put("pointer",new JSONArray(pointer));
        if(pt1!=null)
            rst.put("pt1", new JSONArray(pt1));
        // TODO: 二级指针
        return rst;
    }

    public List<Integer> getAllBlocks() {
        int nblock = size/(Main.disk.blockSize*1024);
        if(size%(Main.disk.blockSize*1024)!=0)
            nblock++;
        List<Integer> rst = new ArrayList<>(nblock);
        if(nblock<=12) {
            for(int i=0; i<nblock; i++) {
                rst.add(pointer[i]);
            }
        }
        else {
            for(int i=0; i<12;i++) {
                rst.add(pointer[i]);
            }
            rst.add(pointer[12]);
            rst.addAll(pt1);
            // TODO: 添加二级指针
        }
        return rst;
    }

    public String modeStr() {
        char[] rst = new char[10];
        rst[0] = ((MyFile)Main.disk.getFiles(ino).toArray()[0]).isFile()?'-':'d';
        rst[1] = (mode>>8)==1?'r':'-';
        rst[2] = ((mode>>7)&1)==1?'w':'-';
        rst[3] = ((mode>>6)&1)==1?'x':'-';
        rst[4] = ((mode>>5)&1)==1?'r':'-';
        rst[5] = ((mode>>4)&1)==1?'w':'-';
        rst[6] = ((mode>>3)&1)==1?'x':'-';
        rst[7] = ((mode>>2)&1)==1?'r':'-';
        rst[8] = ((mode>>1)&1)==1?'w':'-';
        rst[9] = (mode&1)==1?'x':'-';
        return new String(rst);
    }

    public boolean permitToWrite(String uid, String gid) {
        if(uid.equals("root"))
            return true;
        if(uid.equals(this.uid))
            return ((mode>>7)&1)==1;
        if(gid.equals(this.gid))
            return ((mode>>4)&1)==1;
        return ((mode>>1)&1)==1;
    }

    public boolean permitToRead(String uid, String gid) {
        if(uid.equals("root"))
            return true;
        if(uid.equals(this.uid))
            return ((mode>>8)&1)==1;
        if(gid.equals(this.gid))
            return ((mode>>5)&1)==1;
        return ((mode>>2)&1)==1;
    }
}
