package net.optionfactory.journalwebd.client;

import java.util.Spliterator;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

public class BlockingQueueIterator<T> implements Spliterator<T> {

    private final BlockingQueue<NetworkResult<T>> q;

    public BlockingQueueIterator(BlockingQueue<NetworkResult<T>> q) {
        this.q = q;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        try {
            final var got = q.take();
            if (got.done()) {
                return false;
            }
            if (got.ex() != null) {
                throw got.ex();
            }
            action.accept(got.value());
            return true;
        } catch (InterruptedException ex) {
            throw new JournalClientException(ex);
        }
    }

    @Override
    public Spliterator<T> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return Spliterator.ORDERED;
    }

}
