public class PermissionDeniedException extends Exception {
    public PermissionDeniedException() {
        super("权限不足");
    }
}
