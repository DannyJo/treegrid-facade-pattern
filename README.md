# NOTE: This article is a work in progress.



# Description

The TreeGrid component allows rendering of data that is structured with a parent/child relationship. With SmartGWT you can only assign one
datasource to the TreeGrid, now this can cause a bit of a headache if the data is actually separated into multiple datasources. This is
where the TreeGrid Facade Pattern comes in. The TreeGrid Facade Pattern means you create a custom datasource that sits infront of the
underlying datasources and acts like a facade for the TreeGrid component.

In order to bind the data with the TreeGrid, it requires an id to make the nodes unique, a parent id in order to know where to render the
node as well as a name field in order to display the node. The id's will be created by the facade by joining together the name of the
datasource eg. teams and the id of the record, so for instance a team with id 3 would in the TreeGrid have an id of "teams:3". The parent
id works very the same way except for the top level nodes where we leave it null. The name field we populate from what would be the most
sensible field to represent the underlying data, this could even be a concatenation of several fields if need be.

As you can write a datasource both as a client side class and a server side class this article will give you a sample of both methods.

This article only contains snippets of the most important code in order to keep it simple and to explain how to use this pattern. Look
at the full source code to see what the UI code looks like in order to create the tree and the add/edit dialog.

    A working example is available at https://github.com/DannyJo/treegrid-facade-pattern

## Implementation

There are two ways in which this pattern can be implemented, either you create a custom server side OR client side datasource, you
DO NOT need both. This sample will implement a facade to two underlying datasources, teams and players. A team has players and a player
only has one team.

### Option 1 (Server Side)

In order to start this off, we need to create a datasource.

####treeFacadeDS.ds.xml

    <DataSource ID="treeFacadeDS" serverConstructor="com.smartgwt.sample.server.TreeFacadeDS">
        <fields>
            <field name="id" title="id" type="text" primaryKey="true" hidden="true"/>
            <field name="name" title="name" type="text"/>
        </fields>
    </DataSource>

Now this is pretty straight forward, just like a normal datasource and we add a serverConstructor which is a fully qualified identifier
for the server side class that implements this datasource. More information on custom datasource can be found at
http://www.smartclient.com/smartgwtee-latest/javadoc/com/smartgwt/client/docs/WriteCustomDataSource.html

It's important that we make sure the datasource has a primaryKey field, without this the datasource caching won't work.

#### TreeFacadeDS.java

This is the brains of the server side implementation. There are a couple of sections which I will go over in separate but the main
job of this class is to make calls to the underlying datasources (teams and players) based on the parentId from the TreeGrid component.
In this sample the only thing we need to do is implement the executeFetch() method.

__NOTE:__ This sample does not demonstrate how to implement CRUD in the actual facade datasource but we could easily implement the other
methods (executeAdd(), executeUpdate() and executeRemove()) here to allow for true editing and moving of nodes in the tree itself.

#### Override/Implement executeFetch() method

This method is pretty straight forward, it grabs the parentId from the incoming criteria and uses that to figure out if it should
delegate to teams or players datasource. It then creates a new DSRequest and executes that against the datasource. Once the response has
come back we call a method to map the data into a format the TreeGrid can handle. Read from one map, put to another.

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

When the TreeGrid component first fetches its data there is no parentId to fetch data for so parentId will be null. In this case we return
all the teams from the teams datasource as you can see here.

    final DSResponse teamsResponse = new DSRequest("teams", DataSource.OP_FETCH).execute();
    treeItems.addAll(convertToTreeItem(teamsResponse.getDataList(), "teams"));

#### Mapping the data

