package com.smartgwt.sample.client.ui;

import com.smartgwt.client.data.*;
import com.smartgwt.client.types.DSOperationType;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
import com.smartgwt.client.widgets.grid.events.SelectionEvent;
import com.smartgwt.client.widgets.layout.HStack;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.menu.IMenuButton;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;
import com.smartgwt.sample.client.TreeGridFacadeClientSideDataSource;

import java.util.Map;

public class DefaultPage extends VLayout implements TopToolStrip.OnModeChange {

    private TeamsAndPlayersTree teamsAndPlayersTree = new TeamsAndPlayersTree();
    private TopToolStrip topToolStrip = new TopToolStrip(this);
    private Button editButton = new Button("Edit");
    private Button deleteButton = new Button("Delete");

    public DefaultPage() {
        setWidth100();
        setHeight100();
        setMembersMargin(5);
        setLayoutMargin(5);

        addMember(topToolStrip);
        addMember(teamsAndPlayersTree);

        final HStack buttonHStack = new HStack();
        buttonHStack.setLayoutMargin(0);
        buttonHStack.setMembersMargin(5);

        final Menu menu = new Menu();
        menu.setShowShadow(false);
        menu.setShadowDepth(0);
        menu.setShowIcons(false);

        final MenuItem addTeamMenuItem = new MenuItem("Team");
        addTeamMenuItem.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {
            @Override
            public void onClick(final MenuItemClickEvent event) {
                new AddEditModalDialog("Add Team", "teams", null, new DSCallback() {
                    @Override
                    public void execute(DSResponse response, Object rawData, DSRequest request) {
                        teamsAndPlayersTree.updateCache(null, DSOperationType.UPDATE);
                    }
                }).show();
            }
        });

        final MenuItem addPlayerMenuItem = new MenuItem("Player");
        addPlayerMenuItem.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {
            @Override
            public void onClick(final MenuItemClickEvent event) {
                new AddEditModalDialog("Add Player", "players", null, new DSCallback() {
                    @Override
                    public void execute(DSResponse response, Object rawData, DSRequest request) {
                        teamsAndPlayersTree.updateCache(null, DSOperationType.UPDATE);
                        teamsAndPlayersTree.updateCache("teams:" + String.valueOf(request.getAttributeAsMap("data").get("teamId")), DSOperationType.UPDATE);
                    }
                }).show();
            }
        });

        menu.setItems(addTeamMenuItem, addPlayerMenuItem);
        final IMenuButton menuButton = new IMenuButton("Add", menu);
        menuButton.setWidth(100);
        buttonHStack.addMember(menuButton);

        editButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (teamsAndPlayersTree.getSelectedRecord() == null) {
                    deleteButton.disable();
                    return;
                }

                final String dataSourceName = teamsAndPlayersTree.getSelectedRecord().getAttributeAsString("dataSource");
                final Map data = teamsAndPlayersTree.getSelectedRecord().getAttributeAsMap("data");

                // Create new dialog box and on data save, update the trees datasource cache.
                new AddEditModalDialog("Edit", dataSourceName, data, new DSCallback() {
                    @Override
                    public void execute(DSResponse response, Object rawData, DSRequest request) {
                        final String teamId = String.valueOf(request.getAttributeAsMap("data").get("teamId"));
                        final String parentId = teamId != null && !"null".equalsIgnoreCase(teamId) ? "teams:" + teamId : null;

                        teamsAndPlayersTree.updateCache(null, DSOperationType.UPDATE);
                        teamsAndPlayersTree.updateCache(parentId, DSOperationType.UPDATE);
                    }
                }).show();
            }
        });

        editButton.disable();
        buttonHStack.addMember(editButton);

        deleteButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (teamsAndPlayersTree.getSelectedRecord() == null) {
                    deleteButton.disable();
                    return;
                }

                SC.confirm("Are you sure you want to delete '" + teamsAndPlayersTree.getSelectedRecord().getAttributeAsString("name") + "'?", new BooleanCallback() {
                    public void execute(Boolean value) {
                        if (value != null && value) {
                            final String dataSourceName = teamsAndPlayersTree.getSelectedRecord().getAttributeAsString("dataSource");
                            final DataSource dataSource = DataSource.get(dataSourceName);
                            final Map data = teamsAndPlayersTree.getSelectedRecord().getAttributeAsMap("data");
                            final DSRequest request = new DSRequest(DSOperationType.REMOVE, new Criteria("id", String.valueOf(data.get("id"))));

                            // This is the actual call to delete the node, it calls the datasource named in the dataSource attribute of the node.
                            dataSource.removeData(teamsAndPlayersTree.getSelectedRecord(), new DSCallback() {
                                @Override
                                public void execute(DSResponse response, Object rawData, DSRequest request) {
                                    // Once the record has been deleted from the underlying datasource lets issue a REMOVE to the tree's cache and get rid of this node.
                                    final DSResponse deleteResponse = new DSResponse("treegridfacade", DSOperationType.REMOVE, teamsAndPlayersTree.getSelectedRecord());
                                    teamsAndPlayersTree.updateCacheUsingResponse(deleteResponse);
                                    teamsAndPlayersTree.updateCache(null, DSOperationType.UPDATE);
                                }
                            }, request);
                        }
                    }
                });
            }
        });

        deleteButton.disable();
        buttonHStack.addMember(deleteButton);

        addMember(buttonHStack);

        teamsAndPlayersTree.addSelectionChangedHandler(new SelectionChangedHandler() {
            @Override
            public void onSelectionChanged(final SelectionEvent event) {
                if (teamsAndPlayersTree.anySelected()) {
                    editButton.enable();
                    deleteButton.enable();
                } else {
                    editButton.disable();
                    deleteButton.disable();
                }
            }
        });
    }

    @Override
    public void modeChanged(final String newMode) {
        if ("server".equalsIgnoreCase(newMode)) {
            teamsAndPlayersTree.setDataSource(DataSource.get("treegridfacade"));
        } else {
            teamsAndPlayersTree.setDataSource(new TreeGridFacadeClientSideDataSource());
        }

        teamsAndPlayersTree.markForRedraw();
        teamsAndPlayersTree.fetchData();
    }
}
