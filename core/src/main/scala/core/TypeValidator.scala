package core

import java.util.UUID
import org.joda.time.format.ISODateTimeFormat

case class TypeValidatorEnums(name: String, values: Seq[String])

case class TypeValidator(
  enums: Seq[TypeValidatorEnums] = Seq.empty
) {

  private[this] val dateTimeISOParser = ISODateTimeFormat.dateTimeParser()

  private val BooleanValues = Seq("true", "false")

  def assertValidDefault(t: Type, value: String) {
    validate(t, value) match {
      case None => ()
      case Some(msg) => sys.error(msg)
    }
  }

  def validate(
    t: Type,
    value: String,
    errorPrefix: Option[String] = None
  ): Option[String] = {
    t match {

      case Type.Enum(name) => {
        enums.find(_.name == name) match {
          case None => Some(s"Could not find enum named[$name]")
          case Some(enum) => {
            enum.values.find(_ == value) match {
              case None => {
                val msg = s"default[$value] is not a valid value for enum[$name]. Valid values are: " + enum.values.mkString(", ")
                errorPrefix match {
                  case None => Some(msg)
                  case Some(prefix) => Some(s"$prefix $msg")
                }
              }
              case Some(_) => None
            }
          }
        }
      }
      
      case Type.Model(name) => {
        Some(s"Default[$value] is not valid for model[$name]. apidoc does not support default values for models")
      }

      case Type.Primitive(Primitives.Boolean) => {
        if (BooleanValues.contains(value)) {
          None
        } else {
          Some(s"Invalid default[${value}] for boolean. Must be one of: ${BooleanValues.mkString(" ")}")
        }
      }

      case Type.Primitive(Primitives.Double) => {
        try {
          value.toDouble
          None
        } catch {
          case _: Throwable => Some(s"Value[$value] is not a valid double")
        }
      }

      case Type.Primitive(Primitives.Integer) => {
        try {
          value.toInt
          None
        } catch {
          case _: Throwable => Some(s"Value[$value] is not a valid integer")
        }
      }

      case Type.Primitive(Primitives.Long) => {
        try {
          value.toLong
          None
        } catch {
          case _: Throwable => Some(s"Value[$value] is not a valid long")
        }
      }

      case Type.Primitive(Primitives.Decimal) => {
        try {
          BigDecimal(value)
          None
        } catch {
          case _: Throwable => Some(s"Value[$value] is not a valid decimal")
        }
      }

      case Type.Primitive(Primitives.Unit) => {
        if (value == "") {
          None
        } else {
          Some(s"Value[$value] is not a valid unit type - must be the empty string")
        }
      }

      case Type.Primitive(Primitives.Uuid) => {
        try {
          UUID.fromString(value)
          None
        } catch {
          case _: Throwable => Some(s"Value[$value] is not a valid uuid")
        }
      }

      case Type.Primitive(Primitives.DateIso8601) => {
        try {
          dateTimeISOParser.parseDateTime(s"${value}T00:00:00Z")
          None
        } catch {
          case _: Throwable => Some(s"Value[$value] is not a valid date-time-iso8601")
        }
      }

      case Type.Primitive(Primitives.DateTimeIso8601) => {
        try {
          dateTimeISOParser.parseDateTime(value)
          None
        } catch {
          case _: Throwable => Some(s"Value[$value] is not a valid date-iso8601")
        }
      }

      case Type.Primitive(Primitives.String) => {
        None
      }
    }
  }

}
