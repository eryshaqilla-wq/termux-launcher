package com.termux.app.ime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P, application = Application.class)
public class SystemNavBarHelperTest {

    private Context mContext;

    @Before
    public void setUp() {
        mContext = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void settingKeyConstantCorrect() {
        assertEquals("show_keyboard_button", SystemNavBarHelper.SETTING_KEY);
    }

    @Test
    public void readingSettingDefaultsToTrueWhenUnset() {
        boolean enabled = SystemNavBarHelper.isNavBarKeyboardButtonEnabled(mContext);
        assertTrue(enabled);
    }

    @Test
    public void readingSettingReflectsSystemSetting() {
        Settings.Secure.putInt(mContext.getContentResolver(), SystemNavBarHelper.SETTING_KEY, 0);
        assertEquals(false, SystemNavBarHelper.isNavBarKeyboardButtonEnabled(mContext));

        Settings.Secure.putInt(mContext.getContentResolver(), SystemNavBarHelper.SETTING_KEY, 1);
        assertEquals(true, SystemNavBarHelper.isNavBarKeyboardButtonEnabled(mContext));
    }
}
