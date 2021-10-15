package data

import models.Match

class Tables {
    abstract class Matches{
        companion object {
            val _ID = "_id"
            val TABLE_NAME = "matches"
            val COLUMN_ZIP_CODE = "zipcode"
            val COLUMN_RTO = "rto"
            val COLUMN_DATE_TIME = "datetime"
            val matches: MutableList<Match> = ArrayList()
        }
    }
}