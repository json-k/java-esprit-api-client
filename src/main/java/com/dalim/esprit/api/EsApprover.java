package com.dalim.esprit.api;

import java.util.ArrayList;
import java.util.List;

public class EsApprover {
  private String user, group, viewingCondition;
  private int level = 1, approverCount = 1, timeOut = 0;
  private EsApprover.Type type;

  public enum Type {
    Approver, Reviewer, GateKeeper;
  }

  private EsApprover() {

  }

  /**
   * Create and EsApprover from a group name.
   * 
   * @param group group name.
   * @return
   */
  public static EsApprover fromGroup(String group) {
    return new EsApprover().withGroup(group);
  }

  /**
   * Create and EsApprover from a user name.
   * 
   * @param user user name.
   * @return
   */
  public static EsApprover fromUser(String user) {
    return new EsApprover().withUser(user);
  }

  public String getUser() {
    return user;
  }

  public String getGroup() {
    return group;
  }

  public String getViewingCondition() {
    return viewingCondition;
  }

  public int getLevel() {
    return level;
  }

  public int getApproverCount() {
    return approverCount;
  }

  public int getTimeOut() {
    return timeOut;
  }



  public EsApprover.Type getType() {
    return type;
  }

  public EsApprover withType(EsApprover.Type type) {
    this.type = type;
    return this;
  }

  public EsApprover withUser(String user) {
    this.user = user;
    return this;
  }

  public EsApprover withGroup(String group) {
    this.group = group;
    return this;
  }

  public EsApprover withViewingCondition(String viewingCondition) {
    this.viewingCondition = viewingCondition;
    return this;
  }

  public EsApprover withLevel(int level) {
    this.level = level;
    return this;
  }

  public EsApprover withApproverCount(int approverCount) {
    this.approverCount = approverCount;
    return this;
  }

  public EsApprover withTimeOut(int timeOut) {
    this.timeOut = timeOut;
    return this;
  }


  public static class ListOf {
    private List<EsApprover> approvers = new ArrayList<>();
    private String stepName;

    @SuppressWarnings("unused")
    private ListOf() {

    }

    public ListOf(String stepName) {
      this.stepName = stepName;
    }

    public List<EsApprover> getAll() {
      return approvers;
    }

    public String getStepName() {
      return stepName;
    }

    public EsApprover.ListOf addApprover(EsApprover approver) {
      approvers.add(approver);
      return this;
    }

  }


}
