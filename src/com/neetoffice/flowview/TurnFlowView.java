package com.neetoffice.flowview;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Transformation;

public class TurnFlowView extends FlowView {
	private Camera camera = new Camera();
	private int maxRotationAngle = 60;
	private float zoom = 0;

	public TurnFlowView(Context context) {
		super(context);
		setStaticTransformationsEnabled(true);
	}

	public TurnFlowView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setStaticTransformationsEnabled(true);
	}

	public TurnFlowView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setStaticTransformationsEnabled(true);
	}

	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {
		final int scrollX = getScrollX();
		final int scrollY = getScrollY();
		final int childLeft = child.getLeft();
		final int childTop = child.getTop();
		final int horizontalChildCenter = (child.getTop() + child.getBottom()) / 2;
		final int verticalChildCenter = (child.getLeft() + child.getRight()) / 2;
		final int childWidth = child.getWidth();
		final int childHeight = child.getHeight();

		t.clear();
		t.setTransformationType(Transformation.TYPE_MATRIX);

		switch (getTranslate()) {
		case Up:
			if (scrollY > childTop) {
				int rotationAngle = (int) (((scrollY+childHeight/2) - horizontalChildCenter)/(float)childHeight*maxRotationAngle);
				if (Math.abs(rotationAngle) > maxRotationAngle) {
					rotationAngle = (rotationAngle < 0) ? -maxRotationAngle : maxRotationAngle;
				}
				transformImageBitmap(child, t, rotationAngle,0);	
			}
			break;
		case Down:
			if (scrollY < childTop) {
				int rotationAngle = (int) (((scrollY+childHeight/2) - horizontalChildCenter)/(float)childHeight*maxRotationAngle);
				if (Math.abs(rotationAngle) > maxRotationAngle) {
					rotationAngle = (rotationAngle < 0) ? -maxRotationAngle : maxRotationAngle;
				}
				transformImageBitmap(child, t, rotationAngle,0);	
			}
			break;
		case Right:
			if (scrollX < childLeft) {
				int rotationAngle = (int) (((scrollX+childWidth/2) - verticalChildCenter)/(float)childWidth*maxRotationAngle);
				if (Math.abs(rotationAngle) > maxRotationAngle) {
					rotationAngle = (rotationAngle < 0) ? -maxRotationAngle : maxRotationAngle;
				}
				transformImageBitmap(child, t, 0,-rotationAngle);	
			}
			break;
		case Left:
			if (scrollX > childLeft) {
				int rotationAngle = (int) (((scrollX+childWidth/2) - verticalChildCenter)/(float)childWidth*maxRotationAngle);
				if (Math.abs(rotationAngle) > maxRotationAngle) {
					rotationAngle = (rotationAngle < 0) ? -maxRotationAngle : maxRotationAngle;
				}
				transformImageBitmap(child, t, 0,-rotationAngle);	
			}
			break;
		}
		return true;

	}
	
	private void transformImageBitmap(View child, Transformation t,int rotationAngleX,int rotationAngleY) {
		camera.save();
		final Matrix matrix = t.getMatrix();
		final int height = child.getHeight();
		final int width = child.getWidth();
		final int rotationX = Math.abs(rotationAngleX);
		final int rotationY = Math.abs(rotationAngleY);
		camera.translate(0.0f, 0.0f, zoom);

		camera.rotateX(rotationAngleX);
		camera.rotateY(rotationAngleY);
		camera.getMatrix(matrix);
		if (rotationX <= maxRotationAngle || rotationY <= maxRotationAngle ) {
			switch(getTranslate()){
			case Up:
				matrix.preTranslate(-(width / 2), -height);
				matrix.postTranslate((width / 2), height);	
				break;
			case Down:
				matrix.preTranslate(-(width / 2), 0);
				matrix.postTranslate((width / 2), 0);	
				break;
			case Right:
				matrix.preTranslate(0, -(height/2));
				matrix.postTranslate(0, (height/2));	
				break;
			case Left:	
				matrix.preTranslate(-width, -(height/2));
				matrix.postTranslate(width, (height/2));	
				break;
			}
		}
		camera.restore();
	}
}
