package com.smartgwt.sample.server.dao;

import com.isomorphic.datasource.DSRequest;
import com.isomorphic.datasource.DSResponse;
import com.isomorphic.datasource.DataSource;
import com.smartgwt.sample.server.bean.Team;

import java.util.List;

public class Teams {

    public static final String DATASOURCE_NAME = "teams";

    public static List<Team> fetchAllTeams() throws Exception {
        final DSResponse response = new DSRequest(DATASOURCE_NAME, DataSource.OP_FETCH).execute();
        return DaoUtils.convertMapListToTypedList(Team.class, response.getDataList());
    }
}
