package com.dalim.esprit.api.document;

import java.awt.Color;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.dalim.esprit.api.EsClass;
import com.google.gson.annotations.SerializedName;

/**
 * This class represents a dialog Note (basically).
 * 
 * @author Jason Keeber <jason@keeber.org>
 *
 */
public class EsNote {
  private String modifiedAuthor, author, seenBy, modifiedAuthorDisplayName, authorDisplayName, content, dashPattern, originalContent;
  private int displayID, id, pageNumber, parentID = -1, linkID;
  private Date created, modified;
  private boolean checked, checkable;
  private float tx = 0, ty = 0, strokeWidth = 0.5f;
  @SerializedName("anchor.x")
  private float anchorX;
  @SerializedName("anchor.y")
  private float anchorY;
  @SerializedName("position.x")
  private float positionX;
  @SerializedName("position.y")
  private float positionY;
  private Path2D.Float path;
  private CmykColor color = new CmykColor(0, 0, 0, 0);
  private List<EsNote> replies;

  public EsNote(Point2D.Float anchor, String content) {
    this.anchorX = anchor.x;
    this.anchorY = anchor.y;
    this.content = content;
  }

  public static EsNote from(Point2D.Float anchor, String content) {
    return new EsNote(anchor, content);
  }

  public static EsNote from(float anchorX, float anchorY, String content) {
    return new EsNote(new Point2D.Float(anchorX, anchorY), content);
  }

  public String getModifiedAuthor() {
    return modifiedAuthor;
  }

  public String getAuthor() {
    return author;
  }

  public String getSeenBy() {
    return seenBy;
  }

  public String getModifiedAuthorDisplayName() {
    return modifiedAuthorDisplayName;
  }

  public String getAuthorDisplayName() {
    return authorDisplayName;
  }

  public String getContent() {
    return content;
  }

  public String getOriginalContent() {
    return originalContent;
  }

  public EsNote withOriginalContent(String originalContent) {
    this.originalContent = originalContent;
    return this;
  }

  public int getLinkID() {
    return linkID;
  }

  public EsNote withLinkID(int linkID) {
    this.linkID = linkID;
    return this;
  }

  /**
   * Returns the string dash pattern into a float array.
   * 
   * @return
   */
  public float[] getDashPattern() {
    /*
     * Can't do this in serialization because a dash pattern can't really be mapped a java object as
     * such.
     * 
     * Also this is some ugly code but there isn't a clean way to go from Float[] to float[] in Java
     * apparently.
     * 
     * Also, I am not sure that dash patterns even render in the interface.
     */
    float[] result = new float[0];
    if (dashPattern != null) {
      Float[] tmp = Arrays.stream(dashPattern.split("\\s")).map((s) -> (float) Float.parseFloat(s)).toArray(Float[]::new);
      result = new float[tmp.length];
      for (int i = 0; i < tmp.length; i++) {
        result[i] = (float) tmp[i];
      }
    }
    return result;
  }

  public int getDisplayID() {
    return displayID;
  }

  public int getId() {
    return id;
  }

  public int getPageNumber() {
    return pageNumber;
  }

  public int getParentID() {
    return parentID;
  }

  public Date getCreated() {
    return created;
  }

  public Date getModified() {
    return modified;
  }

  public boolean isChecked() {
    return checked;
  }

  public boolean isCheckable() {
    return checkable;
  }

  public EsNote withCheckable(boolean checkable) {
    this.checkable = checkable;
    return this;
  }

  /**
   * Convenience method based on other values.
   * 
   * @return a point based on anchorX & anchorY
   */
  public Point2D.Float getTranslation() {
    return new Point2D.Float(tx, ty);
  }

  public EsNote withTranslation(Point2D.Float point) {
    this.tx = point.x;
    this.ty = point.y;
    return this;
  }

  public float getTx() {
    return tx;
  }

  public float getTy() {
    return ty;
  }

  public float getStrokeWidth() {
    return strokeWidth;
  }

  public EsNote withStrokeWidth(float strokeWidth) {
    this.strokeWidth = strokeWidth;
    return this;
  }

  public float getAnchorX() {
    return anchorX;
  }

  public float getAnchorY() {
    return anchorY;
  }

  /**
   * Convenience method based on other values.
   * 
   * @return a point based on anchorX & anchorY
   */
  public Point2D.Float getAnchor() {
    return new Point2D.Float(anchorX, anchorY);
  }

