package me.jeybi.uztaxi.utils

import com.mapbox.geojson.Point
import java.util.*
import kotlin.collections.ArrayList

class PolyLineUtils {
    companion object {

        data class Coordinate(val longitude: Double, val latitude: Double)

        fun encode(coords: List<Coordinate>): String {
            val result: MutableList<String> = mutableListOf()

            var prevLat = 0
            var prevLong = 0

            for ((long, lat) in coords) {
                val iLat = (lat * 1e5).toInt()
                val iLong = (long * 1e5).toInt()

                val deltaLat = encodeValue(iLat - prevLat)
                val deltaLong = encodeValue(iLong - prevLong)

                prevLat = iLat
                prevLong = iLong

                result.add(deltaLat)
                result.add(deltaLong)
            }

            return result.joinToString("")
        }

        private fun encodeValue(value: Int): String {
            // Step 2 & 4
            var actualValue = if (value < 0) (value shl 1).inv() else (value shl 1)

            // Step 5-8
            val chunks: List<Int> = splitIntoChunks(actualValue)

            // Step 9-10
            return chunks.map { (it + 63).toChar() }.joinToString("")
        }

        private fun splitIntoChunks(toEncode: Int): List<Int> {
            // Step 5-8
            val chunks = mutableListOf<Int>()
            var value = toEncode
            while (value >= 32) {
                chunks.add((value and 31) or (0x20))
                value = value shr 5
            }
            chunks.add(value)
            return chunks
        }

        /**
         * Decodes a polyline that has been encoded using Google's algorithm
         * (http://code.google.com/apis/maps/documentation/polylinealgorithm.html)
         *
         * code derived from : https://gist.github.com/signed0/2031157
         *
         * @param polyline-string
         * @return (long,lat)-Coordinates
         */

        open fun decode(encodedPath: String, precision: Int): ArrayList<Point> {
            val len = encodedPath.length

            // OSRM uses precision=6, the default Polyline spec divides by 1E5, capping at precision=5
            val factor = Math.pow(10.0, precision.toDouble())

            // For speed we preallocate to an upper bound on the final length, then
            // truncate the array before returning.
            val path: ArrayList<Point> = ArrayList()
            var index = 0
            var lat = 0
            var lng = 0
            while (index < len) {
                var result = 1
                var shift = 0
                var temp: Int
                do {
                    temp = encodedPath[index++].toInt() - 63 - 1
                    result += temp shl shift
                    shift += 5
                } while (temp >= 0x1f)
                lat += if (result and 1 != 0) (result shr 1).inv() else result shr 1
                result = 1
                shift = 0
                do {
                    temp = encodedPath[index++].toInt() - 63 - 1
                    result += temp shl shift
                    shift += 5
                } while (temp >= 0x1f)
                lng += if (result and 1 != 0) (result shr 1).inv() else result shr 1
                path.add(Point.fromLngLat(lng / factor, lat / factor))
            }
            return path
        }

        private fun round(value: Double, precision: Int) =
            (value * Math.pow(10.0, precision.toDouble())).toInt().toDouble() / Math.pow(
                10.0,
                precision.toDouble()
            )

        /**
         * https://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm
         */

    }
}
