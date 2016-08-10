package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;

import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;

import models.Item;
import models.Roles;
import models.Sale;
import models.User;
import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;
import views.html.items.details;
import views.html.items.list;
import views.html.items.newitem;
import views.html.items.search;
import views.html.items.tag;
import scala.Tuple2;

import play.data.Form;
import play.libs.Scala;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.BodyParser;
import play.mvc.Security;

import org.apache.commons.io.IOUtils;

import javax.inject.Inject;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static play.mvc.Http.MultipartFormData;

/**
 * Will handle methods relating to Item.
 */
@Security.Authenticated(Secured.class)
public class ItemController extends Controller {
    @Inject
    MailerClient mailerClient;

    public final void sendEmail(final File file) {
        User curUser = User.FIND.byId(request().username());
        String emailUser = curUser.email;
        String cid = "1234";
        Email email = new Email()
                .setSubject("Garage Sale 5 Item Tag")
                .setFrom("G5 FROM <highfivegaragesale@gmail.com >")
                .addTo(request().username() + " TO <" + emailUser + ">")
                // sends text, HTML or both...
                .setBodyText("A text message")
                .setBodyHtml("<html><body><p>An <b>html</b> message with cid <img src=\"cid:" + cid + "\"></p></body></html>");
        if (file != null) {
            String s = "Attachment.pdf";
            email.addAttachment(s, file);
        }
        mailerClient.send(email);
    }
    /**
     * All of the items, passed into list.scala.html.
     * @return rendered page
     */
    public final Result list() {
        List<Item> items = Item.findAll();
        Map<Item, String> itemsMap = new HashMap<>();
        //writes image files to a tmp folder for retrieval
        for (Item item: items) {
            if (item.picture != null) {
                byte[] pictureByte = item.picture;
                FileOutputStream fout = null;
                try {
                    fout = new FileOutputStream("public/images/" + item.id + ".jpg");
                    itemsMap.put(item, "images/" + item.id + ".jpg");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    fout.write(pictureByte);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                itemsMap.put(item, null);
            }
        }
        return ok(list.render(itemsMap, User.FIND.byId(request()
                .username())));
    }

    /**
     * @return whether bidding is successful and success/error message
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result bid() {
        JsonNode receivedJson = request().body().asJson();
        ObjectNode json = Json.newObject();
        String itemID = receivedJson.findPath("itemID").asText();
        double amount = receivedJson.findPath("amount").asDouble();
        Item item = Item.findById(itemID);
        User user = User.findByUsername(request().username());
        if (item.seller.username == user.username) {
            json.put("success", "no");
            json.put("message", "You cannot bid on your own item");
        }
        else if (amount <= item.bidAmount) {
            json.put("success", "no");
            json.put("message", "Amount must be greater than "
                    + item.bidAmount);
        }
        else {
            /*item.setBidAmount(amount);
            item.setBidUser(user);*/
            item.bidAmount = amount;
            item.bidUser = user;
            item.update();
            json.put("success", "yes");
            json.put("message", "Bid placed successfully");
        }
        return ok(json);
    }

    /**
     * Route to a rendered PDF file for printing.
     * @param itemID the ID of item
     * @return tag of the given item
     * @throws DocumentException exception
     * @throws IOException exception
     */
    public final Result printTag(final String itemID) throws DocumentException,
            IOException {
        Item item = Item.findById(itemID);
        String itemFileName = "tmp/Item #" + itemID + ".pdf";
        Document document = new Document(PageSize.POSTCARD.rotate());
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(itemFileName));
        document.open();
        XMLWorkerHelper.getInstance().parseXHtml(writer, document,
                new ByteArrayInputStream(tag.render(item).body().getBytes()));
        document.close();
        sendEmail(new File(itemFileName));
        return ok(new File(itemFileName));
    }

    /**
     * When we want to add a new item, get the current max id, and increment
     * it by one. Pass it in as a read only item.
     * @return rendered page
     */
    public final Result newItem() {
        User user = User.FIND.byId(request().username());
        String sql = "select max(id + 0) from item";
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
        List<Sale> saleList = Sale.findAll();
        List<Tuple2<String, String>> values = new ArrayList<>();
        if (user.superUser) {
            for (Sale s : saleList) {
                values.add(Scala.Tuple(s.id, s.saleName));
            }
        } else {
            for (Sale s : saleList) {
                Roles role = Roles.FIND.where().eq("user",
                        User.findByUsername(request().username()))
                        .eq("sale", s).findUnique();
                if (role != null && (role.seller)) {
                    values.add(Scala.Tuple(s.id, s.saleName));
                }
            }
        }
        return ok(newitem.render(Form.form(Item.class), new Item(), idString,
                User.FIND.byId(request().username()), Scala.toSeq(values)));
    }

    /**
     * The details page on an already existing item, referenced by it's id.
     * @param id the id to look the item up by
     * @return rendered page
     */
    public final Result details(final String id) {
        final Item item = Item.findById(id);
        if (item == null) {
            return notFound(String.format("Product with id %s does not exist"
                    + ".", id));
        }
        Form<Item> filledItem = Form.form(Item.class).fill(item);
        return ok(details.render(filledItem, item, User.FIND.byId(request()
                .username())));
    }

    /**
     * On post from new item page. Saves the item if everything is ok.
     * @return rendered page
     * @throws IOException exception
     * @throws
     */
    public final Result save() throws IOException {
        Form<Item> doneForm = Form.form(Item.class).bindFromRequest();
        if (doneForm.hasErrors()) {
            flash("error", "Please supply all the required fields");
            return newItem();
        }
        Item item = doneForm.get();
        MultipartFormData<File> body = request().body()
                .asMultipartFormData();
        MultipartFormData.FilePart<File> picture = body.getFile("picture");
        if (picture != null) {
            File file = picture.getFile();
            try (InputStream file1 = new FileInputStream(file)) {
                item.picture = IOUtils.toByteArray(file1);
            } catch (IOException e) {
                return internalServerError("Error reading file upload");
            }
        }
        User seller = User.FIND.byId(request().username());
        if (seller != null) {
            item.seller = seller;
        }
        item.listDate = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String sql = "select max(id + 0) from item";
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
        item.id = Integer.toString(id);
        String sName = item.saleName;
        if (sName == null) {
            flash("error", "Need to add to a sale! Contact SA to become a "
                    + "seller");
            return redirect(routes.ItemController.list());
        }
        if (item.biddable) {
            item.bidAmount = item.bidFraction*item.price;
        }
        Sale itemSale = Sale.findById(sName);
        item.sale = itemSale;
        item.save();
        itemSale.items.add(item);
        itemSale.update();
        flash("success", String.format("Successfully added item %s", item));
        return redirect(routes.ItemController.list());
    }

    /**
     * Returns the picture of an item.
     * @param id The id of the item that we need the picture for
     * @return The picture of the item
     */
    public final Result picture(final String id) {
        final Item item = Item.findById(id);
        if (item == null) {
            return notFound();
        }
        return ok(item.picture);
    }

    /**
     * Updates the current item.
     * @param x Not needed, just here to make Play happy
     * @return rendered page
     * @throws IOException exception.
     */
    public final Result update(final String x) throws IOException {
        Form<Item> doneForm = Form.form(Item.class).bindFromRequest();
        if (doneForm.hasErrors()) {
            flash("error", "Please correct the form");
            return badRequest(details.render(doneForm, new Item(), User.FIND
                    .byId(request().username())));
        }
        Item item = doneForm.get();
        MultipartFormData<File> body = request().body()
                .asMultipartFormData();
        MultipartFormData.FilePart<File> picture = body.getFile("picture");
        if (picture != null) {
            File file = picture.getFile();
            try (InputStream file1 = new FileInputStream(file)) {
                item.picture = IOUtils.toByteArray(file1);
            } catch (IOException e) {
                return internalServerError("Error reading file upload");
            }
        }
        String id = item.id;
        Item toCheck = Item.findById(id);
        String uname = toCheck.seller.username;
        String curname = request().username();
        if (!uname.equals(curname)) {
            flash("error", "Can only edit items you are selling!");
            return redirect(routes.ItemController.list());
        }
        /* TODO ajax/javascript to disable submit button on website*/
        item.update();
        flash("success", String.format("Successfully updated item %s", item));
        return redirect(routes.ItemController.list());
    }

    /**
     * Finds a list of items to be presented based on a search term.
     * @param searchTerm the items that we want to be included
     * @return the list of items in a searchList render page
     */
    public final Result search(final String searchTerm) {
        List<Item> searchList = Item.findByName(searchTerm);
        if (searchList.isEmpty()) {
            flash("error", "There does not exist such an item");
            return redirect(routes.ItemController.list());
        }
        return ok(search.render(searchList, User.FIND.byId(
                request().username())));

    }
}
