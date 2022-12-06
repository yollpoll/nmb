package com.yollpoll.nmb.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yollpoll.nmb.App
import com.yollpoll.nmb.model.bean.*

@Database(
    entities = [CookieBean::class, MySpeechBean::class, HistoryBean::class, ArticleItem::class,
        ShieldArticle::class, ForumDetail::class, DraftBean::class],
    version = 6,
    exportSchema = false,
)
abstract class MainDB : RoomDatabase() {
    abstract fun getCookieDao(): CookieDao
    abstract fun getSpeechDao(): MySpeechDao
    abstract fun getHistoryDao(): HistoryDao
    abstract fun getArticleDao(): ArticleDao
    abstract fun getForumDao(): ForumDao
    abstract fun getDraftDao(): DraftDao

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
                            .addMigrations(
                                MIGRATION_1_3,
                                MIGRATION_2_3,
                                MIGRATION_3_4,
                                MIGRATION_4_5,
                                MIGRATION_5_6,
                            )
                            .fallbackToDestructiveMigration()//没有迁移路径时会破坏性的重建表
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
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE `ArticleItem` (" +
                    "`admin` TEXT NOT NULL," +
                    "`content` TEXT NOT NULL, " +
                    "`email` TEXT, " +
                    "`ext` TEXT NOT NULL, " +
                    "`id` INTEGER NOT NULL, " +
                    "`img` TEXT NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`now` TEXT NOT NULL, " +
                    "`ReplyCount` TEXT , " +
                    "`title` TEXT NOT NULL, " +
                    "`user_hash` TEXT NOT NULL, " +
                    "`master` TEXT, " +
                    "`page` INTEGER NOT NULL DEFAULT 1, " +
                    "`sage` INTEGER NOT NULL, " +
                    "`Hide` INTEGER NOT NULL, " +
                    "`replyTo` TEXT, " +
                    "PRIMARY KEY(`id`))"
        )
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE `ShieldArticle` (" +
                    "`articleId` TEXT NOT NULL, " +
                    "PRIMARY KEY(`articleId`))"
        )

        database.execSQL(
            "CREATE TABLE `forum` (" +
                    "`createdAt` TEXT, " +
                    "`fgroup` TEXT, " +
                    "`id:` TEXT NOT NULL, " +
                    "`interval` TEXT , " +
                    "`msg` TEXT NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`showName` TEXT , " +
                    "`sort` TEXT , " +
                    "`status` TEXT , " +
                    "`updateAt` TEXT, " +
                    "`show` INTEGER NOT NULL DEFAULT 1, " +
                    "PRIMARY KEY(`id`))"
        )
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE `draft` (" +
                    "`fid` TEXT NOT NULL, " +
                    "`f_name` TEXT NOT NULL, " +
                    "`mask` TEXT NOT NULL, " +
                    "`email` TEXT NOT NULL, " +
                    "`reply` TEXT , " +
                    "`id` INTEGER, " +
                    "`title` TEXT, " +
                    "`content` TEXT NOT NULL, " +
                    "`update_time` INTEGER NOT NULL DEFAULT 'CURRENT_TIMESTAMP', " +
                    "`img` TEXT , " +
                    "PRIMARY KEY(`id`))"
        )
    }
}