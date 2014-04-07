import com.cvent.api._2006_11._
import com.cvent.schemas.api._2006_11._
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl
import java.io.File
import com.typesafe.config.ConfigFactory
import Config._
import java.util.{GregorianCalendar, Date}
import javax.xml.ws.Holder
import scala.collection.JavaConversions._

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
  
  events.foreach(log)

  // copy event
  val eventParams = new EventParameters()
  eventParams.setTitle(s"Some event ${new Date()}")
  eventParams.setEndDate(new XMLGregorianCalendarImpl(new GregorianCalendar(2014, 06, 01)))
  val parameters = new ArrayOfEventParameters()
  parameters.getEventParameters.add(eventParams)
  val copyEventResultArray = client.copyEvent(events(0).getId, parameters, sessionHeader)
  val results: List[CopyEventResult] = copyEventResultArray.getCopyEventResult.toList
  results.foreach(res => println(res.getErrors.getError.iterator().toList.foreach(_.getDescription)))
  
//  // register an invitee
//  val invitee = new Invitee()
//  invitee.setFirstName(s"Matthew ${new Date().getTime}")
//  invitee.setLastName("O'Brien")
//  val cvoa = new ArrayOfCvObject()
//  cvoa.getCvObject.add(invitee)
//  val simpleEventRegistrationResultArray: SimpleEventRegistrationResultArray = client.simpleEventRegistration(cvoa, RegistrationAction.REGISTER, events(0).getId, "", sessionHeader)
//  val simpleEventRegistrationResults: List[SimpleEventRegistrationResult] = simpleEventRegistrationResultArray.getSimpleEventRegistrationResult.iterator().toList
//  simpleEventRegistrationResults.foreach(_.isSuccess)
  
  private def log(event: Event) = {
    println(s"Id: ${event.getId}")
    println(s"EventTitle: ${event.getEventTitle}")
    println(s"EventCode: ${event.getEventCode}")
    println(s"EventDescription: ${event.getEventDescription}")
    println(s"EventEndDate: ${event.getEventEndDate}")
    println(s"EventLaunchDate: ${event.getEventLaunchDate}")
    println(s"EventStartDate: ${event.getEventStartDate}")
  }
}

object Config {
  lazy val conf = {
    val path = System.getProperty("user.home") + "/.config/test/cvent.json"
    ConfigFactory.parseFile(new File(path))
  }
}
