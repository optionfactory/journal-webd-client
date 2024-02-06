package net.optionfactory.journalwebd.client;


import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.junit.Ignore;
import org.junit.Test;

public class StreamingJournalClientExampleTest {

    @Test
    @Ignore
    public void canCallJournalWebd() throws InterruptedException, ExecutionException {
        final var token = "TEST_TOKEN";
        final var entries = new StreamingJournalClient(URI.create("ws://localhost:8000/ws/stream"), token)
                .stream(JournalRequest.builder().lines(10, false))
                .map(JournalEntry::toString)
                .collect(Collectors.joining("\r\n"));
        System.out.format("results: %s%n", entries);
    }

}
