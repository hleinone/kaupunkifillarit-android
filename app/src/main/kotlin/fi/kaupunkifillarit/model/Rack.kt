package fi.kaupunkifillarit.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject

@JsonObject
class Rack {
    @JsonField
    var id: String = ""
    @JsonField
    var name: String = ""
    @JsonField(name = arrayOf("lat"))
    var latitude: Double = 0.0
    @JsonField(name = arrayOf("lon"))
    var longitude: Double = 0.0
    @JsonField(name = arrayOf("bikesAvailable"))
    var bikes: Int = 0

    constructor() {
    }

    constructor(id: String,
                name: String,
                latitude: Double,
                longitude: Double,
                bikes: Int) {
        this.id = id
        this.name = name
        this.latitude = latitude
        this.longitude = longitude
        this.bikes = bikes
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val tram = other as Rack?

        return id == tram!!.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
