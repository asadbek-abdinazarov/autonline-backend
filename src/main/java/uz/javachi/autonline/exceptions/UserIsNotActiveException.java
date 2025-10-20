package uz.javachi.autonline.exceptions;

public class UserIsNotActiveException extends RuntimeException{
    public UserIsNotActiveException(String message) {
        super(message);
    }
}
