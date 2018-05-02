package com.dalim.esprit.api.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.keeber.esprit.EspritAPI.ApiRequest;

import com.dalim.esprit.api.ESMetadataLayout;
import com.dalim.esprit.api.EsApprover;
import com.dalim.esprit.api.EsObject;
import com.dalim.esprit.api.EsReferenceable;
import com.dalim.esprit.api.EsStatus;
import com.dalim.esprit.api.folder.EsFolder;

public class EsJob extends EsObject {
  private String customerName, creationUser, jobWorkflow, documentWorkflow, projectTemplate, lastModificationUser, description, XMP, colorSpaceName, viewingCondition;
  private boolean reversedView, exportAsRSS, active;
  private float trimmedWidth, trimmedHeight;
  private int priority, projectTemplateID, customerID;
  private List<ESMetadataLayout> metadataLayout;
  private List<EsRole> roles;
  private List<EsDeadline> deadlines = new ArrayList<>();
  private List<EsApprover.ListOf> approvals = new ArrayList<>();


  public List<EsApprover.ListOf> getApprovals() {
    return approvals;
  }

  public List<EsDeadline> getDeadlines() {
    return deadlines;
  }

  public boolean isActive() {
    return active;
  }

  public String getCustomerName() {
    return customerName;
  }

  public String getCreationUser() {
    return creationUser;
  }

  public String getDocumentWorkflow() {
    return documentWorkflow;
  }

