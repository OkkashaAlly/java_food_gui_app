import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableModel;

public class App {
    // GUI components
    JTextField jtf_productname, jtf_productprice;
    JTextArea jta_productdesc;
    JButton jb_add, jb_delete, jb_update, jb_search;
    JTable jt;
    JFrame frame;
    JLabel lbl_productname, lbl_productprice, lbl_productdesc;

    // Data structures
    ArrayList<Product> productlist;
    Product product;
    String header[] = new String[] {
            "ID",
            "Product Name",
            "Product Price",
            "Product Description"
    };
    DefaultTableModel dtm = new DefaultTableModel(0, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    // Database connection
    static Connection conn;
    ResultSet rs;

    public static void main(String[] args) throws Exception {
        // Initialize the SQLite database connection
        String url = "jdbc:sqlite:product.db";
        conn = DriverManager.getConnection(url);

        // Create an instance of the application
        App app = new App();

        // Set up the main interface, check tables, and load initial data
        app.mainInterface();
        app.checkTables();
        app.loadData();
    }

    // Method to set up the main graphical interface
    private void mainInterface() {
        // GUI setup code
        frame = new JFrame();
        lbl_productname = new JLabel();
        lbl_productname.setText("Product Name");
        lbl_productname.setBounds(10, 10, 100, 50);
        frame.add(lbl_productname);

        jtf_productname = new JTextField();
        jtf_productname.setBounds(100, 25, 250, 25);
        frame.add(jtf_productname);

        lbl_productprice = new JLabel();
        lbl_productprice.setText("Price");
        lbl_productprice.setBounds(10, 35, 100, 50);
        frame.add(lbl_productprice);

        jtf_productprice = new JTextField();
        jtf_productprice.setBounds(100, 50, 100, 25);
        frame.add(jtf_productprice);

        lbl_productdesc = new JLabel();
        lbl_productdesc.setText("Description");
        lbl_productdesc.setBounds(10, 55, 100, 50);
        frame.add(lbl_productdesc);

        jta_productdesc = new JTextArea();
        jta_productdesc.setBounds(100, 75, 250, 50);
        jta_productdesc.setBorder(new JTextField().getBorder());
        frame.add(jta_productdesc);

        jb_add = new JButton();
        jb_add.setText("Add");
        jb_add.setBounds(10, 140, 100, 25);
        frame.add(jb_add);
        jb_add.addActionListener(addProductListener);

        jb_delete = new JButton();
        jb_delete.setText("Delete");
        jb_delete.setBounds(120, 140, 100, 25);
        frame.add(jb_delete);
        jb_delete.addActionListener(delProductListener);

        jb_update = new JButton();
        jb_update.setText("Update");
        jb_update.setBounds(230, 140, 100, 25);
        frame.add(jb_update);
        jb_update.addActionListener(updateProductListener);

        jb_search = new JButton();
        jb_search.setText("Search");
        jb_search.setBounds(340, 140, 100, 25);
        frame.add(jb_search);
        jb_search.addActionListener(searchProductListener);

        jt = new JTable();
        jt.setModel(dtm);
        dtm.setColumnIdentifiers(header);
        JScrollPane sp = new JScrollPane(jt);
        sp.setBounds(10, 170, 430, 600);
        frame.add(sp);
        jt.addMouseListener(mouseListener);

        frame.setSize(480, 800);
        frame.setLayout(null); // using no layout managers
        frame.setVisible(true); // making the frame visible
    }

    // ActionListener for adding a new product entry
    ActionListener addProductListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Extract data from GUI components
            String productname = jtf_productname.getText().toString();
            String productprice = jtf_productprice.getText().toString();
            String productdesc = jta_productdesc.getText().toString();

            // Validate input
            if (productname.isEmpty() || productprice.isEmpty() || productdesc.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter product info");
                jtf_productname.requestFocus();
            } else {
                // Confirm with the user before inserting the data
                int result = JOptionPane.showConfirmDialog(frame, "Insert this product data " + productname + "?", "Insert",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                // Execute the SQL query to insert data into the database
                if (result == JOptionPane.YES_OPTION) {
                    try {
                        Statement stmt = conn.createStatement();
                        stmt.executeUpdate("insert into tbl_products (`product_name`, `product_price`, `product_desc`) VALUES ('" +
                                productname + "','" + productprice + "','" + productdesc + "')");

                        // Reload data to update the table
                        loadData();
                    } catch (Exception err) {
                        System.out.println(err);
                    }
                }
            }
        }
    };

