package com.termux.app.ime;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;

import com.termux.app.activities.SettingsActivity;

import juloo.keyboard2.Config;
import juloo.keyboard2.KeyValue;
import juloo.keyboard2.Pointers;

public final class ImeKeyEventHandler implements Config.IKeyEventHandler {

    private final TermuxInputMethodService mService;
    private Pointers.Modifiers mModifiers = Pointers.Modifiers.EMPTY;

    public ImeKeyEventHandler(@NonNull TermuxInputMethodService service) {
        mService = service;
    }

    public void reset() {
        mModifiers = Pointers.Modifiers.EMPTY;
    }

    @Override
    public void key_down(KeyValue value, boolean isSwipe) {
        if (value == null) return;
        if (value.getKind() == KeyValue.Kind.Slider) {
            handleSlider(value.getSlider(), 1);
        }
    }

    @Override
    public void key_up(KeyValue value, Pointers.Modifiers modifiers) {
        if (value == null) return;
        mModifiers = modifiers;
        InputConnection ic = mService.getCurrentInputConnection();
        if (ic == null) return;

        switch (value.getKind()) {
            case Char:
                handleChar(value.getChar(), ic, modifiers);
                break;
            case String:
                ic.commitText(value.getString(), 1);
                break;
            case Keyevent:
                handleKeyEvent(value.getKeyevent(), ic, modifiers);
                break;
            case Editing:
                handleEditing(value.getEditing(), ic);
                break;
            case Event:
                handleEvent(value.getEvent(), ic);
                break;
            case Slider:
                handleSlider(value.getSlider(), value.getSliderRepeat());
                break;
            default:
                break;
        }
    }

    @Override
    public void mods_changed(Pointers.Modifiers modifiers) {
        mModifiers = modifiers;
    }

    @Override
    public void suggestion_entered(String text) {
        InputConnection ic = mService.getCurrentInputConnection();
        if (ic != null && text != null) {
            ic.commitText(text, 1);
        }
    }

    private void handleChar(char c, InputConnection ic, Pointers.Modifiers modifiers) {
        boolean ctrl = modifiers != null && modifiers.has(KeyValue.Modifier.CTRL);
        if (ctrl) {
            switch (Character.toLowerCase(c)) {
                case 'a':
                    ic.performContextMenuAction(android.R.id.selectAll);
                    return;
                case 'c':
                    ic.performContextMenuAction(android.R.id.copy);
                    return;
                case 'v':
                    ic.performContextMenuAction(android.R.id.paste);
                    return;
                case 'x':
                    ic.performContextMenuAction(android.R.id.cut);
                    return;
                case 'z':
                    ic.performContextMenuAction(android.R.id.undo);
                    return;
                case 'y':
                    ic.performContextMenuAction(android.R.id.redo);
                    return;
            }
        }
        ic.commitText(String.valueOf(c), 1);
    }

    private void handleKeyEvent(int keyCode, InputConnection ic, Pointers.Modifiers modifiers) {
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            CharSequence selected = ic.getSelectedText(0);
            if (selected != null && selected.length() > 0) {
                ic.commitText("", 1);
            } else {
                ic.deleteSurroundingText(1, 0);
            }
            return;
        }
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            EditorInfo info = mService.getCurrentInputEditorInfo();
            boolean multiLine = false;
            int actionId = 0;
            if (info != null) {
                multiLine = (info.inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0;
                actionId = info.imeOptions & (EditorInfo.IME_MASK_ACTION | EditorInfo.IME_FLAG_NO_ENTER_ACTION);
            }
            if (!multiLine && (actionId != 0 && (actionId & EditorInfo.IME_FLAG_NO_ENTER_ACTION) == 0)) {
                mService.sendDefaultEditorAction(true);
            } else {
                ic.commitText("\n", 1);
            }
            return;
        }
        if (keyCode == KeyEvent.KEYCODE_TAB) {
            ic.commitText("\t", 1);
            return;
        }

        long now = SystemClock.uptimeMillis();
        int metaState = 0;
        if (modifiers != null) {
            if (modifiers.has(KeyValue.Modifier.SHIFT)) metaState |= KeyEvent.META_SHIFT_ON;
            if (modifiers.has(KeyValue.Modifier.CTRL)) metaState |= KeyEvent.META_CTRL_ON;
            if (modifiers.has(KeyValue.Modifier.ALT)) metaState |= KeyEvent.META_ALT_ON;
        }

        ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, metaState));
        ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0, metaState));
    }

    private void handleEditing(KeyValue.Editing editing, InputConnection ic) {
        switch (editing) {
            case COPY:
                ic.performContextMenuAction(android.R.id.copy);
                break;
            case PASTE:
            case PASTE_PLAIN:
                ic.performContextMenuAction(android.R.id.paste);
                break;
            case CUT:
                ic.performContextMenuAction(android.R.id.cut);
                break;
            case SELECT_ALL:
                ic.performContextMenuAction(android.R.id.selectAll);
                break;
            case UNDO:
                ic.performContextMenuAction(android.R.id.undo);
                break;
            case REDO:
                ic.performContextMenuAction(android.R.id.redo);
                break;
            case BACKSPACE:
                ic.deleteSurroundingText(1, 0);
                break;
            case SPACE_BAR:
                ic.commitText(" ", 1);
                break;
            case DELETE_WORD:
                deletePreviousWord(ic);
                break;
            default:
                break;
        }
    }

    private void deletePreviousWord(InputConnection ic) {
        CharSequence before = ic.getTextBeforeCursor(50, 0);
        if (before == null || before.length() == 0) {
            ic.deleteSurroundingText(1, 0);
            return;
        }
        int len = before.length();
        int i = len - 1;
        while (i >= 0 && Character.isWhitespace(before.charAt(i))) {
            i--;
        }
        while (i >= 0 && !Character.isWhitespace(before.charAt(i))) {
            i--;
        }
        int deleteCount = len - (i + 1);
        if (deleteCount > 0) {
            ic.deleteSurroundingText(deleteCount, 0);
        } else {
            ic.deleteSurroundingText(1, 0);
        }
    }

    private void handleEvent(KeyValue.Event event, InputConnection ic) {
        switch (event) {
            case ACTION:
                mService.sendDefaultEditorAction(true);
                break;
            case HIDE_SELF:
                mService.requestHideSelf(0);
                break;
            case CHANGE_METHOD_PICKER:
            case CHANGE_METHOD_PREV:
            case CHANGE_METHOD_NEXT:
                InputMethodManager imm = (InputMethodManager) mService.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showInputMethodPicker();
                }
                break;
            case CONFIG:
                Intent intent = new Intent(mService, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mService.startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void handleSlider(KeyValue.Slider slider, int count) {
        InputConnection ic = mService.getCurrentInputConnection();
        if (ic == null) return;
        int keyCode = (slider == KeyValue.Slider.LEFT) ? KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_DPAD_RIGHT;
        for (int i = 0; i < count; i++) {
            long now = SystemClock.uptimeMillis();
            ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0));
            ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0));
        }
    }
}
