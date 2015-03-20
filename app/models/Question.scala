package models

import java.util.{Date}
import scala.slick.driver.PostgresDriver.simple._
import play.api.Play.current

case class Question(id: Option[Long] = None, title: String, body: String,
                    creationDate: Option[Date], link: String, tags: String)

class QuestionTable(tag: Tag) extends Table[Question](tag, "question") {
  // Auto Increment the id primary key column
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def title = column[String]("title")
  def body = column[String]("body")
  def creationDate = column[Date]("creationDate")
  def link = column[String]("link")
  def tags = column[String]("tags")
  // the * projection (e.g. select * ...) auto-transforms the tupled
  // column values to / from a Configuration
  def * = (id.?, title, body, creationDate, link, tags) <> (Question.tupled, Question.unapply)
}
