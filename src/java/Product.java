
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONArray;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Eldhose Baby
 */
public class Product  extends HttpServlet{
    private  Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            System.err.println("JDBC Driver Not Found: " + ex.getMessage());
        }

        try {
            String jdbc = "jdbc:mysql://ipro.lambton.on.ca/inventory";
            conn = DriverManager.getConnection(jdbc, "products", "products");
        } catch (SQLException ex) {
            System.err.println("Failed to Connect: " + ex.getMessage());
        }
        return conn;
    }

    
   private String getResults(String query, String... arr) {
     String results=new String();
     try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            for (int i = 1; i <= arr.length; i++) {
                pstmt.setString(i, arr[i - 1]);
            }
            ResultSet res = pstmt.executeQuery();
            JSONArray productArr = new JSONArray();
            while (res.next()) {
                Map productMap = new LinkedHashMap();
                productMap.put("productID", res.getInt("productID"));
                productMap.put("name", res.getString("name"));
                productMap.put("description", res.getString("description"));
                productMap.put("quantity", res.getInt("quantity"));
                productArr.add(productMap);
            }
            results = productArr.toString();
        } catch (SQLException ex) {
            Logger.getLogger(Product.class.getName()).log(Level.SEVERE, null, ex);
        }
        return results.replace("},", "},\n");
       
     }    
   
    @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try (PrintWriter out = response.getWriter()) {
            if (!request.getParameterNames().hasMoreElements()) {
                out.println(getResults("SELECT * FROM products"));
            } else {
                int productID = Integer.parseInt(request.getParameter("productID"));
                out.println(getResults("SELECT * FROM products WHERE productID = ?", String.valueOf(productID)));
            }
        } catch (IOException ex) {
            response.setStatus(500);
            Logger.getLogger(Product.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   
    @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Set<String> keyset = request.getParameterMap().keySet();
        try (PrintWriter out = response.getWriter()) {
            Connection conn = getConnection();
            if (keyset.contains("name") && keyset.contains("description") && keyset.contains("quantity")) {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO `products`(`productID`, `name`, `description`, `quantity`) "
                        + "VALUES (null, '" + request.getParameter("name") + "', '"
                        + request.getParameter("description") + "', "
                        + request.getParameter("quantity") + ");"
                );
                try {
                    pstmt.executeUpdate();
                    request.getParameter("productID");
                    doGet(request, response);
                } catch (SQLException ex) {
                    Logger.getLogger(Product.class.getName()).log(Level.SEVERE, null, ex);
                    out.println("Data inserted Error while retriving data.");
                }
            } else {
                out.println("Error: Not enough data to input");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Product.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   
    @Override
   protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Set<String> keyset = request.getParameterMap().keySet();
        try (PrintWriter out = response.getWriter()) {
            Connection conn = getConnection();
            if (keyset.contains("productID") && keyset.contains("name") && keyset.contains("description") && keyset.contains("quantity")) {
                PreparedStatement pstmt = conn.prepareStatement("UPDATE `products` SET `name`='"
                        + request.getParameter("name") + "',`description`='"
                        + request.getParameter("description")
                        + "',`quantity`=" + request.getParameter("quantity")
                        + " WHERE `productID`=" + request.getParameter("productID"));
                try {
                    pstmt.executeUpdate();
                    doGet(request, response); //shows updated row
                } catch (SQLException ex) {
                    Logger.getLogger(Product.class.getName()).log(Level.SEVERE, null, ex);
                    out.println("Error putting values.");
                }
            } else {
                out.println("Error: Not enough data to update");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Product.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Set<String> kset = request.getParameterMap().keySet();
        try (PrintWriter out = response.getWriter()) {
            Connection conn = getConnection();
            if (kset.contains("productID")) {
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM `products` WHERE `productID`=" + request.getParameter("productID"));
                try {
                    pstmt.executeUpdate();
                } catch (SQLException ex) {
                    Logger.getLogger(Product.class.getName()).log(Level.SEVERE, null, ex);
                    out.println("Error in deleting the product.");
                }
            } else {
                out.println("Error: in data to delete");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Product.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

