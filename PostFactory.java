import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Factory for creating Post objects
 * Design Pattern: Factory Pattern
 */
public class PostFactory {
    private static final AtomicInteger counter = new AtomicInteger(1);
    
    public static Post createPost(String userId, String content) {
        String postId = "POST_" + counter.getAndIncrement();
        return new Post(postId, userId, content);
    }
    
    public static Post createPost(String postId, String userId, String content, LocalDateTime timestamp) {
        return new Post(postId, userId, content, timestamp);
    }
}

