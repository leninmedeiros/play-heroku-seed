package models

import scala.slick.driver.PostgresDriver.simple._
import play.api.Play.current


case class SharedQuestion(id: Option[Int] = None, user_configuration_id: Int,
                          question_id: Int)

class SharedQuestionTable(tag: Tag) extends Table[SharedQuestion](tag, "shared_question") {
  // Auto Increment the id primary key column
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def user_configuration_id = column[Int]("user_configuration_id", O.NotNull)
  def question_id = column[Int]("question_id", O.NotNull)
  // the * projection (e.g. select * ...) auto-transforms the tupled
  // column values to / from a Configuration
  def * = (id.?, user_configuration_id, question_id) <> (SharedQuestion.tupled, SharedQuestion.unapply)
}

object SharedQuestionTable {
  val db = play.api.db.slick.DB
  val sharedQuestions = TableQuery[SharedQuestionTable]
  def insert(newSharedQuestion: SharedQuestion) = db.withTransaction{ implicit session =>
    sharedQuestions += newSharedQuestion
  }
  def findById(id: Int): SharedQuestion = db.withSession{ implicit session =>
    sharedQuestions.filter(_.id === id).first
  }
}