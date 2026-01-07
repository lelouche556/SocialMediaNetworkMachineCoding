import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class User {
    private final String userId;
    private final String name;
    private final Set<String> following; // Set of userIds that this user follows (thread-safe)
    
    public User(String userId, String name) {
        this.userId = userId;
        this.name = name;
        this.following = ConcurrentHashMap.newKeySet(); // Thread-safe set
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getName() {
        return name;
    }
    
    public Set<String> getFollowing() {
        return following;
    }
    
    public void follow(String userId) {
        if (!userId.equals(this.userId)) {
            following.add(userId);
        }
    }
    
    public void unfollow(String userId) {
        following.remove(userId);
    }
    
    public boolean isFollowing(String userId) {
        return following.contains(userId);
    }
}

