package com.example.androidcrud.data.local

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.androidcrud.data.local.EntryEntity
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val TEST_DB = "migration-test"

    @After
    fun cleanup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(TEST_DB)
    }

    @Test
    @Throws(IOException::class)
    fun migrate1To2() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Create database at version 1 manually
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(TEST_DB)
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("CREATE TABLE IF NOT EXISTS `entries` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `entryValue` INTEGER NOT NULL)")
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                    // No-op for manual creation
                }
            })
            .build()
        
        val helper = FrameworkSQLiteOpenHelperFactory().create(config)
        val db = helper.writableDatabase
        
        // 2. Insert data at version 1
        db.execSQL("INSERT INTO entries (timestamp, entryValue) VALUES (1000, 123)")
        db.execSQL("INSERT INTO entries (timestamp, entryValue) VALUES (2000, 456)")
        db.close()

        // 3. Open database with Room at version 2 with migration
        val appDatabase = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            TEST_DB
        )
        .addMigrations(AppDatabase.MIGRATION_1_2)
        .build()

        // 4. Verify data
        val dao = appDatabase.entryDao()
        val entries = dao.getAllEntries().first()

        assertEquals(2, entries.size)
        
        // Check first entry
        val entry1 = entries.find { it.entryValue == 123 }!!
        assertEquals(1000L, entry1.timestamp.toEpochMilli())
        assertNull(entry1.note)

        // Check second entry
        val entry2 = entries.find { it.entryValue == 456 }!!
        assertEquals(2000L, entry2.timestamp.toEpochMilli())
        assertNull(entry2.note)
        
        // 5. Verify we can add an entry with a note
        val newEntry = EntryEntity(
            timestamp = java.time.Instant.now(),
            entryValue = 789,
            note = "Migrated successfully"
        )
        dao.insertEntry(newEntry)
        
        val updatedEntries = dao.getAllEntries().first()
        assertEquals(3, updatedEntries.size)
        val savedNewEntry = updatedEntries.find { it.entryValue == 789 }
        assertEquals("Migrated successfully", savedNewEntry?.note)
    }
}
