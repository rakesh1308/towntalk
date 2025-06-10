# TownTalk Architecture Improvement - AI Agent Implementation Guide

## Document Purpose

This document serves as a comprehensive guide for AI agents implementing the TownTalk application. It provides the architectural blueprint, implementation details, and coordination guidelines for multiple AI agents working in parallel to develop the application efficiently.

## AI Agent Limitations and Context Preservation

### Tool Call Limitations

AI agents have a limitation of 25 tool calls per session. After reaching this limit, the agent will stop and require manual resumption by the user. This limitation affects all AI agents working on the project.

### Context Preservation

To ensure continuity between sessions, AI agents should:

1. **Track Progress**: Each agent should maintain a progress log of completed tasks and remaining work.

2. **Document Dependencies**: Clearly document any dependencies on work from other agents that are pending.

3. **Save Intermediate Results**: Save intermediate results and state to allow resumption from the last completed point.

4. **Summarize Current State**: At the end of each session, provide a summary of the current state, including:
   - Completed tasks
   - In-progress tasks
   - Blockers or issues encountered
   - Next steps for resumption

### Session Management

When an AI agent reaches its tool call limit:

1. **Save Current State**: Document the current state of the implementation.

2. **Request Manual Resumption**: Inform the user that the tool call limit has been reached and request manual resumption.

3. **Provide Context Summary**: Include a brief summary of what has been accomplished and what remains to be done.

4. **Specify Starting Point**: Clearly indicate where the next session should begin.

Example context preservation message:

```
TOOL CALL LIMIT REACHED (25/25)

Current Status:
- Completed: Project structure setup, Hilt configuration, core interfaces
- In Progress: Firebase repository implementations
- Blockers: None
- Next Steps: Complete Firebase repository implementations, implement Room database

Please resume this session to continue implementation.
```

## Implementation Strategy for AI Agents

### Agent Roles and Responsibilities

1. **Architecture Agent**
   - Set up project structure
   - Implement core architecture components
   - Configure dependency injection
   - Establish feature toggle system

2. **Data Layer Agent**
   - Implement repository interfaces
   - Create Firebase implementations
   - Set up Spring Boot alternatives
   - Implement local caching with Room

3. **Domain Layer Agent**
   - Create domain models
   - Implement use cases
   - Define business logic
   - Set up error handling

4. **UI Agent**
   - Implement UI components with Jetpack Compose
   - Create ViewModels
   - Manage UI state
   - Implement navigation

5. **Testing Agent**
   - Create unit tests
   - Implement UI tests
   - Set up continuous integration
   - Generate test data

6. **Integration Agent**
   - Coordinate between agents
   - Resolve dependencies
   - Ensure consistency
   - Manage feature toggles

### Coordination Protocol

1. **Interface-First Development**
   - All agents must agree on interfaces before implementation
   - Interfaces should be documented with clear parameter and return types
   - Changes to interfaces require coordination with all affected agents

2. **Version Control Guidelines**
   - Use feature branches for each component
   - Follow semantic versioning for releases
   - Include detailed commit messages with component tags
   - Example: `[DATA] Implement Firebase PostRepository`

3. **Communication Protocol**
   - Agents should document all decisions affecting other agents
   - Use a shared decision log for architectural decisions
   - Report blockers immediately to the Integration Agent

4. **Testing Requirements**
   - All code must include unit tests
   - UI components must have UI tests
   - Integration tests required for cross-component functionality
   - Test coverage must exceed 80%

## Background

TownTalk is a location-based social networking application that enables users to share information, events, and updates within their local communities. The application was initially developed using Firebase as the backend solution, which provided rapid development capabilities and scalability for the initial user base.

As the application has grown, several challenges have emerged with the current architecture:

1. **Tight Coupling**: The current implementation has tight coupling between the UI, business logic, and data layers, making it difficult to modify or extend features independently.

2. **Firebase Dependency**: The application is heavily dependent on Firebase services, making it challenging to consider alternative backend solutions or implement hybrid approaches.

3. **Testing Limitations**: The current architecture makes it difficult to write comprehensive tests, particularly for components that interact with Firebase services.

4. **Scalability Concerns**: As the user base grows, the current architecture may not scale efficiently, particularly for features that require complex queries or real-time updates.

