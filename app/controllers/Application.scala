package controllers

import scala.concurrent._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current
import scala.slick.driver.PostgresDriver.simple._
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import anorm._

import views._
import models._

object Application extends Controller {
  var current_user_ip = ""
  val LAST_CONFIGURATION_TYPE = 3
  var current_share_message = ""

  /**
   * This result directly redirect to the application home.
   */
  val Home = Redirect(routes.Application.listOfQuestions(0, 4, ""))

  def index = Action { request =>
    // se não tiver nenhum usuário salvo ainda
        // formatar as datas das questões
        // formatar as tags das questões
    if (UserConfigurationTable.listUsers.isEmpty) {
      println("vamos formatar as questões")
    }
    Home
	}

  def listOfQuestions(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    //    FIX ME: this approach to deal with "++" must to be improved and generalized to other special characters
    Ok(html.listOfQuestions(
      QuestionTable.list(page = page, orderBy = orderBy, filter = ("%"+filter+"%")),
      orderBy, filter.replace("%2B%2B","++")
    ))
  }
}
