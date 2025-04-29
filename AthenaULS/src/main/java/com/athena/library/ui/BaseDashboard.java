package com.athena.library.ui;

import com.athena.library.auth.AuthService;
import com.athena.library.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Base dashboard frame that both student and librarian dashboards will extend
 */
public abstract class BaseDashboard extends JFrame {
    protected JPanel mainPanel;
    protected JPanel sidebarPanel;
    protected JPanel contentPanel;
    protected JPanel headerPanel;
    protected JPanel footerPanel;

    protected JLabel userNameLabel;
    protected JLabel userRoleLabel;
    protected JLabel statusLabel;

    protected AuthService authService;

    /**
     * Creates the base dashboard
     * @param title Window title
     * @param userName User's name to display
     * @param userRole User's role to display
     */
    public BaseDashboard(String title, String userName, String userRole) {
        authService = AuthService.getInstance();

        // Set up the frame
        setTitle(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1200, 800));

        // Create the main layout
        initializeUI(userName, userRole);

        pack();
        setLocationRelativeTo(null); // Center on screen
        setVisible(true);
    }

    /**
     * Initializes the base UI components
     * @param userName User's name to display
     * @param userRole User's role to display
     */
    protected void initializeUI(String userName, String userRole) {
        // Main panel with border layout
        mainPanel = new JPanel(new BorderLayout(0, 0));

        // Create header panel
        headerPanel = createHeaderPanel(userName, userRole);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Create sidebar panel
        sidebarPanel = createSidebarPanel();
        mainPanel.add(sidebarPanel, BorderLayout.WEST);

        // Create content panel (empty, to be filled by subclasses)
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(UIUtils.BACKGROUND_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Create footer panel
        footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        // Add main panel to frame
        add(mainPanel);
    }

    /**
     * Creates the header panel with user info
     * @param userName User's name to display
     * @param userRole User's role to display
     * @return The header panel
     */
    protected JPanel createHeaderPanel(String userName, String userRole) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIUtils.PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(-1, 70));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Logo and title on the left
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logoPanel.setOpaque(false);

        JLabel logoLabel = new JLabel();
        logoLabel.setIcon(UIUtils.createImageIcon("/images/logo_small.png", "Athena Library"));
        logoPanel.add(logoLabel);

        JLabel titleLabel = new JLabel("Athena University Library");
        titleLabel.setFont(UIUtils.HEADER_FONT);
        titleLabel.setForeground(UIUtils.LIGHT_TEXT_COLOR);
        logoPanel.add(titleLabel);

        headerPanel.add(logoPanel, BorderLayout.WEST);

        // User info on the right
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);

        userNameLabel = new JLabel(userName);
        userNameLabel.setFont(UIUtils.SUBHEADER_FONT);
        userNameLabel.setForeground(UIUtils.LIGHT_TEXT_COLOR);
        userPanel.add(userNameLabel);

        userRoleLabel = new JLabel(" (" + userRole + ")");
        userRoleLabel.setFont(UIUtils.NORMAL_FONT);
        userRoleLabel.setForeground(UIUtils.LIGHT_TEXT_COLOR);
        userPanel.add(userRoleLabel);

        // Add logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(UIUtils.SMALL_FONT);
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });

        userPanel.add(Box.createHorizontalStrut(20));
        userPanel.add(logoutButton);

        headerPanel.add(userPanel, BorderLayout.EAST);

        return headerPanel;
    }

    /**
     * Creates the sidebar panel with navigation buttons
     * @return The sidebar panel
     */
    protected abstract JPanel createSidebarPanel();

    /**
     * Creates the footer panel with status information
     * @return The footer panel
     */
    protected JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(UIUtils.BACKGROUND_COLOR);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        footerPanel.setPreferredSize(new Dimension(-1, 30));

        // Status label on the left
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(UIUtils.SMALL_FONT);
        footerPanel.add(statusLabel, BorderLayout.WEST);

        // Version and copyright on the right
        JLabel versionLabel = new JLabel("Athena Library v1.0 © 2025");
        versionLabel.setFont(UIUtils.SMALL_FONT);
        footerPanel.add(versionLabel, BorderLayout.EAST);

        return footerPanel;
    }

    /**
     * Creates a sidebar button with standard styling
     * @param text Button text
     * @param icon Button icon
     * @param action Action to perform when clicked
     * @return The styled button
     */
    protected JButton createSidebarButton(String text, Icon icon, ActionListener action) {
        JButton button = new JButton(text, icon);
        button.setFont(UIUtils.NORMAL_FONT);
        button.setForeground(UIUtils.TEXT_COLOR);
        button.setBackground(UIUtils.BACKGROUND_COLOR);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(10);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(200, 40));

        button.addActionListener(action);

        return button;
    }

    /**
     * Updates the status message in the footer
     * @param message Status message to display
     * @param isError Whether this is an error message
     */
    protected void updateStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setForeground(isError ? UIUtils.ERROR_COLOR : UIUtils.TEXT_COLOR);
    }

    /**
     * Logs out and returns to the login screen
     */
    protected void logout() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            authService.logout();
            dispose();
            new LoginScreen();
        }
    }

    /**
     * Shows a panel in the content area
     * @param panel The panel to show
     */
    protected void showPanel(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}