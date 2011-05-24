package sk.gista.medobs.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.widget.Button;

public class SymbolButton extends Button {
	// fields
	private static final int iColor = 0xff777777;
	private static final int iColorActive = 0xff442200;

	// fields
	public enum symbol {
		none, arrowLeft, arrowRight
	};

	// fields
	private Paint pt = new Paint();
	private RectF rect = new RectF();
	private RectF rectDraw = new RectF();
	private symbol symbolType = symbol.none;
	private float density;

	// methods
	public SymbolButton(Context context, symbol symbolType) {
		super(context);
		this.symbolType = symbolType;
		density = getResources().getDisplayMetrics().density;
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		pt.setAntiAlias(true);
		pt.setStrokeCap(Paint.Cap.ROUND);

		rectDraw.set(0, 0, getWidth(), getHeight());
		rectDraw.left += 6 * density;
		rectDraw.right -= 6 * density;
		rectDraw.top += 4 * density;
		rectDraw.bottom -= 8 * density;

		if (symbolType != symbol.none) {
			pt.setStrokeWidth(5 * density);

			pt.setColor(iColor);
			if (this.isPressed() || this.isFocused())
				pt.setColor(iColorActive);

			drawArrow(canvas);
		}
	}

	private void drawArrow(Canvas canvas) {
		rect.set(rectDraw);
		rect.inset(15 * density, 5 * density);
		float c = 6 * density;
		canvas.drawLine(rect.left, rect.centerY(), rect.right, rect.centerY(), pt);
		if (symbolType == symbol.arrowRight) {
			canvas.drawLine(rect.right, rect.centerY(), rect.right - c, rect.top, pt);
			canvas.drawLine(rect.right, rect.centerY(), rect.right - c, rect.bottom, pt);
		}
		if (symbolType == symbol.arrowLeft) {
			canvas.drawLine(rect.left, rect.centerY(), rect.left + c, rect.top, pt);
			canvas.drawLine(rect.left, rect.centerY(), rect.left + c, rect.bottom, pt);
		}
	}

}
