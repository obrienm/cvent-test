import com.cvent.api._2006_11.{CventSessionHeader, V200611, V200611Soap}
import com.cvent.schemas.api._2006_11._
import java.io.File
import com.typesafe.config.ConfigFactory
import Config._
import java.util.Date
import javax.xml.ws.Holder
import scala.collection.JavaConversions._
import com.cvent.api._2006_11.ArrayOfCvObject

object Cvent extends App {

  val account = conf.getString("account")
  val username = conf.getString("username")
  val password = conf.getString("password")
  
  val client: V200611Soap = new V200611().getV200611Soap
  
  // login and get the session header
  val loginResult: LoginResult = client.login(account, username, password)
  val session = loginResult.getCventSessionHeader()
  val cventSessionHeader = new CventSessionHeader()
  cventSessionHeader.setCventSessionValue(session)
  val sessionHeader = new Holder(cventSessionHeader)


  // get all events
  val searchResults: SearchResult = client.search(CvObjectType.EVENT, new CvSearch(), sessionHeader)
  val eventIds = searchResults.getId().toList
  val events: List[Event] = eventIds.flatMap {eventId =>
    val idArray = new IdArray()
    idArray.getId().add(eventId)
    val retrieveResult: RetrieveResult = client.retrieve(CvObjectType.EVENT, idArray, sessionHeader)
    retrieveResult.getCvObject().iterator().toList.asInstanceOf[List[Event]]
  }
  
//  events.foreach(log)
  
  // register an invitee
  val invitee = new Invitee()
  invitee.setFirstName(s"Matthew ${new Date().getTime}")
  invitee.setLastName("O'Brien")
  val cvoa = new ArrayOfCvObject()
  cvoa.getCvObject.add(invitee)
  val simpleEventRegistrationResultArray: SimpleEventRegistrationResultArray = client.simpleEventRegistration(cvoa, RegistrationAction.REGISTER, events(0).getId, "", sessionHeader)
  val simpleEventRegistrationResults: List[SimpleEventRegistrationResult] = simpleEventRegistrationResultArray.getSimpleEventRegistrationResult.iterator().toList
  simpleEventRegistrationResults.foreach(_.isSuccess)
  
  private def log(event: Event) = {
    println(s"Id: ${event.getId}")
    println(s"Capacity: ${event.getCapacity}")
    println(s"Category: ${event.getCategory}")
    println(s"ClosedBy: ${event.getClosedBy}")
    println(s"EventCode: ${event.getEventCode}")
    println(s"EventDescription: ${event.getEventDescription}")
    println(s"EventEndDate: ${event.getEventEndDate}")
    println(s"EventLaunchDate: ${event.getEventLaunchDate}")
    println(s"EventStartDate: ${event.getEventStartDate}")
    println(s"EventStatus: ${event.getEventStatus}")
    println(s"EventTitle: ${event.getEventTitle}")
    println(s"LastModifiedDate: ${event.getLastModifiedDate}")
    println(s"Location: ${event.getLocation}")
    println(s"PlannerEmailAddress: ${event.getPlannerEmailAddress}")
    println(s"PostalCode: ${event.getPostalCode}")
    println(s"StreetAddress1: ${event.getStreetAddress1}")
    println(s"StreetAddress2: ${event.getStreetAddress2}")
    println(s"StreetAddress3: ${event.getStreetAddress3}")
  }
}

object Config {
  lazy val conf = {
    val path = System.getProperty("user.home") + "/.config/test/cvent.json"
    ConfigFactory.parseFile(new File(path))
  }
}
