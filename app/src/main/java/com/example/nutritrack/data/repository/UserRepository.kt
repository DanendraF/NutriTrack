package com.example.nutritrack.data.repository

import com.example.nutritrack.data.local.dao.UserDao
import com.example.nutritrack.data.mapper.toDomainModel
import com.example.nutritrack.data.mapper.toEntity
import com.example.nutritrack.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface UserRepository {
    fun getCurrentUser(): Flow<User?>
    fun getUserById(userId: String): Flow<User?>
    suspend fun saveUser(user: User)
    suspend fun updateUser(user: User)
    suspend fun deleteUser(userId: String)
    suspend fun deleteAllUsers()
}

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override fun getCurrentUser(): Flow<User?> {
        return userDao.getCurrentUser().map { it?.toDomainModel() }
    }

    override fun getUserById(userId: String): Flow<User?> {
        return userDao.getUserById(userId).map { it?.toDomainModel() }
    }

    override suspend fun saveUser(user: User) {
        userDao.insertUser(user.toEntity())
    }

    override suspend fun updateUser(user: User) {
        userDao.updateUser(user.toEntity())
    }

    override suspend fun deleteUser(userId: String) {
        userDao.deleteUser(userId)
    }

    override suspend fun deleteAllUsers() {
        userDao.deleteAllUsers()
    }
}
