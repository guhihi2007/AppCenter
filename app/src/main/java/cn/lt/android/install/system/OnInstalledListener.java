package cn.lt.android.install.system;

public interface OnInstalledListener {
	
	public void packageInstalled(String packageName, int returnCode);

}
