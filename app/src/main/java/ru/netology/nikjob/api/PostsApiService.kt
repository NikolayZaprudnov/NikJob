package ru.netology.nikjob.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.nikjob.dto.Media
import ru.netology.nikjob.dto.Post
import ru.netology.nikjob.dto.PushToken
import ru.netology.nikjob.dto.User


interface PostsApiService {
    @GET("posts")
    suspend fun getAll(): Response<List<Post>>

    @GET("posts/latest")
    suspend fun getLatest(@Query("count") count: Int): Response<List<Post>>

    @POST("posts")
    suspend fun save(@Body post: Post): Response<Post>

    @GET("posts/{postId}/newer")
    suspend fun getNewer(@Path("postId") id: Long): Response<List<Post>>

    @GET("posts/{postId}/before")
    suspend fun getBefore(
        @Path("postId") id: Long,
        @Query("count") count: Int,
    ): Response<List<Post>>

    @GET("posts/{postId}/after")
    suspend fun getAfter(@Path("postId") id: Long, @Query("count") count: Int): Response<List<Post>>

    @GET("posts/{postId}")
    suspend fun getPostById(@Path("postId") id: Long): Response<Post>

    @DELETE("posts/{id}")
    suspend fun removeById(@Path("id") id: Long): Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun unlikeById(@Path("id") id: Long): Response<Post>

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Response<Post>

    @POST("posts/{id}/repost")
    suspend fun repostById(@Path("id") id: Long): Response<Post>

    @POST("users/push-tokens")
    suspend fun saveToken(@Body pushToken: PushToken): Response<Unit>


}

interface MediaApiService {
    @Multipart
    @POST("media")
    suspend fun uploadPhoto(@Part file: MultipartBody.Part): Response<Media>
}

interface AuthApiService {
    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun updateUser(
        @Field("login") login: String,
        @Field("password") pass: String,
    ): Response<User>

    @FormUrlEncoded
    @POST("users/registration")
    suspend fun registerUser(
        @Field("login") login: String,
        @Field("password") pass: String,
        @Field("name") name: String,
    ): Response<User>

    @Multipart
    @POST("users/registration")
    suspend fun registerWithPhoto(
        @Part("login") login: RequestBody,
        @Part("password") pass: RequestBody,
        @Part("name") name: RequestBody,
        @Part media: MultipartBody.Part,
    ): Response<User>
}




