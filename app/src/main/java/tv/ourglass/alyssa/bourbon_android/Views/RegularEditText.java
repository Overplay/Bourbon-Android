package tv.ourglass.alyssa.bourbon_android.Views;

import android.content.Context;
import android.graphics.Typeface;
import android.support.design.widget.TextInputEditText;
import android.util.AttributeSet;

import tv.ourglass.alyssa.bourbon_android.Model.OGConstants;

/**
 * Created by atorres on 3/1/17.
 */

public class RegularEditText extends TextInputEditText {

    public RegularEditText(Context context) {
        super(context);
        setFont(context);
    }

    public RegularEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFont(context);
    }

    private void setFont(Context c) {
        Typeface font = Typeface.createFromAsset(c.getAssets(), OGConstants.regularFont);
        setTypeface(font);
    }
}
