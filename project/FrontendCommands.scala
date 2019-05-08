/**
  * Frontend build commands.
  * Change these if you are using some other package manager. i.e: Yarn
  */
object FrontendCommands {
  val dependencyInstall: String = "heroku run npm install"
  val test: String = "heroku run npm run test:ci"
  val serve: String = "heroku run npm run start"
  val build: String = "heroku run npm run build:prod"
}