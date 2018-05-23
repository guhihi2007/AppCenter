package cn.lt.framework.threadpool;

/**
 * Created by wenchao on 2015/10/21.
 */
public class TestAsyncTask extends LTAsyncTask<String,Integer,String> {

	
    public TestAsyncTask(QueuePriority queuePriority,
			ThreadPriority threadPriority) {
		super(QueuePriority.HIGH, ThreadPriority.HIGH);
		// TODO Auto-generated constructor stub
	}

	@Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        //进度条
        publishProgress();
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }


}
