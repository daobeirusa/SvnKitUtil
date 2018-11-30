package Bean;

public class SvnBean {
    private String url;
    private String name;
    private String pwd;

    public SvnBean(String url, String name, String pwd) {
        this.url = url;
        this.name = name;
        this.pwd = pwd;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
}
