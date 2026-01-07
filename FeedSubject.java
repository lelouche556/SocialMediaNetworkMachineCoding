/**
 * Subject interface for Observer pattern
 * Design Pattern: Observer Pattern
 */
public interface FeedSubject {
    void registerObserver(FeedObserver observer, String userId);
    void unregisterObserver(FeedObserver observer, String userId);
    void notifyObservers(Post post, String authorId);
    void notifyPostDeleted(String postId, String authorId);
}

