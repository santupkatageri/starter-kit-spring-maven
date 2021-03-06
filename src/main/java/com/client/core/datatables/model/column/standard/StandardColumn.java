package com.client.core.datatables.model.column.standard;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.DateTime;

import com.client.core.AppContext;
import com.client.core.ApplicationSettings;
import com.client.core.datatables.model.column.AbstractColumn;
import com.client.core.datatables.tools.enumeration.Editable;
import com.client.core.datatables.tools.enumeration.FieldType;
import com.client.core.datatables.tools.enumeration.Searchable;
import com.client.core.datatables.tools.enumeration.ShowOnForm;
import com.client.core.datatables.tools.enumeration.Sortable;
import com.client.core.datatables.tools.enumeration.Visible;
import com.client.core.datatables.tools.enumeration.IsEntityField;
import com.client.core.datatables.tools.enumeration.Required;
import com.client.core.base.util.Util;

/**
 * One instance of StandardColumn represents one column position on one specific row in the data table. For each row there will be created a
 * number of DataTablesFields that correspond to the number of column in the table.
 * 
 * fieldName has to be the exact name of the field on the Entity fieldLabel is the label that will be used for the column in the data table
 * value is the actual value (in String, Date, Integer etc). displayValue this is the value that will be displayed in the data table
 * 
 * @author magnus.palm
 * 
 */
public final class StandardColumn extends AbstractColumn {
	private final static String dateFormat = AppContext.getApplicationContext()
			.getBean("appSettings", ApplicationSettings.class).getApplicationDateFormat();

	public StandardColumn(FieldType fieldType) {
		setFieldType(fieldType);
	}

    public StandardColumn(String fieldName, String fieldLabel, Object value, Object displayValue, FieldType fieldType, IsEntityField isEntityField,
                          Editable editable, Visible visible, Sortable sortable, Searchable searchable, Required required, ShowOnForm showOnForm,
                          String url, String width, Boolean isListInput) {
        super(fieldName, fieldLabel, value, "", fieldType, isEntityField, editable.getBooleanValue(), visible.getBooleanValue(), sortable
                .getBooleanValue(), searchable.getBooleanValue(), required.getBooleanValue(), showOnForm, url, width, isListInput);
        setDisplayValue(prepareDisplayValue(displayValue));
    }

	@Override
	public String getValueForDisplayOnEditForm() {
		if (getShowOnForm() == ShowOnForm.YES || getFieldType() == FieldType.ID) {
			return prepareDisplayValue(getValue()); // Why isn't this getDisplayValue()? - MG
		}
		return "";
	}

	/**
	 * This method creates the configuration used in the javascript setup for dataTables.editable().
	 * 
	 * The configuration will be inserted into "aoColumns" : [${columnConfigDataTables}] and will determine such things as column data type.
	 */
	@Override
	public String createColumnConfigForDataTablesEditable() {
		if (!isVisible()) {
			return "";
		}

		if (!isEditable()) {
			return "null,";
		}

		StringBuilder result = new StringBuilder();
		String NEW_LINE = System.getProperty("line.separator");
		result.append("{" + NEW_LINE);
		result.append(" tooltip: 'Click to edit'," + NEW_LINE);
		result.append(" cssclass : '" + getFieldType().getCssClass() + "', " + NEW_LINE);

		// Handle dates
		if (getFieldType() == FieldType.DATE) {
			result.append(" type : 'datepicker', " + NEW_LINE);
//			result.append(" datepicker: { dateFormat: '" + Util.convertToJquiDateFormat(dateFormat) + "'} " + NEW_LINE);
			result.append(" datepicker: { format: '" + Util.convertToBootstrapDateFormat(dateFormat) + "'} " + NEW_LINE);
		}
		// Handle passwords
		else if (getFieldType() == FieldType.PASSWORD) {
			result.append(" type : 'password', " + NEW_LINE);
		}

		// Handle selects
		else if (getFieldType() == FieldType.SELECT) {
			result.append(" onblur: 'cancel', " + NEW_LINE);
			result.append(" submit: 'Ok', " + NEW_LINE);
			result.append(" type : 'select', " + NEW_LINE);
			result.append(" cssclass : 'inlineSelect', " + NEW_LINE);
			result.append(" loadurl: '" + getUrl() + "'");
		}
		// handles select yes no
		else if (getFieldType() == FieldType.YESNO) {
			result.append(" onblur: 'cancel', " + NEW_LINE);
			result.append(" submit: 'Ok', " + NEW_LINE);
			result.append(" type : 'select', " + NEW_LINE);
			result.append(" cssclass : 'inlineSelect', " + NEW_LINE);
			result.append(" data: \"{'1':'Yes','0':'No'}\"");

		}

		else if (getFieldType() == FieldType.TEXTAREA) {
			result.append(" type : 'textarea', " + NEW_LINE);
			result.append(" onblur: 'cancel', " + NEW_LINE);
			result.append(" submit: 'Ok', " + NEW_LINE);
			result.append(" cssclass : 'inlineSelect', " + NEW_LINE);
		}

		// Remove possible trailing commas
		String returnValueMinusTrailingComma = result.toString().trim().replaceAll(",$", "");

		return returnValueMinusTrailingComma + "},";
	}

