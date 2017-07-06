package org.keeber.esprit;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
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

import com.dalim.esprit.api.EsClass;
import com.dalim.esprit.api.EsObject;
import com.dalim.esprit.api.EsRef;
import com.dalim.esprit.api.EsStatus;
import com.dalim.esprit.api.EsStream;
import com.dalim.esprit.api.admin.EsError;
import com.dalim.esprit.api.admin.EsLoginInformation;
import com.dalim.esprit.api.admin.EsLoginResponse;
import com.dalim.esprit.api.admin.EsMethodList;
import com.dalim.esprit.api.admin.EsVersion;
import com.dalim.esprit.api.customer.EsCustomer;
import com.dalim.esprit.api.directory.EsUserProfiles;
import com.dalim.esprit.api.document.EsDocument;
import com.dalim.esprit.api.job.EsJob;
import com.dalim.esprit.api.production.EsApproval;
import com.dalim.esprit.api.production.EsColorSpace;
import com.dalim.esprit.api.production.EsProjectTemplate;
import com.dalim.esprit.api.production.EsSelectResult;
import com.dalim.esprit.api.production.EsSmartViewResult;
import com.dalim.esprit.api.production.EsSqlResult;
import com.dalim.esprit.api.production.EsViewingCondition;
import com.dalim.esprit.api.production.EsWorkflow;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

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

  public transient Production production = new Production();

  public transient Metadata metadata = new Metadata();

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
      return login();
    }

    /**
     * Method - "admin.logout"
     * 
     * @deprecated Not strictly deprecated - delegates to the {@link EspritConnection#logout()
     *             logout()} method (which is preferred).
     */
    @Deprecated
    public void logout() {
      close();
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
    public ApiResponse<EsCustomer> get(EsRef ref, boolean withXMP) throws EspritConnectionException {
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
     * @param params
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
    public ApiResponse<EsStatus> delete(EsRef ref) throws EspritConnectionException {
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

    public ApiResponse<EsUserProfiles> userProfiles() throws EspritConnectionException {
      return transport.execute(ApiRequest.from("directory.userProfiles", EsUserProfiles.class));
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
    public ApiResponse<EsDocument> get(EsRef ref, boolean withXMP) throws EspritConnectionException {
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
     * The undocumented (at least in the 'official' API docs) upload method. <b>NOTE:</b> this
     * method closes the input stream (even ine event of a failure).
     * 
     * @param payload stream to upload.
     * @param fileName PageOrder name in ES.
     * @param metadata metadata to add to document see:{@link#newUploadMetadata}
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<Boolean> upload(InputStream payload, String fileName, UploadMetadata metadata) throws EspritConnectionException {
      return transport.upload(payload, fileName, metadata.map);
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
        map.put("Metadata/:" + namespace + "/" + property, value);
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
    public ApiResponse<EsStatus> delete(EsRef ref) throws EspritConnectionException {
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
    public ApiResponse<EsStatus> approve(EsRef ref, Optional<String> comment) throws EspritConnectionException {
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
    public ApiResponse<EsStatus> reject(EsRef ref, Optional<String> comment) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("document.reject", EsStatus.class, ref).put("comment", comment.orElse(null)));
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
     * So what Method - "job.get" This method retrieves all the information about a project.
     * 
     * @param ref One of ID or path is mandatory.
     * @param withXMP Get the XMP associated to that job.
     * @return
     * @throws EspritConnectionException
     */
    public ApiResponse<EsJob> get(EsRef ref, boolean withXMP) throws EspritConnectionException {
      return transport.execute(ApiRequest.from("job.get", EsJob.class, ref).put("withXMP", withXMP));
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
        if (!tables.contains(table)) {
          tables.add(table);
        }
        if (orderby) {
          orderBy = table.toString() + "." + col;
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
     * @param where the query string - customerName LIKE 'CSC%' AND jobActivation='Active' can be
     *        built with {@link #newSelectWhereBuilder}
     * @param esclass either ESClass.Job or ESClass.PageOrder
     * @param columns list of the property names creating the columns
     * @return
     * @throws EspritConnectionException
     */
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
     * @param params
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
       * If true the query is completed with * Default: falseF
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


  }


  /**
   * The "metadata.*" API methods.
   * 
   * Undocumented in the API and the "metadata.list" method appears to dump a list of unimplements
   * toString() method results. So...I didn't implement any of them.
   * 
   * @author Jason Keeber <jason@keeber.org>
   *
   */
  public final class Metadata {

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

    private ApiResponse<Boolean> upload(InputStream payload, String name, Map<String, String> metadata) throws EspritConnectionException {
      ApiResponse<Boolean> response = new ApiResponse<>();
      String boundary = null;
      HttpURLConnection connection = null;
      try {
        connection = (HttpURLConnection) new URL(endpoint.concat(UPL_ENDPOINT)).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(10000);
        if (isLoggedIn()) {
          connection.setRequestProperty("Cookie", "JSESSIONID=" + sessionid);
        } else {
          throw new EspritConnectionException("AUTH [Not logged in to API]");
        }
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + (boundary = "==" + System.currentTimeMillis() + "=="));
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.connect();
        OutputStream os;
        PrintWriter wt = new PrintWriter(new OutputStreamWriter(os = connection.getOutputStream(), io.UTF_8), true);
        {
          // Upload the file
          wt.append("--" + boundary).append(io.LF);
          wt.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(URLEncoder.encode(name, io.UTF_8)).append("\"").append(io.LF);
          wt.append("Content-Type: ").append(HttpURLConnection.guessContentTypeFromName(name)).append(io.LF);
          wt.append("Content-Transfer-Encoding: binary").append(io.LF).append(io.LF);
          wt.flush();
          io.copy(new BufferedInputStream(payload), new BufferedOutputStream(os), false);
          io.close(payload);
          wt.append(io.LF);
          wt.flush();
          // Add the parameters
          for (Entry<String, String> entry : metadata.entrySet()) {
            wt.append("--" + boundary).append(io.LF);
            wt.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"").append(io.LF);
            wt.append("Content-Type: text/plain; charset=").append(io.UTF_8).append(io.LF).append(io.LF);
            wt.append(URLEncoder.encode(io.asString(entry.getValue()), io.UTF_8)).append(io.LF);
            wt.flush();
          }
        }
        // wt.append(io.LF);
        wt.append("--" + boundary + "--").append(io.LF);
        wt.flush();
        io.close(wt);
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

    private <T> ApiResponse<T> execute(ApiRequest<T> command) throws EspritConnectionException {
      ApiResponse<T> response = new ApiResponse<>();
      HttpURLConnection connection = null;
      try {
        connection = (HttpURLConnection) new URL(endpoint.concat(RPC_ENDPOINT)).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
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
        os.write(json.getCompact().toJson(command).getBytes(io.UTF_8));
        os.close();
        int code;
        if ((code = connection.getResponseCode()) == 200) {
          JsonObject raw = json.getCompact().fromJson(io.asString(connection.getInputStream()), JsonObject.class);
          if (raw.has("result")) {
            response.setResult(json.getCompact().fromJson(raw.get("result"), command.getType()));
          }
          if (raw.has("error")) {
            response.setError(json.getCompact().fromJson(raw.get("error"), EsError.class));
          }
        } else if (code == 401) {
          throw new EspritConnectionException("AUTH Failed [" + username + "]");
        } else {
          throw new EspritConnectionException("HTTP Failed [" + code + "]");
        }
      } catch (IOException e) {
        throw new EspritConnectionException("HTTP Exception.", e);
      } finally {
        if (connection != null) {
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
      byte[] buffer = new byte[1024 * 128];
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

    public static Gson getCompact() {
      return compact == null ? compact = new GsonBuilder().create() : compact;
    }

    public static Gson getPretty() {
      return pretty == null ? pretty = new GsonBuilder().setPrettyPrinting().create() : pretty;
    }

    public static void print(Object o) {
      System.out.println(getPretty().toJson(o));
    }

    public static <T> T convert(Object o, Class<T> type) {
      return getCompact().fromJson(getCompact().toJson(o), type);
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

    protected static <T> ApiRequest<T> from(String method, Class<T> type, EsRef ref) {
      return new ApiRequest<T>(method, type).put("ID", ref.getID()).put("path", ref.getPath());
    }

    protected static ApiRequest<JsonObject> from(String method) {
      return new ApiRequest<JsonObject>(method, JsonObject.class);
    }

    protected ApiRequest<T> put(String key, Object value) {
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
