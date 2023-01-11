package com.github.barteksc.sample;

import com.github.barteksc.pdfviewer.calculator.PageSizeCalculatorArgs;
import com.github.barteksc.pdfviewer.calculator.PageSizeCalculatorHandler;
import com.shockwave.pdfium.util.Size;
import com.shockwave.pdfium.util.SizeF;

/**
 * Scales pages so they all fit inside the container of equal with and height
 */
public class EqualBoxPageSizeCalculator implements PageSizeCalculatorHandler {
    /**
     * Gets optimal size for page with greatest width
     *
     * @param args calculator handler parameters
     * @return
     */
    @Override
    public SizeF getOptimalMaxWidthPageSize(PageSizeCalculatorArgs args) {
        float width = args.originalMaxWidthPageSize.getWidth();
        float height = args.originalMaxWidthPageSize.getHeight();
        float maxWidth = args.viewSize.getWidth();
        float ratio = width / height;

        width = maxWidth;
        height = (float) Math.floor(maxWidth / ratio);

        return new SizeF(width, height);
    }

    /**
     * Gets optimal size for page with greatest height
     *
     * @param args calculator handler parameters
     * @return
     */
    @Override
    public SizeF getOptimalMaxHeightPageSize(PageSizeCalculatorArgs args) {
        return getOptimalMaxWidthPageSize(args);
    }

    /**
     * Gets scaled page size
     *
     * @param pageSize original page size
     * @param args calculator handler args
     * @return
     */
    @Override
    public SizeF calculate(Size pageSize, PageSizeCalculatorArgs args) {
        float width = pageSize.getWidth();
        float height = pageSize.getHeight();

        if (width <= 0 || height <= 0) {
            return new SizeF(0, 0);
        }

        float ratio = width / height;
        SizeF optimalMaxWidthPageSize = getOptimalMaxWidthPageSize(args);
        float maxWidth = optimalMaxWidthPageSize.getWidth();
        float maxHeight = optimalMaxWidthPageSize.getHeight();

        width = maxWidth;
        height = (float) Math.floor(maxWidth / ratio);

        if (height > maxHeight) {
            height = maxHeight;
            width = (float) Math.floor(maxHeight * ratio);
        }
        return new SizeF(width, height);
    }
}
