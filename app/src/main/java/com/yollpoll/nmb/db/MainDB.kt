package com.yollpoll.nmb.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yollpoll.nmb.App
import com.yollpoll.nmb.model.bean.ArticleItem
import com.yollpoll.nmb.model.bean.CookieBean
import com.yollpoll.nmb.model.bean.HistoryBean
import com.yollpoll.nmb.model.bean.MySpeechBean

@Database(
    entities = [CookieBean::class, MySpeechBean::class, HistoryBean::class,ArticleItem::class],
    version = 4,
    exportSchema = false,
)
abstract class MainDB : RoomDatabase() {
    abstract fun getCookieDao(): CookieDao
    abstract fun getSpeechDao(): MySpeechDao
    abstract fun getHistoryDao(): HistoryDao
    abstract fun getArticleDao(): ArticleDao

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
                            .addMigrations(MIGRATION_1_3, MIGRATION_2_3)
                            .build()
                    }
                }
            }
            return instance!!

        }
    }
}

val MIGRATION_1_3 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE `historybean` (" +
                    "`id` INTEGER NOT NULL," +
                    " `name` TEXT NOT NULL, " +
                    "`admin` TEXT NOT NULL, " +
                    "`content` TEXT NOT NULL, " +
                    "`email` TEXT , " +
                    "`now` TEXT NOT NULL, " +
                    "`resto` TEXT, " +
                    "`sage` TEXT, " +
                    "`title` TEXT NOT NULL, " +
                    "`user_hash` TEXT NOT NULL, " +
                    "`update_time` INTEGER NOT NULL DEFAULT 'CURRENT_TIMESTAMP', " +
                    "PRIMARY KEY(`id`))"
        )
    }
}
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE HistoryBean ADD COLUMN update_time INTEGER NOT NULL DEFAULT 'CURRENT_TIMESTAMP'"
        )
    }
}