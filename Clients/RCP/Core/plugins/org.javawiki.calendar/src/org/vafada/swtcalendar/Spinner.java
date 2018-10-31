/*******************************************************************************
 * .
 *                           Velo 1.0
 *  ----------------------------------------------------------
 * 
 *            Pacific Northwest National Laboratory
 *                      Richland, WA 99352
 * 
 *                      Copyright (c) 2013
 *            Pacific Northwest National Laboratory
 *                 Battelle Memorial Institute
 * 
 *   Velo is an open-source collaborative content management
 *                and job execution environment
 *              distributed under the terms of the
 *           Educational Community License (ECL) 2.0
 *   A copy of the license is included with this distribution
 *                   in the LICENSE.TXT file
 * 
 *                        ACKNOWLEDGMENT
 *                        --------------
 * 
 * This software and its documentation were developed at Pacific 
 * Northwest National Laboratory, a multiprogram national
 * laboratory, operated for the U.S. Department of Energy by 
 * Battelle under Contract Number DE-AC05-76RL01830.
 ******************************************************************************/
/*

 *  Spinner.java  - A spinner component

 *  Author: Eclipse.org

 *  Modified by: Sergey Prigogin

 *  swtcalendar.sourceforge.net

 *

 *  This program is free software; you can redistribute it and/or

 *  modify it under the terms of the GNU Lesser General Public License

 *  as published by the Free Software Foundation; either version 2

 *  of the License, or (at your option) any later version.

 *

 *  This program is distributed in the hope that it will be useful,

 *  but WITHOUT ANY WARRANTY; without even the implied warranty of

 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the

 *  GNU Lesser General Public License for more details.

 *

 *  You should have received a copy of the GNU Lesser General Public License

 *  along with this program; if not, write to the Free Software

 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

 */

package org.vafada.swtcalendar;



import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.IdentityHashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TypedListener;



/**

 *

 * @version $Revision: 1.0 $
 */

public class Spinner extends Composite implements FocusListener {

    private static final int BUTTON_WIDTH = 16;

    private IdentityHashMap selectionListeners = new IdentityHashMap(3);

    private int minimum;

    private int maximum;

    private boolean cyclic;

    private NumberFormat numberFormat = new DecimalFormat("0");

    private boolean settingValue;

    private boolean inFocus;

    private Text text;

    private RepeatingButton upButton;

    private RepeatingButton downButton;



