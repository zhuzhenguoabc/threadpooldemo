package com.example.threadpooldemo;

import android.app.Application;
import com.hprt.lib_ft800.FTHelper;
import com.hprt.lib_ft800.FTUSBHelper;
import com.hprt.lib_pdf.image.ProcessHelper;

public class MyApp extends Application {
    private static final String qudao_code = "a+Ubi0NBzm49FyzR8kFMo//arTb1odK2EjPx5kKys/qjkx6zF9pVjR8t4w2n3QY3DyBVCS0qHSm51aNNR+u8kpQz4KZFHL2bxU5hcEF8TCXCefT7SLUzuEsr66P2BkZa6yhPpW45MzQyS4gVh/OHdCnaa7oPNlQvpM8igTyi1Fg=";
    private static final String hprt_code = "ncZ9taQbVHgplPF8jmfGkwdPuz1eGBk7CrjBGXYhxlFKXi+F06PrSeRo9k/7sn3WxHslt5H8e/FlfLJCh3V10zOM7oECZa+nrdDTTa9ijCl6b/jQEcfjIlKNtsE1NRqBuNA7Df90qhFPRu6qjPpnNpXmknHwcwOJSMla5hNSKT8=";

    @Override
    public void onCreate() {
        super.onCreate();

        FTHelper.INSTANCE.init(getBaseContext(), hprt_code);
        FTUSBHelper.INSTANCE.init(getBaseContext(), hprt_code);
        ProcessHelper.INSTANCE.init(this);
    }
}