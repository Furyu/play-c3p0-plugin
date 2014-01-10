# C3p0 Plugin for Play2 framework

supported play version is ```2.0.x``` and ```2.1.x``` and ```2.2.x```.

## What is C3p0?

See [C3p0 Site](http://www.mchange.com/projects/c3p0/).

# Release Note

| Version | Release Date | Description |
|:----------|:----------|:------------|
| 0.1 | 2013/07/02 | First Release. |
| 0.2 | 2013/12/19 | Fix maxIdleTime configuration. (#1) |
| 0.2.1 | 2014/01/10 | Supported play 2.2.x |

# Setup

## 1. Install Plugin.

Edit file `project/Build.scala` or `build.sbt`

```
libraryDependencies ++= Seq(
  "jp.furyu" %% "play-c3p0-plugin" % "LATEST_VERSION"
)
```

Edit file `conf/play.plugins`

```
5000:jp.furyu.play.c3p0.C3p0Plugin
```

## 2. Disable Existing DBPlugin.

Edit file `conf/application.conf`

```
dbplugin=disabled
```

## 3. Setting DB Configuration.

Edit file `conf/application.conf`

See (http://www.playframework.com/documentation/2.1.1/ScalaDatabase)

Supported Properties.

* user
* password
* url
* driver
* maxPoolSize
* minPoolSize
* initialPoolSize
* acquireIncrement
* acquireRetryAttempts
* acquireRetryDelay
* maxIdleTime
* maxConnectionAge
* idleConnectionTestPeriod
* preferredTestQuery
* checkoutTimeout
* jndiName

### Examples:

```
db.default.user=user
db.default.password=pass
db.default.url=xxx.yyy.zzz
db.default.driver=com.mysql.jdbc.Driver
db.default.maxPoolSize=200
db.default.minPoolSize=20
db.default.initialPoolSize=40
db.default.acquireIncrement=5
db.default.maxIdleTime=10m
db.default.maxConnectionAge=1h
db.default.idleConnectionTestPeriod=10m
db.default.preferredTestQuery="select 1;"
db.default.checkoutTimeout=3s
```

# License

Copyright (C) 2013 FURYU CORPORATION

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA