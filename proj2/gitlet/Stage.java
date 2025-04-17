package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/** The staging area */

public class Stage implements Serializable {
    //<filename, blob's id>
    private HashMap<String, String> add;
    //<filename>
    private HashSet<String> remove;

    public Stage() {
        add = new HashMap<>();
        remove = new HashSet<>();
    }

    public void addFile(String file, String blobID) {
        add.put(file, blobID);
        remove.remove(file);
    }

    public void removeFile(String file) {
        remove.add(file);
        add.remove(file);
    }

    public boolean ifExistInAdd(String fileNane) {
        return this.add.containsKey(fileNane);
    }

    public HashMap<String, String> getAdd() {
        return add;
    }

    public HashSet<String> getRemove() { return remove; }

    public void writeStageToFile(String fileNane) {}
}
