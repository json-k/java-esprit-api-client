package com.dalim.esprit.api.workflow;

import com.dalim.esprit.api.EsClass;
import com.google.gson.annotations.SerializedName;

public class EsWorkflowStatus {
  private String workflowName, status;
  private int ID, stepID;
  @SerializedName("class")
  private EsClass esclass;

  public String getWorkflowName() {
    return workflowName;
  }

  public String getStatus() {
    return status;
  }

  public int getID() {
    return ID;
  }

  public int getStepID() {
    return stepID;
  }

  public EsClass getEsclass() {
    return esclass;
  }

}
