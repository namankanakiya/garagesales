package controllers;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlUpdate;
import com.avaje.ebean.SqlRow;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import models.Item;
import models.Roles;
import models.Sale;
import models.User;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.libs.Json;
import play.mvc.BodyParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import views.html.sales.newsale;
import views.html.sales.saledetails;
import views.html.sales.salelist;
import views.html.sales.tags;
import views.html.sales.editsale;
import views.html.sales.searchsales;

/**
 * Handles data related to sales.
 */
@Security.Authenticated(Secured.class)
public class SaleController extends Controller {
    /**
     * The list of all items post.
     * @return redirect to page with all the sales.
     */
    public final Result list() {
        return ok(salelist.render(Sale.findAll(), User.FIND.byId(request()
                .username())));
    }

    /**
     * Route to a rendered PDF file for printing.
     * @param saleID the ID of sale
     * @return tag of the given items
     * @throws DocumentException doc exception
     * @throws IOException ioexception
     */
    public final Result printAllTags(final String saleID) throws
            DocumentException, IOException {
        String saleItemsFileName = "tmp/Sale #" + saleID + ".pdf";
        Document document = new Document(PageSize.POSTCARD.rotate());
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(saleItemsFileName));
        document.open();
        XMLWorkerHelper.getInstance().parseXHtml(writer, document,
                new ByteArrayInputStream(tags.render(Sale.findById(saleID)
                        .items).body().getBytes()));
        document.close();
        return ok(new File(saleItemsFileName));
    }

    /**
     * When we want to add a new sale, get the current max id, and increment
     * it by one. Pass it in as a read only sale.
     * @return rendered page
     */
    public final Result newSale() {
        String sql = "select max(id + 0) from sale";
        SqlQuery query = Ebean.createSqlQuery(sql);
        List<SqlRow> row = query.findList();
        int id = 0;
        for (SqlRow r : row) {
            for (Object value : r.values()) {
                if (value == null) {
                    id = 1;
                } else {
                    id = (int) Double.parseDouble(value.toString()) + 1;
                }
            }
        }
        String idString = Integer.toString(id);
        return ok(newsale.render(Form.form(Sale.class), new Sale(), idString,
                User.FIND.byId(request().username())));
    }

    /**
     * Item details for a specific sale.
     * @param id the id of the sale to be shown
     * @return the page with details of that sale
     */
    public final Result details(final String id) {
        final Sale sale = Sale.findById(id);
        if (sale == null) {
            return notFound(String.format("Product with id %s does not exist"
                    + ".", id));
        }
        if (!sale.owner.username.equals(request().username())) {
            flash("error", "Can only edit sales you created");
            return redirect(routes.SaleController.list());
        }
        Form<Sale> filledSale = Form.form(Sale.class).fill(sale);
        return ok(saledetails.render(filledSale, sale, User.FIND.byId(request()
                .username())));
    }

    /**
     * On post from new sales page. Saves the item if everything is ok.
     * @return rendered page
     */
    public final Result save() {
        Form<Sale> doneForm = Form.form(Sale.class).bindFromRequest();
        String sql = "select max(id + 0) from sale";
        SqlQuery query = Ebean.createSqlQuery(sql);
        List<SqlRow> row = query.findList();
        int id = 0;
        for (SqlRow r : row) {
            for (Object value : r.values()) {
                if (value == null) {
                    id = 1;
                } else {
                    id = (int) Double.parseDouble(value.toString()) + 1;
                }
            }
        }
        String idString = Integer.toString(id);
        if (doneForm.hasErrors()) {
            flash("error", "Please correct the form");
            return badRequest(newsale.render(doneForm, new Sale(), idString,
                    User.FIND.byId(request().username())));
        }
        Sale sale = doneForm.get();
        User owner = User.FIND.byId(request().username());
        sale.owner  = owner;
        Roles role = new Roles(owner, sale);
        sale.rolesList.add(role);
        sale.id = Integer.toString(id);
        sale.save();
        role.setAllTrue();
        role.save();
        owner.roles.add(role);
        owner.update();
        flash("success", String.format("Successfully added sale: %s", sale));
        return redirect(routes.SaleController.list());
    }

    /**
     * Updates the current sale.
     * @param x Not needed, just here to make Play happy
     * @return rendered page
     */
    public final Result update(final String x) {
        Form<Sale> doneForm = Form.form(Sale.class).bindFromRequest();
        if (doneForm.hasErrors()) {
            flash("error", "Please correct the form");
            return badRequest(saledetails.render(doneForm, new Sale(), User.FIND
                    .byId(request().username())));
        }
        Sale sale = doneForm.get();
        sale.update();
        flash("success", String.format("Successfully updated sale: %s", sale));
        return redirect(routes.SaleController.list());
    }

    /**
     * @parem username username of current user
     * @param saleID id of sale to edit
     * @return the rendered page
     */
    public final Result editSale(final String saleID) {
        Sale sale = Sale.findById(saleID);
        List<Item> itemBySale = Item.findAllSold();
        List<Item> unsoldItemBySale = Item.findAll();
        List<Item> soldItems = new ArrayList<>();
        List<Item> unsoldItems = new ArrayList<>();
        for (Item item: itemBySale) {
            if (item.sale.toString().equals(sale.saleName)) {
                soldItems.add(item);
            }
        }
        for (Item item: unsoldItemBySale) {
            if (item.sale.toString().equals(sale.saleName)) {
                unsoldItems.add(item);
            }
        }

        User user = User.FIND.byId(request()
                .username());
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
        boolean canBK = false;
        if (allRoles.size() > 0) {
            canBK = true;
        }
        return ok(editsale.render(canBK, soldItems, unsoldItems, sale, User.FIND.byId(request().username())));
    }

    /**
     * @return whether the update is successful
     */
    @BodyParser.Of(BodyParser.Json.class)
    public final Result updateRoles() {
        JsonNode json = request().body().asJson();
        String rolesID = json.findPath("rolesID").asText();
        Roles roles = Roles.FIND.byId(rolesID);
        roles.seller = json.findPath("seller").asBoolean();
        roles.cashier = json.findPath("cashier").asBoolean();
        roles.clerk = json.findPath("clerk").asBoolean();
        roles.bookkeeper = json.findPath("bookkeeper").asBoolean();
        roles.guest = json.findPath("guest").asBoolean();
        if (roles.hasNoRoles()) {
            String username = json.findPath("username").asText();
            String saleID = json.findPath("saleID").asText();
            User user = User.findByUsername(username);
            Sale sale = Sale.findById(saleID);
            user.roles.remove(roles);
            sale.rolesList.remove(roles);
            user.update();
            sale.update();
            String updateString = "DELETE FROM roles WHERE id = "
                                + roles.getId();
            SqlUpdate updateRoles = Ebean.createSqlUpdate(updateString);
            updateRoles.execute();
            return ok("Removal successful");
        }
        roles.save();
        return ok("Update for user \"" + roles.getUser() + "\" successful");
    }

    /**
     * Add roles to a user.
     * @return a JSON containing user's roles information.
     */
    @BodyParser.Of(BodyParser.Json.class)
    public final Result addUser() {
        JsonNode receivedJson = request().body().asJson();
        String username = receivedJson.findPath("username").asText();
        ObjectNode json = Json.newObject();
        User user = User.findByUsername(username);
        if (user == null) {
            json.put("id", -1);
            return ok(json);
        }
        String saleID = receivedJson.findPath("saleID").asText();
        Sale sale = Sale.findById(saleID);
        Roles roles = new Roles(user, sale);
        user.roles.add(roles);
        sale.rolesList.add(roles);
        roles.save();
        user.update();
        sale.update();
        return ok(json);
    }

    /**
     * @return sale name change success message
     */
    @BodyParser.Of(BodyParser.Json.class)
    public final Result changeSaleName() {
        JsonNode json = request().body().asJson();
        String saleName = json.findPath("saleName").asText();
        String saleID = json.findPath("saleID").asText();
        Sale sale = Sale.findById(saleID);
        sale.saleName = saleName;
        sale.update();
        return ok("Sale name updated");
    }

    /**
     * @param saleID the id of the sale to be toggled.
     * @return goes back to the page.
     */
    public final Result toggleSaleStatus(final String saleID) {
        Sale sale = Sale.findById(saleID);
        sale.inactive = !sale.inactive;
        sale.update();
        return redirect(request().getHeader("referer"));
    }

    /**
     * searches for sales.
     * @param searchTerm the sales to search by
     * @return a page with those sales
     */
    public final Result search(final String searchTerm) {
        List<Sale> searchList = Sale.findByName(searchTerm);
        if (searchList.isEmpty()) {
            flash("error", "There does not exist such a sale: " + searchTerm);
            return redirect(routes.SaleController.list());
        }
        return ok(searchsales.render(searchList, User.FIND.byId(request()
                .username())));
    }


}
