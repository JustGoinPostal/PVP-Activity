package com.pvpactivity;

import com.pvpactivity.model.CombatBracket;
import com.pvpactivity.model.WorldActivity;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

public class PvpActivityPanel extends PluginPanel
{
    private static final String[] BRACKET_ORDER = {"3-50", "51-70", "71-90", "91-110", "111-126", "unknown"};

    private static final Color TEXT_PRIMARY = Color.WHITE;
    private static final Color TEXT_MUTED = ColorScheme.LIGHT_GRAY_COLOR;
    private static final Color ACCENT = ColorScheme.BRAND_ORANGE;
    private static final Color GOOD = new Color(92, 184, 92);
    private static final Color BAD = new Color(210, 90, 90);
    private static final Color CARD = ColorScheme.DARKER_GRAY_COLOR;
    private static final Color PANEL_BG = ColorScheme.DARK_GRAY_COLOR;

    private final JLabel bannerLabel = new JLabel();
    private final JPanel bannerPanel = new JPanel(new BorderLayout());

    private final JLabel worldValue = valueLabel();
    private final JLabel combatValue = valueLabel();
    private final JLabel wildernessValue = valueLabel();
    private final JLabel sharingValue = valueLabel();
    private final JLabel serverValue = valueLabel();

    private final JPanel activityContainer = new JPanel();

    public PvpActivityPanel()
    {
        setLayout(new BorderLayout());
        setBackground(PANEL_BG);
        setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(PANEL_BG);
        content.setBorder(new EmptyBorder(10, 8, 12, 8));

        content.add(buildHeader());
        content.add(Box.createRigidArea(new Dimension(0, 10)));

        configureBanner();
        content.add(bannerPanel);
        content.add(Box.createRigidArea(new Dimension(0, 14)));

        content.add(sectionTitle("WILDERNESS ACTIVITY"));
        content.add(Box.createRigidArea(new Dimension(0, 7)));

        activityContainer.setLayout(new BoxLayout(activityContainer, BoxLayout.Y_AXIS));
        activityContainer.setBackground(PANEL_BG);
        activityContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(activityContainer);

        content.add(Box.createRigidArea(new Dimension(0, 15)));
        content.add(sectionTitle("YOUR STATUS"));
        content.add(Box.createRigidArea(new Dimension(0, 7)));
        content.add(buildStatusCard());

        add(content, BorderLayout.NORTH);

        updateLocalStatus(0, 0, false, false, false);
        showServiceDisabled();
    }

    private JPanel buildHeader()
    {
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setBackground(PANEL_BG);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JPanel accentBar = new JPanel();
        accentBar.setBackground(ACCENT);
        accentBar.setPreferredSize(new Dimension(4, 32));
        header.add(accentBar, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setBackground(PANEL_BG);

        JLabel title = new JLabel("PVP Activity");
        title.setForeground(TEXT_PRIMARY);
        title.setFont(FontManager.getRunescapeBoldFont().deriveFont(Font.BOLD, 18f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Opt-in Wilderness activity");
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setFont(FontManager.getRunescapeSmallFont());
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        text.add(title);
        text.add(Box.createRigidArea(new Dimension(0, 1)));
        text.add(subtitle);
        header.add(text, BorderLayout.CENTER);

        return header;
    }

    private void configureBanner()
    {
        bannerPanel.setBackground(CARD);
        bannerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, ACCENT),
            new EmptyBorder(8, 9, 8, 8)
        ));
        bannerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bannerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        bannerLabel.setForeground(TEXT_MUTED);
        bannerLabel.setFont(FontManager.getRunescapeSmallFont());
        bannerLabel.setVerticalAlignment(SwingConstants.CENTER);
        bannerPanel.add(bannerLabel, BorderLayout.CENTER);
    }

