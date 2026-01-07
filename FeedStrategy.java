import java.util.List;

/**
 * Strategy interface for feed generation
 * Design Pattern: Strategy Pattern
 */
public interface FeedStrategy {
    List<Post> generateFeed(String userId, SocialMediaRepository repository);
}

