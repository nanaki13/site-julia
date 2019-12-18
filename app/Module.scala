import bon.jo.helloworld.juliasite.pers.{PostgresRepo, RepositoryContext, SiteRepository}
import com.google.inject.{AbstractModule, Provides}
import controllers.services.Services.{ImageService, MenuService}
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration

import scala.concurrent.ExecutionContext

/**
  * This class is a Guice module that tells Guice how to bind several
  * different types. This Guice module is created when the Play
  * application starts.
  *
  * Play will automatically use any class called `Module` that is in
  * the root package. You can create modules in other locations by
  * adding `play.modules.enabled` settings to the `application.conf`
  * configuration file.
  */
class Module(implicit environment: play.api.Environment, configuration: Configuration) extends AbstractModule with ScalaModule{

  override def configure() = {
  }


  @Provides def imageService(implicit ctxp: ExecutionContext): ImageService = {
    new ImageService {
      override implicit val ctx: ExecutionContext = ctxp

      override def dbConntext: RepositoryContext with SiteRepository = PostgresRepo
    }
  }

  @Provides def menuService(implicit ctxp: ExecutionContext): MenuService = {
    new MenuService {
      override implicit val ctx: ExecutionContext = ctxp

      override def dbConntext: RepositoryContext with SiteRepository = PostgresRepo
    }
  }


}
