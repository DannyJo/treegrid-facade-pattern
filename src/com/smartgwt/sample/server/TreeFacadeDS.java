package com.smartgwt.sample.server;

import com.isomorphic.datasource.BasicDataSource;
import com.isomorphic.datasource.DSRequest;
import com.isomorphic.datasource.DSResponse;
import com.isomorphic.datasource.DataSource;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TreeFacadeDS extends BasicDataSource {

    private static final String TEAMS = "teams";
    private static final String PLAYERS = "players";

    @Override
    public DSResponse executeFetch(final DSRequest request) throws Exception {
        final DSResponse response = new DSResponse(new ArrayList());
        final String parentId = (String) request.getCriteriaValue("parentId");

        if (parentId != null) {
            if ("teams".equalsIgnoreCase(parentId)) {
                final DSResponse teamsResponse = new DSRequest("teams", DataSource.OP_FETCH).execute();

                teamsResponse.getData();
                for (final Object team : teamsResponse.getDataList()) {
                    response.getDataList().add(convertToTreeItem((Map) team, TEAMS));
                }
            } else if ("players".equalsIgnoreCase(parentId)) {
                final DSResponse playersResponse = new DSRequest("players", DataSource.OP_FETCH).execute();

                for (final Object player : playersResponse.getDataList()) {
                    final Map playerMap = (Map) player;
                    response.getDataList().add(convertToTreeItem(playerMap, PLAYERS));
                }
            } else if (StringUtils.contains(parentId, ":")) {
                final String teamId = StringUtils.substringAfter(parentId, ":");

                if (StringUtils.startsWith(parentId, "teams")) {
                    final DSResponse playersResponse = new DSRequest("players", DataSource.OP_FETCH).setCriteria("teamId", teamId).execute();

                    for (final Object player : playersResponse.getDataList()) {
                        response.getDataList().add(convertToTreeItem((Map) player, PLAYERS));
                    }
                }
            }
        } else {
            final DSResponse teamsResponse = new DSRequest("teams", DataSource.OP_FETCH).execute();

            for (final Object team : teamsResponse.getDataList()) {
                if (team != null) {
                    response.getDataList().add(convertToTreeItem((Map) team, TEAMS));
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
