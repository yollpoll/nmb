package com.yollpoll.nmb.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yollpoll.nmb.App
import com.yollpoll.nmb.model.bean.CookieBean

@Database(entities = [CookieBean::class], version = 1, exportSchema = false)
abstract class MainDB : RoomDatabase() {
    abstract fun getCookieDao(): CookieDao

    companion object {
        @Volatile
        private var instance: MainDB? = null

        fun getInstance(): MainDB {

            if (instance == null) {

                synchronized(MainDB::class) {

                    if (instance == null) {

                        instance = Room.databaseBuilder(
                            App.INSTANCE,
                            MainDB::class.java,
                            "nmb.db"
                        )
                            .allowMainThreadQueries()
                            .build()
                    }
                }
            }
            return instance!!

        }
    }
}