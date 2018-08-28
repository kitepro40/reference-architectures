package com.microsoft.pnp

import com.google.gson.{Gson, JsonElement, JsonParser}
import org.apache.spark.sql.Row

import scala.util.{Failure, Success, Try}

object TaxiRideMapper {

  val parser = new JsonParser()
  val gson = new Gson()
  val invalidJsonStringMessage = "not a valid json string, failed at validateJsonString method"
  val invalidTaxiRideObjectMessage = "invalid taxiride record, key for this record contains null, failed at mapJsonToTaxiRide method"
  val jsonToTaxiRideConvertionFailedMessage = "failed at mapJsonToTaxiRide method "

  def mapRowToEncrichedTaxiRideRecord(eventHubRow: Row): EnrichedTaxiDataRecord = {

    validateJsonString(eventHubRow(0).toString) match {

      case Success(_) => mapJsonToTaxiRide(eventHubRow(0).toString) match {

        case Success(taxiRide) => if (!taxiRide.key.contains("null")) {
          EnrichedTaxiDataRecord(
            isValidRecord = true,
            null,
            taxiRide,
            "",
            eventHubRow(0).toString,
            taxiRide.key,
            Utils.eventHubEnqueueTimeStringToTimeStamp(eventHubRow(1).toString).get,
            "taxiRide")
        }
        else {
          EnrichedTaxiDataRecord(
            isValidRecord = false,
            null,
            taxiRide,
            invalidTaxiRideObjectMessage,
            eventHubRow(0).toString,
            taxiRide.key,
            Utils.eventHubEnqueueTimeStringToTimeStamp(eventHubRow(1).toString).get,
            "taxiRide")
        }

        case Failure(_) => EnrichedTaxiDataRecord(
          isValidRecord = false,
          null,
          null,
          jsonToTaxiRideConvertionFailedMessage,
          eventHubRow(0).toString,
          "",
          Utils.eventHubEnqueueTimeStringToTimeStamp(eventHubRow(1).toString).get,
          "taxiRide")
      }
      case Failure(_) => EnrichedTaxiDataRecord(
        isValidRecord = false,
        null,
        null,
        invalidJsonStringMessage,
        eventHubRow(0).toString,
        "",
        Utils.eventHubEnqueueTimeStringToTimeStamp(eventHubRow(1).toString).get,
        "taxiRide")
    }
  }

  def validateJsonString(jsonString: String): Try[JsonElement] = {
    Try(parser.parse(jsonString))
  }

  def mapJsonToTaxiRide(jsonString: String): Try[TaxiRide] = {

    Try(gson.fromJson(jsonString, classOf[TaxiRide]))
  }
}