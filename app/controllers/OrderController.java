package controllers;
import com.avaje.ebean.Ebean;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

//Entities we'll use
import models.Item;
import models.User;
import models.Orders;

//view imports
import views.html.items.tag;
import views.html.order.cart;
import views.html.order.receipt;

//data structure to store
import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//magical ebeans

/**
 * Handles data related to orders.
 */
@Security.Authenticated(Secured.class)
public class OrderController extends Controller {
    @Inject
    MailerClient mailerClient;

    //allows for not having to constantly pass data in mvc
    /**
     * The list of items in a shopping cart.
     */
    public List<Item> shoppingCart = new ArrayList<>();
    /**
     * the total amount owed.
     */
    public double total;

    /**
     * Add new items to shopping cart and provides feedback.
     * @param id the id of the item to be added.
     * @return rendered page
     */
    public final Result pushToCart(final String id) {
        final Item item = Item.findById(id);
        User user = User.FIND.byId(request()
                .username());
        if (item.seller.username.equals(user.username)) {
            flash("error", "You cannot add your own item to cart.");
        } else if (shoppingCart.contains(item)) {
            flash("error", "Item is already in your cart.");
        } else {
            flash("success", "Item was added to cart");
            this.shoppingCart.add(item);
        }
        return redirect(routes.ItemController.list());
    }

    /**
     * Route to a rendered PDF file for printing.
     * @return tag of the given item
     * @throws DocumentException exception
     * @throws IOException exception
     */
    public final Result printTag(final User user, final Orders newOrder) throws
            DocumentException,
            IOException {
        String oid = newOrder.id.toString();
        String itemFileName = "tmp/Receipt #" + oid + ".pdf";
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(itemFileName));
        document.open();
        XMLWorkerHelper.getInstance().parseXHtml(writer, document,
                new ByteArrayInputStream(receipt.render(user, newOrder).body()
                        .getBytes()));
        document.close();
        sendEmail(new File(itemFileName));
        return ok(receipt.render(user, newOrder));
    }

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
     * renders shopping cart with total.
     * @return shopping cart
     */
    public final Result cart() {
        User user = User.FIND.byId(request()
                .username());
        double sum = 0;
        for (Item item: shoppingCart) {
            sum += item.price;
        }
        this.total = sum;
        return ok(cart.render(shoppingCart, user, sum));
    }

    /**
     * Renders and create receipt.
     * @return receipt page.
     */
    public final Result createOrder() throws DocumentException, IOException {
        if (shoppingCart.isEmpty()) {
            flash("error", "Cart is empty. Try adding an item!");
            return redirect(routes.ItemController.list());
        }
        User user = User.FIND.byId(request()
                .username());
        for (Item item: shoppingCart) {
            item.setSold();
            item.buyer = user;
            Ebean.update(item);
        }
        Orders newOrder =
                new Orders(user.username, shoppingCart, null, total, null);
        Ebean.save(newOrder);
        return printTag(user, newOrder);
    }


     /**
     * Clears all items from the cart.
     * @return shopping cart
     */
    public final Result clearCart() {
        User user = User.FIND.byId(request()
                .username());
        shoppingCart.clear();
        total = 0;
        return ok(cart.render(shoppingCart, user, total));
    }

    /**
     * Removes a single item from the cart.
     * @param id the id of the item to remove from the cart.
     * @return shopping cart
     */
    public final Result removeItem(final String id) {
        User user = User.FIND.byId(request()
                .username());
        final Item item = Item.findById(id);
        shoppingCart.remove(item);
        total -= item.price;
        return ok(cart.render(shoppingCart, user, total));
    }

    /**
     * Removes a single item from the cart (client-side).
     * @return new total
     */
    public final Result updateTotal() {
        String id = request().body().asText();
        final Item item = Item.findById(id);
        double amount = item.price;
        shoppingCart.remove(item);
        total -= amount;
        return ok(Double.toString(total));
    }
}
