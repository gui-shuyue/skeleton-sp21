package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;


import static gitlet.Utils.*;
import static java.nio.file.Files.delete;

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
    /** store the name of the head pointer */
    public File HEAD;
    /** The staging area. */
    public File STAGE;

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
        this.STAGE = join(GITLET_DIR, "stage");
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
     * -- [stage]
     * */

    /**
     * 1. Adds a copy of the file as it currently exists to the staging area.
     * 2. Staging an already-staged file overwrites the previous entry in the staging area with the new contents.
     * 3. If the current working version of the file is identical to the version in the current commit, do not
     *    stage it to be added, and remove it from the staging area if it is already there.
     * 4. The file will no longer be staged for removal, if it was at the time of the command.
     */
    public void add(String filename) {
        checkIfInitialized();
        File file = join(CWD, filename);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        // TODO: Too much 'if', how to simplify it?
        Commit headCommit = getHead();
        Stage stage = getStage();
        Blob fileToBlob = new Blob(filename, CWD);
        HashMap<String, String> bolbMap = headCommit.getBlobs();
        for (String key : bolbMap.keySet()) {
            if (key.equals(filename)) {
                // the current working version of the file is identical to the version in the current commit
                if (bolbMap.get(key).equals(fileToBlob.getId())) {
                    // remove it from the staging area if it is already there
                    if (stage.ifExistInAdd(filename)) {
                        stage.getAdd().remove(filename);
                        return;
                    }
                } else {
                    // Staging an already-staged file overwrites the previous entry in the staging area with
                    // the new contents.
                    stage.addFile(filename, fileToBlob.getId());
                    writeBlobToFile(fileToBlob);
                    return;
                }
            }
        }
        writeBlobToFile(fileToBlob);
        stage.addFile(filename, fileToBlob.getId());
        writeStageToFile(stage);
    }

    /**
     * 1. Saves a snapshot of tracked files in the current commit and staging area
     *    so they can be restored at a later time, creating a new commit.
     * 2. A commit will only update the contents of files it is tracking that have
     *    been staged for addition at the time of commit.
     * 3. Files staged for addition and removal are the updates to the commit.
     * 4. The staging area is cleared after a commit.
     * 5. The head pointer now points to the new commit.
     */
    public void commit(String message) throws IOException {
        checkIfInitialized();
        if (STAGE.length() == 0) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        Commit headCommit = getHead();
        Stage stage = getStage();
        Commit newCommit = new Commit(message, List.of(headCommit), stage);
        clearStage();
        writeCommitToFile(newCommit);

        String id = newCommit.getID();
        String branchName = getHeadBranchName();
        File branch = getBranchFile(branchName);
        writeContents(branch, id);
    }

    /** 1. Unstage the file if it is currently staged for addition.
     *  2. If the file is tracked in the current commit, stage it for removal
     *  and remove the file from the working directory if the user has not
     *  already done so (do not remove it unless it is tracked in the current commit)*/
    public void rm(String filename) throws IOException {
        checkIfInitialized();
        File file = join(CWD, filename);

        Commit headCommit = getHead();
        Stage stage = getStage();

        if (!stage.ifExistInAdd(filename) && !headCommit.ifInBlobs(filename)) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }

        if (headCommit.ifInBlobs(filename)) {
            stage.removeFile(filename);
            delete(file.toPath());
        } else if (stage.ifExistInAdd(filename)) {
            stage.removeFile(filename);
        }
        writeStageToFile(stage);
    }

    /** 1. Starting at the current head commit, display information about each
     *  commit backwards along the commit tree until the initial commit.
     *  2. Ignore any second parents found in merge commits.
     */
    public void log() {
        checkIfInitialized();
        Commit commit = getHead();
        while (commit != null) {
            printCommit(commit);
            commit = getCommitFromId(commit.getFirstPaId());
        }
    }

    /** Like log, except displays information about all commits ever made.
     * The order of the commits does not matter.*/
    public void global_log() {
        checkIfInitialized();
        List<String> commitsName = plainFilenamesIn(COMMITS_DIR);
        for (String commitName : commitsName) {
            Commit commit = getCommitFromId(commitName);
            printCommit(commit);
        }
    }

    /** Prints out the ids of all commits that have the given commit message, one per line.
     * If there are multiple such commits, it prints the ids out on separate lines. */
    public void find(String message) {
        checkIfInitialized();
        List<String> commitIds = plainFilenamesIn(COMMITS_DIR);
        for (String commitId : commitIds) {
            Commit commit = getCommitFromId(commitId);
            if (commit.getMessage().equals(message)) {
                System.out.println(commitId);
            }
        }
    }

    /** Displays what branches currently exist, and marks the current branch with a *.
     * Also displays what files have been staged for addition or removal.*/
    public void status() {
        checkIfInitialized();
        System.out.println("=== Branches ===");
        printBranches();
        System.out.println("=== Staged Files ===");
        printStagedFiles();
        System.out.println("=== Removed Files ===");
        printRemoveFiles();
        System.out.println("=== Modifications Not Staged For Commit ===");
        printNotStagedFiles();
        System.out.println("=== Untracked Files ===");
        printUntrackedFiles();
    }


    /** Takes all files in the commit at the head of the given branch, and puts them in the
     * working directory, overwriting the versions of the files that are already there if they exist.*/
    public void checkoutBranch(String branch) throws IOException {
        File branchFile = getBranchFile(branch);
        if (!branchFile.exists()) {
            System.out.println("No such branch.");
            System.exit(0);
        }

        String headBranchName = getHeadBranchName();
        if (headBranchName.equals(branch)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        Commit givenCommit = getCommitFromBranch(branch);

        // Any files that are tracked in the current branch but are not present in the checked-out branch are deleted.
        if (ifUntrackedFileExist(givenCommit)) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }

        clearStage();
        replaceCWDWithCommit(givenCommit);

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
        // Directly store the corresponding content of the file
        // TODO: Is it OK?
        writeContents(file, blob.getContent());
    }

    public void writeStageToFile(Stage stage) {
        writeObject(this.STAGE, stage);
    }

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

    private byte[] getBlobFileFromId(String id) {
        File file = join(BLOBS_DIR, id);
        return readContents(file);
    }

    private Commit getHead() {
        String headBranchName = getHeadBranchName();
        File file = getBranchFile(headBranchName);
        return getCommitFromFile(file);
    }

    private Stage getStage() {
        return readObject(STAGE, Stage.class);
    }

    private void clearStage() throws IOException {
        Files.write(STAGE.toPath(), new byte[0]);
    }

    private void printCommit(Commit commit) {
        System.out.println("===");
        System.out.println("commit " + commit.getID());
        if (commit.getParents().size() == 2) {
            List<String> parents = commit.getParents();
            System.out.println("Merge: " + parents.get(0).substring(0, 7) + " " + parents.get(1).substring(0, 7));
        }
        System.out.println("Date: " + commit.getTimestamp());
        System.out.println(commit.getMessage());
        System.out.println(" ");
    }

    private void printBranches() {
        List<String> branchNames = plainFilenamesIn(HEADS_DIR);
        String head = readContentsAsString(HEAD);
        for (String branchName : branchNames) {
            if (branchName.equals(head)) {
                System.out.println("*" + branchName);
            } else {
                System.out.println(branchName);
            }
        }
    }

    private void printStagedFiles() {
        Stage stage = getStage();
        for (String file : stage.getAdd().keySet()) {
            System.out.println(file);
        }
    }

    private void printRemoveFiles() {
        Stage stage = getStage();
        for (String file : stage.getRemove()) {
            System.out.println(file);
        }
    }

    /** A file in the working directory is “modified but not staged” if it is:
     *  1. Tracked in the current commit, changed in the working directory, but not staged.
     *  2. Staged for addition, but with different contents than in the working directory.
     *  3. Staged for addition, but deleted in the working directory.
     *  4. Not staged for removal, but tracked in the current commit and deleted from the working directory.
     */
    private void printNotStagedFiles() {
        System.out.println();
        List<String> condition_1 = getModifiedFiles();
        List<String> condition_2_3 = getFilesDiffinAdd_CWD();
        List<String> condition_4 = notStagedForRemove();
        List<String> finalList = new ArrayList<>();
        finalList.addAll(condition_1);
        finalList.addAll(condition_2_3);
        finalList.addAll(condition_4);
        Collections.sort(finalList);
        for (String file : finalList) {
            System.out.println(file);
        }
    }

    // 1
    private List<String> getModifiedFiles() {
        Commit headCommit = getHead();
        Stage stage = getStage();
        List<String> modifiedFiles = new ArrayList<String>();
        // <fileName, blobId>
        for (Map.Entry<String, String> entry : headCommit.getBlobs().entrySet()) {
            File fileInCWD = join(CWD, entry.getKey());
            byte[] CWDbyte = readContents(fileInCWD);
            byte[] bytesInBlob = getBlobFileFromId(entry.getValue());
            if (!Arrays.equals(bytesInBlob, CWDbyte)) {
                if (!stage.ifExistInAdd(entry.getKey())) {
                    modifiedFiles.add(entry.getKey());
                }
            }
        }
        return modifiedFiles;
    }

    // 2, 3
    private List<String> getFilesDiffinAdd_CWD() {
        List<String> diffFiles = new ArrayList<>();
        Stage stage = getStage();
        HashMap<String, String> addition = stage.getAdd();
        for (Map.Entry<String, String> entry : addition.entrySet()) {
            String fileName = entry.getKey();
            File fileInCWD = join(CWD, fileName);
            byte[] CWDbyte = readContents(fileInCWD);
            byte[] bytesInBlob = getBlobFileFromId(entry.getValue());
            if (!fileInCWD.exists()) {
                diffFiles.add(fileName);
            } else if (!Arrays.equals(bytesInBlob, CWDbyte)) {
                diffFiles.add(fileName);
            }
        }
        return diffFiles;
    }

    // 4. 4. Not staged for removal, but tracked in the current commit and deleted from the working directory.
    private List<String> notStagedForRemove() {
        List<String> notStagedFiles = new ArrayList<>();
        Stage stage = getStage();
        Commit headCommit = getHead();
        for (Map.Entry<String, String> entry : headCommit.getBlobs().entrySet()) {
            File fileInCWD = join(CWD, entry.getKey());
            if (!fileInCWD.exists() && !stage.ifExistInRemove(entry.getKey())) {
                notStagedFiles.add(entry.getKey());
            }
        }
        return notStagedFiles;
    }

    private void printUntrackedFiles() {
        Stage stage = getStage();
        Commit headCommit = getHead();
        List<String> files = plainFilenamesIn(CWD);
        for (String file : files) {
            if (!stage.ifExistInAdd(file) && !headCommit.ifInBlobs(file)) {
                System.out.println(file);
            }
        }
    }

    private boolean ifUntrackedFileExist(Commit commit) {
        List<String> files = plainFilenamesIn(CWD);
        for (String file : files) {
            if (!commit.ifInBlobs(file)) {
                return true;
            }
        }
        return false;
    }

    public void checkEqual(String actual, String expected) {
        if (!actual.equals(expected)) {
            msgIncorrectOperands();
        }
    }

    private Commit getCommitFromBranch(String branchName) {
        File branchFile = getBranchFile(branchName);
        String commitID = readContentsAsString(branchFile);
        Commit commit = getCommitFromId(commitID);
        return commit;
    }

    private void replaceCWDWithCommit(Commit commit) {
        clearWorkingSpace();

        for (Map.Entry<String, String> item: commit.getBlobs().entrySet()) {
            String fileName = item.getKey();
            String blobId = item.getValue();
            File file = join(CWD, fileName);
            writeContents(file, getBlobFileFromId(blobId));
        }
    }

    private void clearWorkingSpace() {
        File[] files = CWD.listFiles();
        for (File file : files) {
            file.delete();
        }
    }

}
