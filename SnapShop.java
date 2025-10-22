import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import javax.imageio.ImageIO;

public class SnapShop extends JFrame {
    // Colors
    private final Color PRIMARY = new Color(59, 130, 246);
    private final Color SECONDARY = new Color(147, 197, 253);
    private final Color TEXT_DARK = new Color(17, 24, 39);
    private final Color BG_LIGHT = new Color(249, 250, 251);
    private final Color SUCCESS = new Color(34, 197, 94);

    private JPanel mainPanel;
    private CardLayout cardLayout;
    private List<Product> products = new ArrayList<>();
    private List<CartItem> cart = new ArrayList<>();
    private BufferedImage uploadedImage;
    private String overlayText = "";
    private Color textColor = Color.BLACK;
    private Color productColor = Color.WHITE;
    private String selectedSize = "M";
    private Product selectedProduct;
    private JPanel previewPanel;

    public SnapShop() {
        setTitle("SnapShop");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Sample products
        products.add(new Product("Photo Mug", "MUG", 499, "Custom mug"));
        products.add(new Product("Photo T-Shirt", "TSHIRT", 799, "Custom t-shirt"));
        products.add(new Product("Photo Frame", "FRAME", 699, "Custom frame"));

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        updateAllScreens();
        add(mainPanel);
        setVisible(true);
    }

