import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.Proxy

driver = {
    DesiredCapabilities capabilities = DesiredCapabilities.firefox();
    Proxy proxy = new Proxy()
    proxy.setProxyType(Proxy.ProxyType.DIRECT)
    capabilities.setCapability("proxy", proxy)
    def driver = new FirefoxDriver(capabilities);
}

reportsDir = "build/geb-reports"
