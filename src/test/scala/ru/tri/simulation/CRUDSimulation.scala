package ru.tri.simulation

import io.gatling.core.Predef._
import io.gatling.http.Predef._


class CRUDSimulation extends Simulation {

  private val baseUrl = "http://computer-database.gatling.io"

  private val homePageUrl = baseUrl + "/"

  private val newComputerPageAfterCreateUrl = homePageUrl + "computers"

  private val newComputerPageBeforeCreateUrl = homePageUrl + "computers/new"


  val httpProtocol = http
    .baseUrl(baseUrl)
    .inferHtmlResources()
    .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36")

  val headers_0 = Map(
    "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
    "Accept-Encoding" -> "gzip, deflate",
    "Accept-Language" -> "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7",
    "Upgrade-Insecure-Requests" -> "1",
    "dnt" -> "1")

  val headers_4 = Map(
    "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
    "Accept-Encoding" -> "gzip, deflate",
    "Accept-Language" -> "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7",
    "Origin" -> "http://computer-database.gatling.io",
    "Upgrade-Insecure-Requests" -> "1",
    "dnt" -> "1")


  val goToHomePage = exec(http("Going to home page")
    .get(homePageUrl).headers(headers_0))


  val goToNewComputerCreatingPage = exec(http("Going to new computer creating page")
    .get(newComputerPageBeforeCreateUrl).headers(headers_0)
    .resources(http("request_2")
      .get("/assets/stylesheets/bootstrap.min.css"),
      http("request_3")
        .get("/assets/stylesheets/main.css")))

  val oldNameFeeder = csv("data/name.csv").circular

  val newNameFeeder = csv("data/updateName.csv").circular

  val createNewComputerInDatabase =
    feed(oldNameFeeder)
      .exec(http("Creating new computer in database")
        .post(newComputerPageAfterCreateUrl).headers(headers_4)
        .formParam("name", "${name}")
        .formParam("introduced", "2020-04-03")
        .formParam("discontinued", "2020-04-04")
        .formParam("company", "39")
        .resources(http("request_5")
          .get("/assets/stylesheets/bootstrap.min.css"),
          http("request_6")
            .get("/assets/stylesheets/main.css"))
      )


  object Create {

    val createNewComputer = exec(goToHomePage).exec(goToNewComputerCreatingPage).exec(createNewComputerInDatabase)

  }

  val insertSearchCriterion =
    exec(http("Insert computer name")
      .get("/computers?f=${name}")
      .check(css("a:contains('${name}')", "href").saveAs("computerURL"))) // 5
      .pause(1)
      .exec(http("Select")
        .get("${computerURL}"))


  object Read {

    val searchComputerByName = exec(goToHomePage + "computers").exec(insertSearchCriterion)

  }

  val updateComputerInDatabaseAndVerifyThis = feed(oldNameFeeder)
    .exec(insertSearchCriterion)
    .feed(newNameFeeder)
    .exec(http("Update computer name and company")
      .post("${computerURL}")
      .headers(headers_4)
      .formParam("name", "${name}")
      .formParam("introduced", "2020-04-05")
      .formParam("discontinued", "2020-04-06")
      .formParam("company", "26")
      .resources(http("request_10")
        .get("/assets/stylesheets/bootstrap.min.css"),
        http("request_11")
          .get("/assets/stylesheets/main.css")))
    .exec(http("Insert updated computer name")
      .get("/computers?f=${name}")
      .check(css("a:contains('${name}')", "href").saveAs("newComputerURL")))
    .pause(1)
    .exec(http("Select")
      .get("${newComputerURL}"))

  object Update {

    val updateComputerInDataBase = exec(goToHomePage + "computers").exec(updateComputerInDatabaseAndVerifyThis)
  }

  object Delete {

    val deleteComputerFromDatabase = feed(newNameFeeder)
      .exec(insertSearchCriterion)
      .exec(http("Delete computer from database")
        .post("${computerURL}/delete")
        .headers(headers_4))
  }


//  val creatingNewComputer = scenario("Creating scenario").exec(Create.createNewComputer)
//
//  val searchingComputerByName = scenario("Searching computer").exec(Read.searchComputerByName)
//
//  val updatingComputerAndSearchingModifiedComputer = scenario("Updating computer").exec(Update.updateComputerInDataBase)
//
//  val deletingComputerFromDatabase = scenario("deleting computer from database").exec(Delete.deleteComputerFromDatabase)

  val CRUDScenario = scenario(" CRUD scenario").exec(Create.createNewComputer, Read.searchComputerByName, Update.updateComputerInDataBase, Delete.deleteComputerFromDatabase)


//  setUp(creatingNewComputer.inject(atOnceUsers(1)),
//    searchingComputerByName.inject(atOnceUsers(1)),
//    updatingComputerAndSearchingModifiedComputer.inject(atOnceUsers(1)),
//    deletingComputerFromDatabase.inject(atOnceUsers(1))
//  ).protocols(httpProtocol)

  setUp(CRUDScenario.inject(atOnceUsers(5))).protocols(httpProtocol)
}
