package com.dalim.esprit.api.folder;

import java.util.ArrayList;
import java.util.List;

import org.keeber.esprit.EspritAPI.ApiRequest;

import com.dalim.esprit.api.ESMetadataLayout;
import com.dalim.esprit.api.EsObject;
import com.dalim.esprit.api.EsReferenceable;
import com.dalim.esprit.api.EsStatus;
 
public class EsFolder extends EsObject {
  private String creationUser, category, path;
  private int jobID;
  private List<ESMetadataLayout> metadataLayout = new ArrayList<>();
  private boolean isNew = false;


  private EsFolder() {

  }

  private EsFolder(String path) {
    this.path = path;
  }

  /**
   * Only used in the creation of a job from folder.
   * 
   * @param path
   * @return
   */
  public static EsFolder from(String path) {
    return new EsFolder(path);
  }

  public String getCreationUser() {
    return creationUser;
  }

  public String getCategory() {
    return category;
  }

  public String getPath() {
    return path;
  }

  public int getJobID() {
    return jobID;
  }

  public List<ESMetadataLayout> getMetadataLayout() {
    return metadataLayout;
  }

  public boolean isNew() {
    return isNew;
  }

  /**
   * Return Job edit params for given folder ref.
   * 
   * 
   * @param EsReferenceable
   * @return
   */
  public static EsFolder.EditParams create(EsReferenceable ref) {
    return new EditParams(ref);
  }

  public static class EditParams extends ApiRequest<EsStatus> {

    public EditParams(EsReferenceable folder) {
      super("folder.edit", EsStatus.class);
      put("path", folder.getPath());
      put("ID", folder.getID());
    }

    /**
     * This will edit the folder name
     * 
     * @param name
     * @return
     */
    public EditParams withName(String name) {
      put("name", name);
      return this;
    }


    /**
     * This will edit the folder description.
     * 
     * @param name
     * @return
     */
    public EditParams withDescription(String description) {
      put("description", description);
      return this;
    }

    public EditParams addMetadata(String namespace, String property, Object value) {
      arr("metadatas", new Object[] {namespace, property, value});
      return this;
    }

  }

}
