package update;
import update.IPlatUpdateCallback;
interface IPlatUpdateService{

         void checkVersion();

         void requestNetWork();

         void registerCallback(IPlatUpdateCallback callback);

         void removeCallback(IPlatUpdateCallback callback);

 }