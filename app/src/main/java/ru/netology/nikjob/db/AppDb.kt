package ru.netology.nikjob.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.room.Database
import androidx.room.RoomDatabase
import ru.netology.nikjob.dao.PostDao
import ru.netology.nikjob.dao.PostRemoteKeyDao
import ru.netology.nikjob.entity.PostEntity
import ru.netology.nikjob.entity.PostRemoteKeyEntity

@Database(entities = [PostEntity::class, PostRemoteKeyEntity::class], version = 1)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao


    class DbHelper(
        context: Context,
        dbVersion: Int,
        dbName: String,
        private val DDLs: Array<String>,
    ) :
        SQLiteOpenHelper(context, dbName, null, dbVersion) {
        override fun onCreate(db: SQLiteDatabase) {
            DDLs.forEach {
                db.execSQL(it)
            }
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            TODO("Not yet implemented")
        }

        override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            super.onDowngrade(db, oldVersion, newVersion)
        }
    }
}
