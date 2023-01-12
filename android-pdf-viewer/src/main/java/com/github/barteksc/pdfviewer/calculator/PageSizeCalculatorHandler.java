package com.github.barteksc.pdfviewer.calculator;

import com.shockwave.pdfium.util.Size;
import com.shockwave.pdfium.util.SizeF;

/**
 * Customisable PageSizeCalculator handler
 */
public interface PageSizeCalculatorHandler {
    /**
     * Gets optimal size for page with greatest width
     *
     * @param args calculator handler parameters
     * @return
     */
    SizeF getOptimalMaxWidthPageSize(PageSizeCalculatorArgs args);

    /**
     * Gets optimal size for page with greatest height
     *
     * @param args calculator handler parameters
     * @return
     */
    SizeF getOptimalMaxHeightPageSize(PageSizeCalculatorArgs args);

    /**
     * Gets scaled page size
     *
     * @param pageSize original page size
     * @param args calculator handler args
     * @return
     */
    SizeF calculate(Size pageSize, PageSizeCalculatorArgs args);
}
