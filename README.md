# C3p0 Plugin for Play2 framework

supported play version is ```2.0.x``` and ```2.1.x```.

## What is C3p0?

See [C3p0 Site](http://www.mchange.com/projects/c3p0/).

# Release Note

<table>
<tr><th>Version</th><th>Release Date</th><th>Description</th></tr>
<tr><td>0.1</td><td>2013/07/02</td><td>First Release.</td></tr>
</table>

# Setup

## 1. Install Plugin.

Edit file `project/Build.scala`

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
* idleConnectionTestPeriod
* preferredTestQuery
* checkoutTimeout
* jndiName

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