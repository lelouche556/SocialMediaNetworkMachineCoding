/**
 * Observer interface for real-time feed updates
 * Design Pattern: Observer Pattern
 */
public interface FeedObserver {
    void onNewPost(Post post, String followerId);
    void onPostDeleted(String postId, String followerId);
}

