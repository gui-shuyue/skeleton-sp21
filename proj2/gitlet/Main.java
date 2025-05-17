package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author gsy
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        Repository repo = new Repository();
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                repo.checkOperand(args.length, 1);
                repo.init();
                break;
            case "add":
                repo.checkOperand(args.length, 2);
                repo.add(args[1]);
                break;
            case "commit":
                if (args.length < 2) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                }
                repo.checkOperand(args.length, 2);
                repo.commit(args[1]);
                break;
            case "rm":
                repo.checkOperand(args.length, 2);
                repo.rm(args[1]);
                break;
            case "log":
                repo.checkOperand(args.length, 1);
                repo.log();
                break;
            case "global-log":
                repo.checkOperand(args.length, 1);
                repo.global_log();
                break;
            case "find":
                repo.checkOperand(args.length, 2);
                repo.find(args[1]);
                break;
            case "status":
                repo.checkOperand(args.length, 1);
                repo.status();
                break;
            case "checkout":
                int len = args.length;
                if (len < 2 || len > 4) {
                    System.out.println("Incorrect operands");
                    System.exit(0);
                }
                if (len == 2) {
                    // java gitlet.Main checkout [branch name]
                    repo.checkoutBranch(args[1]);
                } else if (len == 3) {
                    // java gitlet.Main checkout -- [file name]
                    repo.checkEqual(args[1], "--");
                    repo.checkoutFile(args[2]);
                } else if (len == 4) {
                    // java gitlet.Main checkout [commit id] -- [file name]
                    repo.checkEqual(args[2], "--");
                    repo.checkFileFromCommitId(args[1], args[3]);
                }
                break;
            case "branch":
                repo.checkOperand(args.length, 2);
                repo.branch(args[1]);
                break;
            case "rm-branch":
                repo.checkOperand(args.length, 2);
                repo.removeBranch(args[1]);
                break;
            case "reset":
                repo.checkOperand(args.length, 2);
                repo.reset(args[1]);
                break;
            case "merge":
                repo.checkOperand(args.length, 2);
                repo.merge(args[1]);
                break;
                // TODO: FILL THE REST IN
        }
    }
}
