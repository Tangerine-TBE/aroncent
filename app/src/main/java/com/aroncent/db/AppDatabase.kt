package com.aroncent.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MsgData::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun msgDao(): MsgDao?

}