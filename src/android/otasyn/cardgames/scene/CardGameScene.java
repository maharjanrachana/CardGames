package android.otasyn.cardgames.scene;

import android.otasyn.cardgames.entity.HandHighlight;
import android.otasyn.cardgames.sprite.CardSprite;
import android.otasyn.cardgames.utility.enumeration.Card;
import org.andengine.entity.IEntity;
import org.andengine.entity.IEntityMatcher;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;

import java.util.ArrayList;
import java.util.List;

public class CardGameScene extends Scene {

    private static final int TEXT_ZINDEX_START = 2000;
    private static final int BUTTONS_ZINDEX_START = 1500;
    private static final int CARDS_ZINDEX_START = 1000;
    private static final int HAND_HIGHLIGHT_ZINDEX = 500;

    private final List<CardSprite> cardSpriteList = new ArrayList<CardSprite>(Card.values().length);
    private CardSprite touchedCardSprite = null;

    @Override
    public void attachChild(final IEntity pEntity) throws IllegalStateException {
        if (pEntity instanceof CardSprite) {
            attachCardSprite((CardSprite) pEntity);
        } else if (pEntity instanceof Text) {
            attachText((Text) pEntity);
        } else {
            super.attachChild(pEntity);
        }
    }

    public void attachText(final Text text) {
        super.attachChild(text);
        text.setZIndex(TEXT_ZINDEX_START);
    }

    public void attachButtonSprite(final ButtonSprite buttonSprite) {
        super.attachChild(buttonSprite);
        buttonSprite.setZIndex(BUTTONS_ZINDEX_START);
        registerTouchArea(buttonSprite);
    }

    public void attachCardSprite(final CardSprite cardSprite) {
        super.attachChild(cardSprite);
        cardSpriteList.add(cardSprite);
        reindexCardSprites();
        registerTouchArea(cardSprite);
    }

    public void attachHandHighlight(final HandHighlight handHighlight) {
        super.attachChild(handHighlight);
        handHighlight.setZIndex(HAND_HIGHLIGHT_ZINDEX);
    }

    @Override
    public boolean detachChild(final IEntity pEntity) {
        if (pEntity instanceof CardSprite) {
            return detachCardSprite((CardSprite) pEntity, true);
        }
        return super.detachChild(pEntity);
    }

    @Override
    public IEntity detachChild(final int pTag) {
        IEntity child = getChildByTag(pTag);
        if (child instanceof CardSprite) {
            if (detachCardSprite((CardSprite) child, true)) {
                return child;
            }
        }
        return super.detachChild(pTag);
    }

    @Override
    public IEntity detachChild(final IEntityMatcher pEntityMatcher) {
        IEntity child = getChildByMatcher(pEntityMatcher);
        if (child instanceof CardSprite) {
            if (detachCardSprite((CardSprite) child, true)) {
                return child;
            }
        }
        return super.detachChild(pEntityMatcher);
    }

    @Override
    public void detachChildren() {
        for (int n = 0; n < getChildCount(); n++) {
            IEntity child = getChildByIndex(n);
            if (child instanceof CardSprite) {
                detachCardSprite((CardSprite) child, false);
            } else {
                detachChild(child);
            }
        }
        reindexCardSprites();
    }

    @Override
    public boolean detachChildren(final IEntityMatcher pEntityMatcher) {
        IEntity child = getChildByMatcher(pEntityMatcher);
        boolean result = true;
        while (child != null) {
            if (child instanceof CardSprite) {
                result = result && detachCardSprite((CardSprite) child, false);
            } else {
                result = result && detachChild(child);
            }
        }
        reindexCardSprites();
        return result;
    }

    public boolean detachCardSprite(final CardSprite cardSprite, final boolean doReindex) {
        boolean result = super.detachChild(cardSprite);
        while (cardSpriteList.remove(cardSprite)) {}
        if (doReindex) {
            reindexCardSprites();
        }
        return result;
    }

    private void reindexCardSprites() {
        for (int n = 0; n < cardSpriteList.size(); n++) {
            cardSpriteList.get(n).setZIndex(CARDS_ZINDEX_START + n);
        }
        this.sortChildren();
    }

    public void moveCardSpriteToFront(final CardSprite cardSprite) {
        while (cardSpriteList.remove(cardSprite)) {}
        cardSpriteList.add(cardSprite);
        reindexCardSprites();
    }

    public CardSprite getTouchedCardSprite() {
        return touchedCardSprite;
    }

    public void setTouchedCardSprite(final CardSprite touchedCardSprite) {
        this.touchedCardSprite = touchedCardSprite;
    }

    public boolean checkAndSetTouchedCardSprite(final CardSprite checkCardSprite) {
        if (this.touchedCardSprite == null ||
                checkCardSprite == null ||
                checkCardSprite.getZIndex() > this.touchedCardSprite.getZIndex()) {
            this.touchedCardSprite = checkCardSprite;
            return true;
        }
        return false;
    }

    public boolean isTouchedCardSprite(final CardSprite cardSprite) {
        return cardSprite != null && touchedCardSprite == cardSprite;
    }

    @Override
    public boolean onSceneTouchEvent(final TouchEvent pSceneTouchEvent) {
        IOnSceneTouchListener listener = this.getOnSceneTouchListener();
        if (listener == null) {
            boolean result = super.onSceneTouchEvent(pSceneTouchEvent);
            switch (pSceneTouchEvent.getAction()) {
                case TouchEvent.ACTION_DOWN:
                    for (ITouchArea touchArea : this.getTouchAreas()) {
                        if (touchArea instanceof CardSprite) {
                            if (touchArea.contains(pSceneTouchEvent.getX(), pSceneTouchEvent.getY())) {
                                checkAndSetTouchedCardSprite((CardSprite) touchArea);
                            }
                        }
                    }
                    if (this.touchedCardSprite != null) {
                        moveCardSpriteToFront(this.touchedCardSprite);
                        this.touchedCardSprite.setTouchOffset(pSceneTouchEvent);
                    }
                    break;
                case TouchEvent.ACTION_MOVE:
                    if (this.isTouchedCardSprite(this.touchedCardSprite)) {
                        this.touchedCardSprite.setPosition(pSceneTouchEvent);
                        return true;
                    }
                    break;
                case TouchEvent.ACTION_UP:
                case TouchEvent.ACTION_CANCEL:
                    this.touchedCardSprite = null;
            }
            return result;
        }
        return super.onSceneTouchEvent(pSceneTouchEvent);
    }
}
