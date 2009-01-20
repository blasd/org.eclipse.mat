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
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;


public class CheckBoxEditor extends ArgumentEditor
{
    private Button checkBox;
    private Boolean value = false;
    private Type type;

    public enum Type
    {
        INCLUDE_CLASS_INSTANCE("include class instance (if defined by a pattern)", null), //
        INCLUDE_SUBCLASSES("include subclasses (if object is a class)", //
                        "If specified together with <pattern> the subclasses of the classes matching the pattern will also be included"), //
        INTEPRET_AS_CLASSLOADER("include loaded objects (if specified object is a classloader)",
                        "If specified together with <object address> the address will be interpet as address of a classloader"), //
        RETAINED("as retained set",
                        "Specifies that the retained set of the described object set should be taken, and not the object set itself"), //
        VERBOSE("verbose", "Prints information about added classes to the error log");

        private String label;
        private String helpText;

        private Type(String label, String helpText)
        {
            this.label = label;
            this.helpText = helpText;
        }

        public String getLabel()
        {
            return label;
        }

        public String getHelpText()
        {
            return helpText;
        }
    }

    public CheckBoxEditor(Composite parent, ArgumentDescriptor descriptor, TableItem item, Type type)
    {
        super(parent, descriptor, item);
        this.type = type;
        setBackground(parent.getBackground());
        setLayout(new FillLayout());
        createContents(parent);
    }

    private void createContents(Composite parent)
    {
        checkBox = new Button(this, SWT.CHECK);
        checkBox.setBackground(parent.getBackground());
        checkBox.setText(type.getLabel());
        checkBox.addFocusListener(new FocusListener()
        {

            public void focusGained(FocusEvent e)
            {
                fireFocusEvent(type.getHelpText());
            }

            public void focusLost(FocusEvent e)
            {
                fireFocusEvent(null);
            }
        });

        checkBox.addSelectionListener(new SelectionListener()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {}

            public void widgetSelected(SelectionEvent e)
            {
                editingDone();
            }
        });
        checkBox.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                if (e.character == '\r')
                { // Return key
                    editingDone();
                }
            }
        });

    }

    protected void editingDone()
    {
        this.value = checkBox.getSelection();
        fireValueChangedEvent(this.value, this);
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
        checkBox.setSelection(this.value.booleanValue());

    }

    @Override
    public boolean setFocus()
    {
        return checkBox.setFocus();
    }

    public Type getType()
    {
        return type;
    }
}