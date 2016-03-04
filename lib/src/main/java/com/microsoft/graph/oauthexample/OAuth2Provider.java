package com.microsoft.graph.oauthexample;

import android.app.Activity;
import android.app.Application;

import com.microsoft.graph.sdk.authentication.IAuthProvider;
import com.microsoft.graph.sdk.concurrency.ICallback;
import com.microsoft.graph.sdk.concurrency.SimpleWaiter;
import com.microsoft.graph.sdk.core.ClientException;
import com.microsoft.graph.sdk.core.GraphErrorCodes;
import com.microsoft.graph.sdk.http.IHttpRequest;
import com.microsoft.graph.sdk.logger.DefaultLogger;
import com.microsoft.graph.sdk.logger.ILogger;
import com.microsoft.graph.sdk.options.HeaderOption;
import com.microsoft.services.msa.LiveAuthClient;
import com.microsoft.services.msa.LiveAuthException;
import com.microsoft.services.msa.LiveAuthListener;
import com.microsoft.services.msa.LiveConnectSession;
import com.microsoft.services.msa.LiveStatus;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Supports login, logout, and signing requests with authorization information.
 */
public abstract class OAuth2Provider implements IOAuth2Provider {

    /**
     * The authorization header name.
     */
    public static final String AUTHORIZATION_HEADER_NAME = "Authorization";

    /**
     * The bearer prefix.
     */
    public static final String OAUTH_BEARER_PREFIX = "bearer ";

    /**
     * The logger instance.
     */
    private ILogger mLogger;

    /**
     * The live auth client.
     */
    private final LiveAuthClient mLiveAuthClient;

    /**
     * The application instance.
     */
    private final Application mApplication;

    /**
     * The client id for this authenticator.
     * http://graph.microsoft.io/en-us/app-registration
     *
     * @return The client id.
     */
    public abstract String getClientId();

    /**
     * The scopes for this application.
     * http://graph.microsoft.io/en-us/docs/authorization/permission_scopes
     *
     * @return The scopes for this application.
     */
    public abstract String[] getScopes();
    @Override
    public void setLogger(final ILogger logger) {
        mLogger = logger;
    }

    /**
     * Create a new instance of the provider
     * 
     * @param application the application instance
     */
    public OAuth2Provider(final Application application) {
        mLogger = new DefaultLogger();
        application.getBaseContext();
        mApplication = application;
        mLiveAuthClient = new LiveAuthClient(
                application.getApplicationContext(),
                getClientId(),
                Arrays.asList(getScopes()),
                MicrosoftOAuth2v2Endpoint.getInstance());
    }

    @Override
    public void authenticateRequest(final IHttpRequest request) {
        mLogger.logDebug("Authenticating request, " + request.getRequestUrl());

        // If the request already has an authorization header, do not intercept it.
        for (final HeaderOption option : request.getHeaders()) {
            if (option.getName().equals(AUTHORIZATION_HEADER_NAME)) {
                mLogger.logDebug("Found an existing authorization header!");
                return;
            }
        }

        if (hasValidSession()) {
            mLogger.logDebug("Found account information");
            if (mLiveAuthClient.getSession().isExpired()) {
                mLogger.logDebug("Account access token is expired, refreshing");
                loginSilentBlocking();
            }

            final String accessToken = mLiveAuthClient.getSession().getAccessToken();
            request.addHeader(AUTHORIZATION_HEADER_NAME, OAUTH_BEARER_PREFIX + accessToken);
        } else {
            final ClientException exception = new ClientException("Unable to authenticate request, No active account found",
                                                                  null,
                                                                  GraphErrorCodes.AuthenticationFailure);
            mLogger.logError(exception.getMessage(), exception);
            throw exception;
        }
    }

    @Override
    public void logout(final ICallback<Void> callback) {
        mLogger.logDebug("Logout started");

        if (callback == null) {
            throw new IllegalArgumentException("callback");
        }

        mLiveAuthClient.logout(new LiveAuthListener() {
            @Override
            public void onAuthComplete(final LiveStatus status,
                                       final LiveConnectSession session,
                                       final Object userState) {
                mLogger.logDebug("Logout complete");
                callback.success(null);
            }

            @Override
            public void onAuthError(final LiveAuthException exception, final Object userState) {
                final ClientException clientException = new ClientException("Logout failure",
                        exception,
                        GraphErrorCodes.AuthenticationFailure);
                mLogger.logError(clientException.getMessage(), clientException);
                callback.failure(clientException);
            }
        });
    }

