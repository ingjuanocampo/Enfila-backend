package com.ingjuanocampo.enfila.backend.services

import com.ingjuanocampo.enfila.backend.data.models.*
import com.ingjuanocampo.enfila.backend.data.repositories.TipRepository

interface TipService {
    suspend fun publishTip(request: PublishTipRequest): ApiResponse<Tip>
    suspend fun getPublishedTips(): ApiResponse<List<Tip>>
    suspend fun getTipsForUser(userId: String): ApiResponse<List<TipWithStatus>>
    suspend fun deleteTip(id: String): ApiResponse<Unit>
}

class TipServiceImpl(
    private val tipRepository: TipRepository,
    private val usageMilestoneEvaluator: UsageMilestoneEvaluator,
) : TipService {

    override suspend fun publishTip(request: PublishTipRequest): ApiResponse<Tip> {
        return try {
            tipRepository.publish(request).toApiResponse()
        } catch (e: Exception) {
            "Failed to publish tip: ${e.message}".toErrorResponse()
        }
    }

    override suspend fun getPublishedTips(): ApiResponse<List<Tip>> {
        return try {
            tipRepository.getPublishedOrdered().toApiResponse()
        } catch (e: Exception) {
            "Failed to get tips: ${e.message}".toErrorResponse()
        }
    }

    override suspend fun getTipsForUser(userId: String): ApiResponse<List<TipWithStatus>> {
        return try {
            val achieved = usageMilestoneEvaluator.evaluate(userId)
            val tips = tipRepository.getPublishedOrdered().map { tip ->
                TipWithStatus(
                    id = tip.id,
                    order = tip.order,
                    question = tip.question,
                    answer = tip.answer,
                    milestone = tip.milestone,
                    isUnlocked = achieved.contains(tip.milestone),
                )
            }
            tips.toApiResponse()
        } catch (e: Exception) {
            "Failed to get tips for user: ${e.message}".toErrorResponse()
        }
    }

    override suspend fun deleteTip(id: String): ApiResponse<Unit> {
        return try {
            val deleted = tipRepository.delete(id)
            if (deleted) {
                Unit.toApiResponse()
            } else {
                "Tip not found".toErrorResponse()
            }
        } catch (e: Exception) {
            "Failed to delete tip: ${e.message}".toErrorResponse()
        }
    }
}
