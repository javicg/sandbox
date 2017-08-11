package streams


object Domain {
  final case class Event(id: Long, eventType: String)
}
