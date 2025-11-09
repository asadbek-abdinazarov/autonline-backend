package uz.javachi.autonline.exceptions;

public class UserBlockedOrDeletedException extends RuntimeException {
    public  UserBlockedOrDeletedException(String message) {
        super(message);
    }
}