5. **Feature Management**: The lack of a feature toggle system makes it difficult to gradually roll out new features or disable problematic ones without deploying a new version.

## Motivations for Change

### Technical Motivations

1. **Improved Maintainability**: A modular architecture will make the codebase more maintainable by separating concerns and reducing dependencies between components.

2. **Enhanced Testability**: By abstracting external dependencies and implementing proper interfaces, we can significantly improve the test coverage of the application.

3. **Backend Flexibility**: The ability to switch between Firebase and Spring Boot (or use both simultaneously) will provide greater flexibility in how we handle data and authentication.

4. **Performance Optimization**: A more efficient architecture will improve app performance, particularly for users with limited connectivity or older devices.

5. **Code Reusability**: A modular approach will enable better code reuse across features and potentially across different platforms in the future.

### Business Motivations

1. **Cost Management**: The ability to use Spring Boot for certain features may reduce Firebase-related costs as the application scales.

2. **Feature Experimentation**: A feature toggle system will allow for A/B testing and gradual feature rollouts, enabling data-driven product decisions.

3. **Market Adaptability**: A more flexible architecture will allow the application to adapt more quickly to changing market conditions and user needs.

4. **Developer Productivity**: A cleaner architecture will improve developer onboarding and productivity, reducing the time required to implement new features.

5. **User Experience**: Improved performance and reliability will enhance the overall user experience, potentially leading to increased user retention and engagement.

## Market and User Considerations

### Current User Base

TownTalk currently serves approximately 50,000 active users across multiple cities. The user base is diverse, ranging from young professionals to retirees, with varying levels of technical proficiency. Key user segments include:

- **Community Organizers**: Use the platform to share events and announcements
- **Local Businesses**: Promote offers and engage with customers
- **Residents**: Share information about local issues and seek recommendations
- **Emergency Responders**: Share critical information during emergencies

### User Feedback

Recent user feedback has highlighted several areas for improvement:

1. **Offline Functionality**: Users in areas with poor connectivity have requested better offline support.
2. **Performance**: Some users have reported slow loading times, particularly when viewing posts with multiple images.
3. **Feature Requests**: Users have requested additional features such as private messaging, event RSVPs, and advanced search capabilities.
4. **Privacy Concerns**: Some users have expressed concerns about data privacy and location sharing.

### Competitive Landscape

The local community app market is becoming increasingly competitive, with several established players and new entrants:

- **Nextdoor**: A well-funded competitor with strong brand recognition
- **Facebook Groups**: Leveraging existing social networks for local communities
- **Citizen**: Focusing on safety and emergency notifications
- **Ring Neighbors**: Integrated with home security systems

To maintain and grow our market position, we need to differentiate through superior user experience, unique features, and reliable performance.

## Technical Considerations

### Current Technical Debt

The current codebase has accumulated significant technical debt:

1. **Inconsistent Architecture**: Different features follow different architectural patterns, making the codebase harder to understand and maintain.

2. **Limited Error Handling**: Error handling is inconsistent across the application, leading to poor user experience when issues occur.

3. **Insufficient Logging**: The current logging system provides limited visibility into application behavior, making it difficult to diagnose and fix issues.

4. **Outdated Dependencies**: Some dependencies are outdated and may have security vulnerabilities.

5. **Incomplete Documentation**: Documentation is inconsistent and often outdated, making it difficult for new developers to understand the codebase.

### Technology Stack Considerations

#### Firebase vs. Spring Boot

**Firebase Advantages**:
- Real-time updates out of the box
- Built-in authentication and security rules
- Scalable without significant infrastructure management
- Good for rapid prototyping and MVPs

**Firebase Limitations**:
- Limited query capabilities for complex data relationships
- Potential cost concerns at scale
- Limited control over data processing and storage
- Dependency on Google's infrastructure

**Spring Boot Advantages**:
- Full control over backend logic and data processing
- More cost-effective at scale
- Better support for complex queries and relationships
- Can be deployed on any infrastructure

**Spring Boot Limitations**:
- Requires more development and maintenance effort
- Need to implement real-time capabilities manually
- More complex deployment and scaling

### Mobile Architecture Considerations

#### Current Architecture

