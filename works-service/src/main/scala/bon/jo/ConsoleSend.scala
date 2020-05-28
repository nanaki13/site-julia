package bon.jo

import java.awt.Image
import java.io.{File, FileWriter}
import java.nio.file.{Files, Path, Paths}

import bon.jo.WebServer.repo
import bon.jo.service.ServicesFactory
import javax.swing.JFileChooser
import javax.swing.plaf.FileChooserUI

import scala.concurrent.Await
import scala.jdk.CollectionConverters._
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration.Duration
import scala.io.{Source, StdIn}
import scala.util.matching.Regex

object ConsoleSend extends App {
  val servies = new ServicesFactory(repo.PostgresRepo)

  val getEnd: Regex = """.*\.([^.]*$)""".r


  var current : Option[Path] = None
  def saveParent(path: Path): Unit = {
    if(current.isEmpty){
      current = Some(path)
      val f = new FileWriter("./lastFile")
      f.append(path.toString)
      f.flush()
      f.close()
    }

  }
  def read(): Path = {
    val s  = Source.fromFile("./lastFile")
    val ret = s.getLines().mkString("\n")
    s.close()
    Paths.get( ret)
  }
  def readOption: Option[Path] = if((new File("./lastFile").exists())){
    Some(read())
  }else None


  import servies.oeuvreService.dbContext.profile.api._

  var d = 1

  def send(): Unit = Await.result(for {mId <- servies.imageService.run(servies.imageService.images.map(_.id).max.map(_ + 1).result)} yield {
    mId.foreach(maxId => {
      val fc= getFC
      fc.showDialog(null, "Choisi pour envoi")
      fc.requestFocus()
      fc.getSelectedFiles.toList.map(_.toPath).foreach(e => {
        saveParent(e.getParent)
        servies.imageService.saveImage(Some(Files.readAllBytes(e)), maxId + d, ct(e), e.getFileName.toString, "/" + ReadConf.read().baseApiUrlImage).foreach(ee=>{
          println(s"save $e: ok")
        })
        d += 1
      })


    })

  }, Duration.Inf)

  import javax.imageio.ImageIO
  import java.awt.image.BufferedImage
  import java.io.IOException

  def redimensionne(widthTarget: Int): Unit = {

     val fc= getFC
    fc.showDialog(null, s"Choisi pour redimension\n à  $widthTarget")
    fc.requestFocus()
    fc.getSelectedFiles.toList.foreach(e => {

      saveParent(e.toPath.getParent)
      var image: BufferedImage = null

      try {
        image = ImageIO.read(e)
        val r = widthTarget / image.getWidth.toFloat
        val h = Math.round((image.getHeight * r))
        getEnd.findFirstMatchIn(e.toPath.getFileName.toString).foreach(m => {

          val nFilName = e.toPath.getFileName.toString.replace(m.subgroups.head, s"$widthTarget-$h.${m.subgroups.head}")
          val target = e.getParentFile.toPath.resolve(Paths.get(nFilName))
          //  ImageIO.write(image, "jpg", target.toFile)

          import java.awt.AlphaComposite
          import java.awt.Graphics2D
          import java.awt.RenderingHints
          import java.awt.image.BufferedImage


          val scale = image.getScaledInstance(widthTarget, h, Image.SCALE_DEFAULT)

          val resizedImage = new BufferedImage(widthTarget, h, if (image.getType != 0) image.getType else BufferedImage.TYPE_INT_ARGB)
          val g = resizedImage.createGraphics

          g.setComposite(AlphaComposite.Src)
          g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
          g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
          g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
          g.drawImage(scale, 0, 0, null)


          g.dispose()

          ImageIO.write(resizedImage, m.subgroups.head, target.toFile)
          println("... to " + target.toString)
          println("... to " + target.toFile.exists())
        })


      } catch {
        case e: IOException =>
          e.printStackTrace()
      }
    })

  }

  def getFC : JFileChooser = {
    val fc = new JFileChooser()
    fc.setMultiSelectionEnabled(true)
    readOption.foreach(e =>fc.setCurrentDirectory(e.toFile))
    fc
  }
  def toJpeg(): Unit = {
    val fc= getFC
    fc.showDialog(null, "Choisi pour convertir en jpeg")

    fc.getSelectedFiles.toList.foreach(e => {
      saveParent(e.toPath.getParent)

      var image: BufferedImage = null

      try {
        image = ImageIO.read(e)
        getEnd.findFirstMatchIn(e.toPath.getFileName.toString).foreach(m => {
          val nFilName = e.toPath.getFileName.toString.replace(m.subgroups.head, "jpg")
          val target = e.getParentFile.toPath.resolve(Paths.get(nFilName.replace("'","-").replace(" ","-")))
          ImageIO.write(image, "jpg", target.toFile)

          println("... to " + target.toString)
          println("... to " + target.toFile.exists())
        })


      } catch {
        case e: IOException =>
          e.printStackTrace()
      }
    })

  }

  def ct(p: Path) = {
    p.getFileName.toString match {
      case getEnd(ex) =>
        ex match {
          case IType(JPG) => "image/jpeg"
          case IType(PNG) => "image/png"
          case _ => throw new Exception("->" + ex)
        }
    }
  }

  object JPG extends IType

  object PNG extends IType

  sealed trait IType

  object IType {
    def unapply(string: String): Option[IType] = {
      string.toLowerCase match {
        case "jpg" | "jpeg" => Some(JPG)
        case "png" => Some(PNG)
      }
    }
  }

  object Convertit extends ProcessChoix {
    override def run(): Unit = toJpeg()

    val cmd: String = "conv"
  }

  case class Redimensionne(width: Int) extends ProcessChoix {
    override def run(): Unit = redimensionne(width)

    val cmd = "red "+width
  }

  object Redimensionne {
    val cmdM: String = "red"

    def unapply(arg: String): Option[Redimensionne] = {
      if (arg.startsWith(cmdM)) {
       Some( Redimensionne(arg.replace(cmdM,"").trim.toInt) )
      } else {
        None
      }
    }
  }

  object Send extends ProcessChoix {
    override def run(): Unit = send()

    val cmd: String = "send"
  }

  object Exit extends ProcessChoix {
    override def run(): Unit = System.exit(0)

    val cmd: String = "exit"
  }

  object Unkonw extends ProcessChoix {
    override def run(): Unit = {
    }

    val cmd: String = "???"
  }

  sealed case class Choix(userChoix: String)

  trait ProcessChoix {
    def run(): Unit

    def cmd: String
  }


  object ProcessChoix {
    def unapply(arg: String): Option[ProcessChoix] = {
      p.lift(arg)
    }
    def p : PartialFunction[String,ProcessChoix] = {
      case "conv" => Convertit
      case "send" => Send
      case "exit" => Exit
      case Redimensionne(w) => w
    }
  }

  val choice = List(Convertit, Send, Exit,Redimensionne(1200))


  @scala.annotation.tailrec
  def go(str: String): Unit = {
    val choix = str match {
      case ProcessChoix(e) => e
      case _ => Unkonw
    }
    if (choix != Unkonw) {
      choix.run()
      current = None
    }
    go(StdIn.readLine("Que faire : " + choice.map(_.cmd).mkString(",")+"\n"))
  }

  go(StdIn.readLine("Que faire : " + choice.map(_.cmd).mkString(",")+"\n"))


}
