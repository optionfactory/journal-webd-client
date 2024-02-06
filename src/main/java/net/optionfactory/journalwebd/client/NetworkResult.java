package net.optionfactory.journalwebd.client;

public record NetworkResult<T>(boolean done, JournalClientException ex, T value) {

    public static <T> NetworkResult<T> value(T value) {
        return new NetworkResult<>(false, null, value);
    }

    public static <T> NetworkResult<T> closed(Class<T> klass) {
        return new NetworkResult<>(true, null, null);
    }

    public static <T> NetworkResult<T> exception(Throwable t) {
        final var ex = t.getMessage() != null && t.getMessage().contains("Broken pipe")
                ? new JournalClientException("Broken pipe, possibily an authentication failure", t)
                : new JournalClientException(t);
        return new NetworkResult<>(false, ex, null);
    }

}
