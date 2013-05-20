package com.smartgwt.sample.client;

import com.smartgwt.client.data.*;
import com.smartgwt.client.rpc.RPCResponse;
import com.smartgwt.client.types.DSOperationType;
import com.smartgwt.client.types.DSProtocol;
import com.smartgwt.client.types.FieldType;

public class TreeGridFacadeClientSideDataSource extends DataSource {

    public TreeGridFacadeClientSideDataSource() {
        super();

        setDataProtocol(DSProtocol.CLIENTCUSTOM);

        final DataSourceField idField = new DataSourceField("id", FieldType.TEXT);
        final DataSourceField nameField = new DataSourceField("name", FieldType.TEXT);
        final DataSourceField parentIdField = new DataSourceField("parentId", FieldType.TEXT);

        setFields(idField, nameField, parentIdField);
    }

    @Override
    protected Object transformRequest(final DSRequest request) {
        final String requestId = request.getRequestId();
        final DSResponse response = new DSResponse();

        if (DSOperationType.FETCH.equals(request.getOperationType())) {
            executeFetch(requestId, request, response);
        }

        return request.getData();
    }

    private void executeFetch(final String requestId, final DSRequest request, final DSResponse response) {
        final String parentId = request.getCriteria().getAttributeAsString("parentId");

        if (parentId == null) {
            DataSource.get("teams").fetchData(new Criteria("", ""), new DSCallback() {
                @Override
                public void execute(final DSResponse fetchResponse, final Object rawData, final DSRequest fetchRequest) {
                    response.setStatus(RPCResponse.STATUS_SUCCESS);
                    response.setData(getTeamRecords(fetchResponse.getDataAsRecordList()));
                    processResponse(requestId, response);
                }
            });
        } else {
            final String sourceId = parentId.substring(parentId.indexOf(":") + 1);

            DataSource.get("players").fetchData(new Criteria("teamId", sourceId), new DSCallback() {
                @Override
                public void execute(final DSResponse fetchResponse, final Object rawData, final DSRequest fetchRequest) {
                    response.setStatus(RPCResponse.STATUS_SUCCESS);
                    response.setData(getPlayerRecords(fetchResponse.getDataAsRecordList()));
                    processResponse(requestId, response);
                }
            });
        }
    }

    private Record[] getTeamRecords(final RecordList recordList) {
        final Record[] records = new Record[recordList.getLength()];

        for (int i = 0; i < recordList.getLength(); i++) {
            records[i] = new Record();
            records[i].setAttribute("id", "teams:" + recordList.get(i).getAttributeAsString("id"));
            records[i].setAttribute("name", recordList.get(i).getAttributeAsString("name") + " (" + recordList.get(i).getAttributeAsString("playerCount") + ")");
            records[i].setAttribute("dataSource", "teams");
            records[i].setAttribute("data", recordList.get(i));
            records[i].setAttribute("isFolder", true);
            records[i].setAttribute("canEdit", false);
            records[i].setAttribute("sourceId", recordList.get(i).getAttributeAsString("id"));
        }

        return records;
    }

    private Record[] getPlayerRecords(final RecordList recordList) {
        final Record[] records = new Record[recordList.getLength()];

        for (int i = 0; i < recordList.getLength(); i++) {
            records[i] = new Record();
            records[i].setAttribute("parentId", "teams:" + recordList.get(i).getAttributeAsString("teamId"));
            records[i].setAttribute("id", "players:" + recordList.get(i).getAttributeAsString("id"));
            records[i].setAttribute("name", recordList.get(i).getAttributeAsString("name"));
            records[i].setAttribute("icon", "player.png");
            records[i].setAttribute("dataSource", "players");
            records[i].setAttribute("isFolder", false);
            records[i].setAttribute("canEdit", false);
            records[i].setAttribute("data", recordList.get(i));
            records[i].setAttribute("sourceId", recordList.get(i).getAttributeAsString("id"));
        }

        return records;
    }
}
