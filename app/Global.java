import play.*;
import play.Application;
import play.GlobalSettings;
import play.api.*;
import play.libs.*;
import com.avaje.ebean.Ebean;
import models.*;
import java.util.*;

import static play.mvc.Controller.session;


/**
 * Created by Naman on 6/6/2016.
 */
public class Global extends GlobalSettings {
    /**
     * Adds the users and sales from the input file to the database if the
     * database is empty.
     * @param app the starter application
     */
    /*@Override
    public void onStart(Application app) {
        if (User.find.findRowCount() == 0) {
            Ebean.save((List) Yaml.load("testdata.yml"));
        }
    }*/

    /*@Override
    public void onStop(Application app) {
        session().clear();
    }*/
}
