package helloworld.handler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.encryptionsdk.*;
import com.amazonaws.encryptionsdk.kms.KmsMasterKey;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import helloworld.AOSApplication;
import helloworld.model.AgentData;
import helloworld.repo.AgentRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import javax.mail.MessagingException;

public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static ApplicationContext applicationContext = SpringApplication.run(AOSApplication.class);
    private AgentRepository repo;

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        this.repo = applicationContext.getBean(AgentRepository.class);
        ObjectMapper objectMapper = new ObjectMapper();

        String method = input.getHttpMethod();
        String resource = input.getResource();

        Map<String, String> queryParam = input.getQueryStringParameters();

        if(resource.equals("/register-agents")) {
            if(method.equals("POST")) {
                String body = input.getBody();
                try {
                    AgentData data = objectMapper.readValue(body, AgentData.class);
                    if(data != null) {
                        String result = makeJsonStringFromObject(objectMapper, repo.registerAgent(data));
                        return buildResponse(200, result);
                    }
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    return buildResponse(500, "AOS: Internal Server Error");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } if(resource.equals("/get-agents/by-userid")) {
            if(method.equals("GET") && queryParam != null && queryParam.containsKey("userID")) {
                String userID = queryParam.get("userID");
                String result = makeJsonStringFromObject(objectMapper, repo.getAgentsByUserID(userID));
                return buildResponse(200, result);
            }
        } if(resource.equals("/get-agents/by-dis-channel")) {
            if(method.equals("GET") && queryParam != null && queryParam.containsKey("channel")) {
                String channel = queryParam.get("channel");
                String result = makeJsonStringFromObject(objectMapper, repo.getAgentsByDisChannel(channel));
                return buildResponse(200, result);
            }
        } if(resource.equals("/update-agents")) {
            if(method.equals("PUT")) {
              String body = input.getBody();
              try {
                AgentData data = objectMapper.readValue(body, AgentData.class);
                if (data != null) {
                  String result = makeJsonStringFromObject(objectMapper, repo.updateAgentData(data));
                  return buildResponse(200, result);
                }
              } catch (JsonProcessingException e) {
                e.printStackTrace();
                return buildResponse(500, "AOS: Internal Server Error");
              }
            }
        } if(resource.equals("/send-notification")) {
            if(method.equals("POST")) {
                String body = input.getBody();
                try {
                    String result = makeJsonStringFromObject(objectMapper, repo.sendNotification(body));
                    return buildResponse(200, result);
                } catch (MessagingException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return buildResponse(500, "AOS: Internal Server Error");
                }
            }
        } if(resource.equals("/dashboard-data")) {
            if(method.equals("GET")) {
                String result = makeJsonStringFromObject(objectMapper, repo.getDashboardData());
                return buildResponse(200, result);
            }
        }
        return buildResponse(500, "");
    }

    public APIGatewayProxyResponseEvent buildResponse(int status, String obj) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(status);
        Map<String, String> map = new HashMap<String, String>();
        map.put("Content-Type", "application/json");
        map.put("Access-Control-Allow-Headers", "*");
        map.put("Access-Control-Allow-Origin", "*");
        map.put("Access-Control-Allow-Methods", "*");
        map.put("Access-Control-Allow-Credentials", "true");
        response.setHeaders(map);
        response.setBody(obj);

        return response;
    }

    public String makeJsonStringFromObject(ObjectMapper objectMapper, Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch(JsonProcessingException e1) {
            e1.printStackTrace();
            return "";
        }
    }

}