    @Override
    public void login(final Activity activity, final ICallback<Void> callback) {
        mLogger.logDebug("Login started");

        if (callback == null) {
            throw new IllegalArgumentException("callback");
        }

        if (hasValidSession()) {
            mLogger.logDebug("Already logged in");
            callback.success(null);
            return;
        }

        final LiveAuthListener listener = new LiveAuthListener() {
            @Override
            public void onAuthComplete(final LiveStatus status,
                                       final LiveConnectSession session,
                                       final Object userState) {
                mLogger.logDebug(String.format("LiveStatus: %s, LiveConnectSession good?: %s, UserState %s",
                                               status,
                                               session != null,
                                               userState));

                if (status == LiveStatus.NOT_CONNECTED) {
                    mLogger.logDebug("Received invalid login failure from silent authentication, ignoring.");
                    return;
                }

                if (status == LiveStatus.CONNECTED) {
                    mLogger.logDebug("Login completed");
                    callback.success(null);
                    return;
                }

                final ClientException clientException = new ClientException("Unable to login successfully",
                                                                            null,
                                                                            GraphErrorCodes.AuthenticationFailure);
                mLogger.logError(clientException.getMessage(), clientException);
                callback.failure(clientException);
            }

            @Override
            public void onAuthError(final LiveAuthException exception, final Object userState) {
                final ClientException clientException = new ClientException("Login failure",
                                                                            exception,
                                                                            GraphErrorCodes.AuthenticationFailure);
                mLogger.logError(clientException.getMessage(), clientException);
                callback.failure(clientException);
            }
        };

        // Make sure the login process is started with the current activity information
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLiveAuthClient.login(activity, listener);
            }
        });
    }

    /**
     * Login a user with no ui
     *
     * @param callback The callback when the login is complete or an error occurs
     */
    private void loginSilent(final ICallback<Void> callback) {
        mLogger.logDebug("Login silent started");

        if (callback == null) {
            throw new IllegalArgumentException("callback");
        }

        final LiveAuthListener listener = new LiveAuthListener() {
            @Override
            public void onAuthComplete(final LiveStatus status,
                                       final LiveConnectSession session,
                                       final Object userState) {
                mLogger.logDebug(String.format("LiveStatus: %s, LiveConnectSession good?: %s, UserState %s",
                                               status,
                                               session != null,
                                               userState));

                if (status == LiveStatus.CONNECTED) {
                    mLogger.logDebug("Login completed");
                    callback.success(null);
                    return;
                }

                final ClientException clientException = new ClientException("Unable to login silently",
                                                                            null,
                                                                            GraphErrorCodes.AuthenticationFailure);
                mLogger.logError(clientException.getMessage(), clientException);
                callback.failure(clientException);
            }

            @Override
            public void onAuthError(final LiveAuthException exception, final Object userState) {
                final ClientException clientException = new ClientException("Unable to login silently",
                                                                            null,
                                                                            GraphErrorCodes.AuthenticationFailure);
                mLogger.logError(clientException.getMessage(), clientException);
                callback.failure(clientException);
            }
        };

        mLiveAuthClient.loginSilent(listener);
    }

    /**
     * Login silently while blocking for the call to return
     *
     * @return the result of the login attempt
     * @throws ClientException The exception if there was an issue during the login attempt
     */
    private Void loginSilentBlocking() throws ClientException {
        final SimpleWaiter waiter = new SimpleWaiter();
        final AtomicReference<Void> returnValue = new AtomicReference<>();
        final AtomicReference<ClientException> exceptionValue = new AtomicReference<>();

        loginSilent(new ICallback<Void>() {
            @Override
            public void success(final Void aVoid) {
                returnValue.set(aVoid);
                waiter.signal();
            }

            @Override
            public void failure(final ClientException ex) {
                exceptionValue.set(ex);
                waiter.signal();
            }
        });

        waiter.waitForSignal();

        //noinspection ThrowableResultOfMethodCallIgnored
        if (exceptionValue.get() != null) {
            throw exceptionValue.get();
        }

        return returnValue.get();
    }

    /**
     * Is the session object valid
     *
     * @return true, if the session is valid (but not necessary unexpired)
     */
    private boolean hasValidSession() {
        return mLiveAuthClient.getSession() != null && mLiveAuthClient.getSession().getAccessToken() != null;
    }
}
