import com.ibm.codait.SparkFlightConnector
import org.apache.arrow.flight.{FlightClient, Location}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.rand
import org.apache.spark.sql.functions.col


object flight_example {
  //import subprocess


  //from pyspark.sql.pandas.types import to_arrow_schema

  //import pyarrow as pa
  //import pyarrow.flight as pa_flight

  def main(args: Array[String]): Unit = {
    //Location of the Flight Service
    val host: String = "127.0.0.1"
    val port: String = "8888"

    // Unique identifier for flight data
    val flight_desc = "spark-flight-descriptor"

    // --------------------------------------------- #
    // Run Spark to put Arrow data to Flight Service #
    // --------------------------------------------- #
    val spark = SparkSession.builder.appName("spark-flight").getOrCreate()

    val df = spark.range(10).select((col("id") % 2).alias("label")).withColumn("data", rand())

    df.show(10)

    //Put the Spark DataFrame to the Flight Service
    new SparkFlightConnector().put(df, host, port.toInt, flight_desc)

    //------------------------------------------------------------- #
    // Create a Pandas DataFrame from a pyarrow Flight client reader #
    // ------------------------------------------------------------- #

    //Connect to the Flight service and get endpoints from FlightInfo
    val client = FlightClient.builder().build()

    val desc = client.FlightDescriptor.for_path(flight_desc)
    val info = client.getInfo(desc)
    val endpoints = info.getEndpoints

    //Read all flight endpoints into pyarrow Tables
    //val tables = []
    endpoints.forEach { endpoint =>
      val flight_reader = client.getStream(endpoint.getTicket) //do_get(e.ticket)
      val table = flight_reader.read_all()
      tables.append(table)
    }

    // Convert Tables to a single Pandas DataFrame
    val table = pa.concat_tables(tables)
    val pdf = table.to_pandas()
    println(f"DataFrame from Flight streams:\n{pdf}")

    spark.stop()

  }

}