  public float getPositionX() {
    return positionX;
  }

  public float getPositionY() {
    return positionY;
  }

  /**
   * Convenience method based on other values.
   * 
   * @return a point based on positionX & positionY
   */
  public Point2D.Float getPosition() {
    return new Point2D.Float(positionX, positionY);
  }

  public EsNote withPosition(Point2D.Float point) {
    this.positionX = point.x;
    this.positionY = point.y;
    return this;
  }

  public Path2D.Float getPath() {
    return path;
  }

  /**
   * Must be present when the note is a shape.
   *
   * @param path
   * @param translation Must be present when the note is a shape (path is present and non empty)
   */
  public EsNote withPath(Path2D.Float path, Point2D.Float translation) {
    this.path = path;
    return withTranslation(translation);
  }

  /**
   * Must be present when the note is a shape.
   *
   * @param path
   * @param translation Must be present when the note is a shape (path is present and non empty)
   */
  public EsNote withPath(Path2D.Float path, float translateX, float translateY) {
    return withPath(path, new Point2D.Float(translateX, translateY));
  }

  public CmykColor getColor() {
    return color;
  }

  public EsNote withColor(float c, float m, float y, float k) {
    this.color = new CmykColor(c, m, y, k);
    return this;
  }

  public boolean isReply() {
    // Shortcut based on another value
    return parentID != 0;
  }

  public List<EsNote> getReplies() {
    return replies == null ? replies = new ArrayList<EsNote>() : replies;
  }

  /**
   * Response from note creation.
   * 
   * @author Jason Keeber <jason@keeber.org>
   *
   */
  public static class EsNoteResponse {
    private String noteID;

    public String getNoteID() {
      return noteID;
    }

  }

  /*
   * Other classes
   */

  /**
   * This represents a CMYK color with a dodgy RGB conversion.
   * 
   * @author Jason Keeber <jason@keeber.org>
   *
   */
  public static class CmykColor {
    private float C, M, Y, K;
    private Color RGB;

    public CmykColor(float C, float M, float Y, float K) {
      this.C = C;
      this.M = M;
      this.Y = Y;
      this.K = K;
    }

    public Color getRGB() {
      return RGB == null ? RGB = new Color(Math.round(255 * (1 - C) * (1 - K)), Math.round(255 * (1 - M) * (1 - K)), Math.round(255 * (1 - Y) * (1 - K))) : RGB;
    }

    public float getC() {
      return C;
    }

    public float getM() {
      return M;
    }

    public float getY() {
      return Y;
    }

    public float getK() {
      return K;
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + id;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof EsNote)) {
      return false;
    }
    if (id != ((EsNote) obj).id) {
      return false;
    }
    return true;
  }


  /**
   * The main list of class - I am digging this pattern.
   * 
   * @author Jason Keeber <jason@keeber.org>
   *
   */
  public static class ListOf {
    private int ID, pageCount, revision, jobID;
    private String activefb, unum, mimeType;
    @SerializedName("class")
    private EsClass esclass;
    private Rectangle2D.Float mediaBox;
    private List<EsNote> notes;

    public int getID() {
      return ID;
    }

    public void setID(int iD) {
      ID = iD;
    }

    public int getPageCount() {
      return pageCount;
    }

    public void setPageCount(int pageCount) {
      this.pageCount = pageCount;
    }

    public int getRevision() {
      return revision;
    }

    public void setRevision(int revision) {
      this.revision = revision;
    }

    public int getJobID() {
      return jobID;
    }

    public void setJobID(int jobID) {
      this.jobID = jobID;
    }

    public String getActivefb() {
      return activefb;
    }

    public void setActivefb(String activefb) {
      this.activefb = activefb;
    }

    public String getUnum() {
      return unum;
    }

    public void setUnum(String unum) {
      this.unum = unum;
    }

    public String getMimeType() {
      return mimeType;
    }

    public void setMimeType(String mimeType) {
      this.mimeType = mimeType;
    }

    public EsClass getEsclass() {
      return esclass;
    }

    public void setEsclass(EsClass esclass) {
      this.esclass = esclass;
    }

    public Rectangle2D.Float getMediaBox() {
      return mediaBox;
    }

    public void setMediaBox(Rectangle2D.Float mediaBox) {
      this.mediaBox = mediaBox;
    }

    public List<EsNote> getNotes() {
      return notes;
    }

    public void setNotes(List<EsNote> notes) {
      this.notes = notes;
    }

  }

}
