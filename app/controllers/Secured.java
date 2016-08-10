package controllers;

import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

/**
 * The class which checks if a user is signed in before allowing the person
 * to go to the requested page. On unauthorized access, redirect them to the
 * login page.
 */
public class Secured extends Security.Authenticator {
    /**
     * Gets the username (string) based on the session.
     * @param ctx The current session?
     * @return the username of the user logged in
     */
    @Override
    public final String getUsername(final Http.Context ctx) {
        return ctx.session().get("username");
    }

    /**
     * Redirects the user to the login page on unauth access.
     * @param ctx Th current session? See above
     * @return a redirect to the login page
     */
    @Override
    public final Result onUnauthorized(final Http.Context ctx) {
        return redirect(routes.HomeController.login());
    }
}
