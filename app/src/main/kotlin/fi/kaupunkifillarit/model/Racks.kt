package fi.kaupunkifillarit.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject

@JsonObject
class Racks {
    @JsonField(name = arrayOf("bikeRentalStations"))
    var racks: Set<Rack> = emptySet()

    constructor() {
    }

    constructor(racks: Set<Rack>) {
        this.racks = racks
    }
}
