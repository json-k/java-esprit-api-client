package org.keeber.esprit;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dalim.esprit.api.EsBase;
import com.dalim.esprit.api.EsClass;
import com.dalim.esprit.api.EsClassable;
import com.dalim.esprit.api.EsDirectoryObject;
import com.dalim.esprit.api.EsObject;
import com.dalim.esprit.api.EsReferenceable;
import com.dalim.esprit.api.EsStatus;
import com.dalim.esprit.api.EsStream;
import com.dalim.esprit.api.EsXMP;
import com.dalim.esprit.api.admin.EsError;
import com.dalim.esprit.api.admin.EsLoginInformation;
import com.dalim.esprit.api.admin.EsLoginResponse;
import com.dalim.esprit.api.admin.EsMethodList;
import com.dalim.esprit.api.admin.EsVersion;
import com.dalim.esprit.api.customer.EsCustomer;
import com.dalim.esprit.api.directory.EsUserProfiles;
import com.dalim.esprit.api.document.EsApprovalStatus;
import com.dalim.esprit.api.document.EsDocument;
import com.dalim.esprit.api.document.EsNote;
import com.dalim.esprit.api.document.EsNote.CmykColor;
import com.dalim.esprit.api.folder.EsFolder;
import com.dalim.esprit.api.job.EsJob;
import com.dalim.esprit.api.production.EsApproval;
import com.dalim.esprit.api.production.EsColorSpace;
import com.dalim.esprit.api.production.EsProjectTemplate;
import com.dalim.esprit.api.production.EsSelectResult;
import com.dalim.esprit.api.production.EsSmartViewResult;
import com.dalim.esprit.api.production.EsSqlResult;
import com.dalim.esprit.api.production.EsViewingCondition;
import com.dalim.esprit.api.production.EsWorkflow;
import com.dalim.esprit.api.workflow.EsExportedWorkflow;
import com.dalim.esprit.api.workflow.EsWorkflowStatus;
import com.dalim.esprit.api.workflow.EsWorkflowStep;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * <p>
 * Main entry point, implementation of the RPC Dalim API.
 * 
 * <p>
 * This class is structured with accessible and inaccessible sub classes - so why not separate
 * packages? There is no concept of a sub-package in Java, this class design is specifically for
 * scoping when the class is used in production.
 * 
 * @author Jason Keeber <jason@keeber.org>
 *
 */
public class EspritAPI implements Closeable {
  private static final String INT_ENDPOINT = "/Esprit/public/Interface/";
  private static final String RPC_ENDPOINT = INT_ENDPOINT + "rpc";
  private static final String UPL_ENDPOINT = INT_ENDPOINT + "upload";

