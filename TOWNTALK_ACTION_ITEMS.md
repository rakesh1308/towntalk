# TownTalk Application: Detailed Action Items

## Overall Goal:
Implement the TownTalk application based on the provided architecture (`TOWN_TALK_ARCHITECTURE_CONTEXT.md`), ensuring modularity, testability, and backend flexibility.

## Guiding Principles:
*   Address one module at a time.
*   Always keep in mind the architecture in case of fixing any error.
*   Consider the Mockups while designing the ui if no mockup is present then create one.

---

## I. Architecture Agent (Lines 59-64 of ARCHITECTURE_CONTEXT.md)

**Status:** Some foundational work seems to be in place (project structure, Hilt).

**Action Items:**

1.  **Initialize Project with Correct Package Structure (Line 462, 580-616):**
    *   [/] Verify current structure against `com.pixelsface.towntalk/core/` and `com.pixelsface.towntalk/features/*` (auth, feed, home, profile). (Feed feature structure is evolving)
    *   [ ] Ensure sub-packages within `core` (`common/di`, `common/domain/model`, `common/error`, `common/utils`, `data/remote/api`, `data/remote/dto`, `data/local/dao`, `data/local/entity`, `data/repository`, `di`) are created.
    *   [/] Ensure sub-packages within each feature module (`data`, `domain/model`, `domain/repository`, `domain/usecase`, `ui`) are created. (Partially for Feed)
2.  **Set up Hilt for Dependency Injection (Line 463, 770-830):**
    *   [/] Verify `@HiltAndroidApp` is in `TownTalkApplication.kt`. (Assumed, but good to confirm)
    *   [/] Verify `AppModule` (Lines 775-808) is implemented and provides `FirebaseFirestore`, `FirebaseStorage`, `FirebaseAuth`, `SharedPreferences`, and `FeatureToggle`. (Firebase services used, SharedPreferences/FeatureToggle pending)
    *   [/] Verify `RepositoryModule` (Lines 810-819) is implemented to bind repository interfaces to their Firebase implementations (e.g., `FirebasePostRepository` to `PostRepository`). (Implicitly, as PostRepository is used)
    *   [/] Ensure each feature module (e.g., `FeedModule` lines 822-829) has its Hilt module providing use cases and other feature-specific dependencies. (Partially for Feed with `CreatePostViewModel`)
3.  **Implement Feature Toggle System (Line 464, 725-768):**
    *   [ ] Create `Feature` enum (Lines 736-743) with initial features: `NEW_UI`, `SPRING_BOOT_BACKEND`, `OFFLINE_SUPPORT`, `PRIVATE_MESSAGING`, `EVENT_RSVP`. Add `EVENTS` as per Lines 339-342.
    *   [ ] Create `FeatureToggle` interface (Lines 730-734).
    *   [ ] Implement `SharedPreferencesFeatureToggle` (Lines 745-768), including default values.
    *   [ ] Ensure `FeatureToggle` is provided by Hilt (already in `AppModule` plan).
4.  **Create Core Interfaces and Base Classes (Line 465):**
    *   [ ] Implement `UserManager` interface in `core.common.domain.model` (or similar shared location) (Lines 390-394).
    *   [ ] Identify and implement other shared base classes or utility functions that would reside in `core.common.utils`.
    *   [x] Implement the `Result` sealed class for error handling in `core.common.error` (Lines 1339-1362). (Similar `ValidationResult` was created, assuming core `Result` is also in place or can be adapted from it)

---

## II. Data Layer Agent (Lines 65-70 of ARCHITECTURE_CONTEXT.md)

**Status:** `FirebasePostRepository` is partially implemented. Room setup is pending. Spring Boot alternatives are pending.

**Action Items:**

1.  **Create Repository Interfaces (Auth & Feed) (Line 468):**
    *   [ ] Define `AuthRepository` interface in `features.auth.domain.repository` (API spec Lines 902-913).
    *   [/] Define `PostRepository` interface in `features.feed.domain.repository` (API spec Lines 641-649, 917-926). (Implicitly used by ViewModels)
    *   [ ] Define `EventRepository` interface in `features.events.domain.repository` (Lines 299-305).
    *   [ ] (Future) Define repository interfaces for other features (Profile, Comments, etc.).
