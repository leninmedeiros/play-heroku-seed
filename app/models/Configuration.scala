package models

import scala.slick.driver.PostgresDriver.simple._
import play.api.Play.current

case class Configuration(id: Option[Int] = None, message: String)

class ConfigurationTable(tag: Tag) extends Table[Configuration](tag, "configuration") {
  // Auto Increment the id primary key column
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def message = column[String]("message")
  // the * projection (e.g. select * ...) auto-transforms the tupled
  // column values to / from a Configuration
  def * = (id.?, message) <> (Configuration.tupled, Configuration.unapply)
}

object ConfigurationTable {
	val db = play.api.db.slick.DB
	val configurations = TableQuery[ConfigurationTable]
	def all: List[Configuration] = db.withSession { implicit session =>
		configurations.sortBy(_.id.asc.nullsFirst).list
	}
	def create(newConfiguration: Configuration) = db.withTransaction{ implicit session =>
		configurations += newConfiguration
	}
	def findById(id: Int): Option[Configuration] = db.withSession{ implicit session =>
		Some(configurations.filter(_.id === id).first)
	}
	def update(updateConfiguration: Configuration) = db.withTransaction{ implicit session =>
		configurations.filter(_.id === updateConfiguration.id).update(updateConfiguration)
	}
	def delete(id: Int) = db.withTransaction{ implicit session =>
		configurations.filter(_.id === id).delete
	}
}