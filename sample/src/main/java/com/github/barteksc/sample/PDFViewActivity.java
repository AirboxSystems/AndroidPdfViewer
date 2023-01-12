/**
 * Copyright 2016 Bartosz Schiller
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.barteksc.sample;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.net.Uri;
import android.provider.OpenableColumns;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnDrawListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.util.Size;
import com.shockwave.pdfium.util.SizeF;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.NonConfigurationInstance;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.options)
public class PDFViewActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener,
        OnPageErrorListener, OnDrawListener, View.OnTouchListener {

    private static final String TAG = PDFViewActivity.class.getSimpleName();

    private boolean mIsAnnotateEnabled;
    private Path mAnnotationDrawingPath;
    private List<PointF> mAnnotationDrawingPoints;
    private  AnnotateMode mAnnotateMode;
    private final List<TAnnotation> mAnnotations = new ArrayList<TAnnotation>();
    private final PointF mLastTouchPoint = new PointF();
    private final PointF mStartTouchPoint = new PointF();

    private static final double CLICK_SIZE = 3;
    private final static int REQUEST_CODE = 42;
    public static final int PERMISSION_CODE = 42042;

    public static final String SAMPLE_FILE = "sample.pdf";
    public static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";

    @ViewById
    AbxPdfView pdfView;

    @NonConfigurationInstance
    Uri uri;

    @NonConfigurationInstance
    Integer pageNumber = 0;

    String pdfFileName;

    @OptionsItem(R.id.pickFile)
    void pickFile() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{READ_EXTERNAL_STORAGE},
                    PERMISSION_CODE
            );

            return;
        }

        launchPicker();
    }

    @OptionsItem(R.id.annotate)
    void annotate() {
        mIsAnnotateEnabled = !mIsAnnotateEnabled;
        mAnnotateMode = AnnotateMode.NONE;
    }

    void launchPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            //alert user that file manager not working
            Toast.makeText(this, R.string.toast_pick_file_error, Toast.LENGTH_SHORT).show();
        }
    }

    @AfterViews
    void afterViews() {
        pdfView.setBackgroundColor(Color.LTGRAY);
        pdfView.setOnTouchInterceptor(this);
        if (uri != null) {
            displayFromUri(uri);
        } else {
            displayFromAsset(SAMPLE_FILE);
        }
        setTitle(pdfFileName);
    }

    private void displayFromAsset(String assetFileName) {
        pdfFileName = assetFileName;

        pdfView.fromAsset(SAMPLE_FILE)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .pageSizeCalculatorHandler(new EqualBoxPageSizeCalculator())
                .spacing(1) // in dp
                .onPageError(this)
                .onDraw(this)
                .load();
    }

    private void displayFromUri(Uri uri) {
        pdfFileName = getFileName(uri);

        pdfView.fromUri(uri)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .pageSizeCalculatorHandler(new EqualBoxPageSizeCalculator())
                .spacing(1) // in dp
                .onPageError(this)
                .onDraw(this)
                .load();
    }

    @OnActivityResult(REQUEST_CODE)
    public void onResult(int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            uri = intent.getData();
            displayFromUri(uri);
        }
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        Log.e(TAG, "title = " + meta.getTitle());
        Log.e(TAG, "author = " + meta.getAuthor());
        Log.e(TAG, "subject = " + meta.getSubject());
        Log.e(TAG, "keywords = " + meta.getKeywords());
        Log.e(TAG, "creator = " + meta.getCreator());
        Log.e(TAG, "producer = " + meta.getProducer());
        Log.e(TAG, "creationDate = " + meta.getCreationDate());
        Log.e(TAG, "modDate = " + meta.getModDate());

        printBookmarksTree(pdfView.getTableOfContents(), "-");

    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

    /**
     * Listener for response to user permission request
     *
     * @param requestCode  Check that permission request code matches
     * @param permissions  Permissions that requested
     * @param grantResults Whether permissions granted
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchPicker();
            }
        }
    }

    @Override
    public void onPageError(int page, Throwable t) {
        Log.e(TAG, "Cannot load page " + page);
    }

    @Override
    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
        // Draw in progress annotation (currently drawn by user)
        if (mAnnotationDrawingPath != null) {
            Paint paint1 = new Paint();
            paint1.setStyle(Paint.Style.STROKE);
            paint1.setStrokeWidth(10);
            paint1.setColor(Color.BLACK);
            canvas.drawPath(mAnnotationDrawingPath, paint1);
        }

        // Draw cached annotations
        Paint paint2 = new Paint();
        paint2.setStyle(Paint.Style.STROKE);
        paint2.setStrokeWidth(10);
        paint2.setColor(Color.RED);

        Log.d("OnDrawLayer", "############ ANNOTATIONS #################");

        for (int i = 0; i < mAnnotations.size(); i++) {
            Path path = new Path();
            TAnnotation a = mAnnotations.get(i);
            PointF startPoint = a.getPoints().get(0);
            float documentWidth = pdfView.getDocumentSize().getWidth();
            float documentHeight = pdfView.getDocumentSize().getHeight();
            float pageXOffset = pdfView.getPageOffset(displayedPage).x;
            float pageYOffset = pdfView.getPageOffset(displayedPage).y;
            float startX = startPoint.x * documentWidth - pageXOffset;
            float startY = startPoint.y * documentHeight - pageYOffset;
            path.moveTo(startX, startY);

            Log.d("OnDrawLayer", String.format("annotationIndex = %s", i));
            Log.d("OnDrawLayer", String.format("startPoint = {%s, %s}", startX, startY));

            for (int j = 1; j < a.getPoints().size(); j++)
            {
                PointF endPoint = a.getPoints().get(j);
                float endX = endPoint.x * documentWidth - pageXOffset;
                float endY = endPoint.y * documentHeight - pageYOffset;
                path.lineTo(endX, endY);

                Log.d("OnDrawLayer", String.format("endPoint = {%s, %s}", endX, endY));
            }

            canvas.drawPath(path, paint2);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (!mIsAnnotateEnabled) {
            return false;
        }

        PointF touchPoint0 = new PointF(motionEvent.getX(), motionEvent.getY());

        SizeF documentSize = pdfView.getDocumentSize();
        PointF touchPagePoint = convertTouchToPagePoint(pdfView, touchPoint0);
        PointF touchDocumentPoint = convertPageToDocumentPoint(pdfView, touchPagePoint);
        PointF touchDocumentUniversalPoint = new PointF(touchDocumentPoint.x / documentSize.getWidth(), touchDocumentPoint.y / documentSize.getHeight());

        Log.d("onTouch", "#############################################################");
        Log.d("onTouch", String.format("touchPoint = %s", touchPoint0.toString()));
        Log.d("onTouch", String.format("touchPagePoint = %s", touchPagePoint.toString()));
        Log.d("onTouch", String.format("touchDocumentPoint = %s", touchDocumentPoint.toString()));
        Log.d("onTouch", String.format("touchDocumentUniversalPoint = %s", touchDocumentUniversalPoint.toString()));

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastTouchPoint.set(touchPoint0);
                mStartTouchPoint.set(mLastTouchPoint);

                if (mIsAnnotateEnabled && mAnnotateMode == AnnotateMode.NONE) {
                    mAnnotationDrawingPath = new Path();
                    mAnnotationDrawingPath.moveTo(touchPagePoint.x, touchPagePoint.y);

                    mAnnotationDrawingPoints = new ArrayList<PointF>();
                    mAnnotationDrawingPoints.add(touchDocumentUniversalPoint);

                    mAnnotateMode = AnnotateMode.DRAW;
                } else if (mAnnotateMode == AnnotateMode.NONE) {
                    mAnnotateMode = AnnotateMode.DRAG;
                }

                break;

            case MotionEvent.ACTION_MOVE:
                if (mAnnotateMode == AnnotateMode.DRAG) {
                    mLastTouchPoint.set(touchPoint0.x, touchPoint0.y);
                } else if (mAnnotateMode == AnnotateMode.DRAW) {
                    mAnnotationDrawingPath.lineTo(touchPagePoint.x, touchPagePoint.y);
                    mAnnotationDrawingPoints.add(touchDocumentUniversalPoint);

                    pdfView.postInvalidate();
                }

                break;

            case MotionEvent.ACTION_UP:
                float xDiff = Math.abs(touchPoint0.x - mStartTouchPoint.x);
                float yDiff = Math.abs(touchPoint0.y - mStartTouchPoint.y);

                if (xDiff < CLICK_SIZE && yDiff < CLICK_SIZE) {
                    pdfView.performClick();
                } else if (mAnnotateMode == AnnotateMode.DRAW) {
                    mAnnotationDrawingPoints.add(touchDocumentUniversalPoint);

                    mAnnotations.add(new TAnnotation(mAnnotationDrawingPoints));

                    mAnnotationDrawingPath = null;
                }

                mAnnotateMode = AnnotateMode.NONE;

                break;

            case MotionEvent.ACTION_POINTER_UP:
                mAnnotateMode = AnnotateMode.NONE;

                break;
        }

        pdfView.invalidate();
        return  true;
    }

    private  PointF convertTouchToPagePoint(PDFView view, PointF touchPoint) {
        float mappedX = view.toRealScale(-view.getCurrentXOffset() + touchPoint.x);
        float mappedy = view.toRealScale(-view.getCurrentYOffset() + touchPoint.y);
        PointF currentPageOffset = view.getPageOffset(view.getCurrentPage(), 1);

        mappedX = (mappedX - currentPageOffset.x) * view.getZoom();
        mappedy = (mappedy - currentPageOffset.y) * view.getZoom();

        PointF pagePoint = new PointF(mappedX, mappedy);

        return pagePoint;
    }

    private  PointF convertPageToDocumentPoint(PDFView view, PointF pagePoint) {
        PointF currentPageOffset = view.getPageOffset(view.getCurrentPage());
        PointF documentPoint = new PointF(pagePoint.x + currentPageOffset.x, pagePoint.y + currentPageOffset.y);
        return  documentPoint;
    }

    private enum AnnotateMode
    {
        NONE,
        DRAG,
        ZOOM,
        DRAW,
        DELETE
    }

    private class TAnnotation
    {
        private List<PointF> mPoints;

        public TAnnotation(List<PointF> points)
        {
            mPoints = points;
        }

        public List<PointF> getPoints()
        {
            return  mPoints;
        }
    }
}
