package com.pixelsface.towntalk.features.explore.data.repository

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.core.common.error.UnknownException
import com.pixelsface.towntalk.features.explore.data.model.CategoryDto
import com.pixelsface.towntalk.features.explore.domain.model.Category
import com.pixelsface.towntalk.features.explore.domain.repository.CategoryRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query // Added import
import kotlinx.coroutines.Dispatchers
// import kotlinx.coroutines.delay // No longer needed for hardcoded list simulation
import kotlinx.coroutines.tasks.await // Added import
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseCategoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore // Firestore instance injected
) : CategoryRepository {

    companion object {
        private const val CATEGORIES_COLLECTION = "categories"
    }

    override suspend fun getCategories(): Result<List<Category>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(CATEGORIES_COLLECTION)
                .orderBy("sortOrder", Query.Direction.ASCENDING) // Optional: order by sortOrder
                .get()
                .await()

            val categories = snapshot.documents.mapNotNull { document ->
                // Map Firestore document to CategoryDto, then to Category domain model
                document.toObject(CategoryDto::class.java)?.copy(id = document.id)?.toDomain()
            }
            Result.Success(categories)
        } catch (e: Exception) {
            // Log the exception or handle it more gracefully depending on requirements
            // For example, return a specific error type or a default list
            Result.Error(UnknownException("Failed to fetch categories from Firestore: ${e.message}", e))
        }
    }
} 