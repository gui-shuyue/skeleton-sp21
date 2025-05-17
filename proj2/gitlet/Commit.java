package gitlet;

import java.text.SimpleDateFormat;
import java.util.*;
import static gitlet.Utils.*;

import java.io.Serializable;
import java.util.Date;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private String id;
    private String timestamp;
    private List<String> parents;
    private HashMap<String, String> blobs; // TODO: potential problems, may need to be changed to TreeMap

    /** initial commit */
    public Commit() {
        this.message = "initial commit";
        this.timestamp = formatTime();
        this.id = sha1(message, timestamp);
        this.parents = new ArrayList<>();
        this.blobs = new HashMap<String, String>();
    }

    public Commit(String message, List<Commit> parents, Stage stage) {
        this.message = message;
        this.timestamp = formatTime();
        this.parents = new ArrayList<String>(2);
        for (Commit parent : parents) {
            this.parents.add(parent.id);
        }

        this.blobs = new HashMap<>(parents.get(0).getBlobs()); //note that "parents" here is tne argument, not variable.
        for(Map.Entry<String, String> blob : stage.getAdd().entrySet()) {
            this.blobs.put(blob.getKey(), blob.getValue());
        }

        for (String file :stage.getRemove()) {
            this.blobs.remove(file);
        }

        this.id = sha1(message, timestamp, parents.toString(), blobs.toString());
    }

    public String getMessage() {
        return message;
    }

    public String getID() {
        return id;
    }

    public List<String> getParents() {
        return parents;
    }

    public String getTimestamp() {
        return timestamp;
    }

    private String formatTime() {
        Date d;
        if (this.message.equals("initial commit")) {
            d = new Date(0);
        }
        else {
            d = new Date();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z", Locale.US);
        return sdf.format(d);
    }

    public HashMap<String, String> getBlobs() {
        return this.blobs;
    }

    public boolean ifInBlobs(String filename) {
        return this.blobs.containsKey(filename);
    }

    public String getFirstPaId() {
        if (parents.isEmpty()) {
            return null;  // or throw an IllegalStateException("No parent")
        }
        return parents.get(0);
    }
}
