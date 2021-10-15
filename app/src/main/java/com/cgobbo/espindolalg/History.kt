package com.cgobbo.espindolalg

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import data.DataDbHelper
import models.Match
import java.io.File

class History : AppCompatActivity() {
    // var db: DataDbHelper? = null
    // private var listView = ListView(this)

    private val CSV_HEADER = "id,rto,codigo postal,fecha\n"
    private lateinit var return_query: List<Match>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)


        val db: DataDbHelper?
        db = DataDbHelper(this)
        // Eliminamos los escaneos de dias anteriores
        db.deleteData()
        return_query = db.getData()
        val list = mutableListOf<String>()

        val adapter: ArrayAdapter<String> = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,list
        )
        val listView: ListView = findViewById(R.id.listView)
        listView.adapter = adapter

        for (item in return_query){
            list.add(0, item.toString())
        }
        adapter.notifyDataSetChanged()
    }

    fun writeFile(view: View){
        var fileContent: String = CSV_HEADER

        for (item in return_query){
            fileContent += item.getId().toString() + ',' + item.getRto() + ',' + item.getZipcode() +
                    ',' + item.getDatetime() + '\n'
        }

        //File(Environment.DIRECTORY_DOWNLOADS+"/"+"filename.txt")
        //val destPath: String = mContext.getExternalFilesDir(null).getAbsolutePath()
        File(this.externalCacheDir!!.absolutePath+"/"+"scanned_data.csv").writeText(fileContent)

        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(this.externalCacheDir!!.absolutePath+"/"+"scanned_data.csv")))
        sendIntent.type = "application/csv"
        startActivity(Intent.createChooser(sendIntent, "SHARE"))
    }

    /*
    fun writeFile(view: View?){
        val qponFile = File.createTempFile("scanned_data", "csv")
        var fileWriter: FileWriter? = null

        try {
            fileWriter = FileWriter("scanned_data.csv")

            fileWriter.append(CSV_HEADER)
            fileWriter.append('\n')


            for (item in return_query){
                fileWriter.append(item.getId().toString())
                fileWriter.append(',')
                fileWriter.append(item.getRto())
                fileWriter.append(',')
                fileWriter.append(item.getZipcode())
                fileWriter.append(',')
                fileWriter.append(item.getDatetime())
                fileWriter.append('\n')
            }
            println("Write CSV successfully!")
            fileWriter.close()

        } catch (e: Exception) {
            println("Writing CSV error!")
            e.printStackTrace()
        }


        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(qponFile))
        sendIntent.type = "application/csv"
        startActivity(Intent.createChooser(sendIntent, "SHARE"))
    }

     */
}