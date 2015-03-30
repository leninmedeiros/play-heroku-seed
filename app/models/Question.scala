package models

import java.sql.Date
import scala.slick.driver.PostgresDriver.simple._
import play.api.Play.current

case class Question(id: Option[Int] = None, title: String, body: String,
                    creationDate: Date, link: String, tags: String, creationDateString: String,
                    titleHtml: String)


class QuestionTable(tag: Tag) extends Table[Question](tag, "question") {
  // Auto Increment the id primary key column
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def title = column[String]("title")
  def body = column[String]("body")
  def creationDate = column[Date]("creationDate")
  def link = column[String]("link")
  def tags = column[String]("tags")
  def creationDateString = column[String]("creationDateString")
  def titleHtml = column[String]("titleHtml")
  // the * projection (e.g. select * ...) auto-transforms the tupled
  // column values to / from a Configuration
  def * = (id.?, title, body, creationDate, link, tags, creationDateString, titleHtml) <> (Question.tupled, Question.unapply)
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
  def findById(id: Int): Option[Question] = db.withSession{ implicit session =>
    Some(questions.filter(_.id === id).first)
  }
  def update(updateQuestion: Question) = db.withTransaction{ implicit session =>
    questions.filter(_.id === updateQuestion.id).update(updateQuestion)
  }
  def listQuestions: List[Question] = db.withSession { implicit session =>
    questions.sortBy(_.id.asc.nullsFirst).list
  }
  def getPageOfQuestionsWithTags(filter : String, pageSize: Int, offset: Int): List[Question] = db.withSession { implicit session =>
    questions.filter(_.tags.like(filter)).sortBy(_.creationDate.asc).drop(offset).take(pageSize).list
  }
  def getTotalSizeOfQuestionsWithTags(filter: String): Int = db.withTransaction { implicit session =>
    questions.filter(_.tags.like(filter)).list.size
  }
  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Page[(Question)] = {
    val offset = pageSize * page

    Page(this.getPageOfQuestionsWithTags(filter, pageSize, offset),
      page,offset,this.getTotalSizeOfQuestionsWithTags(filter))
  }
}