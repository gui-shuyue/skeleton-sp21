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
            // TODO: FILL THE REST IN
        }
    }
}
