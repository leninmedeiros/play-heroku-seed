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
    // formatar as datas das questões
    // formatar as tags das questões

    var current_last_configuration:LastConfiguration = new LastConfiguration(Some(0)) // last new configuration
    var current_configuration = 0 // the current configuration
    val lc = LastConfigurationTable.findLastConfiguration // get last new configuration
    val id:Option[Any] = lc.id
    val messageConfigurationNumber = id match {
      case Some(x:Int) => x
      case _ => Int.MinValue
    }
    if(messageConfigurationNumber == LAST_CONFIGURATION_TYPE) {
      current_last_configuration = new LastConfiguration(Some(1))
    } else {
      current_last_configuration = new LastConfiguration(Some(messageConfigurationNumber+1))
    }
    current_user_ip = request.remoteAddress
    println("o ip do usuário atual é: " + current_user_ip)

    if (UserConfigurationTable.findByIp(current_user_ip).isEmpty) {
      println("não existe usuário salvo com este ip")
      UserConfigurationTable.insert(UserConfiguration(None,current_last_configuration.id.get,current_user_ip))
      current_configuration = current_last_configuration.id.get
      LastConfigurationTable.insert(current_last_configuration)
    } else {
      println("já existe um usuário salvo com este ip")
      UserConfigurationTable.findByIp(current_user_ip).map { user_configuration =>
        current_configuration = user_configuration.configuration_id
      }
    }

    ConfigurationTable.findById(current_configuration).map { configuration =>
      current_share_message = configuration.message match {
        case x:String => x
        case _ => current_share_message
      }
    }

    println("current_share_message é: "+current_share_message)

    Home
	}

  def listOfQuestions(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    //    FIX ME: this approach to deal with "++" must to be improved and generalized to other special characters
    Ok(html.listOfQuestions(
      QuestionTable.list(page = page, orderBy = orderBy, filter = ("%"+filter+"%")),
      orderBy, filter.replace("%2B%2B","++")
    ))
  }

  def showQuestion(id: Int, page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    QuestionTable.findById(id).map { question =>
      Ok(html.showQuestion(question, this.current_share_message, page, orderBy, filter))
    }.getOrElse(NotFound)
  }

  def shareQuestion(id: Int, page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    var user_id : Int = -100
    UserConfigurationTable.findByIp(current_user_ip).map { user_configuration =>
      user_id = user_configuration.id match {
        case Some(x:Int) => x
        case _ => user_id
      }
    }
    QuestionTable.findById(id).map { question =>
      SharedQuestionTable.insert(SharedQuestion(None,user_id,question.id.get))
      Ok(html.shareQuestion(question, page, orderBy, filter))
    }.getOrElse(NotFound)
  }
}
