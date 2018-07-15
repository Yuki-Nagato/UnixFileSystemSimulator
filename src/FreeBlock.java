import com.sun.istack.internal.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FreeBlock {
    Map<Integer, List<Integer>> stacks = new HashMap<>();
    List<Integer> usedForGroup = new ArrayList<>();

    // 建立一个新的磁盘，其中0~nInodeBlock-1用于存储Inode，剩下的nInodeBlock~nBlock-1用于存储文件和空闲盘块组
    // 每个List中最多有100个元素，其中第0个是下一组的编号，为0表示无下一组
    // stacks[0]在内存中
    public FreeBlock(int nBlock, int nInodeBlock) {
        stacks.put(0, new ArrayList<Integer>(100){{add(0);}});

        List<Integer> toAdd = new ArrayList<>(nBlock-nInodeBlock);
        for(int i=nInodeBlock; i<nBlock; i++) {
            toAdd.add(i);
        }
        free(toAdd);
    }

    public FreeBlock(JSONObject freeblockObject) {
        for(Map.Entry<String, Object> entry : freeblockObject.getJSONObject("stacks").toMap().entrySet()) {
            List<Integer> value = (List<Integer>)entry.getValue();
            //List<Integer> temp = new ArrayList<>(value.size());
            //for(Object it : value) {
            //    temp.add((Integer)it);
            //}
            //stacks.put(Integer.parseInt(entry.getKey()), temp);
            stacks.put(Integer.parseInt(entry.getKey()), value);
        }
        for(Object it : freeblockObject.getJSONArray("usedForGroup")) {
            usedForGroup.add((Integer)it);
        }
    }

    public JSONObject toJSONObject() {
        JSONObject rst = new JSONObject();
        JSONObject stacksObject = new JSONObject();
        for(Map.Entry<Integer, List<Integer>> entry : stacks.entrySet()) {
            stacksObject.put(String.valueOf(entry.getKey()), new JSONArray(entry.getValue()));
        }
        rst.put("stacks", stacksObject);
        rst.put("usedForGroup", new JSONArray(usedForGroup));
        return rst;
    }

    // 申请count个磁盘块
    public List<Integer> alloc(int count) throws SpaceNotEnoughException {
        List<Integer> rst = new ArrayList<>(count);
        List<Integer> mainStack = stacks.get(0);

        for(int i=0; i<count; i++) {
            if(mainStack.size()>1) {
                rst.add(mainStack.get(mainStack.size()-1));
                mainStack.remove(mainStack.size()-1);
            }
            else {
                int next = mainStack.get(0);
                if(next==0) {
                    free(rst);
                    throw new SpaceNotEnoughException();
                }
                else {
                    // 分配原来存储下一组的盘块
                    rst.add(next);
                    // 下一组装入内存
                    mainStack = stacks.get(next);
                    stacks.put(0, mainStack);
                    stacks.remove(next);
                    usedForGroup.remove(usedForGroup.size()-1);
                }
            }
        }
        return rst;
    }

    // 回收blocks
    public void free(List<Integer> blocks) {
        List<Integer> mainStack = stacks.get(0);
        for(int block : blocks) {
            if(mainStack.size()<100) {
                mainStack.add(block);
            }
            else {
                usedForGroup.add(block);
                stacks.put(block, mainStack);
                mainStack = new ArrayList<Integer>(100){{add(block);}};
                stacks.put(0, mainStack);
            }
        }
    }

}
