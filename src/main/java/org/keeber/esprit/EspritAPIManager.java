package org.keeber.esprit;

import java.io.Closeable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.keeber.esprit.EspritAPI.EspritConnectionException;

/**
 * This class manages a single EspritAPI instance.
 * 
 * <p>
 * The {@link #acquireConnection()} method basically always returns a logged in EspritAPI instance -
 * which is then returned with the {@link #releaseConnection()} method.
 * 
 * <p>
 * Once an EspritAPI instance has been released it is logged out after the @link
 * {@link #connectionTimeout connection timeout} has expired. The EspritAPI instance returned from
 * this class is special insomuch that closing it will also release the connection. This allows it
 * to be used in a try with resources block.
 * 
 * <p>
 * This class ensures that a 'hot' connection is always available without worrying about sessions
 * and timeouts.
 * 
 * @author Jason Keeber <jason@keeber.org>
 *
 */
public class EspritAPIManager {
  private transient Logger logger;
  private transient ExecutorService connPool;
  private ManagedEspritAPI api;
  private transient final AtomicInteger count = new AtomicInteger();
  private int connectionTimeout = 0;


  public final class ManagedEspritAPI extends EspritAPI implements Closeable {

    public ManagedEspritAPI(String endpoint, String auth) {
      super(endpoint, auth);
    }

    public ManagedEspritAPI(String endpoint, String username, String password, boolean login) throws EspritConnectionException {
      super(endpoint, username, password, login);
    }

    public ManagedEspritAPI(String endpoint, String username, String password) {
      super(endpoint, username, password);
    }

    @Override
    public void close() {
      releaseAPI();
    }

  }

  public EspritAPIManager(String endpoint, String username, String password) {
    this.api = new ManagedEspritAPI(endpoint, username, password);
    init();
  }

  public EspritAPIManager(String endpoint, String auth) {
    this.api = new ManagedEspritAPI(endpoint, auth);
    init();
  }

  private boolean hooked = false;

  private void init() {
    if (!hooked) { // stops the shutdown hook getting set twice
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        shutdown();
      }));
    }
  }

  /**
   * Returns an open EspritAPI connection. This connection should be 'returned' using
   * {@link #releaseConnection()}. Once the manager is holding 0 connections (after the connection
   * timeout) - it will close the connection to Esprit.
   * 
   * @return An logged in EspritAPI connection.
   * @throws EspritConnectionException
   */
  public ManagedEspritAPI acquireAPI() throws EspritConnectionException {
    synchronized (count) {
      if (count.get() == 0) {
        try {
          EspritConnectionException e = getConnectionQueue().submit(new ConnectionWorker(true)).get();
          if (e != null) {
            throw e;
          }
        } catch (InterruptedException | ExecutionException e) {
          getLogger().log(Level.SEVERE, "[Connection] Error acquiring.", e);
        }
      }
      count.incrementAndGet();
    }
    return api;
  }

  /**
   * This should be called after a connection instance (obtained with {@link #acquireConnection()})
   * is no longer needed - OR you can logout / close the connection instance.
   * 
   */
  public void releaseAPI() {
    if (connectionTimeout > 0) {
      new Timer(false).schedule(new TimerTask() {

        @Override
        public void run() {
          releaseInternal();
        }
      }, connectionTimeout);
    } else {
      releaseInternal();
    }
  }

  private void releaseInternal() {
    synchronized (count) {
      if (count.decrementAndGet() <= 0) {
        try {
          getConnectionQueue().submit(new ConnectionWorker(false)).get();
        } catch (InterruptedException | ExecutionException e) {
          getLogger().log(Level.SEVERE, "[Connection] Error returning..", e);
        }
      }
      if (count.get() < 0) {
        count.set(0);
      }
    }
  }

  /**
   * The number of milliseconds to wait before closing this managers connection after it has been
   * released.
   * 
   * @param connectionTimeout (in milliseconds).
   * @return the instance it was call on (for chaining).
   */
  public EspritAPIManager setConnectionTimeout(int connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
    return this;
  }


  /**
   * Should be called when the manager is no longer needed - it does not destroy the instance. A
   * shut down manager can be used again.
   * 
   */
  public void shutdown() {
    try {
      if (api != null && api.isLoggedIn()) {
        api.logout();
      }
    } finally {
      if (connPool != null) { // Shut down the connection queue.
        connPool.shutdownNow();
        try {
          connPool.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

        }
      }
    }
    connPool = null;
  }

  private ExecutorService getConnectionQueue() {
    return connPool == null ? connPool = Executors.newSingleThreadExecutor() : connPool;
  }

  /**
   * A logger instance for this manager - attach a handler to it to listen along.
   * 
   * @return the logger instance for this class instance.
   */
  public Logger getLogger() {
    return logger == null ? logger = Logger.getAnonymousLogger() : logger;
  }

  private class ConnectionWorker implements Callable<EspritConnectionException> {
    private boolean open;

    private ConnectionWorker(boolean open) {
      this.open = open;
    }

    @Override
    public EspritConnectionException call() {
      if (open && !api.isLoggedIn()) {
        getLogger().fine("[Connection] Opening...");
        long start = System.currentTimeMillis();
        try {
          api.login().get();
        } catch (EspritConnectionException e) {
          return e;
        }
        getLogger().fine("[Connection] Open [" + (System.currentTimeMillis() - start) + "]ms.");
      }
      if (!open && api.isLoggedIn()) {
        getLogger().fine("[Connection] Closing...");
        api.logout();
        getLogger().fine("[Connection] Closed.");
      }
      return null;
    }
  }

}
