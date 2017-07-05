package com.dalim.esprit.api;

/**
 * This class is created from either a path in the form /customer/project/pageorder or a specific ES
 * ID
 * 
 * <p>
 * The static 'from' methods are provided for convenience.
 * 
 * {@code
 *  ESRef.from(23423)
 * }
 * 
 * @author Jason
 *
 */
public class EsRef {
  private String path;
  private Integer ID;

  /**
   * Path to the object in the form /customer/job/document
   * 
   * The leading slash is required - even when referencing a customer.
   * 
   * @param path
   */
  public EsRef(String path) {
    this.path = path;
    if (!this.path.startsWith("/")) {
      throw new RuntimeException("ES Paths must begin with a '/' - even for customer references.");
    }
  }

  /**
   * ES Object ID.
   * 
   * @param ID
   */
  public EsRef(Integer ID) {
    this.ID = ID;
  }

  /**
   * Path to the object in the form /customer/job/document
   * 
   * The leading slash is required - even when referencing a customer.
   * 
   * @param ID
   * @return
   */
  public static EsRef from(String path) {
    return new EsRef(path);
  }

  /**
   * ES Object ID.
   * 
   * @param ID
   */
  public static EsRef from(Integer ID) {
    return new EsRef(ID);
  }

  /**
   * Create from the ID of the ESObject.
   * 
   * @param object
   * @return
   */
  public static EsRef from(EsObject object) {
    return new EsRef(object.getID());
  }


  public String getPath() {
    return path;
  }

  public Integer getID() {
    return ID;
  }



}
