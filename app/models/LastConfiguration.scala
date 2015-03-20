package models

import scala.slick.driver.PostgresDriver.simple._
import play.api.Play.current

case class LastConfiguration(id: Option[Int] = None)

class LastConfigurationTable(tag: Tag) extends Table[LastConfiguration](tag, "last_configuration") {
  // Auto Increment the id primary key column
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  // the * projection (e.g. select * ...) auto-transforms the tupled
  // column values to / from a Configuration
  def * = (id.?) <> (LastConfiguration, LastConfiguration.unapply)
}

object LastConfigurationTable {
  val db = play.api.db.slick.DB
  val last_configurations = TableQuery[LastConfigurationTable]
  def findLastConfiguration: LastConfiguration = db.withSession { implicit session =>
    last_configurations.sortBy(_.id.asc.nullsFirst).first
  }
  def insert(lastConfiguration: LastConfiguration) = db.withTransaction{ implicit session =>
    last_configurations.filter(_.id === lastConfiguration.id).update(lastConfiguration)
  }
}