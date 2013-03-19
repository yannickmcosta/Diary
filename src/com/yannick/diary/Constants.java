package com.yannick.diary;

public class Constants {
	
	public static final int REQUEST_CODE_TAKE_PICTURE = 1002;

	private Constants(){}
	
	public static final String DIARY_ARRAY = "diaryArrayData";
	public static final String DIARY_ENTRY = "diaryEntry";
	
	public static final String DIARY_EXTRA_FILENAME = "com.yannick.diary.filename";
	/** If true,causes the service to save to a local file */
	public static final String DIARY_EXTRA_EXPORT_TO_CSV = "export_to_csv";

	
	/** Preferences */
	public static final String PREF_SERVER_PUT_URL = "sync_server_put_url";
	public static final String PREF_SERVER_GET_URL = "sync_server_get_url";
	public static final String DIARY_ID = "diary_id";
	public static final long DIARY_ITEM_UNDEFINED = -1;
	
	/** keys for Intent extras for the simple approach taken in exercise 4.2 */
	public static final String DIARY_DESCRIPTION = "diary_description";
	public static final String DIARY_AMOUNT = "diary_amount";
	public static final String DIARY_DATE = "diary_date";
	
	
	public static final int REQUEST_CODE_EDIT = 1;
	protected static final int DATE_DIALOG_ID = 100;
	
	public static final String DEFAULT_CSV_FILENAME = "diary.csv";;

	/** Action to launch the simulated camera activity */
	protected static final String ACTION_IMAGE_CAPTURE = "com.yannick.diary.CatureImage";
}
