package ru.netology.nikjob.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nikjob.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT*FROM PostEntity WHERE hidden = 0 ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Query("SELECT*FROM PostEntity WHERE hidden = 0 ORDER BY id DESC")
    fun getPagingSourse(): PagingSource<Int, PostEntity>

    @Query(
        """UPDATE PostEntity SET
            hidden = 0
            WHERE hidden = 1
        """
    )
    suspend fun showAll()

    @Insert(onConflict = REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("UPDATE PostEntity SET content = :content WHERE id = :id")
    suspend fun updateContentById(id: Long, content: String)

    @Query("""
        UPDATE PostEntity SET
        likes = likes + CASE WHEN likedByMe THEN -1 ELSE +1 END,
        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
        WHERE id = :id
    """)
    suspend fun likeById(id: Long)

    @Query("""
        UPDATE PostEntity SET
        likes = likes + CASE WHEN likedByMe THEN -1 ELSE +1 END,
        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
        WHERE id = :id
    """)
    suspend fun unlikeById(id: Long)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)
    suspend fun save(post: PostEntity) =
        if (post.id == 0L) {
            insert(post)
        } else updateContentById(post.id, post.content)

    @Query("DELETE FROM PostEntity")
    suspend fun clear()
}