package com.smartgwt.sample.client.ui;

import com.smartgwt.client.data.*;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickEvent;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.SelectItem;
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

    private void createForm(final String dataSourceName, final Map data) {
        form.setDataSource(DataSource.get(dataSourceName));
        form.setMargin(5);
        form.setLayoutAlign(Alignment.CENTER);
        form.setAutoFocus(true);

        if (data == null) {
            form.editNewRecord();
        } else {
            form.editRecord(new Record(data));
        }

        // Add sorting by the name for the team dropdown
        final SelectItem selectItem = (SelectItem) form.getField("teamId");

        if (selectItem != null) {
            selectItem.setSortField("name");
        }

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
                if (form.validate()) {
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
