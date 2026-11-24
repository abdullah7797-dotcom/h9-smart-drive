package com.h9smartdrive.app

import android.app.Application
import androidx.room.Room
import com.h9smartdrive.app.data.local.H9Database

class H9Application : Application() {
    val database: H9Database by lazy {
        Room.databaseBuilder(this, H9Database::class.java, "h9-smart-drive.db")
            .fallbackToDestructiveMigration()
            .build()
    }
}
