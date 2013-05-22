package com.smartgwt.sample.client;

import com.smartgwt.client.data.*;
import com.smartgwt.client.types.DSOperationType;
import com.smartgwt.client.types.DSProtocol;
import com.smartgwt.client.types.FieldType;

public class TreeFacadeClientDS extends DataSource {

    public TreeFacadeClientDS() {
        super();

        setDataProtocol(DSProtocol.CLIENTCUSTOM);
        setAutoCacheAllData(true);

        final DataSourceField idField = new DataSourceField("id", FieldType.TEXT);
        idField.setPrimaryKey(true);
        idField.setHidden(true);
        final DataSourceField nameField = new DataSourceField("name", FieldType.TEXT);

        setFields(idField, nameField);
    }

    @Override
    protected Object transformRequest(final DSRequest request) {
        final String requestId = request.getRequestId();
        final DSResponse response = new DSResponse();

        if (DSOperationType.FETCH.equals(request.getOperationType())) {
            final String parentId = request.getCriteria().getAttributeAsString("parentId");

            if (parentId == null) {
                DataSource.get("teams").fetchData(null, new DSCallback() {
                    @Override
                    public void execute(final DSResponse fetchResponse, final Object rawData, final DSRequest fetchRequest) {
                        response.setData(getRecords(fetchResponse.getDataAsRecordList(), "teams"));
                        processResponse(requestId, response);
                    }
                });
            } else {
                final String sourceId = parentId.substring(parentId.indexOf(":") + 1);

                DataSource.get("players").fetchData(new Criteria("teamId", sourceId), new DSCallback() {
                    @Override
                    public void execute(final DSResponse fetchResponse, final Object rawData, final DSRequest fetchRequest) {
                        response.setData(getRecords(fetchResponse.getDataAsRecordList(), "players"));
                        processResponse(requestId, response);
                    }
                });
            }
        }

        return request.getData();
    }

    private Record[] getRecords(final RecordList recordList, final String dataSourceName) {
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
