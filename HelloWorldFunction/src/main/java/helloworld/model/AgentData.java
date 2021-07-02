package helloworld.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.datamodeling.encryption.DoNotTouch;
import helloworld.config.CustomGeneratedKey;

import java.util.Map;

@DynamoDBTable(tableName = "AOS_database")
public class AgentData {

  private String userID;

  @DynamoDBRangeKey(attributeName = "joiningDate")
  @DynamoDBIndexRangeKey(attributeName = "joiningDate", globalSecondaryIndexName = "distriChannel-joiningDate-index")
  private String joiningDate;

  @DynamoDBAttribute(attributeName = "personalInfo")
  private Map<String, String> personalInfo;

  @DynamoDBAttribute(attributeName = "aadhar")
  private Map<String, String> aadhar;

  @DynamoDBAttribute(attributeName = "pan")
  private Map<String, String> pan;

  @DynamoDBAttribute(attributeName = "edu")
  private Map<String, String> edu;

  @DynamoDBAttribute(attributeName = "training")
  private Map<String, String> training;

  @DynamoDBAttribute(attributeName = "licenseNo")
  private String licenseNo;

  @DynamoDBAttribute(attributeName = "licenseExp")
  private String licenseExp;

  @DynamoDBAttribute(attributeName = "insurer")
  private String insurer;

  @DynamoDBAttribute(attributeName = "distriChannel")
  @DynamoDBIndexHashKey(attributeName = "distriChannel", globalSecondaryIndexName = "distriChannel-joiningDate-index")
  @DoNotTouch
  private String distriChannel = "undefined";

  @DynamoDBAttribute(attributeName = "login")
  private Map<String, Map<String, String>> login;

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @DynamoDBAttribute(attributeName = "status")
  private String status;

  @DynamoDBAttribute(attributeName = "message")
  private String message;

  public AgentData() { }

  public AgentData(String userID, String joiningDate, Map<String, String> personalInfo,
                   Map<String, String> aadhar, Map<String, String> pan, Map<String, String> edu,
                   Map<String, String> training, String licenseNo, String licenseExp,
                   String insurer, String distriChannel, Map<String, Map<String, String>> login,
                   String status, String message) {
    this.status = status;
    this.message = message;
    this.userID = userID;
    this.joiningDate = joiningDate;
    this.personalInfo = personalInfo;
    this.aadhar = aadhar;
    this.pan = pan;
    this.edu = edu;
    this.training = training;
    this.licenseNo = licenseNo;
    this.licenseExp = licenseExp;
    this.insurer = insurer;
    this.distriChannel = distriChannel;
    this.login = login;
  }

  @DynamoDBHashKey(attributeName = "userID")
  @CustomGeneratedKey(prefix = "JDIA")
  public String getUserID() {
    return userID;
  }

  public void setUserID(String userID) {
    this.userID = userID;
  }

  public String getJoiningDate() {
    return joiningDate;
  }

  public void setJoiningDate(String joiningDate) {
    this.joiningDate = joiningDate;
  }

  public Map<String, String> getPersonalInfo() {
    return personalInfo;
  }

  public void setPersonalInfo(Map<String, String> personalInfo) {
    this.personalInfo = personalInfo;
  }

  public Map<String, String> getAadhar() {
    return aadhar;
  }

  public void setAadhar(Map<String, String> aadhar) {
    this.aadhar = aadhar;
  }

  public Map<String, String> getPan() {
    return pan;
  }

  public void setPan(Map<String, String> pan) {
    this.pan = pan;
  }

  public Map<String, String> getEdu() {
    return edu;
  }

  public void setEdu(Map<String, String> edu) {
    this.edu = edu;
  }

  public Map<String, String> getTraining() {
    return training;
  }

  public void setTraining(Map<String, String> training) {
    this.training = training;
  }

  public String getLicenseNo() {
    return licenseNo;
  }

  public void setLicenseNo(String licenseNo) {
    this.licenseNo = licenseNo;
  }

  public String getLicenseExp() {
    return licenseExp;
  }

  public void setLicenseExp(String licenseExp) {
    this.licenseExp = licenseExp;
  }

  public String getInsurer() {
    return insurer;
  }

  public void setInsurer(String insurer) {
    this.insurer = insurer;
  }

  public String getDistriChannel() {
    return distriChannel;
  }

  public void setDistriChannel(String distriChannel) {
    this.distriChannel = distriChannel;
  }

  public Map<String, Map<String, String>> getLogin() {
    return login;
  }

  public void setLogin(Map<String, Map<String, String>> login) {
    this.login = login;
  }
}

