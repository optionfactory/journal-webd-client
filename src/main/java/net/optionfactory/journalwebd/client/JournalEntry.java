package net.optionfactory.journalwebd.client;

public record JournalEntry(
        String bootId,
        String pid,
        String hostname,
        String systemdUnit,
        String message,
        long timestamp) {

}
