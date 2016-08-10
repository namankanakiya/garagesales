package controllers;

import models.Item;
import models.Roles;
import models.Sale;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.finance.financeMain;
import views.html.finance.itemReport;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Controls the processes related to the finances.
 */
@Security.Authenticated(Secured.class)
public class FinanceController extends Controller {

    /**
     * Renders page for finance's main.
     *
     * @return finance main page.
     */
    public final Result financeMain() {
        User user = User.FIND.byId(request()
                .username());
        Set<String> salesSet = new HashSet<>();
        Set<String> activeSalesSet = new HashSet<>();
        Set<String> inactiveSalesSet = new HashSet<>();
        List<Sale> allActiveSales = Sale.findAll();
        List<Sale> allInactiveSales = Sale.findAllInactive();
        for (Sale sale: allActiveSales) {
            activeSalesSet.add(sale.saleName);
        }
        for (Sale sale: allInactiveSales) {
            inactiveSalesSet.add(sale.saleName);
        }

        Set<Roles> bkSet = Roles.FIND.where().eq("user", user).where()
                .eq("bookkeeper", 1).findSet();
        Set<Roles> saSet = Roles.FIND.where().eq("user", user).where()
                .eq("salesAdmin", 1).findSet();
        Set<Roles> suSet = Roles.FIND.where().eq("user", user).where()
                .eq("superUser", 1).findSet();
        Set<Roles> sel = Roles.FIND.where().eq("user", user).where()
                .eq("seller", 1).findSet();
        Set<Roles> allRoles = new HashSet<>();
        allRoles.addAll(bkSet);
        allRoles.addAll(saSet);
        allRoles.addAll(suSet);
        allRoles.addAll(sel);
        for (Roles role: allRoles) {
            salesSet.add(role.sale.saleName);
        }
        return ok(financeMain.render(salesSet, activeSalesSet, inactiveSalesSet, user));
    }

    /**
     * Gets total profit of items belonging to a sale.
     * @param saleName is name of sale that user clicks on
     * @return itemReport for that sale.
     */
    public final Result financeItem(final String saleName) {

        List<Item> itemBySale = Item.findAllSold();
        List<Item> unsoldItemBySale = Item.findAll();
        List<Item> mySale = new ArrayList<>();
        List<Item> myUnsold = new ArrayList<>();
        double total = 0;
        //return if user has no sale.
        for (Item item: itemBySale) {
            if (item.sale.toString().equals(saleName)) {
                mySale.add(item);
                total += item.price;
            }
        }
        for (Item item: unsoldItemBySale) {
            if (item.sale.toString().equals(saleName)) {
                myUnsold.add(item);
            }
        }
        if (mySale.isEmpty() && myUnsold.isEmpty()) {
            flash("error", "Sale is empty.");
            return redirect(routes.FinanceController.financeMain());
        }
        User user = User.FIND.byId(request()
                .username());
        return ok(itemReport.render(saleName, mySale, myUnsold, user, total));
    }

}
