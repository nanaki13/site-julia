package bon.jo

import bon.jo.juliasite.pers.{RepositoryContext, SiteRepository}

class ServicesFactory(repo : RepositoryContext with  SiteRepository)(implicit v :  scala.concurrent.ExecutionContext){
   object menuService extends MenuServiceImpl(repo)
   object imageService extends ImageServiceImpl(repo)
   object oeuvreService extends OeuvreServiceImpl(repo)
   val servies:List[RootCreator[_]] = List(menuService,imageService,oeuvreService)
}
