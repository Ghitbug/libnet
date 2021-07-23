package rxhttp.wrapper.param;


import com.gh.lib.net.param.PostText;

/**
 * Github
 * https://github.com/liujingxing/RxHttp
 * https://github.com/liujingxing/RxLife
 */
public class RxHttpPostText extends RxHttpJsonParam {
    public RxHttpPostText(PostText param) {
        super(param);
    }

    public RxHttpPostText setText(String text) {
        ((PostText) param).setText(text);
        return this;
    }
}
