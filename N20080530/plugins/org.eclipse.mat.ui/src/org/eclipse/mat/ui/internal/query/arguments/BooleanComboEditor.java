/*******************************************************************************
 * Copyright (c) 2008 SAP AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.mat.ui.internal.query.arguments;

import org.eclipse.mat.impl.query.ArgumentDescriptor;
import org.eclipse.mat.snapshot.SnapshotException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;


public class BooleanComboEditor extends ArgumentEditor
{
    private static final String BOOLEAN_TRUE = Boolean.TRUE.toString();
    private static final String BOOLEAN_FALSE = Boolean.FALSE.toString();

    private CCombo combo;
    private Boolean value;   

    public BooleanComboEditor(Composite parent, ArgumentDescriptor descriptor, TableItem item)
    {
        super(parent, descriptor, item);
        setBackground(parent.getBackground());
        setLayout(new FillLayout());
        createContents(parent);
    }

    private void createContents(Composite parent)
    {
        combo = new CCombo(this, SWT.READ_ONLY | SWT.SIMPLE);
        combo.setBackground(parent.getBackground());
        combo.add(BOOLEAN_TRUE, 0);
        combo.add(BOOLEAN_FALSE, 1);

        combo.addSelectionListener(new SelectionListener()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {}

            public void widgetSelected(SelectionEvent e)
            {
                editingDone();
            }
        });

    }

    protected void editingDone()
    {
        try
        {
            this.value = (Boolean) this.descriptor.stringToValue(combo.getText());
            fireValueChangedEvent(this.value, this);
        }
        catch (SnapshotException e)
        {
            // $JL-EXC$
            // true/false can not fail
        }

    }

    @Override
    public Object getValue()
    {
        return value;
    }

    @Override
    public void setValue(Object value) throws SnapshotException
    {
        this.value = (Boolean) value;
        combo.select(this.value.booleanValue() ? 0 : 1);

    }

    @Override
    public boolean setFocus()
    {        
        return combo.setFocus();
    }
    
    
}