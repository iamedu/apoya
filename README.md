# apoya

[![Build Status](https://travis-ci.org/iamedu/apoya.png)](https://travis-ci.org/iamedu/apoya)

Apoya is a very opinionated server and webapp, it has every part of a webapp that I end up writing again and again.

Apoya comes from a series of projects where the following has always been true:

* Great customer support and service are needed in order to be successful
* Time is an extremely limited resource
* Requirements are not clear (except for this one: "We need it done yesterday")

Given those requirements I normally start writing the app, and later implement some tools that allow me to execute sql, and a few things more.
The idea this time is to have a stack and then build the app on top of it.

Here's a list of the implemented features

* Flexible authentication and authorization scheme
* SQL Scripting shell, with several sessions and transaction support
* JSR223 REPL
* JSR223 Scripting shell
* Hot-deploy
* Multi site
* Multi language
* "Su" (for support and debugging purposes)
* Great error reporting
* Forms for users to report and error when something breaks
* LESS support
* Scheduled Tasks

Some of the desired functionality:

* A webchat (with xmpp protocol)
* Mailing templates
* Possibilities to configure almost everything from admin's GUI
* Scheduled Reports in XLSX

## Technology

Apoya's stack uses:

* Clojure
* Clojurescript
* AngularJS
* RabbitMQ
* PostgreSQL (should be easy to move away from it)

Management tools:

| Responsability                   | Tool                  |
| -------------------------------- | --------------------- |
| Manage source code               | Git                   |
| Manage dependencies              | Leiningen             |
| Manage javascript dependencies   | Bower                 |
| Database Migrations              | Ragtime (leiningen)   |
| Replicate environment            | Vagrant               |

## Gettings started

In order to get started you need:

This software:

* VirtualBox
* Vagrant (http://www.vagrantup.com/)
* Ruby (possibly with rvm)
* Rubygems (rvm also takes care of this)
* Librarian Chef Gem (gem install librarian chef)
* Node.js and NPM (http://nodejs.org/download/)
* Bower (npm install -g bower)

And most important:

* An internet connection
* Patience

### Install apoya

1. Go to 'apoya/data/fs/sites/default'
2. $ bower install
3. Go to 'apoya/vagrant'
4. $ librarian-chef install
5. $ vagrant up

And then wait a long time :(, Maybe an hour or two depends a lot on your internet connection.

## Demo

Sites running in:

* http://apoya.ediaz.me
* http://apoya.herokuapp.com

I'll post a user that isn't admin sometime

## License

Copyright © 2013 Eduardo Díaz

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
