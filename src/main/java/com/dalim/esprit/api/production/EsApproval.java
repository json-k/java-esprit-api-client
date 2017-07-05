package com.dalim.esprit.api.production;

import java.util.List;

import com.dalim.esprit.api.EsClass;
import com.google.gson.annotations.SerializedName;

public class EsApproval {
  private String approverType, step;
  private Integer ID;
  @SerializedName("class")
  private EsClass esclass;

  public String getApproverType() {
    return approverType;
  }

  public String getStep() {
    return step;
  }

  public Integer getID() {
    return ID;
  }

  public EsClass getEsclass() {
    return esclass;
  }

  public static class ListOf {
    private List<EsApproval> approvalList;

    public List<EsApproval> getApprovalList() {
      return approvalList;
    }

  }

}