  public String getJobWorkflow() {
    return jobWorkflow;
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

  public String getColorSpaceName() {
    return colorSpaceName;
  }

  public String getViewingCondition() {
    return viewingCondition;
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

    @SuppressWarnings("unused")
    private EsRole() {

    }

    public EsRole(String role, String user) {
      this.role = role;
      this.user = user;
    }

    public static EsRole from(String role, String user) {
      return new EsRole(role, user);
    }

    public String getRole() {
      return role;
    }

    public String getUser() {
      return user;
    }

  }

  public static class EsDeadline {
    private String name;
    private Date date;

    @SuppressWarnings("unused")
    private EsDeadline() {

    }

    public EsDeadline(String name, Date date) {
      this.name = name;
      this.date = date;
    }

    public static EsDeadline from(String name, Date date) {
      return new EsDeadline(name, date);
    }

    public String getName() {
      return name;
    }

    public Date getDate() {
      return date;
    }

  }

  /**
   * Return Job edit params for given job ref.
   * 
   * 
   * @param EsRef
   * @return
   */
  public static EsJob.EditParams edit(EsReferenceable ref) {
    return new EditParams(ref);
  }

  public static class EditParams extends ApiRequest<EsStatus> {

    public EditParams(EsReferenceable job) {
      super("job.edit", EsStatus.class);
      put("path", job.getPath());
      put("ID", job.getID());
    }

    /**
     * Active if true. Completed if false.
     * 
     * @param active (see above).
     * @return
     */
    public EditParams withActive(boolean active) {
      put("active", active);
      return this;
    }

    /**
     * This will edit the JobName
     * 
     * @param name
     * @return
     */
    public EditParams withName(String name) {
      put("name", name);
      return this;
    }

    /**
     * Allows you to change the attachment of the project from one customer to another. Only one of
     * the customerID or customerName parameters must be present.
     * 
     * @param customerName
     * @return
     */
    public EditParams withCustomerName(String customerName) {
      put("customerName", customerName);
      put("customerID", null);
      return this;
    }

    /**
     * Allows you to change the attachment of the project from one customer to another. Only one of
     * the customerID or customerName parameters must be present.
     * 
     * @param customerID
     * @return
     */
    public EditParams withCustomerID(int customerID) {
      put("customerName", null);
      put("customerID", customerID);
      return this;
    }

    /**
     * Name of the project template to use for the job. Default: the default project template of the
     * customer
     * 
     * @param projectTemplateName
     * @return
     */
    public EditParams withProjectTemplateName(String projectTemplateName) {
      put("projectTemplateName", projectTemplateName);
      return this;
    }

    /**
     * Workflow for the job. Default: the one defined in the project template.
     * 
     * @param jobWorkflow
     * @return
     */
    public EditParams withJobWorkflow(String jobWorkflow) {
      put("jobWorkflow", jobWorkflow);
      return this;
    }

    /**
     * Workflow for the document. Default: the one defined in the project template.
     * 
     * @param documentWorkflow
     * @return
     */
    public EditParams withDocumentWorkflow(String documentWorkflow) {
      put("documentWorkflow", documentWorkflow);
      return this;
    }

    /**
     * Number of documents (for flatplan).
     * 
     * @param nbDocuments
     * @return
     */
    public EditParams withNbDocuments(int nbDocuments) {
      put("nbDocuments", nbDocuments);
      return this;
    }

    public EditParams withColorSpaceName(String csname) {
      put("colorSpaceName", csname);
      return this;
    }

    public EditParams withViewingCondition(String condition) {
      put("viewingCondition", condition);
      return this;
    }

    /**
     * Addition params one of:
     * 
     * <ul>
     * <li>colorSpaceName
     * <li>viewingCondition
     * <li>reversedView
     * <li>exportAsRSS
     * <li>priority
     * </ul>
     * 
     * @param param
     * @param value
     * @return
     */
    public EditParams withParam(String param, String value) {
      put(param, value);
      return this;
    }

    public EditParams addDeadline(EsJob.EsDeadline deadline) {
      arr("deadlines", deadline);
      return this;
    }

    public EditParams withDeadlines(List<EsJob.EsDeadline> deadlines) {
      put("deadlines", deadlines);
      return this;
    }

    public EditParams addApproval(EsApprover.ListOf approval) {
      arr("approvals", approval);
      return this;
    }

    public EditParams addMetadata(String namespace, String property, Object value) {
      arr("metadatas", new Object[] {namespace, property, value});
      return this;
    }

  }

  /**
   * Return Job creation params for given customer ref and job name.
   * 
   * @param customerRef
   * @param name of the job.
   * @return
   */
  public static EsJob.CreationParams create(EsReferenceable customerRef, String name) {
    return new CreationParams(customerRef, name);
  }

  /**
   * Return Job creation params for given customer ref and folder.
   * 
   * <p>
   * NOTE: folder name will be job name.
   * 
   * @param customerRef
   * @param name folder to convert to job (must exist).
   * @return
   */
  public static EsJob.CreationParams create(EsReferenceable customerRef, EsFolder folder) {
    return new CreationParams(customerRef, folder);
  }


  /**
   * Creation params for new Jobs.
   * 
   * @author Jason Keeber <jason@keeber.org>
   *
   */
  public static class CreationParams extends ApiRequest<EsObject> {
    private List<EsApprover.ListOf> approvals = new ArrayList<>();

    private CreationParams(EsReferenceable customerRef) {
      super("job.create", EsObject.class);
      put("customerID", customerRef.getID());
      put("customerName", customerRef.getPath().replaceFirst("^/", ""));
    }

    public CreationParams(EsReferenceable customerRef, String jobName) {
      this(customerRef);
      put("jobName", jobName);
    }

    public CreationParams(EsReferenceable customerRef, EsFolder folder) {
      this(customerRef);
      put("folder", folder);
    }

    /**
     * Adding a folder path will make this a filesystem based job.
     * 
     * @param path on the ES filesytem.
     * @return
     */
    public CreationParams setFolder(String path) {
      if (path != null) {
        put("folder", EsFolder.from(path));
        put("name", null);
      }
      return this;
    }

    /**
     * Name of the project template to use for the job. Default: the default project template of the
     * customer
     * 
     * @param projectTemplateName
     * @return
     */
    public CreationParams withProjectTemplateName(String projectTemplateName) {
      put("projectTemplateName", projectTemplateName);
      return this;
    }

    /**
     * Workflow for the job. Default: the one defined in the project template.
     * 
     * @param jobWorkflow
     * @return
     */
    public CreationParams withJobWorkflow(String jobWorkflow) {
      put("jobWorkflow", jobWorkflow);
      return this;
    }

    /**
     * Workflow for the document. Default: the one defined in the project template.
     * 
     * @param documentWorkflow
     * @return
     */
    public CreationParams withDocumentWorkflow(String documentWorkflow) {
      put("documentWorkflow", documentWorkflow);
      return this;
    }

    /**
     * Number of documents (for flatplan).
     * 
     * @param nbDocuments
     * @return
     */
    public CreationParams withNbDocuments(int nbDocuments) {
      put("nbDocuments", nbDocuments);
      return this;
    }

    public CreationParams withColorSpaceName(String csname) {
      put("colorSpaceName", csname);
      return this;
    }

    public CreationParams withViewingCondition(String condition) {
      put("viewingCondition", condition);
      return this;
    }

    /**
     * Addition params one of:
     * 
     * <ul>
     * <li>colorSpaceName
     * <li>viewingCondition
     * <li>reversedView
     * <li>exportAsRSS
     * <li>priority
     * </ul>
     * 
     * @param param
     * @param value
     * @return
     */
    public CreationParams withParam(String param, String value) {
      put(param, value);
      return this;
    }

    public CreationParams addDeadline(EsJob.EsDeadline deadline) {
      arr("deadlines", deadline);
      return this;
    }

    public CreationParams withDeadlines(List<EsJob.EsDeadline> deadlines) {
      put("deadlines", deadlines);
      return this;
    }

    public CreationParams addApproval(EsApprover.ListOf approval) {
      approvals.add(approval);
      return this;
    }

    public CreationParams withApprovals(List<EsApprover.ListOf> approvals) {
      this.approvals.clear();
      this.approvals.addAll(approvals);
      return this;
    }

    public CreationParams addMetadata(String namespace, String property, Object value) {
      arr("metadatas", new Object[] {namespace, property, value});
      return this;
    }

  }


}