The current architecture follows a simplified MVVM pattern with:
- Activities and Fragments for UI
- ViewModels for business logic
- Repositories for data access
- Direct Firebase SDK usage in repositories

#### Proposed Architecture

The proposed architecture will:
- Separate the UI, domain, and data layers more clearly
- Introduce use cases for complex business logic
- Abstract data sources behind interfaces
- Implement proper dependency injection
- Add local caching for offline support

## Modularity and Module Management

### Module Independence

The proposed architecture is designed with a strong emphasis on module independence, allowing features to be added or removed with minimal impact on the rest of the application. This is achieved through several key design principles:

1. **Feature Modules**: Each feature (auth, feed, profile, etc.) is encapsulated in its own module with clear boundaries.

2. **Dependency Inversion**: All dependencies between modules are managed through interfaces rather than concrete implementations.

3. **Event-Based Communication**: Inter-module communication is handled through events rather than direct method calls.

4. **Feature Toggles**: Features can be enabled or disabled at runtime without code changes.

### Adding New Modules

To add a new module to the application:

1. **Create Module Structure**: Set up the standard module structure (data, domain, ui).

2. **Define Interfaces**: Create repository and use case interfaces in the domain layer.

3. **Implement Data Layer**: Implement the repository interfaces with Firebase and/or Spring Boot.

4. **Create UI Components**: Develop the UI using Jetpack Compose.

5. **Register with DI**: Add the module's dependencies to the DI system.

6. **Add Navigation**: Update the navigation graph to include the new module.

7. **Enable Feature Toggle**: Add a feature toggle for the new module.

Example of adding a new "Events" module:

```kotlin
// 1. Create module structure
// features/events/
//   ├── data/
//   │   ├── domain/
//   │   │   ├── model/
//   │   │   ├── repository/
//   │   │   └── usecase/
//   │   └── ui/

// 2. Define interfaces
interface EventRepository {
    suspend fun getEvents(city: String): Result<List<Event>>
    suspend fun getEvent(id: String): Result<Event>
    suspend fun createEvent(event: Event): Result<String>
    suspend fun rsvpToEvent(eventId: String): Result<Unit>
    suspend fun cancelRsvp(eventId: String): Result<Unit>
}

// 3. Implement data layer
class FirebaseEventRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : EventRepository {
    // Implementation...
}

// 4. Create UI components
@Composable
fun EventsScreen(
    viewModel: EventsViewModel = hiltViewModel(),
    onEventClick: (String) -> Unit
) {
    // UI implementation...
}

// 5. Register with DI
@Module
@InstallIn(ViewModelComponent::class)
object EventsModule {
    @Provides
    fun provideGetEventsUseCase(eventRepository: EventRepository): GetEventsUseCase {
        return GetEventsUseCase(eventRepository)
    }
}

// 6. Add navigation
// In the navigation graph:
// composable("events") { EventsScreen(...) }

// 7. Enable feature toggle
enum class Feature {
    // Existing features...
    EVENTS
}
```

### Removing Modules

To remove a module from the application:

1. **Disable Feature Toggle**: Set the feature toggle to disabled.

2. **Remove Navigation**: Remove the module's routes from the navigation graph.

3. **Remove DI Bindings**: Remove the module's dependencies from the DI system.

4. **Delete Module Code**: Delete the module's code files.

Example of removing the "Events" module:

```kotlin
// 1. Disable feature toggle
featureToggle.setEnabled(Feature.EVENTS, false)

// 2. Remove navigation
// Remove from navigation graph:
// composable("events") { EventsScreen(...) }

// 3. Remove DI bindings
// Remove from DI modules:
// @Module
// @InstallIn(ViewModelComponent::class)
// object EventsModule { ... }

// 4. Delete module code
// Delete the entire features/events/ directory
```

### Module Dependencies

While modules are designed to be independent, some dependencies between modules may be necessary. These dependencies are managed through:

1. **Core Module**: Common functionality shared across modules is placed in the core module.

2. **Interface-Based Dependencies**: Modules depend on interfaces rather than concrete implementations.

3. **Event Bus**: Inter-module communication is handled through an event bus.

Example of module dependency management:

