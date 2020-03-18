package bon.jo.view

trait ValueConsumer[V] {
  def consume(v: V): Unit
}
