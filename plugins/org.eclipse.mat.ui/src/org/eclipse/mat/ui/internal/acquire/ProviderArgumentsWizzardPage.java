/*******************************************************************************
 * Copyright (c) 2010 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.mat.ui.internal.acquire;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mat.query.registry.AnnotatedObjectArgumentsSet;
import org.eclipse.mat.ui.Messages;
import org.eclipse.mat.ui.internal.acquire.AcquireDialog.ProcessSelectionListener;
import org.eclipse.mat.ui.internal.acquire.ProviderArgumentsTable.ITableListener;
import org.eclipse.mat.ui.internal.browser.QueryContextHelp;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;

public class ProviderArgumentsWizzardPage extends WizardPage implements ITableListener, ProcessSelectionListener
{
	private ProviderArgumentsTable table;

	private AcquireDialog acquireDialog;
	private QueryContextHelp helpPopup;

	public ProviderArgumentsWizzardPage(AcquireDialog acquireDialog)
	{
		super(Messages.ProviderArgumentsWizzardPage_HeapDumpProviderArgumentsTitle, Messages.ProviderArgumentsWizzardPage_HeapDumpProviderArgumentsTitle, null);
		this.acquireDialog = acquireDialog;
	}

	public void createControl(Composite parent)
	{
		ScrolledComposite composite = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);

		composite.setLayout(new GridLayout());
		composite.setExpandHorizontal(true);
		composite.setExpandVertical(true);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

		Composite tableComposite = new Composite(composite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).indent(0, 0).applyTo(tableComposite);

		Dialog.applyDialogFont(composite);
		table = new ProviderArgumentsTable(tableComposite, SWT.FULL_SELECTION | SWT.SINGLE);
		table.addListener(this);

		tableComposite.layout();
		tableComposite.pack();

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.eclipse.mat.ui.help.query_arguments"); //$NON-NLS-1$
		composite.setContent(tableComposite);
		setControl(composite);

		acquireDialog.addProcessSelectionListener(this);
		table.addListener(this);
		
        Listener listener = new Listener()
        {
            public void handleEvent(Event event)
            {
                relocateHelp(false);
            }
        };
        getShell().addListener(SWT.Resize, listener);
        getShell().addListener(SWT.Move, listener);
	}

	/* package */void updateDescription()
	{
		setDescription(table.getProviderDescriptor().getName());
		relocateHelp(true);
		getContainer().updateButtons();
	}

	public ProviderArgumentsTable getTable()
	{
		return table;
	}

	public AnnotatedObjectArgumentsSet getArgumentSet()
	{
		return table.getArgumentSet();
	}

	public void onInputChanged()
	{
		updateDescription();
//		getContainer().updateButtons();
	}

	public void onError(String message)
	{
		setErrorMessage(message);
		// a work around: calling onFocus ensures a correct update of the error
		// message. Without this call it doesn't update the message correct.
		onFocus(null);
	}

	public void onFocus(String message)
	{
		if (getErrorMessage() != null) setMessage(getErrorMessage(), IMessageProvider.ERROR);
		else if (message != null) setMessage(message, IMessageProvider.INFORMATION);
		else setMessage(table.getProviderDescriptor().getName());
		getContainer().updateButtons();
	}

	@Override
	public boolean isPageComplete()
	{
		return table != null && table.getArgumentSet() != null && table.getArgumentSet().isExecutable();
	}

	public void relocateHelp(final boolean create)
	{
		final AnnotatedObjectArgumentsSet argumentSet = table.getArgumentSet();
		if (argumentSet == null) return;

		if (argumentSet.getDescriptor().isHelpAvailable() && //
				(create || (helpPopup != null && helpPopup.getShell() != null)))
		{
			if (getShell() == null)
			{
				helpPopup.close();
				return;
			}
			getShell().getDisplay().timerExec(100, new Runnable() {
				public void run()
				{
					if (getShell() != null && !getShell().isDisposed())
					{
						Rectangle myBounds = getShell().getBounds();
						Rectangle helpBounds = new Rectangle(myBounds.x, myBounds.y + myBounds.height, myBounds.width, SWT.DEFAULT);

                        if (helpPopup != null)
                        {
                            if (!create)
                            {
                                helpPopup.resize(helpBounds);
                                return;
                            }
                            else
                            {
                                // Get ready to create a new pop-up with new help text
                                helpPopup.close();
                            }
                        }

						helpPopup = new QueryContextHelp(getShell(), argumentSet.getDescriptor(), helpBounds);
						helpPopup.open();
					}
				}
			});
		}
	}

	public void processSelected(AnnotatedObjectArgumentsSet argumentSet)
	{
		table.providerSelected(argumentSet);
	}
	
}
