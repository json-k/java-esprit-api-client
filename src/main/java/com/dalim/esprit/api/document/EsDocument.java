package com.dalim.esprit.api.document;

import java.util.List;

import com.dalim.esprit.api.ESMetadataLayout;
import com.dalim.esprit.api.EsObject;

public class EsDocument extends EsObject {
  private String creationUser, lastModificationUser, documentWorkflow, UUID, currentRevision, XMP;
  private Integer jobID, lastRevision, priority;
  private List<ESMetadataLayout> metadataLayout;


  public String getCreationUser() {
    return creationUser;
  }

  public String getLastModificationUser() {
    return lastModificationUser;
  }

  public String getDocumentWorkflow() {
    return documentWorkflow;
  }

  public String getUUID() {
    return UUID;
  }

  public String getCurrentRevision() {
    return currentRevision;
  }

  public Integer getJobID() {
    return jobID;
  }

  public Integer getLastRevision() {
    return lastRevision;
  }

  public Integer getPriority() {
    return priority;
  }

  public String getXMP() {
    return XMP;
  }

  public List<ESMetadataLayout> getMetadataLayouts() {
    return metadataLayout;
  }


}
