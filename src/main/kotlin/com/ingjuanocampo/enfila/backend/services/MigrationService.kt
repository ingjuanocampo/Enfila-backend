package com.ingjuanocampo.enfila.backend.services

// Migration service temporarily disabled Firebase imports for basic version
/*
import com.google.firebase.firestore.Firestore
*/
import com.ingjuanocampo.enfila.backend.data.models.*
import com.ingjuanocampo.enfila.backend.data.repositories.ClientRepository
import com.ingjuanocampo.enfila.backend.data.repositories.CompanySiteRepository
import com.ingjuanocampo.enfila.backend.data.repositories.ShiftRepository
import com.ingjuanocampo.enfila.backend.data.repositories.UserRepository

interface MigrationService {
    suspend fun migrateFromFirebase()
    suspend fun getMigrationStatus(): MigrationResponse
}

class MigrationServiceImpl(
    private val userRepository: UserRepository,
    private val clientRepository: ClientRepository,
    private val shiftRepository: ShiftRepository,
    private val companySiteRepository: CompanySiteRepository
) : MigrationService {

    override suspend fun migrateFromFirebase() {
        // Implementation for Firebase migration
        // This would connect to Firebase Admin SDK and migrate data

        // For now, we'll create some sample data
        createSampleData()
    }

    private suspend fun createSampleData() {
        // Create sample company site
        val companySite = companySiteRepository.create(
            CreateCompanySiteRequest(name = "Main Restaurant")
        )

        // Create sample client
        val client = clientRepository.create(
            CreateClientRequest(id = "+1234567890", name = "John Doe")
        )

        // Create sample user
        userRepository.create(
            CreateUserRequest(
                id = "idAssignedBYGoogle1231231",
                phone = "+1234567890",
                name = "Admin User",
                companyIds = listOf(companySite.id)
            )
        )

        // Create sample shift
        shiftRepository.create(
            CreateShiftRequest(
                parentCompanySite = companySite.id,
                number = 1,
                contactId = client.id,
                notes = "First customer"
            )
        )
    }

    override suspend fun getMigrationStatus(): MigrationResponse {
        return MigrationResponse(
            success = true,
            message = "Migration service ready"
        )
    }
}
