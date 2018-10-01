# MSGraph SDK Android MSA Auth for Android Adapter

[ ![Download](https://api.bintray.com/packages/microsoftgraph/Maven/msgraph-sdk-android-msa-auth-for-android-adapter/images/download.svg) ](https://bintray.com/microsoftgraph/Maven/msgraph-sdk-android-msa-auth-for-android-adapter/_latestVersion)
[![Build Status](https://travis-ci.org/microsoftgraph/msgraph-sdk-android-msa-auth-for-android-adapter.svg?branch=master)](https://travis-ci.org/microsoftgraph/msgraph-sdk-android-msa-auth-for-android-adapter)

## Overview

The MSGraph SDK Android MSA Auth for Android Adapter library provides an implementation of the Authentication Interface for the Microsoft Graph SDK to use with the v2.0 authentication endpoint. 

This authentication provider is limited in scope and is provided to facilitate development with the v2.0 authentication endpoint while development of MSAL for each platform progresses to general availability.  You can learn more about what is currently supported on the v2.0 authentication endpoint [here](https://azure.microsoft.com/documentation/articles/active-directory-v2-compare/).

> **Note:** This project is intended to help developers get off the ground quickly with the [MSGraph SDK for Android](https://github.com/microsoftgraph/msgraph-sdk-android) and not to serve as an all-inclusive authentication library.

This library provides the following features
* login
* request signing 
* logout  

Features not listed are not supported or included and are the responsibility of the developer to support and implement.

You can fork or use this implementation as a starting point to develop functionality specific to your needs.

## 1. Installation

### 1.1 Install AAR via Gradle
Add the maven central repository to your project's build.gradle file then add a compile dependency for `com.microsoft.graph:msa-auth-for-android-adapter:0.10.+`

```gradle
repository {
    jcenter()
}

dependency {
    // Include MSGraph SDK Android MSA Auth for Android Adapter as a dependency
    compile 'com.microsoft.graph:msa-auth-for-android-adapter:0.10.+'
}
```

## 2. Getting started

### 2.1 Register your application

Register your application by following [these](http://graph.microsoft.io/en-us/app-registration) steps.

#### 2.1.1 Create a new application in Application Registration Portal

In the [Application Registration Portal](http://apps.dev.microsoft.com/) create a new application for the converged endpoint, which will function with both MSA and AAD accounts.

At the top of the app registration page note the Application Id value. This is the _client id_ we'll use in your code later on. 

#### 2.1.2 Add a mobile application platform to the application

Once the application has been created select 'Add Platform' in the Platforms section and then select the `Native Application` option in the dialog that appears.  This creates a new mobile application with a redirect url of `urn:ietf:wg:oauth:2.0:oob`.

**Note:** Make sure you scroll to the button of the page and hit **Save** to save your platform changes.

### 2.2 Set your application Id and scopes

In the `getClientId()` method shown below , replace the `<client-id>` placeholder with the Application Id value found on your app's registration page. It has this format: `00000000-0000-0000-0000-000000000000`. 

In the `getScopes()` method, add all the Microsoft Graph permission scopes your app needs. You can find a complete list of all available permission scopes [here](https://graph.microsoft.io/docs/authorization/permission_scopes). 


```java
final IAuthenticationAdapter authenticationAdapter = new MSAAuthAndroidAdapter() {
    @Override
    public String getClientId() {
        return "<client-id>";
    }

    @Override
    public String[] getScopes() {
        return new String[] {
            // An example set of scopes your application could use
            "https://graph.microsoft.com/Calendars.ReadWrite",
            "https://graph.microsoft.com/Contacts.ReadWrite",
            "https://graph.microsoft.com/Files.ReadWrite",
            "https://graph.microsoft.com/Mail.ReadWrite",
            "https://graph.microsoft.com/Mail.Send",
            "https://graph.microsoft.com/User.ReadBasic.All",
            "https://graph.microsoft.com/User.ReadWrite",
            "offline_access",
            "openid"
        };
    }
};
```

## 3. Using the IAuthenticationAdapter with a GraphService client

### 3.1 Logging In

Once you have set the client-id and scopes, you need to have your application manage the sign-in state of the user, so you can make requests against the Microsoft Graph servicee. Use the login method to force a user login, during the login flow the user will consent to use the application.

Once the authorization flow is completed the callback provided will be called, returning the flow of control back to the application.

```java
authenticationAdapter.login(getActivity(), new ICallback<Void>() {
    @Override
    public void success(final Void aVoid) {
        //Handle successful login
    }

    @Override
    public void failure(final ClientException ex) {
        //Handle failed login
    }
});
```

### 3.2 Logging Out

```java
authenticationAdapter.logout(new ICallback<Void>() {
    @Override
    public void success(final Void aVoid) {
        //Handle successful logout
    }

    @Override
    public void failure(final ClientException ex) {
        //Handle failed logout
    }
});
```

### 3.3 Integration with Graph Service client

Once the application has signed in, create a `GraphServiceClient` instance so that you can issue service requests.

```java
// Use the authentication provider previously defined within the project and create a configuration instance
final IClientConfig config = DefaultClientConfig.createWithAuthenticationProvider(authenticationAdapter);

// Create the service client from the configuration
final IGraphServiceClient client = new GraphServiceClient.Builder()
                                        .fromConfig(config)
                                        .buildClient();
```

### 3.4 Handling Authentication exceptions

While the `IAuthenticationAdapter` is being used to sign requests, it is possible for the account state to become invalid and the user would need to login again.  The recommended way to handle this scenario is to check for the `AuthenticationFailure` error code from a failed request.

```java
// Create a callback that can understand if AuthenticationFailure occurred.
final ICallback<User> callback = new ICallback<User>() {
    @Override
    public void success(final User user) {
        //Handle successful logout
    }

    @Override
    public void failure(final ClientException ex) {
        if (ex.isError(GraphErrorCodes.AuthenticationFailure)) {
            // Reset application to login again
        }

        // Handle other failed causes
    }
};

// Make a request for the current user object
client
    .getMe()
    .buildRequest()
    .get(callback);
```

## 4. Issues

For known issues, see [issues](https://github.com/microsoftgraph/msgraph-sdk-android-msa-auth-for-android-adapter/issues).

## 5. Contributions

The MSGraph SDK Android MSA Auth for Android Adapter is open for contribution for bug fixes.  This project is intended to help developers get off the ground quickly with the [MSGraph SDK for Android](https://github.com/microsoftgraph/msgraph-sdk-android) and not to serve as an all inclusive authentication library.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

## 6. Supported Android Versions
This library is supported at runtime for [Android API revision 15](http://source.android.com/source/build-numbers.html) and greater. To build the sdk you need to install Android API revision 23 or greater.

## 7. License

Copyright (c) Microsoft Corporation.  All Rights Reserved.  Licensed under the [MIT license](LICENSE).
