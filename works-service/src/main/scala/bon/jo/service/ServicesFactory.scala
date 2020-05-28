package bon.jo.service

import bon.jo.juliasite.pers.{RepositoryContext, SiteRepository}
import bon.jo.RootCreator

class ServicesFactory(repo : RepositoryContext with  SiteRepository)(implicit v :  scala.concurrent.ExecutionContext){
   object menuService extends MenuServiceImpl(repo)
   object imageService extends ImageServiceImpl(repo)
   object oeuvreService extends OeuvreServiceImpl(repo)
   object textService extends TextService(repo)
   val servies:List[RootCreator[_,_]] = List(menuService,imageService,oeuvreService,textService)
}
