/*
 * Created on 26/giu/2010
 *
 * Copyright 2010 by Andrea Vacondio (andrea.vacondio@gmail.com).
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
package org.sejda.core.validation.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.sejda.core.manipulation.model.input.PageRange;
import org.sejda.core.validation.constraint.EndGreaterThenOrEqualToStart;

/**
 * Validator for the {@link EndGreaterThenOrEqualToStart} constraint.
 * 
 * @author Andrea Vacondio
 * 
 */
public class PageRangeValidator implements ConstraintValidator<EndGreaterThenOrEqualToStart, PageRange> {

    public void initialize(EndGreaterThenOrEqualToStart constraintAnnotation) {
        // on purpose
    }

    public boolean isValid(PageRange value, ConstraintValidatorContext context) {
        if (value != null) {
            return value.getStart() <= value.getEnd();
        }
        return true;
    }

}