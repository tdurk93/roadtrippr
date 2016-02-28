package roadtrippr.roadtrippr;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

/**
 * Created by keattz on 2/28/16.
 */
public class ZoomView extends LinearLayout {

    private float sf = 1.3f;

    public ZoomView (final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setStaticTransformationsEnabled(true);
    }

    public ZoomView(final Context context) {
        super(context);
        setStaticTransformationsEnabled(true);
    }

    public void setScaling(final float sf) {
        this.sf = sf;
    }

    @Override
    protected boolean getChildStaticTransformation(final View child, final Transformation t) {
        t.clear();
        t.setTransformationType(Transformation.TYPE_MATRIX);
        final Matrix m = t.getMatrix();
        m.setScale(this.sf, this.sf);
        return true;
    }

}