package helloworld.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AOSConfig {

  private String keyARN = "arn:aws:kms:us-east-2:875743156223:alias/aos-key";
  private String accessKey = "AKIA4XZS4C77QQ45AKWV";
  private String secretKey = "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";
  private String region = "us-east-2";

  @Bean
  public DynamoDBMapper mapper() {
    return new DynamoDBMapper(client());
  }

  public AmazonDynamoDB client() {
    return AmazonDynamoDBClientBuilder.standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("dynamodb.us-east-2.amazonaws.com", region))
            .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
            .build();
  }

  @Bean
  public AmazonS3 s3Client() {
    return AmazonS3ClientBuilder.standard()
            .withRegion(region)
            .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
            .build();
  }

  @Bean
  public AWSKMS kmsClient() {
    return AWSKMSClientBuilder.standard()
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
}
