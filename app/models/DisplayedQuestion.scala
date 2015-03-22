package models

import scala.slick.driver.PostgresDriver.simple._
import play.api.Play.current


case class DisplayedQuestion(id: Option[Int] = None, user_configuration_id: Int,
                          question_id: Int)

class DisplayedQuestionTable(tag: Tag) extends Table[DisplayedQuestion](tag, "displayed_question") {
  // Auto Increment the id primary key column
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def user_configuration_id = column[Int]("user_configuration_id", O.NotNull)
  def question_id = column[Int]("question_id", O.NotNull)
  // the * projection (e.g. select * ...) auto-transforms the tupled
  // column values to / from a Configuration
  def * = (id.?, user_configuration_id, question_id) <> (DisplayedQuestion.tupled, DisplayedQuestion.unapply)
}

object DisplayedQuestionTable {
  val db = play.api.db.slick.DB
  val displayedQuestions = TableQuery[DisplayedQuestionTable]
  def insert(newDisplayedQuestion: DisplayedQuestion) = db.withTransaction{ implicit session =>
    displayedQuestions += newDisplayedQuestion
  }
  def findById(id: Int): DisplayedQuestion = db.withSession{ implicit session =>
    displayedQuestions.filter(_.id === id).first
  }
}