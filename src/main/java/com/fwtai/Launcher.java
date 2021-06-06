package com.fwtai;

import com.fwtai.service.IndexHandle;
import com.fwtai.service.ProductHandle;
import com.fwtai.service.SqlServerHandle;
import com.fwtai.service.TemplateService;
import com.fwtai.service.UrlHandle;
import com.fwtai.service.UserService;
import com.fwtai.tool.ToolClient;
import com.fwtai.tool.ToolLambda;
import com.fwtai.tool.ToolMySQL;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.Log4JLoggerFactory;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 异步处理方式;区别在于 router.get("/sync").handler 和 router.get("/async").blockingHandler,即异步是 handler;同步(阻塞)是 blockingHandler
 * @注意 需要处理异常报错,todo 一直在转圈圈说明没有返回值
 * @作者 田应平
 * @版本 v1.0
 * @创建时间 2020年9月17日 13:27:41
 * @QQ号码 444141300
 * @Email service@dwlai.com
 * @官网 http://www.fwtai.com
 * 使用vertx共享数据:https://www.cnblogs.com/endv/p/12814470.html
*/
public final class Launcher extends AbstractVerticle {

  final InternalLogger logger = Log4JLoggerFactory.getInstance(getClass());

  private final String address = "com.fwtai.app.address";

  //第一步,声明router,如果有重复的 path 路由的话,它匹配顺序是从上往下的,仅会执行第一个.那如何更改顺序呢？可以通过 order(x)来更改顺序,值越小越先执行!
  private Router router;

  private ToolMySQL toolMySQL;

  private ThymeleafTemplateEngine thymeleaf;//Thymeleaf模版引擎

