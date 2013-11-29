/*
 * Copyright (C) 2013 FURYU CORPORATION
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package jp.furyu.play.c3p0

import play.api.db.DBPlugin
import play.api.db.DBApi
import com.mchange.v2.c3p0.{ DataSources, ComboPooledDataSource }
import play.api._
import play.api.libs._

import java.sql._
import javax.sql._

/**
 * the implementation of DBPlugin for c3p0.
 *
 * @param app
 */
class C3p0Plugin(app: Application) extends DBPlugin {

  lazy val dbConfig = app.configuration.getConfig("db").getOrElse(Configuration.empty)
  private def dbURL(conn: Connection): String = {
    val u = conn.getMetaData.getURL
    conn.close()
    u
  }

  // should be accessed in onStart first
  private lazy val dbApi: DBApi = new C3p0Api(dbConfig, app.classloader)

  /**
   * Is this plugin enabled.
   */
  override def enabled = true

  /**
   * Retrieves the underlying `DBApi` managing the data sources.
   */
  def api: DBApi = dbApi

  /**
   * Reads the configuration and connects to every data source.
   */
  override def onStart() {
    // Try to connect to each, this should be the first access to dbApi
    dbApi.datasources.map { ds =>
      try {
        ds._1.getConnection.close()
        app.mode match {
          case Mode.Test =>
          case mode => play.api.Logger.info("database [" + ds._2 + "] connected at " + dbURL(ds._1.getConnection))
        }
      } catch {
        case t: Throwable => {
          throw dbConfig.reportError(ds._2 + ".url", "Cannot connect to database [" + ds._2 + "]", Some(t.getCause))
        }
      }
    }
  }

  /**
   * Closes all data sources.
   */
  override def onStop() {
    dbApi.datasources.foreach {
      case (ds, _) => try {
        dbApi.shutdownPool(ds)
      } catch { case t: Throwable => }
    }
    val drivers = DriverManager.getDrivers()
    while (drivers.hasMoreElements) {
      val driver = drivers.nextElement
      DriverManager.deregisterDriver(driver)
    }
  }

}

private class C3p0Api(configuration: Configuration, classloader: ClassLoader) extends DBApi {

  private def error(db: String, message: String = "") = throw configuration.reportError(db, message)

  private val dbNames = configuration.subKeys

  private def register(driver: String, c: Configuration) {
    try {
      DriverManager.registerDriver(new play.utils.ProxyDriver(Class.forName(driver, true, classloader).newInstance.asInstanceOf[Driver]))
    } catch {
      case t: Throwable => throw c.reportError("driver", "Driver not found: [" + driver + "]", Some(t))
    }
  }

