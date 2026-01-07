# Social Media Network System - Architecture Documentation

## Overview
A scalable, thread-safe social media network system with real-time feed updates, supporting concurrent operations.

## Design Patterns Implemented

### 1. Observer Pattern
- **Purpose**: Real-time feed updates when users post
- **Components**:
  - `FeedSubject`: Interface for subject (SocialMediaNetwork)
  - `FeedObserver`: Interface for observers
  - `RealTimeFeedObserver`: Concrete observer implementation
- **Usage**: When User1 posts, all followers (User2, User3, etc.) are notified immediately

### 2. Factory Pattern
- **Purpose**: Centralized object creation
- **Components**:
  - `PostFactory`: Creates Post objects with unique IDs
  - `UserFactory`: Creates User objects
- **Benefits**: Encapsulates creation logic, makes it easy to change creation strategies

### 3. Repository Pattern
- **Purpose**: Abstracts data access layer
- **Component**: `SocialMediaRepository`
- **Benefits**: Separation of concerns, easier to swap data storage implementations

### 4. Strategy Pattern
- **Purpose**: Flexible feed generation algorithms
- **Components**:
  - `FeedStrategy`: Interface for feed generation
  - `RecentFeedStrategy`: Implementation for recent 10 posts
- **Benefits**: Easy to add new feed types (e.g., trending, personalized)

## Concurrency Features

### Thread-Safe Collections
- `ConcurrentHashMap` for users, posts, and userPosts mappings
- `CopyOnWriteArrayList` for observer lists and post lists
- `ConcurrentHashMap.newKeySet()` for user following sets

### ReadWriteLock
- Allows multiple concurrent reads
- Exclusive writes for data modifications
- Optimizes performance for read-heavy workloads

### ExecutorService
- Asynchronous notification of observers
- Non-blocking post uploads and deletions
- Thread pool for managing concurrent operations

## Key Features

### 1. Real-Time Feed Updates
- When User1 posts, all followers receive notification immediately
- Uses Observer pattern with async notifications
- Non-blocking implementation

### 2. Concurrent Operations
- Multiple users can post simultaneously
- Multiple users can read feeds concurrently
- Thread-safe data structures ensure consistency

### 3. Scalability
- Repository pattern allows easy database integration
- Strategy pattern allows different feed algorithms
- Factory pattern simplifies object creation at scale

## Class Structure

```
SocialMediaNetwork (Main System)
├── SocialMediaRepository (Data Access)
├── FeedStrategy (Feed Generation)
│   └── RecentFeedStrategy
├── PostFactory (Post Creation)
├── UserFactory (User Creation)
└── FeedSubject (Observer Pattern)
    ├── FeedObserver
    └── RealTimeFeedObserver
```

## Usage Example

```java
// Create network
SocialMediaNetwork network = new SocialMediaNetwork();

// Create users
network.createUser("user1", "Alice");
network.createUser("user2", "Bob");

// Set up real-time observer
RealTimeFeedObserver observer = new RealTimeFeedObserver("user2");
network.registerObserver(observer, "user2");

// Follow
network.followUser("user2", "user1");

// Post (triggers real-time notification)
network.uploadPost("user1", "Hello!");

// Get feed
List<Post> feed = network.getFeed("user2");
```

## Performance Characteristics

- **Read Operations**: O(1) average case with ConcurrentHashMap
- **Write Operations**: O(1) average case with proper locking
- **Feed Generation**: O(N log N) where N is total posts (due to sorting)
- **Real-time Notifications**: O(F) where F is number of followers (async)

## Future Optimizations

1. **Caching**: Add feed caching for frequently accessed users
2. **Pagination**: Implement cursor-based pagination for feeds
3. **Database Integration**: Replace in-memory storage with database
4. **Message Queue**: Use message queue (Kafka/RabbitMQ) for notifications
5. **Distributed System**: Add support for distributed deployment
6. **Indexing**: Add indexes for faster feed queries

