package models

class Match {
    private var id: Int = 0
    private var zipcode: String = ""
    private var rto: String = ""
    private var datetime: String = ""

    constructor(id: Int,zipcode: String, rto: String, datetime: String) {
        this.id = id
        this.zipcode = zipcode
        this.rto = rto
        this.datetime = datetime
    }

    fun getId(): Int{
        return  id;
    }

    fun getZipcode():String{
        return zipcode;
    }

    fun getRto():String{
        return rto;
    }

    fun getDatetime():String{
        return datetime;
    }
    
    override fun toString(): String {
        return "Rto: $rto - CP: $zipcode\nFecha: $datetime"
    }
}