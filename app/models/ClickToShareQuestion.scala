package models

import scala.slick.driver.PostgresDriver.simple._
import play.api.Play.current


case class ClickToShareQuestion(id: Option[Int] = None, user_configuration_id: Int,
                          question_id: Int)

class ClickToShareQuestionTable(tag: Tag) extends Table[ClickToShareQuestion](tag, "click_to_share_question") {
  // Auto Increment the id primary key column
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def user_configuration_id = column[Int]("user_configuration_id", O.NotNull)
  def question_id = column[Int]("question_id", O.NotNull)
  // the * projection (e.g. select * ...) auto-transforms the tupled
  // column values to / from a Configuration
  def * = (id.?, user_configuration_id, question_id) <> (ClickToShareQuestion.tupled, ClickToShareQuestion.unapply)
}

object ClickToShareQuestionTable {
  val db = play.api.db.slick.DB
  val ClickToShareQuestions = TableQuery[ClickToShareQuestionTable]
  def insert(newClickToShareQuestion: ClickToShareQuestion) = db.withTransaction{ implicit session =>
    ClickToShareQuestions += newClickToShareQuestion
  }
  def findById(id: Int): ClickToShareQuestion = db.withSession{ implicit session =>
    ClickToShareQuestions.filter(_.id === id).first
  }
}