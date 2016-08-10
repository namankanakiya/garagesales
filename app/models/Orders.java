/**
 * This package is for models.
 */
package models;

//play stuff
import com.avaje.ebean.Model;

//data structure for putting items in a list
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Lob;
import java.util.List;


/**
 * Order class is an order entity.
 * Bidders, buyers, and cashiers can generate orders.
 */

@Entity
public class Orders extends Model {
    /**
     * ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    /**
     * username.
     */
    @ManyToOne
    public String username;

    /**
     * list of items.
     */
    @Lob
    public List<Item> itemsBought;

    /**
     * amount.
     */
    public String payment;

    /**
     * total.
     */
    public double total;

    /**
     * salename.
     */
    public String saleName;

    /**
     * Empty constructor.
     */
    public Orders() {
        //this is a comment
    }

    /**
     * Constructor with paramaters.
     * @param username the buyer's username
     * @param itemsBought the items contained in this Order
     * @param payment payment as string for this Order
     * @param total actual total payment as number for this Order
     * @param saleName name of the sale
     */
    public Orders(final String username, final List<Item> itemsBought,
                  final String payment, final Double total, final String saleName) {
        this();
        this.username = username;
        this.itemsBought = itemsBought;
        this.payment = payment;
        this.total = total;
        this.saleName = saleName;
    }
}