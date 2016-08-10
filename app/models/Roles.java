/**
 * This package is for models.
 */
package models;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import static play.mvc.Controller.request;

/**
 * Class which contains the roles of each user for specific sales or in
 * general (where sales = null).
 */

@Entity
public class Roles extends Model {
    /**
     * id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    /**
     * user.
     */
    @ManyToOne
    public User user;

    /**
     * sale.
     */
    @ManyToOne
    public Sale sale;

    /**
     * seller.
     */
    public boolean seller;

    /**
     * salesadmin.
     */
    public boolean salesAdmin;

    /**
     * superuser.
     */
    public boolean superUser;

    /**
     * clerk.
     */
    public boolean clerk;

    /**
     * cashier.
     */
    public boolean cashier;

    /**
     * bookeeeper.
     */
    public boolean bookkeeper;

    /**
     * guest.
     */
    public boolean guest;

    /**
     * finder.
     */
    public static final Finder<String, Roles> FIND = new Finder<String, Roles>(
            String.class, Roles.class);

    /**
     * @return the user owning this
     */
    public final User getUser() {
        return user;
    }

    /**
     * @return the ID of this
     */
    public final long getId() {
        return id;
    }

    /**
     * Public constructor for a role with a sale.
     * @param user user for which the role is
     * @param sale the sale that is specified
     */
    public Roles(final User user, final Sale sale) {
        this.user = user;
        this.sale = sale;
        guest = true;
    }

    /**
     * has no roeles.
     * @return whether all roles are false
     */
    public final boolean hasNoRoles() {
        return !(seller || salesAdmin || superUser || clerk
                || cashier || bookkeeper || guest);
    }

    /**
     * The no sale constructor.
     * @param user the user that these roles describe
     */
    public Roles(final User user) {
        this.user = user;
        guest = true;
    }

    /**
     * setall true.
     */
    public final void setAllTrue() {
        this.seller = true;
        this.salesAdmin = true;
        this.clerk = true;
        this.cashier = true;
        this.bookkeeper = true;
    }
    /**
     * To string.
     */
    @Override
    public final String toString() {
        if (sale == null) {
            return "All sales: Guest";
        }
        String x = sale.saleName + ":";
        if (seller) {
            x = x + " Seller, ";
        }
        if (salesAdmin) {
            x += " Sales Administrator, ";
        }
        if (clerk) {
            x += " Clerk, ";
        }
        if (cashier) {
            x += " Cashier, ";
        }
        if (bookkeeper) {
            x += " Bookkeeper, ";
        }
        if (guest) {
            x += " Guest, ";
        }
        return x.substring(0, x.length() - 2);
    }

}
