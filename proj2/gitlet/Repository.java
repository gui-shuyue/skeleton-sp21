package gitlet;

import java.io.File;
import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The commits directory. */
    public File COMMITS_DIR;
    /** The blobs directory. */
    public File BLOBS_DIR;
    /** All the branches' name*/
    public File REFS_DIR;
    public File HEADS_DIR;
    /** store the address of the head pointer */
    public File HEAD;

    public void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        configDirs();

        Commit initCommit = new Commit();
        writeCommitToFile(initCommit);
        String id = initCommit.getID();

        // creat branch "master"
        String branchName = "master";
        writeContents(HEAD, branchName);
        File master = join(HEADS_DIR, branchName);
        writeContents(master, id);
    }

    private void configDirs() {
        GITLET_DIR.mkdirs();
        this.COMMITS_DIR = join(GITLET_DIR, "commits");
        this.BLOBS_DIR = join(GITLET_DIR, "blobs");
        this.REFS_DIR = join(GITLET_DIR, "refs");
        this.HEADS_DIR = join(REFS_DIR, "heads");
        this.HEAD = join(GITLET_DIR, "HEAD");
        COMMITS_DIR.mkdirs();
        BLOBS_DIR.mkdirs();
        REFS_DIR.mkdirs();
        HEADS_DIR.mkdirs();
    }

    // TODO: stage to be completed.
    /**
     * The .gitlet directory
     * .gitlet
     * -- commits
     * -- blobs
     * -- refs
     *  -- heads
     *   -- [master][branch name]
     *    -- commit id
     * -- [HEAD]
     * */

    public void add(String filename) {
        checkIfInitialized();
        File file = join(CWD, filename);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        // TODO: blob
        Commit headCommit = getHead();
        
    }

    public void checkOperand(int input, int expected) {
        if (input != expected) {
            msgIncorrectOperands();
        }
    }

    private void msgIncorrectOperands() {
        System.out.println("Incorrect operands.");
        System.exit(0);
    }

    private void writeCommitToFile(Commit commit) {
        File file = join(COMMITS_DIR, commit.getID());
        writeObject(file, commit);
    }

    private void writeBlobToFile(Blob blob) {
        File file = join(BLOBS_DIR, blob.getId());
        writeObject(file, blob);
    }// TODO: unsure about its accuracy

    private void checkIfInitialized() {
        if (!GITLET_DIR.isDirectory()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    private String getHeadBranchName() {
        return readContentsAsString(HEAD);
    }

    /** get the file that contains a commit id, which is the commit at the front of a branch. */
    private File getBranchFile(String branchName) {
        File file = join(HEADS_DIR, branchName);
        return file;
    } // TODO: may need to change for remote function.

    private Commit getCommitFromFile(File file) {
        String id = readContentsAsString(file);
        return getCommitFromId(id);
    }

    private Commit getCommitFromId(String id) {
        File file = join(COMMITS_DIR, id);
        return readObject(file, Commit.class);
    }

    private Commit getHead() {
        String headBranchName = getHeadBranchName();
        File file = getBranchFile(headBranchName);
        return getCommitFromFile(file);
    }
}
