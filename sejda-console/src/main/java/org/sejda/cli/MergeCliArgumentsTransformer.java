/*
 * Created on Jul 1, 2011
 * Copyright 2011 by Eduard Weissmann (edi.weissmann@gmail.com).
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.sejda.cli;

import java.util.ArrayList;
import java.util.List;

import org.sejda.cli.adapters.MultiplePdfMergeInputAdapter;
import org.sejda.cli.adapters.PdfFileSourceAdapter;
import org.sejda.core.exception.SejdaRuntimeException;
import org.sejda.core.manipulation.model.input.PdfFileSource;
import org.sejda.core.manipulation.model.input.PdfMergeInput;
import org.sejda.core.manipulation.model.parameter.MergeParameters;

/**
 * {@link CommandCliArgumentsTransformer} for the Merge task command line interface
 * 
 * @author Eduard Weissmann
 * 
 */
public class MergeCliArgumentsTransformer extends BaseCliArgumentsTransformer implements
        CommandCliArgumentsTransformer<MergeTaskCliArguments, MergeParameters> {

    /**
     * Transforms {@link MergeTaskCliArguments} to {@link MergeParameters}
     * 
     * @param taskCliArguments
     * @return
     */
    public MergeParameters toTaskParameters(MergeTaskCliArguments taskCliArguments) {
        MergeParameters parameters = new MergeParameters(taskCliArguments.isCopyFields());
        populateAbstractParameters(parameters, taskCliArguments);

        MultiplePdfMergeInputAdapter mergeInputsAdapter = extractPdfMergeInputs(taskCliArguments);

        for (PdfMergeInput eachMergeInput : mergeInputsAdapter.getPdfMergeInputs()) {
            parameters.addInput(eachMergeInput);
        }

        return parameters;
    }

    /**
     * @param taskCliArguments
     * @return
     */
    private MultiplePdfMergeInputAdapter extractPdfMergeInputs(MergeTaskCliArguments taskCliArguments) {
        // input files can be specified in 3 ways: explicitly, via a folder or via a config file
        List<PdfFileSource> inputFiles = null;
        if (taskCliArguments.isDirectory()) {
            inputFiles = taskCliArguments.getDirectory().getFileSourceList();
        } else if (taskCliArguments.isFiles()) {
            inputFiles = extractFiles(taskCliArguments.getFiles());
        } else if (taskCliArguments.isFilesListConfig()) {
            inputFiles = taskCliArguments.getFilesListConfig().getFileSourceList();
        }

        if (inputFiles == null) {
            throw new SejdaRuntimeException("No input files specified");
        }

        MultiplePdfMergeInputAdapter mergeInputsAdapter = new MultiplePdfMergeInputAdapter(inputFiles, taskCliArguments
                .getPageSelection().iterator());
        return mergeInputsAdapter;
    }

    private List<PdfFileSource> extractFiles(List<PdfFileSourceAdapter> adapters) {
        List<PdfFileSource> result = new ArrayList<PdfFileSource>();
        for (PdfFileSourceAdapter eachAdapter : adapters) {
            result.add(eachAdapter.getPdfFileSource());
        }
        return result;
    }
}
