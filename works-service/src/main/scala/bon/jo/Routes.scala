package bon.jo

import java.security.interfaces.RSAPrivateKey
import java.security.{KeyPairGenerator, SecureRandom}
import java.util.{Date, UUID}

import akka.http.scaladsl.model.HttpHeader.ParsingResult
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.model.{HttpEntity, HttpHeader, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{Directives, RequestContext, Route}
import akka.stream.Materializer
import bon.jo.SiteModel.OkResponse
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.{JWT, JWTVerifier}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

object KeyContext {
  val pu = "I:\\work\\web\\julia\\web-server-bld\\site-julia\\public.pem"
  val pr = "I:\\work\\web\\julia\\web-server-bld\\site-julia\\pr.pem"
}


object Token {


  import java.security.interfaces.{RSAPrivateCrtKey, RSAPublicKey}


  def generateKeyPair(): Unit = {
    val kpg = KeyPairGenerator.getInstance("RSA")
    kpg.initialize(2048, new SecureRandom())
    val kp = kpg.generateKeyPair
    val rsaPriv = kp.getPrivate.asInstanceOf[RSAPrivateCrtKey]
    val rsaPub = kp.getPublic.asInstanceOf[RSAPublicKey]
  }

  //HMAC
  def algorithmHS: Algorithm = Algorithm.HMAC256("secret");

  val algo: Try[Algorithm] = algorithmRS

  def algorithmRS: Try[Algorithm] = ReadKey.getPublicPrivateKeys(KeyContext.pu, KeyContext.pr).map { e => {

    e
  }
  }.map(e => Algorithm.RSA512(e._1.asInstanceOf[RSAPublicKey], e._2.asInstanceOf[RSAPrivateKey]));


  def getToken(user: Login, issuer: String, validiteHour: Float, claims: Map[String, String] = Map.empty): Try[String] =


    algo map { a =>
      val t = JWT.create.withIssuer(issuer)
        .withClaim("role", "admin")
        .withClaim("name", user.name)

        .withJWTId(UUID.randomUUID().toString)
        .withKeyId(UUID.randomUUID().toString)
        .withExpiresAt(new Date(System.currentTimeMillis() + scala.math.round(validiteHour * 3600 * 1000)))
      claims.foreach(e => t.withClaim(e._1, e._2))
      t.sign(a)
    }


  def validToken(str: String): Try[DecodedJWT] = {
    (algo map {
      al =>
        val verifier: JWTVerifier = JWT.require(al).withIssuer("julia-lecorre").build()

        Try {
          val jwt: DecodedJWT = verifier.verify(str);
          jwt
        }
    }).getOrElse(Failure(new Exception("pas d'algo")))
  }


}

object ReadKey {


  import java.io.{File, FileNotFoundException, FileReader, IOException}
  import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
  import java.security.{KeyFactory, PrivateKey, PublicKey}

  import org.bouncycastle.util.io.pem.PemReader

  def getPublicPrivateKeys(pathPublic: String, pathPrivate: String): Try[(PublicKey, PrivateKey)] = {

    PemUtils.readPublicKeyFromFile(pathPublic, "RSA").flatMap(e => {
      PemUtils.readPrivateKeyFromFile(pathPrivate, "RSA") map { ee => {
        (e, ee)
      }
      }

    })

  }

  object PemUtils {
    @throws[IOException]
    private def parsePEMFile(pemFile: File): Array[Byte] = {
      if (!pemFile.isFile || !pemFile.exists) throw new FileNotFoundException(String.format("The file '%s' doesn't exist.", pemFile.getAbsolutePath))
      val reader = new PemReader(new FileReader(pemFile))
      val pemObject = reader.readPemObject
      val content = pemObject.getContent
      reader.close()
      content
    }

    private def getPublicKey(keyBytes: Array[Byte], algorithm: String): Try[PublicKey] = {

      Try {
        val kf = KeyFactory.getInstance(algorithm)
        val keySpec = new X509EncodedKeySpec(keyBytes)
        kf.generatePublic(keySpec)
      }

    }

    private def getPrivateKey(keyBytes: Array[Byte], algorithm: String): Try[PrivateKey] = {
      Try {
        val kf = KeyFactory.getInstance(algorithm)
        val keySpec = new PKCS8EncodedKeySpec(keyBytes)
        kf.generatePrivate(keySpec)
      }
    }

    @throws[IOException]
    def readPublicKeyFromFile(filepath: String, algorithm: String): Try[PublicKey] = {
      val bytes = PemUtils.parsePEMFile(new File(filepath))
      PemUtils.getPublicKey(bytes, algorithm)
    }

    @throws[IOException]
    def readPrivateKeyFromFile(filepath: String, algorithm: String): Try[PrivateKey] = {
      val bytes = PemUtils.parsePEMFile(new File(filepath))
      PemUtils.getPrivateKey(bytes, algorithm)
    }
  }

}

case class Login(login: String, name: String, mdp: String = "test")

class Routes(services: List[RootCreator[_]]) extends Directives with RouteHandle {


  def doWithContext(ctx: RequestContext): Route = {
    implicit val m: Materializer = ctx.materializer
    implicit val ec: ExecutionContext = ctx.executionContext

    services.map( e => {

       e.crudRoot
    })

    concat(services.map(_.crudRoot): _ *)
  }


  object CredentialManager {
    val login = Login("julia", "Julia Le Corre")
    val extractLogin: PartialFunction[(String, String), Login] = {
      case (login.login, login.mdp) => login
    }

  }

  def allRoutes(implicit ec: ExecutionContext): Route = {

    val static =
      concat(pathPrefix("julia") {
        getFromDirectory("html")
      }

        , path("auth" / "verify") {
          get {
            headerValueByName("Authorization") {
              e =>
                e.split(" ") match {
                  case Array("Bearer", token) => {
                    Token.validToken(token) match {
                      case Failure(exception) => complete(StatusCodes.Unauthorized,s"token invalid : ${exception.getMessage}")
                      case Success(value) => complete(StatusCodes.NoContent)
                    }
                  }
                }
            }
          }
        }
        , path("auth") {
          get {
            parameter(Symbol("login"), Symbol("pwd")) { (a, b) => {
              (a, b) match {
                case CredentialManager.extractLogin(user) => {
                  Token.getToken(user, "julia-lecorre", 1f) match {
                    case Failure(exception) => complete(exception.getMessage)
                    case Success(value) => complete(value)
                  }
                }
                case _ => complete(StatusCodes.Unauthorized)
              }
            }
            }

          }
        })
    concat(static, pathPrefix("api") {
      extractRequestContext { ctx => {
        doWithContext(ctx)
      }
      }
    })

  }
}
