package co.bitshifted.ignite.exception;

public class CommunicationException extends Exception{

    public CommunicationException(String msg) {
        super(msg);
    }

    public CommunicationException(Throwable th) {
        super(th);
    }
}