	@Override
	public Object getValue() {
		if (getFieldType() == FieldType.DATE) {
			return handleDates(super.getValue());
		}
		return super.getValue();
	}

	@Override
	public void setDisplayValue(String displayValue) {
		super.setDisplayValue(displayValue);
	}

    private String prepareDisplayValue(Object displayValue) {
        if (getFieldType() == FieldType.BUTTON) {
            return "<a href='"
                    + getUrl()
                    + "' role='button' class='button ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only'><span class='ui-button-text'>"
                    + displayValue.toString() + "</span></a>";
        }

        if (getFieldType() == FieldType.BUTTONONCLICK) {
            return "<button type='button' onclick='" + getUrl() + "' class='btn btn-primary'>" + displayValue.toString() + "</button>";
        }

        if (getFieldType() == FieldType.DELETEROWBUTTON) {
//			return "<a class='table-action-deletelink ui-icon ui-icon-trash'>Delete</a>";
            return "<a href='#' class='table-action-deletelink'><i class='icon-trash'></i> Delete</a>";
        }

        if (getFieldType() == FieldType.EDITROWBUTTON) {
//			return "<a class='table-action-EditData ui-icon ui-icon-pencil'>Edit</a>";
            return "<a class='table-action-EditData'><i class='icon-pencil'></i> Edit</a>";
        }

        if (getFieldType() == FieldType.DELETEANDEDITROWBUTTON) {
            return "<a id='" + getValue() + "' class='table-action-deletelink'><i class='icon-trash'></i> Delete</a>"
                    + "<a id='table-action-EditData' class='table-action-EditData'><i class='icon-pencil'></i> Edit</a>";
        }

        if (getUrl() != null && !getUrl().isEmpty() && getFieldType() != FieldType.SELECT) {
            return "<a href='" + getUrl() + "' target='_blank'>" + displayValue.toString() + "</a>";
        }

        String result;

        if (displayValue == null) {
            result = "";
        } else if (displayValue instanceof Date) {
            result = prepareDateDisplayValue(displayValue);
        } else if (displayValue instanceof DateTime) {
            result = prepareDateDisplayValue(displayValue);
        } else if (displayValue instanceof BigDecimal) {
            result = prepareDateDisplayValue(displayValue);
        } else if (displayValue instanceof XMLGregorianCalendar) {
            XMLGregorianCalendar xmlGregorianCalendar = (XMLGregorianCalendar) displayValue;
            result = prepareDateDisplayValue(Util.xmlGregorianCalToDate(xmlGregorianCalendar));
        } else {
            result = displayValue.toString();
        }

        if(this.getIsListInput()) {
            return new StringBuilder("<input type=\"hidden\" name=\"").append(getFieldName())
                    .append("\" id=\"").append(getFieldName()).append("\" value=\"").append(result).append("\" />").toString();
        }

        return result;
    }

	private String prepareDateDisplayValue(Object date) {
		return Util.prepareDisplayValue(date, dateFormat);
	}

	private Object handleDates(Object date) {
		if (date instanceof Date) {
			return (Date) date;
		}

		if (date instanceof DateTime) {
			return (DateTime) date;
		}

		if (date instanceof XMLGregorianCalendar) {
			XMLGregorianCalendar gregCal = (XMLGregorianCalendar) date;
			return Util.xmlGregorianCalToDate(gregCal);
		}

		String dateString = "" + date;

		if (date == null || dateString.trim().isEmpty()) {
			return null;
		}

		DateFormat df = new SimpleDateFormat(dateFormat);// TODO: move to util?

		Date dateObj = null;
		try {
			dateObj = df.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dateObj;
	}
}
