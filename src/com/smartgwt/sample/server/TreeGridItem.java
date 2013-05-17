package com.smartgwt.sample.server;

import com.isomorphic.util.DataTools;

import java.io.Serializable;
import java.util.Map;

public class TreeGridItem implements Serializable {

    private String id;
    private String parentId;
    private String name;
    private String icon;
    private String dataSource;
    private String sourceId;
    private Boolean isFolder = Boolean.TRUE;
    private Boolean canEdit = Boolean.FALSE;
    private Object data;

    public TreeGridItem() {

    }

    public TreeGridItem(final Map attributes) throws Exception {
        DataTools.setProperties(attributes, this);
    }

    public TreeGridItem(final String id, final String name, final String icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Boolean getIsFolder() {
        return isFolder;
    }

    public void setIsFolder(Boolean isFolder) {
        this.isFolder = isFolder;
    }

    public Boolean getCanEdit() {
        return canEdit;
    }

    public void setCanEdit(Boolean canEdit) {
        this.canEdit = canEdit;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