```kotlin
// Core module contains shared interfaces
interface UserManager {
    fun getCurrentUser(): User?
    fun isUserLoggedIn(): Boolean
}

// Auth module implements the interface
class AuthUserManager(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) : UserManager {
    // Implementation...
}

// Other modules depend on the interface, not the implementation
class FeedViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userManager: UserManager // Interface dependency
) : ViewModel() {
    // Implementation...
}
```

### Dynamic Module Loading

For advanced scenarios, the architecture supports dynamic module loading, allowing modules to be loaded at runtime:

1. **Feature Toggle System**: Controls which modules are active.

2. **Lazy Initialization**: Modules are initialized only when needed.

3. **Conditional DI**: Dependencies are provided conditionally based on feature toggles.

Example of dynamic module loading:

```kotlin
// Conditional DI based on feature toggle
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Binds
    @Singleton
    @ConditionalOnFeature(Feature.SPRING_BOOT_BACKEND)
    abstract fun bindPostRepository(
        springBootPostRepository: SpringBootPostRepository
    ): PostRepository
    
    @Binds
    @Singleton
    @ConditionalOnFeature(Feature.SPRING_BOOT_BACKEND, false)
    abstract fun bindPostRepository(
        firebasePostRepository: FirebasePostRepository
    ): PostRepository
}

// Custom annotation for conditional DI
@Qualifier
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class ConditionalOnFeature(
    val feature: Feature,
    val value: Boolean = true
)
```

## Implementation Plan for AI Agents

### Day 1: Foundation and Core Features

#### Morning Session (4 hours)

**Architecture Agent Tasks**:
- Initialize project with correct package structure
- Set up Hilt for dependency injection
- Implement feature toggle system
- Create core interfaces and base classes

**Data Layer Agent Tasks**:
- Create repository interfaces for auth and feed
- Implement Firebase repository implementations
- Set up Room database structure
- Create data mappers

**Domain Layer Agent Tasks**:
- Create domain models for User, Post, Comment
- Implement basic use cases for auth and feed
- Set up error handling framework
- Create validation logic

#### Afternoon Session (4 hours)

**UI Agent Tasks**:
- Implement core UI components with Jetpack Compose
- Create ViewModels for auth and feed
- Set up navigation framework
- Implement state management

**Testing Agent Tasks**:
- Set up testing framework
- Create unit tests for repositories
- Implement UI tests for core components
- Generate test data

**Integration Agent Tasks**:
- Coordinate integration of components
- Resolve dependencies between agents
- Ensure consistent naming and patterns
- Manage feature toggles

### Day 2: Feature Implementation and Polish

#### Morning Session (4 hours)

**Data Layer Agent Tasks**:
- Implement Spring Boot repository alternatives
- Complete local caching implementation
- Add offline support
- Implement data synchronization

**Domain Layer Agent Tasks**:
- Implement advanced use cases
- Add business logic for all features
- Complete error handling
- Implement validation for all inputs

**UI Agent Tasks**:
- Implement remaining UI components
- Add animations and transitions
- Implement responsive design
- Create accessibility features

#### Afternoon Session (4 hours)

**Testing Agent Tasks**:
- Complete test coverage
- Implement integration tests
- Add performance tests
- Create UI automation tests

**Integration Agent Tasks**:
- Final integration of all components
- Resolve remaining issues
- Optimize performance
- Prepare for deployment

**All Agents**:
- Code review and refactoring
- Documentation
- Final testing
- Deployment preparation

## Technical Implementation Details

### Current Codebase Structure

The current codebase follows a feature-based package structure:

```
com.pixelsface.towntalk/
├── auth/
│   ├── data/
│   │   └── repository/
│   │       ├── AuthRepository.kt
│   │       └── AuthRepositoryImpl.kt
│   ├── ui/
│   │   ├── login/
│   │   └── register/
│   └── AuthViewModel.kt
├── feed/
│   ├── data/
│   │   ├── model/
│   │   │   └── Post.kt
│   │   └── repository/
│   │       ├── PostRepository.kt
│   │       └── PostRepositoryImpl.kt
│   └── ui/
│       ├── addpost/
│       ├── detail/
│       └── list/
├── home/
│   └── HomeViewModel.kt
├── profile/
│   └── ProfileViewModel.kt
└── TownTalkApplication.kt
```

