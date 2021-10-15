package com.cgobbo.espindolalg

import android.content.Context
import android.widget.Toast
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.NullPointerException

fun getDiccionarioDeCallesFromInputV2(input_file: InputStream): MutableList<Map<String, String>>{
    val inputFile = InputStreamReader(input_file)
    val reader = BufferedReader(inputFile)

    var line : String

    // leemos la primera linea para saltear los headers del csv
    reader.readLine()
    val recorridos : MutableList<Map<String, String>> = mutableListOf()
    do {
        try {
            line = reader.readLine()
            if (line == null){
                break
            }
        } catch (e: NullPointerException){
            break
        }

        val row : List<String> = line.split(";")

        val recorrido : Map<String, String> = mapOf(
            Pair("ZIP_CODE", row[0]),
            Pair("ZIP_NAME", row[1]),
            Pair("RTO", row[2])
        )

        recorridos.add(recorrido)

    } while (true)

    return recorridos
}

private fun getZipCode(scannedDataArray: List<String>): String{
    var zipcode: String
    try {

        /*
            Este try matcheara con el siguiente tipo de cadena escaneada:
            0                      |1       |2  |3 |4                      |5                 |6                                |7                    |8     |9           |10
            00008805557G8T756E11701|80000001|SUC|CP|DALMANIA SA DALMANIA SA|Maria Elena Garcia|DR BERNARDO DE IRIGOYEN 2647     |Sucursal SOLEIL-B0010|SOLEIL|BUENOS AIRES|1609
         */
        zipcode = scannedDataArray[10].trim()

        // Si no es un codigo postal no va a poder convertirlo en string y va a dar un error
        // esto se hace para determinar en que posicion del array escaneado está el codigo postal
        zipcode.toInt()
    } catch (ex: Exception) {
        try {
            /*
                Este try matcheara con el siguiente tipo de cadena escaneada:
                0                           |1    |2                 |3|4        |5   |6
                {"carrier_data":"40646415841|18148|162769A626CC2P1701| |Domicilio|1607|7-BUEE-CFI","id":"40646415841"}
             */

            zipcode = scannedDataArray[5].trim()
            zipcode.toInt()
        } catch (ex: Exception) {
            // arrojamos la excepcion
            throw ex
        }

    }
    return zipcode
}

fun matchV2(diccionarioDeCalles: MutableList<Map<String, String>>, scannedData: String, ctx: Context): MutableList<String>{

    var matchReturn = ""
    var zipCode = ""
    var returns: MutableList<String> = ArrayList()

    try {
        val scannedDataArray = convertScannedDataToArray(scannedData)
        zipCode = getZipCode(scannedDataArray)
    }catch (ex: Exception) {
        when(ex) {
            is IndexOutOfBoundsException, is NumberFormatException -> {
                // Toast.makeText(ctx, "Could not get zipCode from: \n" + scannedData, Toast.LENGTH_SHORT).show()

                returns.add(matchReturn)
                returns.add(zipCode)
                returns.add("No se pudo obteber el codigo postal del codigo QR escaneado: \n" + scannedData)
                return returns
            }
            else -> throw ex
        }
    }

    for (recorrido in diccionarioDeCalles){
        if (recorrido["ZIP_CODE"] == zipCode){
            matchReturn = recorrido["RTO"]!!
        }
    }
    returns.clear()
    returns.add(matchReturn)
    returns.add(zipCode)
    returns.add("")
    return returns
}