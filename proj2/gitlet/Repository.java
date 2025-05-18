package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;


import static gitlet.Utils.*;
import static java.nio.file.Files.delete;


/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Gui
 */
public class Repository {
    /**
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

    private boolean conflictFlag = false;
    public Repository() {
        configDirs();  // 在 new Repo() 时自动跑一遍
    }

    public void init() {
        if (GITLET_DIR.exists() && GITLET_DIR.isDirectory()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdirs();
        COMMITS_DIR.mkdirs();
        BLOBS_DIR.mkdirs();
        REFS_DIR.mkdirs();
        HEADS_DIR.mkdirs();
        Stage stage = new Stage();
        writeObject(STAGE, stage);

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
        this.COMMITS_DIR = join(GITLET_DIR, "commits");
        this.BLOBS_DIR = join(GITLET_DIR, "blobs");
        this.REFS_DIR = join(GITLET_DIR, "refs");
        this.HEADS_DIR = join(REFS_DIR, "heads");
        this.HEAD = join(GITLET_DIR, "HEAD");
        this.STAGE = join(GITLET_DIR, "stage");


    }

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
                        writeStageToFile(stage);
                        return;
                    }
                    return;
                } else {
                    // Staging an already-staged file overwrites the previous entry in the staging area with
                    // the new contents.
                    stage.addFile(filename, fileToBlob.getId());
                    writeBlobToFile(fileToBlob);
                    writeStageToFile(stage);
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
        if (checkIfStageClear()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        if (message.isEmpty()) {
            System.out.println("Please enter a commit message.");
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
     *  already done so (do not remove it unless it is tracked in the current commit)
     *  文件刚被add进addstage而没有commit，直接删除addstage中的Blob就可以。
     *
     * 文件被当前Commit追踪并且存在于工作目录中，那么就将及放入removestage并且在工作目录中删除此文件。在下次commit中进行记录。
     *
     * 文件被当前Commit追踪并且不存在于工作目录中，那么就将及放入removestage并即可（commit之后手动删除文件，然后执行rm，第二次rm就对应这种情况）。*/
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
            if (file.exists()) {
                stage.removeFile(filename);
                delete(file.toPath());
            } else {
                stage.removeFile(filename);
            }

        } else if (stage.ifExistInAdd(filename)) {
            stage.getAdd().remove(filename);
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
            String parentId = commit.getFirstPaId();
            if (parentId == null) {
                break;
            }
            commit = getCommitFromId(parentId);
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
                return;
            }
        }
        System.out.println("Found no commit with that message.");
    }

    /** Displays what branches currently exist, and marks the current branch with a *.
     * Also displays what files have been staged for addition or removal.*/
    public void status() {
        checkIfInitialized();
        System.out.println("=== Branches ===");
        printBranches();
        System.out.println();
        System.out.println("=== Staged Files ===");
        printStagedFiles();
        System.out.println();
        System.out.println("=== Removed Files ===");
        printRemoveFiles();
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        printNotStagedFiles();
        System.out.println();
        System.out.println("=== Untracked Files ===");
        printUntrackedFiles();
        System.out.println();
    }



    /** Takes all files in the commit at the head of the given branch, and puts them in the
     * working directory, overwriting the versions of the files that are already there if they exist.*/
    public void checkoutBranch(String branch) throws IOException {
        File branchFile = getBranchFile(branch);
        if (!branchFile.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        Commit headCommit = getHead();

        String headBranchName = getHeadBranchName();
        if (headBranchName.equals(branch)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        Stage stage = getStage();
        Commit givenCommit = getCommitFromBranch(branch);

        // Any files that are tracked in the current branch but are not present in the checked-out branch are deleted.
        for (String fname : Utils.plainFilenamesIn(CWD)) {
            boolean here = headCommit.ifInBlobs(fname);
            boolean there = givenCommit.ifInBlobs(fname);
            // 只有「当前没跟踪、目标有跟踪」才阻塞
            if (!here && there && !stage.ifExistInAdd(fname)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            }
        }


        clearStage();
        checkoutCommit(givenCommit);
        writeContents(HEAD, branch);

    }

    /** Takes the version of the file as it exists in the head commit and puts it in the working directory, overwriting
     * the version of the file that’s already there if there is one. The new version of the file is not staged.*/
    public void checkoutFile(String filename) {
        checkIfInitialized();

        Commit headCommit = getHead();
        if (!headCommit.ifInBlobs(filename)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        File file = join(CWD, filename);
        String blobId = headCommit.getBlobs().get(filename);
        writeContents(file, getBlobFileFromId(blobId));
    }

    /** Takes the version of the file as it exists in the commit with the given id, and puts it in the working
     * directory, overwriting the version of the file that’s already there if there is one. The new version of the
     * file is not staged.
     * Note that the commitId in args is shortened.*/
    public void checkFileFromCommitId(String commitId, String filename) {
        checkIfInitialized();
        commitId = getCompleteCommitId(commitId);
        File commitFile = join(COMMITS_DIR, commitId);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        Commit commit = getCommitFromId(commitId);
        if (!commit.ifInBlobs(filename)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        String blobId = commit.getBlobs().get(filename);
        File file = join(CWD, filename);
        writeContents(file, getBlobFileFromId(blobId));
    }

    /** Creates a new branch with the given name, and points it at the current head commit.*/
    public void branch(String branch) {
        checkIfInitialized();

        File branchFile = join(HEADS_DIR, branch);
        if (branchFile.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }

        Commit headCommit = getHead();
        String headCommitId = headCommit.getID();
        writeContents(branchFile, headCommitId);
    }

    /** Deletes the branch with the given name. This only means to delete the pointer associated with the branch;
     * it does not mean to delete all commits that were created under the branch, or anything like that.*/
    public  void removeBranch(String branch) {
         File branchFile = join(HEADS_DIR, branch);
         if (!branchFile.exists()) {
             System.out.println("A branch with that name does not exist.");
             System.exit(0);
         }
         String headBranchName = getHeadBranchName();
         if (headBranchName.equals(branch)) {
             System.out.println("Cannot remove the current branch.");
             System.exit(0);
         }

         branchFile.delete();
    }

    public void reset(String commitId) throws IOException {
        checkIfInitialized();
        commitId = getCompleteCommitId(commitId);
        Commit headCommit = getHead();
        File commitFile = join(COMMITS_DIR, commitId);

        Stage stage = getStage();
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit targetCommit = getCommitFromId(commitId);
        for (String fname : Utils.plainFilenamesIn(CWD)) {
            boolean here = headCommit.ifInBlobs(fname);
            boolean there = targetCommit.ifInBlobs(fname);
            // 只有「当前没跟踪、目标有跟踪」才阻塞
            if (!here && there && !stage.ifExistInAdd(fname)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        clearStage();
        checkoutCommit(targetCommit);

        String headBranchName = getHeadBranchName();
        writeContents(join(HEADS_DIR, headBranchName), commitId);
    }

    public void merge(String branch) throws IOException {
        checkIfInitialized();
        if (!checkIfStageClear()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        File branchFile = join(HEADS_DIR, branch);
        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        String headBranch = getHeadBranchName();
        if (headBranch.equals(branch)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        Commit headCommit = getHead();
        if (ifUntrackedFileExist(headCommit)) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }

        Commit givenCommit = getCommitFromBranch(branch);
        Commit splitCommit = findSplitPoint(headCommit, givenCommit);

        //System.out.printf("DEBUG: split=%s   head=%s   given=%s\n",
                //splitCommit.getID(), headCommit.getID(), givenCommit.getID());

        // if (splitCommit.equals(headCommit))
        if (splitCommit.getID().equals(headCommit.getID())){
            System.out.println("Current branch fast-forwarded.");
            clearStage();
            checkoutCommit(givenCommit);
            writeContents(HEAD, branch);
            System.exit(0);
        }
        //if (splitCommit.equals(givenCommit)) {
        if (splitCommit.getID().equals(givenCommit.getID())){
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }

        String message = "Merged " + branch + " into " + headBranch + ".";
        List<Commit> parents = new ArrayList<>(List.of(headCommit, givenCommit));
        Stage stage = calculateNewStage(splitCommit, headCommit, givenCommit);

        if (conflictFlag == true) {
            System.out.println("Encountered a merge conflict.");
        }
        
        Commit newCommit = new Commit(message, parents, stage);
        writeCommitToFile(newCommit);
        File headBranchFile = getBranchFile(headBranch);
        writeContents(headBranchFile, newCommit.getID());
        checkoutCommit(newCommit);

    }

    private void checkoutCommit(Commit commit) {
        Commit headCommit = getHead();
        for (String fname : headCommit.getBlobs().keySet()) {
            if (!commit.getBlobs().containsKey(fname)) {
                Utils.restrictedDelete(Utils.join(CWD, fname));
            }
        }
        for (Map.Entry<String, String> e : commit.getBlobs().entrySet()) {
            String fileName = e.getKey();
            String blobId   = e.getValue();
            byte[] data     = getBlobFileFromId(blobId);
            File f = join(CWD, fileName);
            writeContents(f, data);
        }
    }
    private Stage calculateNewStage(Commit splitCommit, Commit headCommit, Commit givenCommit) {
        List<String> allFiles = calculateAllFiles(splitCommit, headCommit, givenCommit);
        Stage stage = new Stage();
        HashMap<String, String> splitCommitMap = splitCommit.getBlobs();
        HashMap<String, String> headCommitMap = headCommit.getBlobs();
        HashMap<String, String> givenCommitMap = givenCommit.getBlobs();
        for (String file : allFiles) {
            if (splitCommitMap.containsKey(file)) {

                if (splitCommitMap.get(file).equals(headCommitMap.get(file))) {
                    // 6.unmodified in HEAD but not present in given -> remove
                    if (!givenCommitMap.containsKey(file)) {
                        stage.removeFile(file);
                        continue;
                    }
                    // 1.modified in given but not HEAD -> given
                    else if (!splitCommitMap.get(file).equals(givenCommitMap.get(file))) {
                        stage.addFile(file, givenCommitMap.get(file));
                    }
                }

                if (splitCommitMap.get(file).equals(givenCommitMap.get(file))) {
                    // 7.unmodified in given but not present in HEAD ->
                    if (!headCommitMap.containsKey(file)) {
                        continue;
                    }
                    // 2.modified in HEAD but not given -> HEAD
                    else if (!splitCommitMap.get(file).equals(headCommitMap.get(file))) {
                        stage.addFile(file, headCommitMap.get(file));
                    }

                }
                // 3.modified in both commits
                else {
                    if (headCommitMap.get(file).equals(givenCommitMap.get(file))) {
                        stage.addFile(file, givenCommitMap.get(file));
                    }
                    else {
                        String headId = headCommitMap.get(file);
                        String givenId = givenCommitMap.get(file);
                        dealWithConflict(file, headId, givenId, stage);
                        conflictFlag = true;
                    }

                }

            }
            else {
                // 4. not in split nor other but in HEAD -> HEAD
                if (!givenCommitMap.containsKey(file) && headCommitMap.containsKey(file)) {
                    stage.addFile(file, headCommitMap.get(file));
                }
                // 5. not in split nor HEAD but in given -> given
                else if (!headCommitMap.containsKey(file) && givenCommitMap.containsKey(file)) {
                    stage.addFile(file, givenCommitMap.get(file));
                }
            }

        }
        return stage;
    }

    private void dealWithConflict(String file, String headId, String givenId, Stage stage) {
        byte[] headBytes  = (headId  == null ? new byte[0] : getBlobFileFromId(headId));
        byte[] givenBytes = (givenId == null ? new byte[0] : getBlobFileFromId(givenId));
        String headContent = new String(headBytes);
        String givenContent = new String(givenBytes);

        StringBuilder sb = new StringBuilder();
        sb.append("<<<<<<< HEAD\n");
        sb.append(headContent);
        sb.append("=======\n");
        sb.append(givenContent);
        sb.append(">>>>>>>\n");
        String conflictText = sb.toString();
        byte[] conflictBytes = conflictText.getBytes();

        File f = join(CWD, file);
        writeContents(f, conflictBytes);

        Blob newBlob = new Blob(file, CWD);
        writeBlobToFile(newBlob);
        stage.addFile(file, newBlob.getId());
    }

    // get all files' names have to be considered.
    private List<String> calculateAllFiles(Commit splitCommit, Commit headCommit, Commit givenCommit) {
        List<String> allFiles = new ArrayList<String>(splitCommit.getBlobs().keySet());
        allFiles.addAll(headCommit.getBlobs().keySet());
        allFiles.addAll(givenCommit.getBlobs().keySet());
        Set<String> set = new HashSet<>(allFiles);
        allFiles.clear();
        allFiles.addAll(set);
        return allFiles;
    }

    // Depths of commits from a head of a branch Map<commitId, depth>
    private Map<String, Integer> calculateCommitMap(Commit commit, int length) {
        Map<String, Integer> commitMap = new HashMap<>();
        if (commit.getParents().isEmpty()) {
            commitMap.put(commit.getID(), length);
            return commitMap;
        }

        commitMap.put(commit.getID(), length);
        length++;
        for (String commitId : commit.getParents()) {
            Commit parent = getCommitFromId(commitId);
            commitMap.putAll(calculateCommitMap(parent, length));
        }
        return commitMap;
    }

    private Commit calculateSplitPoint(Map<String, Integer> map1, Map<String, Integer> map2) {
        int minLength = Integer.MAX_VALUE;
        String minId = "";
        for (String id : map1.keySet()) {
            if (map2.containsKey(id) && map2.get(id) < minLength) {
                minLength = map2.get(id);
                minId = id;
            }
        }
        Commit splitPoint = getCommitFromId(minId);
        return splitPoint;
    }

    private Commit findSplitPoint(Commit commit1, Commit commit2) {
        Map<String, Integer> map1 = calculateCommitMap(commit1, 0);
        Map<String, Integer> map2 = calculateCommitMap(commit2, 0);
        return calculateSplitPoint(map1, map2);
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
        writeObject(STAGE, new Stage());
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
            String fileName = entry.getKey();
            File fileInCWD = join(CWD, fileName);
            String blobId = entry.getValue();

            if (!fileInCWD.exists()) {
                // 工作区里文件被删了，但如果它已经被 staged for removal，就跳过；
                // 否则算作“未暂存的修改”里的“删除”情况
                if (!stage.ifExistInRemove(fileName)) {
                    modifiedFiles.add(fileName);
                }
            } else {
                // 文件确实存在，才去读内容比较
                byte[] cwdBytes  = readContents(fileInCWD);
                byte[] blobBytes = getBlobFileFromId(blobId);
                if (!Arrays.equals(cwdBytes, blobBytes)
                        && !stage.ifExistInAdd(fileName)) {
                    modifiedFiles.add(fileName);
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
        for (Map.Entry<String, String> entry : stage.getAdd().entrySet()) {
            String fileName = entry.getKey();
            File fileInCWD = join(CWD, fileName);
            if (!fileInCWD.exists()) {
                // 被 staged for addition 后又被删掉
                diffFiles.add(fileName);
            } else {
                byte[] cwdBytes  = readContents(fileInCWD);
                byte[] blobBytes = getBlobFileFromId(entry.getValue());
                if (!Arrays.equals(cwdBytes, blobBytes)) {
                    // 文件内容被修改但没重新 add
                    diffFiles.add(fileName);
                }
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
            if (stage.ifExistInRemove(file) || (!stage.ifExistInAdd(file) && !headCommit.ifInBlobs(file))) {
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

    private String getCompleteCommitId(String commitId) {
        if (commitId.length() == UID_LENGTH) {
            return commitId;
        }

        for (String filename : COMMITS_DIR.list()) {
            if (filename.startsWith(commitId)) {
                return filename;
            }
        }
        return null;
    }

    private boolean checkIfStageClear() {
        Stage stage = getStage();
        if (stage.getAdd().isEmpty() && stage.getRemove().isEmpty()) {
            return true;
        }
        return false;
    }

}
