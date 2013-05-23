package com.smartgwt.sample.client;

import com.smartgwt.client.data.*;
import com.smartgwt.client.types.DSOperationType;
import com.smartgwt.client.types.DSProtocol;
import com.smartgwt.client.types.FieldType;

public class TreeFacadeClientDS extends DataSource {

    public TreeFacadeClientDS() {
        /**
         * This setting entirely bypasses the Smart GWT comm system. Instead of the DataSource sending an HTTP request to the
         * server, the developer is expected to implement {@link com.smartgwt.client.data.DataSource#transformRequest
         * DataSource.transformRequest} to perform their own custom data manipulation logic, and then call {@link
         * com.smartgwt.client.data.DataSource#processResponse DataSource.processResponse} to handle the results of this action.
         */
        setDataProtocol(DSProtocol.CLIENTCUSTOM);

        // Lets create the primaryKey field id and set it to hidden, this is needed to ensure the caching will work.
        final DataSourceField idField = new DataSourceField("id", FieldType.TEXT);
        idField.setPrimaryKey(true);
        idField.setHidden(true);

        // This is the only field we actually display in the tree, the name.
        final DataSourceField nameField = new DataSourceField("name", FieldType.TEXT);

        // Set the fields on the datasource.
        setFields(idField, nameField);
    }

    @Override
    protected Object transformRequest(final DSRequest request) {
        final String requestId = request.getRequestId(); // Grab the requestId, its needed in order to process the response later.
        final DSResponse response = new DSResponse();

        // If this request is of DSOperationType.FETCH, lets handle it.
        if (DSOperationType.FETCH.equals(request.getOperationType())) {
            // Grab the parentId from the criteria
            final String parentId = request.getCriteria().getAttributeAsString("parentId");

            // If there was no parentId then the TreeGrid wants the root nodes so lets return the teams.
            if (parentId == null) {
                // Using DataSource.fetchData() we go and fetch some data from the teams datasource.
                DataSource.get("teams").fetchData(null, new DSCallback() {
                    @Override
                    public void execute(final DSResponse fetchResponse, final Object rawData, final DSRequest fetchRequest) {
                        response.setData(convertToTreeItem(fetchResponse.getDataAsRecordList(), "teams"));
                        processResponse(requestId, response); // This will ensure that the response is processed and returned to the component.
                    }
                });
            } else {
                // Now if we have a parentId and because our parentIds look like "datasourcename:id" we need to string parse it to get the id.
                final String sourceId = parentId.substring(parentId.indexOf(":") + 1);

                // Using DataSource.fetchData() we now use a criteria to fetch all players in the specific team.
                DataSource.get("players").fetchData(new Criteria("teamId", sourceId), new DSCallback() {
                    @Override
                    public void execute(final DSResponse fetchResponse, final Object rawData, final DSRequest fetchRequest) {
                        response.setData(convertToTreeItem(fetchResponse.getDataAsRecordList(), "players"));
                        processResponse(requestId, response); // This will ensure that the response is processed and returned to the component.
                    }
                });
            }
        }

        return request.getData();
    }

    private Record[] convertToTreeItem(final RecordList recordList, final String dataSourceName) {
        final Record[] records = new Record[recordList.getLength()];

        for (int i = 0; i < recordList.getLength(); i++) {
            records[i] = new Record();

            if ("players".equalsIgnoreCase(dataSourceName)) {
                records[i].setAttribute("parentId", "teams:" + recordList.get(i).getAttributeAsString("teamId"));
                records[i].setAttribute("id", "players:" + recordList.get(i).getAttributeAsString("id"));
                records[i].setAttribute("icon", "player.png");
                records[i].setAttribute("isFolder", false);
            } else {
                records[i].setAttribute("id", "teams:" + recordList.get(i).getAttributeAsString("id"));
                records[i].setAttribute("isFolder", true);
            }

            records[i].setAttribute("name", recordList.get(i).getAttributeAsString("name"));
            records[i].setAttribute("dataSourceName", dataSourceName);
            records[i].setAttribute("data", recordList.get(i));
        }

        return records;
    }
}
