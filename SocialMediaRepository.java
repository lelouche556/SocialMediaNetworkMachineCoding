import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Repository for data access
 * Design Pattern: Repository Pattern
 */
public class SocialMediaRepository {
    private final Map<String, User> users;
    private final Map<String, Post> posts;
    private final Map<String, List<String>> userPosts; // userId -> List of postIds
    
    public SocialMediaRepository() {
        this.users = new ConcurrentHashMap<>();
        this.posts = new ConcurrentHashMap<>();
        this.userPosts = new ConcurrentHashMap<>();
    }
    
    public void addUser(User user) {
        users.put(user.getUserId(), user);
        userPosts.putIfAbsent(user.getUserId(), new CopyOnWriteArrayList<>());
    }
    
    public User getUser(String userId) {
        return users.get(userId);
    }
    
    public boolean userExists(String userId) {
        return users.containsKey(userId);
    }
    
    public void addPost(Post post) {
        posts.put(post.getPostId(), post);
        List<String> postsList = userPosts.get(post.getUserId());
        if (postsList != null) {
            postsList.add(post.getPostId());
        }
    }
    
    public Post getPost(String postId) {
        return posts.get(postId);
    }
    
    public boolean postExists(String postId) {
        return posts.containsKey(postId);
    }
    
    public void removePost(String postId) {
        Post post = posts.remove(postId);
        if (post != null) {
            List<String> postsList = userPosts.get(post.getUserId());
            if (postsList != null) {
                postsList.remove(postId);
            }
        }
    }
    
    public List<String> getUserPostIds(String userId) {
        return userPosts.getOrDefault(userId, new CopyOnWriteArrayList<>());
    }
    
    public Map<String, User> getAllUsers() {
        return new ConcurrentHashMap<>(users);
    }
}

