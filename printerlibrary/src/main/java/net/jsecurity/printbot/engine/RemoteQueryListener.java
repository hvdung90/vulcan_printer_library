package net.jsecurity.printbot.engine;

import java.util.List;
import net.jsecurity.printbot.engine.RemoteQueryTask;

public interface RemoteQueryListener {
    void onReturnFromRemoteQuery(RemoteQueryTask.QueryMode queryMode, List<String> list);
}
