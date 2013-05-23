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

    @Override
    public DSResponse executeFetch(final DSRequest request) throws Exception {
        final List<Map> treeItems = new ArrayList<Map>();
        final DSResponse response = new DSResponse(treeItems);
        final String parentId = (String) request.getCriteriaValue("parentId");

        // If we have no parentId then return the root nodes which are all the teams.
        if (parentId == null) {
            final DSResponse teamsResponse = new DSRequest("teams", DataSource.OP_FETCH).execute();
            treeItems.addAll(convertToTreeItem(teamsResponse.getDataList(), "teams"));
        } else {
            // If we have a parentId, check to see if it starts with teams: and if it does parse it and grab the teamId
            if (StringUtils.startsWith(parentId, "teams:")) {
                final String teamId = StringUtils.substringAfter(parentId, ":");

                // Issue a request to fetch all players for the specific team.
                final DSResponse playersResponse = new DSRequest("players", DataSource.OP_FETCH).setCriteria("teamId", teamId).execute();
                treeItems.addAll(convertToTreeItem(playersResponse.getDataList(), "players"));
            }
        }

        return response;
    }

    private List<Map> convertToTreeItem(final List inputDataList, final String dataSourceName) {
        final List<Map> outputDataList = new ArrayList<Map>();

        if (inputDataList != null) {
            for (final Object dataObject : inputDataList) {
                final Map data = (Map) dataObject;

                final Map<String, Object> treeItem = new HashMap<String, Object>();
                treeItem.put("id", dataSourceName + ":" + data.get("id"));
                treeItem.put("dataSourceName", dataSourceName);
                treeItem.put("name", data.get("name"));
                treeItem.put("data", data);

                if ("players".equals(dataSourceName)) {
                    treeItem.put("isFolder", false);
                    treeItem.put("parentId", "teams:" + data.get("teamId"));
                    treeItem.put("icon", "player.png");
                } else {
                    treeItem.put("isFolder", true);
                }

                outputDataList.add(treeItem);
            }
        }

        return outputDataList;
    }
}