2.  **Implement Firebase Repository Implementations (Line 469):**
    *   [/] Complete `FirebasePostRepository` in `features.feed.data` (Lines 664-689), ensuring all methods from the `PostRepository` interface are implemented and use the `Result` type for error handling (Lines 1364-1390). (Firestore rules fixed, indicating some implementation exists)
    *   [ ] Implement `FirebaseAuthRepository` in `features.auth.data` based on `AuthRepository` interface and Firebase Auth.
    *   [ ] Implement `FirebaseEventRepository` in `features.events.data` (Lines 308-313).
    *   [/] Ensure all Firebase calls are wrapped in `withContext(Dispatchers.IO)`. (Good practice, to be verified everywhere)
3.  **Set up Room Database Structure (Line 470, 990-1053):**
    *   [ ] Add Room dependencies to `build.gradle`.
    *   [ ] Define `UserEntity`, `PostEntity`, `CommentEntity` (Lines 1011-1052).
    *   [ ] Define `UserDao`, `PostDao`, `CommentDao` interfaces with necessary CRUD operations.
    *   [ ] Implement `TownTalkDatabase` abstract class (Lines 1000-1009).
    *   [ ] Provide `TownTalkDatabase` and DAOs via Hilt in `AppModule` or a dedicated database module.
4.  **Create Data Mappers (Line 471):**
    *   [ ] Implement mappers for converting:
        *   Firebase DTOs (if any, or `DocumentSnapshot`) to Domain Models (e.g., `Post` from Firestore document).
        *   Domain Models to Room Entities (e.g., `Post` to `PostEntity`).
        *   Room Entities to Domain Models (e.g., `PostEntity` to `Post`).
        *   (Future) Spring Boot DTOs to Domain Models.
        *   (Future) Domain Models to Spring Boot DTOs.
    *   [ ] Place mappers in the respective data layer modules (e.g., `FirebasePostMapper`, `RoomPostMapper`).
5.  **Implement Spring Boot Repository Alternatives (Line 504):**
    *   [ ] Define Retrofit `PostApi` interface in `features.feed.data.remote.api` (Lines 964-986).
    *   [ ] Define Retrofit `AuthApi` interface in `features.auth.data.remote.api` (Lines 934-958).
    *   [ ] Provide Retrofit instance and API services via Hilt.
    *   [ ] Implement `SpringBootPostRepository` in `features.feed.data` (Lines 691-722), integrating with `PostApi` and `PostDao` for caching.
    *   [ ] Implement `SpringBootAuthRepository` in `features.auth.data`.
6.  **Complete Local Caching Implementation (Line 505):**
    *   [ ] Ensure repositories (especially Spring Boot versions) use DAOs to cache data fetched from network and serve from cache when appropriate (as in `SpringBootPostRepository` example).
7.  **Add Offline Support (Line 506):**
    *   [ ] Repositories should attempt to serve data from Room cache if network is unavailable.
    *   [ ] Implement a mechanism to queue create/update/delete operations when offline and sync when back online (requires more design: Conflict resolution, background service).
8.  **Implement Data Synchronization (Line 507):**
    *   [ ] Develop strategy for syncing local Room data with the backend (Firebase/Spring Boot) periodically or on specific triggers.

---

## III. Domain Layer Agent (Lines 71-76 of ARCHITECTURE_CONTEXT.md)

**Status:** Some domain models (`Post`, `User`, `ValidationResult`) and validation constants exist. Some use cases might be implicitly in ViewModels.

**Action Items:**

1.  **Create Domain Models (User, Post, Comment, etc.) (Line 474):**
    *   [x] Verify/complete `Post` model in `features.feed.domain.model` (Lines 626-638). (Used and refined)
    *   [x] Create `User` model in `core.common.domain.model` (or `features.auth.domain.model` if more specific). (Used in `CreatePostViewModel`)
    *   [ ] Create `Comment` model in `features.feed.domain.model` (or a dedicated `comments` feature).
    *   [ ] Create `Event` model in `features.events.domain.model`.
    *   [/] Ensure all models are data classes and represent pure business entities. (Post, User are; others to be checked)
