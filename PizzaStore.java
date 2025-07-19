/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;


/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class PizzaStore {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of PizzaStore
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public PizzaStore(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end PizzaStore

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      // int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      // boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            PizzaStore.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      PizzaStore esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the PizzaStore object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new PizzaStore (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Profile");
                System.out.println("2. Update Profile");
                System.out.println("3. View Menu");
                System.out.println("4. Place Order"); //make sure user specifies which store
                System.out.println("5. View Full Order ID History");
                System.out.println("6. View Past 5 Order IDs");
                System.out.println("7. View Order Information"); //user should specify orderID and then be able to see detailed information about the order
                System.out.println("8. View Stores"); 

                //**the following functionalities should only be able to be used by drivers & managers**
                System.out.println("9. Update Order Status");

                //**the following functionalities should only be able to be used by managers**
                System.out.println("10. Update Menu");
                System.out.println("11. Update User");

                System.out.println(".........................");
                System.out.println("20. Log out\n");
                switch (readChoice()){
                   case 1: viewProfile(esql, authorisedUser); break;
                   case 2: updateProfile(esql, authorisedUser); break;
                   case 3: viewMenu(esql); break;
                   case 4: placeOrder(esql, authorisedUser); break;
                   case 5: viewAllOrders(esql, authorisedUser); break;
                   case 6: viewRecentOrders(esql, authorisedUser); break;
                   case 7: viewOrderInfo(esql, authorisedUser); break;
                   case 8: viewStores(esql); break;
                   case 9: updateOrderStatus(esql, authorisedUser); break;
                   case 10: updateMenu(esql, authorisedUser); break;
                   case 11: updateUser(esql, authorisedUser); break;



                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      System.out.print('\n');
      return input;
   }//end readChoice

   /*
   * Creates a new user
   */
   public static void CreateUser(PizzaStore esql) {
      try {
         System.out.print("Enter login: ");
         String login = in.readLine().trim();
         System.out.print("Enter password: ");
         String password = in.readLine().trim();
         System.out.print("Enter phone number (format xxx-xxx-xxxx): ");
         String phone = in.readLine().trim();

         if (login.isEmpty() || password.isEmpty() || phone.isEmpty()) {
               System.out.println("All fields are required. Exiting create user.");
               return;
         }

         if (!isValidPhoneNumber(phone)) {
            System.out.println("Invalid phone number format. Use format xxx-xxx-xxxx. Exiting create user.");
            return;
         }

         String query = String.format(
            "INSERT INTO Users (login, password, role, favoriteItems, phoneNum) VALUES ('%s', '%s', 'customer', NULL, '%s');",
            login, password, phone);
         esql.executeUpdate(query); // create user in db
         System.out.println("User created successfully!");
      }catch (Exception e) {
         System.err.println("Error while creating user: " + e.getMessage());
      }
   }// end CreateUser

   public static boolean isValidPhoneNumber(String phone) {
      if (phone.length() != 12) {
         return false;
      }

      if (phone.charAt(3) != '-' || phone.charAt(7) != '-') { // characters at specific positions are '-'
         return false;
      }
      
      for (int i = 0; i < phone.length(); i++) { // all other characters are digits
         if (i == 3 || i == 7) { // skip the '-'s
            continue;
         }
         if (!Character.isDigit(phone.charAt(i))) {
            return false;
         }
      }
      return true;
   }

   /*
   * Check log in credentials for an existing user
   * @return User login or null if the user does not exist
   */
   // USER (login!!,password,role,favoriteItems,phoneNum)
   public static String LogIn(PizzaStore esql) {
      try {
         System.out.print("Enter login: ");
         String login = in.readLine().trim();
         System.out.print("Enter password: ");
         String password = in.readLine().trim();

         String query = String.format(
            "SELECT * FROM Users WHERE login = '%s' AND password = '%s';",
            login, password);
         int userCount = esql.executeQuery(query);

         if (userCount != 0) {
            System.out.println("Login successful!");
            return login;
         } else {
            System.out.println("Invalid login or password.");
            return null;
         }
      }catch (Exception e) {
         System.err.println("Error during login: " + e.getMessage());
         return null;
      }
   }// end

   // Rest of the functions definition go in here

   public static void viewProfile (PizzaStore esql, String login) {
      try {
         String query = String.format(
            "SELECT favoriteItems, phoneNum FROM Users WHERE login = '%s';",
            login);
         List<List<String>> result = esql.executeQueryAndReturnResult(query);
            
         List<String> userProfile = result.get(0);
         String currentFavoriteItem = userProfile.get(0) != null ? userProfile.get(0) : "No favorite item set";
         String currentPhone = userProfile.get(1);

         System.out.println("Your profile information:");
         System.out.println("Favorite Item: " + currentFavoriteItem);
         System.out.println("Phone Number: " + currentPhone + '\n');
      }
      catch (Exception e) {
         System.err.println("Error while viewing profile: " + e.getMessage());
      }
   }

   public static void updateProfile(PizzaStore esql, String login) {
      try {
         System.out.println("Please choose what you would like to update:");
         System.out.println("1. Update Favorite Item");
         System.out.println("2. Update Phone Number");
         System.out.println("3. Update Password");
         System.out.println("4. Exit update profile");

         switch(readChoice()) {
            case 1:
               System.out.print("Enter new Favorite Item: ");
               String newFavoriteItem = in.readLine().trim();
               String updateFavoriteItemQuery = String.format(
                  "UPDATE Users SET favoriteItems = '%s' WHERE login = '%s';",
                  newFavoriteItem, login);
               esql.executeUpdate(updateFavoriteItemQuery);
               System.out.println("Favorite Item updated successfully!");
               break;

            case 2:
               System.out.print("Enter new Phone Number (format xxx-xxx-xxxx): ");
               String newPhone = in.readLine().trim();

               if (!isValidPhoneNumber(newPhone)) {
                  System.out.println("Invalid phone number format. Use format xxx-xxx-xxxx.\nExiting update profile.");
                  return;
               }

               String updatePhoneQuery = String.format(
                  "UPDATE Users SET phoneNum = '%s' WHERE login = '%s';",
                  newPhone, login);
               esql.executeUpdate(updatePhoneQuery);
               System.out.println("Phone Number updated successfully!");
               break;

            case 3:
               System.out.print("Enter new Password: ");
               String newPassword = in.readLine().trim();
               String updatePasswordQuery = String.format(
                  "UPDATE Users SET password = '%s' WHERE login = '%s';", 
                  newPassword, login);
               esql.executeUpdate(updatePasswordQuery);
               System.out.println("Password updated successfully!");
               break;

            case 4:
               System.out.println("Exiting update profile.");
               return;

            default:
               System.out.println("Invalid choice. Exiting update profile.");
               return;
         }
      } // end of try
      catch (Exception e) {
         System.err.println("Error while updating profile: " + e.getMessage());
      }
   }

   // ITEM (itemName!!,"ingredients",typeOfItem,price,"description")
   public static void viewMenu(PizzaStore esql) {
      try {
         System.out.println("How would you like the menu to be displayed?");
         System.out.println("1. Unfiltered display of all items");
         System.out.println("2. Filter display based on type");
         System.out.println("3. Filter display based on price (highest->lowest)");
         System.out.println("4. Filter display based on price (lowest->highest)");
         System.out.println("5. Filter display based on both type and price (highest->lowest)");
         System.out.println("6. Filter display based on both type and price (lowest->highest)");
         System.out.println("7.. Exit view menu");
         
         String typeInput = "";
         String[] types;

         switch(readChoice()) {
            case 1: // BASIC IMPLEMENTATION
               String itemMenu = String.format(
                  "SELECT * FROM Items;");
               List<List<String>> result = esql.executeQueryAndReturnResult(itemMenu);
         
               if (result.isEmpty()) {
                  System.out.println("The menu is currently empty.");
                  return;
               }
               else {
                  System.out.println("Full menu:");
                  System.out.println("*******************************************************");

                  for (List<String> row : result) {
                     String itemName = row.get(0);
                     String ingredients = row.get(1);
                     String typeOfItem = row.get(2);
                     String price = row.get(3);
                     String description = row.get(4);
                     System.out.println("(Item type: " + typeOfItem + ") " + itemName + " - $" + price);
                     System.out.println("\tDescription: " + description);
                     System.out.println("\t\tIngredients: " + ingredients);
                  }
                  System.out.println("*******************************************************");
               } // end case 1 basic implementation
               break;

            case 2: // filter based only on type
               System.out.print("Enter food type to filter by (e.g., entree, drinks, sides). Separate with commas for multiple types: ");
               typeInput = in.readLine().trim();
               types = typeInput.split(",\\s*");

               for (int i = 0; i < types.length; i++) {
                  types[i] = types[i].toLowerCase();
                  if (!(types[i].equalsIgnoreCase("entree") || types[i].equalsIgnoreCase("drinks") || types[i].equalsIgnoreCase("sides"))) {
                     System.out.println("Invalid type entered: " + types[i] + ". Only 'entree', 'drinks', or 'sides' are allowed. Exiting view menu.");
                     return;
                  }
               }

               String typeQuery = String.format(
                  "SELECT * FROM Items WHERE typeOfItem IN ('%s');",
                  String.join("', '", types));
               List<List<String>> typeResult = esql.executeQueryAndReturnResult(typeQuery);

               if(typeResult.isEmpty()) {
                  System.out.println("No items found for the specified type.");
               }
               else {
                  System.out.println("Menu filtered by: Item type (" + String.join(", ", types) + "):");
                  System.out.println("*******************************************************");

                  for (List<String> row : typeResult) {
                     String itemName = row.get(0);
                     String ingredients = row.get(1);
                     String typeOfItem = row.get(2);
                     String price = row.get(3);
                     String description = row.get(4);
                     System.out.println("(Item type: " + typeOfItem + ") " + itemName + " - $" + price);
                     System.out.println("\t" + description);
                     System.out.println("\t\t" + ingredients);
                  }
                  System.out.println("*******************************************************");
               } // end case 2 type filter
               break;

            case 3: // filter based only on price highest->lowest
               System.out.print("Enter the maximum price to filter by: ");
               double maxPrice = Double.parseDouble(in.readLine().trim());

               String priceQuery = String.format(
                  "SELECT * FROM Items WHERE price <= %d ORDER BY price DESC;"
                  , maxPrice);
               List<List<String>> priceResult = esql.executeQueryAndReturnResult(priceQuery);

               if (priceResult.isEmpty()) {
                  System.out.println("No items found for the specified price range.");
               }
               else {
                  System.out.println("Menu filtered by: Price <= $" + maxPrice + ":");
                  System.out.println("*******************************************************");

                  for (List<String> row : priceResult) {
                     String itemName = row.get(0);
                     String ingredients = row.get(1);
                     String typeOfItem = row.get(2);
                     String price = row.get(3);
                     String description = row.get(4);
                     System.out.println("(Item type: " + typeOfItem + ") " + itemName + " - $" + price);
                     System.out.println("\t" + description);
                     System.out.println("\t\t" + ingredients);
                  }
                  System.out.println("*******************************************************");
               }
               break;

            case 4: // filter based on price lowest->highest
               System.out.print("Enter the maximum price to filter by: ");
               double maxPriceTwo = Double.parseDouble(in.readLine().trim());

               String priceQueryTwo = String.format(
                  "SELECT * FROM Items WHERE price <= %d ORDER BY price ASC;"
                  , maxPriceTwo);
               List<List<String>> priceResultTwo = esql.executeQueryAndReturnResult(priceQueryTwo);

               if (priceResultTwo.isEmpty()) {
                  System.out.println("No items found for the specified price range.");
               }
               else {
                  System.out.println("Menu filtered by: Price <= $" + maxPriceTwo + ":");
                  System.out.println("*******************************************************");

                  for (List<String> row : priceResultTwo) {
                     String itemName = row.get(0);
                     String ingredients = row.get(1);
                     String typeOfItem = row.get(2);
                     String price = row.get(3);
                     String description = row.get(4);
                     System.out.println("(Item type: " + typeOfItem + ") " + itemName + " - $" + price);
                     System.out.println("\t" + description);
                     System.out.println("\t\t" + ingredients);
                  }
                  System.out.println("*******************************************************");
               }
               break;

            case 5: // filter based on both type and price highest->lowest
               System.out.print("Enter item type to filter by (e.g., entree, drinks, sides). Separate with commas for multiple types: ");
               typeInput = in.readLine().trim();
               types = typeInput.split(",\\s*");

               for (int i = 0; i < types.length; i++) {
                  types[i] = types[i].toLowerCase();
                  if (!(types[i].equals("entree") || types[i].equals("drinks") || types[i].equals("sides"))) {
                      System.out.println("Invalid type entered: " + types[i] + ". Only 'entree', 'drinks', or 'sides' are allowed. Exiting view menu.");
                      return;
                  }
               }

               System.out.print("Enter the maximum price to filter by: ");
               maxPrice = Double.parseDouble(in.readLine().trim());

               String bothQuery = String.format(
                  "SELECT * FROM Items WHERE typeOfItem IN ('%s') AND price <= %d ORDER BY price DESC;", 
                  String.join("', '", types), maxPrice);
               List<List<String>> bothResult = esql.executeQueryAndReturnResult(bothQuery);

               if (bothResult.isEmpty()) {
                  System.out.println("No items found for the specified filters.");
               }
               else {
                  System.out.println("Menu filtered by: Item Type (" + String.join(", ", types) + "), Price <= $" + maxPrice + ":");
                  System.out.println("*******************************************************");
                  for (List<String> row : bothResult) {
                     String itemName = row.get(0);
                     String ingredients = row.get(1);
                     String typeOfItem = row.get(2);
                     String price = row.get(3);
                     String description = row.get(4);
                     System.out.println("(Item type: " + typeOfItem + ") " + itemName + " - $" + price);
                     System.out.println("\t" + description);
                     System.out.println("\t\t" + ingredients);
                  }
                  System.out.println("*******************************************************");
               }
               break;

            case 6: // filter based on both type and price highest->lowest
            System.out.print("Enter item type to filter by (e.g., entree, drinks, sides). Separate with commas for multiple types: ");
            typeInput = in.readLine().trim();
            types = typeInput.split(",\\s*");

            for (int i = 0; i < types.length; i++) {
               types[i] = types[i].toLowerCase();
               if (!(types[i].equals("entree") || types[i].equals("drinks") || types[i].equals("sides"))) {
                   System.out.println("Invalid type entered: " + types[i] + ". Only 'entree', 'drinks', or 'sides' are allowed. Exiting view menu.");
                   return;
               }
            }

            System.out.print("Enter the maximum price to filter by: ");
            maxPrice = Double.parseDouble(in.readLine().trim());

            String bothQueryTwo = String.format(
               "SELECT * FROM Items WHERE typeOfItem IN ('%s') AND price <= %d ORDER BY price ASC;", 
               String.join("', '", types), maxPrice);
            List<List<String>> bothResultTwo = esql.executeQueryAndReturnResult(bothQueryTwo);

            if (bothResultTwo.isEmpty()) {
               System.out.println("No items found for the specified filters.");
            }
            else {
               System.out.println("Menu filtered by: Item Type (" + String.join(", ", types) + "), Price <= $" + maxPrice + ":");
               System.out.println("*******************************************************");
               for (List<String> row : bothResultTwo) {
                  String itemName = row.get(0);
                  String ingredients = row.get(1);
                  String typeOfItem = row.get(2);
                  String price = row.get(3);
                  String description = row.get(4);
                  System.out.println("(Item type: " + typeOfItem + ") " + itemName + " - $" + price);
                  System.out.println("\t" + description);
                  System.out.println("\t\t" + ingredients);
               }
               System.out.println("*******************************************************");
            }
            break;

            case 7:
               System.out.println("Exiting view menu.");
               return;

            default:
               System.out.println("Invalid choice. Please try again.");
               break;
         }
      }catch (Exception e) {
         System.err.println("Error while viewing menu: " + e.getMessage());
      }
   }

   // STORE (storeID!!,address,city,state,isOpen,reviewScore)
   public static void placeOrder(PizzaStore esql, String login) {
      try {
         String storeQuery = "SELECT storeID, address, city, state FROM Store WHERE isOpen = true;";
         List<List<String>> stores = esql.executeQueryAndReturnResult(storeQuery);
          
         if (stores.isEmpty()) {
            System.out.println("Sorry, there are no open stores available to place an order.");
            return;
         }
          
         System.out.println("Available stores:");
         for (int i = 0; i < stores.size(); i++) {
            List<String> store = stores.get(i);
            String storeID = store.get(0);
            String address = store.get(1);
            String city = store.get(2);
            String state = store.get(3);
            System.out.println((i + 1) + ". " + address + ", " + city + ", " + state + " (Store ID: " + storeID + ")");
         }
  
         System.out.print("Enter the number of the store you want to order from: ");
         int storeChoice = Integer.parseInt(in.readLine().trim());

         if (!(storeChoice >= 1 && storeChoice <= stores.size())) {
            System.out.println("Invalid store selection.");
            return;
         }
  
         String selectedStoreID = stores.get(storeChoice - 1).get(0);
         System.out.println("You have selected store " + selectedStoreID + '.');
         System.out.println("Loading menu");
         viewMenu(esql);

         double totalOrderPrice = 0.0;
         String itemName = "";
         int itemQuantity = 0;
         List<List<String>> orderItems = new ArrayList<>();
  
         do {
            System.out.print("Enter the item name of the food, or type 'done' to finish ordering: ");
            itemName = in.readLine().trim();
  
            if (itemName.equalsIgnoreCase("done")) {
               break;
            }
  
            String itemQuery = String.format(
               "SELECT price FROM Items WHERE itemName LIKE '%s';", 
               itemName);
            List<List<String>> itemResults = esql.executeQueryAndReturnResult(itemQuery);
  
            if (itemResults.isEmpty()) {
               System.out.println("Item not found. Please try again.");
               continue;
            }
  
            double itemPrice = Double.parseDouble(itemResults.get(0).get(0));
            System.out.println("You have selected " + itemName + " - $" + itemPrice);
            System.out.print("Enter the quantity you want to order: ");
            itemQuantity = Integer.parseInt(in.readLine().trim());
  
            if (itemQuantity <= 0) {
               System.out.println("Invalid quantity.");
               continue;
            }
  
            double itemTotalPrice = itemPrice * itemQuantity;
            totalOrderPrice += itemTotalPrice;
  
            System.out.println("Added " + itemQuantity + " of " + itemName + " to your order.");
            System.out.println("Total so far: $" + totalOrderPrice);
  
            orderItems.add(Arrays.asList(itemName, String.valueOf(itemQuantity)));
  
         }while (true);
  
         if (totalOrderPrice > 0) {
            System.out.println("Total order price: $" + totalOrderPrice);
            System.out.print("Enter 'yes' to confirm your order: ");
            String confirmation = in.readLine().trim();
              
            if (confirmation.equalsIgnoreCase("yes")) {
               String lastOrderQuery = "SELECT MAX(orderID) FROM FoodOrder;";
               List<List<String>> lastOrderResult = esql.executeQueryAndReturnResult(lastOrderQuery);
  
               int orderID = 10000; // default starting orderID is used if there is no order in FoodOrder
               if (lastOrderResult.get(0).get(0) != null) {
                  orderID = Integer.parseInt(lastOrderResult.get(0).get(0)) + 1;
               }
  
               String orderTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
               String orderStatus = "incomplete";
  
               String insertOrderQuery = String.format(
                  "INSERT INTO FoodOrder (orderID, login, storeID, totalPrice, orderTimestamp, orderStatus) VALUES (%d, '%s', '%s', %d, '%s', '%s');",
                  orderID, login, selectedStoreID, totalOrderPrice, orderTimestamp, orderStatus);
               esql.executeUpdate(insertOrderQuery);
  
               for (List<String> orderItem : orderItems) {
                  String itemNameInOrder = orderItem.get(0);
                  int quantityInOrder = Integer.parseInt(orderItem.get(1));
  
                  String insertItemsInOrderQuery = String.format(
                     "INSERT INTO ItemsInOrder (orderID, itemName, quantity) VALUES (%d, '%s', %d);",
                     orderID, itemNameInOrder, quantityInOrder);
                  esql.executeUpdate(insertItemsInOrderQuery);
               }
               System.out.println("Order confirmed! Thank you for your purchase.");
            }else {
               System.out.println("Order cancelled.");
               orderItems.clear();
            }
         }else {
            System.out.println("No items selected. Exiting order process.");
         }
      }catch (Exception e) {
         System.err.println("Error while placing order: " + e.getMessage());
      }
  }
  
  
   public static void viewAllOrders(PizzaStore esql, String login) {
      try {
         String roleQuery = String.format(
            "SELECT role FROM Users WHERE login = '%s';",
            login);
         List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);

         if (roleResult.isEmpty()) {
            System.out.println("User not found.");
            return;
         }

         String userRole = roleResult.get(0).get(0);
         String orderQuery = "";
         
         if (userRole.equalsIgnoreCase("customer")) {
            System.out.println(login + "'s orderID history:\n");
            System.out.println("Order ID\t| Store ID\t| Total Price\tOrder Timestamp\t| Order Status");
            System.out.println("*******************************************************");
            orderQuery = String.format(
               "SELECT orderID, storeID, totalPrice, orderTimestamp, orderStatus FROM FoodOrder WHERE login = '%s';",
               login);
         }
         else if (userRole.equalsIgnoreCase("manager") || userRole.equalsIgnoreCase("driver")) {
            System.out.print("Enter the login of the user to view their orderID history: ");
            String viewLogin = in.readLine().trim();

            String query = String.format(
               "SELECT login FROM Users WHERE login = '%s';",
               viewLogin);
            int userCount = esql.executeQuery(query);

            if (userCount == 0) {
               System.out.println("Invalid login. Exiting view orderID history.");
               return;
            }

            System.out.println(viewLogin + "'s' orderID history:\n");
            System.out.println("Order ID\t| Store ID\t| Total Price\tOrder Timestamp\t| Order Status");
            System.out.println("*******************************************************");
            orderQuery = String.format(
               "SELECT orderID, storeID, totalPrice, orderTimestamp, orderStatus FROM FoodOrder WHERE login = '%s';",
               viewLogin);
         }
         else {
            System.out.println("Invalid role assignment. Exiting view all orders.");
            return;
         }

         List<List<String>> orderResults = esql.executeQueryAndReturnResult(orderQuery);

         if (orderResults.isEmpty()) {
            System.out.println("No orders found.");
            return;
         }

         String orderID = ""; String storeID = ""; String totalPrice = ""; String orderTimestamp = ""; String orderStatus = "";
         
         for (List<String> order : orderResults) {
            orderID = order.get(0);
            storeID = order.get(1);
            totalPrice = order.get(2);
            orderTimestamp = order.get(3);
            orderStatus = order.get(4);

            System.out.println(String.format(
               "%s\t| %s\t| $%s\t| %s\t| %s\n",
               orderID, storeID, totalPrice, orderTimestamp, orderStatus));
         }
         System.out.println("*******************************************************");
      }catch (Exception e) {
         System.err.println("Error while viewing all orders: " + e.getMessage());
      }
   }

   public static void viewRecentOrders(PizzaStore esql, String login) {
      try {
         String roleQuery = String.format(
            "SELECT role FROM Users WHERE login = '%s';",
            login);
         List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);

         if (roleResult.isEmpty()) {
            System.out.println("User not found.");
            return;
         }

         String userRole = roleResult.get(0).get(0);
         String orderQuery = "";
         
         if (userRole.equalsIgnoreCase("customer")) {
            System.out.println(login + "'s 5 most recent orderIDs:\n");
            System.out.println("Order ID\t| Store ID\t| Total Price\tOrder Timestamp\t| Order Status");
            System.out.println("*******************************************************");
            orderQuery = String.format(
               "SELECT orderID, storeID, totalPrice, orderTimestamp, orderStatus FROM FoodOrder WHERE login = '%s' ORDER BY orderTimestamp DESC LIMIT 5;",
               login);
         }
         else if (userRole.equalsIgnoreCase("manager") || userRole.equalsIgnoreCase("driver")) {
            System.out.print("Enter the login of the user to view their recent 5 orderID history: ");
            String viewLogin = in.readLine().trim();

            String query = String.format(
               "SELECT login FROM Users WHERE login = '%s';",
               viewLogin);
            int userCount = esql.executeQuery(query);

            if (userCount == 0) {
               System.out.println("Invalid login. Exiting view orderID history.");
               return;
            }

            System.out.println(viewLogin + "'s' 5 most recent orderIDs:\n");
            System.out.println("Order ID\t\t| Store ID\t| Total Price\tOrder Timestamp\t| Order Status");
            System.out.println("*******************************************************");
            orderQuery = String.format(
               "SELECT orderID, storeID, totalPrice, orderTimestamp, orderStatus FROM FoodOrder WHERE login = '%s' ORDER BY orderTimestamp DESC LIMIT 5;",
               viewLogin);
         }
         else {
            System.out.println("Invalid role assignment. Exiting view recent orders.");
            return;
         }

         List<List<String>> orderResults = esql.executeQueryAndReturnResult(orderQuery);

         if (orderResults.isEmpty()) {
            System.out.println("No orders found.");
            return;
         }

         String orderID = ""; String storeID = ""; String totalPrice = ""; String orderTimestamp = ""; String orderStatus = "";
         
         for (List<String> order : orderResults) {
            orderID = order.get(0);
            storeID = order.get(1);
            totalPrice = order.get(2);
            orderTimestamp = order.get(3);
            orderStatus = order.get(4);

            System.out.println(String.format(
               "%s\t| %s\t| $%s\t| %s\t| %s\n",
               orderID, storeID, totalPrice, orderTimestamp, orderStatus));
         }
         System.out.println("*******************************************************");
      }catch (Exception e) {
         System.err.println("Error while viewing recent order: " + e.getMessage());
      }
   }

   public static void viewOrderInfo(PizzaStore esql, String login) {
      try {
         String roleQuery = String.format(
            "SELECT role FROM Users WHERE login = '%s';",
            login);
         List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);

         if (roleResult.isEmpty()) {
            System.out.println("User not found.");
            return;
         }

         String userRole = roleResult.get(0).get(0);
         String orderQuery = "";
         String orderID = "";
         int queryNum = 0;

         if (userRole.equalsIgnoreCase("customer")) {
            System.out.print("Enter the orderID to view its details: ");
            orderID = in.readLine().trim();

            orderQuery = String.format(
               "SELECT orderTimestamp, totalPrice, orderStatus FROM FoodOrder WHERE login = '%s' AND orderID = '%s';",
               login, orderID);

            queryNum = esql.executeQuery(orderQuery);
            
            if (queryNum == 0) {
               System.out.println("Invalid role access or orderID. Exiting view order info.");
               return;
            }
         }
         else if (userRole.equalsIgnoreCase("manager") || userRole.equalsIgnoreCase("driver")) {
            System.out.print("Enter the orderID to view its details: ");
            orderID = in.readLine().trim();

            orderQuery = String.format(
               "SELECT orderTimestamp, totalPrice, orderStatus FROM FoodOrder WHERE orderID = '%s';",
               orderID);
            
            queryNum = esql.executeQuery(orderQuery);

            if (queryNum == 0) {
               System.out.println("Invalid orderID. Exiting view order info.");
               return;
            }
         }
         else {
            System.out.println("Invalid role assignment. Exiting view order info.");
            return;
         }

         List<List<String>> orderResults = esql.executeQueryAndReturnResult(orderQuery);

         // orderID,login,storeID,totalPrice,"orderTimestamp",orderStatus
         String orderTimestamp = orderResults.get(0).get(0);
         String totalPrice = orderResults.get(0).get(1);
         String orderStatus = orderResults.get(0).get(2);

         System.out.println("Order Timestamp: " + orderTimestamp);
         System.out.println("Total Price: $" + totalPrice);
         System.out.println("Order Status: " + orderStatus); 
         System.out.println("*******************************************************");

         String itemsQuery = String.format(
            "SELECT itemName, quantity FROM ItemsInOrder WHERE orderID = '%s';",
            orderID);
         List<List<String>> itemsResults = esql.executeQueryAndReturnResult(itemsQuery);

         System.out.println("Items in this order:\n");
         System.out.println("Item Name\t| Quantity");
         System.out.println("*******************************************************");

         for (List<String> item : itemsResults) {
            String itemName = item.get(0);
            String quantity = item.get(1);
            System.out.println(itemName + "\t| " + quantity);
         }
         System.out.println("*******************************************************");
      }catch (Exception e) {
         System.err.println("Error while viewing order info: " + e.getMessage());
      }
   }

   // storeID,address,city,state,isOpen,reviewScore
   public static void viewStores(PizzaStore esql) { // CHECK IF GOOD
      try {
         String storeQuery = "SELECT storeID, address, city, state, isOpen, reviewScore FROM Store;";
         List<List<String>> storeResults = esql.executeQueryAndReturnResult(storeQuery);

         if (storeResults.isEmpty()) {
            System.out.println("No stores found.");
            return;
         }

         System.out.println("StoreID\t| Address\t| City\t| State\t| Open\t| Review Score");
         System.out.println("*******************************************************");

         String storeID = ""; String address = ""; String city = ""; String state = ""; String isOpen = ""; String reviewScore = "";
         
         for (List<String> store: storeResults) {
            storeID = store.get(0);
            address = store.get(1);
            city = store.get(2);
            state = store.get(3);
            isOpen = store.get(4);
            reviewScore = store.get(5);

            System.out.println(String.format(
               "%s\t| %s\t| %s\t| %s\t| %s\t| %s\n",
               storeID, address, city, state, isOpen, reviewScore));
         }
      }catch (Exception e) {
         System.err.println("Error while viewing store: " + e.getMessage());
      }
   }

   public static void updateOrderStatus(PizzaStore esql, String login) { // drivers and managers only
      try {
         String roleQuery = String.format(
            "SELECT role FROM Users WHERE login = '%s';",
            login);
         List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);

         String userRole = roleResult.get(0).get(0);

         if (!userRole.equalsIgnoreCase("manager") || !userRole.equalsIgnoreCase("driver")) {
            System.out.println("Invalid role access. Exiting update user.");
            return;
         }

         System.out.print("Enter the orderID of the order whose status you wish to update: ");
         String orderID = in.readLine().trim();

         // orderID,login,storeID,totalPrice,"orderTimestamp",orderStatus
         String orderQuery = String.format(
            "SELECT orderStatus FROM FoodOrder WHERE orderID = '%s';",
            orderID);
         List<List<String>> orderResult = esql.executeQueryAndReturnResult(orderQuery);

         if (orderResult.isEmpty()) {
            System.out.println("Invalid orderID. Exiting update order status.");
            return;
         }

         String orderStatus = orderResult.get(0).get(0);

         if (orderStatus.equalsIgnoreCase("incomplete")) {
            orderStatus = "complete";
         }
         else { // if orderStatus = "complete"
            orderStatus = "incomplete";
         }

         String changeQuery = String.format(
            "UPDATE FoodOrder SET orderStatus = '%s' WHERE orderID = '%s';",
            orderStatus, orderID);
         esql.executeUpdate(changeQuery);

         System.out.println("OrderID " + orderID + "'s status has been changed to " + orderStatus + '.');
      }catch (Exception e) {
         System.err.println("Error while updating order status: " + e.getMessage());
      }
   }

   public static void updateMenu(PizzaStore esql, String login) { // manager only
      try {
         String roleQuery = String.format(
            "SELECT role FROM Users WHERE login = '%s';",
            login);
         List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);

         String userRole = roleResult.get(0).get(0);

         if (!userRole.equalsIgnoreCase("manager")) {
            System.out.println("Invalid role access. Exiting update user.");
            return;
         }

         System.out.println("How would you like to update the menu?");
         System.out.println("1. Add an item");
         System.out.println("2. Remove an item");
         System.out.println("3. Update an item's name");
         System.out.println("4. Update an item's ingredients list");
         System.out.println("5. Update an item's type");
         System.out.println("6. Update an item's price");
         System.out.println("7. Update an item's description");
         System.out.println("8. Exit update order status");

         // itemName!!,"ingredients",typeOfItem,price,"description"
         switch(readChoice()) {
            case 1: // add
               System.out.println("Enter the name of the new item: ");
               String itemName = in.readLine().trim();

               String nameQuery = String.format(
                  "SELECT COUNT(*) FROM Items WHERE itemName = '%s';",
                  itemName);
               List<List<String>> nameResult = esql.executeQueryAndReturnResult(nameQuery);

               if (Integer.parseInt(nameResult.get(0).get(0)) != 0) { // item name already exists
                  System.out.println("Invalid item name. This item name already exists! Exiting update menu.");
                  return;
               }

               System.out.print("Enter the ingredients for item " + itemName + ": ");
               String ingredients = in.readLine().trim();
               System.out.print("Enter the item type for item " + itemName + ": ");
               String typeOfItem = in.readLine().trim();
               System.out.print("Enter the price for item " + itemName + ": ");
               String price = in.readLine().trim();
               System.out.print("Enter the description for item " + itemName + ": ");
               String description = in.readLine().trim();

               String createQuery = String.format(
                  "INSERT INTO Items (itemName, ingredients, typeOfItem, price, description) VALUES ('%s', '%s', '%s', %d, '%s');",
                  itemName, ingredients, typeOfItem, price, description);
               esql.executeUpdate(createQuery);

               System.out.println("Successfully added new item " + itemName + " to the menu!!");
               break;

            case 2: // remove
               System.out.println("Enter the name of the item to be removed.");
               String remove = in.readLine().trim();

               // itemName!!,"ingredients",typeOfItem,price,"description"
               String query = String.format(
                  "SELECT * FROM Items WHERE itemName = '%s';",
                  remove);
               List<List<String>> removeResult = esql.executeQueryAndReturnResult(query);

               if (removeResult.isEmpty()) {
                  System.out.println("There is no item " + remove + " in the menu. Exiting update menu.");
                  return;
               }

               String removeQuery = String.format(
                  "DELETE FROM Items WHERE itemName = '%s';",
                  remove);
               esql.executeUpdate(removeQuery);

               System.out.println("Item " + remove + " successfully removed from the menu.");
               break;

            case 3: // update name
               System.out.println("Enter the name of the item to be updated: ");
               String updateName = in.readLine().trim();

               String updateNameQuery = String.format(
                  "SELECT * FROM Items WHERE itemName = '%s';",
                  updateName);
               List<List<String>> updateNameResult = esql.executeQueryAndReturnResult(updateNameQuery);

               if (updateNameResult.isEmpty()) {
                  System.out.println("Item " + updateName + " does not exist. Exiting update menu.");
                  return;
               }

               System.out.print("Enter the new item name: ");
               String newName = in.readLine().trim();

               String checkNameQuery = String.format(
                  "SELECT COUNT(*) FROM Items WHERE itemName = '%s';",
                  newName);
               List<List<String>> checkResult = esql.executeQueryAndReturnResult(checkNameQuery);

               if (Integer.parseInt(checkResult.get(0).get(0)) > 0) {
                  System.out.println("Invalid item name. There already exists an item with item name " + newName + ". Exiting update menu.");
                  return;
               }

               String updateNewNameQuery = String.format(
                  "UPDATE Items SET itemName = '%s' WHERE itemName = '%s';",
                  newName, updateName);
               esql.executeUpdate(updateNewNameQuery);

               System.out.println("Successfully updated the name of item " + updateName + '!');
               break;
            
            case 4: // update ingredients
               System.out.print("Enter the name of the item to update ingredients: ");
               String updateIngredientsName = in.readLine().trim();

               String updateIngredientsQuery = String.format(
                  "SELECT * FROM Items WHERE itemName = '%s';",
                  updateIngredientsName);
               List<List<String>> updateIngredientsResult = esql.executeQueryAndReturnResult(updateIngredientsQuery);

               if (updateIngredientsResult.isEmpty()) {
                  System.out.println("Item " + updateIngredientsName + " does not exist. Exiting update menu.");
                  return;
               }

               System.out.print("Enter the new ingredients for item " + updateIngredientsName + ": ");
               String newIngredients = in.readLine().trim();

               String updateIngredientsItemQuery = String.format(
                  "UPDATE Items SET ingredients = '%s' WHERE itemName = '%s';",
                  newIngredients, updateIngredientsName);
               esql.executeUpdate(updateIngredientsItemQuery);

               System.out.println("Successfully updated the ingredients of item " + updateIngredientsName + '!');
               break;

            case 5: // update item type
               System.out.print("Enter the name of the item to update type: ");
               String updateTypeName = in.readLine().trim();

               String updateTypeQuery = String.format(
                  "SELECT * FROM Items WHERE itemName = '%s';",
                  updateTypeName);
               List<List<String>> updateTypeResult = esql.executeQueryAndReturnResult(updateTypeQuery);

               if (updateTypeResult.isEmpty()) {
                  System.out.println("Item " + updateTypeName + " does not exist. Exiting update menu.");
                  return;
               }

               System.out.print("Enter the new item type for item " + updateTypeName + ": ");
               String newType = in.readLine().trim();

               String updateItemTypeQuery = String.format(
                  "UPDATE Items SET typeOfItem = '%s' WHERE itemName = '%s';",
                  newType, updateTypeName);
               esql.executeUpdate(updateItemTypeQuery);

               System.out.println("Successfully updated the type of item " + updateTypeName + '!');
               break;

            case 6: // update price
               System.out.print("Enter the name of the item to update price: ");
               String updatePriceName = in.readLine().trim();

               String updatePriceQuery = String.format(
                  "SELECT * FROM Items WHERE itemName = '%s';",
                  updatePriceName);
               List<List<String>> updatePriceResult = esql.executeQueryAndReturnResult(updatePriceQuery);

               if (updatePriceResult.isEmpty()) {
                  System.out.println("Item " + updatePriceName + " does not exist. Exiting update menu.");
                  return;
               }

               System.out.print("Enter the new price for item " + updatePriceName + ": ");
               String newPrice = in.readLine().trim();

               try {
                  double checkPrice = Double.parseDouble(newPrice);
               }catch (NumberFormatException e) {
                  System.out.println("Invalid price format. Exiting update menu.");
                  return;
               }

               String updateItemPriceQuery = String.format(
                  "UPDATE Items SET price = %d WHERE itemName = '%s';",
                  Double.parseDouble(newPrice), updatePriceName);
               esql.executeUpdate(updateItemPriceQuery);

               System.out.println("Successfully updated the price of item " + updatePriceName + '!');
               break;

            case 7: // update description
               System.out.print("Enter the name of the item to update description: ");
               String updateDescriptionName = in.readLine().trim();

               String updateDescriptionQuery = String.format( 
                  "SELECT * FROM Items WHERE itemName = '%s';",
                  updateDescriptionName);
               List<List<String>> updateDescriptionResult = esql.executeQueryAndReturnResult(updateDescriptionQuery);

               if (updateDescriptionResult.isEmpty()) {
                  System.out.println("Item " + updateDescriptionName + " does not exist. Exitting update menu.");
                  return;
               }

               System.out.print("Enter the new description for item " + updateDescriptionName + ": ");
               String newDescription = in.readLine().trim();

               String updateItemDescriptionQuery = String.format(
                  "UPDATE Items SET description = '%s' WHERE itemName = '%s';",
                  newDescription, updateDescriptionName);
               esql.executeUpdate(updateItemDescriptionQuery);

               System.out.println("Successfully updated the description of item " + updateDescriptionName + "!");
               break;

            case 8: // exit
               System.out.println("Exiting update menu.");
               break;

            default:
               System.out.println("Invalid choice. Exiting update profile.");
               return;
         }
      }catch (Exception e) {
         System.err.println("Error while updating menu: " + e.getMessage());
      }
   }

   public static void updateUser(PizzaStore esql, String login) { // DONE
      try {
         String roleQuery = String.format(
            "SELECT role FROM Users WHERE login = '%s';",
            login);
         List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);

         String userRole = roleResult.get(0).get(0);

         if (!userRole.equalsIgnoreCase("manager")) {
            System.out.println("Invalid role access. Exiting update user.");
            return;
         }

         System.out.print("Hello manager " + login + ", which account would you like to update? ");
         String mLogin = in.readLine().trim();
         
         String checkQuery = String.format(
            "SELECT * FROM Users WHERE login = '%s';",
            mLogin);
         int userCount = esql.executeQuery(checkQuery);

         if (userCount != 0) {
            System.out.println("Account found. Continuing with update profile.");
         } else {
            System.out.println("Invalid login. Exiting update profile.");
            return;
         }

         if (!login.equalsIgnoreCase(mLogin)) { // manager updating a different person's account -> can only update other user's login or role
            String mQuery = String.format(
               "SELECT login, role FROM Users WHERE login = '%s';",
               mLogin);
            List<List<String>> mResult = esql.executeQueryAndReturnResult(mQuery);
            System.out.println("Please choose what you would like to update:");
            System.out.println("1. Update Login");
            System.out.println("2. Update Role");
            System.out.println("3. Exit update profile");

            switch(readChoice()) {
               case 1:
                  System.out.print("Enter new Login: ");
                  String newLogin = in.readLine().trim();

                  String checkLoginQuery = String.format(
                     "SELECT COUNT(*) FROM Users WHERE login = '%s';",
                     newLogin);
                  List<List<String>> checkResult = esql.executeQueryAndReturnResult(checkLoginQuery);

                  if (Integer.parseInt(checkResult.get(0).get(0)) > 0) {
                     System.out.println("The new login already exists. Logins must be unique. Exiting update profile.");
                     return;
                  }

                  String updateLogin = String.format(
                     "UPDATE Users SET login = '%s' WHERE login = '%s';",
                     newLogin, mLogin);
                  esql.executeUpdate(updateLogin);
                  System.out.println("Login updated successfully!");
                  return;

               case 2:
                  System.out.print("Enter new Role: ");
                  String newRole = in.readLine().trim();

                  if (!newRole.equalsIgnoreCase("customer") || !newRole.equalsIgnoreCase("driver") || !newRole.equalsIgnoreCase("manager")) { // check for valid role assignment
                     System.out.println("Invalid role assignment. Role has not been changed. Exiting update user.");
                     return;
                  }

                  if (newRole.equalsIgnoreCase(mResult.get(0).get(1))) { // trying to assign to the same role assignment that they already had
                     System.out.println(String.format(
                        "'%s' has already been assigned the '%s' role. Role has not been changed. Exiting update profile.",
                        mLogin, newRole));
                     return;
                  }

                  String updateRole = String.format(
                     "UPDATE Users SET role '%s' WHERE login = '%s';",
                     newRole, mLogin);
                  esql.executeUpdate(updateRole);
                  System.out.println("Role updated successfully!");
                  return;

               case 3:
                  System.out.println("Exiting update profile.");
                  return;

               default:
                  System.out.println("Invalid choice. Exiting update profile.");
                  return;
            }
         }
         else { // manager updating their own profile
            updateProfile(esql, login);
         }
      }catch (Exception e) {
         System.err.println("Error while updating user: " + e.getMessage());
      }
   }
}//end PizzaStore