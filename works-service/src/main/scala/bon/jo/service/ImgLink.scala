package bon.jo.service

import bon.jo.ReadConf

object ImgLink {

  def apply(id: Int, contentType: String): String =
    s"/${ReadConf.conf.baseApiUrlImage}/${id}.${
      contentType.substring(contentType.lastIndexOf('/') + 1)
    }"
}
