package com.aroncent.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MsgDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(vararg data: MsgData?)

    @Query("DELETE FROM MsgData")
    fun deleteAll()
    @Query("SELECT * FROM MsgData")
    fun loadAllData(): List<MsgData>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll( items: List<MsgData>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(item:MsgData)
    @Query("SELECT * FROM MsgData where morsecode = :morse")
    fun searchDataWithMorse(morse:String):List<MsgData>

}