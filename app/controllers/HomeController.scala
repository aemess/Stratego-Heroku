package controllers

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import de.htwg.se.stratego.Stratego
import de.htwg.se.stratego.controller.controllerComponent.{ControllerInterface, FieldChanged, PlayerSwitch}
import de.htwg.se.stratego.model.matchFieldComponent.matchFieldBaseImpl.{Field, Matrix}
import javax.inject._
import play.api.libs.json.{JsNumber, JsValue, Json}
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import akka.stream.Materializer

import scala.swing.Reactor



@Singleton
class HomeController @Inject()(cc: ControllerComponents) (implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {

  val gameController: ControllerInterface = Stratego.controller

  def matchFieldText: String = {
    gameController.matchFieldToString.replaceAll(s"\\033\\[.{1,5}m","")
  }

  def index(): Action[AnyContent] = Action {
    Ok(views.html.index())
  }

  def stratego: Action[AnyContent] = Action {
    gameToJson
    Ok(views.html.matchfield(gameController))
  }

  def menu: Action[AnyContent] = Action {
    Ok(views.html.menu(gameController))
  }

  def load(): Action[AnyContent] = Action {
    gameController.load
    Ok(views.html.matchfield(gameController))
  }

  def save(): Action[AnyContent] = Action {
    gameController.save
    Ok(views.html.matchfield(gameController))
  }

  def undo(): Action[AnyContent] = Action {
    gameController.undo
    Ok(views.html.matchfield(gameController))
  }

  def redo(): Action[AnyContent] = Action {
    gameController.redo
    Ok(views.html.matchfield(gameController))
  }

  def setPlayers(player1: String, player2: String): Action[AnyContent] = Action {
    gameController.createEmptyMatchfield(gameController.getSize)
    gameController.setPlayers(player1 + " " + player2)
    gameController.initMatchfield
    Redirect(controllers.routes.HomeController.stratego())
  }

  def setFigure(row: String, col: String, figure: String): Action[AnyContent] = Action {
    gameController.set(row.toInt,col.toInt, figure)
    Ok(views.html.matchfield(gameController))
  }

  def move: Action[JsValue] = Action(parse.json) {
    moveRequest: Request[JsValue] => {
      val dir = (moveRequest.body \ "dir").as[String].toCharArray
      val row = (moveRequest.body \ "row").as[Int]
      val col = (moveRequest.body \ "col").as[Int]

      if(dir(0) == 'r'){
        val rowD = row + 1
        if(rowD >= 0 && rowD < gameController.getField.matrixSize){
          if(gameController.getField.field(col, rowD).isSet){
            gameController.attack(col,row,col, rowD)
          }
        }
        gameController.move(dir(0),col,row)

      } else if(dir(0) == 'l'){
        val rowD = row - 1
        if(rowD >= 0 && rowD < gameController.getField.matrixSize){
          if(gameController.getField.field(col, rowD).isSet){
            gameController.attack(col,row,col, rowD)
          }
        }
        gameController.move(dir(0),col,row)

      }else if(dir(0) == 'd'){
        val colD = col + 1
        if(colD >= 0 && colD < gameController.getField.matrixSize){
          if(gameController.getField.field(colD, row).isSet){
            gameController.attack(col,row,colD, row)
          }
        }
        gameController.move(dir(0),col,row)

      } else if(dir(0) == 'u'){
        val colD = col - 1
        if(colD >= 0 && colD < gameController.getField.matrixSize){
          if(gameController.getField.field(colD, row).isSet){
            gameController.attack(col,row,colD, row)
          }
        }
        gameController.move(dir(0),col,row)
      }

      Ok(Json.obj(
        "matchField"-> Json.toJson(
          for{
            row <- 0 until gameController.getField.matrixSize
            col <- 0 until gameController.getField.matrixSize
          } yield {
            var obj = Json.obj(
              "row" -> row,
              "col" -> col
            )
            if(gameController.getField.field(row,col).isSet) {
              obj = obj.++(Json.obj(
                "figName" -> gameController.getField.field(row, col).character.get.figure.name,
                "figValue" -> gameController.getField.field(row, col).character.get.figure.value,
                "colour" -> gameController.getField.field(row, col).colour.get.value
              )
              )
            }
            obj
          }
        ),
        "currentPlayerIndex" -> JsNumber(gameController.currentPlayerIndex),
        "currentPlayer" -> (gameController.playerList(gameController.currentPlayerIndex)).toString()
      ))
    }
  }


  def gameToJson: Action[AnyContent] = Action {
    Ok(    Json.obj(
      "currentPlayerIndex" -> JsNumber(gameController.currentPlayerIndex),
      "currentPlayer" -> (gameController.playerList(gameController.currentPlayerIndex)).toString(),
      "players" -> (gameController.playerList.head + " "+ gameController.playerList(1)),
      "matchField"-> Json.toJson(
        for{
          row <- 0 until gameController.getField.matrixSize
          col <- 0 until gameController.getField.matrixSize
        } yield {
          var obj = Json.obj(
            "row" -> row,
            "col" -> col
          )
          if(gameController.getField.field(row,col).isSet) {
            obj = obj.++(Json.obj(
              "figName" -> gameController.getField.field(row, col).character.get.figure.name,
              "figValue" -> gameController.getField.field(row, col).character.get.figure.value,
              "colour" -> gameController.getField.field(row, col).colour.get.value
            )
            )
          }
          obj
        }
      )
    ))
  }

  def socket: WebSocket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef { out =>
      println("Connect received")
      StrategoWebSocketActorFactory.create(out)
    }
  }

  object StrategoWebSocketActorFactory{
    def create(out: ActorRef): Props = {
      Props(new StrategoWebSocketActor(out))
    }
  }

  class StrategoWebSocketActor(out: ActorRef) extends Actor with Reactor{
    listenTo(gameController)

    override def receive: Receive = {
      case msg: String =>
        out ! Json.obj(
          "currentPlayerIndex" -> JsNumber(gameController.currentPlayerIndex),
          "currentPlayer" -> (gameController.playerList(gameController.currentPlayerIndex)).toString(),
          "players" -> (gameController.playerList.head + " "+ gameController.playerList(1)),
          "matchField"-> Json.toJson(
            for{
              row <- 0 until gameController.getField.matrixSize
              col <- 0 until gameController.getField.matrixSize
            } yield {
              var obj = Json.obj(
                "row" -> row,
                "col" -> col
              )
              if(gameController.getField.field(row,col).isSet) {
                obj = obj.++(Json.obj(
                  "figName" -> gameController.getField.field(row, col).character.get.figure.name,
                  "figValue" -> gameController.getField.field(row, col).character.get.figure.value,
                  "colour" -> gameController.getField.field(row, col).colour.get.value
                )
                )
              }
              obj
            }
          )
        ).toString()
    }

    reactions += {
      case event: FieldChanged => sendJsonToClient()
      case event: PlayerSwitch => sendJsonToClient()
    }

    def sendJsonToClient(): Unit = {
      println("Received event from Controller")
      out ! Json.obj(
        "currentPlayerIndex" -> JsNumber(gameController.currentPlayerIndex),
        "currentPlayer" -> (gameController.playerList(gameController.currentPlayerIndex)).toString(),
        "players" -> (gameController.playerList.head + " "+ gameController.playerList(1)),
        "matchField"-> Json.toJson(
          for{
            row <- 0 until gameController.getField.matrixSize
            col <- 0 until gameController.getField.matrixSize
          } yield {
            var obj = Json.obj(
              "row" -> row,
              "col" -> col
            )
            if(gameController.getField.field(row,col).isSet) {
              obj = obj.++(Json.obj(
                "figName" -> gameController.getField.field(row, col).character.get.figure.name,
                "figValue" -> gameController.getField.field(row, col).character.get.figure.value,
                "colour" -> gameController.getField.field(row, col).colour.get.value
              )
              )
            }
            obj
          }
        )
      ).toString()
    }
  }
}
