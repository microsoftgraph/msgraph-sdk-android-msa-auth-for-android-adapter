package com.microsoft.graph.oauthexample;

import android.app.Activity;

import com.microsoft.graph.sdk.authentication.IAuthProvider;
import com.microsoft.graph.sdk.concurrency.ICallback;
import com.microsoft.graph.sdk.http.IHttpRequest;
import com.microsoft.graph.sdk.logger.ILogger;

/**
 * Created by pnied on 3/3/2016.
 */
public interface IOAuth2Provider extends IAuthProvider {

    /**
     * Set the logger to a specific instance
     *
     * @param logger The logger instance to use
     */
    void setLogger(ILogger logger);

    /**
     * Logs out the user
     *
     * @param callback The callback when the logout is complete or an error occurs
     */
    void logout(ICallback<Void> callback);

    /**
     * Login a user by popping UI
     *
     * @param activity The current activity
     * @param callback The callback when the login is complete or an error occurs
     */
    void login(Activity activity, ICallback<Void> callback);
}
