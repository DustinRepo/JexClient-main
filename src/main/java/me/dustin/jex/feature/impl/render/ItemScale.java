package me.dustin.jex.feature.impl.render;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventRenderItem;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;
import net.minecraft.client.util.math.MatrixStack;

@Feat(name = "ItemScale", category = FeatureCategory.VISUAL, description = "Change the scale and positioning of items in your hands")
public class ItemScale extends Feature {

    @Op(name = "Right Hand")
    public boolean rightHand = true;
    @OpChild(name = "RH Scale", min = 0.1f, max = 3, inc = 0.05f, parent = "Right Hand")
    public float rightHandScale = 1;
    @OpChild(name = "RH X", min = -2, max = 2, inc = 0.05f, parent = "Right Hand")
    public float rightHandX;
    @OpChild(name = "RH Y", min = -2, max = 2, inc = 0.05f, parent = "Right Hand")
    public float rightHandY;
    @OpChild(name = "RH Z", min = -2, max = 2, inc = 0.05f, parent = "Right Hand")
    public float rightHandZ;

    @Op(name = "Left Hand")
    public boolean leftHand = true;
    @OpChild(name = "LH Scale", min = 0.1f, max = 3, inc = 0.05f, parent = "Left Hand")
    public float leftHandScale = 1;
    @OpChild(name = "LH X", min = -2, max = 2, inc = 0.05f, parent = "Left Hand")
    public float leftHandX;
    @OpChild(name = "LH Y", min = -2, max = 2, inc = 0.05f, parent = "Left Hand")
    public float leftHandY;
    @OpChild(name = "LH Z", min = -2, max = 2, inc = 0.05f, parent = "Left Hand")
    public float leftHandZ;

    @EventListener(events = EventRenderItem.class)
    private void runMethod(EventRenderItem eventRenderItem) {

        if (eventRenderItem.getType().isFirstPerson()) {
            MatrixStack matrixStack = eventRenderItem.getMatrixStack();
            switch (eventRenderItem.getRenderTime()) {
                case PRE:
                    matrixStack.push();
                    switch (eventRenderItem.getType()) {
                        case FIRST_PERSON_RIGHT_HAND:
                            matrixStack.translate(rightHandX, rightHandY, rightHandZ);
                            matrixStack.scale(rightHandScale, rightHandScale, rightHandScale);
                            break;
                        case FIRST_PERSON_LEFT_HAND:
                            matrixStack.translate(leftHandX, leftHandY, leftHandZ);
                            matrixStack.scale(leftHandScale, leftHandScale, leftHandScale);
                            break;
                    }
                    break;
                case POST:
                    matrixStack.pop();
                    break;
            }
        }
    }

}