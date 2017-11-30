package boot;

import com.sqlite.utils.ReaderManager;

public class AlertManager
{
    public static void alertAndStop(final int alertType) {
        ReaderManager.stopPrinter(800);
    }
}
