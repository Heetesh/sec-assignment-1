package sec.assignment.app.controller;

/** Exception class for Logging of files error */
public class LoggerException  extends Exception {
    public LoggerException() {
    }

    public LoggerException(String message) {
        super(message);
    }

    public LoggerException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoggerException(Throwable cause) {
        super(cause);
    }

}
