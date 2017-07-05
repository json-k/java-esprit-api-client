package com.dalim.esprit.api.job;

import java.util.List;

import com.dalim.esprit.api.ESMetadataLayout;
import com.dalim.esprit.api.EsObject;

public class EsJob extends EsObject {
  private String customerName, creationUser, documentWorkflow, projectTemplate, lastModificationUser, description, XMP;
  private boolean reversedView, exportAsRSS;
  private float trimmedWidth, trimmedHeight;
  private int priority, projectTemplateID, customerID;
  private List<ESMetadataLayout> metadataLayout;
  private List<EsRole> roles;



  public String getCustomerName() {
    return customerName;
  }



  public String getCreationUser() {
    return creationUser;
  }



  public String getDocumentWorkflow() {
    return documentWorkflow;
  }



  public String getProjectTemplate() {
    return projectTemplate;
  }



  public String getLastModificationUser() {
    return lastModificationUser;
  }



  public String getDescription() {
    return description;
  }



  public String getXMP() {
    return XMP;
  }



  public boolean isReversedView() {
    return reversedView;
  }



  public boolean isExportAsRSS() {
    return exportAsRSS;
  }



  public float getTrimmedWidth() {
    return trimmedWidth;
  }



  public float getTrimmedHeight() {
    return trimmedHeight;
  }



  public int getPriority() {
    return priority;
  }



  public int getProjectTemplateID() {
    return projectTemplateID;
  }



  public int getCustomerID() {
    return customerID;
  }



  public List<ESMetadataLayout> getMetadataLayout() {
    return metadataLayout;
  }



  public List<EsRole> getRoles() {
    return roles;
  }



  public static class EsRole {
    private String role, user;

    public String getRole() {
      return role;
    }

    public String getUser() {
      return user;
    }

  }
}
