package com.dalim.esprit.api.workflow;

public class EsExportedWorkflow {
  private String name, workflow;

  public String getName() {
    return name;
  }

  /**
   * The actual content of the workflow.
   * 
   * @return Either XML or a Base64 encoded ZIP of the XML (I think).
   */
  public String getWorkflow() {
    return workflow;
  }



}