### Proposed Architecture

The proposed architecture will follow Clean Architecture principles with a modular approach:

```
com.pixelsface.towntalk/
├── core/
│   ├── common/
│   │   ├── di/
│   │   ├── domain/
│   │   │   └── model/
│   │   ├── error/
│   │   └── utils/
│   ├── data/
│   │   ├── remote/
│   │   │   ├── api/
│   │   │   └── dto/
│   │   ├── local/
│   │   │   ├── dao/
│   │   │   └── entity/
│   │   └── repository/
│   └── di/
├── features/
│   ├── auth/
│   │   ├── data/
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── usecase/
│   │   └── ui/
│   ├── feed/
│   │   ├── data/
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── usecase/
│   │   └── ui/
│   ├── home/
│   └── profile/
└── TownTalkApplication.kt
```

### Key Architectural Components

#### 1. Domain Layer

The domain layer will contain business logic and models:

```kotlin
// Example: Post domain model
data class Post(
    val id: String,
    val title: String,
    val content: String,
    val authorId: String,
    val authorName: String,
    val timestamp: Long,
    val likes: Int,
    val comments: Int,
    val mediaUrls: List<String>,
    val category: String,
    val city: String
)

// Example: Post repository interface
interface PostRepository {
    suspend fun getPosts(city: String): Result<List<Post>>
    suspend fun getPost(id: String): Result<Post>
    suspend fun createPost(post: Post): Result<String>
    suspend fun likePost(id: String): Result<Unit>
    suspend fun unlikePost(id: String): Result<Unit>
    suspend fun addComment(postId: String, comment: Comment): Result<Unit>
    suspend fun deletePost(id: String): Result<Unit>
}

// Example: Use case
class GetPostsUseCase(private val postRepository: PostRepository) {
    suspend operator fun invoke(city: String): Result<List<Post>> {
        return postRepository.getPosts(city)
    }
}
```

#### 2. Data Layer

The data layer will implement the repository interfaces:

```kotlin
// Example: Firebase implementation
class FirebasePostRepository(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) : PostRepository {
    override suspend fun getPosts(city: String): Result<List<Post>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("posts")
                .whereEqualTo("city", city)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val posts = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Post::class.java)?.copy(id = doc.id)
            }
            
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Other implementation methods...
}

// Example: Spring Boot implementation
class SpringBootPostRepository(
    private val api: PostApi,
    private val postDao: PostDao
) : PostRepository {
    override suspend fun getPosts(city: String): Result<List<Post>> = withContext(Dispatchers.IO) {
        try {
            // Try to get from local cache first
            val cachedPosts = postDao.getPostsByCity(city)
            if (cachedPosts.isNotEmpty()) {
                return@withContext Result.success(cachedPosts.map { it.toDomain() })
            }
            
            // If not in cache, fetch from API
            val response = api.getPosts(city)
            if (response.isSuccessful) {
                val posts = response.body()?.map { it.toDomain() } ?: emptyList()
                
                // Cache the results
                postDao.insertAll(posts.map { it.toEntity() })
                
                Result.success(posts)
            } else {
                Result.failure(Exception("Failed to fetch posts: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Other implementation methods...
}
```

#### 3. Feature Toggle System

The feature toggle system will allow enabling/disabling features at runtime:

```kotlin
// Example: Feature toggle interface
interface FeatureToggle {
    fun isEnabled(feature: Feature): Boolean
    fun setEnabled(feature: Feature, enabled: Boolean)
}

// Example: Feature enum
enum class Feature {
    NEW_UI,
    SPRING_BOOT_BACKEND,
    OFFLINE_SUPPORT,
    PRIVATE_MESSAGING,
    EVENT_RSVP
}

// Example: Implementation
class SharedPreferencesFeatureToggle(
    private val sharedPreferences: SharedPreferences
) : FeatureToggle {
    override fun isEnabled(feature: Feature): Boolean {
        return sharedPreferences.getBoolean(feature.name, feature.defaultValue)
    }
    
    override fun setEnabled(feature: Feature, enabled: Boolean) {
        sharedPreferences.edit().putBoolean(feature.name, enabled).apply()
    }
    
    companion object {
        private val Feature.defaultValue: Boolean
            get() = when (this) {
                Feature.NEW_UI -> false
                Feature.SPRING_BOOT_BACKEND -> false
                Feature.OFFLINE_SUPPORT -> true
                Feature.PRIVATE_MESSAGING -> false
                Feature.EVENT_RSVP -> false
            }
    }
}
```

