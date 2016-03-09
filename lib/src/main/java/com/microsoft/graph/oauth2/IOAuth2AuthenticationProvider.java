package com.microsoft.graph.oauth2;

import android.app.Activity;

import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.logger.ILogger;

/**
 * An auth provider for OAuth2 http://oauth.net/2/
 */
public interface IOAuth2AuthenticationProvider extends IAuthenticationProvider {

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
