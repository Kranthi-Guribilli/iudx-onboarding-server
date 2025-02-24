package iudx.onboarding.server.apiserver.integrationtests.mlayerApis;

import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;
import iudx.onboarding.server.apiserver.integrationtests.RestAssuredConfiguration;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import static io.restassured.RestAssured.given;
import static iudx.onboarding.server.apiserver.integrationtests.tokens.TokenForITs.*;
import static org.hamcrest.Matchers.equalTo;


/* Rest Assured Integration tests for the CRUD operations of Mlayer Domain in Catalogue Middle layer specific APIs. The tests cover
 creating, updating, retrieving and deleting Mlayer Domain in Mlayer APIs*/


@ExtendWith(RestAssuredConfiguration.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MlayerDomain {
  private static String domainId;
  //Creating Mlayer Domain
  @Test
  @Order(1)
  @DisplayName("Create Mlayer Domain Success Test-201")
  public void createMlayerDomainTest(){
    //Request Body
    JsonObject requestBody = new JsonObject()
      .put("description", "Data Models that pertain to civic domain")
      .put("icon", "https://iudx-catalogue-assets.s3.ap-south-1.amazonaws.com/instances/icons/civic.png")
      .put("label", "Civic")
      .put("name", "civic");

    Response resp= given()
      .header("Content-Type", "application/json")
      .header("token", cosAdminToken)
      .body(requestBody.encodePrettily())
      .when()
      .post("/internal/ui/domain");
    JsonObject respJson = new JsonObject(resp.body().asString());
    JsonObject firstResult = respJson.getJsonArray("results").getJsonObject(0);
    domainId = firstResult.getString("id");
    resp.then()
      .statusCode(201)
      //.log().body()
      .body("type", equalTo("urn:dx:cat:Success"));
  }
  @Test
  @Order(2)
  @DisplayName("Create Mlayer Domain with Invalid Schema Test-400")
  public void createMlayerDomainWithInvalidSchemaTest(){
    JsonObject requestBody = new JsonObject()
      .put("descriiiption", "Data Models that pertain to civic domain")
      .put("icon", "https://iudx-catalogue-assets.s3.ap-south-1.amazonaws.com/instances/icons/civic.png")
      .put("label", "Civic")
      .put("name", "civic");

    given()
      .queryParam("id",domainId)
      .header("Content-Type", "application/json")
      .header("token", cosAdminToken)
      .body(requestBody.encodePrettily())
      .when()
      .post("/internal/ui/domain")
      .then()
      .statusCode(400)
      //.log().body()
      .body("type", equalTo("urn:dx:cat:InvalidSchema"));
  }
  @Test
  @Order(3)
  @DisplayName("Create Mlayer Domain With Invalid Token Test-401")
  public void createMlayerDomainWithInvalidTokenTest(){
    //String domainId="6aeed588-a28e-4c17-9694-edfa24e7d227";
    JsonObject requestBody = new JsonObject()
      .put("description", "Data Models that pertain to civic domain")
      .put("icon", "https://iudx-catalogue-assets.s3.ap-south-1.amazonaws.com/instances/icons/civic.png")
      .put("label", "Civic")
      .put("name", "civic");
    given()
      .queryParam("id",domainId)
      .header("Content-Type", "application/json")
      .header("token", "abc")
      .body(requestBody.encodePrettily())
      .when()
      .post("/internal/ui/domain")
      .then()
      .statusCode(401)
      //.log().body()
      .body("type", equalTo("urn:dx:cat:InvalidAuthorizationToken"));
  }

  //Updating Mlayer Domain

  @Test
  @Order(4)
  @DisplayName("Update Mlayer Domain Success Test-200")
  public void updateMlayerDomainTest(){
    JsonObject requestBody = new JsonObject()
      .put("icon", "https://iudx-catalogue-assets.s3.ap-south-1.amazonaws.com/instances/icons/civic.jpeg")
      .put("name", "civic")
      .put("description", "Data Models that pertain to civic domain")
      .put("label", "Civic");
    given()
      .queryParam("id",domainId)
      .header("Content-Type","application/json")
      .header("token",cosAdminToken)
      .body(requestBody.encodePrettily())
      .when()
      .put("/internal/ui/domain")
      .then()
      .statusCode(200)
      //.log().body()
      .body("type",equalTo("urn:dx:cat:Success"));
  }
  @Test
  @Order(5)
  @DisplayName("Update Mlayer Domain With Invalid Schema Test-400")
  public void updateMlayerDomainWithInvalidSchemaTest(){
    JsonObject requestBody = new JsonObject()
      .put("descriiiptionn", "Data Models that pertain to civic domain")
      .put("icon", "https://iudx-catalogue-assets.s3.ap-south-1.amazonaws.com/instances/icons/civic.png")
      .put("label", "Civic")
      .put("name", "civic");

    given()
      .queryParam("id",domainId)
      .header("Content-Type","application/json")
      .header("token",cosAdminToken)
      .body(requestBody.encodePrettily())
      .when()
      .put("/internal/ui/domain")
      .then()
      .statusCode(400)
      //.log().body()
      .body("type",equalTo("urn:dx:cat:InvalidSchema"));
  }
  @Test
  @Order(6)
  @DisplayName("Update Mlayer Domain With Invalid Token Test-401")
  public void updateMlayerDomainWithInvalidTokenTest(){
    JsonObject requestBody = new JsonObject()
      .put("icon", "https://iudx-catalogue-assets.s3.ap-south-1.amazonaws.com/instances/icons/civic.jpeg")
      .put("name", "civic")
      .put("description", "Data Models that pertain to civic domain")
      .put("label", "Civic");
    given()
      .queryParam("id",domainId)
      .header("Content-Type","application/json")
      .header("token","abc")
      .body(requestBody.encodePrettily())
      .when()
      .put("/internal/ui/domain")
      .then()
      .statusCode(401)
      //.log().body()
      .body("type",equalTo("urn:dx:cat:InvalidAuthorizationToken"));
  }

  @Test
  @Order(7)
  @DisplayName("Get Mlayer Domain by Id Success Test-200")
  public void getMlayerDomainByIdTest() {
    given()
      .param("id", domainId)
      .when()
      .get("/internal/ui/domain")
      .then()
      .statusCode(200)
      //.log().body()
      .body("type", equalTo("urn:dx:cat:Success"));
  }


  //Deleting Mlayer Domain

  @Test
  @Order(8)
  @DisplayName("Delete Mlayer Domain Success Test-200")
  public void deleteMlayerDomainTest(){
    given()
      .queryParam("id",domainId)
      .header("token",cosAdminToken)
      .when()
      .delete("/internal/ui/domain")
      .then()
      .statusCode(200)
      //.log().body()
      .body("type",equalTo("urn:dx:cat:Success"));
  }
  @Test
  @Order(9)
  @DisplayName("Delete Mlayer Domain With Invalid Token Test-401")
  public void deleteMlayerDomainWithInvalidTokenTest(){
    given()
      .queryParam("id",domainId)
      .header("token","abc")
      .when()
      .delete("/internal/ui/domain")
      .then()
      .statusCode(401)
      //.log().body()
      .body("type",equalTo("urn:dx:cat:InvalidAuthorizationToken"));
  }
  @AfterEach
  public void tearDown() {
    // Introduce a delay
    try {
      Thread.sleep(1000); // 1 second delay
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

}