    // --- SCREENS ---
    private JPanel createHomeScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_LIGHT);
        JLabel title = new JLabel("SnapShop", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 48));
        title.setForeground(PRIMARY);
        panel.add(title, BorderLayout.CENTER);

        JButton startBtn = createStyledButton("Browse Products", e -> showCatalog());
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(BG_LIGHT);
        btnPanel.add(startBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createCatalogScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createHeader("Catalog"), BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(0, 3, 20, 20));
        content.setBackground(BG_LIGHT);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        for (Product p : products) {
            JPanel card = new JPanel();
            card.setBackground(Color.WHITE);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)));

            JLabel name = new JLabel(p.name);
            name.setFont(new Font("Arial", Font.BOLD, 16));
            name.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(name);

            JLabel price = new JLabel("₹" + p.price);
            price.setForeground(PRIMARY);
            price.setFont(new Font("Arial", Font.BOLD, 14));
            price.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(price);

            JButton customizeBtn = createStyledButton("Customize", e -> {
                selectedProduct = p;
                showCustomize();
            });
            customizeBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            card.add(customizeBtn);

            content.add(card);
        }

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCustomizeScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createHeader("Customize Product"), BorderLayout.NORTH);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(BG_LIGHT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);

        previewPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (selectedProduct != null) {
                    BufferedImage img = createCustomPreview();
                    if (img != null) g.drawImage(img, (getWidth()-img.getWidth())/2, (getHeight()-img.getHeight())/2, null);
                }
            }
        };
        previewPanel.setPreferredSize(new Dimension(400, 400));
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.6; gbc.weighty = 1.0;
        content.add(previewPanel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.4;
        content.add(new JScrollPane(createControlPanel()), gbc);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCartScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createHeader("Shopping Cart"), BorderLayout.NORTH);

        JPanel cartContainer = new JPanel();
        cartContainer.setLayout(new BoxLayout(cartContainer, BoxLayout.Y_AXIS));
        cartContainer.setBackground(BG_LIGHT);
        cartContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        if (cart.isEmpty()) {
            JLabel empty = new JLabel("Your cart is empty", SwingConstants.CENTER);
            empty.setFont(new Font("Arial", Font.BOLD, 24));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            cartContainer.add(empty);
        } else {
            for (int i = 0; i < cart.size(); i++) {
                CartItem item = cart.get(i);
                final int index = i;
                cartContainer.add(createCartItemPanel(item, index));
            }
            int total = cart.stream().mapToInt(ci -> ci.product.price).sum();
            JLabel totalLabel = new JLabel("Total: ₹" + total);
            totalLabel.setFont(new Font("Arial", Font.BOLD, 28));
            totalLabel.setForeground(PRIMARY);
            cartContainer.add(totalLabel);

            JButton checkoutBtn = createStyledButton("Proceed to Checkout", e -> checkout());
            cartContainer.add(checkoutBtn);
        }

        JScrollPane scroll = new JScrollPane(cartContainer);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCheckoutScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createHeader("Checkout"), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_LIGHT);
        JLabel processing = new JLabel("Processing your order...");
        processing.setFont(new Font("Arial", Font.BOLD, 28));
        content.add(processing);

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setStringPainted(true);
        bar.setPreferredSize(new Dimension(400, 30));
        content.add(bar);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    // --- CONTROLS ---
    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        JButton uploadBtn = createStyledButton("Upload Image", e -> uploadImage());
        panel.add(uploadBtn);

        JTextField textField = new JTextField();
        textField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                overlayText = textField.getText();
                refreshPreview();
            }
        });
        panel.add(textField);

        panel.add(new JLabel("Text Color:"));
        JPanel colorPanel = new JPanel();
        for (Color c : new Color[]{Color.BLACK, Color.RED, Color.WHITE, PRIMARY}) {
            JButton btn = createColorButton(c, col -> { textColor = col; refreshPreview(); });
            colorPanel.add(btn);
        }
        panel.add(colorPanel);

        panel.add(new JLabel("Product Color:"));
        JPanel prodPanel = new JPanel();
        for (Color c : new Color[]{Color.WHITE, Color.BLACK, Color.RED, PRIMARY}) {
            JButton btn = createColorButton(c, col -> { productColor = col; refreshPreview(); });
            prodPanel.add(btn);
        }
        panel.add(prodPanel);

        panel.add(new JLabel("Size:"));
        JPanel sizePanel = new JPanel();
        for (String s : new String[]{"S","M","L","XL"}) {
            JRadioButton rb = new JRadioButton(s);
            rb.setSelected(s.equals("M"));
            rb.addActionListener(e -> selectedSize = s);
            sizePanel.add(rb);
        }
        panel.add(sizePanel);

        JButton addToCartBtn = createStyledButton("Add to Cart", e -> addToCart());
        panel.add(addToCartBtn);

        return panel;
    }

    // --- ACTIONS ---
    private void refreshPreview() { if (previewPanel != null) previewPanel.repaint(); }
    private void addToCart() {
        if (selectedProduct == null) return;
        BufferedImage preview = createCustomPreview();
        cart.add(new CartItem(selectedProduct, preview, selectedSize, productColor));
        uploadedImage = null; overlayText = "";
        updateAllScreens();
        showCart();
    }
    private void checkout() {
        if (cart.isEmpty()) return;
        cardLayout.show(mainPanel, "CHECKOUT");
    }

    private void uploadImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg","png","jpeg"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try { uploadedImage = ImageIO.read(chooser.getSelectedFile()); refreshPreview(); } 
            catch(Exception ex){ ex.printStackTrace(); }
        }
    }

    // --- UI HELPERS ---
    private JButton createColorButton(Color c, Consumer<Color> listener) {
        JButton btn = new JButton();
        btn.setBackground(c);
        btn.setPreferredSize(new Dimension(30,30));
        btn.addActionListener(e -> listener.accept(c));
        return btn;
    }

    private JButton createStyledButton(String text, ActionListener l) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.addActionListener(l);
        return btn;
    }

    private JPanel createHeader(String title) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        JLabel label = new JLabel(title);
        label.setFont(new Font("Arial", Font.BOLD, 26));
        header.add(label, BorderLayout.WEST);
        JButton back = new JButton("← Back"); back.addActionListener(e -> showHome());
        header.add(back, BorderLayout.EAST);
        return header;
    }

    private JPanel createCartItemPanel(CartItem item, int index) {
        JPanel p = new JPanel(new BorderLayout());
        JLabel name = new JLabel(item.product.name + " (" + item.size + ") - ₹" + item.product.price);
        p.add(name, BorderLayout.CENTER);
        JButton remove = new JButton("Remove");
        remove.addActionListener(e -> { cart.remove(index); updateAllScreens(); showCart(); });
        p.add(remove, BorderLayout.EAST);
        return p;
    }

    private BufferedImage createCustomPreview() {
        BufferedImage img = new BufferedImage(300,300,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(productColor); g.fillRect(0,0,300,300);
        if(uploadedImage!=null) g.drawImage(uploadedImage,50,50,200,200,null);
        g.setColor(textColor);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString(overlayText, 20, 280);
        g.dispose();
        return img;
    }

    private void showHome() { cardLayout.show(mainPanel, "HOME"); }
    private void showCatalog() { cardLayout.show(mainPanel, "CATALOG"); }
    private void showCustomize() { cardLayout.show(mainPanel, "CUSTOMIZE"); refreshPreview(); }
    private void showCart() { cardLayout.show(mainPanel, "CART"); }

    private void updateAllScreens() {
        mainPanel.removeAll();
        mainPanel.add(createHomeScreen(), "HOME");
        mainPanel.add(createCatalogScreen(), "CATALOG");
        mainPanel.add(createCustomizeScreen(), "CUSTOMIZE");
        mainPanel.add(createCartScreen(), "CART");
        mainPanel.add(createCheckoutScreen(), "CHECKOUT");
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // --- MODELS ---
    static class Product { String name,type; int price; Product(String n,String t,int p,String d){name=n;type=t;price=p;} }
    static class CartItem { Product product; BufferedImage preview; String size; Color color; CartItem(Product p,BufferedImage img,String s,Color c){product=p;preview=img;size=s;color=c;} }

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new SnapShop()); }
}