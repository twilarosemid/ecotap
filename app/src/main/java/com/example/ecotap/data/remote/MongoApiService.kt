package com.example.ecotap.data.remote

import com.example.ecotap.data.model.RedemptionTransaction
import com.example.ecotap.data.model.User
import com.example.ecotap.data.model.WasteDepositTransaction
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class HealthResponse(
    val status: String,
    val database: String,
    val service: String,
    val timestamp: String
)

data class UserRegistrationRequest(
    val fullName: String,
    val address: String,
    val nfcCardId: String,
    val pinCode: String
)

data class PinUpdateRequest(
    val newPin: String
)

data class DepositRequest(
    val userId: String,
    val userName: String,
    val nfcCardId: String,
    val categoryId: String,
    val categoryName: String,
    val weightKg: Double,
    val pointsEarned: Int
)

data class RedemptionRequest(
    val userId: String,
    val userName: String,
    val nfcCardId: String,
    val itemId: String,
    val itemTitle: String,
    val pointsDeducted: Int
)

interface MongoApiService {

    @GET("api/health")
    suspend fun checkHealth(): Response<HealthResponse>

    @GET("api/users")
    suspend fun getUsers(): Response<List<User>>

    @GET("api/users/nfc/{nfcId}")
    suspend fun getUserByNfc(@Path("nfcId") nfcId: String): Response<User>

    @POST("api/users")
    suspend fun registerUser(@Body request: UserRegistrationRequest): Response<User>

    @PUT("api/users/{id}/pin")
    suspend fun updateUserPin(@Path("id") userId: String, @Body request: PinUpdateRequest): Response<Map<String, String>>

    @POST("api/deposits")
    suspend fun recordDeposit(@Body request: DepositRequest): Response<WasteDepositTransaction>

    @GET("api/deposits")
    suspend fun getDeposits(): Response<List<WasteDepositTransaction>>

    @POST("api/redemptions")
    suspend fun recordRedemption(@Body request: RedemptionRequest): Response<RedemptionTransaction>

    @GET("api/redemptions")
    suspend fun getRedemptions(): Response<List<RedemptionTransaction>>
}
