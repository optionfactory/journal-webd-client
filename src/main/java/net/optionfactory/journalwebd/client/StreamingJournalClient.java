package net.optionfactory.journalwebd.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import jakarta.websocket.ContainerProvider;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

public class StreamingJournalClient {

    private final Configuration conf;
    private final StandardWebSocketClient client;
    private final ObjectMapper om;

    public record Configuration(URI uri, String bearerToken, Duration sendTimeout, Duration idleSessionTimeout, int maxBinaryBufferSize, int maxTextBufferSize) {

        public static Configuration withDefaults(URI uri, String bearerToken) {
            return new Configuration(
                    uri,
                    bearerToken,
                    Duration.ofSeconds(1),
                    Duration.ofSeconds(30),
                    4096,
                    72 * 1024);

        }
    }

    public StreamingJournalClient(Configuration conf) {
        this.conf = conf;
        final var container = ContainerProvider.getWebSocketContainer();
        container.setAsyncSendTimeout(conf.sendTimeout().toMillis());
        container.setDefaultMaxSessionIdleTimeout(conf.idleSessionTimeout().toMillis());
        container.setDefaultMaxBinaryMessageBufferSize(conf.maxBinaryBufferSize());
        container.setDefaultMaxTextMessageBufferSize(conf.maxTextBufferSize());
        this.client = new StandardWebSocketClient(container);
        this.om = new ObjectMapper();
        om.addMixIn(JournalEntry.class, JournalEntryMixin.class);
    }

    public Stream<JournalEntry> stream(JournalRequest request) {
        final WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add("Authorization", String.format("Bearer %s", conf.bearerToken()));

        final var q = new LinkedBlockingQueue<NetworkResult<JournalEntry>>(/*unlimited*/);

        CompletableFuture<WebSocketSession> session = client.execute(new WebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                final var serialized = om.writeValueAsString(request);
                session.sendMessage(new TextMessage(serialized));
            }

            @Override
            public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
                try {
                    q.add(NetworkResult.value(om.readValue((String) message.getPayload(), JournalEntry.class)));
                } catch (Exception ex) {
                    q.add(NetworkResult.exception(ex));
                }
            }

            @Override
            public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
                q.add(NetworkResult.exception(exception));
            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                if (closeStatus.getCode() != CloseStatus.NORMAL.getCode()) {
                    final var ex = new IllegalStateException(String.format("journaclient error: %s: %s", closeStatus.getCode(), closeStatus.getReason()));
                    q.add(NetworkResult.exception(ex));
                    return;
                }
                q.add(NetworkResult.closed(JournalEntry.class));
            }

            @Override
            public boolean supportsPartialMessages() {
                return false;
            }
        }, headers, conf.uri());
        session.whenComplete((sess, ex) -> {
            if (ex != null) {
                q.add(NetworkResult.exception(ex));
            }
        });

        return StreamSupport.stream(new BlockingQueueIterator<>(q), false);
    }

}
