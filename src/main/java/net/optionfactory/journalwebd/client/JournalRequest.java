package net.optionfactory.journalwebd.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.lang.Nullable;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public record JournalRequest(
        @Nullable
        String filter,
        @Nullable
        String[] units,
        @Nullable
        String[] hosts,
        @Nullable
        RangeLines rangeLines,
        @Nullable
        RangePeriod rangePeriod,
        @Nullable
        RangeMinutes rangeMinutes) {

    public record RangeLines(int lines, boolean follow) {

    }

    public record RangePeriod(String since, String until) {

    }

    public record RangeMinutes(int minutes, boolean follow) {

    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String filter;
        private String[] units;
        private String[] hosts;

        public Builder filter(String value) {
            this.filter = value;
            return this;
        }

        public Builder units(String... values) {
            this.units = values;
            return this;
        }

        public Builder hosts(String... values) {
            this.hosts = values;
            return this;
        }

        public JournalRequest period(String since, String until) {
            return new JournalRequest(filter, units, hosts, null, new RangePeriod(since, until), null);
        }

        public JournalRequest minutes(int minutes, boolean follow) {
            return new JournalRequest(filter, units, hosts, null, null, new RangeMinutes(minutes, follow));
        }

        public JournalRequest lines(int lines, boolean follow) {
            return new JournalRequest(filter, units, hosts, new RangeLines(lines, follow), null, null);
        }

    }

}
