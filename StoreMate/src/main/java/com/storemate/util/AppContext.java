package com.storemate.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Jembatan Swing -> Spring. Jendela Swing dibuat manual dengan "new" (bukan oleh Spring),
 * sehingga View mengambil Controller yang dikelola Spring lewat kelas ini.
 */
@Component
public class AppContext implements ApplicationContextAware {
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    /** Ambil bean dari Spring. Untuk bean ber-scope prototype, setiap pemanggilan menghasilkan objek baru. */
    public static <T> T get(Class<T> type) { return context.getBean(type); }
}
