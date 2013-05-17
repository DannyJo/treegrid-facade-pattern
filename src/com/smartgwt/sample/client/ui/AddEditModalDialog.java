package com.smartgwt.sample.client.ui;

import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.DSOperationType;
import com.smartgwt.client.types.FetchMode;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickEvent;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.layout.HLayout;

import java.util.Map;

public class AddEditModalDialog extends Window {

    private final DynamicForm form = new DynamicForm();
    private final DSCallback callback;
    private AddEditModalDialog dialogInstance;

    public AddEditModalDialog(final String windowTitle, final String dataSourceName) {
        this(windowTitle, dataSourceName, null, null);
    }

    public AddEditModalDialog(final String windowTitle, final String dataSourceName, final Map formData, final DSCallback callback) {
        this.callback = callback;
        dialogInstance = this;
        setWidth(330);
        setAutoSize(true);
        setPadding(5);
        setTitle(windowTitle);
        setShowModalMask(true);

        createForm(dataSourceName, formData);

        setShowMinimizeButton(false);
        setIsModal(true);
        centerInPage();

        addCloseClickHandler(new CloseClickHandler() {
            public void onCloseClick(CloseClickEvent event) {
                dialogInstance.destroy();
            }
        });
    }

    @Override
    public void show() {
        super.show();
        form.focusInItem(0);
    }

    private void createForm(final String dataSourceName, final Map data) {
        form.setDataSource(DataSource.get(dataSourceName));
        form.setIsGroup(false);
        form.setNumCols(2);
        form.setMargin(5);
        form.setColWidths(60, "*");
        form.setLayoutAlign(VerticalAlignment.BOTTOM);
        form.setLayoutAlign(Alignment.CENTER);
        form.setDataFetchMode(FetchMode.BASIC);

        if (data == null) {
            form.setSaveOperationType(DSOperationType.ADD);
        } else {
            form.setSaveOperationType(DSOperationType.UPDATE);
            form.setValues(data);
        }

        form.setValue("dataSource", dataSourceName);

        addItem(form);

        final HLayout buttonRow = new HLayout();
        buttonRow.setWidth100();
        buttonRow.setHeight(30);
        buttonRow.setAlign(Alignment.CENTER);
        buttonRow.setMembersMargin(5);

        final Button saveButton = new Button("Save");

        saveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                form.saveData(new DSCallback() {
                    @Override
                    public void execute(final DSResponse response, Object rawData, DSRequest request) {
                        if (callback != null) {
                            callback.execute(response, rawData, request);
                        }

                        dialogInstance.destroy();
                    }
                });

                dialogInstance.hide();
            }
        });

        final Button cancelButton = new Button("Cancel");

        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialogInstance.destroy();
            }
        });

        buttonRow.addMember(saveButton);
        buttonRow.addMember(cancelButton);

        addItem(buttonRow);
    }
}
