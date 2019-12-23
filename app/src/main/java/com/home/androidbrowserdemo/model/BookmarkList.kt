package com.home.androidbrowserdemo.model

import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.preference.PreferenceManager
import java.util.*

// establish connection with SQLiteDataBase
class BookmarkList(private val c: Context?) {

    companion object {
        //define static variable
        private const val dbVersion = 7
        private const val dbName = "pass_DB_v01.db"
        private const val dbTable = "pass"
    }

    private var sqlDb: SQLiteDatabase? = null

    private class DatabaseHelper internal constructor(context: Context?) :
        SQLiteOpenHelper(context, dbName, null, dbVersion) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS $dbTable (_id INTEGER PRIMARY KEY autoincrement, pass_title, pass_content, pass_icon, pass_attachment, pass_creation, UNIQUE(pass_content))")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $dbTable")
            onCreate(db)
        }
    }

    @Throws(SQLException::class)
    fun open() {
        val dbHelper = DatabaseHelper(c)
        sqlDb = dbHelper.writableDatabase
    }

    //fetch data
    fun fetchAllData(activity: Context): Cursor? {
        val sp = PreferenceManager.getDefaultSharedPreferences(activity)
        val columns = arrayOf(
            "_id",
            "pass_title",
            "pass_content",
            "pass_icon",
            "pass_attachment",
            "pass_creation"
        )
        when (Objects.requireNonNull(sp.getString("sortDBB", "title"))) {
            "title" -> return sqlDb!!.query(
                dbTable,
                columns,
                null,
                null,
                null,
                null,
                "pass_title" + " COLLATE NOCASE DESC;"
            )

            "icon" -> {
                val orderBy =
                    "pass_creation" + " COLLATE NOCASE DESC;" + "," + "pass_title" + " COLLATE NOCASE ASC;"
                return sqlDb!!.query(dbTable, columns, null, null, null, null, orderBy)
            }
        }
        return null
    }

    //fetch data
    fun fetchAllForSearch(): Cursor {
        val columns =
            arrayOf("pass_title", "pass_content", "pass_icon", "pass_attachment", "pass_creation")
        return sqlDb!!.query(
            dbTable,
            columns,
            null,
            null,
            null,
            null,
            "pass_title" + " COLLATE NOCASE DESC;"
        )
    }

    //fetch data by filter
    @Throws(SQLException::class)
    fun fetchDataByFilter(inputText: String?, filterColumn: String): Cursor? {
        val row: Cursor?
        var query = "SELECT * FROM $dbTable"
        if (inputText == null || inputText.length == 0) {
            row = sqlDb!!.rawQuery(query, null)
        } else {
            query = "SELECT * FROM $dbTable WHERE $filterColumn like '%$inputText%'"
            row = sqlDb!!.rawQuery(query, null)
        }
        row?.moveToFirst()
        return row
    }
}