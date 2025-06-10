package com.pixelsface.towntalk.features.location.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.core.common.error.UnknownException
import com.pixelsface.towntalk.features.location.domain.model.Location
import com.pixelsface.towntalk.features.location.domain.repository.LocationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseLocationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : LocationRepository {

    private val locationsCollection = firestore.collection("locations")
    private val userLocationsCollection = firestore.collection("user_locations")

    override fun getLocations(): Flow<List<Location>> = callbackFlow {
        val subscription = locationsCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val locations = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Location::class.java)?.copy(id = doc.id)
                    }
                    trySend(locations)
                }
            }
        awaitClose { subscription.remove() }
    }.catch { error ->
        emit(emptyList())
    }

    override suspend fun searchLocations(query: String): Result<List<Location>> {
        return try {
            val snapshot = locationsCollection
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + '\uf8ff')
                .limit(10)
                .get()
                .await()

            val locations = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Location::class.java)?.copy(id = doc.id)
            }
            Result.success(locations)
        } catch (e: Exception) {
            Result.error<List<Location>>(UnknownException("Failed to search locations: ${e.message}", e))
        }
    }

    override suspend fun saveSelectedLocation(location: Location): Result<Unit> {
        return try {
            val userId = getUserId()
            userLocationsCollection.document(userId).set(mapOf(
                "locationId" to location.id,
                "timestamp" to System.currentTimeMillis()
            )).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error<Unit>(UnknownException("Failed to save selected location: ${e.message}", e))
        }
    }

    override suspend fun getSelectedLocation(): Result<Location> {
        return try {
            val userId = getUserId()
            val userLocationDoc = userLocationsCollection.document(userId).get().await()
            val locationId = userLocationDoc.getString("locationId") 
                ?: return Result.error<Location>(UnknownException("No location selected"))
            
            val locationDoc = locationsCollection.document(locationId).get().await()
            val location = locationDoc.toObject(Location::class.java)?.copy(id = locationDoc.id)
                ?: return Result.error<Location>(UnknownException("Selected location not found"))
            
            Result.success(location)
        } catch (e: Exception) {
            Result.error<Location>(UnknownException("Failed to get selected location: ${e.message}", e))
        }
    }

    override suspend fun getCurrentLocation(): Result<Location> {
        return try {
            val snapshot = locationsCollection
                .whereEqualTo("isCurrent", true)
                .limit(1)
                .get()
                .await()

            val location = snapshot.documents.firstOrNull()?.let { doc ->
                doc.toObject(Location::class.java)?.copy(id = doc.id)
            } ?: return Result.error<Location>(UnknownException("No current location found"))

            Result.success(location)
        } catch (e: Exception) {
            Result.error<Location>(UnknownException("Failed to get current location: ${e.message}", e))
        }
    }

    override suspend fun saveLocation(location: Location): Result<Unit> {
        return try {
            val locationData = location.copy(id = "")
            val docRef = if (location.id.isNotEmpty()) {
                locationsCollection.document(location.id)
            } else {
                locationsCollection.document()
            }
            
            docRef.set(locationData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error<Unit>(UnknownException("Failed to save location: ${e.message}", e))
        }
    }

    private fun getUserId(): String {
        return firestore.app.options.projectId ?: "default_user"
    }
} 