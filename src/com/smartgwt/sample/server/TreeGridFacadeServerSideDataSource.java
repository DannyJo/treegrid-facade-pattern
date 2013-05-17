package com.smartgwt.sample.server;

import com.isomorphic.datasource.BasicDataSource;
import com.isomorphic.datasource.DSRequest;
import com.isomorphic.datasource.DSResponse;
import com.smartgwt.sample.server.bean.Player;
import com.smartgwt.sample.server.bean.Team;
import com.smartgwt.sample.server.dao.Players;
import com.smartgwt.sample.server.dao.Teams;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class TreeGridFacadeServerSideDataSource extends BasicDataSource {

    @Override
    public DSResponse executeFetch(final DSRequest request) throws Exception {
        final List<TreeGridItem> treeGridItems = new ArrayList<TreeGridItem>();
        final DSResponse response = new DSResponse(treeGridItems);
        final String parentId = (String) request.getCriteriaValue("parentId");

        if (parentId != null) {
            if (Teams.DATASOURCE_NAME.equalsIgnoreCase(parentId)) {
                treeGridItems.addAll(convertTeamsToTreeGridItems(Teams.fetchAllTeams()));
            } else if (Players.DATASOURCE_NAME.equalsIgnoreCase(parentId)) {
                treeGridItems.addAll(convertPlayersToTreeGridItems(Players.fetchAllPlayers()));
            } else if (StringUtils.contains(parentId, ":")) {
                final String id = StringUtils.substringAfter(parentId, ":");

                if (StringUtils.startsWith(parentId, Teams.DATASOURCE_NAME)) {
                    treeGridItems.addAll(convertPlayersToTreeGridItems(Players.fetchPlayersInTeam(id)));
                }
            }
        } else {
            treeGridItems.addAll(convertTeamsToTreeGridItems(Teams.fetchAllTeams()));
        }

        return response;
    }

    private List<TreeGridItem> convertPlayersToTreeGridItems(final List<Player> players) {
        final List<TreeGridItem> items = new ArrayList<TreeGridItem>();

        for (final Player player : players) {
            items.add(convertPlayerToTreeGridItem(player));
        }

        return items;
    }

    private TreeGridItem convertPlayerToTreeGridItem(final Player player) {
        final TreeGridItem item = new TreeGridItem();
        item.setIcon("player.png");
        item.setId(Players.DATASOURCE_NAME + ":" + player.getId());
        item.setDataSource(Players.DATASOURCE_NAME);
        item.setName(player.getName());
        item.setIsFolder(false);
        item.setCanEdit(false);
        item.setSourceId(player.getId());
        item.setParentId(Teams.DATASOURCE_NAME + ":" + player.getTeamId());
        item.setData(player);

        return item;
    }

    private List<TreeGridItem> convertTeamsToTreeGridItems(final List<Team> teams) {
        final List<TreeGridItem> items = new ArrayList<TreeGridItem>();

        for (final Team team : teams) {
            items.add(convertTeamToTreeGridItem(team));
        }

        return items;
    }

    private TreeGridItem convertTeamToTreeGridItem(final Team team) {
        final TreeGridItem item = new TreeGridItem();
        item.setId(Teams.DATASOURCE_NAME + ":" + team.getId());
        item.setIsFolder(true);
        item.setDataSource(Teams.DATASOURCE_NAME);
        item.setName(team.getName() + " (" + team.getPlayerCount() + ")");
        item.setCanEdit(false);
        item.setSourceId(team.getId());
        item.setData(team);

        return item;
    }
}
