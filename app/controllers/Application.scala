package controllers

import java.sql.Date
import java.text.{SimpleDateFormat, DateFormat}

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

import scala.util.matching.Regex

import java.net._;
import java.io._;

object Application extends Controller {
  var current_user_ip = ""
  val FIRST_CONFIGURATION_TYPE = 1
  val LAST_CONFIGURATION_TYPE = 3
  var current_share_message = ""

  /**
   * This result directly redirect to the application home.
   */
  val Home = Redirect(routes.Application.listOfQuestions(0, 4, ""))

  def preConfig(request:Request[AnyContent]): Unit = {
    var ip : String = ""

    println("X-Forwarded-For: "+request.headers.get("X-Forwarded-For"))

    if (request.headers.get("X-Forwarded-For") != None) {
      ip = request.headers.get("X-Forwarded-For").get
    } else {
      ip = request.remoteAddress
    }

    var current_configuration : Int = 0

    current_user_ip = ip
    println("Entrou no preConfig.")
    println("O IP do usuário atual é: " + current_user_ip)

    if (UserConfigurationTable.findByIp(current_user_ip).isEmpty) {
      println("Não existe usuário salvo com este IP.")
      if (UserConfigurationTable.listUsers.isEmpty) {
        println("Ainda não existe nenhum usuário salvo no banco de dados.")

        QuestionTable.listQuestions.map { question =>
          QuestionTable.update(Question(question.id,question.title,
            question.body,question.creationDate,question.link,
            this.formatTags(question.tags),this.formatCreationDateString(question.creationDate), this.formatQuestionTitleHtml(question.body)))
        }

        current_configuration = FIRST_CONFIGURATION_TYPE
        println("Salvando usuário com configuração "+current_configuration)
        UserConfigurationTable.insert(UserConfiguration(None, current_configuration, current_user_ip))

      } else {
        println("Já existem usuários salvos no banco de dados.")
        val lastUser : UserConfiguration = UserConfigurationTable.findLastUser
        val current_user_config_id = lastUser.configuration_id match {
          case LAST_CONFIGURATION_TYPE => FIRST_CONFIGURATION_TYPE
          case x:Int => x+1
        }
        println("Salvando usuário com configuração "+current_user_config_id)
        UserConfigurationTable.insert(UserConfiguration(None, current_user_config_id, current_user_ip))

        current_configuration = current_user_config_id
      }

    } else {
      println("Já existe um usuário salvo com este IP")
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

    println("O valor de current_share_message é: "+current_share_message)
  }

  def index = Action { request =>
    println("Entrou no index.")
    Home
	}

  def listOfQuestions(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    preConfig(request)
    //    FIX ME: this approach to deal with "++" must to be improved and generalized to other special characters
    //    FIX ME: this approach to deal with "#" must to be improved and generalized to other special characters
    //    FIX ME: this approach to deal with filter "c" must to be improved
    var filterParsed : String = filter
    if (filter.equalsIgnoreCase("c")) {
      filterParsed = "<c>"
    }
    Ok(html.listOfQuestions(
      QuestionTable.list(page = page, orderBy = orderBy, filter = ("%"+filter+"%")),
      orderBy, filter.replace("%2B%2B","++").replace("%23","#").replace("<c>","c")
    ))
  }

  def showQuestion(id: Int, page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    preConfig(request)
    var user_id : Int = -100
    UserConfigurationTable.findByIp(current_user_ip).map { user_configuration =>
      user_id = user_configuration.id match {
        case Some(x:Int) => x
        case _ => user_id
      }
    }
    DisplayedQuestionTable.insert(DisplayedQuestion(None,user_id,id))
    QuestionTable.findById(id).map { question =>
      Ok(html.showQuestion(question, this.current_share_message, page, orderBy, filter))
    }.getOrElse(NotFound)
  }

  def shareQuestion(id: Int, page: Int, orderBy: Int, filter: String, typeOfShare: Int) = Action { implicit request =>
    preConfig(request)
    var user_id : Int = -100
    UserConfigurationTable.findByIp(current_user_ip).map { user_configuration =>
      user_id = user_configuration.id match {
        case Some(x:Int) => x
        case _ => user_id
      }
    }
    QuestionTable.findById(id).map { question =>
      SharedQuestionTable.insert(SharedQuestion(None,user_id,question.id.get,typeOfShare))
      Ok(html.showQuestion(question, this.current_share_message, page, orderBy, filter))
    }.getOrElse(NotFound)
  }

  //  FIX ME: improve this algorithm and put the respective solution in some js to be executed when the page loads
  //  FIX ME: the approach to deal with "++" must to be improved and generalized to other special characters
  def formatTags(tagString : String) : String = {
    var stringHTML = ""
    tagString.split(">").map(string =>
      stringHTML = stringHTML +
        "<a class='post-tag' href=\"/questions?f="+string.replace("<","").replace("++","%2B%2B").replace("#","%23")+"\">"+string.replace("<","")+"</a>"
    )
    stringHTML
  }

  //  FIX ME: improve this algorithm and put the respective solution in some js to be executed when the page loads
  def formatCreationDateString(creationDate: Date) : String = {
    val df: DateFormat = new SimpleDateFormat("dd/MM/yyyy");
    val text: String = df.format(creationDate);
    text
  }

  //  FIX ME: improve this algorithm and put the respective solution in some js to be executed when the page loads
  def formatQuestionTitleHtml(title : String) : String = {
    val CHAR_LIMIT : Int = 500
    val pattern = new Regex("<p>(.*?)</p>.*")
    val parsedTitleHtml : String =
      (pattern findAllIn title).mkString(" [...] ").replace("<p>","").replace("</p>","")
    if (parsedTitleHtml.length() > CHAR_LIMIT) {
      parsedTitleHtml.substring(0,CHAR_LIMIT)+"..."
    } else {
      parsedTitleHtml
    }
  }

  def clickToShare(questionId : Int) = Action { implicit request =>
//    response().setContentType("text/javascript");
    var user_id : Int = - 100
    UserConfigurationTable.findByIp(current_user_ip).map { user_configuration =>
      user_id = user_configuration.id match {
        case Some(x:Int) => x
        case _ => user_id
      }
    }
    val click : ClickToShareQuestion = new ClickToShareQuestion(None,user_id,questionId)
    ClickToShareQuestionTable.insert(click)
    Ok("Salvando click no botão de compartilhar (msg de Application.scala)...")
  }
}
