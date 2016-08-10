package controllers;

import models.Item;
import models.Roles;
import models.Sale;
import models.User;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.about;
import views.html.home;
import views.html.index;
import views.html.login;
import views.html.register;
import views.html.about;
import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;
import javax.inject.Inject;
import java.io.File;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import org.apache.commons.mail.EmailAttachment;


/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    /**
     * The controller to the index page. Makes sure the session is
     * authenticated and gives the inputs of the sales the user is part of,
     * and the items the user is selling, as well as passing the User in as
     * well.
     * @return A result code
     */
    @Security.Authenticated(Secured.class)
    public final Result index() {
        User curUser = User.FIND.byId(request().username());
        List<Sale> allSales = Sale.findAllSales();

        return ok(index.render(curUser, allSales));
    }

    /**
     * POST method for updating profile.
     * @return good on update
     */
    public final Result updateProfile() {
        Form<User> doneForm = Form.form(User.class).bindFromRequest();
        User user = doneForm.get();
        user.update();
        flash("success", "Successfully updated profile");
        return redirect(request().getHeader("referer"));
    }

    /**
     * Maps the form in the page to the Login class defined below.
     * @return HTTP Result
     */
    public final Result login() {
        if (!session().isEmpty()) {
            return redirect("/index");
        }
        return ok(login.render(Form.form(Login.class), new User()));
    }

    /**
     * Maps the registration form to the User class (as we are registering
     * users).
     * @return HTTP Results
     */
    public final Result register() {
        if (!session().isEmpty()) {
            return redirect("/index");
        }

        return ok(register.render(Form.form(User.class), new User()));
    }

    /**
     * On POST in registration page, this will check if the form has errors
     * (as defined by the validate method defined in User.class). If no
     * errors, it will create the User and save it to the database.
     * @return HTTP Result
     */
    public final Result newRegistration() {
        Form<User> registrationFormComplete =
                Form.form(User.class).bindFromRequest();
        if (registrationFormComplete.hasErrors()) {
            flash("error", "Please correct the form below");
            return badRequest(register.render(registrationFormComplete, new
                    User()));
        }
        User newUser = registrationFormComplete.get();
        if (newUser.findByUsername(newUser.username) != null) {
            flash("error", "Username already taken");
            return badRequest(register.render(registrationFormComplete, new
                    User()));
        }
        newUser.generateSalt();
        newUser.password = newUser.password + newUser.salt;
        String hashPW = getHash(newUser.password);
        if (hashPW != null) {
            newUser.password = hashPW;
        }
        Roles origRole = new Roles(newUser);
        newUser.roles.add(origRole);
        User.create(newUser);
        origRole.save();
        flash("success", "Registered successfully");
        return redirect(routes.HomeController.home());
    }

    /**
     * from the salt and pw, generate a hash.
     * @param saltPw the salted pw
     * @return the hashed pw
     */
    private String getHash(final String saltPw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(saltPw.getBytes("UTF-8"));
            return DatatypeConverter.printHexBinary(hash);
        } catch (UnsupportedEncodingException ex) {
            Logger.error("some error", ex);
        } catch (NoSuchAlgorithmException ns) {
            Logger.error("some error", ns);
        }
        return null;
    }

    /**
     * Goes to the home page.
     * @return HTTP Result
     */
    public final Result home() {
        return ok(home.render("Main Landing", new User()));
    }

    /**
     * On POST from the login page, this will check if the login form has any
     * errors (as defined by the validate method in the Login.class). If no
     * errors, it will switch the session to the User defined by the username.
     * @return HTTP Result
     */
    public final Result authenticate() {
        Form<Login> loginForm = Form.form(Login.class).bindFromRequest();
        if (loginForm.hasErrors()) {
            return ok(login.render(loginForm, new User()));
        }
        session().clear();
        session("username", loginForm.get().username);
        return redirect(routes.HomeController.index());
    }

    /**
     * On clocking logout, it will clear the session, let the user know they
     * have logged out, and escape to the landing page.
     * @return HTTP Result
     */
    public final Result logout() {
        session().clear();
        flash("Success", "You've been logged out!");
        return redirect(routes.HomeController.home());
    }

    /**
     * Checks if a username already exists in the database.
     * @param username username to search
     * @return a String indicating if username matches
     */
    public final Result userExists(final String username) {
        if (User.findByUsername(username) != null) {
            return ok("true");
        }
        return ok("false");
    }

    /**
     * Renders the about page.
     * @return about page.
     */
    public final Result about() {
        return ok(about.render(new User()));
    }

    /**
     * The login class to detect if we have the right combination.
     */
    public static class Login {
        /**
         * The username.
         */
        public String username;
        /**
         * The password.
         */
        public String password;

        /**
         * Authenticates the username with the password. Returns different
         * messages depending on the cause of error, or of success.
         * @return null if all good, message otherwise
         */
        public final String validate() {
            String result = User.authenticate(username, password);
            if (result == null) {
                return "Invalid user or password";
            } else if (result.equals("Locked")) {
                return "Locked";
            }
            return null;
        }
    }

}
