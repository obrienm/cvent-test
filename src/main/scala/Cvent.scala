import com.cvent.api._2006_11.V200611
import com.cvent.api._2006_11.V200611Soap
import com.cvent.schemas.api._2006_11.LoginResult
import java.io.File
import com.typesafe.config.ConfigFactory
import Config._

object Cvent extends App {

  val account = conf.getString("account")
  val username = conf.getString("username")
  val password = conf.getString("password")
  
  val ws = new V200611()
  val soapClient: V200611Soap = ws.getV200611Soap
  val loginResult: LoginResult = soapClient.login(account, username, password)
  
  println(loginResult.isLoginSuccess)
}

object Config {
  lazy val conf = {
    val path = System.getProperty("user.home") + "/.config/test/cvent.json"
    ConfigFactory.parseFile(new File(path))
  }
}
