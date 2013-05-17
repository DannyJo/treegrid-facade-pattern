package com.smartgwt.sample.server.dao;

import com.isomorphic.datasource.DSRequest;
import com.isomorphic.datasource.DSResponse;
import com.isomorphic.datasource.DataSource;
import com.smartgwt.sample.server.bean.Player;

import java.util.List;

public class Players {

    public static final String DATASOURCE_NAME = "players";

    public static List<Player> fetchAllPlayers() throws Exception {
        final DSResponse response = new DSRequest(DATASOURCE_NAME, DataSource.OP_FETCH).execute();
        return DaoUtils.convertMapListToTypedList(Player.class, response.getDataList());
    }

    public static List<Player> fetchPlayersInTeam(final String teamId) throws Exception {
        final DSRequest request = new DSRequest(DATASOURCE_NAME, DataSource.OP_FETCH).setCriteria("teamId", teamId);
        final DSResponse response = request.execute();
        return DaoUtils.convertMapListToTypedList(Player.class, response.getDataList());
    }
}
