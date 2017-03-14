package com.gentics.mesh.auth;

import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gentics.mesh.Mesh;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.impl.AuthHandlerImpl;
import io.vertx.ext.web.handler.impl.JWTAuthHandlerImpl;

/**
 * This class extends the Vert.x AuthHandler, so that it also works when the token is set as a cookie. Central authentication handler for mesh.
 */
@Singleton
public class MeshAuthHandler extends AuthHandlerImpl implements JWTAuthHandler {

	private static final Logger log = LoggerFactory.getLogger(JWTAuthHandlerImpl.class);

	private static final Pattern BEARER = Pattern.compile("^Bearer$", Pattern.CASE_INSENSITIVE);

	private final JsonObject options;

	private MeshAuthProvider authProvider;

	@Inject
	public MeshAuthHandler(MeshAuthProvider authProvider) {
		super(authProvider);
		this.authProvider = authProvider;
		options = new JsonObject();
	}

	@Override
	public JWTAuthHandler setAudience(List<String> audience) {
		options.put("audience", new JsonArray(audience));
		return this;
	}

	@Override
	public JWTAuthHandler setIssuer(String issuer) {
		options.put("issuer", issuer);
		return this;
	}

	@Override
	public JWTAuthHandler setIgnoreExpiration(boolean ignoreExpiration) {
		options.put("ignoreExpiration", ignoreExpiration);
		return this;
	}

	@Override
	public void handle(RoutingContext context) {

		// 1. Mesh accepts JWT tokens via the cookie as well in order to handle JWT even for regular HTTP Download requests (eg. non ajax requests (static file downloads)).
		// Store the found token value into the authentication header value. This will effectively overwrite the AUTHORIZATION header value. 
		Cookie tokenCookie = context.getCookie(MeshAuthProvider.TOKEN_COOKIE_KEY);
		if (tokenCookie != null) {
			context.request().headers().set(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCookie.getValue());
		}

		User user = context.user();
		if (user != null) {
			// Already authenticated in, just authorise
			authorise(user, context);
			return;
		}
		final HttpServerRequest request = context.request();
		String token = null;

		// 2. Try to load the token from the AUTHORIZATION header value
		final String authorization = request.headers().get(HttpHeaders.AUTHORIZATION);
		if (authorization != null) {
			String[] parts = authorization.split(" ");
			if (parts.length == 2) {
				final String scheme = parts[0], credentials = parts[1];

				if (BEARER.matcher(scheme).matches()) {
					token = credentials;
				}
			} else {
				log.warn("Format is Authorization: Bearer [token]");
				context.fail(401);
				return;
			}
		} else {
			log.warn("No Authorization header was found");
			handle401(context);
			return;
		}

		// 3. Check whether an actual token value was found otherwise we can exit early
		if (token == null) {
			log.warn("No Authorization token value was found");
			handle401(context);
			return;
		}

		// 4. Authenticate the found token using JWT
		JsonObject authInfo = new JsonObject().put("jwt", token).put("options", options);
		authProvider.authenticate(authInfo, res -> {

			// Authentication was successful. Lets update the token cookie to keep it alive
			if (res.succeeded()) {
				final User user2 = res.result();
				context.setUser(user2);
				String jwtToken = authProvider.generateToken(user2);
				// Remove the original cookie and set the new one
				context.removeCookie(MeshAuthProvider.TOKEN_COOKIE_KEY);
				context.addCookie(Cookie.cookie(MeshAuthProvider.TOKEN_COOKIE_KEY, jwtToken)
						.setMaxAge(Mesh.mesh().getOptions().getAuthenticationOptions().getTokenExpirationTime()).setPath("/"));
				authorise(user2, context);
			} else {
				log.warn("JWT decode failure", res.cause());
				handle401(context);
			}
		});

	}

	private void handle401(RoutingContext context) {
		context.fail(401);
	}

}