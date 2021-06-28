package helloworld.repo;

import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.CommitmentPolicy;
import com.amazonaws.encryptionsdk.CryptoInputStream;
import com.amazonaws.encryptionsdk.CryptoResult;
import com.amazonaws.encryptionsdk.kms.KmsMasterKey;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.encryption.DynamoDBEncryptor;
import com.amazonaws.services.dynamodbv2.datamodeling.encryption.providers.DirectKmsMaterialProvider;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import helloworld.model.AgentData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AgentRepository {

  @Autowired
  private DynamoDBMapper mapper;

  @Autowired
  private AmazonS3 s3Client;

  @Autowired
  private KmsMasterKeyProvider masterKeyProvider;

  public List<AgentData> getAgentsByUserID(String userID) {
    Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
    eav.put(":userID", new AttributeValue().withS(userID));

    DynamoDBQueryExpression<AgentData> query = new DynamoDBQueryExpression<AgentData>();
    query.withKeyConditionExpression("userID = :userID")
            .withExpressionAttributeValues(eav);

    return mapper.query(AgentData.class, query);
  }

  public AgentData updateAgentData(AgentData data) {
    String userID = data.getUserID();
    String joiningDate = data.getJoiningDate();
    AgentData updatedData = mapper.load(AgentData.class, userID, joiningDate);

    if(updatedData != null) {
      updatedData.setDistriChannel(data.getDistriChannel());
      updatedData.setJoiningDate(data.getJoiningDate());
      updatedData.setLogin(data.getLogin());
    }

    mapper.save(updatedData);

    return updatedData;
  }

  public List<AgentData> getAgentsByDisChannel(String distriChannel) {
      Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
      eav.put(":distriChannel", new AttributeValue().withS(distriChannel));

      DynamoDBQueryExpression<AgentData> query = new DynamoDBQueryExpression<AgentData>();
      query.withIndexName("distriChannel-joiningDate-index")
              .withKeyConditionExpression("distriChannel = :distriChannel")
              .withExpressionAttributeValues(eav);
      query.setConsistentRead(false);

      return mapper.query(AgentData.class, query);
  }

  public AgentData registerAgent(AgentData data) throws IOException {
      HashMap<String, String> urlsMap = new HashMap<String, String>();
      urlsMap.put("aadhar", data.getAadhar().get("link"));
      urlsMap.put("pan", data.getPan().get("link"));
      urlsMap.put("tenth", data.getEdu().get("tenth"));
      urlsMap.put("twelfth", data.getEdu().get("twelfth"));
      urlsMap.put("grad", data.getEdu().get("grad"));
      urlsMap.put("life", data.getTraining().get("life"));
      urlsMap.put("health", data.getTraining().get("health"));
      urlsMap.put("general", data.getTraining().get("general"));

      AwsCrypto crypto = AwsCrypto.builder()
              .withCommitmentPolicy(CommitmentPolicy.RequireEncryptRequireDecrypt)
              .build();

      TransferManager tm = TransferManagerBuilder.standard()
              .withS3Client(s3Client)
              .build();

      for(String key:urlsMap.keySet()) {
          Map<String, String> encryptionContext = Collections.singletonMap(key, key);

          if(urlsMap.get(key) != null) {
              URL newUrl = new URL(urlsMap.get(key));
              InputStream is = getImageInputStream(newUrl);

              ObjectMetadata metadata = new ObjectMetadata();
              metadata.setContentType("image/jpg");

              CryptoInputStream<KmsMasterKey> encryptingStream = crypto.createEncryptingStream(masterKeyProvider, is, encryptionContext);


              Upload upload = tm.upload("aos-agent-documents",
                      "LIC/" + data.getUserID() + "_" + key + ".jpg", encryptingStream, metadata);

              try {
                  upload.waitForCompletion();
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
          }
      }

      mapper.save(data);
      return data;
  }

  private InputStream getImageInputStream(URL url) throws IOException {

      String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";
      URLConnection con = null;
      try {
          con = url.openConnection();
          con.setRequestProperty("User-Agent", USER_AGENT);
      } catch (IOException e) {
          e.printStackTrace();
      }

      return con.getInputStream();
  }

}