#### 4. Dependency Injection

Dependency injection will be implemented using Hilt:

```kotlin
// Example: App module
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("towntalk_prefs", Context.MODE_PRIVATE)
    }
    
    @Provides
    @Singleton
    fun provideFeatureToggle(sharedPreferences: SharedPreferences): FeatureToggle {
        return SharedPreferencesFeatureToggle(sharedPreferences)
    }
}

// Example: Repository module
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindPostRepository(
        firebasePostRepository: FirebasePostRepository
    ): PostRepository
}

// Example: Feature module
@Module
@InstallIn(ViewModelComponent::class)
object FeedModule {
    @Provides
    fun provideGetPostsUseCase(postRepository: PostRepository): GetPostsUseCase {
        return GetPostsUseCase(postRepository)
    }
}
```

#### 5. UI Layer

The UI layer will use Jetpack Compose for modern UI development:

```kotlin
// Example: Feed screen
@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel(),
    onPostClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    when (val state = uiState) {
        is FeedUiState.Loading -> LoadingIndicator()
        is FeedUiState.Success -> PostList(
            posts = state.posts,
            onPostClick = onPostClick
        )
        is FeedUiState.Error -> ErrorMessage(
            message = state.message,
            onRetry = { viewModel.loadPosts() }
        )
    }
}

// Example: ViewModel
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getPostsUseCase: GetPostsUseCase,
    private val featureToggle: FeatureToggle
) : ViewModel() {
    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()
    
    init {
        loadPosts()
    }
    
    fun loadPosts() {
        viewModelScope.launch {
            _uiState.value = FeedUiState.Loading
            
            val city = "current_user_city" // Get from user preferences
            getPostsUseCase(city).fold(
                onSuccess = { posts ->
                    _uiState.value = FeedUiState.Success(posts)
                },
                onFailure = { error ->
                    _uiState.value = FeedUiState.Error(error.message ?: "Unknown error")
                }
            )
        }
    }
}

// Example: UI state
sealed class FeedUiState {
    object Loading : FeedUiState()
    data class Success(val posts: List<Post>) : FeedUiState()
    data class Error(val message: String) : FeedUiState()
}
```

## API Specifications

### Firebase API

#### Authentication

```kotlin
interface AuthRepository {
    suspend fun registerWithEmail(email: String, password: String, name: String): Result<User>
    suspend fun signInWithEmail(email: String, password: String): Result<User>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signInWithPhone(phoneNumber: String, verificationId: String, code: String): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun getCurrentUser(): Result<User?>
    suspend fun updateProfile(name: String, photoUrl: String?): Result<Unit>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
}
```

#### Posts

```kotlin
interface PostRepository {
    suspend fun getPosts(city: String): Result<List<Post>>
    suspend fun getPost(id: String): Result<Post>
    suspend fun createPost(post: Post): Result<String>
    suspend fun likePost(id: String): Result<Unit>
    suspend fun unlikePost(id: String): Result<Unit>
    suspend fun addComment(postId: String, comment: Comment): Result<Unit>
    suspend fun deletePost(id: String): Result<Unit>
}
```

### Spring Boot API

#### Authentication

```kotlin
interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): Response<AuthResponse>
    
    @POST("auth/phone")
    suspend fun loginWithPhone(@Body request: PhoneLoginRequest): Response<AuthResponse>
    
    @POST("auth/logout")
    suspend fun logout(): Response<Unit>
    
    @GET("auth/me")
    suspend fun getCurrentUser(): Response<UserResponse>
    
    @PUT("auth/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<Unit>
    
    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<Unit>
}
```

#### Posts

