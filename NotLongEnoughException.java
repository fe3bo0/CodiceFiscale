package cf;
public class NotLongEnoughException extends Exception{
    public NotLongEnoughException() {super();}
    public NotLongEnoughException(String message) {super(message);}
    public NotLongEnoughException(String message, Throwable cause) {super(message, cause);}
    public NotLongEnoughException(Throwable cause) {super(cause);}
}