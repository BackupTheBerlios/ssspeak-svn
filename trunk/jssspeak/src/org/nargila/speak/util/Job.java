package jssspeak.util;

public interface Job {
    public static final int STATE_INITIAL = 0;
    public static final int STATE_STOP = 1;
    public static final int STATE_PAUSE = 2;
    public static final int STATE_RUN = 3;
    public static final int STATE_DONE = 4;
    public static final int STATE_FINISH = 5;

    public void finish();
    public void abort();
	public void pause();
	public void resume();
}

