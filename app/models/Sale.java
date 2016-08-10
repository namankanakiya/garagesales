/**
 * This package is for models.
 */
package models;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Column;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;
import play.data.format.Formats;

/**
 * Sale entity represents a garage sale.
 * It holds information specific to that sale.
 */
@Entity
public class Sale extends Model {
    /**
     * The ID of this Sale.
     */
    @Id
    public String id;

    /**
     * The name of this Sale.
     */
    @NotNull
    public String saleName;

    /**
     * The items contained in this Sale.
     */
    @ManyToMany(cascade = CascadeType.REMOVE)
    public List<Item> items = new ArrayList<Item>();

    /**
     * The list of Roles associated to this Sale.
     */
    @OneToMany(mappedBy = "sale")
    public List<Roles> rolesList = new ArrayList<Roles>();

    /**
     * The User who owns this sale.
     */
    @ManyToOne
    public User owner;

    /**
     * Whether the Sale is inactive.
     */
    public boolean inactive;

    /**
     * The date of this Sale.
     */
    @Temporal(TemporalType.DATE)
    @Column(columnDefinition = "date")
    @Future(message = "Has to be a future date!")
    @Formats.DateTime(pattern = "yyyy-MM-dd")
    @NotNull(message = "Correct pattern: yyyy-MM-dd")
    public Date date;

    /**
     * Finds the Sale based on the unique ID.
     */
    public static final Finder<Long, Sale> FIND = new Finder<Long, Sale>(
            Long.class, Sale.class);

    /**
     * No args constructor.
     */
    public Sale() {
        //meant to be empty
    }

    /**
     * Constructor for creating a Sale.
     * @param saleName the name of the sale
     * @param owner the owner who started the sale
     * @param id the ID of this Sale
     */
    public Sale(final String saleName, final User owner, final String id) {
        this.saleName = saleName;
        //members.add(owner);
        this.owner = owner;
        this.id = id;
    }



    /**
     * Way to save the Sale into the database.
     * @param saleName the name of the Sale
     * @param owner the owner of the Sale (string)
     * @param id the ID of this Sale
     * @return the Sale saved into the db
     */
    public static Sale create(final String saleName, final String owner,
            final String id) {
        User creator = User.FIND.ref(owner);
        Sale sale = new Sale(saleName, creator, id);
        sale.save();
        Ebean.saveManyToManyAssociations(sale, "members");
        return sale;
    }

    /**
     * Finds all the sales involving a certain user.
     * @param user the username in string format
     * @return the list of sales the user is part of
     */
    public static List<Sale> findInvolving(final String user) {
        List<Sale> allSales = FIND.all();
        List<Sale> sales = new ArrayList<>();
        for (Sale sale : allSales) {
            List<Roles> rolesList = sale.rolesList;
            for (Roles roles : rolesList) {
                if (roles.user.username == user) {
                    sales.add(sale);
                }
            }
        }
        // return FIND.where().eq("members.username", user).findList();
        return sales;
    }

    /**
     * Finds all the sales involving a certain user,
     * who acts as SA.
     * @param user The username in string format
     * @return the list of sales the user is part of
     */
    public static List<Sale> findInvolvingSA(final String user) {
        return FIND.where().eq("owner.username", user).findList();
    }

    /**
     * Find all the active sales.
     * @return list of active sales
     */
    public static List<Sale> findAll() {
        return FIND.where().eq("inactive", false).findList();
    }

    /**
     * Find all the inactive sales.
     * @return list of inactive sales
     */
    public static List<Sale> findAllInactive() {
        return FIND.where().eq("inactive", true).findList();
    }

    /**
     * Find all active and inactive sales.
     * @return list of all sales
     */
    public static List<Sale> findAllSales() {
        return FIND.all();
    }


    /**
     * Finds the item with a particular id.
     * @param id id to find item by
     * @return item, or null if not found
     */
    public static Sale findById(final String id) {
        return FIND.where().eq("id", id).findUnique();
    }

    /**
     * Finds all sales with a certain saleName.
     * @param saleName the name of the sale
     * @return The list of sales that are associated with that sale name
     */
    public static List<Sale> findByName(final String saleName) {
        List<Sale> allItems = findAll();
        final List<Sale> result = new ArrayList<Sale>();
        for (Sale sale : allItems) {
            if (sale.saleName.toLowerCase().contains(saleName.toLowerCase())) {
                result.add(sale);
            }
        }
        return result;
    }

    @Override
    /**
     * String rep of sale.
     */
    public final String toString() {
        return saleName;
    }
}