  private def createDataSource(dbName: String, url: String, driver: String, conf: Configuration): DataSource = {

    val datasource = new ComboPooledDataSource

    // Try to load the driver
    conf.getString("driver").map { driver =>
      try {
        DriverManager.registerDriver(new play.utils.ProxyDriver(Class.forName(driver, true, classloader).newInstance.asInstanceOf[Driver]))
      } catch {
        case t: Throwable => throw conf.reportError("driver", "Driver not found: [" + driver + "]", Some(t))
      }
    }

    val PostgresFullUrl = "^postgres://([a-zA-Z0-9_]+):([^@]+)@([^/]+)/([^\\s]+)$".r
    val MysqlFullUrl = "^mysql://([a-zA-Z0-9_]+):([^@]+)@([^/]+)/([^\\s]+)$".r
    val MysqlCustomProperties = ".*\\?(.*)".r
    val H2DefaultUrl = "^jdbc:h2:mem:.+".r

    conf.getString("url") match {
      case Some(PostgresFullUrl(username, password, host, dbname)) =>
        datasource.setJdbcUrl("jdbc:postgresql://%s/%s".format(host, dbname))
        datasource.setUser(username)
        datasource.setPassword(password)
      case Some(url @ MysqlFullUrl(username, password, host, dbname)) =>
        val defaultProperties = """?useUnicode=yes&characterEncoding=UTF-8&connectionCollation=utf8_general_ci"""
        val addDefaultPropertiesIfNeeded = MysqlCustomProperties.findFirstMatchIn(url).map(_ => "").getOrElse(defaultProperties)
        datasource.setJdbcUrl("jdbc:mysql://%s/%s".format(host, dbname + addDefaultPropertiesIfNeeded))
        datasource.setUser(username)
        datasource.setPassword(password)
      case Some(url @ H2DefaultUrl()) if !url.contains("DB_CLOSE_DELAY") =>
        if (Play.maybeApplication.exists(_.mode == Mode.Dev)) {
          datasource.setJdbcUrl(url + ";DB_CLOSE_DELAY=-1")
        } else {
          datasource.setJdbcUrl(url)
        }
      case Some(s: String) =>
        datasource.setJdbcUrl(s)
      case _ =>
        throw conf.globalError("Missing url configuration for database [%s]".format(conf))
    }

    conf.getString("user").foreach(datasource.setUser(_))
    conf.getString("pass").foreach(datasource.setPassword(_))
    conf.getString("password").foreach(datasource.setPassword(_))

    datasource.setDriverClass(driver)
    // Pool configuration
    conf.getInt("maxPoolSize").foreach(datasource.setMaxPoolSize(_))
    conf.getInt("minPoolSize").foreach(datasource.setMinPoolSize(_))
    conf.getInt("initialPoolSize").foreach(datasource.setInitialPoolSize(_))
    conf.getInt("acquireIncrement").foreach(datasource.setAcquireIncrement(_))
    conf.getInt("acquireRetryAttempts").foreach(datasource.setAcquireRetryAttempts(_))
    conf.getMilliseconds("acquireRetryDelay").foreach(v => datasource.setAcquireRetryDelay(v.toInt)) // ms
    conf.getInt("maxIdleTime").foreach(datasource.setMaxIdleTime(_)) // s
    conf.getInt("maxConnectionAge").foreach(datasource.setMaxConnectionAge(_)) // s
    conf.getInt("idleConnectionTestPeriod").foreach(datasource.setIdleConnectionTestPeriod(_)) // s
    conf.getString("preferredTestQuery").foreach(datasource.setPreferredTestQuery(_))
    conf.getMilliseconds("checkoutTimeout").foreach(v => datasource.setCheckoutTimeout(v.toInt)) // ms

    // Bind in JNDI
    conf.getString("jndiName").map { name =>
      JNDI.initialContext.rebind(name, datasource)
      play.api.Logger.info("datasource [" + conf.getString("url").get + "] bound to JNDI as " + name)
    }

    datasource

  }

  val datasources: List[(DataSource, String)] = dbNames.map { dbName =>
    val url = configuration.getString(dbName + ".url").getOrElse(error(dbName, "Missing configuration [db." + dbName + ".url]"))
    val driver = configuration.getString(dbName + ".driver").getOrElse(error(dbName, "Missing configuration [db." + dbName + ".driver]"))
    val extraConfig = configuration.getConfig(dbName).getOrElse(error(dbName, "Missing configuration [db." + dbName + "]"))
    register(driver, extraConfig)
    createDataSource(dbName, url, driver, extraConfig) -> dbName
  }.toList

  def shutdownPool(ds: DataSource) = {
    ds match {
      case ds: ComboPooledDataSource => DataSources.destroy(ds)
      case _ => error(" - could not recognize DataSource, therefore unable to shutdown this pool")
    }
  }

  /**
   * Retrieves a JDBC connection, with auto-commit set to `true`.
   *
   * Don't forget to release the connection at some point by calling close().
   *
   * @param name the data source name
   * @return a JDBC connection
   * @throws an error if the required data source is not registered
   */
  def getDataSource(name: String): DataSource = {
    datasources.filter(_._2 == name).headOption.map(e => e._1).getOrElse(error(" - could not find datasource for " + name))
  }

}