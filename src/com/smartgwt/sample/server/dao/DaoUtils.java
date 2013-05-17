package com.smartgwt.sample.server.dao;

import com.isomorphic.util.DataTools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DaoUtils {

    public static <T> T convertMapToTypedObject(final Class<T> clazz, final Map map) throws Exception {
        return clazz.cast(DataTools.setProperties(map, clazz.newInstance()));
    }

    public static <T> List<T> convertMapListToTypedList(final Class<T> clazz, final List list) throws Exception {
        final List<T> typedList = new ArrayList<T>();

        for (final Object mapObject : list) {
            typedList.add(convertMapToTypedObject(clazz, (Map) mapObject));
        }

        return typedList;
    }
}
