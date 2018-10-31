/*******************************************************************
 * Copyright (c) 2006 - 2015, Martin Kesting, All rights reserved.
 *
 * This software is licenced under the Eclipse Public License v1.0,
 * see the LICENSE file or http://www.eclipse.org/legal/epl-v10.html
 * for details.
 *******************************************************************/
package net.sf.jautodoc.preferences;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;


/**
 * Prefix configuration element for [G,S]etter from field comment.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class GetSetFromFieldReplacement implements ITableEntry, Comparable<GetSetFromFieldReplacement> {

    public static final GetSetFromFieldReplacement EMPTY = new GetSetFromFieldReplacement("", "", "");

    private final String fieldPrefix;
    private final String returnPrefix;
    private final String parameterPrefix;

    public GetSetFromFieldReplacement(final String fieldPrefix, final String returnPrefix, final String parameterPrefix) {
        this.fieldPrefix = fieldPrefix;
        this.returnPrefix = returnPrefix;
        this.parameterPrefix = parameterPrefix;
    }

    public String getFieldPrefix() {
        return fieldPrefix;
    }

    public String getReturnPrefix() {
        return returnPrefix;
    }

    public String getParameterPrefix() {
        return parameterPrefix;
    }

    public static GetSetFromFieldReplacement fromString(final String string) {
        final String[] values = string.split("\\|");
        return new GetSetFromFieldReplacement(values[0], values[1], values[2]);
    }

    @Override
    public String toString() {
        return fieldPrefix + "|" + returnPrefix + "|" + parameterPrefix;
    }

    @Override
    public String getColumnText(final int columnIndex) {
        switch (columnIndex) {
        case 0:
            return getFieldPrefix();
        case 1:
            return getReturnPrefix();
        case 2:
            return getParameterPrefix();
        default:
            throw new IllegalArgumentException("Invalid columnIndex.");
        }
    }

    @Override
    public int compareTo(final GetSetFromFieldReplacement o) {
        if (fieldPrefix.isEmpty() && !o.fieldPrefix.isEmpty()) {
            return 1;
        }
        if (!fieldPrefix.isEmpty() && o.fieldPrefix.isEmpty()) {
            return -1;
        }
        return fieldPrefix.compareToIgnoreCase(o.fieldPrefix);
    }

    @Override
    public int hashCode() {
        return fieldPrefix.toLowerCase().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return compareTo((GetSetFromFieldReplacement) obj) == 0;
    }
}
