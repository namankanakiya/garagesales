/**
 * This package is for models.
 */
package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.OneToMany;
import javax.xml.bind.DatatypeConverter;

import com.avaje.ebean.Model;
import play.data.Form;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.Logger;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.ArrayList;
import java.security.SecureRandom;

/**
 * Class which describes the User entity.
 */
@Entity
public class User extends Model {
    /**
     * The username of this User.
     */
    @Id
    @Constraints.Required(message = "Please provide a username")
    @Constraints.Pattern(value = "[a-zA-Z]\\w*", message = "Invalid username")
    @Column(name = "username")
    public String username;

    /**
     * The email of this User.
     */
    @Constraints.Required(message = "Please provide an email address")
    @Constraints.Email(message = "Invalid email address")
    public String email;

    /**
     * The minimum password length.
     */
    private static final int MIN_PASS_LENGTH = 4;

    /**
     * Finds and returns a user via String(username) and the User class.
     */
    public static final Finder<String, User> FIND
            = new Finder<String, User>(String.class, User.class);

    /**
     * The maximum login attempts.
     */
    private static final int MAX_LOGIN_ATTEMPTS = 3;

    /**
     * The maximum length of string to be used decremented by 1.
     */
    private static final int MAX_SALT_INDEX = 12;

    /**
     * The password of this User.
     */
    @Constraints.Required(message = "Please provide a password")
    @Constraints.MinLength(value = MIN_PASS_LENGTH,
            message = "Password must be at least 8 characters long")
    public String password;

    /**
     * The roles this User has.
     */
    @OneToMany
    public List<Roles> roles;

    /**
     * The salted password.
     */
    public String salt;

    /**
     * The name of this User.
     */
    public String name;

    /**
     * The address of this User.
     */
    public String address;

    /**
     * The description of this User.
     */
    public String description;

    /**
     * The phone number of this User.
     */
    public String phoneNumber;

    /**
     * Whether this User is a SuperUser.
     */
    public boolean superUser;

    /**
     * Whether this User is locked.
     */
    public boolean locked;

    /**
     * The number of login attempts.
     */
    public int loginCounter;

    /**
     * The default constructor for User.
     */
    public User() {
        //intentional empty constructor.
    }

    /**
     * Constructor for the User by email, username, and password.
     * @param email Their email address
     * @param username Their username
     * @param password Their password (salted)
     */
    public User(final String email, final String username,
            final String password) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.salt = generateSalt();
        this.loginCounter = 0;
    }

    /**
     * @return a User with null properties
     */
    public static User newUser() {
        return new User(null, null, null);
    }

    /**
     * The authentication method. Checks if there is a User in the database
     * which has both the username and password that is being inputted.
     * @param username Username
     * @param password password
     * @return User if found, null otherwise
     */
    public static String authenticate(final String username,
            final String password) {
        User user =  FIND.where().eq("username", username).findUnique();
        if (user == null) {
            return null;
        } else if (user.locked || user.loginCounter > MAX_LOGIN_ATTEMPTS) {
            user.locked = true;
            user.save();
            return "Locked";
        }
        String salt = user.salt;
        String saltPw = password + salt;
        String hashPw = getHash(saltPw);
        if (user.password.equals(hashPw)) {
            user.loginCounter = 0;
            user.save();
            return user.username;
        } else {
            user.loginCounter = user.loginCounter + 1;
            user.save();
            return null;
        }
    }

    /**
     * @return all sales this user is the sale administrator of
     */
    public final List<Sale> getSales() {
        List<Sale> salesList = new ArrayList<>();
        for (Roles role : roles) {
            if (role.salesAdmin) {
                salesList.add(role.sale);
            }
        }
        return salesList;
    }
    /**
     * @return all users
     */
    public final List<User> findAllUsers() {
        return FIND.all();
    }

    /**
     * Return a user based on their username.
     * @param username String username
     * @return User with that username
     */
    public static User findByUsername(final String username) {
        return FIND.where().eq("username", username).findUnique();
    }

    /**
     * Validation method for the registration form. If username already
     * exists in the database, return an error message.
     * @return null if username available, error message if not.
     */
    public final List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<ValidationError>();
        if (errors.isEmpty()) {
            return null;
        }
        return errors;
    }

    /**
     * Saves the user in the database.
     * @param user User to be saved into db
     */
    public static void create(final User user) {
        user.save();
    }

    /**
     * @return the username
     */
    @Override
    public final String toString() {
        return username;
    }

    /**
     * @return form filled with this user's information
     */
    public final Form<User> getForm() {
        return Form.form(User.class).fill(this);
    }

    /**
     * @return a random salted string
     */
    public final String generateSalt() {
        SecureRandom random = new SecureRandom();
        Long randomLong = random.nextLong();
        String salt = randomLong.toString().substring(0, MAX_SALT_INDEX);
        this.salt = salt;
        return salt;
    }

    /**
     * @param saltPw the salted password
     * @return the hash for the salted password
     */
    private static String getHash(final String saltPw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(saltPw.getBytes("UTF-8"));
            return DatatypeConverter.printHexBinary(hash);
        } catch (NoSuchAlgorithmException ex) {
            Logger.error("some error", ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.error("some error", ex);
        }
        return null;
    }

    /**
     * @return the size of list of roles decremented by 1
     */
    public final int modifiedSize() {
        return roles.size() - 1;
    }

    /**
     * @param value the value to change locked status to
     */
    public final void toggle(final boolean value) {
        locked = value;
    }
}
