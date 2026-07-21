package com.ingjuanocampo.enfila.backend.services

import com.ingjuanocampo.enfila.backend.data.models.ShiftState
import com.ingjuanocampo.enfila.backend.data.models.TipMilestone
import com.ingjuanocampo.enfila.backend.data.repositories.ClientRepository
import com.ingjuanocampo.enfila.backend.data.repositories.CompanySiteRepository
import com.ingjuanocampo.enfila.backend.data.repositories.ShiftRepository
import com.ingjuanocampo.enfila.backend.data.repositories.UserRepository

interface UsageMilestoneEvaluator {
    suspend fun evaluate(userId: String): Set<TipMilestone>
}

class UsageMilestoneEvaluatorImpl(
    private val userRepository: UserRepository,
    private val shiftRepository: ShiftRepository,
    private val clientRepository: ClientRepository,
    private val companySiteRepository: CompanySiteRepository,
) : UsageMilestoneEvaluator {

    override suspend fun evaluate(userId: String): Set<TipMilestone> {
        val user = userRepository.getById(userId) ?: return emptySet()
        val achieved = mutableSetOf(TipMilestone.ON_LOGIN)

        val companyId = user.companyIds?.firstOrNull() ?: return achieved
        val shifts = shiftRepository.getByCompanySite(companyId)
        val clients = clientRepository.getAll()
        val company = companySiteRepository.getById(companyId)

        if (shifts.isNotEmpty()) {
            achieved.add(TipMilestone.FIRST_SHIFT_ASSIGNED)
        }
        if (shifts.any { it.state == ShiftState.CALLING || it.state == ShiftState.FINISHED }) {
            achieved.add(TipMilestone.FIRST_SHIFT_CALLED)
        }
        if (shifts.any { it.state == ShiftState.FINISHED || it.state == ShiftState.CANCELLED }) {
            achieved.add(TipMilestone.FIRST_SHIFT_COMPLETED)
        }
        if (clients.isNotEmpty()) {
            achieved.add(TipMilestone.HAS_CLIENTS)
        }
        if (!user.name.isNullOrBlank() && !company?.name.isNullOrBlank()) {
            achieved.add(TipMilestone.PROFILE_COMPLETE)
        }

        return achieved
    }
}
