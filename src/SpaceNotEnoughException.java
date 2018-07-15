public class SpaceNotEnoughException extends Exception {
    public SpaceNotEnoughException() {
        super("磁盘空间不足");
    }
}
