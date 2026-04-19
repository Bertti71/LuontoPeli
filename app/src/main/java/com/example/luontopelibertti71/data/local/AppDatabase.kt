package com.example.luontopelibertti71.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.luontopelibertti71.data.local.dao.NatureSpotDao
import com.example.luontopelibertti71.data.local.dao.WalkSessionDao
import com.example.luontopelibertti71.data.local.entity.NatureSpot
import com.example.luontopelibertti71.data.local.entity.WalkSession

//Paikallinen Room-tietokanta — kaksi taulua: luontolöydöt ja kävelysessiot
@Database(entities = [NatureSpot::class, WalkSession::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun natureSpotDao(): NatureSpotDao
    abstract fun walkSessionDao(): WalkSessionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "luontopeli_database")
                    .fallbackToDestructiveMigration() // tyhjentää tietokannan jos versio muuttuu
                    .build().also { INSTANCE = it }
            }
        }
    }
}