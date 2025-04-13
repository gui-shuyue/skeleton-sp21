package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author gsy
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
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
                break;
            // TODO: FILL THE REST IN
        }
    }
}