    /**
     * Constructor for Spinner.
     * @param parent Composite
     * @param style int
     */
    public Spinner(Composite parent, int style) {

        super(parent, style);

        setFont(parent.getFont());



        minimum = 0;

        maximum = 9;

        setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

        final GridLayout gridLayout = new GridLayout();

        gridLayout.numColumns = 2;

        gridLayout.verticalSpacing = 0;

        gridLayout.marginWidth = 0;

        gridLayout.marginHeight = 0;

        gridLayout.horizontalSpacing = 0;

        setLayout(gridLayout);



        {

            text = new Text(this, SWT.RIGHT);

            text.setFont(getFont());

            final GridData gridData = new GridData(GridData.FILL_HORIZONTAL);

            text.setLayoutData(gridData);

            text.addVerifyListener(new VerifyListener() {

                public void verifyText(VerifyEvent event) {

                    verify(event);

                }

            });

            text.addTraverseListener(new TraverseListener() {

                public void keyTraversed(TraverseEvent event) {

                    traverse(event);

                }

            });

            text.addFocusListener(this);

        }

        {

            final Composite buttonHolder = new Composite(this, SWT.NO_FOCUS);

            buttonHolder.setFont(getFont());

            final GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);

            gridData.widthHint = BUTTON_WIDTH;

            buttonHolder.setLayoutData(gridData);

            buttonHolder.setLayout(new FillLayout(SWT.VERTICAL));



            upButton = new RepeatingButton(buttonHolder, SWT.ARROW | SWT.CENTER | SWT.UP | SWT.NO_FOCUS);

            upButton.setFont(getFont());

            upButton.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent event) {

                    upInternal();

                    text.setFocus();

                }

            });



            downButton = new RepeatingButton(buttonHolder, SWT.ARROW | SWT.CENTER | SWT.DOWN | SWT.NO_FOCUS);

            downButton.setFont(getFont());

            downButton.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent event) {

                    downInternal();

                    text.setFocus();

                }

            });

        }



        setTabList(new Control[]{text});

        setValueInternal(minimum);

    }



    public void up() {

        settingValue = true;

        try {

            upInternal();

        } finally {

            settingValue = false;

        }

    }



    public void down() {

        settingValue = true;

        try {

            downInternal();

        } finally {

            settingValue = false;

        }

    }



    /**
     * Method setValue.
     * @param value int
     */
    public void setValue(int value) {

        settingValue = true;

        try {

            setValueInternal(value);

        } finally {

            settingValue = false;

        }

    }



    /**
     * Method getValue.
     * @return int
     */
    public int getValue() {

        try {

            return numberFormat.parse(text.getText()).intValue();

        } catch (ParseException e) {

            return minimum;

        }

    }



    /**
     * Method setMaximum.
     * @param maximum int
     */
    public void setMaximum(int maximum) {

        this.maximum = maximum;

        setValue(getValue());

    }



    /**
     * Method getMaximum.
     * @return int
     */
    public int getMaximum() {

        return maximum;

    }



    /**
     * Method setMinimum.
     * @param minimum int
     */
    public void setMinimum(int minimum) {

        this.minimum = minimum;

        setValue(getValue());

    }



    /**
     * Method getMinimum.
     * @return int
     */
    public int getMinimum() {

        return minimum;

    }



    /**

     * Returns <code>true</code> if the Spinner is in cyclic mode, otherwise <code>false</code>.

     * @return boolean
     */

    public boolean isCyclic() {

        return cyclic;

    }



    /**

     * Sets cyclic mode. In cyclic mode pressing the up arrow button repeatedly

     * increments the value from <code>minimum</code> to <code>maximum</code> and then

     * starts from <code>minimum</code> again.

     *

     * @param cyclic <code>true</code> to set cyclic mode, <code>false</code> to turn it off.

     */

    public void setCyclic(boolean cyclic) {

        this.cyclic = cyclic;

    }



    /**
     * Method setRange.
     * @param minimum int
     * @param maximum int
     * @param cyclic boolean
     */
    public void setRange(int minimum, int maximum, boolean cyclic) {

        this.minimum = minimum;

        this.maximum = maximum;

        this.cyclic = cyclic;

        setValueInternal(getValue());

    }



    /**

    

     * @return Returns the number format used by the spinner. */

    public NumberFormat getNumberFormat() {

        return numberFormat;

    }



    /**

     * @param numberFormat The number format to set.

     */

    public void setNumberFormat(NumberFormat numberFormat) {

        int val = getValue();

        this.numberFormat = numberFormat;

        setValue(val);

    }



    /**

    

     * @return Returns the initial repeat delay in milliseconds. */

    public int getInitialRepeatDelay() {

        return upButton.getInitialRepeatDelay();

    }



    /**

     * @param initialRepeatDelay The new initial repeat delay in milliseconds.

     */

    public void setInitialRepeatDelay(int initialRepeatDelay) {

        upButton.setInitialRepeatDelay(initialRepeatDelay);

        downButton.setInitialRepeatDelay(initialRepeatDelay);

    }



    /**

    

     * @return Returns the repeat delay in millisecons. */

    public int getRepeatDelay() {

        return upButton.getRepeatDelay();

    }



    /**

     * @param repeatDelay The new repeat delay in milliseconds.

     */

    public void setRepeatDelay(int repeatDelay) {

        upButton.setRepeatDelay(repeatDelay);

        downButton.setRepeatDelay(repeatDelay);

    }



    /* (non-Javadoc)

     * @see org.eclipse.swt.widgets.Control#setFont(org.eclipse.swt.graphics.Font)

     */

    public void setFont(Font font) {

        super.setFont(font);

        if (text != null) {

            text.setFont(font);

        }

    }



    /**
     * Method isSettingValue.
     * @return boolean
     */
    public boolean isSettingValue() {

        return settingValue;

    }



    /**
     * Method addModifyListener.
     * @param listener ModifyListener
     */
    public void addModifyListener(ModifyListener listener) {

        text.addModifyListener(listener);

    }



    /**
     * Method removeModifyListener.
     * @param listener ModifyListener
     */
    public void removeModifyListener(ModifyListener listener) {

        text.removeModifyListener(listener);

    }



    /**
     * Method addSelectionListener.
     * @param listener SelectionListener
     */
    public void addSelectionListener(SelectionListener listener) {

        if (listener == null) {

            throw new SWTError(SWT.ERROR_NULL_ARGUMENT);

        }

        TypedListener typedListener = new TypedListener(listener);

        selectionListeners.put(listener, typedListener);

        addListener(SWT.Selection, typedListener);

    }



    /**
     * Method removeSelectionListener.
     * @param listener SelectionListener
     */
    public void removeSelectionListener(SelectionListener listener) {

        if (listener == null) {

            throw new SWTError(SWT.ERROR_NULL_ARGUMENT);

        }

        TypedListener typedListener = (TypedListener) selectionListeners.remove(listener);

        if (typedListener != null) {

            removeListener(SWT.Selection, typedListener);

        }

    }



    /* (non-Javadoc)

     * @see org.eclipse.swt.widgets.Control#computeSize(int, int)

     */

    /**
     * Method computeSize.
     * @param wHint int
     * @param hHint int
     * @param changed boolean
     * @return Point
     */
    public Point computeSize(int wHint, int hHint, boolean changed) {

        if (wHint == SWT.DEFAULT) {

            GC gc = new GC(text);

            wHint = Math.max(gc.textExtent(numberFormat.format(maximum)).x,

                    gc.textExtent(numberFormat.format(maximum)).x);

            gc.dispose();

        }



        Point size = text.computeSize(wHint, hHint, changed);

        size.x += BUTTON_WIDTH;

        if ((getStyle() & SWT.BORDER) != 0) {

            int border = getBorderWidth();

            size.x += border * 2;

            size.y += border * 2 + 3;

        }

        size.y = (size.y + 1) & ~1; // Round up to an even number.

        return size;

    }



    protected void upInternal() {

        int val = getValue();

        val++;

        if (val > maximum) {

            if (cyclic) {

                val = minimum;

            } else {

                val = maximum;

            }

        }

        setValueInternal(val);

        notifyListeners(SWT.Selection, new Event());

    }



    protected void downInternal() {

        int val = getValue();

        val--;

        if (val < minimum) {

            if (cyclic) {

                val = maximum;

            } else {

                val = minimum;

            }

        }

        setValueInternal(val);

        notifyListeners(SWT.Selection, new Event());

    }



    /**
     * Method setValueInternal.
     * @param value int
     */
    protected void setValueInternal(int value) {

        if (value < minimum) {

            value = minimum;

        } else if (value > maximum) {

            value = maximum;

        }

        String str = numberFormat.format(value);

        if (!str.equals(text.getText())) {

            text.setText(str);

        }

    }



    /**
     * Method verify.
     * @param event VerifyEvent
     */
    private void verify(VerifyEvent event) {

        for (int i = 0; i < event.text.length(); i++) {

            char c = event.text.charAt(i);

            if (!Character.isDigit(c) && !(minimum < 0 && c == '-' && i == 0 && event.start == 0) &&

                    numberFormat.format(minimum).indexOf(c) < 0) {

                event.doit = false;

                break;

            }

        }

    }



    /**
     * Method traverse.
     * @param event TraverseEvent
     */
    private void traverse(TraverseEvent event) {

        switch (event.detail) {

            case SWT.TRAVERSE_ARROW_PREVIOUS:

                if (event.keyCode == SWT.ARROW_UP) {

                    event.doit = true;

                    event.detail = SWT.NULL;

                    upInternal();

                }

                break;



            case SWT.TRAVERSE_ARROW_NEXT:

                if (event.keyCode == SWT.ARROW_DOWN) {

                    event.doit = true;

                    event.detail = SWT.NULL;

                    downInternal();

                }

                break;

        }

    }



    /* (non-Javadoc)

     * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)

     */

    public void focusGained(FocusEvent focusEvent) {

        if (!inFocus) {

            inFocus = true;

            Event event = new Event();

            event.time = focusEvent.time;

            notifyListeners(SWT.FocusIn, event);

        }

    }



    /* (non-Javadoc)

     * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)

     */

    public void focusLost(FocusEvent focusEvent) {

        if (!isFocusControl()) {

            inFocus = false;

            Event event = new Event();

            event.time = focusEvent.time;

            notifyListeners(SWT.FocusOut, event);

        }

    }



    /* (non-Javadoc)

     * @see org.eclipse.swt.widgets.Control#isFocusControl()

     */

    public boolean isFocusControl() {

        Control control = getDisplay().getFocusControl();

        return control == this || control == text;

    }

}