2.  **Implement Basic & Advanced Use Cases (Auth, Feed, Events) (Line 475, 510):**
    *   [/] Create `GetPostsUseCase` in `features.feed.domain.usecase` (Lines 651-657). (Implicitly, logic might be in ViewModel)
    *   [/] Create `CreatePostUseCase` in `features.feed.domain.usecase`. (Implicitly, logic might be in ViewModel)
    *   [ ] Create `LikePostUseCase`, `UnlikePostUseCase`, `AddCommentUseCase`, `DeletePostUseCase` for the feed.
    *   [ ] Create use cases for Auth: `RegisterUseCase`, `SignInUseCase`, `SignOutUseCase`, `GetCurrentUserUseCase`.
    *   [ ] Create use cases for Events: `GetEventsUseCase` (Lines 329-331), `CreateEventUseCase`, `RsvpToEventUseCase`.
    *   [/] Ensure all use cases depend on repository interfaces, not implementations. (To be verified if use cases are explicit)
    *   [/] Ensure use cases return `Result<T>`. (Implemented in ViewModels, should be in UseCases)
3.  **Set up/Complete Error Handling Framework (Line 476, 512):**
    *   [x] Ensure the `Result` sealed class (Lines 1339-1362) is consistently used by all use cases and repositories. (Or similar like `ValidationResult`)
    *   [ ] Define specific business-level error types/exceptions if needed, to be wrapped in `Result.Error`.
4.  **Create/Complete Validation Logic (Line 477, 513):**
    *   [x] Centralize validation rules (e.g., `PostValidation` for min/max lengths). (Done for Post)
    *   [/] Implement validation logic within use cases or dedicated validator classes for all user inputs (registration, login, post creation, event creation, etc.). (Done for Post creation in ViewModel)
5.  **Define Business Logic for All Features (Line 511):**
    *   [/] This is largely covered by implementing use cases. Ensure all specific business rules (e.g., conditions for liking a post, rules for event visibility) are encapsulated within the domain layer. (Partially, as some logic is in ViewModels)

---

## IV. UI Agent (Lines 77-82 of ARCHITECTURE_CONTEXT.md)

**Status:** `CreatePostScreen` is significantly developed. `FeedScreen` example exists in architecture.

**Action Items:**

1.  **Implement Core UI Components (Line 482):**
    *   [/] Develop reusable common components (e.g., `LoadingIndicator`, `ErrorMessage`, standard buttons, cards) based on UI Design Guidelines (Lines 1103-1195) and `TownTalkColors` (Lines 1107-1123) / `TownTalkTypography` (Lines 1128-1166). (Some components like `Snackbar` for error, `CircularProgressIndicator` for loading are used in `CreatePostScreen`)
    *   [ ] Place these in a shared UI module or `core.ui.components`.
2.  **Create ViewModels (Auth, Feed, Profile, Events, Home) (Line 483):**
    *   [ ] Implement `AuthViewModel` for login/registration screens.
    *   [/] Implement `FeedViewModel` for displaying the feed (Example Lines 859-886). (Architecture example exists)
    *   [ ] Implement `ProfileViewModel` for user profile display and editing.
    *   [ ] Implement `EventsViewModel` for listing and viewing events (Example Line 318).
    *   [ ] Implement `HomeViewModel` (Line 570).
    *   [/] All ViewModels should use Hilt, inject UseCases and FeatureToggles, manage `UiState` using `StateFlow`, and handle user interactions. (`CreatePostViewModel` is a good example of this)
3.  **Set up Navigation Framework (Line 484):**
    *   [/] Implement Jetpack Compose Navigation. (Partially, through callbacks in `CreatePostScreen`)
    *   [ ] Define navigation graph with routes for all screens (login, register, feed, create post, post detail, profile, events, event detail, home).
    *   [ ] Handle argument passing between screens.
    *   [ ] Ensure `TopAppBar` and `BottomNavigation` (Lines 1192-1194) are implemented as per design. (`TopAppBar` implemented in `CreatePostScreen`)
4.  **Implement State Management (Line 485):**
    *   [x] For each screen with a ViewModel, define a corresponding `UiState` sealed class (e.g., `FeedUiState` Lines 889-893, `CreatePostUiState`). (`CreatePostUiState` is implemented)
    *   [x] ViewModels expose `StateFlow<UiState>`. (Done in `CreatePostViewModel`)
    *   [x] Composable screens collect and react to these states. (Done in `CreatePostScreen`)
