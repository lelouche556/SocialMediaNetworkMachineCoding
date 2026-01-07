import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Social Media Network System (Scalable & Concurrent) ===\n");
        
        SocialMediaNetwork network = new SocialMediaNetwork();
        
        // Create users
        System.out.println("Creating users...");
        network.createUser("user1", "Alice");
        network.createUser("user2", "Bob");
        network.createUser("user3", "Charlie");
        network.createUser("user4", "Diana");
        System.out.println("Users created successfully!\n");
        
        // Set up real-time feed observers
        System.out.println("Setting up real-time feed observers...");
        RealTimeFeedObserver observer1 = new RealTimeFeedObserver("user1");
        RealTimeFeedObserver observer2 = new RealTimeFeedObserver("user2");
        RealTimeFeedObserver observer3 = new RealTimeFeedObserver("user3");
        
        network.registerObserver(observer1, "user1");
        network.registerObserver(observer2, "user2");
        network.registerObserver(observer3, "user3");
        System.out.println("Real-time observers registered!\n");
        
        // Follow users
        System.out.println("Setting up follow relationships...");
        network.followUser("user1", "user2");
        System.out.println("User1 followed User2");
        
        network.followUser("user1", "user3");
        System.out.println("User1 followed User3");
        
        network.followUser("user2", "user1");
        System.out.println("User2 followed User1");
        
        network.followUser("user2", "user4");
        System.out.println("User2 followed User4");
        
        network.followUser("user3", "user1");
        System.out.println("User3 followed User1");
        System.out.println();
        
        // Upload initial posts
        System.out.println("Uploading initial posts...");
        String post1 = network.uploadPost("user1", "Hello world! This is my first post.");
        System.out.println("User1 uploaded post: " + post1);
        
        String post2 = network.uploadPost("user2", "Just finished a great workout!");
        System.out.println("User2 uploaded post: " + post2);
        
        String post3 = network.uploadPost("user3", "Working on a new project.");
        System.out.println("User3 uploaded post: " + post3);
        
        // Wait a bit for async notifications
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println();
        
        // Demonstrate concurrent operations
        System.out.println("=== Demonstrating Concurrent Operations ===");
        CountDownLatch latch = new CountDownLatch(3);
        
        // Thread 1: User1 uploads multiple posts concurrently
        new Thread(() -> {
            try {
                for (int i = 1; i <= 3; i++) {
                    String postId = network.uploadPost("user1", "Concurrent post " + i + " from Alice");
                    System.out.println("[THREAD-1] User1 uploaded: " + postId);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        }).start();
        
        // Thread 2: User2 uploads multiple posts concurrently
        new Thread(() -> {
            try {
                for (int i = 1; i <= 3; i++) {
                    String postId = network.uploadPost("user2", "Concurrent post " + i + " from Bob");
                    System.out.println("[THREAD-2] User2 uploaded: " + postId);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        }).start();
        
        // Thread 3: User3 reads feed concurrently
        new Thread(() -> {
            try {
                for (int i = 0; i < 3; i++) {
                    List<Post> feed = network.getFeed("user3");
                    System.out.println("[THREAD-3] User3 feed size: " + feed.size());
                    Thread.sleep(150);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        }).start();
        
        // Wait for all threads to complete
        try {
            latch.await(5, TimeUnit.SECONDS);
            Thread.sleep(500); // Wait for async notifications
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println();
        
        // Get feed for user1
        System.out.println("=== Feed for User1 (Alice) ===");
        List<Post> feed1 = network.getFeed("user1");
        for (int i = 0; i < feed1.size(); i++) {
            Post post = feed1.get(i);
            System.out.println((i + 1) + ". [" + post.getUserId() + "] " + post.getContent() + 
                             " (Posted at: " + post.getTimestamp() + ")");
        }
        System.out.println();
        
        // Get feed for user2
        System.out.println("=== Feed for User2 (Bob) ===");
        List<Post> feed2 = network.getFeed("user2");
        for (int i = 0; i < feed2.size(); i++) {
            Post post = feed2.get(i);
            System.out.println((i + 1) + ". [" + post.getUserId() + "] " + post.getContent() + 
                             " (Posted at: " + post.getTimestamp() + ")");
        }
        System.out.println();
        
        // Demonstrate real-time feed updates
        System.out.println("=== Demonstrating Real-Time Feed Updates ===");
        System.out.println("User2 will post, and User1 should receive it in real-time...\n");
        
        // Start a thread to listen for real-time updates
        CountDownLatch realtimeLatch = new CountDownLatch(2);
        
        new Thread(() -> {
            try {
                System.out.println("[REALTIME-LISTENER] User1 waiting for new posts...");
                for (int i = 0; i < 2; i++) {
                    Post newPost = observer1.pollPost();
                    if (newPost != null) {
                        System.out.println("[REALTIME-LISTENER] User1 received: " + newPost.getContent());
                        realtimeLatch.countDown();
                    } else {
                        Thread.sleep(100);
                        i--; // Retry
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        
        // Post new content that should trigger real-time updates
        try {
            Thread.sleep(200);
            String newPost1 = network.uploadPost("user2", "Real-time update: Just posted this!");
            System.out.println("User2 posted: " + newPost1);
            
            Thread.sleep(200);
            String newPost2 = network.uploadPost("user1", "Real-time update: Alice's new post!");
            System.out.println("User1 posted: " + newPost2);
            
            Thread.sleep(500); // Wait for notifications
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println();
        
        // Delete a post
        System.out.println("Deleting post...");
        boolean deleted = network.deletePost("user1", post1);
        System.out.println("Post " + post1 + " deleted: " + deleted);
        
        try {
            Thread.sleep(300); // Wait for deletion notification
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println();
        
        // Get feed after deletion
        System.out.println("=== Feed for User1 after deletion ===");
        List<Post> feedAfterDelete = network.getFeed("user1");
        System.out.println("Feed size: " + feedAfterDelete.size());
        for (int i = 0; i < Math.min(5, feedAfterDelete.size()); i++) {
            Post post = feedAfterDelete.get(i);
            System.out.println((i + 1) + ". [" + post.getUserId() + "] " + post.getContent());
        }
        System.out.println();
        
        // Unfollow a user
        System.out.println("Unfollowing user...");
        network.unfollowUser("user1", "user3");
        System.out.println("User1 unfollowed User3");
        System.out.println();
        
        // Get feed after unfollow
        System.out.println("=== Feed for User1 after unfollowing User3 ===");
        List<Post> feedAfterUnfollow = network.getFeed("user1");
        System.out.println("Feed size: " + feedAfterUnfollow.size());
        for (int i = 0; i < Math.min(5, feedAfterUnfollow.size()); i++) {
            Post post = feedAfterUnfollow.get(i);
            System.out.println((i + 1) + ". [" + post.getUserId() + "] " + post.getContent());
        }
        System.out.println();
        
        // Shutdown the network
        network.shutdown();
        
        System.out.println("=== Demo completed successfully! ===");
        System.out.println("\nDesign Patterns Used:");
        System.out.println("1. Observer Pattern - Real-time feed updates");
        System.out.println("2. Factory Pattern - Post and User creation");
        System.out.println("3. Repository Pattern - Data access abstraction");
        System.out.println("4. Strategy Pattern - Feed generation strategies");
        System.out.println("\nConcurrency Features:");
        System.out.println("1. Thread-safe collections (ConcurrentHashMap, CopyOnWriteArrayList)");
        System.out.println("2. ReadWriteLock for concurrent reads and exclusive writes");
        System.out.println("3. ExecutorService for async operations");
        System.out.println("4. Real-time notifications via Observer pattern");
    }
}
