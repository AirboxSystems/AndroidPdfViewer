package com.github.barteksc.pdfviewer.calculator;

import com.shockwave.pdfium.util.Size;
import com.shockwave.pdfium.util.SizeF;

/**
 * Default PageSizeCalculator handler
 */
public class DefaultPageSizeCalculatorHandler implements PageSizeCalculatorHandler {
    /**
     * Gets optimal size for page with greatest width
     *
     * @param args calculator handler parameters
     * @return
     */
    @Override
    public SizeF getOptimalMaxWidthPageSize(PageSizeCalculatorArgs args) {
        return calculateMaxPages(args).optimalMaxWidthPageSize;
    }

    /**
     * Gets optimal size for page with greatest height
     *
     * @param args calculator handler parameters
     * @return
     */
    @Override
    public SizeF getOptimalMaxHeightPageSize(PageSizeCalculatorArgs args) {
        return calculateMaxPages(args).optimalMaxHeightPageSize;
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
        if (pageSize.getWidth() <= 0 || pageSize.getHeight() <= 0) {
            return new SizeF(0, 0);
        }

        CalculateMaxPagesResult result = calculateMaxPages(args);
        float maxWidth = args.fitEachPage ? args.viewSize.getWidth() : pageSize.getWidth() * result.widthRatio;
        float maxHeight = args.fitEachPage ? args.viewSize.getHeight() : pageSize.getHeight() * result.heightRatio;

        switch (args.fitPolicy) {
            case HEIGHT:
                return fitHeight(pageSize, maxHeight);
            case BOTH:
                return fitBoth(pageSize, maxWidth, maxHeight);
            default:
                return fitWidth(pageSize, maxWidth);
        }
    }

    /**
     * Calculates optimal page sizes, width and height scale ratios
     *
     * @param args calculator handler args
     * @return
     */
    private CalculateMaxPagesResult calculateMaxPages(PageSizeCalculatorArgs args) {
        SizeF optimalMaxWidthPageSize;
        SizeF optimalMaxHeightPageSize;
        float widthRatio = 0;
        float heightRatio = 0;

        switch (args.fitPolicy) {
            case HEIGHT:
                optimalMaxHeightPageSize = fitHeight(args.originalMaxHeightPageSize, args.viewSize.getHeight());
                heightRatio = optimalMaxHeightPageSize.getHeight() / args.originalMaxHeightPageSize.getHeight();
                optimalMaxWidthPageSize = fitHeight(args.originalMaxWidthPageSize, args.originalMaxWidthPageSize.getHeight() * heightRatio);
                break;
            case BOTH:
                SizeF localOptimalMaxWidth = fitBoth(args.originalMaxWidthPageSize, args.viewSize.getWidth(), args.viewSize.getHeight());
                float localWidthRatio = localOptimalMaxWidth.getWidth() / args.originalMaxWidthPageSize.getWidth();
                optimalMaxHeightPageSize = fitBoth(args.originalMaxHeightPageSize, args.originalMaxHeightPageSize.getWidth() * localWidthRatio,
                        args.viewSize.getHeight());
                heightRatio = optimalMaxHeightPageSize.getHeight() / args.originalMaxHeightPageSize.getHeight();
                optimalMaxWidthPageSize = fitBoth(args.originalMaxWidthPageSize, args.viewSize.getWidth(), args.originalMaxWidthPageSize.getHeight() * heightRatio);
                widthRatio = optimalMaxWidthPageSize.getWidth() / args.originalMaxWidthPageSize.getWidth();
                break;
            default:
                optimalMaxWidthPageSize = fitWidth(args.originalMaxWidthPageSize, args.viewSize.getWidth());
                widthRatio = optimalMaxWidthPageSize.getWidth() / args.originalMaxWidthPageSize.getWidth();
                optimalMaxHeightPageSize = fitWidth(args.originalMaxHeightPageSize, args.originalMaxHeightPageSize.getWidth() * widthRatio);
                break;
        }

        return new CalculateMaxPagesResult(optimalMaxWidthPageSize, optimalMaxHeightPageSize, widthRatio, heightRatio);
    }

    /**
     * Gets scaled page size to fit page horizontally
     *
     * @param pageSize original page size
     * @param maxWidth max width available
     * @return
     */
    private SizeF fitWidth(Size pageSize, float maxWidth) {
        float w = pageSize.getWidth(), h = pageSize.getHeight();
        float ratio = w / h;
        w = maxWidth;
        h = (float) Math.floor(maxWidth / ratio);
        return new SizeF(w, h);
    }

    /**
     * Gets scaled page size to fit page vertically
     *
     * @param pageSize original page size
     * @param maxHeight max height available
     * @return
     */
    private SizeF fitHeight(Size pageSize, float maxHeight) {
        float w = pageSize.getWidth(), h = pageSize.getHeight();
        float ratio = h / w;
        h = maxHeight;
        w = (float) Math.floor(maxHeight / ratio);
        return new SizeF(w, h);
    }

    /**
     * Gets sclard page size to fit page both vertically and horizontally
     *
     * @param pageSize original page size
     * @param maxWidth max available width
     * @param maxHeight max available height
     * @return
     */
    private SizeF fitBoth(Size pageSize, float maxWidth, float maxHeight) {
        float w = pageSize.getWidth(), h = pageSize.getHeight();
        float ratio = w / h;
        w = maxWidth;
        h = (float) Math.floor(maxWidth / ratio);
        if (h > maxHeight) {
            h = maxHeight;
            w = (float) Math.floor(maxHeight * ratio);
        }
        return new SizeF(w, h);
    }

    /**
     * Calculate max pages result model
     */
    private class CalculateMaxPagesResult {
        /**
         * Optimal page size for page with greatest width
         */
        public final SizeF optimalMaxWidthPageSize;

        /**
         * Optimal page size for page with greatest height
         */
        public final SizeF optimalMaxHeightPageSize;

        /**
         * page width scale ratio
         */
        public final float widthRatio;

        /**
         * page height scale ratio
         */
        public final float heightRatio;

        public CalculateMaxPagesResult(SizeF optimalMaxWidthPageSize, SizeF optimalMaxHeightPageSize,
                                       float widthRatio, float heightRatio) {
            this.optimalMaxWidthPageSize = optimalMaxWidthPageSize;
            this.optimalMaxHeightPageSize = optimalMaxHeightPageSize;
            this.widthRatio = widthRatio;
            this.heightRatio = heightRatio;
        }
    }
}
