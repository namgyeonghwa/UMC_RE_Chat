package com.chatsoone.rechat.data.local

import android.content.Context
import androidx.room.*
import com.chatsoone.rechat.ApplicationClass.Companion.APP_DATABASE
import com.chatsoone.rechat.data.entity.Icon
import com.chatsoone.rechat.data.entity.PrivacyInformation

@Database(entities = [Icon::class, PrivacyInformation::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun iconDao(): IconDao
    abstract fun privacyInformationDao(): PrivacyInformationDao

    companion object {
        private var instance: AppDatabase? = null

        @Synchronized
        fun getInstance(context: Context): AppDatabase? {
            if (instance == null) {
                synchronized(AppDatabase::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        APP_DATABASE
                    ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
                }
            }
            return instance
        }
    }
}
