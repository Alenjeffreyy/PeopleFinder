package com.android.peoplefinder.activity.Db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query



@Dao
interface ApiResponseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApiResponse(apiResponse: ApiResponse)

    @Query("UPDATE ApiResponse SET response = :response WHERE key = :key")
    suspend fun updateResponseByKey(key: Int, response: String)

    @Query("SELECT * FROM ApiResponse WHERE `key` = :key")
    suspend fun getResponseByKey(key: Int): ApiResponse?

    @Query("""SELECT * FROM users WHERE first_name LIKE :query OR last_name LIKE :query OR (first_name || ' ' || last_name) LIKE :query ORDER BY first_name""")
    fun searchUsers(query: String): PagingSource<Int, User>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    @Query("SELECT * FROM users")
    fun getUsers(): PagingSource<Int, User>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Query("SELECT * FROM users ORDER BY slNo ASC")
    fun getAllUsersPagingSource(): PagingSource<Int, User>

    @Query("DELETE FROM users")
    suspend fun clearAll()

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertAll(users: List<User>)




}


