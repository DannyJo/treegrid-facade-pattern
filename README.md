# NOTE: This article is still a work in progress.



# Description

The TreeGrid component allows rendering of data that is structured with a parent/child relationship. With SmartGWT you can only assign
the TreeGrid one datasource, now this can cause a bit of a headache if the data is actually separated into multiple datasources. This
is where the TreeGrid Facade Pattern comes in.

The TreeGrid Facade Pattern means you create a custom datasource that sits infront of the underlying datasources and acts like a facade
for the TreeGrid component. As you can write a datasource both as a client side class and a server side class this article will give
you a sample of both methods.

    A working example is available at https://github.com/DannyJo/treegrid-facade-pattern

## Implementation

This sample implementation uses two underlying datasources, teams and players. A team has players and a player only has one team.

teams.ds.xml

    <DataSource ID="teams"
                serverType="sql"
                tableName="teams"
                recordName="team"
                testFileName="teams.data.xml"
                titleField="name">

        <fields>
            <field name="id" title="ID" type="sequence" primaryKey="true" hidden="true"/>
            <field name="name" title="Name" type="text" length="128"/>
        </fields>
    </DataSource>


players.ds.xml

    <DataSource ID="players"
                serverType="sql"
                tableName="players"
                recordName="player"
                testFileName="players.data.xml"
                titleField="name">

        <fields>
            <field name="id" title="ID" type="sequence" primaryKey="true" hidden="true"/>
            <field name="name" title="Name" type="text" length="128"/>
            <field name="teamId" title="Team" optionsDataSource="teams" valueField="id" displayField="name" foreignKey="teams.id"/>
            <field name="team" includeFrom="teams.name" hidden="true"/>
        </fields>
    </DataSource>


## Server side implementation

In order to start this off, we need to create a generic datasource.

treeFacadeDS.ds.xml

    <DataSource ID="treeFacadeDS"
                serverType="generic"
                serverConstructor="com.smartgwt.sample.server.TreeFacadeDS">

        <fields>
            <field name="id" title="id" type="text" primaryKey="true" hidden="true"/>
            <field name="name" title="name" type="text"/>
        </fields>
    </DataSource>

Now this is pretty straight forward, just like a normal datasource except we specify a serverType of generic and we add a serverConstructor
which is a fully qualified identifier for the server side class that implements this datasource. More information on custom datasource can
be found at http://www.smartclient.com/smartgwtee/javadoc/com/smartgwt/client/docs/WriteCustomDataSource.html

TreeFacadeDS.java

    package com.smartgwt.sample.server;

    import com.isomorphic.datasource.BasicDataSource;
    import com.isomorphic.datasource.DSRequest;
    import com.isomorphic.datasource.DSResponse;
    import com.isomorphic.datasource.DataSource;
    import org.apache.commons.lang.StringUtils;

    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    public class TreeFacadeDS extends BasicDataSource {

        private static final String TEAMS = "teams";
        private static final String PLAYERS = "players";

        @Override
        public DSResponse executeFetch(final DSRequest request) throws Exception {
            final List<Map> treeItems = new ArrayList<Map>();
            final DSResponse response = new DSResponse(treeItems);
            final String parentId = (String) request.getCriteriaValue("parentId");

            if (parentId != null) {
                if ("teams".equalsIgnoreCase(parentId)) {
                    final DSResponse teamsResponse = new DSRequest("teams", DataSource.OP_FETCH).execute();

                    teamsResponse.getData();
                    for (final Object team : teamsResponse.getDataList()) {
                        treeItems.add(convertToTreeItem((Map) team, TEAMS));
                    }
                } else if ("players".equalsIgnoreCase(parentId)) {
                    final DSResponse playersResponse = new DSRequest("players", DataSource.OP_FETCH).execute();

                    for (final Object player : playersResponse.getDataList()) {
                        final Map playerMap = (Map) player;
                        treeItems.add(convertToTreeItem(playerMap, PLAYERS));
                    }
                } else if (StringUtils.contains(parentId, ":")) {
                    final String teamId = StringUtils.substringAfter(parentId, ":");

                    if (StringUtils.startsWith(parentId, "teams")) {
                        final DSResponse playersResponse = new DSRequest("players", DataSource.OP_FETCH).setCriteria("teamId", teamId).execute();

                        for (final Object player : playersResponse.getDataList()) {
                            treeItems.add(convertToTreeItem((Map) player, PLAYERS));
                        }
                    }
                }
            } else {
                final DSResponse teamsResponse = new DSRequest("teams", DataSource.OP_FETCH).execute();

                for (final Object team : teamsResponse.getDataList()) {
                    if (team != null) {
                        treeItems.add(convertToTreeItem((Map) team, TEAMS));
                    }
                }
            }

            return response;
        }

        private Map convertToTreeItem(final Map data, final String dataSourceName) {
            final Map treeItem = new HashMap();
            treeItem.put("id", dataSourceName + ":" + data.get("id"));
            treeItem.put("dataSourceName", dataSourceName);
            treeItem.put("name", data.get("name"));
            treeItem.put("data", data);

            if (PLAYERS.equals(dataSourceName)) {
                treeItem.put("isFolder", false);
                treeItem.put("parentId", "teams:" + data.get("teamId"));
                treeItem.put("icon", "player.png");
            } else {
                treeItem.put("isFolder", true);
            }

            return treeItem;
        }
    }

This is the brains of the server side implementation. There are a couple of sections which I will go over in separate but the main job of
this class is to make calls to the underlying datasources (teams and players) based on the parentId from the TreeGrid component.

When the TreeGrid component first fetches its data there is no parentId to fetch data for so parentId will be null. In this case we return
all the teams from the teams datasource.

    final DSResponse teamsResponse = new DSRequest("teams", DataSource.OP_FETCH).execute();

    for (final Object team : teamsResponse.getDataList()) {
        if (team != null) {
            treeItems.add(convertToTreeItem((Map) team, TEAMS));
        }
    }

The above code will issue a fetch request to the teams datasource, it then loops through all the teams in the response and converts them
to a format that the TreeGrid will understand, in this case it's a simple Map with 5 or 7 properties.

We've got a method that will translate the records for us.

    private Map convertToTreeItem(final Map data, final String dataSourceName) {
        final Map treeItem = new HashMap();
        treeItem.put("id", dataSourceName + ":" + data.get("id"));
        treeItem.put("dataSourceName", dataSourceName);
        treeItem.put("name", data.get("name"));
        treeItem.put("data", data);

        if (PLAYERS.equals(dataSourceName)) {
            treeItem.put("isFolder", false);
            treeItem.put("parentId", "teams:" + data.get("teamId"));
            treeItem.put("icon", "player.png");
        } else {
            treeItem.put("isFolder", true);
        }

        return treeItem;
    }

This copies the properties from the teams and players datasource records into the Map the TreeGrid will understand how to handle.
Something important to note here is the property called __data__ which is the native record from the underlying datasource. This will be
used on the client side when we edit the items in the TreeGrid.






## Client side implementation
