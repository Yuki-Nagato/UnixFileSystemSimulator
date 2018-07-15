public class InodeOverflowException extends Exception {
    public InodeOverflowException() {
        super("Inode空间不足");
    }
}
