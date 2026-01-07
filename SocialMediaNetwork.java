import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Main Social Media Network System
 * Design Patterns: Observer, Factory, Repository, Strategy
 * Thread-safe and scalable implementation
 */
public class SocialMediaNetwork implements FeedSubject {
    private final SocialMediaRepository repository;
    private final FeedStrategy feedStrategy;
    private final Map<String, List<FeedObserver>> observers; // userId -> List of observers
    private final ExecutorService executorService;
    private final ReadWriteLock lock;
    
    public SocialMediaNetwork() {
        this.repository = new SocialMediaRepository();
        this.feedStrategy = new RecentFeedStrategy(10);
        this.observers = new ConcurrentHashMap<>();
        this.executorService = Executors.newFixedThreadPool(10);
        this.lock = new ReentrantReadWriteLock();
    }
    
    // Create a new user
    public void createUser(String userId, String name) {
        lock.writeLock().lock();
        try {
            if (repository.userExists(userId)) {
                throw new IllegalArgumentException("User already exists: " + userId);
            }
            User user = UserFactory.createUser(userId, name);
            repository.addUser(user);
            observers.putIfAbsent(userId, new CopyOnWriteArrayList<>());
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    // Upload a post
    public String uploadPost(String userId, String content) {
        lock.readLock().lock();
        try {
            if (!repository.userExists(userId)) {
                throw new IllegalArgumentException("User " + userId + " does not exist");
            }
        } finally {
            lock.readLock().unlock();
        }
        
        Post post = PostFactory.createPost(userId, content);
        
        lock.writeLock().lock();
        try {
            if (repository.postExists(post.getPostId())) {
                throw new IllegalArgumentException("Post already posted");
            }
            repository.addPost(post);
        } finally {
            lock.writeLock().unlock();
        }
        
        // Notify observers asynchronously
        notifyObserversAsync(post, userId);
        
        return post.getPostId();
    }
    
    // Delete a post
    public boolean deletePost(String userId, String postId) {
        lock.writeLock().lock();
        try {
            Post post = repository.getPost(postId);
            if (post == null) {
                return false;
            }
            
            if (!post.getUserId().equals(userId)) {
                throw new IllegalArgumentException("User can only delete their own posts");
            }
            
            repository.removePost(postId);
            
            // Notify observers asynchronously
            notifyPostDeletedAsync(postId, userId);
            
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    // Follow a user
    public void followUser(String userId, String followUserId) {
        lock.readLock().lock();
        try {
            if (!repository.userExists(userId)) {
                throw new IllegalArgumentException("User does not exist: " + userId);
            }
            if (!repository.userExists(followUserId)) {
                throw new IllegalArgumentException("User to follow does not exist: " + followUserId);
            }
            if (userId.equals(followUserId)) {
                throw new IllegalArgumentException("User cannot follow themselves");
            }
        } finally {
            lock.readLock().unlock();
        }
        
        User user = repository.getUser(userId);
        if (user != null) {
            user.follow(followUserId);
        }
    }
    
    // Unfollow a user
    public void unfollowUser(String userId, String unfollowUserId) {
        lock.readLock().lock();
        try {
            if (!repository.userExists(userId)) {
                throw new IllegalArgumentException("User does not exist: " + userId);
            }
        } finally {
            lock.readLock().unlock();
        }
        
        User user = repository.getUser(userId);
        if (user != null) {
            user.unfollow(unfollowUserId);
        }
    }
    
    // Get feed - recent 10 posts from user's account and followings' accounts
    public List<Post> getFeed(String userId) {
        lock.readLock().lock();
        try {
            if (!repository.userExists(userId)) {
                throw new IllegalArgumentException("User does not exist: " + userId);
            }
            return feedStrategy.generateFeed(userId, repository);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    // Get user's posts
    public List<Post> getUserPosts(String userId) {
        lock.readLock().lock();
        try {
            if (!repository.userExists(userId)) {
                throw new IllegalArgumentException("User does not exist: " + userId);
            }
            
            List<String> postIds = repository.getUserPostIds(userId);
            return postIds.stream()
                    .map(repository::getPost)
                    .filter(Objects::nonNull)
                    .sorted((p1, p2) -> p2.getTimestamp().compareTo(p1.getTimestamp()))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    // Get user info
    public User getUser(String userId) {
        lock.readLock().lock();
        try {
            return repository.getUser(userId);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    // Observer Pattern Implementation
    @Override
    public void registerObserver(FeedObserver observer, String userId) {
        observers.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(observer);
    }
    
    @Override
    public void unregisterObserver(FeedObserver observer, String userId) {
        List<FeedObserver> userObservers = observers.get(userId);
        if (userObservers != null) {
            userObservers.remove(observer);
        }
    }
    
    @Override
    public void notifyObservers(Post post, String authorId) {
        User author = repository.getUser(authorId);
        if (author == null) return;
        
        // Get all followers of the author
        Set<String> followers = new HashSet<>();
        for (User user : repository.getAllUsers().values()) {
            if (user.isFollowing(authorId)) {
                followers.add(user.getUserId());
            }
        }
        
        // Also notify the author themselves
        followers.add(authorId);
        
        // Notify all observers
        for (String followerId : followers) {
            List<FeedObserver> userObservers = observers.get(followerId);
            if (userObservers != null) {
                for (FeedObserver observer : userObservers) {
                    observer.onNewPost(post, followerId);
                }
            }
        }
    }
    
    @Override
    public void notifyPostDeleted(String postId, String authorId) {
        User author = repository.getUser(authorId);
        if (author == null) return;
        
        // Get all followers of the author
        Set<String> followers = new HashSet<>();
        for (User user : repository.getAllUsers().values()) {
            if (user.isFollowing(authorId)) {
                followers.add(user.getUserId());
            }
        }
        
        // Also notify the author themselves
        followers.add(authorId);
        
        // Notify all observers
        for (String followerId : followers) {
            List<FeedObserver> userObservers = observers.get(followerId);
            if (userObservers != null) {
                for (FeedObserver observer : userObservers) {
                    observer.onPostDeleted(postId, followerId);
                }
            }
        }
    }
    
    // Async notification methods
    private void notifyObserversAsync(Post post, String authorId) {
        executorService.submit(() -> notifyObservers(post, authorId));
    }
    
    private void notifyPostDeletedAsync(String postId, String authorId) {
        executorService.submit(() -> notifyPostDeleted(postId, authorId));
    }
    
    // Shutdown executor service
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