    // Method to check and create tables if they don't exist
    private void checkTables() {
        System.out.println("Check table");
        String sql = "CREATE TABLE IF NOT EXISTS tbl_products (" +
                "	id integer PRIMARY KEY AUTOINCREMENT," +
                "	product_name text NOT NULL," +
                "	product_price real NOT NULL," +
                "	product_desc text NOT NULL" +
                ");";

        // Execute the SQL query to create the table if it doesn't exist
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        } catch (Exception err) {
            System.out.println(err);
        }
    }

    // Method to load data from the database and populate the table
    private void loadData() throws SQLException {
        System.out.println("Load data");
        productlist = new ArrayList<>();
        Statement stmt = conn.createStatement();
        rs = stmt.executeQuery("select * from tbl_products");
        productlist.clear();
        while (rs.next()) {
            productlist.add(new Product(rs.getInt(1), rs.getString(2), rs.getFloat(3), rs.getString(4)));
        }
        dtm.setRowCount(0); // reset data model
        for (int i = 0; i < productlist.size(); i++) {
            Object[] objs = {
                    productlist.get(i).productid,
                    productlist.get(i).productname,
                    productlist.get(i).productprice,
                    productlist.get(i).productdesc,
            };
            dtm.addRow(objs);
        }
    }

    // Mouse listener to capture mouse clicks on the table
    MouseInputAdapter mouseListener = new MouseInputAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            int row = jt.rowAtPoint(evt.getPoint());
            int col = jt.columnAtPoint(evt.getPoint());
            if (row >= 0 && col >= 0) {
                jtf_productname.setText(jt.getValueAt(row, 1).toString());
                jtf_productprice.setText(jt.getValueAt(row, 2).toString());
                jta_productdesc.setText(jt.getValueAt(row, 3).toString());
                product = new Product(Integer.parseInt(jt.getValueAt(row, 0).toString()), jt.getValueAt(row, 1).toString(),
                        Double.parseDouble(jt.getValueAt(row, 0).toString()), jt.getValueAt(row, 0).toString());
            }
        }
    };

    // ActionListener for updating a product entry
    ActionListener updateProductListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String productname = jtf_productname.getText().toString();
            String productprice = jtf_productprice.getText().toString();
            String productdesc = jta_productdesc.getText().toString();
            if (product == null) {
                System.out.println("Null");
            } else {

                int result = JOptionPane.showConfirmDialog(frame, "Update " + product.productname + "?", "Swing Tester",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (result == JOptionPane.YES_OPTION) {
                    try {
                        System.out.println("Product " + product.productname);
                        Statement stmt = conn.createStatement();
                        stmt.executeUpdate("update tbl_products set product_name = '" + productname + "', product_price = " +
                                productprice + ", product_desc='" + productdesc + "' where id =" + product.productid + "");
                        loadData();
                    } catch (Exception err) {
                        System.out.println(err);
                    }
                }

            }
        }
    };

        // ActionListener for deleting a product entry
    ActionListener delProductListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (product == null) {
                System.out.println("Null");
            } else {

                int result = JOptionPane.showConfirmDialog(frame, "Delete " + product.productname + "?", "Swing Tester",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (result == JOptionPane.YES_OPTION) {
                    try {
                        System.out.println("Product " + product.productname);
                        Statement stmt = conn.createStatement();
                        stmt.executeUpdate("delete from tbl_products where id = '" + product.productid + "'");
                        loadData();
                    } catch (Exception err) {
                        System.out.println(err);
                    }
                }

            }

        }
    };

        // ActionListener for searching for a product entry
    ActionListener searchProductListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {

            String search = JOptionPane.showInputDialog("Enter product name");
            System.out.println(search);

            productlist = new ArrayList<>();
            try {

                Statement stmt = conn.createStatement();
                rs = stmt.executeQuery("select * from tbl_products where product_name LIKE '%" + search + "%'");
                productlist.clear();
                while (rs.next()) {
                    productlist.add(new Product(rs.getInt(1), rs.getString(2), rs.getFloat(3), rs.getString(4)));
                }
                dtm.setRowCount(0); // reset data model
                for (int i = 0; i < productlist.size(); i++) {
                    Object[] objs = {
                            productlist.get(i).productid,
                            productlist.get(i).productname,
                            productlist.get(i).productprice,
                            productlist.get(i).productdesc,
                    };
                    dtm.addRow(objs);
                }

            } catch (Exception err) {
                System.out.println(err);
            }
        }

    };

}