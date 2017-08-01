package com.dalim.esprit.api.workflow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.dalim.esprit.api.EsClass;
import com.google.gson.annotations.SerializedName;

public class EsWorkflowStep {
  private String stepClass, stepName, status;
  private Date started, ended;
  private int stepID, level;

  public String getStepClass() {
    return stepClass;
  }

  public String getStepName() {
    return stepName;
  }

  public String getStatus() {
    return status;
  }

  public Date getStarted() {
    return started;
  }

  public Date getEnded() {
    return ended;
  }

  public int getStepID() {
    return stepID;
  }

  public int getLevel() {
    return level;
  }

  public class ListOf {
    private String workflowName;
    private int ID;
    @SerializedName("class")
    private EsClass esclass;
    private List<EsWorkflowStep> stepList = new ArrayList<>();

    public String getWorkflowName() {
      return workflowName;
    }

    public int getID() {
      return ID;
    }

    public EsClass getEsclass() {
      return esclass;
    }

    public List<EsWorkflowStep> getAll() {
      return stepList;
    }

    public List<EsWorkflowStep> getByStatus(String status) {
      return stepList.stream().filter(o -> o.getStatus().toLowerCase().equals(status.toLowerCase())).collect(Collectors.toList());
    }

    public List<EsWorkflowStep> getByStepName(String stepName) {
      return stepList.stream().filter(o -> o.getStepName().equals(stepName)).collect(Collectors.toList());
    }

    public Optional<EsWorkflowStep> getByStepID(int id) {
      return stepList.stream().filter(o -> o.getStepID() == id).findFirst();
    }

    public Optional<EsWorkflowStep> getByStepNameAndStatus(String stepName, String status) {
      return stepList.stream().filter(o -> o.getStepName().equals(stepName) && o.getStatus().toLowerCase().equals(status.toLowerCase())).findFirst();
    }

  }

}
