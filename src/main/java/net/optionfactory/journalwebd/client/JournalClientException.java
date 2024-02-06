package net.optionfactory.journalwebd.client;

public class JournalClientException extends IllegalStateException {

    public JournalClientException(String message, Throwable t) {
        super(message, t);
    }

    public JournalClientException(Throwable t) {
        super(t);
    }

    public JournalClientException(String message) {
        super(message);
    }

}