```kotlin
interface PostApi {
    @GET("posts")
    suspend fun getPosts(@Query("city") city: String): Response<List<PostResponse>>
    
    @GET("posts/{id}")
    suspend fun getPost(@Path("id") id: String): Response<PostResponse>
    
    @POST("posts")
    suspend fun createPost(@Body request: CreatePostRequest): Response<PostResponse>
    
    @POST("posts/{id}/like")
    suspend fun likePost(@Path("id") id: String): Response<Unit>
    
    @DELETE("posts/{id}/like")
    suspend fun unlikePost(@Path("id") id: String): Response<Unit>
    
    @POST("posts/{id}/comments")
    suspend fun addComment(@Path("id") id: String, @Body request: AddCommentRequest): Response<Unit>
    
    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") id: String): Response<Unit>
}
```

## Database Schema

### Room Database

```kotlin
@Database(
    entities = [
        UserEntity::class,
        PostEntity::class,
        CommentEntity::class
    ],
    version = 1
)
abstract class TownTalkDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun commentDao(): CommentDao
    
    companion object {
        const val DATABASE_NAME = "towntalk_db"
    }
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String?,
    val city: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val authorId: String,
    val authorName: String,
    val timestamp: Long,
    val likes: Int,
    val comments: Int,
    val mediaUrls: List<String>,
    val category: String,
    val city: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey
    val id: String,
    val postId: String,
    val authorId: String,
    val authorName: String,
    val content: String,
    val timestamp: Long,
    val createdAt: Long,
    val updatedAt: Long
)
```

### Firebase Collections

#### Users Collection

```
users/
  {userId}/
    name: string
    email: string
    photoUrl: string (optional)
    city: string
    createdAt: timestamp
    updatedAt: timestamp
```

#### Posts Collection

```
posts/
  {postId}/
    title: string
    content: string
    authorId: string
    authorName: string
    timestamp: timestamp
    likes: number
    comments: number
    mediaUrls: array<string>
    category: string
    city: string
    createdAt: timestamp
    updatedAt: timestamp
```

#### Comments Collection

```
comments/
  {commentId}/
    postId: string
    authorId: string
    authorName: string
    content: string
    timestamp: timestamp
    createdAt: timestamp
    updatedAt: timestamp
```

## UI Design Guidelines

### Color Palette

```kotlin
object TownTalkColors {
    val Primary = Color(0xFF1976D2)
    val PrimaryDark = Color(0xFF1565C0)
    val PrimaryLight = Color(0xFF42A5F5)
    val Secondary = Color(0xFF4CAF50)
    val SecondaryDark = Color(0xFF388E3C)
    val SecondaryLight = Color(0xFF81C784)
    val Background = Color(0xFFF5F5F5)
    val Surface = Color(0xFFFFFFFF)
    val Error = Color(0xFFD32F2F)
    val OnPrimary = Color(0xFFFFFFFF)
    val OnSecondary = Color(0xFFFFFFFF)
    val OnBackground = Color(0xFF000000)
    val OnSurface = Color(0xFF000000)
    val OnError = Color(0xFFFFFFFF)
}
```

### Typography

```kotlin
val TownTalkTypography = Typography(
    h1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),
    h2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    h3 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    body2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
)
```

### Component Guidelines

1. **Buttons**:
   - Primary buttons: Filled with Primary color
   - Secondary buttons: Outlined with Secondary color
   - Text buttons: Text only with Primary color
   - Icon buttons: Circular with icon only

2. **Cards**:
   - Elevation: 2dp
   - Corner radius: 8dp
   - Padding: 16dp

3. **Text Fields**:
   - Outlined style
   - Label text above input
   - Error state with red text and outline

4. **Lists**:
   - Divider between items
   - Ripple effect on tap
   - Swipe actions where appropriate

5. **Navigation**:
   - Bottom navigation for main sections
   - Top app bar with title and actions
   - Back button in top-left corner

## Testing Strategy

### Unit Tests

