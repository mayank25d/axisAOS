package helloworld.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.AttributeEncryptor;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.encryption.DynamoDBEncryptor;
import com.amazonaws.services.dynamodbv2.datamodeling.encryption.providers.DirectKmsMaterialProvider;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AOSConfig {

  private String cmkARN = "arn:aws:kms:us-east-2:875743156223:key/mrk-f6ef6447e00e401f9d44c55bea3e858b";
  private String keyARN = "arn:aws:kms:us-east-2:875743156223:alias/aos-key";
  private String accessKey = ""; // Place your access key here
  private String secretKey = ""; // Place your secret key here
  private String region = "us-east-2";

  @Bean
  public AmazonS3 s3Client() {
    return AmazonS3ClientBuilder.standard()
            .withRegion(region)
            .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
            .build();
  }

  @Bean
  public KmsMasterKeyProvider masterKeyProvider() {
    return KmsMasterKeyProvider.builder()
            .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
            .buildStrict(keyARN);
  }

  @Bean
  public DynamoDB dynamoDB() {
    return new DynamoDB(client());
  }

  @Bean
  public DynamoDBMapper mapper() {
    return new DynamoDBMapper(client(), mapperConfig(), new AttributeEncryptor(encryptor));
  }

  public DirectKmsMaterialProvider cmp = new DirectKmsMaterialProvider(kmsClient(), cmkARN);
  public DynamoDBEncryptor encryptor = DynamoDBEncryptor.getInstance(cmp);

  public DynamoDBMapperConfig mapperConfig() {
    return DynamoDBMapperConfig.builder()
            .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.PUT)
            .build();
  }

  public AmazonDynamoDB client() {
    return AmazonDynamoDBClientBuilder.standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("dynamodb.us-east-2.amazonaws.com", region))
            .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
            .build();
  }

  public AWSKMS kmsClient() {
    return AWSKMSClientBuilder.standard()
            .withRegion(region)
            .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
            .build();
  }

}

