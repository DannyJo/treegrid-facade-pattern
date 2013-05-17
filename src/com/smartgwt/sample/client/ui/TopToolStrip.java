package com.smartgwt.sample.client.ui;

import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;

/**
 * @author Daniel Johansson
 * @since 16/05/13
 */
public class TopToolStrip extends ToolStrip {

    private boolean serverMode = true;

    private final OnModeChange onModeChange;

    public TopToolStrip(final OnModeChange onModeChange) {
        this.onModeChange = onModeChange;
        setWidth("100%");
        setPadding(2);

        final ToolStripButton adminConsoleButton = new ToolStripButton("Admin Console");
        adminConsoleButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                com.smartgwtee.tools.client.SCEE.openDataSourceConsole();
            }
        });

        addButton(adminConsoleButton);

        final ToolStripButton clientServerToggleButton = new ToolStripButton("Client Mode");
        clientServerToggleButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                serverMode = !serverMode;

                if (serverMode) {
                    clientServerToggleButton.setTitle("Client Mode");
                } else {
                    clientServerToggleButton.setTitle("Server Mode");
                }

                if (onModeChange != null) {
                    onModeChange.modeChanged(serverMode ? "server" : "client");
                }
            }
        });

        addButton(clientServerToggleButton);
    }

    public static interface OnModeChange {

        public void modeChanged(final String newMode);
    }
}
