package bon.jo.service

trait WithService {
  implicit val siteService: SiteService
}
