package com.smartgwt.sample.client.ui;

import com.smartgwt.client.data.*;
import com.smartgwt.client.types.DSOperationType;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.widgets.tree.TreeGrid;

public class TeamsAndPlayersTree extends TreeGrid {

    public TeamsAndPlayersTree() {
        setDataSource(DataSource.get("treeFacadeDS"));
        setLoadDataOnDemand(true);
        setWidth("25%");
        setHeight("80%");
        setAutoFetchData(true);
        setCanReparentNodes(false);
        setSortField("name");
        setSortDirection(SortDirection.ASCENDING);
    }

    public void updateCache() {
        updateCache(null, DSOperationType.UPDATE);
    }

    public void updateCache(final String parentId, final DSOperationType operationType) {
        getDataSource().fetchData(new Criteria("parentId", parentId), new DSCallback() {
            @Override
            public void execute(final DSResponse response, final Object rawData, final DSRequest request) {
                request.setOperationType(operationType);
                getDataSource().updateCaches(response, request);
            }
        });
    }

    public void updateCacheUsingResponse(final DSResponse response) {
        getDataSource().updateCaches(response);
    }
}
