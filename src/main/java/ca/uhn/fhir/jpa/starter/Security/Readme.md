# Methods For Authentication

## JWT Authentication

Users authenticate by presenting a JWT token with each request. The token contains information about the user's identity and any associated roles or permissions. Your application verifies the integrity and validity of the token using a JWT validation service (such as your JWTTokenService class) and extracts user information from the token. This approach is stateless and does not require server-side session management.
Includes(JWTConfig, JWTFilter, JWTTokenService)

## Username-Password Authentication

Alternatively, you can still use username-password authentication if needed for certain parts of your application or for specific use cases. In this case, you would typically use an AuthenticationProvider to authenticate users based on their username and password. This approach is stateful and typically involves server-side session management to keep track of authenticated users.
Includes(StaffAuthProvider)

## SCENEARIOS

wtUtils Class: This class contains methods for generating, validating, and extracting JWT tokens, as well as getting authentication from a JWT token.

JWTFilter Class: In the doFilterInternal method, it generates a JWT token based on the currently authenticated user (if any) and adds it to the response header. The shouldNotFilter method ensures that this filter is only applied to specific endpoints.

JWTFilterValidator Class: This class validates JWT tokens extracted from incoming requests. If a valid token is found, it retrieves authentication details from the token and sets them in the security context.

AuthController Class: This controller handles the authentication endpoint (/api/authenticate). It authenticates users based on their email and password, generates a JWT token using JwtUtils, and returns it in the response.

    # HOW SECURITY WORKS

    - Filter Chain Initialization:

    When your Spring application starts up, Spring Security initializes a chain of servlet filters known as the "Filter Chain".
    The Filter Chain intercepts incoming HTTP requests and processes them according to the security rules configured in your application.

    - Request Processing:

    When a request is received by your application, it passes through the Filter Chain.
    Each filter in the chain performs specific tasks related to security, such as authentication, authorization, CSRF protection, etc.

    - Filter Order:

    Filters are executed in a specific order defined by Spring Security.
    The most important filters typically come first in the chain, such as the SecurityContextPersistenceFilter, which loads the SecurityContext for the current request.

    - Security Context:

    The SecurityContext holds the authentication information for the current request.
    It contains details about the authenticated user, such as their principal (username), authorities (roles), and any additional details.

    - Authentication:

    If a request requires authentication (e.g., accessing a protected resource), Spring Security triggers the authentication process.
    This process involves determining the authentication mechanism to use (e.g., form-based login, HTTP Basic authentication, OAuth), validating credentials, and loading the user's details.

    - Authentication Providers:

    Authentication is typically handled by one or more AuthenticationProvider implementations.
    Each AuthenticationProvider is responsible for validating credentials and returning an Authentication object representing the authenticated user.

    - Authentication Manager:

    The AuthenticationManager orchestrates the authentication process by delegating to the appropriate AuthenticationProvider.
    It iterates over the available providers until one is able to successfully authenticate the user, or until authentication fails.

    - Successful Authentication:

    If authentication is successful, an Authentication object is created and stored in the SecurityContext.
    The request is then allowed to proceed to the next filters or the target controller method.

    - Authorization:

    After authentication, Spring Security performs authorization checks to determine if the authenticated user is allowed to access the requested resource.
    Authorization rules are typically configured using method security annotations (@PreAuthorize, @Secured), URL-based security configurations, or custom AccessDecisionManager implementations.

    - Access Decision:

    The AccessDecisionManager evaluates the authorization rules and decides whether to grant or deny access to the resource.
    It considers factors such as the user's roles, permissions, and any additional access control rules configured in the application.

    - Security Exceptions:

    If authentication or authorization fails, Spring Security may throw security-related exceptions, such as AuthenticationException, AccessDeniedException, etc.
    These exceptions can be handled using exception handling mechanisms provided by Spring Security, such as AuthenticationEntryPoint, AccessDeniedHandler, etc.

    - Response Processing:

    Once the request has been processed by the Filter Chain and any security checks have been performed, the response is returned to the client.
