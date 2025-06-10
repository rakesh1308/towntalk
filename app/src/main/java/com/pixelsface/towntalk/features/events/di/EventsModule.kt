package com.pixelsface.towntalk.features.events.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pixelsface.towntalk.features.events.data.repository.FirebaseEventRepository
import com.pixelsface.towntalk.features.events.domain.repository.EventRepository
import com.pixelsface.towntalk.features.events.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object EventsModule {

    @Provides
    @ViewModelScoped
    fun provideEventRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): EventRepository {
        return FirebaseEventRepository(firestore, auth)
    }

    @Provides
    @ViewModelScoped
    fun provideGetEventsUseCase(eventRepository: EventRepository): GetEventsUseCase {
        return GetEventsUseCase(eventRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetEventByIdUseCase(eventRepository: EventRepository): GetEventByIdUseCase {
        return GetEventByIdUseCase(eventRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideCreateEventUseCase(eventRepository: EventRepository): CreateEventUseCase {
        return CreateEventUseCase(eventRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideUpdateEventUseCase(eventRepository: EventRepository): UpdateEventUseCase {
        return UpdateEventUseCase(eventRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideDeleteEventUseCase(eventRepository: EventRepository): DeleteEventUseCase {
        return DeleteEventUseCase(eventRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideRsvpToEventUseCase(eventRepository: EventRepository): RsvpToEventUseCase {
        return RsvpToEventUseCase(eventRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideCancelRsvpUseCase(eventRepository: EventRepository): CancelRsvpUseCase {
        return CancelRsvpUseCase(eventRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetEventsByOrganizerUseCase(eventRepository: EventRepository): GetEventsByOrganizerUseCase {
        return GetEventsByOrganizerUseCase(eventRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetAttendingEventsUseCase(eventRepository: EventRepository): GetAttendingEventsUseCase {
        return GetAttendingEventsUseCase(eventRepository)
    }
} 