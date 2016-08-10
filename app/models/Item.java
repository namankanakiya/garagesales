/**
 * This package is for models.
 */
package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Lob;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;
import play.data.validation.Constraints;

/**
 * Item class is an entity for item listed in a garage sale.
 */

@Entity
public class Item extends Model {
    /**
     * Minimum bid amount as a fraction of the price
     */
   /* public static double BID_FRACTION = 0.8;*/

    /**
     * Finds the items based off of the id.
     */
    public static final Finder<Long, Item> find = new Finder<Long, Item>(Long
            .class, Item.class);

    /**
     * The ID of this Item.
     */
    @Id
    @Constraints.Required
    public String id;

    /**
     * This Item's name.
     */
    @Constraints.Required(message = "Need an item name")
    public String itemName;

    /**
     * The price of this Item.
     */
    @Constraints.Required(message = "Need a price!")
    public double price;

    /**
     * Whether this item has already been sold.
     */
    public boolean sold;

    /**
     * The date this item is listed for.
     */
    public String listDate;

    /**
     * The description of this item.
     */
    public String description;

    /**
     * The seller of this Item.
     */
    @ManyToOne
    public User seller;

    /**
     * The name of the Sale this Item is associated with.
     */
    public String saleName;

    /**
     * The highest bid amount offered.
     */
    public double bidAmount;

    /**
     * The last user who bid on this Item.
     */
    public User bidUser;

    /**
     * Whether this Item is open for bidding or not.
     */
    public boolean biddable;

    /**
     * The Sale this Item is associated with.
     */

    /**
     * User defined fraction for bid start.
     */
    public double bidFraction;

    /**
     * Which sale this is part of.
     */
    @ManyToOne
    public Sale sale;

    /**
     * The User who bought this Item.
     */
    @ManyToOne
    public User buyer;

    /**
     * The picture of this Item.
     */
    @Lob
    public byte[] picture;

    /**
     * No args constructor.
     */
    public Item() {
        super();
    }

    /**
     * Constructor.
     *
     * @param id       id of item
     * @param itemName Name of item
     * @param price    price of item
     */
    public Item(final String id, final String itemName,
                final double price) {
        this(id, itemName, price, false, "No Description");
    }


    /**
     * Constructor.
     * @param id          id of item
     * @param itemName    Name of item
     * @param price       price of item
     * @param sold        whether it is sold or not
     * @param description item's description
     */
    public Item(final String id, final String itemName,
                final double price, final boolean sold, final String
                description) {
        super();
        this.id = id;
        this.itemName = itemName;
        this.price = price;
        this.sold = sold;
        this.listDate = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy MM dd"));
        this.description = description;
    }

    /**
     * Find all the unsold items.
     *
     * @return list of unsold items
     */
    public static List<Item> findAll() {
        return find.where().eq("sold", false).findList();
    }

    /**
     * Find all the sold items.
     *
     * @return list of sold items
     */
    public static List<Item> findAllSold() {
        return find.where().eq("sold", true).findList();
    }

    /**
     * Finds the item with a particular id.
     *
     * @param id id to find item by
     * @return item, or null if not found
     */
    public static Item findById(final String id) {
        return find.where().eq("id", id).findUnique();
    }

    /**
     * Finds items by name.
     *
     * @param itemName the itemName to search from
     * @return a list of items with the itemName contained by their name
     */
    public static List<Item> findByName(final String itemName) {
        final List<Item> allItems = findAll();
        final List<Item> result = new ArrayList<Item>();
        for (final Item item : allItems) {
            if (item.itemName.toLowerCase().contains(itemName.toLowerCase())) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * Saves the item to the db as sold.
     */
    public final void setSold() {
        this.sold = true;
    }

    /**
     * Removes the item.
     *
     * @param id the id of the item to remove
     * @return whether the item was successfully deleted or not
     */
    public static boolean removeItem(final String id) {
        boolean condition = true;
        try {
            Ebean.delete(findById(id));
        } catch (NullPointerException e) {
            condition =  false;
        }
        return condition;
    }


    /**
     * Saves the item to the db.
     *
     * @param item item to be saved
     */
    public static void save(final Item item) {
        Ebean.save(item);
    }

    /**
     * Finds all the items listed by a particular username.
     *
     * @param user the username in string format
     * @return a List of items that the user is selling
     */
    public static List<Item>
    findAllInvolving(final String user) {
        return find.fetch("sale").where().eq("sold", false).eq("sale"
                + ".rolesList.user.username", user).findList();
    }

    /**
     * Creates an item and saves it in the database. Needs to be worked on.
     *
     * @param item     Input is an item
     * @param sale     The sale the items is part of
     * @param saleName The name of the sale. (redundent?)
     * @return returns the saved db item.
     */
    public static Item create(final Item item,
                              final Long sale,
                              final String saleName) {
        item.sale = Sale.FIND.ref(sale);
        item.saleName = saleName;
        item.save();
        return item;
    }

    /**
     * new to string.
     */
    @Override
    public final String toString() {
        return String.format("%s - %s", id, itemName);
    }
}
