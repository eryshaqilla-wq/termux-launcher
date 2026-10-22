package com.termux.app.ime;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import android.app.Application;
import android.os.Build;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ServiceController;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P, application = Application.class)
public class TermuxInputMethodServiceTest {

    private ServiceController<TermuxInputMethodService> mController;
    private TermuxInputMethodService mService;

    @Before
    public void setUp() {
        mController = Robolectric.buildService(TermuxInputMethodService.class);
        mService = mController.create().get();
    }

    @Test
    public void inputViewCreatesSuccessfully() {
        View view = mService.onCreateInputView();
        assertNotNull(view);
    }

    @Test
    public void fullscreenModeIsAlwaysDisabled() {
        assertFalse(mService.onEvaluateFullscreenMode());
    }

    @Test
    public void startAndFinishInputViewRunCleanly() {
        mService.onCreateInputView();
        EditorInfo info = new EditorInfo();
        mService.onStartInputView(info, false);
        mService.onFinishInputView(false);
    }
}
