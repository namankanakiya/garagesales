package controllers;

import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.user.userlist;

import java.util.List;


/**
 * This controller handles some user information.
 */

public class UserController extends Controller {

    /**
     * Displays the table of users and status.
     * @return goes back to the userlist
     */
    @Security.Authenticated(Secured.class)
    public final Result userList() {
        User curUser = User.FIND.byId(request().username());
        return ok(userlist.render(
                curUser));
    }
    /**
     * Will lock/unlock a user.
     @param userid the id of user to be unlocked/locked
     @return goes back to the user list.
     */
    @Security.Authenticated(Secured.class)
    public final Result unlockUser(final String userid) {
        User user = User.findByUsername(userid);
        user.toggle(false);
        user.loginCounter = 0;
        user.save();
        return redirect(
                routes.UserController.userList());
    }
    /**
     * Will lock/unlock a user.
     @param userid the id of user to be unlocked/locked
     @return goes back to the user list.
     */
    @Security.Authenticated(Secured.class)
    public final Result lockUser(final String userid) {
        User user = User.findByUsername(userid);
        user.toggle(true);
        user.save();
        return redirect(
                routes.UserController.userList());
    }

    /**
     * Returns all users.
     * @return the list of sales the user is part of
     */
    public final List<User> generateEntireUserList() {
        User curUser = User.FIND.byId(request().username());
        return curUser.findAllUsers();
    }

}
