package fi.kaupunkifillarit.util

/**
 * Map nullable to another kind of nullable.
 */
fun <T : Any, U : Any> T?.map(handler: ((T) -> U)): U? {
    if (this == null) {
        return null
    } else {
        return handler(this)
    }
}
