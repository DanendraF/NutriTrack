package com.example.nutritrack.data.remote.interceptor

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val firebaseAuth: FirebaseAuth
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip auth for public endpoints
        val url = originalRequest.url.toString()
        android.util.Log.d("AuthInterceptor", "Intercepting request to: $url")

        if (url.contains("/health") || url.contains("/ping")) {
            android.util.Log.d("AuthInterceptor", "Public endpoint, skipping auth")
            return chain.proceed(originalRequest)
        }

        // Check if user is logged in
        val currentUser = firebaseAuth.currentUser
        android.util.Log.d("AuthInterceptor", "Current Firebase user: ${currentUser?.email ?: "NONE"}")

        // Get Firebase ID token
        val token = runBlocking {
            try {
                val idToken = currentUser?.getIdToken(false)?.await()?.token
                android.util.Log.d("AuthInterceptor", "Firebase token obtained: ${if (idToken != null) "YES (${idToken.take(20)}...)" else "NO"}")
                idToken
            } catch (e: Exception) {
                android.util.Log.e("AuthInterceptor", "Failed to get Firebase token", e)
                null
            }
        }

        // Add Authorization header if token exists
        val newRequest = if (token != null) {
            android.util.Log.d("AuthInterceptor", "Adding Authorization header to request")
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            android.util.Log.w("AuthInterceptor", "No token available, proceeding without auth header")
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}
