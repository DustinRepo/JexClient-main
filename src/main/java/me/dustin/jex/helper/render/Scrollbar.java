package me.dustin.jex.helper.render;

import net.minecraft.client.util.math.MatrixStack;

public class Scrollbar {
    
    private float x,y,width,height;
    private float viewportY, viewportHeight;
    private float contentHeight;
    private int color;

    public Scrollbar(float x, float y, float width, float height, float viewportHeight, float contentHeight, int color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.viewportY = y;
        this.viewportHeight = viewportHeight;
        this.contentHeight = contentHeight;
        this.color = color;
    }

    public void render(MatrixStack matrixStack) {
        updateHeight();
        if (contentHeight > viewportHeight) {
            Render2DHelper.INSTANCE.fill(matrixStack, x, viewportY, x + width, viewportY + viewportHeight + 1, 0xff353535);
            Render2DHelper.INSTANCE.fill(matrixStack, x, y, x + width, y + height, color);
        }
    }

    public boolean isHovered() {
        return Render2DHelper.INSTANCE.isHovered(x,y,width,height);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void updateHeight() {
        this.height = viewportHeight * (viewportHeight / contentHeight);
    }

    public float getViewportHeight() {
        return viewportHeight;
    }

    public void setViewportHeight(float viewportHeight) {
        this.viewportHeight = viewportHeight;
    }

    public float getContentHeight() {
        return contentHeight;
    }

    public void setContentHeight(float contentHeight) {
        this.contentHeight = contentHeight;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public float getViewportY() {
        return viewportY;
    }

    public void setViewportY(float viewportY) {
        this.viewportY = viewportY;
    }

    public void moveDown() {
        this.y += viewportHeight / contentHeight;
    }

    public void moveUp() {
        this.y -= viewportHeight / contentHeight;
    }
}
