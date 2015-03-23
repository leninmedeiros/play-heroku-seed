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
  println("o valor de page é = "+page)
  println("o valor de offset é = "+offset)
  println("o valor de total é = "+total)
  println("o valor de items.size é = "+items.size)
  lazy val prev = Option(page - 1).filter(_ >= 0)
  println("o valor de prev é = "+prev)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
  println("o valor de next é = "+next)
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