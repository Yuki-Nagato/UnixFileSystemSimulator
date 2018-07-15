import org.json.JSONObject;

import javax.swing.*;
import java.io.*;

public class Main {
    public static Disk disk;

    public static void save() {
        JSONObject object = new JSONObject();
        object.put("disk", disk.toJSONObject());

        try {
            FileWriter fw = new FileWriter("filesystem.json");
            fw.write(object.toString());
            fw.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void load() throws IOException {
        String json = new String(readAllFile(new File("filesystem.json")));
        JSONObject fileSystemObject = new JSONObject(json);
        disk = new Disk(fileSystemObject.getJSONObject("disk"));
    }

    public static byte[] readAllFile(File file) throws IOException {
        FileInputStream fin = new FileInputStream(file);
        byte[] rst = new byte[(int) file.length()];
        fin.read(rst);
        fin.close();
        return rst;
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        File filesystem = new File("filesystem.json");
        if(filesystem.exists()) {
            load();
        }
        else {
            disk = new Disk(10240,1024,4,1);
            save();
        }

        new LoginWindow();
    }
}