There is nothing special with this, the only reason it's in its own method is for re-usability but you could decide to implement this
in a separate testable class if you wish. It simply reads from one map and puts into another.

    private Map convertToTreeItem(final Map data, final String dataSourceName) {
        final Map treeItem = new HashMap();
        treeItem.put("id", dataSourceName + ":" + data.get("id"));
        treeItem.put("dataSourceName", dataSourceName);
        treeItem.put("name", data.get("name"));
        treeItem.put("data", data);
        if (PLAYERS.equals(dataSourceName)) {
            treeItem.put("isFolder", false);
            treeItem.put("parentId", "teams:" + data.get("teamId"));
            treeItem.put("icon", "player.png");
        } else {
            treeItem.put("isFolder", true);
        }
        return treeItem;
    }

Something important to note here is the property called data which is the native record from the underlying datasource. This will be
used on the client side when we edit the items in the TreeGrid.

That's it for the server side implementation. Go on and have a look at the client side implementation below and study them to see what
the difference is.

### Option 2 (Client Side)

The second option is to write a custom client side datasource to act as the facade. In order to do this all we need to do is create a
class that extends the com.smartgwt.client.data.DataSource class. This will be very similar to the server side datasource class especially
in the way the data is mapped across to the format the TreeGrid can understand.

#### TreeFacadeClientDS.java

For the client side implementation we extend the DataSource class and override the transformRequest() method. It is important here to set
the data protocol to DSProtocol.CLIENTCUSTOM, this will allow us to actually transform the requests. In the constructor we need to define
the datasource, in the server side implementation this was done using a ds.xml file but here we define the fields in java.

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

        // This is the parent id, also the foreign key field.
        final DataSourceField parentIdField = new DataSourceField("parentId", FieldType.TEXT);
        parentIdField.setForeignKey("id");

        // This is the only field we actually display in the tree, the name.
        final DataSourceField nameField = new DataSourceField("name", FieldType.TEXT);
        // Set the fields on the datasource.
        setFields(idField, parentIdField, nameField);
    }

Make sure you have a read of the included comments about the DSProtocol.CLIENTCUSTOM that are included above.

#### Override/Implement transformRequest() method

Now we need to override the transformRequest() method. This is a bit different to the server side implementation and perhaps less clear
on what you need to do. A request to this datasource will pass through this method so here we need to add our facade logic which will
delegate to the correct underlying datasource.

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
            } else if (parentId.startsWith("teams:")) {
                // Now if we have a parentId and because our parentIds look like "datasourcename:id" we need to string parse it to get the id.
                final String teamId = parentId.substring(parentId.indexOf(":") + 1);
                // Using DataSource.fetchData() we now use a criteria to fetch all players in the specific team.
                DataSource.get("players").fetchData(new Criteria("teamId", teamId), new DSCallback() {
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

If you follow the flow you can see that the logic is exactly the same as in the server side datasource we created. We check the parentId
and then fire off a request to the appropriate datasource. The way we call the underlying datasource is slightly different here, as you
can see we call DataSource.fetchData() instead of creating a DSRequest object. This way requires us to pass a callback implementation
through as this is all run client side and using AJAX.

    DataSource.get("teams").fetchData(null, new DSCallback() {
        @Override
        public void execute(final DSResponse fetchResponse, final Object rawData, final DSRequest fetchRequest) {
            response.setData(convertToTreeItem(fetchResponse.getDataAsRecordList(), "teams"));
            processResponse(requestId, response); // This will ensure that the response is processed and returned to the component.
        }
    });

Take note of the call to processResponse() in the callback method. This is required in order for the component to get the data back.

#### Mapping the data

Just like in the server side implementation we need to map the data to a format that the TreeGrid will understand so we have a method to
solve that here as well. It is very similar to the server side method but uses Record instead of Map. Read from one record and write to a
new record.

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

That's it for the client side implementation. A quick look at the differences reveals that the calls to the underlying datasources are
done in a different way as well as the how you implement the custom datasources using either executeFetch() for server side or
transformRequest() for client side.


### Conclusion

To sum up, in this article we discussed the follow
* How to implement a custom datasource both as a server and client side datasource.
* What the TreeGrid Facade Pattern is and how you can implement a solution for it.
* How to issue a request to another datasource from the custom datasource and the difference between doing it client side versus server side.
