/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 *  All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 *
 * @author bfitzpat
 ******************************************************************************/
package org.switchyard.tools.ui.editor.diagram.componentreference;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.context.impl.AddContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.soa.sca.sca1_1.model.sca.ComponentReference;
import org.eclipse.soa.sca.sca1_1.model.sca.Composite;
import org.eclipse.soa.sca.sca1_1.model.sca.Multiplicity;
import org.eclipse.soa.sca.sca1_1.model.sca.Reference;
import org.eclipse.soa.sca.sca1_1.model.sca.ScaPackage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.switchyard.tools.ui.editor.ImageProvider;
import org.switchyard.tools.ui.editor.diagram.StyleUtil;
import org.switchyard.tools.ui.editor.diagram.shared.BaseNewContractWizard;

/**
 * @author bfitzpat
 * 
 */
public class SCADiagramCustomPromoteReferenceFeature extends AbstractCustomFeature {

    private boolean _hasDoneChanges = false;

    /**
     * @param fp the feature provider
     */
    public SCADiagramCustomPromoteReferenceFeature(IFeatureProvider fp) {
        super(fp);
    }

    @Override
    public void execute(ICustomContext context) {
        PictogramElement[] pes = context.getPictogramElements();
        if (pes != null && pes.length == 1) {
            Object bo = getBusinessObjectForPictogramElement(pes[0]);
            if (bo instanceof ComponentReference) {
                ComponentReference componentReference = (ComponentReference) bo;
                Reference newReference = null;
                BaseNewContractWizard wizard = new BaseNewContractWizard("Promote Component Reference",
                        "Specify details for the new composite reference.", ScaPackage.eINSTANCE.getReference());
                wizard.init(componentReference);
                Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                WizardDialog wizDialog = new WizardDialog(shell, wizard);
                int rtn_code = wizDialog.open();
                if (rtn_code == Window.OK) {
                    newReference = (Reference) wizard.getContract();
                    newReference.setMultiplicity(Multiplicity._01);
                    newReference.getPromote().add(componentReference);
                } else {
                    _hasDoneChanges = false;
                    return;
                }

                Composite composite = (Composite) componentReference.eContainer().eContainer();
                composite.getReference().add(newReference);

                ContainerShape compositeShape = (ContainerShape) getFeatureProvider()
                        .getPictogramElementForBusinessObject(composite);

                AddContext addRefContext = new AddContext();
                addRefContext.setNewObject(newReference);
                addRefContext.setTargetContainer(compositeShape);
                addRefContext.setX(compositeShape.getGraphicsAlgorithm().getWidth()
                        - StyleUtil.COMPOSITE_REFERENCE_WIDTH);
                addRefContext.setY(compositeShape.getGraphicsAlgorithm().getY() + 3 * StyleUtil.COMPOSITE_EDGE);

                Shape referenceShape = (Shape) addGraphicalRepresentation(addRefContext, newReference);
                if (referenceShape != null) {
                    // make sure the new reference is positioned
                    // correctly.
                    layoutPictogramElement(compositeShape);
                }
                _hasDoneChanges = true;
            }
        }
    }

    @Override
    public String getDescription() {
        return "Promote Component Reference";
    }

    @Override
    public boolean canExecute(ICustomContext context) {
        PictogramElement[] pes = context.getPictogramElements();
        if (pes != null && pes.length == 1) {
            return getBusinessObjectForPictogramElement(pes[0]) instanceof ComponentReference;
        }

        return false;
    }

    @Override
    public String getName() {
        return "Promote Component Reference";
    }

    @Override
    public boolean hasDoneChanges() {
        return this._hasDoneChanges;
    }

    @Override
    public String getImageId() {
        return ImageProvider.IMG_16_PLUS;
    }

    @Override
    public boolean isAvailable(IContext context) {
        return true;
    }

}