```kotlin
class GetPostsUseCaseTest {
    private lateinit var useCase: GetPostsUseCase
    private lateinit var repository: FakePostRepository
    
    @Before
    fun setup() {
        repository = FakePostRepository()
        useCase = GetPostsUseCase(repository)
    }
    
    @Test
    fun `when repository returns success, use case returns success`() = runTest {
        // Given
        val city = "New York"
        val posts = listOf(
            Post(
                id = "1",
                title = "Test Post",
                content = "Test Content",
                authorId = "author1",
                authorName = "Test Author",
                timestamp = 1234567890,
                likes = 0,
                comments = 0,
                mediaUrls = emptyList(),
                category = "General",
                city = "New York"
            )
        )
        repository.setPosts(city, posts)
        
        // When
        val result = useCase(city)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(posts, result.getOrNull())
    }
    
    @Test
    fun `when repository returns failure, use case returns failure`() = runTest {
        // Given
        val city = "New York"
        val error = Exception("Network error")
        repository.setError(city, error)
        
        // When
        val result = useCase(city)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
```

### UI Tests

```kotlin
@RunWith(AndroidJUnit4::class)
class FeedScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun feedScreen_displaysPosts() {
        // Given
        val posts = listOf(
            Post(
                id = "1",
                title = "Test Post",
                content = "Test Content",
                authorId = "author1",
                authorName = "Test Author",
                timestamp = 1234567890,
                likes = 0,
                comments = 0,
                mediaUrls = emptyList(),
                category = "General",
                city = "New York"
            )
        )
        
        // When
        composeTestRule.setContent {
            TownTalkTheme {
                FeedScreen(
                    viewModel = FakeFeedViewModel(posts),
                    onPostClick = {}
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("Test Post").assertExists()
        composeTestRule.onNodeWithText("Test Content").assertExists()
        composeTestRule.onNodeWithText("Test Author").assertExists()
    }
    
    @Test
    fun feedScreen_displaysLoadingState() {
        // When
        composeTestRule.setContent {
            TownTalkTheme {
                FeedScreen(
                    viewModel = FakeFeedViewModel(loading = true),
                    onPostClick = {}
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithTag("loading_indicator").assertExists()
    }
    
    @Test
    fun feedScreen_displaysErrorState() {
        // When
        composeTestRule.setContent {
            TownTalkTheme {
                FeedScreen(
                    viewModel = FakeFeedViewModel(error = "Network error"),
                    onPostClick = {}
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("Network error").assertExists()
        composeTestRule.onNodeWithText("Retry").assertExists()
    }
}
```

## Error Handling Patterns

### Result Type

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    
    fun isSuccess() = this is Success
    fun isError() = this is Error
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    fun exceptionOrNull(): Exception? = when (this) {
        is Success -> null
        is Error -> exception
    }
    
    companion object {
        fun <T> success(data: T) = Success(data)
        fun error(exception: Exception) = Error(exception)
    }
}
```

### Error Handling in Repositories

```kotlin
class FirebasePostRepository(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) : PostRepository {
    override suspend fun getPosts(city: String): Result<List<Post>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("posts")
                .whereEqualTo("city", city)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val posts = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Post::class.java)?.copy(id = doc.id)
            }
            
            Result.success(posts)
        } catch (e: Exception) {
            Result.error(e)
        }
    }
}
```

### Error Handling in ViewModels

```kotlin
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getPostsUseCase: GetPostsUseCase,
    private val featureToggle: FeatureToggle
) : ViewModel() {
    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()
    
    init {
        loadPosts()
    }
    
    fun loadPosts() {
        viewModelScope.launch {
            _uiState.value = FeedUiState.Loading
            
            val city = "current_user_city" // Get from user preferences
            getPostsUseCase(city).fold(
                onSuccess = { posts ->
                    _uiState.value = FeedUiState.Success(posts)
                },
                onFailure = { error ->
                    _uiState.value = FeedUiState.Error(error.message ?: "Unknown error")
                }
            )
        }
    }
}
```

## Conclusion

This document provides a comprehensive guide for AI agents implementing the TownTalk application. By following the architectural principles, implementation details, and coordination protocols outlined in this document, AI agents can work together efficiently to develop a high-quality, maintainable application that meets the requirements of the TownTalk project.

The modular architecture ensures that features can be added or removed with minimal impact on the rest of the application, providing the flexibility needed to adapt to changing requirements and market conditions. The feature toggle system allows for gradual rollouts and A/B testing, enabling data-driven product decisions.

With proper coordination and a clear understanding of the architecture, AI agents can complete the implementation of the TownTalk application in a short timeframe, delivering a high-quality product that meets the needs of users and stakeholders.
