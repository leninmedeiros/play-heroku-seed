package models

import java.sql.Date
import scala.slick.driver.PostgresDriver.simple._
import play.api.Play.current

case class Question(id: Option[Int] = None, title: String, body: String,
                    creationDate: Date, link: String, tags: String)

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

/**
 * Helper for pagination.
 */
case class Page[A](items: Seq[A], page: Int, offset: Int, total: Int) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

object QuestionTable {
  val db = play.api.db.slick.DB
  val questions = TableQuery[QuestionTable]
  def findById(id: Int): Question = db.withSession{ implicit session =>
    questions.filter(_.id === id).first
  }
  def update(updateQuestion: Question) = db.withTransaction{ implicit session =>
    questions.filter(_.id === updateQuestion.id).update(updateQuestion)
  }
  def listQuestions: List[Question] = db.withSession { implicit session =>
    questions.sortBy(_.id.asc.nullsFirst).list
  }
  def getQuestionsWithTags(filter : String): List[Question] = db.withSession { implicit session =>
    questions.filter(_.tags.like(filter,'%')).list
  }
  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Page[(Question)] = {
    val offset = pageSize * page
    val questionsReturned = this.getQuestionsWithTags(filter)

    Page(questionsReturned,page,offset,questionsReturned.size)
  }
}