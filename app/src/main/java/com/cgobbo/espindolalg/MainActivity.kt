ttpackage com.cgobbo.espindolalg

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cgobbo.espindolalg.RequestPermissionHandler.RequestPermissionListener
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import data.DataDbHelper
import models.Match
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val CAMERA_REQUEST_CODE = 101

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName
    private var barcodeView: DecoratedBarcodeView? = null
    private var beepManager: BeepManager? = null
    private var lastText: String? = null
    private var diccionarioDeCalles: MutableList<Map<String, MutableList<Map<String, Int>>>> = mutableListOf()
    private var diccionarioDeCallesV2: MutableList<Map<String, String>> = mutableListOf()
    private var list: MutableList<Match> = ArrayList()
    var db: DataDbHelper? = null
    private val sdf = SimpleDateFormat("yyyy-MM-dd")
    private var mRequestPermissionHandler: RequestPermissionHandler? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = DataDbHelper(this)
        //addDatos(0, "403", "1607", sdf.format(Date()))
        mRequestPermissionHandler = RequestPermissionHandler()

        // diccionarioDeCalles = getDiccionarioDeCallesFromInput(assets.open("input_viejo.csv"))

        diccionarioDeCallesV2 = getDiccionarioDeCallesFromInputV2(assets.open("input.csv"))

        // matchV2(diccionarioDeCallesV2, "00008805557G8T756E11701|80000001|SUC|CP|DALMANIA SA DALMANIA SA|Maria Elena Garcia|DR BERNARDO DE IRIGOYEN 2647     |Sucursal SOLEIL-B0010|SOLEIL|BUENOS AIRES|1609", this)
        setupPermissions()

        // val diccionarioDeCalles: MutableList<Map<String, MutableList<Map<String, Int>>>> = getDiccionarioDeCallesFromInput(reader);
        barcodeView = findViewById<View>(R.id.barcode_scanner) as DecoratedBarcodeView
        val formats: Collection<BarcodeFormat> =
            Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39)
        barcodeView!!.barcodeView.decoderFactory = DefaultDecoderFactory(formats)
        barcodeView!!.setStatusText("")
        barcodeView!!.initializeFromIntent(intent)
        barcodeView!!.decodeContinuous(callback)

        beepManager = BeepManager(this)

    }

    private fun addDatos(rto: String, zipCode: String){
        val dateTime = sdf.format(Date())
        val id = 0
        list.add(Match(id, rto, zipCode, dateTime))
        db!!.insert(list)
        list.clear()
        // val return_query: List<Match> = db!!.getData()
        // print(1)
    }

    override fun onResume() {
        super.onResume()
        barcodeView!!.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView!!.pause()
    }

    fun pause(view: View?) {
        barcodeView!!.pause()
    }

    fun resume(view: View?) {
        barcodeView!!.resume()
    }

    fun triggerScan(view: View?) {
        barcodeView!!.decodeSingle(callback)
    }

    fun showHistory(view: View?){
        val intent = Intent(this, History::class.java)
        val return_query: List<Match> = db!!.getData()
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        // intent.putExtra("return_query", return_query)
        startActivity(intent)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return barcodeView!!.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }


    private val callback: BarcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            if (result.text == null || result.text == lastText) {
                // Prevent duplicate scans
                return
            }
            lastText = result.text


            runOnUiThread {

                val match_found : MutableList<String> = matchV2(diccionarioDeCallesV2, result.text, this@MainActivity)
                val text_to_show: String

                if (match_found[0] != "" && match_found[1] != ""){
                    text_to_show = match_found[0] + " " + match_found[1]
                    addDatos(match_found[0], match_found[1])
                } else {
                    // text_to_show = "Could not get rto from scanned data\n" + result.text
                    text_to_show = match_found[2]
                }

                Toast.makeText(applicationContext, text_to_show, Toast.LENGTH_SHORT).show()
                // barcodeView!!.setStatusText(result.text)
                barcodeView!!.setStatusText(text_to_show)
                beepManager!!.playBeepSoundAndVibrate()
            }

            //Added preview of scanned barcode
            //ImageView imageView = (ImageView) findViewById(R.id.barcodePreview);
            //imageView.setImageBitmap(result.getBitmapWithResultPoints(Color.YELLOW));
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    private fun setupPermissions(){
        cameraPermissions()
        storagePermissions()
    }

    private fun storagePermissions(){
        mRequestPermissionHandler!!.requestPermission(this, arrayOf( Manifest.permission.WRITE_EXTERNAL_STORAGE
        ), 123, object : RequestPermissionListener {
            override fun onSuccess() {
                Toast.makeText(
                    this@MainActivity,
                    "request permission success",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onFailed() {
                Toast.makeText(
                    this@MainActivity,
                    "request permission failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

    }

    private fun cameraPermissions(){

        val permission = ContextCompat.checkSelfPermission(this,
            android.Manifest.permission.CAMERA)

        if (permission != PackageManager.PERMISSION_GRANTED){
            makeRequest()
        }
    }

    private fun makeRequest(){
        ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,
                        "Necesita darle permisos de uso de camara",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}