5.  **Implement All UI Screens with Jetpack Compose (Line 516):**
    *   [ ] **Auth:** Login, Registration, Forgot Password screens.
    *   [/] **Feed:** Feed list, Create Post (largely done), Post Detail (showing full post and comments).
    *   [ ] **Profile:** User profile display, Edit Profile screen.
    *   [ ] **Events:** Events list, Event Detail, Create Event screen.
    *   [ ] **Home:** Main landing screen (structure to be defined, likely a container for other features).
    *   [/] Follow UI Design Guidelines (Color, Typography, Components). Create mockups if not available. (`CreatePostScreen` follows some guidelines)
6.  **Add Animations and Transitions (Line 517):**
    *   [x] Implement subtle animations for screen transitions and component appearances (as seen in `CreatePostScreen`).
7.  **Implement Responsive Design (Line 518):**
    *   [ ] Ensure layouts adapt to different screen sizes and orientations.
8.  **Create Accessibility Features (Line 519):**
    *   [/] Add content descriptions to images and icon buttons. (Partially in `CreatePostScreen`)
    *   [ ] Ensure sufficient touch target sizes.
    *   [ ] Check color contrast.

---

## V. Testing Agent (Lines 83-88 of ARCHITECTURE_CONTEXT.md)

**Status:** Minimal to no tests currently implemented.

**Action Items:**

1.  **Set up Testing Frameworks (Line 488):**
    *   [ ] Ensure JUnit, Mockito (or MockK), Turbine (for Flow testing), and Espresso/Compose Test Rule are configured.
2.  **Create Unit Tests for Repositories & Use Cases (Line 489, 524):**
    *   [ ] Write unit tests for all methods in every repository interface implementation (Firebase, SpringBoot, Room). Mock dependencies (Firebase SDKs, APIs, DAOs). (Example `GetPostsUseCaseTest` Lines 1200-1254).
    *   [ ] Write unit tests for all Use Cases, mocking repository dependencies.
    *   [ ] Aim for >80% test coverage (Line 117).
3.  **Implement UI Tests for Core Components & Screens (Line 490, 527):**
    *   [ ] Write UI tests for all screens using `createComposeRule`. Test different UI states (loading, success, error). (Example `FeedScreenTest` Lines 1259-1332).
    *   [ ] Test user interactions and navigation.
4.  **Generate Test Data (Line 491):**
    *   [ ] Create fake/mock data generators for `Post`, `User`, `Comment`, `Event` to be used in tests.
    *   [ ] Implement `FakePostRepository` (used in `GetPostsUseCaseTest`) and similar fakes for other repositories.
5.  **Implement Integration Tests (Line 525):**
    *   [ ] Test interactions between layers (e.g., ViewModel -> UseCase -> Repository).
    *   [ ] Test DI setup.
6.  **Add Performance Tests (Line 526):** (Potentially later stage)
    *   [ ] Profile app startup time, screen load times, and resource usage.
7.  **Set up Continuous Integration (Line 86):** (Potentially later stage)
    *   [ ] Configure CI pipeline (e.g., GitHub Actions) to run tests on every push/PR.

---

## VI. Integration Agent (Lines 89-94 of ARCHITECTURE_CONTEXT.md)

**Status:** Ongoing.

**Action Items:**

1.  **Coordinate Integration of Components (Line 494, 530):**
    *   [/] Regularly review PRs to ensure components integrate correctly. (Implicitly through our interactions)
    *   [/] Facilitate communication between other agents regarding interface contracts and dependencies. (Implicitly)
2.  **Resolve Dependencies Between Agents (Line 495, 531):**
    *   [/] Act as the primary point of contact for resolving blockers related to inter-agent dependencies. (Implicitly)
3.  **Ensure Consistent Naming and Patterns (Line 496):**
    *   [/] Enforce coding standards, naming conventions, and architectural patterns across the codebase. (Through iterative refinement)
4.  **Manage Feature Toggles (Line 497):**
    *   [ ] Oversee the usage of feature toggles.
    *   [ ] Coordinate which features are active for different builds/releases.
    *   [ ] Ensure new features are added with toggles (Lines 266, 284, 338-342).
5.  **Final Integration and Optimization (Line 530-533):**
    *   [ ] Lead the final assembly of all features.
    *   [ ] Address any remaining integration issues.
    *   [ ] Work with other agents to optimize performance.
6.  **Prepare for Deployment (Line 533, 539):**
    *   [ ] Manage build configurations (debug, release).
    *   [ ] Handle app signing.
    *   [ ] Oversee creation of store listings and release notes.

--- 