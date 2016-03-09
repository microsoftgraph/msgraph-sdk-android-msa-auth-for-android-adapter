# OAuth2 IAuthenticationProvider Example Library for Android

[ ![Download](https://api.bintray.com/packages/microsoftgraph/Maven/oauth2/images/download.svg) ](https://bintray.com/microsoftgraph/Maven/oauth2/_latestVersion)
[![Build Status](https://travis-ci.org/microsoftgraph/oauth2.svg?branch=master)](https://travis-ci.org/microsoftgraph/oauth2)

## 1. Installation

### 1.1 Install AAR via Gradle
Add the maven central repository to your projects build.gradle file then add a compile dependency for com.microsoft.graph.oauth2:oauth2:1+

```gradle
repository {
    jcenter()
}

dependency {
    // Include OAuth2 as a dependency
    compile 'om.microsoft.graph.oauth2:oauth2:1+'
}
```

## 2. Getting started

### 2.1 Register your application

Register your application by following [these](http://graph.microsoft.io/en-us/app-registration) steps.

### 2.2 Set your application Id and scopes

Note that your _client id_ should look like `00000000-0000-0000-0000-000000000000`.

```java
final IOAuth2AuthenticationProvider authProvider = new OAuth2AuthenticationProvider() {
    @Override
    public String getClientId() {
        return "<adal-client-id>";
    }

    @Override
    protected String getScopes() {
        return "https://localhost";
    }
}
```

### 2.3 Logging In

Once you have set the correct application Id and scopes, you need to have your application manage the sign in state of the user, so you can make requests against the Graph Service.

```java
authProvider.login(getActivity(), new ICallback<Void>() {
    @Override
    public void success(final Void aVoid) {
        //Handle successful login
    }

    @Override
    public void failure(final ClientException ex) {
        //Handle failed login
    }
};
```

### 2.4 Create a Graph Service client

Once you have an application that is signed in with a user account you should then create a `GraphServiceClient` to make requests with.

```java
// Use the auth provider previously defined within the project and create a configuration instance
final IClientConfig config = DefaultClientConfig.createWithAuthProvider(authProvider);

// Create the service client from the configuration
final IGraphServiceClient client = new GraphServiceClient.Builder()
                                        .fromConfig(config)
                                        .buildClient();
```
