package ru.netology.nikjob.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nikjob.entity.EventsEntity
import ru.netology.nikjob.entity.PostEntity

@Dao
interface EventDao {
    @Query("SELECT * FROM EventsEntity ORDER BY id DESC")
    fun getAllEvents(): Flow<List<EventsEntity>>

    @Query("SELECT*FROM EventsEntity ORDER BY id DESC")
    fun getPagingSourse(): PagingSource<Int, EventsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(event: EventsEntity)

    @Query("UPDATE EventsEntity SET content = :content WHERE id = :id")
    suspend fun updateContentById(id: Long, content: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(event: List<EventsEntity>)

    @Query("UPDATE EventsEntity SET content = :content WHERE id = :id")
    suspend fun updateContentEventsById(id: Long, content: String)
    suspend fun saveEvent(event: EventsEntity) =
        if (event.id == 0L) insertEvents(event) else updateContentById(event.id, event.content)

    @Query(
        """
        UPDATE EventsEntity SET
        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
        WHERE id = :id
        """
    )
    suspend fun likeByIdEvent(id: Long)


    @Query("DELETE FROM EventsEntity WHERE id = :id")
    suspend fun removeByIdEvent(id: Long)
}