    private JPanel buildStatusCard()
    {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(7, 10, 7, 10));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 128));

        card.add(statusRow("World", worldValue));
        card.add(separator());
        card.add(statusRow("Combat", combatValue));
        card.add(separator());
        card.add(statusRow("Wilderness", wildernessValue));
        card.add(separator());
        card.add(statusRow("Sharing", sharingValue));
        card.add(separator());
        card.add(statusRow("Server", serverValue));

        return card;
    }

    public void updateLocalStatus(int world, int combat, boolean inWilderness, boolean sharing, boolean connected)
    {
        SwingUtilities.invokeLater(() ->
        {
            worldValue.setText(world > 0 ? Integer.toString(world) : "—");

            if (combat > 0)
            {
                CombatBracket bracket = CombatBracket.fromLevel(combat);
                combatValue.setText(combat + "  ·  " + bracket.getKey());
            }
            else
            {
                combatValue.setText("—");
            }

            if (world > 0)
            {
                wildernessValue.setText(inWilderness ? "YES" : "NO");
                wildernessValue.setForeground(inWilderness ? GOOD : TEXT_PRIMARY);
            }
            else
            {
                wildernessValue.setText("—");
                wildernessValue.setForeground(TEXT_MUTED);
            }

            sharingValue.setText(sharing ? "ON" : "OFF");
            sharingValue.setForeground(sharing ? GOOD : TEXT_MUTED);

            if (!sharing)
            {
                serverValue.setText("DISABLED");
                serverValue.setForeground(TEXT_MUTED);
            }
            else if (connected)
            {
                serverValue.setText("CONNECTED");
                serverValue.setForeground(GOOD);
            }
            else
            {
                serverValue.setText("OFFLINE");
                serverValue.setForeground(BAD);
            }
        });
    }

    public void showServiceDisabled()
    {
        setBanner("Sharing is off", "Enable sharing to contribute your status and view opted-in activity.", TEXT_MUTED);
        setActivityMessage("Activity is hidden until sharing is enabled.");
    }

    public void showLoginRequired()
    {
        setBanner("Login required", "Log into Old School RuneScape to begin sharing and view activity.", ACCENT);
        setActivityMessage("Waiting for an in-game session.");
    }

    public void showConnecting()
    {
        setBanner("Connecting", "Establishing a secure connection to PVP Activity…", ACCENT);
        setActivityMessage("Loading Wilderness activity…");
    }

    public void updateActivity(List<WorldActivity> worlds)
    {
        SwingUtilities.invokeLater(() ->
        {
            setBannerNow("Live", "Sharing is active. Activity refreshes automatically.", GOOD);
            activityContainer.removeAll();

            if (worlds == null || worlds.isEmpty())
            {
                activityContainer.add(buildEmptyCard(
                    "No activity right now",
                    "No opted-in players are currently reporting from the Wilderness."
                ));
            }
            else
            {
                for (int i = 0; i < worlds.size(); i++)
                {
                    if (i > 0)
                    {
                        activityContainer.add(Box.createRigidArea(new Dimension(0, 7)));
                    }
                    activityContainer.add(buildWorldCard(worlds.get(i)));
                }
            }

            activityContainer.revalidate();
            activityContainer.repaint();
        });
    }

    private JPanel buildWorldCard(WorldActivity world)
    {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(8, 10, 8, 10));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        JLabel worldLabel = new JLabel("World " + world.getWorld());
        worldLabel.setForeground(TEXT_PRIMARY);
        worldLabel.setFont(FontManager.getRunescapeBoldFont());

        JLabel totalLabel = new JLabel(world.getTotal() + " PKer" + (world.getTotal() == 1 ? "" : "s"));
        totalLabel.setForeground(ACCENT);
        totalLabel.setFont(FontManager.getRunescapeBoldFont());

        header.add(worldLabel, BorderLayout.WEST);
        header.add(totalLabel, BorderLayout.EAST);
        card.add(header);

        Map<String, Integer> brackets = world.getBrackets();
        boolean hasBracket = false;
        for (String bracketName : BRACKET_ORDER)
        {
            Integer count = brackets == null ? null : brackets.get(bracketName);
            if (count != null && count > 0)
            {
                if (!hasBracket)
                {
                    card.add(Box.createRigidArea(new Dimension(0, 5)));
                    hasBracket = true;
                }

                String displayName = "unknown".equals(bracketName) ? "Unknown" : "Level " + bracketName;
                card.add(bracketRow(displayName, count));
            }
        }

        return card;
    }

    private JPanel bracketRow(String name, int count)
    {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(CARD);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setForeground(TEXT_MUTED);
        nameLabel.setFont(FontManager.getRunescapeSmallFont());

        JLabel countLabel = new JLabel(Integer.toString(count));
        countLabel.setForeground(TEXT_PRIMARY);
        countLabel.setFont(FontManager.getRunescapeSmallFont());

        row.add(nameLabel, BorderLayout.WEST);
        row.add(countLabel, BorderLayout.EAST);
        return row;
    }

    private JPanel buildEmptyCard(String title, String message)
    {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(10, 10, 10, 10));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setFont(FontManager.getRunescapeBoldFont());
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel messageLabel = wrappedLabel(message, 185);
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(messageLabel);
        return card;
    }

    private void setActivityMessage(String message)
    {
        SwingUtilities.invokeLater(() ->
        {
            activityContainer.removeAll();
            activityContainer.add(buildEmptyCard("Activity unavailable", message));
            activityContainer.revalidate();
            activityContainer.repaint();
        });
    }

    private void setBanner(String title, String message, Color accentColor)
    {
        SwingUtilities.invokeLater(() -> setBannerNow(title, message, accentColor));
    }

    private void setBannerNow(String title, String message, Color accentColor)
    {
        bannerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, accentColor),
            new EmptyBorder(8, 9, 8, 8)
        ));
        bannerLabel.setText(
            "<html><body style='width:178px'><b style='color:#ffffff'>" + escapeHtml(title) +
                "</b><br>" + escapeHtml(message) + "</body></html>"
        );
        bannerPanel.revalidate();
        bannerPanel.repaint();
    }

    private static JLabel sectionTitle(String text)
    {
        JLabel label = new JLabel(text);
        label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        label.setFont(FontManager.getRunescapeBoldFont().deriveFont(Font.BOLD, 12f));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private static JPanel statusRow(String name, JLabel value)
    {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(CARD);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 21));

        JLabel label = new JLabel(name);
        label.setForeground(TEXT_MUTED);
        label.setFont(FontManager.getRunescapeSmallFont());
        value.setFont(FontManager.getRunescapeBoldFont());

        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);
        return row;
    }

    private static JPanel separator()
    {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(CARD);
        wrapper.setBorder(new EmptyBorder(1, 0, 1, 0));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 3));

        JPanel line = new JPanel();
        line.setBackground(ColorScheme.DARK_GRAY_COLOR);
        line.setPreferredSize(new Dimension(1, 1));
        wrapper.add(line, BorderLayout.CENTER);
        return wrapper;
    }

    private static JLabel valueLabel()
    {
        JLabel label = new JLabel("—", SwingConstants.RIGHT);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    private static JLabel wrappedLabel(String text, int width)
    {
        JLabel label = new JLabel(
            "<html><body style='width:" + width + "px'>" + escapeHtml(text) + "</body></html>"
        );
        label.setForeground(TEXT_MUTED);
        label.setFont(FontManager.getRunescapeSmallFont());
        return label;
    }

    private static String escapeHtml(String value)
    {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;");
    }
}