  /**
   * Construct from the given username / password combination for the defined host endpoint.
   * 
   * @param endpoint eg: https//server.domain.com
   * @param username Esprit Username
   * @param password Esprit Password
   */
  public EspritAPI(String endpoint, String username, String password) {
    this(endpoint, Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));
  }

  /**
   * Construct from the given Base64 auth username:password combination for the defined host
   * endpoint.
   * 
   * @param endpoint eg: https//server.domain.com
   * @param auth base64 username:password combination
   */
  public EspritAPI(String endpoint, String auth) {
    // All constructors lead hear.
    transport = new Transport(endpoint, auth);
    Logger.getLogger("com.joestelmach.natty.Parser").setLevel(Level.OFF);
  }

  /**
   * Construct from the given username / password combination for the defined host endpoint - and
   * optionally open it on creation. This method is useful in the try-with-resources pattern (as
   * this object is closeable).
   * 
   * @param endpoint eg: https//server.domain.com
   * @param username Esprit Username
   * @param password Esprit Password
   * @param login open this connection on creation
   * @throws EspritConnectionException
   */
  public EspritAPI(String endpoint, String username, String password, boolean login) throws EspritConnectionException {
    this(endpoint, username, password);
    if (login) {
      login();
    }
  }

  public ApiResponse<EsLoginResponse> login() throws EspritConnectionException {
    if (isLoggedIn()) {
      throw new EspritConnectionException("LOGIN Failed [Not called][Already logged in]");
    }
    ApiResponse<EsLoginResponse> response = transport.execute(ApiRequest.from("admin.login", EsLoginResponse.class));
    response.ifResult(o -> {
      transport.setSession(o.getSessionID());
    });
    return response;
  }

  public boolean isLoggedIn() {
    return transport.hasSession();
  }

  /**
   * Delegates the the {@link EspritAPI#logout() logout()} method - this allows this class to be
   * used as a resource.
   */
  public void close() {
    logout();
  }

  public void logout() {
    if (isLoggedIn()) {
      try {
        transport.execute(ApiRequest.from("admin.logout"));
      } catch (EspritConnectionException e) {
        // Close is silent
      }
      transport.setSession(null);
    }
  }

  public transient Admin admin = new Admin();

  public transient Customer customer = new Customer();

  public transient Directory directory = new Directory();

  public transient Document document = new Document();

  public transient Job job = new Job();

  public transient Folder folder = new Folder();

  public transient Production production = new Production();

  public transient Metadata metadata = new Metadata();

  public transient Monitoring monitoring = new Monitoring();

  public transient Workflow workflow = new Workflow();

  /**
   * The "admin.*" API methods.
   * 
   * @author Jason Keeber <jason@keeber.org>
   *
   */
  public final class Admin {

    /**
     * Method - "admin.getVersion" This method sends back the version of the API supported by the ES
     * server.
     * 
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsVersion> getVersion() throws EspritConnectionException {
      return transport.execute(ApiRequest.from("admin.getVersion", EsVersion.class));
    }

    /**
     * Method - "admin.login"
     * 
     * @deprecated Not strictly deprecated - delegates to the {@link EspritAPI#login() login()}
     *             method (which is preferred).
     * 
     * @return APIResponse<ESLoginResponse>
     * @throws EspritConnectionException
     */
    @Deprecated
    public ApiResponse<EsLoginResponse> login() throws EspritConnectionException {
      return EspritAPI.this.login();
    }

    /**
     * Method - "admin.logout"
     * 
     * @deprecated Not strictly deprecated - delegates to the {@link EspritConnection#logout()
     *             logout()} method (which is preferred).
     */
    @Deprecated
    public void logout() {
      EspritAPI.this.logout();
    }

    /**
     * Method - "admin.getMethodList" This method is undocumented in the API docs (+200 points for
     * irony).
     * 
     * @return a list of all of the ES API methods
     * @throws EspritConnectionException
     */
    public ApiResponse<EsMethodList> getMethodList() throws EspritConnectionException {
      return transport.execute(ApiRequest.from("admin.getMethodList", EsMethodList.class));
    }

    /**
     * Method - "admin.getLoginInformation" Returns the login information for the current user. This
     * is how you get the username as opposed to the login (undocumented in the API docs).
     * 
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsLoginInformation> getLoginInformation() throws EspritConnectionException {
      return transport.execute(ApiRequest.from("admin.getLoginInformation", EsLoginInformation.class));
    }
  }

  /**
   * The "customer.*" API methods
   * 
   * @author Jason Keeber <jason@keeber.org>
   *
   */
  public final class Customer {

    /**
     * <p>
     * Method - "customer.get" Retrieves the general information of a customer (as created with the
     * method customer.create).
     * 
     * @param ref One of ID or path is mandatory.
     * @param withXMP If true
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsCustomer> get(EsReferenceable ref, boolean withXMP) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("customer.get", EsCustomer.class, ref).put("withXMP", withXMP));
    }

    /**
     * Method - "customer.create" Creates a customer.
     * <p>
     * Create params are created using the static ESCustomer.create method:
     * <p>
     * {@code
     * ESCustomer.create("Customer Name").withCode("CST")
     *}
     *
     * @param params Customer creation params.
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsObject> create(EsCustomer.CreationParams params) throws EspritConnectionException {
      return transport.execute(params);
    }

    /**
     * Method - "customer.edit" Edit an existing customer. The list of parameters allows to change
     * the value of each of them, except the name of the customer.
     * <p>
     * Edit params are created using the static ESCustomer.edit method:
     * <p>
     * {@code
     * ESCustomer.edit(ESRef.from("/Customer Name")).withDescription("New description")
     *}
     * 
     * @param params
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsStatus> edit(EsCustomer.EditParams params) throws EspritConnectionException {
      return transport.execute(params);
    }

    /**
     * Method - "customer.delete" Deletes a customer.
     * 
     * <p>
     * In testing this deleted the customer but did not appear to remove all references. When trying
     * to create a customer with the same name I received an error.
     * 
     * @param params
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsStatus> delete(EsReferenceable ref) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("customer.delete", EsStatus.class, ref));
    }

  }

  /**
   * The "directory.*" API methods.
   * 
   * @author Jason Keeber <jason@keeber.org>
   *
   */
  public final class Directory {


    /**
     * Method - "directory.userProfiles" Returns the list of existing user profiles.
     * 
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsUserProfiles> userProfiles() throws EspritConnectionException {
      return transport.execute(ApiRequest.from("directory.userProfiles", EsUserProfiles.class));
    }

    /**
     * Method - "directory.search" This method retrieves directory items (users, customers,
     * groups...) according to a Lucene query.
     * 
     * @param params from {@link #newSearchParams(String)}
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsDirectoryObject.ListOf> search(SearchParams params) throws EspritConnectionException {
      return transport.execute(params);
    }

    public SearchParams newSearchParams(String query) {
      return new SearchParams(query);
    }

    public class SearchParams extends ApiRequest<EsDirectoryObject.ListOf> {

      protected SearchParams(String query) {
        super("directory.search", EsDirectoryObject.ListOf.class);
        put("query", query);
      }

      /**
       * Default: 3000
       * 
       * @param maxHits
       * @return
       */
      public SearchParams maxHits(int maxHits) {
        put("maxHits", maxHits);
        return this;
      }

      /**
       * If true the query is completed with * Default: false
       * 
       * @param substringSearch
       * @return
       */
      public SearchParams substringSearch(boolean substringSearch) {
        put("substringSearch", substringSearch);
        return this;
      }

      public SearchParams acceptCustomer(boolean acceptCustomer) {
        put("acceptCustomer", acceptCustomer);
        return this;
      }

      public SearchParams acceptUser(boolean acceptUser) {
        put("acceptUser", acceptUser);
        return this;
      }
    }

  }
  /**
   * The "document.*" API methods.
   * 
   * @author Jason Keeber <jason@keeber.org>
   *
   */
  public final class Document {

    /**
     * Method - "document.get" This method retrieves the information about a document.
     * 
     * @param ref One of ID or path is mandatory.
     * @param withXMP Get the XMP associated to that document.
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsDocument> get(EsReferenceable ref, boolean withXMP) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("document.get", EsDocument.class, ref).put("withXMP", withXMP));
    }

    /**
     * Streams the file, preview, thumbnail or note report of the document corresponding to the
     * pageOrder ID.
     * 
     * @param documentID ID of the page order.
     * @param type EsStream type.
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<InputStream> get(int documentID, EsStream type) throws EspritConnectionException {
      return transport.stream(documentID, type);
    }

    /**
     * Method - "document.noteReport" Retrieves the note report of the document corresponding to the
     * ID
     * 
     * <p>
     * Don't forget to close the stream on the response.
     * 
     * @param ref One of ID or path is mandatory.
     * @param xml to retrieve the report in XML format (else PDF).
     * @param allRevision to include or not the revision.
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<InputStream> noteReport(EsReferenceable ref, boolean xml, boolean allRevision) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("document.noteReport", InputStream.class, ref).put("mode", xml ? "xml" : null).put("allRevision", allRevision));
    }

    /**
     * Method - "document.historyReport" Returns the history report of a document as an XML or PDF
     * file.
     * 
     * @param ref ref One of ID or path is mandatory.
     * @param xml to retrieve the report in XML format (else PDF).
     * @param allRevision to include or not the revision.
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<InputStream> historyReport(EsReferenceable ref, boolean xml, boolean allRevision) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("document.historyReport", InputStream.class, ref).put("mode", xml ? "xml" : null).put("allRevision", allRevision));
    }

    /**
     * Method - "document.create" Uploads a document in an existing project (except it kind of
     * doesn't).
     * <p>
     * Create params are created using the static EsDocument.create method:
     * <p>
     * {@code
     * EsDocument.create(EsRef.from(234234234),"Pageordername.tif")
     *}
     *
     * @param params
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsObject> create(EsDocument.CreationParams params) throws EspritConnectionException {
      return transport.execute(params);
    }

    /**
     * Method - "document.edit" Edit an existing document. The list of parameters allows to change
     * the value of each of them.
     * <p>
     * Edit params are created using the static EsDocument.edit method:
     * <p>
     * {@code
     * EsDocument.edit(ESRef.from(908309345)).withDescription("New description")
     *}
     * 
     * @param params
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsStatus> edit(EsDocument.EditParams params) throws EspritConnectionException {
      return transport.execute(params);
    }


    /**
     * The undocumented (at least in the 'official' API docs) upload method. <b>NOTE:</b> this
     * method closes the input stream (even in the event of a failure).
     * 
     * <p>
     * The optional length parameter allows 'full' streaming - the form data is written to the
     * socket (basically). This would make a progress bar accurate.
     * 
     * @param payload stream to upload.
     * @param length when provided will enable full streaming.
     * @param fileName PageOrder name in ES.
     * @param metadata metadata to add to document see:{@link#newUploadMetadata}
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<Boolean> upload(InputStream payload, Optional<Long> length, String fileName, UploadMetadata metadata) throws EspritConnectionException {
      return transport.upload(payload, length, fileName, metadata.map);
    }

    /**
     * Create a new upload metadata instance for the given JobID.
     * 
     * @param jobID the ES job ID.
     * @return
     */
    public UploadMetadata newUploadMetadata(int jobID) {
      return new UploadMetadata(jobID);
    }

    /**
     * A map to be constructed with a Job ID. Internally it formats the entries as expected.
     * 
     * @author Jason Keeber <jason@keeber.org>
     *
     */
    public class UploadMetadata {
      private Map<String, String> map = new HashMap<>();

      private UploadMetadata(int jobID) {
        map.put("JobID", jobID + "");
      }

      public UploadMetadata add(String namespace, String property, String value) {
        map.put("MetaData/:" + namespace + "/" + property, value);
        return this;
      }

    }

    /**
     * Method - "document.delete" This method deletes document(s).
     * 
     * @param ref One of ID or path is mandatory.
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsStatus> delete(EsReferenceable ref) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("document.delete", EsStatus.class, ref));
    }

    /**
     * Method - "document.approve" This method approves a document.
     * 
     * In testing this didn't appear to work when a default viewing condition was present.
     * 
     * @param ref One of ID or path is mandatory.
     * @param comment User comment on the approval.
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsStatus> approve(EsReferenceable ref, Optional<String> comment) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("document.approve", EsStatus.class, ref).put("comment", comment.orElse(null)));
    }

    /**
     * Method - "document.reject" This method rejects a document
     * 
     * @param ref One of ID or path is mandatory.
     * @param comment User comment on the approval.
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsStatus> reject(EsReferenceable ref, Optional<String> comment) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("document.reject", EsStatus.class, ref).put("comment", comment.orElse(null)));
    }

    /**
     * Method - "document.approvalStatus" This method gives the approval status and the viewing
     * condition of a document
     * 
     * @param ref One of ID or path is mandatory.
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsApprovalStatus> approvalStatus(EsReferenceable ref) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("document.approvalStatus", EsApprovalStatus.class, ref));
    }

    /**
     * Method - "document.getNotes" This method returns the DIALOGUE ENGINE annotations of a
     * document.
     * 
     * @param ref One of ID or path is mandatory.
     * @param pageNumber optional page number (default is all).
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsNote.ListOf> getNotes(EsReferenceable ref, Optional<Integer> pageNumber) throws EspritConnectionException {
      ApiResponse<EsNote.ListOf> response = transport.execute(ApiRequest.from("document.getNotes", EsNote.ListOf.class, ref).put("pageNumber", pageNumber.orElse(-1)));

      if (response.hasResult()) {
        // Arrange the replies under each note.
        for (EsNote note : response.get().getNotes().toArray(new EsNote[0])) {
          for (EsNote reply : response.get().getNotes().toArray(new EsNote[0])) {
            if (reply.getParentID() == note.getId()) {
              response.get().getNotes().remove(reply);
              note.getReplies().add(reply);
            }
          }
        } ;
      }
      return response;
    }

    /**
     * Method - "document.addNote" This method adds an annotation to a document (position and
     * content)
     * 
     * <p>
     * Create not with EsNote.from... methods.
     * 
     * @param ref One of ID or path is mandatory.
     * @param note Dictionary describing the note.
     * @param pageNumber In case of multipage get the notes at a given page number (default 1).
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsNote.EsNoteResponse> addNote(EsReferenceable ref, EsNote note, Optional<Integer> pageNumber) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("document.addNote", EsNote.EsNoteResponse.class, ref).put("note", note).put("pageNumber", pageNumber.orElse(1)));
    }

    /**
     * Method - "document.editNote" This method edits the parameters of an existing note.
     * 
     * @param ref One of ID or path is mandatory.
     * @param noteID ID of the note to edit.
     * @param note Dictionary describing the note.
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsStatus> editNote(EsReferenceable ref, int noteID, EsNote note) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("document.editNote", EsStatus.class, ref).put("noteID", noteID).put("note", note));
    }

    /**
     * Method - "document.getXMP" This method retrieves directly the XMP of a document.
     * 
     * @param ref One of ID or path is mandatory.
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsXMP> getXMP(EsReferenceable ref) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("document.getXMP", EsXMP.class, ref));
    }

    /**
     * Method - "document.dialogueView" This method starts the DIALOGUE ENGINE Viewer.
     * 
     * @param references either document IDs or paths.
     * @param arePaths true if the references are paths.
     * @param closeURL URL to call when the applet quit.
     * @return HTML that starts the DIALOGUE ENGINE Viewer.
     * @throws EspritConnectionException
     */
    public ApiResponse<String> dialogueView(List<String> references, boolean arePaths, Optional<String> closeURL) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("document.dialogueView", String.class).put(arePaths ? "paths" : "IDs", references).put("closeURL", closeURL.orElse(null)));
    }

    /**
     * Method - "document.deleteNote" This method deletes one note..
     * 
     * @param ref One of ID or path is mandatory.
     * @param noteID ID of the note to delete.
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsStatus> deleteNote(EsReferenceable ref, int noteID) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("document.deleteNote", EsStatus.class, ref).put("noteID", noteID));
    }

  }



  /**
   * The "job.*" API methods.
   * 
   * @author Jason Keeber <jason@keeber.org>
   *
   */
  public final class Job {

    /**
     * Method - "job.get" This method retrieves all the information about a project.
     * 
     * @param ref One of ID or path is mandatory.
     * @param withXMP Get the XMP associated to that job.
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsJob> get(EsReferenceable ref, boolean withXMP) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("job.get", EsJob.class, ref).put("withXMP", withXMP));
    }

    /**
     * Method - "job.create" Uploads a document in an existing project (except it kind of doesn't).
     * <p>
     * Create params are created using the static EsJob.create method:
     * <p>
     * {@code
     * EsJob.create(EsReferenceable.from(234234234),"MyJOB",Optional.empty());
     *}
     */
    public ApiResponse<EsObject> create(EsJob.CreationParams params) throws EspritConnectionException {
      return transport.execute(params);
    }

    /**
     * Method - "job.edit" Allows you to change the information of an existing job ticket.
     * <p>
     * Edit params are created using the static EsJob.edit method:
     * <p>
     * {@code
     * EsJob.edit(EsRef.from(234234234));
     *}
     */
    public ApiResponse<EsStatus> edit(EsJob.EditParams params) throws EspritConnectionException {
      return transport.execute(params);
    }

    /**
     * Method - "job.delete" Allows you to delete an existing project..
     * 
     * @param ref One of ID or path is mandatory.
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsStatus> delete(EsReferenceable ref) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("job.delete", EsStatus.class, ref));
    }

    /**
     * Method - "job.getWFLs" Returns the list of workflows attached to the project.
     * 
     * @param ref One of ID or path is mandatory.
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsBase.ListOf> getWFLs(EsReferenceable ref) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("job.getWFLs", EsBase.ListOf.class, ref));
    }

    /**
     * Method - "job.getXMP" Returns the XMP information of a project.
     * 
     * @param ref One of ID or path is mandatory.
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsXMP> getXMP(EsReferenceable ref) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("job.getXMP", EsXMP.class, ref));
    }

  }


  /**
   * The "folder.*" API methods.
   * 
   * @author Jason Keeber <jason@keeber.org>
   *
   */
  public final class Folder {

    /**
     * Method = "folder.get" Undocumented in the API.
     * 
     * @param ref One of ID or path is mandatory.
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsFolder> get(EsReferenceable ref) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("folder.get", EsFolder.class, ref));
    }

    /**
     * Creates a subfolder or a collection folder if the class "Job" is not specified as argument.
     * 
     * @param ref One of ID or path is mandatory.
     * @param isJob would be true if the parent object is a job folder.
     * @param name Name of the folder.
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsFolder> create(EsReferenceable ref, boolean isJob, String name) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("folder.create", EsFolder.class, ref).put("name", name).put("class", isJob ? EsClass.Job : null));
    }

    /**
     * Method - "folder.edit" Method to call the folder edition (I always thought edition was a real
     * word (in this context) but it's not).
     * <p>
     * Edit params are created using the static EsFolder.edit method:
     * <p>
     * {@code
     * EsFolder.edit(EsRef.from(234234234));
     *}
     */
    public ApiResponse<EsStatus> edit(EsFolder.EditParams params) throws EspritConnectionException {
      return transport.execute(params);
    }

    /**
     * Allows you to delete an existing collection folder.
     * 
     * @param ref One of ID or path is mandatory.
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsStatus> delete(EsReferenceable ref) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("folder.delete", EsStatus.class, ref));
    }

  }


  /**
   * The "production.*" API methods.
   * 
   * @author Jason Keeber <jason@keeber.org>
   *
   */
  public final class Production {

    /**
     * Method - "production.colorSpaces" Returns the list of color spaces defined in the ES server.
     * 
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsColorSpace.ListOf> colorSpaces() throws EspritConnectionException {
      return transport.execute(ApiRequest.from("production.colorSpaces", EsColorSpace.ListOf.class));
    }

    /**
     * Method - "production.list" This method is used to browse the ES object tree starting at top
     * level with customers and Folders.
     * 
     * @param root ESObject to list - for the root use ESObject.ROOT
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsObject.ListOf> list(EsObject root) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("production.list", EsObject.ListOf.class).put("ID", root.getID()).put("class", root.getEsclass()));
    }

    /**
     * Method - "production.projectTemplates" Returns the list of project templates.
     * 
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsProjectTemplate.ListOf> projectTemplates() throws EspritConnectionException {
      return transport.execute(ApiRequest.from("production.projectTemplates", EsProjectTemplate.ListOf.class));
    }

    /**
     * Method - "production.executeSQL" The method production.executeSQL execute Sql command on some
     * tables that ES exposes
     * 
     * @param query
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsSqlResult> executeSQL(String query) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("production.executeSQL", EsSqlResult.class).put("sql", query));
    }

    /**
     * Create a new builder to create the query for {@link #executeSQL(String)}
     * 
     * <p>
     * {@code
     * QueryBuilder query = api.production.newQueryBuilder().addColumn(ESSQLResult.Table.DOCUMENT, "name", "documentName").addClause(Table.DOCUMENT, "Selection Information:selection", "=", 1).limit(50);
     *}
     * 
     * @return
     */
    public SQLQueryBuilder newSQLQueryBuilder() {
      return new SQLQueryBuilder();
    }

    public class SQLQueryBuilder {
      private List<String> columns = new ArrayList<>();
      private List<String> clauses = new ArrayList<>();
      private List<EsSqlResult.Table> tables = new ArrayList<>();
      private Integer limit;
      private String orderBy;

      public SQLQueryBuilder addClause(EsSqlResult.Table table, String col, String comp, Object value) {
        clauses.add(table.toString() + "." + col.replaceAll(" ", "\\\\ ") + " " + comp + " '" + io.asString(value) + "'");
        return this;
      }

      public SQLQueryBuilder addColumn(EsSqlResult.Table table, String col, String as) {
        return addColumn(table, col, as, false);
      }

      public SQLQueryBuilder addColumn(EsSqlResult.Table table, String col, String as, boolean orderby) {
        return addColumn(table, col, as, orderby, false);
      }

      public SQLQueryBuilder addColumn(EsSqlResult.Table table, String col, String as, boolean orderby, boolean descending) {
        if (!tables.contains(table)) {
          tables.add(table);
        }
        if (orderby) {
          orderBy = table.toString() + "." + col.replaceAll(" ", "\\\\ ") + (descending ? " DESC" : "");
        }
        columns.add(table.toString() + "." + col.replaceAll(" ", "\\\\ ") + " as " + as.replaceAll(" ", "\\\\ "));
        return this;
      }

      public SQLQueryBuilder limit(Integer limit) {
        this.limit = limit;
        return this;
      }

      public String build(Integer offset) {
        StringBuilder builder = new StringBuilder("SELECT ");
        builder.append(io.join(",", columns)).append(" FROM ").append(io.join(",", tables));
        if (!clauses.isEmpty()) {
          builder.append(" WHERE ").append(io.join(" AND ", clauses));
        }
        if (orderBy != null) {
          builder.append(" ORDER BY " + orderBy);
        }
        if (limit != null) {
          builder.append(" LIMIT " + limit);
        }
        if (offset != null) {
          builder.append(" OFFSET " + offset);
        }
        return builder.toString();
      }

    }

    /**
     * Method - "production.select" This method allows the recovery of properties and metadata with
     * the help of filters. The properties that can be retrieved are the properties that are shown
     * when using the document.get or job.get methods. The metadata must have been declared as &lt;
     * MetadataNameSpace label &gt; . &lt; MetadataPropertyName &gt;. Two special filters are
     * available : status : can take two kinds of values, either one of 'toApprove', 'onApproval',
     * 'Aborted', or a workflow milestone. jobActivation : can take one of two values, 'Active' or
     * 'Inactive'.
     * 
     * @deprecated - this appears to be going away in ES5(something).
     * 
     * @param where the query string - customerName LIKE 'CSC%' AND jobActivation='Active' can be
     *        built with {@link #newSelectWhereBuilder}
     * @param esclass either ESClass.Job or ESClass.PageOrder
     * @param columns list of the property names creating the columns
     * @return
     * @throws EspritConnectionException
     */
    @Deprecated
    public ApiResponse<EsSelectResult> select(EsClass esclass, String where, String... columns) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("production.select", EsSelectResult.class).put("class", esclass).put("where", where).put("columns", columns));
    }

    /**
     * Create a new builder to create the select statement.
     * 
     * <p>
     * {@code
     * String select = api.production.newSelectWhereBuilder().addClause("jobID", "=",
     *       12522156).build(); }
     * 
     * @return
     */
    public SelectWhereBuilder newSelectWhereBuilder() {
      return new SelectWhereBuilder();
    }

    public class SelectWhereBuilder {
      private List<String> clauses = new ArrayList<>();

      public SelectWhereBuilder addClause(String col, String comp, Object value) {
        clauses.add(col.replaceAll(" ", "\\\\ ") + " " + comp + " '" + io.asString(value) + "'");
        return this;
      }

      public String build() {
        StringBuilder builder = new StringBuilder("");
        if (!clauses.isEmpty()) {
          builder.append(io.join(" AND ", clauses));
        }
        return builder.toString();
      }
    }

    /**
     * Method - "production.viewingConditions" Returns the list of viewing conditions defined in the
     * ES server.
     * 
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsViewingCondition.ListOf> viewingConditions() throws EspritConnectionException {
      return transport.execute(ApiRequest.from("production.viewingConditions", EsViewingCondition.ListOf.class));
    }

    /**
     * Method - "production.workflows" Returns the list of workflows installed in the ES server
     * 
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsWorkflow.ListOf> workflows() throws EspritConnectionException {
      return transport.execute(ApiRequest.from("production.workflows", EsWorkflow.ListOf.class));
    }

    /**
     * Method - "production.workflows" This method is used to get the list of Objects that a
     * workflow requires for approval
     * 
     * @param reviewer List the objects where the logged user is a reviewer
     * @param approver List the objects where the logged user is an approver
     * @param gateKeeper List the objects where the logged user is a gate keeper
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsApproval.ListOf> toApprove(boolean reviewer, boolean approver, boolean gateKeeper) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("production.toApprove", EsApproval.ListOf.class).put("reviewer", reviewer).put("approver", approver).put("gateKeeper", gateKeeper));
    }

    /**
     * Method - "production.smartView" This method executes a smart view defined in the ES server
     * and sends back the result.
     * 
     * <p>
     * What this even does is anybody's guess: I got it to execute in testing but the results don't
     * make sense AND the {@link #executeSQL(String)} method does a much better job of this, and is
     * faster.
     * 
     * <p>
     * It think this might not support spaces, or something.
     * 
     * @param name
     * @param filters
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsSmartViewResult> smartView(String name, String... filters) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("production.smartView", EsSmartViewResult.class).put("name", name).put("filters", filters));
    }

    /**
     * Method - "production.search" Executes a Lucene search to retrieve objects from ES.
     * 
     * @param params from {@link #newSearchParams(String)}
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsObject.ListOf> search(SearchParams params) throws EspritConnectionException {
      return transport.execute(params);
    }

    public SearchParams newSearchParams(String query) {
      return new SearchParams(query);
    }

    public class SearchParams extends ApiRequest<EsObject.ListOf> {

      protected SearchParams(String query) {
        super("production.search", EsObject.ListOf.class);
        put("query", query);
      }

      /**
       * Default: 3000
       * 
       * @param maxHits
       * @return
       */
      public SearchParams maxHits(int maxHits) {
        put("maxHits", maxHits);
        return this;
      }

      /**
       * If true the query is completed with * Default: false
       * 
       * @param substringSearch
       * @return
       */
      public SearchParams substringSearch(boolean substringSearch) {
        put("substringSearch", substringSearch);
        return this;
      }

      public SearchParams acceptJob(boolean acceptJob) {
        put("acceptJob", acceptJob);
        return this;
      }

      public SearchParams acceptPageOrder(boolean acceptPageOrder) {
        put("acceptPageOrder", acceptPageOrder);
        return this;
      }

      public SearchParams acceptCustomer(boolean acceptCustomer) {
        put("acceptCustomer", acceptCustomer);
        return this;
      }

    }


    /**
     * Method - "production.getObjectByPath" Undocumented.
     * 
     * @param path
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<JsonObject> getObjectByPath(String path) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("production.getObjectByPath", JsonObject.class).put("path", path));
    }

    /**
     * Method - "production.getObject" Undocumented.
     * 
     * <p>
     * Seems to return the JSON content for the original type.
     * 
     * @param ID
     * @param esclass
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<JsonObject> getObject(int ID, EsClass esclass) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("production.getObject", JsonObject.class).put("ID", ID).put("class", esclass));
    }
  }


  /**
   * The "metadata.*" API methods.
   * 
   * <p>
   * Undocumented in the API and the "metadata.list" method appears to dump a list of toString()
   * object results. So...I didn't implement any of them.
   * 
   * <p>
   * Looks like it might be useful if it worked.
   * 
   * @author Jason Keeber <jason@keeber.org>
   *
   */
  public final class Metadata {

    /**
     * Just in case we needed to see what it did.
     * 
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<JsonObject> list() throws EspritConnectionException {
      return transport.execute(ApiRequest.from("metadata.list"));
    }
  }

  /**
   * The "monitoring.*" API methods.
   * 
   * @author Jason Keeber <jason@keeber.org>
   *
   */
  public final class Monitoring {

    /**
     * Never returns anything - so.....yeah.
     * 
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<JsonObject> activity() throws EspritConnectionException {
      return transport.execute(ApiRequest.from("monitoring.activity"));
    }

  }

  /**
   * The "workflow.*" API methods.
   * 
   * @author Jason Keeber <jason@keeber.org>
   *
   */
  public final class Workflow {

    /**
     * Method - "workflow.exportWFL" Exports a document or project workflow.
     * 
     * 
     * @param name Name of the workflow to export.
     * @param compressed Boolean. If true the workflow description is compressed and compressed in
     *        ASCII base64.
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsExportedWorkflow> exportWFL(String name, boolean compressed) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("workflow.exportWFL", EsExportedWorkflow.class).put("name", name).put("compressed", compressed));
    }

    /**
     * Method - "workflow.importWFL" Imports a previously exported document or project workflow.
     * 
     * @param name If defined renames the workflow.
     * @param description Description of the workflow.
     * @param workflow Workflow key generated by the export.
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsExportedWorkflow> importWFL(String name, String description, String workflow) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("workflow.importWFL", EsExportedWorkflow.class).put("name", name).put("description", description).put("workflow", workflow));
    }


    /**
     * Method - "workflow.get" Retrieves information about a project or document workflow, including
     * the status of all its steps.
     * 
     * @param ref A ES classable ref EsRef.WithClass.from(... or a Document, or Job.
     * @param name of the workflow.
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsWorkflowStep.ListOf> get(EsClassable ref, String name) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("workflow.get", EsWorkflowStep.ListOf.class, ref).put("name", name));
    }

    /// Internal method because these methods have a very similar signature.
    private ApiRequest<EsWorkflowStatus> base(EsClassable ref, String method, String name) {
      return ApiRequest.from("workflow." + method, EsWorkflowStatus.class, ref).put("name", name);
    }

    /**
     * Method - "workflow.reject" Rejects a milestone requiring a validation in a document or
     * project workflow.
     * 
     * @param ref A ES classable ref EsRef.WithClass.from(... or a Document, or Job
     * @param name of the workflow.
     * @param stepID to reject.
     * @param comment optional.
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsWorkflowStatus> reject(EsClassable ref, String name, int stepID, Optional<String> comment) throws EspritConnectionException {
      return transport.execute(base(ref, "reject", name).put("stepID", stepID).put("comment", comment.orElse(null)));
    }

    /**
     * Method - "workflow.validate" Validates milestones requiring a validation in document or
     * project workflows.
     * 
     * @param ref A ES classable ref EsRef.WithClass.from(... or a Document, or Job
     * @param name of the workflow.
     * @param stepID to validate.
     * @param comment optional.
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsWorkflowStatus> validate(EsClassable ref, String name, int stepID, Optional<String> comment) throws EspritConnectionException {
      return transport.execute(base(ref, "validate", name).put("stepID", stepID).put("comment", comment.orElse(null)));
    }

    /**
     * Method - "workflow.stop" DOES NOT WORK AS DOCOUMENTED - complains about the Missing stepID.
     * 
     * @param ref
     * @param name
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsWorkflowStatus> stop(EsClassable ref, String name) throws EspritConnectionException {
      return transport.execute(base(ref, "stop", name));
    }

    /**
     * Method - "workflow.start" Starts a document or project workflow.
     * 
     * @param ref A ES classable ref EsRef.WithClass.from(... or a Document, or Job
     * @param name of the workflow.
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsWorkflowStatus> start(EsClassable ref, String name) throws EspritConnectionException {
      return transport.execute(base(ref, "start", name));
    }

    /**
     * Method - "workflow.restart" Restarts a document or project workflow, with the possibility to
     * define the step from which to restart.
     * 
     * @param ref A ES classable ref EsRef.WithClass.from(... or a Document, or Job
     * @param name of the workflow.
     * @param stepID ID of the step from which to restart the workflow
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsWorkflowStatus> restart(EsClassable ref, String name, Optional<Integer> stepID) throws EspritConnectionException {
      return transport.execute(base(ref, "restart", name).put("stepID", stepID.orElse(null)));
    }

    /**
     * Method - "workflow.startUserAction" Executes a User Action on documents or projects. The
     * class of objects used as parameters must match the class of the User Action.
     * 
     * <p>
     * Metadata can be build with api.workflow.newMetadataBuilder().add(... etc. ).build();
     * 
     * @param name Name of the User Action.
     * @param IDs ID of the ES object attached to the workflow
     * @param metadata List of metadata
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsStatus> startUserAction(String name, List<Integer> IDs, List<String[]> metadata) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("workflow.startUserAction", EsStatus.class).put("name", name).put("ID", IDs.size() == 1 ? IDs.get(0) : null).put("IDs", IDs.size() > 1 ? IDs : null).put("metadatas", metadata));
    };

    /**
     * Creates a chainable metadata builder.
     * 
     * @return
     */
    public EsMetadataBuilder newEsMetadataBuilder() {
      return new EsMetadataBuilder();
    }

    public class EsMetadataBuilder {
      private List<String[]> data = new ArrayList<>();

      public EsMetadataBuilder add(String namespace, String property, String value) {
        data.add(new String[] {namespace, property, value});
        return this;
      }

      public List<String[]> build() {
        return data.isEmpty() ? null : data;
      }

    }

  }

  private Transport transport;

  private final class Transport {
    private String endpoint, username, auth;
    private transient String sessionid = null;

    private Transport(String endpoint, String auth) {
      this.endpoint = endpoint;
      this.username = new String(Base64.getDecoder().decode(auth)).split(":")[0];
      this.auth = auth;
    }

    private boolean hasSession() {
      return sessionid != null;
    }

    private void setSession(String sessionid) {
      this.sessionid = sessionid;
    }

    private ApiResponse<Boolean> upload(InputStream payload, Optional<Long> length, String name, Map<String, String> metadata) throws EspritConnectionException {
      ApiResponse<Boolean> response = new ApiResponse<>();
      String boundary = "==" + System.currentTimeMillis() + "==";
      HttpURLConnection connection = null;
      try {
        byte[] preContent;
        byte[] pstContent;
        // We write the form data to streams so we can calculate their length - because if we have
        // the length of the file we can stream this.
        {
          ByteArrayOutputStream bos;
          PrintWriter wt;
          // Write the PREFILE FORM
          wt = new PrintWriter(new OutputStreamWriter(bos = new ByteArrayOutputStream(), io.UTF_8), true);
          // Add the parameters
          for (Entry<String, String> entry : metadata.entrySet()) {
            wt.append("--" + boundary).append(io.LF);
            wt.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"").append(io.LF);
            wt.append("Content-Type: text/plain; charset=").append(io.UTF_8).append(io.LF).append(io.LF);
            wt.append(io.asString(entry.getValue())).append(io.LF);
            wt.flush();
          }
          // File upload header
          wt.append("--" + boundary).append(io.LF);
          wt.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(URLEncoder.encode(name, io.UTF_8)).append("\"").append(io.LF);
          wt.append("Content-Type: ").append(HttpURLConnection.guessContentTypeFromName(name)).append(io.LF);
          wt.append("Content-Transfer-Encoding: binary").append(io.LF).append(io.LF);
          wt.flush();
          io.close(wt);
          preContent = bos.toByteArray();
          // Write the PSTFILE FORM
          wt = new PrintWriter(new OutputStreamWriter(bos = new ByteArrayOutputStream(), io.UTF_8), true);
          wt.append(io.LF);
          wt.append("--" + boundary + "--").append(io.LF);
          wt.flush();
          io.close(wt);
          pstContent = bos.toByteArray();
        }
        connection = (HttpURLConnection) new URL(endpoint.concat(UPL_ENDPOINT)).openConnection();
        if (length.isPresent()) {
          connection.setFixedLengthStreamingMode(preContent.length + length.get() + pstContent.length);
        }
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(10000);
        if (isLoggedIn()) {
          connection.setRequestProperty("Cookie", "JSESSIONID=" + sessionid);
        } else {
          throw new EspritConnectionException("AUTH [Not logged in to API]");
        }
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);;
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.connect();
        OutputStream os = new BufferedOutputStream(connection.getOutputStream());
        {
          io.copy(new ByteArrayInputStream(preContent), os, false);
          io.copy(new BufferedInputStream(payload), os, false);
          io.close(payload);
          io.copy(new ByteArrayInputStream(pstContent), os, false);
        }

        io.close(os);
        response.setResult(connection.getResponseCode() == 204);
      } catch (IOException e) {
        throw new EspritConnectionException("HTTP Exception.", e);
      } finally {
        io.close(payload);
        if (connection != null) {
          connection.disconnect();
        }
      }
      return response;
    }

    private ApiResponse<InputStream> stream(int documentID, EsStream type) throws EspritConnectionException {
      ApiResponse<InputStream> response = new ApiResponse<>();
      HttpURLConnection connection = null;
      try {
        connection = (HttpURLConnection) new URL(endpoint.concat(INT_ENDPOINT).concat(type.toString()).concat("/").concat(io.asString(documentID))).openConnection();
        connection.setRequestMethod("GET");
        if (isLoggedIn()) {
          connection.setRequestProperty("Cookie", "JSESSIONID=" + sessionid);
        } else {
          throw new EspritConnectionException("AUTH [Not logged in to API]");
        }
        connection.connect();
        if (connection.getResponseCode() == 200) {
          response.setResult(new io.AutocloseConnectionStream(connection, connection.getInputStream()));
        }
      } catch (IOException e) {
        if (connection != null) {
          connection.disconnect();
        }
        throw new EspritConnectionException("HTTP Exception.", e);
      }
      return response;
    }

    @SuppressWarnings("unchecked")
    private <T> ApiResponse<T> execute(ApiRequest<T> command) throws EspritConnectionException {
      ApiResponse<T> response = new ApiResponse<>();
      HttpURLConnection connection = null;
      boolean streaming = command.getType().equals(InputStream.class);
      try {
        connection = (HttpURLConnection) new URL(endpoint.concat(RPC_ENDPOINT)).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "*/*");
        connection.setConnectTimeout(10000);
        if (isLoggedIn()) {
          connection.setRequestProperty("Cookie", "JSESSIONID=" + sessionid);
        } else {
          if ("admin.login".equals(command.getMethod())) {
            connection.setRequestProperty("Authorization", "Basic " + auth);
          } else {
            throw new EspritConnectionException("AUTH Failed [Not called][Not logged in to API]");
          }
        }
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.connect();
        OutputStream os = connection.getOutputStream();
        //System.out.println("curl -H 'Content-Type: application/json' -H '" + ("admin.login".equals(command.getMethod()) ? ("Authorization: Basic " + auth) : ("Cookie: JSESSIONID=" + sessionid)) + "' -X POST -d '" + json.getCompact().toJson(command) + "' " + endpoint.concat(RPC_ENDPOINT));
        os.write(json.getCompact().toJson(command).getBytes(io.UTF_8));
        os.close();
        int code;
        if ((code = connection.getResponseCode()) == 200) {
          if (streaming) {
            response.setResult((T) new io.AutocloseConnectionStream(connection, connection.getInputStream()));
          } else {
            if (command.getType().equals(String.class)) {
              response.setResult((T) io.asString(connection.getInputStream()));
            } else {
              JsonObject raw = json.getCompact().fromJson(io.asString(connection.getInputStream()), JsonObject.class);
              if (raw.has("result")) {
                response.setResult(json.getCompact().fromJson(raw.get("result"), command.getType()));
              }
              if (raw.has("error")) {
                response.setError(json.getCompact().fromJson(raw.get("error"), EsError.class));
              }
            }
          }
        } else if (code == 401) {
          throw new EspritConnectionException("AUTH Failed [" + username + "]");
        } else {
          throw new EspritConnectionException("HTTP Failed [" + code + "]");
        }
      } catch (IOException e) {
        throw new EspritConnectionException("HTTP Exception.", e);
      } finally {
        if (connection != null && !streaming) {
          connection.disconnect();
        }
      }
      return response;
    }

  }

  private static class io {
    private static final String UTF_8 = "UTF-8";
    private static final String LF = "\r\n";

    protected static String asString(InputStream is) throws IOException {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      copy(is, bos, true);
      return bos.toString();
    }

    protected static void close(Closeable toclose) {
      if (toclose != null) {
        try {
          toclose.close();
        } catch (IOException e) {

        }
      }
    }

    protected static void copy(InputStream is, OutputStream os, boolean close) throws IOException {
      byte[] buffer = new byte[1024 * 1024];
      int len;
      while ((len = is.read(buffer)) > 0) {
        os.write(buffer, 0, len);
      }
      os.flush();
      if (close) {
        close(is);
        close(os);
      }
    }

    protected static String join(String delimiter, Collection<?> c) {
      StringBuilder builder = new StringBuilder();
      Iterator<?> i = c.iterator();
      while (i.hasNext()) {
        builder.append(asString(i.next())).append(i.hasNext() ? delimiter : "");
      }
      return builder.toString();
    }

    protected static String asString(Object obj) {
      return asString(obj, "");
    }

    protected static String asString(Object obj, String nullValue) {
      return obj == null ? nullValue : obj.toString();
    }

    /**
     * A stream that disconnection the Http URL Connection when it is closed.
     * 
     * @author Jason Keeber <jason@keeber.org>
     *
     */
    protected static class AutocloseConnectionStream extends FilterInputStream {
      private transient HttpURLConnection connection;

      protected AutocloseConnectionStream(HttpURLConnection connection, InputStream in) {
        super(in);
        this.connection = connection;
      }

      @Override
      public void close() throws IOException {
        super.close();
        connection.disconnect();
      }

    }

  }

  public static class json {
    private static Gson compact = null;
    private static Gson pretty = null;

    public static GsonBuilder detaultBuilder() {
      return new GsonBuilder().registerTypeAdapter(Date.class, new DateAdapter()).registerTypeAdapter(CmykColor.class, new CmykColorAdapter()).registerTypeAdapter(Path2D.Float.class, new PathAdapter()).registerTypeAdapter(Rectangle2D.Float.class,
          new Rectangle2DAdapter());
    }

    public static Gson getCompact() {
      return compact == null ? compact = detaultBuilder().create() : compact;
    }

    public static Gson getPretty() {
      return pretty == null ? pretty = detaultBuilder().setPrettyPrinting().create() : pretty;
    }

    public static void print(Object o) {
      System.out.println(getPretty().toJson(o));
    }

    public static <T> T convert(Object o, Class<T> type) {
      return getCompact().fromJson(getCompact().toJson(o), type);
    }

    /**
     * Adapter for date - later this can be extended to do the conversion using the natty parser
     * (probably).
     * 
     * @author Jason Keeber <jason@keeber.org>
     *
     */
    private static class DateAdapter implements JsonDeserializer<Date>, JsonSerializer<Date> {
      // private DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

      @Override
      public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(src));
      }

      @Override
      public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
          return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").parse(json.getAsString());
        } catch (Exception e) {
          return null;
        }
      }

    }

    /**
     * Adapters for Note CMYK Color.
     * 
     * @author Jason Keeber <jason@keeber.org>
     *
     */
    private static class CmykColorAdapter implements JsonDeserializer<EsNote.CmykColor>, JsonSerializer<EsNote.CmykColor> {

      @Override
      public CmykColor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        double[] values = Arrays.stream(json.getAsString().split("\\s")).mapToDouble(Double::parseDouble).toArray();
        return new EsNote.CmykColor((float) values[0], (float) values[1], (float) values[2], (float) values[3]);
      }

      @Override
      public JsonElement serialize(EsNote.CmykColor src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getC() + " " + src.getM() + " " + src.getY() + " " + src.getK());
      }

    }

    private static Pattern pathPattern = Pattern.compile("([A-Z][^A-Z]+)");

    /**
     * Adapter for a PostScript (String) Postscript Path to Java Path.
     * 
     * @author Jason Keeber <jason@keeber.org>
     *
     */
    private static class PathAdapter implements JsonDeserializer<Path2D.Float>, JsonSerializer<Path2D.Float> {

      @Override
      public Path2D.Float deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        Path2D.Float path = new Path2D.Float();
        String content;
        Matcher m = pathPattern.matcher(content = json.getAsString());
        String t;
        double[] values;
        while (m.find()) {
          t = m.group(1).substring(0, 1).toLowerCase();
          if ("z".equals(t)) {
            //
          } else {
            values = Arrays.stream(m.group(1).substring(1).trim().split("\\s")).mapToDouble(Double::parseDouble).toArray();
            if ("m".equals(t) && values.length == 2) {
              path.moveTo(values[0], values[1]);
            }
            if ("l".equals(t) && values.length == 2) {
              path.lineTo(values[0], values[1]);
            }
            if ("c".equals(t) && values.length == 6) {
              path.curveTo(values[0], values[1], values[2], values[3], values[4], values[5]);
            }
          }
        }
        if (content.endsWith("Z")) {
          path.closePath();
        }
        path.transform(AffineTransform.getScaleInstance((1 / 25.4 * 72f), (1 / 25.4 * 72f)));
        return path;
      }

      @Override
      public JsonElement serialize(Path2D.Float src, Type typeOfSrc, JsonSerializationContext context) {
        List<String> result = new ArrayList<>();
        AffineTransform transform = AffineTransform.getScaleInstance((1 / 25.4 * 72f), (1 / 25.4 * 72f));
        try {
          transform.invert();
        } catch (NoninvertibleTransformException e) {
          // e.printStackTrace();
        }
        float[] coords = new float[6];
        for (PathIterator it = src.getPathIterator(transform); !it.isDone(); it.next()) {
          int type = it.currentSegment(coords);
          if (type == PathIterator.SEG_MOVETO) {
            result.add("M" + coords[0] + " " + coords[1]);
          }
          if (type == PathIterator.SEG_CLOSE) {
            result.add("Z");
          }
          if (type == PathIterator.SEG_LINETO) {
            result.add("L" + coords[0] + " " + coords[1]);
          }
          if (type == PathIterator.SEG_QUADTO) {
            result.add("L" + coords[0] + " " + coords[1]);
            result.add("L" + coords[2] + " " + coords[3]);
          }
          if (type == PathIterator.SEG_CUBICTO) {
            result.add("C" + coords[0] + " " + coords[1] + " " + coords[2] + " " + coords[3] + " " + coords[4] + " " + coords[5]);
          }
        }

        return new JsonPrimitive(String.join(" ", result));
      }

    }

    /**
     * String rectangle to Java rectangle.
     * 
     * @author Jason Keeber <jason@keeber.org>
     *
     */
    private static class Rectangle2DAdapter implements JsonDeserializer<Rectangle2D.Float>, JsonSerializer<Rectangle2D.Float> {

      @Override
      public Rectangle2D.Float deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        double[] values = Arrays.asList(json.getAsString().split("\\s")).stream().mapToDouble(Double::parseDouble).toArray();
        return new Rectangle2D.Float((float) values[0], (float) values[1], (float) values[2], (float) values[3]);
      }

      @Override
      public JsonElement serialize(Float src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getX() + " " + src.getY() + " " + src.getWidth() + " " + src.getHeight());
      }

    }

  }

  public static class EspritConnectionException extends Exception {

    public EspritConnectionException(String message, Throwable throwable) {
      super(message, throwable);
    }

    public EspritConnectionException(Throwable throwable) {
      super(throwable);
    }

    public EspritConnectionException(String message) {
      super(message);
    }

  }

  public static class ApiRequest<T> {
    private Map<String, Object> params = new HashMap<>();
    private String method;
    private transient Class<T> type;

    protected ApiRequest(String method, Class<T> type) {
      this.method = method;
      this.type = type;
    }

    protected static <T> ApiRequest<T> from(String method, Class<T> type) {
      return new ApiRequest<T>(method, type);
    }

    protected static <T> ApiRequest<T> from(String method, Class<T> type, EsClassable ref) {
      return from(method, type, (EsReferenceable) ref).put("class", ref.getEsclass());
    }

    protected static <T> ApiRequest<T> from(String method, Class<T> type, EsReferenceable ref) {
      return new ApiRequest<T>(method, type).put("ID", ref.getID()).put("path", ref.getPath());
    }

    protected static ApiRequest<JsonObject> from(String method) {
      return new ApiRequest<JsonObject>(method, JsonObject.class);
    }

    protected ApiRequest<T> put(String key, Object value) {
      if (key.toLowerCase().endsWith("id") && value != null && value.toString().matches("[0-9]+")) {
        value = value.toString();
      }
      params.put(key, value);
      return this;
    }

    @SuppressWarnings("unchecked")
    protected ApiRequest<T> arr(String key, Object value) {
      if (params.containsKey(key)) {
        ((ArrayList<Object>) params.get(key)).add(value);
      } else {
        params.put(key, new ArrayList<Object>(Collections.singletonList(value)));
      }
      return this;
    }

    protected Class<T> getType() {
      return type;
    }

    public String getMethod() {
      return method;
    }
  }

  /**
   * API Response object with Optional errors / returned values.
   * 
   * @author Jason Keeber <jason@keeber.org>
   *
   * @param <T>
   */
  public static class ApiResponse<T> {
    private Optional<EsError> error = Optional.empty();
    private Optional<T> result = Optional.empty();

    protected ApiResponse<T> setError(EsError e) {
      this.error = Optional.ofNullable(e);
      return this;
    }

    protected ApiResponse<T> setResult(T v) {
      this.result = Optional.ofNullable(v);
      return this;
    }

    public EsError error() {
      return error.get();
    }

    public boolean hasError() {
      return error.isPresent();
    }

    public boolean hasResult() {
      return result.isPresent();
    }

    public void ifError(Consumer<EsError> consumer) {
      error.ifPresent(consumer);
    }

    /**
     * Returns the result of this API call (if there was one).
     * 
     * @return
     */
    public T get() {
      return result.get();
    }

    /**
     * Return the result or throw an exception.
     * 
     * @param exceptionSupplier
     * @return
     * @throws X
     */
    public <X extends Throwable> T orThrow(Supplier<? extends X> exceptionSupplier) throws X {
      return result.orElseThrow(exceptionSupplier);
    }

    public void ifResult(Consumer<? super T> consumer) {
      result.ifPresent(consumer);
    }

    /**
     * Get the result if present, otherwise return other.
     * 
     * @param other
     * @return
     */
    public T or(T other) {
      return result.orElse(other);
    }



  }

}
