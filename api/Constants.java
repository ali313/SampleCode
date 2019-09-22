package ir.nabaksoft.office.api;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Ali on 7/1/2019.
 */

public class Constants
{
    @IntDef({TYPE_RECEIVED, TYPE_SEND})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LetterType {}

    @IntDef({TYPE_RECEIVED, TYPE_SEND})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MessageType {}

    public static final int TYPE_RECEIVED = 0;
    public static final int TYPE_SEND = 1;


    @IntDef({STATE_CURRENT, STATE_DELETED,STATE_ARCHIVED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LetterState {}
    //public @interface MessageState {}
    public static final int STATE_CURRENT = 0;
    public static final int STATE_DELETED = 1;
    public static final int STATE_ARCHIVED = 2;


    public static final int SENT_STATE_SENT = 1;
    public static final int SENT_STATE_TEMP = 2;
}
