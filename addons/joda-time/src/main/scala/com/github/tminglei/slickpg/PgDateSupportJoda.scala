package com.github.tminglei.slickpg

import scala.slick.driver.PostgresDriver
import org.joda.time._
import org.joda.time.format.{ISODateTimeFormat, DateTimeFormat}
import scala.slick.lifted.Column
import org.postgresql.util.PGInterval

trait PgDateSupportJoda extends date.PgDateExtensions with utils.PgCommonJdbcTypes { driver: PostgresDriver =>
  import PgJodaSupportUtils._

  /// alias
  trait DateTimeImplicits extends JodaDateTimeImplicits

  trait JodaDateTimeImplicits {
    val jodaDateFormatter = ISODateTimeFormat.date()
    val jodaTimeFormatter = DateTimeFormat.forPattern("HH:mm:ss.SSSSSS")
    val jodaTimeFormatter_NoFraction = DateTimeFormat.forPattern("HH:mm:ss")
    val jodaDateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
    val jodaDateTimeFormatter_NoFraction = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
    val jodaTzDateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSSSSZ")
    val jodaTzDateTimeFormatter_NoFraction = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ssZ")

    implicit val jodaDateTypeMapper = new GenericJdbcType[LocalDate]("date",
      LocalDate.parse(_, jodaDateFormatter), _.toString(jodaDateFormatter), hasLiteralForm=false)
    implicit val jodaTimeTypeMapper = new GenericJdbcType[LocalTime]("time",
      fnFromString = (s) => LocalTime.parse(s, if(s.indexOf(".") > 0 ) jodaTimeFormatter else jodaTimeFormatter_NoFraction),
      fnToString = (v) => v.toString(jodaTimeFormatter),
      hasLiteralForm = false)
    implicit val jodaDateTimeTypeMapper = new GenericJdbcType[LocalDateTime]("timestamp",
      fnFromString = (s) => LocalDateTime.parse(s, if(s.indexOf(".") > 0 ) jodaDateTimeFormatter else jodaDateTimeFormatter_NoFraction),
      fnToString = (v) => v.toString(jodaDateTimeFormatter),
      hasLiteralForm = false)
    implicit val jodaPeriodTypeMapper = new GenericJdbcType[Period]("interval",
      pgIntervalStr2jodaPeriod, hasLiteralForm=false)
    implicit val jodaTimestampTZTypeMapper = new GenericJdbcType[DateTime]("timestamptz",
      fnFromString = (s) => DateTime.parse(s, if(s.indexOf(".") > 0 ) jodaTzDateTimeFormatter else jodaTzDateTimeFormatter_NoFraction),
      fnToString = (v) => v.toString(jodaTzDateTimeFormatter),
      hasLiteralForm = false)

    ///
    implicit def jodaDateColumnExtensionMethods(c: Column[LocalDate]) =
      new DateColumnExtensionMethods[LocalDate, LocalTime, LocalDateTime, Period, LocalDate](c)
    implicit def jodaDateOptColumnExtensionMethods(c: Column[Option[LocalDate]]) =
      new DateColumnExtensionMethods[LocalDate, LocalTime, LocalDateTime, Period, Option[LocalDate]](c)

    implicit def jodaTimeColumnExtensionMethods(c: Column[LocalTime]) =
      new TimeColumnExtensionMethods[LocalDate, LocalTime, LocalDateTime, Period, LocalTime](c)
    implicit def jodaTimeOptColumnExtensionMethods(c: Column[Option[LocalTime]]) =
      new TimeColumnExtensionMethods[LocalDate, LocalTime, LocalDateTime, Period, Option[LocalTime]](c)

    implicit def jodaTimestampColumnExtensionMethods(c: Column[LocalDateTime]) =
      new TimestampColumnExtensionMethods[LocalDate, LocalTime, LocalDateTime, Period, LocalDateTime](c)
    implicit def jodaTimestampOptColumnExtensionMethods(c: Column[Option[LocalDateTime]]) =
      new TimestampColumnExtensionMethods[LocalDate, LocalTime, LocalDateTime, Period, Option[LocalDateTime]](c)

    implicit def jodaIntervalColumnExtensionMethods(c: Column[Period]) =
      new IntervalColumnExtensionMethods[LocalDate, LocalTime, LocalDateTime, Period, Period](c)
    implicit def jodaIntervalOptColumnExtensionMethods(c: Column[Option[Period]]) =
      new IntervalColumnExtensionMethods[LocalDate, LocalTime, LocalDateTime, Period, Option[Period]](c)

    implicit def jodaTzTimestampColumnExtensionMethods(c: Column[DateTime]) =
      new TimestampColumnExtensionMethods[LocalDate, LocalTime, DateTime, Period, DateTime](c)
    implicit def jodaTzTimestampOptColumnExtensionMethods(c: Column[Option[DateTime]]) =
      new TimestampColumnExtensionMethods[LocalDate, LocalTime, DateTime, Period, Option[DateTime]](c)
  }
}

object PgJodaSupportUtils {
  /// pg interval string --> joda Duration
  def pgIntervalStr2jodaPeriod(intervalStr: String): Period = {
    val pgInterval = new PGInterval(intervalStr)
    val seconds = Math.floor(pgInterval.getSeconds) .asInstanceOf[Int]
    val millis  = ((pgInterval.getSeconds - seconds) * 1000) .asInstanceOf[Int]

    new Period(
      pgInterval.getYears,
      pgInterval.getMonths,
      0,  // weeks
      pgInterval.getDays,
      pgInterval.getHours,
      pgInterval.getMinutes,
      seconds, millis
    )
  }
}