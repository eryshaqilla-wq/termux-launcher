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

        // If voice typing is actively listening and user interacts with another key or toggles voice
        if (mService.isListeningVoice()) {
            mService.stopVoiceTyping();
            if (value.getKind() == KeyValue.Kind.Launcher_tool
                && value.getLauncherTool() != null
                && "voice.dictate".equalsIgnoreCase(value.getLauncherTool().toolId)) {
                return;
            }
            if (value.getKind() == KeyValue.Kind.Event
                && (value.getEvent() == KeyValue.Event.SWITCH_VOICE_TYPING
                    || value.getEvent() == KeyValue.Event.SWITCH_VOICE_TYPING_CHOOSER)) {
                return;
            }
        }

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
            case Launcher_tool:
                handleLauncherTool(value.getLauncherTool(), ic);
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
            handleBackspace(ic);
            return;
        }
        if (keyCode == KeyEvent.KEYCODE_FORWARD_DEL) {
            handleForwardDelete(ic);
            return;
        }
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            boolean shift = modifiers != null && modifiers.has(KeyValue.Modifier.SHIFT);
            if (shift) {
                ic.commitText("\n", 1);
                return;
            }

            EditorInfo info = mService.getCurrentInputEditorInfo();
            if (info == null) {
                ic.commitText("\n", 1);
                return;
            }

            boolean multiLine = (info.inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0;
            boolean noEnterAction = (info.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0;
            int imeAction = info.imeOptions & EditorInfo.IME_MASK_ACTION;
            int effectiveAction = imeAction;
            if (effectiveAction == EditorInfo.IME_ACTION_UNSPECIFIED || effectiveAction == EditorInfo.IME_ACTION_NONE) {
                if (info.actionId > 0) {
                    effectiveAction = info.actionId;
                }
            }

            if (effectiveAction != EditorInfo.IME_ACTION_UNSPECIFIED && effectiveAction != EditorInfo.IME_ACTION_NONE) {
                if (!multiLine || !noEnterAction) {
                    ic.performEditorAction(effectiveAction);
                    return;
                }
            }

            if (!multiLine) {
                long now = SystemClock.uptimeMillis();
                ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER, 0, 0));
                ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER, 0, 0));
                return;
            }

            ic.commitText("\n", 1);
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
                handleBackspace(ic);
                break;
            case SPACE_BAR:
                ic.commitText(" ", 1);
                break;
            case DELETE_WORD:
                deletePreviousWord(ic);
                break;
            case FORWARD_DELETE_WORD:
                deleteNextWord(ic);
                break;
            default:
                break;
        }
    }

    private void handleBackspace(InputConnection ic) {
        if (ic == null) return;
        if (!ic.deleteSurroundingText(1, 0)) {
            long now = SystemClock.uptimeMillis();
            ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL, 0, 0));
            ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL, 0, 0));
        }
    }

    private void handleForwardDelete(InputConnection ic) {
        if (ic == null) return;
        if (!ic.deleteSurroundingText(0, 1)) {
            long now = SystemClock.uptimeMillis();
            ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_FORWARD_DEL, 0, 0));
            ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_FORWARD_DEL, 0, 0));
        }
    }

    private void deletePreviousWord(InputConnection ic) {
        if (hasActiveSelection(ic)) {
            ic.commitText("", 1);
            return;
        }
        CharSequence before = null;
        try {
            before = ic.getTextBeforeCursor(50, 0);
        } catch (Exception ignored) {
        }
        if (before == null || before.length() == 0) {
            handleBackspace(ic);
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
            handleBackspace(ic);
        }
    }

    private void deleteNextWord(InputConnection ic) {
        if (hasActiveSelection(ic)) {
            ic.commitText("", 1);
            return;
        }
        CharSequence after = null;
        try {
            after = ic.getTextAfterCursor(50, 0);
        } catch (Exception ignored) {
        }
        if (after == null || after.length() == 0) {
            handleForwardDelete(ic);
            return;
        }
        int len = after.length();
        int i = 0;
        while (i < len && Character.isWhitespace(after.charAt(i))) {
            i++;
        }
        while (i < len && !Character.isWhitespace(after.charAt(i))) {
            i++;
        }
        int deleteCount = i;
        if (deleteCount > 0) {
            ic.deleteSurroundingText(0, deleteCount);
        } else {
            handleForwardDelete(ic);
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
            case SWITCH_VOICE_TYPING:
            case SWITCH_VOICE_TYPING_CHOOSER:
                mService.toggleVoiceTyping(event == KeyValue.Event.SWITCH_VOICE_TYPING_CHOOSER);
                break;
            default:
                break;
        }
    }

    private void handleLauncherTool(KeyValue.LauncherTool tool, InputConnection ic) {
        if (tool == null) return;
        String id = tool.toolId;
        if ("voice.dictate".equalsIgnoreCase(id)) {
            mService.toggleVoiceTyping(false);
            return;
        }
    }

    private void handleSlider(KeyValue.Slider slider, int count) {
        InputConnection ic = mService.getCurrentInputConnection();
        if (ic == null || count == 0 || slider == null) return;
        boolean reverse = count < 0;
        int keyCode;
        switch (slider) {
            case Cursor_left:
                keyCode = reverse ? KeyEvent.KEYCODE_DPAD_RIGHT : KeyEvent.KEYCODE_DPAD_LEFT;
                break;
            case Cursor_right:
                keyCode = reverse ? KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_DPAD_RIGHT;
                break;
            case Cursor_up:
                keyCode = reverse ? KeyEvent.KEYCODE_DPAD_DOWN : KeyEvent.KEYCODE_DPAD_UP;
                break;
            case Cursor_down:
                keyCode = reverse ? KeyEvent.KEYCODE_DPAD_UP : KeyEvent.KEYCODE_DPAD_DOWN;
                break;
            case Selection_cursor_left:
                keyCode = KeyEvent.KEYCODE_DPAD_LEFT;
                break;
            case Selection_cursor_right:
                keyCode = KeyEvent.KEYCODE_DPAD_RIGHT;
                break;
            default:
                keyCode = KeyEvent.KEYCODE_DPAD_RIGHT;
                break;
        }
        int repeats = Math.abs(count);
        int metaState = 0;
        if (slider == KeyValue.Slider.Selection_cursor_left || slider == KeyValue.Slider.Selection_cursor_right) {
            metaState = KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON;
        }
        for (int i = 0; i < repeats; i++) {
            long now = SystemClock.uptimeMillis();
            ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, metaState));
            ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0, metaState));
        }
    }
}
