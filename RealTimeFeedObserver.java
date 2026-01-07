import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Real-time feed observer implementation
 * Design Pattern: Observer Pattern
 */
public class RealTimeFeedObserver implements FeedObserver {
    private String userId;
    private BlockingQueue<Post> feedQueue;
    private volatile boolean active;
    
    public RealTimeFeedObserver(String userId) {
        this.userId = userId;
        this.feedQueue = new LinkedBlockingQueue<>();
        this.active = true;
    }
    
    @Override
    public void onNewPost(Post post, String followerId) {
        if (active && userId.equals(followerId)) {
            feedQueue.offer(post);
            System.out.println("[REALTIME] User " + userId + " received new post from " + post.getUserId() + ": " + post.getContent());
        }
    }
    
    @Override
    public void onPostDeleted(String postId, String followerId) {
        if (active && userId.equals(followerId)) {
            System.out.println("[REALTIME] User " + userId + " notified: Post " + postId + " was deleted");
        }
    }
    
    public Post getNextPost() throws InterruptedException {
        return feedQueue.take();
    }
    
    public Post pollPost() {
        return feedQueue.poll();
    }
    
    public void stop() {
        this.active = false;
    }
    
    public boolean isActive() {
        return active;
    }
}

