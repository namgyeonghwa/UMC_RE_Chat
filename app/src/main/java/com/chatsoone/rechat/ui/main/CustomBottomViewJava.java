package com.chatsoone.rechat.ui.main;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;

import com.google.android.material.bottomnavigation.BottomNavigationView;

// BottomNavigationView 상속하는 CustomBottomView 생성, xml 확인
public class CustomBottomViewJava extends BottomNavigationView {
    private Path mPath;
    private Paint mPaint;

    // 가운데 버튼의 반지름
    private final int CURVED_CIRCLE_RADIUS=256/2;

    // 첫번째 곡선 그리기
    private Point mFirstCurveStartPoint=new Point();
    private Point mFirstCurveEndPoint=new Point();
    private Point mFirstCurveControlPoint1=new Point();
    private Point mFirstCurveControlPoint2=new Point();

    // 두번째 곡선 그리기
    private Point mSecondCurveStartPoint=new Point();
    private Point mSecondCurveEndPoint=new Point();
    private Point mSecondCurveControlPoint1=new Point();
    private Point mSecondCurveControlPoint2=new Point();

    private int mNavigationBarWidth;
    private int mNavigationBarHeight;

    public CustomBottomViewJava(Context context){
        super(context);
        init();
    }
    public CustomBottomViewJava(Context context, AttributeSet attrs){
        super(context, attrs);
        init();
    }
    public CustomBottomViewJava(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init(){
        mPath=new Path();
        mPaint=new Paint();
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setShadowLayer(3, 0, 0, Color.GRAY);
        mPaint.setColor(Color.WHITE); // 곡선 도형 채운 색상 하얀색
        setBackgroundColor(Color.TRANSPARENT); // 배경 투명색
        setLayerType(LAYER_TYPE_SOFTWARE, mPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);

        // navigation bar 의 w, h 가져오기
        mNavigationBarWidth=getWidth();
        mNavigationBarHeight=getHeight();

        // 첫번째 곡선의 시작점 (x, y) 설정
        mFirstCurveStartPoint.set((mNavigationBarWidth/2)-(CURVED_CIRCLE_RADIUS*2)-(CURVED_CIRCLE_RADIUS / 3), 65);
        // 첫번째 곡선의 끝점 (x, y) 설정
        mFirstCurveEndPoint.set(mNavigationBarWidth/2, 8);
        // 두번째 곡선의 시작점 (x, y) 설정, 첫번째 곡선의 끝점과 동일
        mSecondCurveStartPoint=mFirstCurveEndPoint;
        // 두번째 곡선의 끝점 (x, y) 설정
        mSecondCurveEndPoint.set((mNavigationBarWidth/2)+(CURVED_CIRCLE_RADIUS*2)+(CURVED_CIRCLE_RADIUS/3), 65);

        // 첫번째 곡선의 베지어 첫번째 점 설정
        mFirstCurveControlPoint1.set(mFirstCurveStartPoint.x+(CURVED_CIRCLE_RADIUS * 2)-CURVED_CIRCLE_RADIUS/2, mFirstCurveStartPoint.y);
        // 첫번째 곡선의 베지어 두번째 점 설정
        mFirstCurveControlPoint2.set(mFirstCurveEndPoint.x-CURVED_CIRCLE_RADIUS+CURVED_CIRCLE_RADIUS/4, mFirstCurveEndPoint.y);
        // 두번째 곡선의 베지어 첫번째 점 설정
        mSecondCurveControlPoint1.set(mSecondCurveStartPoint.x+CURVED_CIRCLE_RADIUS-CURVED_CIRCLE_RADIUS/4, mSecondCurveStartPoint.y);
        // 두번째 곡선의 베지어 두번째 점 설정
        mSecondCurveControlPoint2.set(mSecondCurveEndPoint.x-(CURVED_CIRCLE_RADIUS*2)+CURVED_CIRCLE_RADIUS/2, mSecondCurveEndPoint.y);

        mPath.reset();
        mPath.moveTo(0,65);
        mPath.lineTo(mFirstCurveStartPoint.x, mFirstCurveStartPoint.y);

        mPath.cubicTo(mFirstCurveControlPoint1.x, mFirstCurveControlPoint1.y, mFirstCurveControlPoint2.x, mFirstCurveControlPoint2.y, mFirstCurveEndPoint.x, mFirstCurveEndPoint.y);
        mPath.cubicTo(mSecondCurveControlPoint1.x, mSecondCurveControlPoint1.y, mSecondCurveControlPoint2.x, mSecondCurveControlPoint2.y, mSecondCurveEndPoint.x, mSecondCurveEndPoint.y);

        mPath.lineTo(mNavigationBarWidth, 65);
        mPath.lineTo(mNavigationBarWidth, mNavigationBarHeight);
        mPath.lineTo(0, mNavigationBarHeight);
        mPath.close();
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        canvas.drawPath(mPath, mPaint);
    }
}
