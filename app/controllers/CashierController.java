package controllers;

import com.avaje.ebean.Ebean;
import models.Orders;
import models.Roles;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

//Entities we'll use
import models.Item;
import models.User;

//view imports
import views.html.order.cashierReceipt;
import views.html.order.cashier;

//data structure to store
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handles data related to orders.
 */
@Security.Authenticated(Secured.class)
public class CashierController extends Controller {

    //allows for not having to constantly pass data in mvc
    //dont need to make private because play already does so in the backend
    /**
     * A new transaction.
     */
    public Orders newTransaction;
    /**
     * The user who is being charged.
     */
    public String username;
    /**
     * How much they owe.
     */
    public String payment;
    /**
     * List of the items being bought.
     */
    public List<Item> cashierCart = new ArrayList<>();
    /**
     * The total price needing to be payed.
     */
    public double total;

    /**
     * Renders page for cashier.
     *
     * @return cashier page.
     */
    public final Result cashierTransaction() {
        User user = User.FIND.byId(request()
                .username());
        this.cashierCart.clear();
        this.total = 0;
        return ok(cashier.render(user));
    }

    /**
     * Validates username to ensure that
     * cashier is not starting a transaction for
     * themselves.
     *
     * @param username from form
     * @return result of validation
     */
    public final Result validateUsername(final String username) {
        User newCustomer = User.findByUsername(username);
        if (username.equals(request().username())) {
            return ok("own");
        } else if (newCustomer == null) {
            return ok("true");
        }
        this.username = newCustomer.username;
        return ok(this.username);
    }


    /**
     * Validates item to ensure that cashier is authorized
     * to checkout that specific item.
     * Users who can use cashier function include those assigned
     * as cashier, seller, sales admin, super user.
     *
     * @param itemID id number from form
     * @return result of validation
     */
    public final Result validateItem(final String itemID) {
        boolean canCashier = false;
        Item newItem = Item.findById(itemID);
        if (newItem == null) {
            return ok("true");
        } else {
            Set<Roles> cas = Roles.FIND.where().eq("sale", newItem.sale).where()
                    .eq("cashier", 1).findSet();
            Set<Roles> sa = Roles.FIND.where().eq("sale", newItem.sale).where()
                    .eq("salesAdmin", 1).findSet();
            Set<Roles> sel = Roles.FIND.where().eq("sale", newItem.sale).where()
                    .eq("seller", 1).findSet();
            Set<Roles> allRoles = new HashSet<>();
            allRoles.addAll(cas);
            allRoles.addAll(sa);
            allRoles.addAll(sel);
            for (Roles role : allRoles) {
                if (request().username().equals(role.user.username)) {
                    canCashier = true;
                }
            }

            Set<Roles> suSet = Roles.FIND.where().eq("user", User.FIND.byId(request()
                    .username())).where().eq("superUser", 1).findSet();

            if (!suSet.isEmpty()) {
                canCashier = true;
            }
        }
        if (cashierCart.contains(newItem)) {
            return ok("duplicate");
        } else if (newItem.sold) {
            return ok("sold");
        } else if (canCashier) {
            this.total += newItem.price;
            this.cashierCart.add(newItem);
            return ok("Your subtotal is " + this.total);
        }
        return ok("seller");
    }

    /**
     * Processes type of payment (cash/paypal/checks etc)
     * @param payment type from form
     * @return payment type.
     */
    public final Result takePayment(final String payment) {
        this.payment = payment;
        return ok(this.payment);
    }

    /**
     * Renders and creates receipt.
     *
     * @return receipt page.
     */
    public final Result createCashierOrder() {
        User user = User.FIND.byId(request()
                .username());
        String salename = "";
        for (Item item: cashierCart) {
            //need to get into database and mark this as sold
            item.setSold();
            item.buyer = User.findByUsername(this.username);
            salename = item.saleName;
            Ebean.update(item);
        }
        newTransaction = new Orders(username, cashierCart, payment, total,
                salename);
        Ebean.save(newTransaction);
        //this.payment = null;
        //this.username = null;
        this.total = 0;
        return ok(cashierReceipt.render(newTransaction, user));
    }
}
