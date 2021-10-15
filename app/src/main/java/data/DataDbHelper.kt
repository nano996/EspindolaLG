package data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import models.Match
import java.text.SimpleDateFormat
import java.util.*

class DataDbHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null,
DATABASE_VERSION) {
    private val db: SQLiteDatabase
    private val values: ContentValues
    private val sdf = SimpleDateFormat("yyyy-MM-dd")
    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "matches"

    }
    init {
        db = this.writableDatabase
        values = ContentValues()
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL("CREATE TABLE " + Tables.Matches.TABLE_NAME + " (" +
                Tables.Matches._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Tables.Matches.COLUMN_RTO + " TEXT NOT NULL," +
                Tables.Matches.COLUMN_ZIP_CODE + " TEXT NOT NULL," +
                Tables.Matches.COLUMN_DATE_TIME + " TEXT NOT NULL);")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    fun insert (match: List<Match>){
        values.put(Tables.Matches.COLUMN_RTO, match[0].getRto())
        values.put(Tables.Matches.COLUMN_ZIP_CODE, match[0].getZipcode())
        values.put(Tables.Matches.COLUMN_DATE_TIME, match[0].getDatetime())
        db.insert(Tables.Matches.TABLE_NAME, null, values)
    }

    fun getData(): MutableList<Match>{
        Tables.Matches.matches.clear()
        val columns = arrayOf(Tables.Matches._ID, Tables.Matches.COLUMN_RTO,
                              Tables.Matches.COLUMN_ZIP_CODE, Tables.Matches.COLUMN_DATE_TIME)

        val cursor = db.query(Tables.Matches.TABLE_NAME, columns, null, null,
            null, null, null)
        if (cursor.moveToFirst()){
            do {
                Tables.Matches.matches.add(
                    Match(cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3)
                    )
                )
            } while (cursor.moveToNext())
        }
        return Tables.Matches.matches
    }

    fun deleteData() {
        db.delete(
            Tables.Matches.TABLE_NAME,
            Tables.Matches.COLUMN_DATE_TIME + "<\"" + sdf.format(Date()) + "\"",
            null
        )
    }
}
