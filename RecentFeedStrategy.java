import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Strategy for generating recent feed (top 10 posts)
 * Design Pattern: Strategy Pattern
 */
public class RecentFeedStrategy implements FeedStrategy {
    private final int feedLimit;
    
    public RecentFeedStrategy(int feedLimit) {
        this.feedLimit = feedLimit;
    }
    
    @Override
    public List<Post> generateFeed(String userId, SocialMediaRepository repository) {
        User user = repository.getUser(userId);
        if (user == null) {
            return new ArrayList<>();
        }
        
        Set<String> feedUserIds = new HashSet<>();
        feedUserIds.add(userId); // Include own posts
        feedUserIds.addAll(user.getFollowing()); // Include followings' posts
        
        List<Post> feedPosts = new ArrayList<>();
        
        // Collect all posts from user and their followings
        for (String feedUserId : feedUserIds) {
            List<String> postIds = repository.getUserPostIds(feedUserId);
            for (String postId : postIds) {
                Post post = repository.getPost(postId);
                if (post != null) {
                    feedPosts.add(post);
                }
            }
        }
        
        // Sort by timestamp (most recent first) and return top N
        return feedPosts.stream()
                .sorted((p1, p2) -> p2.getTimestamp().compareTo(p1.getTimestamp()))
                .limit(feedLimit)
                .collect(Collectors.toList());
    }
}

