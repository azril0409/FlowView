package com.neetoffice.flowview;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Transformation;
public class CircleTurnFlowView extends FlowView {
	private Camera camera = new Camera();
	private int maxRotationAngle = 60;
	private float zoom = 0;

	public CircleTurnFlowView(Context context) {
		super(context);
		setStaticTransformationsEnabled(true);
	}

	public CircleTurnFlowView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setStaticTransformationsEnabled(true);
	}

	public CircleTurnFlowView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setStaticTransformationsEnabled(true);
	}

	public float getZoom() {
		return zoom;
	}

	public void setZoom(float zoom) {
		this.zoom = zoom;
	}

	public int getMaxRotationAngle() {
		return maxRotationAngle;
	}

	public void setMaxRotationAngle(int maxRotationAngle) {
		this.maxRotationAngle = maxRotationAngle;
	}
	
	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {
		final int scrollX = getScrollX();
		final int scrollY = getScrollY();
		final int childLeft = child.getLeft();
		final int childTop = child.getTop();
		final int horizontalChildCenter = (child.getTop()+child.getBottom())/2;
		final int verticalChildCenter = (child.getLeft()+child.getRight())/2;
		final int childWidth = child.getWidth();
		final int childHeight = child.getHeight();
		
		t.clear();
		t.setTransformationType(Transformation.TYPE_MATRIX);

		switch(getTranslate()){
		case Up:
		case Down:			
			if (scrollY == childTop) {
				transformImageBitmap(child, t, 0,0);
			} else {
				int rotationAngle = (int) (((scrollY+childHeight/2) - horizontalChildCenter)/(float)childHeight*maxRotationAngle);
				if (Math.abs(rotationAngle) > maxRotationAngle) {
					rotationAngle = (rotationAngle < 0) ? -maxRotationAngle : maxRotationAngle;
				}
				transformImageBitmap(child, t, rotationAngle,0);				
			}
			break;
		case Right:
		case Left:			
			if (scrollX == childLeft) {
				transformImageBitmap(child, t,0, 0);
			} else {
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
		// 如视图的角度更少,放大			
		if (rotationX <= maxRotationAngle || rotationY <= maxRotationAngle ) {
			double a = 1.0;
			switch(getTranslate()){
			case Up:
			case Down:
				a=getHeight()/2.0;
				break;
			case Right:
			case Left:	
				a=getWidth()/2.0;
				break;
			}
			//float zoomAmount = (float) (maxZoom + (Math.max(rotationX, rotationY)*Math.PI));
			float zoomAmount = (float)(Math.sin(Math.PI*Math.max(rotationX, rotationY)/360.0)*a);
			//float zoomAmount = (float)(maxZoom+(Math.max(rotationX, rotationY)/maxRotationAngle*a/1.414));
			camera.translate(0, 0, zoomAmount);			
		}

		camera.rotateX(rotationAngleX);
		camera.rotateY(rotationAngleY);
		camera.getMatrix(matrix);
		matrix.preTranslate(-(width / 2), -(height / 2));
		matrix.postTranslate((width / 2), (height / 2));	
		camera.restore();
	}

}
