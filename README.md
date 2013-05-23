# NOTE: This article is a work in progress.



# Description

The TreeGrid component allows rendering of data that is structured with a parent/child relationship. With SmartGWT you can only assign
one datasource to the TreeGrid, now this can cause a bit of a headache if the data is actually separated into multiple datasources. This
is where the TreeGrid Facade Pattern comes in.

The TreeGrid Facade Pattern means you create a custom datasource that sits infront of the underlying datasources and acts like a facade
for the TreeGrid component.

As you can write a datasource both as a client side class and a server side class this article will give you a sample of both methods.

    A working example is available at https://github.com/DannyJo/treegrid-facade-pattern

## Implementation

There are two ways in which this pattern can be implemented, either you create a server side custom datasource __OR__ a client side custom
datasource, you DO NOT need both.

This sample will implement a facade to two underlying datasources, teams and players. A team has players and a player only has one team.


### Server side

In order to start this off, we need to create a generic datasource.

####treeFacadeDS.ds.xml

    <DataSource ID="treeFacadeDS"
                serverType="generic"
                serverConstructor="com.smartgwt.sample.server.TreeFacadeDS">

        <fields>
            <field name="id" title="id" type="text" primaryKey="true" hidden="true"/>
            <field name="name" title="name" type="text"/>
        </fields>
    </DataSource>

Now this is pretty straight forward, just like a normal datasource except we specify a serverType of generic and we add a serverConstructor
which is a fully qualified identifier for the server side class that implements this datasource. More information on custom datasource can
be found at http://www.smartclient.com/smartgwtee/javadoc/com/smartgwt/client/docs/WriteCustomDataSource.html

It's important that we make sure the datasource has a primaryKey field, without this the datasource caching won't work.

#### TreeFacadeDS.java

This is the brains of the server side implementation. There are a couple of sections which I will go over in separate but the main job of
this class is to make calls to the underlying datasources (teams and players) based on the parentId from the TreeGrid component.

In this sample the only thing we need to do is implement the __executeFetch()__ method.

__NOTE:__ This sample does not demonstrate how to implement CRUD in the actual facade datasource but we could easily implement the other
methods (__executeAdd()__, __executeUpdate()__ and __executeRemove()__) here to allow for true editing and moving of nodes in the tree.

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

The __executeFetch()__ method is pretty straight forward, it grabs the __parentId__ from the incoming criteria and uses that to figure out
if it should delegate to __teams__ or __players__ datasource. It then creates a new __DSRequest__ and executes that against the datasource.

Once the response has come back we call a method to map the data into a format the TreeGrid can handle. Read from one map, put to another.

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
    }


When the TreeGrid component first fetches its data there is no parentId to fetch data for so parentId will be null. In this case we return
all the teams from the teams datasource.

    final DSResponse teamsResponse = new DSRequest("teams", DataSource.OP_FETCH).execute();

    for (final Object team : teamsResponse.getDataList()) {
        if (team != null) {
            treeItems.add(convertToTreeItem((Map) team, TEAMS));
        }
    }

The above code will issue a fetch request to the teams datasource, it then loops through all the teams in the response and converts them
to a format that the TreeGrid will understand.

Something important to note here is the property called __data__ which is the native record from the underlying datasource. This will be
used on the client side when we edit the items in the TreeGrid.



### Client side

Now in order to use a client side implementation to create a facade datasource all we need to do is create a class that extends the
com.smartgwt.client.data.DataSource class. This will be very similar to the server side datasource class especially in the way the data
is mapped across to the format the TreeGrid can understand.

####TreeFacadeClientDS.java

For the client side implementation we extend the DataSource class and override the __transformRequest()__ method. It is important here to
set the data protocol to __DSProtocol.CLIENTCUSTOM__, this will allow us to actually transform the requests.

So in the constructor we need to define the datasource, in the server side implementation this was done using a ds.xml file but here
we define the fields in java.

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

Now on to the __transformRequest()__ method. This is a bit different to the server side implementation and perhaps less clear on what you
need to do. A request to this custom datasource will pass through this method so here we need to add our facade logic which will delegate
to the correct underlying datasource.

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
