/**
 * Copyright 2017 Bartosz Schiller
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
package com.github.barteksc.pdfviewer.util;

import com.github.barteksc.pdfviewer.calculator.PageSizeCalculatorArgs;
import com.github.barteksc.pdfviewer.calculator.PageSizeCalculatorHandler;
import com.shockwave.pdfium.util.Size;
import com.shockwave.pdfium.util.SizeF;

/**
 * Page size calculator utility to handle size calculations for rendering in view
 */
public class PageSizeCalculator {
    private final SizeF optimalMaxWidthPageSize;
    private final SizeF optimalMaxHeightPageSize;
    private final PageSizeCalculatorArgs calculatorArgs;
    private  final PageSizeCalculatorHandler calculatorHandler;

    public PageSizeCalculator(FitPolicy fitPolicy, Size originalMaxWidthPageSize, Size originalMaxHeightPageSize,
                              Size viewSize, boolean fitEachPage, PageSizeCalculatorHandler calculatorHandler) {
        this.calculatorHandler = calculatorHandler;
        calculatorArgs = new PageSizeCalculatorArgs(fitPolicy, originalMaxWidthPageSize, originalMaxHeightPageSize,
                                                            viewSize, fitEachPage);
        optimalMaxWidthPageSize = calculatorHandler.getOptimalMaxWidthPageSize(calculatorArgs);
        optimalMaxHeightPageSize = calculatorHandler.getOptimalMaxHeightPageSize(calculatorArgs);
    }

    /**
     * Gets optimal page size for rendering in view
     *
     * @param pageSize original page size
     * @return
     */
    public SizeF calculate(Size pageSize) {
        return calculatorHandler.calculate(pageSize, calculatorArgs);
    }

    /**
     * Gets optimal size for the page with greatest width
     *
     * @return
     */
    public SizeF getOptimalMaxWidthPageSize() {
        return optimalMaxWidthPageSize;
    }

    /**
     * Gets optimal size for the page with greatest height
     *
     * @return
     */
    public SizeF getOptimalMaxHeightPageSize() {
        return optimalMaxHeightPageSize;
    }
}