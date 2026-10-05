package com.pvpactivity;

import com.pvpactivity.model.WorldActivity;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.PluginPanel;

public class PvpActivityPanel extends PluginPanel
{
    private static final String[] BRACKET_ORDER = {"3-50", "51-70", "71-90", "91-110", "111-126", "unknown"};

    private final JLabel worldValue = valueLabel();
    private final JLabel combatValue = valueLabel();
    private final JLabel wildernessValue = valueLabel();
    private final JLabel sharingValue = valueLabel();
    private final JLabel serverValue = valueLabel();
    private final JPanel activityContainer = new JPanel();

    public PvpActivityPanel()
    {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("PVP ACTIVITY", SwingConstants.CENTER);
        title.setAlignmentX(CENTER_ALIGNMENT);
        content.add(title);
        content.add(Box.createRigidArea(new Dimension(0, 10)));

        activityContainer.setLayout(new BoxLayout(activityContainer, BoxLayout.Y_AXIS));
        content.add(activityContainer);
        content.add(Box.createRigidArea(new Dimension(0, 12)));

        JLabel statusTitle = new JLabel("YOUR STATUS");
        statusTitle.setAlignmentX(LEFT_ALIGNMENT);
        content.add(statusTitle);
        content.add(Box.createRigidArea(new Dimension(0, 6)));
        content.add(statusRow("World", worldValue));
        content.add(statusRow("Combat", combatValue));
        content.add(statusRow("Wilderness", wildernessValue));
        content.add(statusRow("Sharing", sharingValue));
        content.add(statusRow("Server", serverValue));

        add(content, BorderLayout.NORTH);
        showServiceDisabled();
        updateLocalStatus(0, 0, false, false, false);
    }

    public void updateLocalStatus(int world, int combat, boolean inWilderness, boolean sharing, boolean connected)
    {
        SwingUtilities.invokeLater(() ->
        {
            worldValue.setText(world > 0 ? Integer.toString(world) : "-");
            combatValue.setText(combat > 0 ? Integer.toString(combat) : "-");
            wildernessValue.setText(inWilderness ? "YES" : "NO");
            sharingValue.setText(sharing ? "ON" : "OFF");
            serverValue.setText(!sharing ? "DISABLED" : (connected ? "CONNECTED" : "OFFLINE"));
        });
    }

    public void showServiceDisabled()
    {
        setActivityMessage("Enable the service in plugin settings to share and view activity.");
    }

    public void updateActivity(List<WorldActivity> worlds)
    {
        SwingUtilities.invokeLater(() ->
        {
            activityContainer.removeAll();

            if (worlds == null || worlds.isEmpty())
            {
                JLabel none = new JLabel("No opted-in PKers currently reported.");
                none.setAlignmentX(LEFT_ALIGNMENT);
                activityContainer.add(none);
            }
            else
            {
                for (WorldActivity world : worlds)
                {
                    JLabel header = new JLabel("World " + world.getWorld() + " — " + world.getTotal() + " PKer" + (world.getTotal() == 1 ? "" : "s"));
                    header.setAlignmentX(LEFT_ALIGNMENT);
                    activityContainer.add(header);

                    Map<String, Integer> brackets = world.getBrackets();
                    for (String bracketName : BRACKET_ORDER)
                    {
                        Integer count = brackets.get(bracketName);
                        if (count != null && count > 0)
                        {
                            String displayName = "unknown".equals(bracketName) ? "Unknown" : "Lv " + bracketName;
                            JLabel bracket = new JLabel("   " + displayName + ": " + count);
                            bracket.setAlignmentX(LEFT_ALIGNMENT);
                            activityContainer.add(bracket);
                        }
                    }
                    activityContainer.add(Box.createRigidArea(new Dimension(0, 7)));
                }
            }

            activityContainer.revalidate();
            activityContainer.repaint();
        });
    }

    private void setActivityMessage(String message)
    {
        SwingUtilities.invokeLater(() ->
        {
            activityContainer.removeAll();
            JLabel label = new JLabel("<html><body style='width:200px'>" + message + "</body></html>");
            label.setAlignmentX(LEFT_ALIGNMENT);
            activityContainer.add(label);
            activityContainer.revalidate();
            activityContainer.repaint();
        });
    }

    private static JPanel statusRow(String name, JLabel value)
    {
        JPanel row = new JPanel(new BorderLayout());
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        row.add(new JLabel(name), BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);
        return row;
    }

    private static JLabel valueLabel()
    {
        return new JLabel("-", SwingConstants.RIGHT);
    }
}
