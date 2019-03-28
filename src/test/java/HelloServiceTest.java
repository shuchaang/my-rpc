import com.sc.rabbit.client.proxy.RpcProxy;
import com.sc.rabbit.service.HelloService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * @author shuchang
 * Created on  2019-03-27
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:application-consumer.xml"})
public class HelloServiceTest {


    @Resource
    private RpcProxy rpcProxy;



    @Test
    public void test(){
        HelloService proxy = rpcProxy.createProxy(HelloService.class);

        String sc = proxy.hello("sc");

        System.out.println(sc);


    }
}
