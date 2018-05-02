package com.dalim.esprit.api.document;

import java.util.List;

import org.keeber.esprit.EspritAPI.ApiRequest;

import com.dalim.esprit.api.ESMetadataLayout;
import com.dalim.esprit.api.EsObject;
import com.dalim.esprit.api.EsReferenceable;
import com.dalim.esprit.api.EsStatus;

public class EsDocument extends EsObject {
  private String creationUser, lastModificationUser, documentWorkflow, UUID, currentRevision, XMP, colorSpaceName, viewingCondition;
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



  public String getColorSpaceName() {
    return colorSpaceName;
  }

  public String getViewingCondition() {
    return viewingCondition;
  }

  public List<ESMetadataLayout> getMetadataLayouts() {
    return metadataLayout;
  }

  /**
   * Returns creation params for new documents. The path or ID - of the JOB - is required, as well
   * as the document name.
   * 
   * <p>
   * The document name will be the PageOrderName if there is a document with that name already it
   * will stack.
   * 
   * @param jobRef One of ID or path is mandatory.
   * @param name of the document.
   * @return
   */
  public static EsDocument.CreationParams create(EsReferenceable jobRef, String name) {
    return new CreationParams(jobRef, name);
  }

  /**
   * Creation parameters for new documents. The name and a reference to a job to create the document
   * are required..
   * 
   * @author Jason Keeber <jason@keeber.org>
   *
   */
  public static class CreationParams extends ApiRequest<EsObject> {

    public CreationParams(EsReferenceable jobRef, String name) {
      super("document.create", EsObject.class);
      put("name", name);
      put("jobID", jobRef.getID());
      put("jobPath", jobRef.getPath());
    }

    public CreationParams withDescription(String description) {
      put("description", description);
      return this;
    }

    /**
     * URL of file to register for that document
     * 
     * @param URL of file to register.
     * @return
     */
    public CreationParams withURL(String URL) {
      put("URL", URL);
      return this;
    }

    /**
     * If true the file is uploaded to ES else the file stays where it is provided the URL starts as
     * a SHARED Volume defined in ES.
     * 
     * @param moveFile upload the file?
     * @return
     */
    public CreationParams withMoveFile(boolean moveFile) {
      put("moveFile", moveFile);
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
     * Add to the list of metadata.
     * 
     * <p>
     * Note: complex objects in ES are JSON strings in this context.
     * 
     * @param namespace Metadata namespace as defined in ES.
     * @param property Metadata name as defined in ES.
     * @param value Metadata value.
     * @return
     */
    public CreationParams addMetadata(String namespace, String property, Object value) {
      arr("metadatas", new Object[] {namespace, property, value});
      return this;
    }

    /**
     * Possible additional params for the document type include:
     * 
     * <ul>
     * <li>colorSpaceName
     * <li>viewingCondition
     * <li>documentWorkflow
     * </ul>
     * 
     * @param param name.
     * @param value to set.
     * @return
     */
    public CreationParams withParam(String param, String value) {
      put(param, value);
      return this;
    }

  }

  /**
   * Edit parameters for existing document. The path or ID is required.
   * 
   * @param ref One of ID or path is mandatory.
   * @return
   */
  public static EsDocument.EditParams edit(EsReferenceable ref) {
    return new EditParams(ref);
  }

  /**
   * Edit parameters for existing document. Reference to existing document required.
   * 
   * @author Jason Keeber <jason@keeber.org>
   *
   */
  public static class EditParams extends ApiRequest<EsStatus> {

    public EditParams(EsReferenceable ref) {
      super("document.edit", EsStatus.class);
      put("path", ref.getPath());
      put("ID", ref.getID());
    }

    public EditParams withName(String name) {
      put("name", name);
      return this;
    }

    public EditParams withDescription(String description) {
      put("description", description);
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
     * Possible additional params for the document type include:
     * 
     * <ul>
     * <li>colorSpaceName
     * <li>viewingCondition
     * <li>documentWorkflow
     * </ul>
     * 
     * @param param name.
     * @param value to set.
     * @return
     */
    public EditParams withParam(String param, String value) {
      put(param, value);
      return this;
    }

    /**
     * Add to the list of metadata.
     * 
     * <p>
     * Note: complex objects in ES are JSON strings in this context.
     * 
     * @param namespace Metadata namespace as defined in ES.
     * @param property Metadata name as defined in ES.
     * @param value Metadata value.
     * @return
     */
    public EditParams addMetadata(String namespace, String property, Object value) {
      this.arr("metadatas", new Object[] {namespace, property, value});
      return this;
    }

  }

}
