package com.cgobbo.espindolalg

import android.content.Context
import android.widget.Toast
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.NullPointerException

fun matchImpar(addressNumber: Int, desde: Int, hasta: Int): Boolean {
    if (addressNumber %2 != 0){
        if (addressNumber in desde..hasta){
            return true
        }
    }
    return false
}

fun matchPar(addressNumber: Int, desde: Int, hasta: Int): Boolean {
    if (addressNumber %2 == 0){
        if (addressNumber in desde..hasta){
            return true
        }
    }
    return false
}

fun matchAny(addressNumber: Int, desde: Int, hasta: Int): Boolean {
    if (addressNumber in desde..hasta){
        return true
    }
    return false
}

fun par(addressNumber: Int): Boolean{
    if (addressNumber %2 == 0){
        return true
    }
    return false
}

fun getDiccionarioDeCallesFromInput(input_file: InputStream): MutableList<Map<String, MutableList<Map<String, Int>>>> {
    val inputFile = InputStreamReader(input_file)
    val reader = BufferedReader(inputFile)

    var line : String
    val diccionarioDeCalles: MutableList<Map<String, MutableList<Map<String, Int>>>> = mutableListOf()

    // leemos la primera linea para saltear los headers del csv
    reader.readLine()
    do {
        try {
            line = reader.readLine()
            if (line == null){
                break
            }
        } catch (e: NullPointerException){
            break
        }

        val row : List<String> = line.split(",")



        // Creamos una lista temporal donde vamos a ir guardando todos los desde hasta y rto
        val recorridos : MutableList<Map<String, Int>> = mutableListOf()

        // Creamos esta variable para ir guardando todos los desde hasta
        val calle : MutableMap<String, MutableList<Map<String, Int>>> = mutableMapOf()

        // Recorremos cada 4 la lineas, ya que sabemos que es 'desde', 'hasta', 'rto', 'par/impar'
        for (value in 1..row.size step 4){
            // Chequeamos que no haya llegado al final de la cadena
            try {
                row[value]
            } catch (e: IndexOutOfBoundsException){
                break
            }
            if (row[value] == ""){
                break
            }

            val matchType : Int = when {
                row[value + 3].uppercase() == "IMPAR" -> {
                    0
                }
                row[value + 3].uppercase() == "PAR" -> {
                    1
                }
                else -> {
                    2
                }
            }

            val recorrido : Map<String, Int> = mapOf(
                Pair("Desde", (row[value].toInt())),
                Pair("Hasta", row[value + 1].toInt()),
                Pair("Rto", row[value + 2].toInt()),
                Pair("MatchType", matchType)
            )
            recorridos.add(recorrido)
        }

        // Nos guardamos el nombre de la calle como clave y todos los 'Desde, Hasta, Rto'
        // como  valor guardamos todos los recorridos de esa calle
        calle[row[0]] = recorridos

        diccionarioDeCalles.add(calle)

    } while (true)

    return diccionarioDeCalles
}

private fun getNumericValues(cadena: String): Int {

    val result : MutableList<Int> = mutableListOf()
    var numberStr = ""
    for(i : Int in cadena.indices){
        val c: Char = cadena[i]

        if(c in '0'..'9'){
            numberStr += c
            if(i == cadena.length - 1){
                result.add(numberStr.toInt())
            }
        }else if(!numberStr.isBlank()){
            result.add(numberStr.toInt())
            numberStr = ""
        }

    }

    return result.joinToString(File.separator, "").toInt()
}

private fun removeNumbersFromString(string: String): String{
    val re = Regex("[0-9]")
    return re.replace(string, "")
}

fun convertScannedDataToArray(scannedData: String): List<String>{
    return scannedData.split("|")
}

private fun getAddress(scannedDataArray: List<String>): String{
    return scannedDataArray[6].trimEnd()
}

private fun getAddressNumber(address: String): Int{
    return getNumericValues(address)
}

private fun getAddressName(address: String): String{
    return removeNumbersFromString(address).trimEnd().uppercase()
}

fun match(diccionarioDeCalles: MutableList<Map<String, MutableList<Map<String, Int>>>>, scannedData: String, ctx: Context): String{
    val addressName: String
    val addressNumber: Int

    var matchReturn = ""
    try {
        val scannedDataArray = convertScannedDataToArray(scannedData)
        val address = getAddress(scannedDataArray)
        addressName = getAddressName(address)
        addressNumber = getAddressNumber(address)

    } catch (ex: Exception) {
        when(ex) {
            is IndexOutOfBoundsException, is NumberFormatException -> {
                Toast.makeText(ctx, "Could not get address from: \n" + scannedData, Toast.LENGTH_SHORT).show()
                return matchReturn
            }
            else -> throw ex
        }
    }

    for (calle in diccionarioDeCalles){
        if (calle.containsKey(addressName)){
            val recorridos : MutableList<Map<String, Int>>? = calle.get(addressName)

            if (recorridos != null) {
                for (recorrido in recorridos){
                    val desde: Int? = recorrido["Desde"]
                    val hasta: Int? = recorrido["Hasta"]
                    if (recorrido.get("MatchType") == 0 && !par(addressNumber)) {
                        if (desde != null && hasta != null) {
                            if (matchImpar(addressNumber, desde, hasta)){
                                matchReturn = recorrido.get("Rto").toString()
                                break
                            }
                        }

                    }else if (recorrido.get("MatchType") == 1 && par(addressNumber)) {
                        if (desde != null && hasta != null) {
                            if (matchPar(addressNumber, desde, hasta)){
                                matchReturn = recorrido.get("Rto").toString()
                                break
                            }
                        }

                    }else if (recorrido.get("MatchType") == 2){
                        if (desde != null && hasta != null) {
                            if (matchAny(addressNumber, desde, hasta)){
                                matchReturn = recorrido.get("Rto").toString()
                                break
                            }
                        }
                    }
                }
            }
        }
    }
    return matchReturn
}