package com.github.barteksc.pdfviewer.calculator;

import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.shockwave.pdfium.util.Size;

/**
 * Page size calculator arguments
 */
public class PageSizeCalculatorArgs {
    /**
     * Page fit policy
     */
    public final FitPolicy fitPolicy;

    /**
     * Page with greatest width original size
     */
    public final Size originalMaxWidthPageSize;

    /**
     * Page with greatest height original size
     */
    public final Size originalMaxHeightPageSize;

    /**
     * View (Canvas) size
     */
    public final Size viewSize;

    /**
     * Indicates if each page should fit the view (canvas)
     */
    public final boolean fitEachPage;

    public PageSizeCalculatorArgs(FitPolicy fitPolicy, Size originalMaxWidthPageSize,
                                  Size originalMaxHeightPageSize, Size viewSize, boolean fitEachPage) {
        this.fitPolicy = fitPolicy;
        this.originalMaxWidthPageSize = originalMaxWidthPageSize;
        this.originalMaxHeightPageSize = originalMaxHeightPageSize;
        this.viewSize = viewSize;
        this.fitEachPage = fitEachPage;
    }
}
