package helloworld;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import helloworld.handler.App;
import org.junit.Test;

public class AppTest {
  @Test
  public void successfulResponse() {
    App app = new App();
    APIGatewayProxyResponseEvent response = app.buildResponse(200, "Hello Mayank");
    assertEquals(response.getStatusCode().intValue(), 200);
    assertEquals(response.getHeaders().get("Content-Type"), "application/json");
    assertEquals(response.getHeaders().get("Access-Control-Allow-Headers"), "*");
    assertEquals(response.getHeaders().get("Access-Control-Allow-Origin"), "*");
    assertEquals(response.getBody(), "Hello Mayank");
//    String content = result.getBody();
//    assertNotNull(content);
//    assertTrue(content.contains("\"message\""));
//    assertTrue(content.contains("\"hello world\""));
//    assertTrue(content.contains("\"location\""));
  }
}
