package models

import scala.slick.driver.PostgresDriver.simple._
import play.api.Play.current

case class UserConfiguration(id: Option[Int] = None, configuration_id: Int, ip: String)

class UserConfigurationTable(tag : Tag) extends Table[UserConfiguration](tag, "user_configuration") {
  // Auto Increment the id primary key column
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def configuration_id = column[Int]("configuration_id", O.NotNull)
  def ip = column[String]("ip", O.NotNull)
  // the * projection (e.g. select * ...) auto-transforms the tupled
  // column values to / from a Configuration
  def * = (id.?, configuration_id, ip) <> (UserConfiguration.tupled, UserConfiguration.unapply)
}

object UserConfigurationTable {
  val db = play.api.db.slick.DB
  val users = TableQuery[UserConfigurationTable]
  def listUsers: List[UserConfiguration] = db.withSession { implicit session =>
    users.sortBy(_.id.asc.nullsFirst).list
  }
  def insert(newUser: UserConfiguration) = db.withTransaction{ implicit session =>
    println("Mensagem do insert de UserConfigurationTable - INIT")
    if (this.findByIp(newUser.ip).isEmpty) {
      println("Não existe nenhum usuário salvo com IP "+newUser.ip+". Salvando usuário...")
      users += newUser
    } else {
      println("Já existe usuário salvo com IP "+newUser.ip+". Atualizando usuário...")
      this.update(newUser)
    }
    println("Mensagem do insert de UserConfigurationTable - END")
  }
  def findByIp(ip: String): List[UserConfiguration]= db.withSession{ implicit session =>
    users.filter(_.ip === ip).list
  }
  def findLastUser: UserConfiguration = db.withSession{ implicit session =>
    users.sortBy(_.id.desc).first
  }
  def update(updateUser: UserConfiguration) = db.withTransaction{ implicit session =>
    users.filter(_.id === updateUser.id).update(updateUser)
  }
  def delete(id: Int) = db.withTransaction{ implicit session =>
    users.filter(_.id === id).delete
  }
}