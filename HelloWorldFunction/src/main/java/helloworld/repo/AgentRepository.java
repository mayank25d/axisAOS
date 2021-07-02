package helloworld.repo;

import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.CommitmentPolicy;
import com.amazonaws.encryptionsdk.CryptoInputStream;
import com.amazonaws.encryptionsdk.kms.KmsMasterKey;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import helloworld.model.AgentData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Component
public class AgentRepository {

  @Autowired
  private DynamoDBMapper mapper;

  @Autowired
  private DynamoDB dynamoDB;

  @Autowired
  private AmazonS3 s3Client;

  @Autowired
  private KmsMasterKeyProvider masterKeyProvider;

  public HashMap<String, String> getDashboardData() {
    Table table = dynamoDB.getTable("AOS_database");

    ScanSpec scanSpec = new ScanSpec().withProjectionExpression("userID, distriChannel");

    HashMap<String, String> dataMap = new HashMap<String, String>();
    int channelUndefined = 0;
    int channelF2F = 0;
    int channelTele = 0;
    try{
      ItemCollection<ScanOutcome> items = table.scan(scanSpec);
      Iterator<Item> iter = items.iterator();
      while (iter.hasNext()) {
        Item item = iter.next();
        if(item.asMap().get("distriChannel").equals("undefined")) {
          channelUndefined = channelUndefined+1;
        } if(item.asMap().get("distriChannel").equals("f2f")) {
          channelF2F = channelF2F+1;
        } if(item.asMap().get("distriChannel").equals("tele")) {
          channelTele = channelTele+1;
        }
      }
      dataMap.put("totalCount", String.valueOf(items.getAccumulatedScannedCount()));
      dataMap.put("totalDocCount", String.valueOf(items.getAccumulatedScannedCount()*5));
      dataMap.put("totalTele", String.valueOf(channelTele));
      dataMap.put("totalF2F", String.valueOf(channelF2F));
      dataMap.put("totalUndefiend", String.valueOf(channelUndefined));
    } catch (Exception e) {
      e.printStackTrace();
    }

    return dataMap;
  }

  public String sendNotification(String data) throws MessagingException, UnsupportedEncodingException {
    String from = "mayank25d@gmail.com";
    String fromName = "JD Bank";
    String recipient = "dshivam408@gmail.com";
    String subject = "Rejected list of agents";
    String CONFIGSET = "ConfigSet";
    String smtpUsername = ""; // Your SMTP username, can be created using SES SMTP settings
    String smtpPass = ""; // Your SMTP password, can be created using SES SMTP settings
    String host = "email-smtp.us-east-2.amazonaws.com";
    int port = 587;

    String bodyText = data;

    Properties props = System.getProperties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.port", port);
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.auth", "true");

    Session session = Session.getDefaultInstance(props);

    MimeMessage msg = new MimeMessage(session);
    msg.setFrom(new InternetAddress(from,fromName));
    msg.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
    msg.setSubject(subject);
    msg.setContent(bodyText,"text/html");

    Transport transport = session.getTransport();

    String response;
    try {
      transport.connect(host, smtpUsername, smtpPass);
      transport.sendMessage(msg, msg.getAllRecipients());
      response = "Email Sent";
    }
    catch (Exception ex) {
      ex.printStackTrace();
      response = "The Email was not sent";
    }
    finally
    {
      transport.close();
    }

    return response;
  }

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