  @Override
  public void start(final Promise<Void> startPromise) throws Exception {

    final ConfigRetriever retriever = ConfigRetriever.create(vertx);//实例化配置文件,配置文件默认的路径 resources/config.json;当然也可以把配置文件config.json放在jar文件同一个目录

    toolMySQL = new ToolMySQL(vertx,retriever);

    thymeleaf = ThymeleafTemplateEngine.create(vertx);//实例化

    final List<HttpVersion> alpns = Arrays.asList(HttpVersion.HTTP_1_1,HttpVersion.HTTP_2);
    final HttpServerOptions options = new HttpServerOptions();
    options.setAlpnVersions(alpns);

    //创建HttpServer
    final HttpServer server = vertx.createHttpServer(options);

    //第二步,初始化|实例化 Router,若要添加跨域请求的话,随着就配置跨域
    router = Router.router(vertx);
    final Set<HttpMethod> methods = new HashSet<>();
    methods.add(HttpMethod.OPTIONS);
    methods.add(HttpMethod.GET);
    methods.add(HttpMethod.POST);

    //router.route().handler(CorsHandler.create("vertx\\.io").allowedMethods(methods));支持 "*.fwtai.com" ;支持正则表达式,此处只能用 handler,不能使用 blockingHandler,否则会报Internal Server Error错!!!
    router.route().handler(CorsHandler.create("http://192.168.3.108").allowCredentials(true).allowedHeader("content-type").maxAgeSeconds(86400).allowedMethods(methods));

    // 处理静态资源,整合静态资源文件,前端无需写webroot目录,因为默认就是 webroot 目录下的文件,因为加了‘/static’所以在访问时需要添加前缀 /static/xx.xx
    router.route("/static/*").handler(StaticHandler.create());// http://192.168.3.108/static/robots.txt
    // 若是不加 ‘/static’也就是 /* 在访问是无需添加前缀,即 /favicon.ico 就可以访问图标 ,当然两个也可以同时写!!!,但是不推荐,会增加服务器压力???
    // router.route("/*").handler(StaticHandler.create()); //ok,访问方式 /favicon.ico

    //若有表单提交数据,此项必须,若想要或body的参数[含表单的form-data和json格式]需要添加,此处只能用 handler,不能使用 blockingHandler,否则会报Internal Server Error错!!!
    router.route().handler(BodyHandler.create());//支持文件上传的目录,ctrl + p 查看,BodyHandler.create()支持文件上传

    /*
    final SessionStore session1 = LocalSessionStore.create(vertx);//ok,当然也可以使用下面的方式创建!!!
    final SessionStore session2 = ClusteredSessionStore.create(vertx);//ok
    router.route().handler(LoggerHandler.create());
    //Session
    router.route().handler(SessionHandler.create(session1));// BodyHandler.create(),支持文件上传!!!
    router.route().handler(CorsHandler.create("127.0.0.1"));
    router.route().handler(CSRFHandler.create("RjF9vTHCS2yr0zX3D50CKRiarMX+0qOpHAfcu24gWZ9bL39s48euPQniE2RhGx"));//自定义参数,低版本只有1个参数，高版本有2个参数
    */

    //二级路由开始
    final Router productApi = Router.router(vertx);
    //访问方式: http://192.168.3.108/product/add/1024
    productApi.get("/add/:kid").handler(context -> {
      final String kid = context.request().getParam("kid");
      ToolClient.responseSucceed(context,kid+",二级路由请求成功-add方法");
    });
    //访问方式: http://192.168.3.108/product/edit/1024
    productApi.get("/edit/:kid").handler(context -> {
      final String kid = context.request().getParam("kid");
      ToolClient.responseSucceed(context,kid+",二级路由请求成功-edit方法");
    });
    //访问方式: http://192.168.3.108/product/list/10/1
    productApi.get("/list/:size/:page").handler(new ProductHandle(vertx));
    router.mountSubRouter("/product",productApi);
    //二级路由结束

    //第三步,配置Router解析url
    router.get("/").handler(context -> {
      logger.info("操作成功");
      logger.error("error,仅把error信息写入到日志文档!");
      logger.debug("debug!");
      ToolClient.responseJson(context,ToolClient.jsonSucceed());
    });

    router.route("/login").order(1).handler(context -> {
      ToolClient.responseJson(context,ToolClient.jsonSucceed("登录成功!"));
    });

    // http://192.168.3.108/register?username=txh&password=000000
    router.get("/register").handler((context) -> {
      final String username = context.request().getParam("username");
      final String password = context.request().getParam("password");
      final String sql = "INSERT INTO sys_user(username,`password`) VALUES (?,?)";
      final ArrayList<Object> params = new ArrayList<>();
      params.add(username);
      params.add(password);
      toolMySQL.exeSql(context,sql,params);
    });

    //获取url参数,经典模式,即url的参数 http://192.168.3.108/url?page=1&size=10
    router.route("/url").handler(context -> {
      final String page = context.request().getParam("page");
      final String size = context.request().getParam("size");
      final List<Object> params = new ArrayList<>();
      params.add("11");
      final List<Object> result = toolMySQL.pageParams(Integer.parseInt(page),Integer.parseInt(size));
      params.addAll(result);//注意添加顺序!!!
      final String sql = "SELECT kid,username,password FROM sys_user where kid = ? limit ?,?";
      toolMySQL.queryList(context,sql,params);
      logger.info("请求url为/url,获取数据列表");
    });

    // http://192.168.3.108/rest/1
    router.route("/rest/:kid").handler(new UserService(toolMySQL));

    //获取url参数,restful模式,用:和url上的/对应的绑定,它和vue的:Xxx="Yy"同样的意思,注意顺序! http://192.168.3.108/restful/10/30
    router.route("/restful/:page/:size").handler(context -> {
      final String page = context.request().getParam("page");
      final String size = context.request().getParam("size");
      ToolClient.responseJson(context,ToolClient.jsonSucceed(page+",获取url参数,restful模式,"+size));
    });

    //获取body参数-->表单 multipart/form-data 格式,即请求头的 "Content-Type","application/x-www-form-urlencoded"
    router.route("/form").handler(context -> {
      final String page = context.request().getFormAttribute("page");
      final String param = context.request().getParam("page");
      ToolClient.responseJson(context,ToolClient.jsonSucceed(param + ",获取body参数-->表单form-data格式," + page));
    });

    //获取body参数-->json格式,即请求头的 "Content-Type","application/json"
    router.route("/json").handler(context -> {
      final JsonObject page = context.getBodyAsJson();
      final String json = ToolClient.createJson(200,page.toString() + "获取body参数-->json格式,"+page.encode()+",解析:"+page.getValue("page"));
      ToolClient.responseJson(context,json);
    });

    // http://127.0.0.1/controller
    router.route("/controller").handler(new IndexHandle());

    // http://127.0.0.1/client
    router.route("/client").handler(new UrlHandle(vertx));

    // http://127.0.0.1/api/sqlServer?route=map|list
    router.route("/api/sqlServer").handler(new SqlServerHandle(vertx));

    /*通配符*/ // http://127.0.0.1/api/v.1.0/role/1
    router.get("/api/v.1.0/role/*").handler(context->{
      final String query = context.request().query();
      ToolClient.responseSucceed(context,"/api/v.1.0/role/* 通配符请求方式成功" + query);
    });

    // http://127.0.0.1/api/v.1.0/user?type=1
    router.get("/api/v.1.0/user").handler(context->{
      final String query = context.request().query();//query的值是 type=1
      ToolClient.responseSucceed(context,"/api/v.1.0/user 请求方式成功" + query);
    });

    // 重定向302,应用场景不同, http://127.0.0.1/redirect
    router.get("/redirect").handler(context->{
      context.response().setStatusCode(302).putHeader("Location","http://www.yinlz.com").end();
    });

    //退出-重定向,应用场景不同,http://127.0.0.1/logout
    router.get("/logout").handler(context -> {
      context.clearUser();
      context.response().setStatusCode(302).putHeader("Location","/").end();
    });

    // 前端模版引擎用法,http://127.0.0.1/thymeleaf
    router.route("/thymeleaf").handler(context->{
      final JsonObject json = new JsonObject();
      json.put("name","田应平,从后端返回数据");
      thymeleaf.render(json,"templates/index.html",bufferAsyncResult ->{
        if(bufferAsyncResult.succeeded()){
          context.response().putHeader("content-type","text/html;charset=utf-8").end(bufferAsyncResult.result());
        }else{
          ToolClient.responseSucceed(context,"加载页面失败");
        }
      });
    });

    // 前端模版引擎用法,http://127.0.0.1/thymeleaf2
    router.route("/thymeleaf2").handler(new TemplateService(vertx));

    //全局异常处理,放在最后一个route
    router.route().last().handler(context -> {
      ToolClient.responseJson(context,ToolClient.createJson(404,"访问的url不存在"));
    }).failureHandler(context -> {
      ToolClient.responseJson(context,ToolClient.createJson(204,"操作失败,系统出现错误"));
    });

    //获取请求头,获取token
    router.route().handler(context ->{
      final String accessToken = context.request().getHeader("accessToken");
      if("myToken".equals(accessToken)){
        context.next();//继续
      }else{
        ToolClient.responseJson(context,ToolClient.jsonPermission());
      }
    });

    //************************只能写在最后面,否则路由会访问不到,可能会导致出现 Internal Server Error ************************/
    ToolLambda.getConfig(retriever).onSuccess(config ->{
      final Integer port = config.getInteger("appPort");
      //第四步,将router和 HttpServer 绑定[若是使用配置文件则这样实例化,如果不配置文件则把它挪动到lambda外边即可]
      server.requestHandler(router).listen(port,http -> {
        if (http.succeeded()){
          startPromise.complete();
          logger.info("---应用启动成功---,http://127.0.0.1:"+port);
        } else {
          //startPromise.fail(http.cause());
          logger.error("Launcher应用启动失败,"+http.cause());
        }
      });
    }).onFailure(throwable->{
      logger.error("Launcher读取配置文件失败,"+throwable.getMessage());
    });

    vertx.eventBus().consumer(address,message->{
      System.out.println("message:"+message.body());
    });

    vertx.setTimer(5000,handler->{
      sendEvent();
    });
  }

  protected void sendEvent(){
    vertx.eventBus().send(address,"发送消息");
  }

  //封装了重定向,调用方式:handler(this::redirect);
  protected void redirect(final RoutingContext context){
    context.response().setStatusCode(302).putHeader("Location","http://www.yinlz.com").end();
  }
}