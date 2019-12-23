package com.home.androidbrowserdemo.model

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.home.androidbrowserdemo.controller.unit.RecordUnit
import java.util.*

class RecordAction(context: Context?) {

    private var database: SQLiteDatabase? = null
    private val helper: RecordHelper = RecordHelper(context)

    fun open(rw: Boolean) {
        database = if (rw) helper.writableDatabase else helper.readableDatabase
    }

    fun close() {
        helper.close()
    }

    fun addHistory(record: Record?) {
        if (record?.title == null || record.title!!.trim { it <= ' ' }.isEmpty()
            || record.url == null || record.url!!.trim().isEmpty() || record.time < 0L
        ) {
            return
        }
        val values = ContentValues()
        values.put(RecordUnit.COLUMN_TITLE, record.title!!.trim { it <= ' ' })
        values.put(RecordUnit.COLUMN_URL, record.url!!.trim())
        values.put(RecordUnit.COLUMN_TIME, record.time)
        database!!.insert(RecordUnit.TABLE_HISTORY, null, values)
    }

    fun checkHistory(url: String?): Boolean {
        if (url == null || url.trim { it <= ' ' }.isEmpty()) {
            return false
        }
        val cursor = database!!.query(
            RecordUnit.TABLE_HISTORY,
            arrayOf(RecordUnit.COLUMN_URL),
            RecordUnit.COLUMN_URL + "=?",
            arrayOf(url.trim { it <= ' ' }), null, null, null
        )
        if (cursor != null) {
            val result = cursor.moveToFirst()
            cursor.close()
            return result
        }
        return false
    }

    fun checkDomain(domain: String?, table: String): Boolean {
        if (domain == null || domain.trim { it <= ' ' }.isEmpty()) {
            return false
        }
        val cursor = database!!.query(
            table, arrayOf(RecordUnit.COLUMN_DOMAIN),
            RecordUnit.COLUMN_DOMAIN + "=?",
            arrayOf(domain.trim { it <= ' ' }), null, null, null
        )
        if (cursor != null) {
            val result = cursor.moveToFirst()
            cursor.close()
            return result
        }
        return false
    }

    fun deleteHistoryItemByURL(domain: String?) {
        if (domain == null || domain.trim { it <= ' ' }.isEmpty()) {
            return
        }
        database!!.execSQL("DELETE FROM " + RecordUnit.TABLE_HISTORY + " WHERE " + RecordUnit.COLUMN_URL + " = " + "\"" + domain.trim { it <= ' ' } + "\"")
    }

    fun clearHome() {
        database!!.execSQL("DELETE FROM " + RecordUnit.TABLE_GRID)
    }

    fun clearHistory() {
        database!!.execSQL("DELETE FROM " + RecordUnit.TABLE_HISTORY)
    }

    private fun getRecord(cursor: Cursor): Record {
        val record = Record()
        record.title = cursor.getString(0)
        record.url = cursor.getString(1)
        record.time = cursor.getLong(2)
        return record
    }

    fun listEntries(activity: Activity?, listAll: Boolean): List<Record> {
        val list = ArrayList<Record>()
        var cursor: Cursor
        if (listAll) {
            //add startSite
            cursor = database!!.query(
                RecordUnit.TABLE_GRID,
                arrayOf(
                    RecordUnit.COLUMN_TITLE, RecordUnit.COLUMN_URL,
                    RecordUnit.COLUMN_FILENAME, RecordUnit.COLUMN_ORDINAL
                ), null, null, null, null,
                RecordUnit.COLUMN_ORDINAL
            )
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                list.add(getRecord(cursor))
                cursor.moveToNext()
            }
            cursor.close()
            //add bookmarks
            val db = BookmarkList(activity)
            db.open()
            cursor = db.fetchAllForSearch()
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                list.add(getRecord(cursor))
                cursor.moveToNext()
            }
            cursor.close()
        }
        //add history
        cursor = database!!.query(
            RecordUnit.TABLE_HISTORY,
            arrayOf(RecordUnit.COLUMN_TITLE, RecordUnit.COLUMN_URL, RecordUnit.COLUMN_TIME),
            null,
            null,
            null,
            null,
            RecordUnit.COLUMN_TIME + " asc"
        )
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            list.add(getRecord(cursor))
            cursor.moveToNext()
        }
        cursor.close()
        return list
    }

    @SuppressLint("Recycle")
    fun listDomains(table: String): List<String> {
        val list = ArrayList<String>()
        val cursor = database!!.query(
            table,
            arrayOf(RecordUnit.COLUMN_DOMAIN), null, null, null, null,
            RecordUnit.COLUMN_DOMAIN
        ) ?: return list
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            list.add(cursor.getString(0))
            cursor.moveToNext()
        }
        cursor.close()
        return list
    }
}
