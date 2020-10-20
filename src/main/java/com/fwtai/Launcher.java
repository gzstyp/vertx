package com.fwtai;

import com.fwtai.service.IndexHandle;
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
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 异步处理方式;区别在于 router.get("/sync").handler 和 router.get("/async").blockingHandler,即同步是 handler;异步是 blockingHandler
 * @作者 田应平
 * @版本 v1.0
 * @创建时间 2020年9月17日 13:27:41
 * @QQ号码 444141300
 * @Email service@dwlai.com
 * @官网 http://www.fwtai.com
*/
public class Launcher extends AbstractVerticle {

  final InternalLogger logger = Log4JLoggerFactory.getInstance(getClass());

  //第一步,声明router,如果有重复的 path 路由的话,它匹配顺序是从上往下的,仅会执行第一个.那如何更改顺序呢？可以通过 order(x)来更改顺序,值越小越先执行!
  private Router router;

  private ToolMySQL toolMySQL;

  private ThymeleafTemplateEngine thymeleaf;//Thymeleaf模版引擎

  @Override
  public void start(final Promise<Void> startPromise) throws Exception {

    final ConfigRetriever retriever = ConfigRetriever.create(vertx);//实例化配置文件,全局

    toolMySQL = new ToolMySQL(vertx,retriever);

    thymeleaf = ThymeleafTemplateEngine.create(vertx);//实例化

    //创建HttpServer
    final HttpServer server = vertx.createHttpServer();

    //第二步,初始化|实例化 Router
    router = Router.router(vertx);

    // 处理静态资源,整合静态资源文件,前端无需写webroot目录,因为默认就是 webroot 目录下的文件,因为加了‘/static’所以在访问时需要添加前缀 /static/xx.xx
    router.route("/static/*").handler(StaticHandler.create());// http://192.168.3.108/static/robots.txt
    // 若是不加 ‘/static’也就是 /* 在访问是无需添加前缀,即 /favicon.ico 就可以访问图标 ,当然两个也可以同时写!!!,但是不推荐,会增加服务器压力???
    // router.route("/*").handler(StaticHandler.create()); //ok

    //二级路由开始
    final Router restAPI = Router.router(vertx);
    //访问方式: http://192.168.4.185/productsApi/products/485
    restAPI.get("/products/:kid").handler(context -> {
      final String kid = context.request().getParam("kid");
      ToolClient.responseSucceed(context,kid+",二级路由请求成功");
    });
    router.mountSubRouter("/productsApi",restAPI);
    //二级路由结束

    //若想要或body的参数[含表单的form-data和json格式]需要添加,可选
    router.route().handler(BodyHandler.create());//支持文件上传的目录,ctrl + p 查看

    final Set<HttpMethod> methods = new HashSet<>();
    methods.add(HttpMethod.OPTIONS);
    methods.add(HttpMethod.GET);
    methods.add(HttpMethod.POST);

    ToolLambda.getConfig(retriever).onSuccess(config ->{
      final Integer port = config.getInteger("appPort");
      final String allowedOrigin = config.getString("allowedOrigin");
      //router.route().handler(CorsHandler.create("vertx\\.io").allowedMethods(methods));//支持正则表达式
      router.route().blockingHandler(CorsHandler.create(allowedOrigin).allowedMethods(methods));//支持正则表达式
      //第三步,将router和 HttpServer 绑定[若是使用配置文件则这样实例化,如果不配置文件则把它挪动到lambda外边即可]
      server.requestHandler(router).listen(port,http -> {
        if (http.succeeded()){
          //startPromise.complete();
          logger.info("---应用启动成功---"+port);
        } else {
          //startPromise.fail(http.cause());
          logger.error("Launcher应用启动失败,"+http.cause());
        }
      });
    }).onFailure(throwable->{
      logger.error("Launcher读取配置文件失败,"+throwable.getMessage());
    });

    //第四步,配置Router解析url
    router.get("/").blockingHandler(context -> {
      ToolClient.responseJson(context,ToolClient.jsonSucceed());
    });

    router.route("/login").order(1).blockingHandler(context -> {
      ToolClient.responseJson(context,ToolClient.jsonSucceed("登录成功!"));
    });

    // http://192.168.3.108/register?username=txh&password=000000
    router.get("/register").blockingHandler((context) -> {
      final String username = context.request().getParam("username");
      final String password = context.request().getParam("password");
      final String sql = "INSERT INTO sys_user(username,`password`) VALUES (?,?)";
      final ArrayList<Object> params = new ArrayList<>();
      params.add(username);
      params.add(password);
      toolMySQL.exeSql(context,sql,params);
    });

    //获取url参数,经典模式,即url的参数 http://192.168.3.108/url?page=1&size=10
    router.route("/url").blockingHandler(context -> {
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
    router.route("/rest/:kid").blockingHandler(new UserService(toolMySQL));

    //获取url参数,restful模式,用:和url上的/对应的绑定,它和vue的:Xxx="Yy"同样的意思,注意顺序! http://192.168.3.108/restful/10/30
    router.route("/restful/:page/:size").blockingHandler(context -> {
      final String page = context.request().getParam("page");
      final String size = context.request().getParam("size");
      ToolClient.responseJson(context,ToolClient.jsonSucceed(page+",获取url参数,restful模式,"+size));
    });

    //获取body参数-->表单 multipart/form-data 格式,即请求头的 "Content-Type","application/x-www-form-urlencoded"
    router.route("/form").blockingHandler(context -> {
      final String page = context.request().getFormAttribute("page");
      final String param = context.request().getParam("page");
      ToolClient.responseJson(context,ToolClient.jsonSucceed(param + ",获取body参数-->表单form-data格式," + page));
    });

    //获取body参数-->json格式,即请求头的 "Content-Type","application/json"
    router.route("/json").blockingHandler(context -> {
      final JsonObject page = context.getBodyAsJson();
      final String json = ToolClient.createJson(200,page.toString() + "获取body参数-->json格式,"+page.encode()+",解析:"+page.getValue("page"));
      ToolClient.responseJson(context,json);
    });

    // http://127.0.0.1/controller
    router.route("/controller").blockingHandler(new IndexHandle());

    // http://127.0.0.1/client
    router.route("/client").blockingHandler(new UrlHandle(vertx));

    // http://127.0.0.1/api/sqlServer?route=map|list
    router.route("/api/sqlServer").blockingHandler(new SqlServerHandle(vertx));

    /*通配符*/ // http://127.0.0.1/api/v.1.0/role/1
    router.get("/api/v.1.0/role/*").blockingHandler(context->{
      final String query = context.request().query();
      ToolClient.responseSucceed(context,"/api/v.1.0/role/* 通配符请求方式成功" + query);
    });

    // http://127.0.0.1/api/v.1.0/user?type=1
    router.get("/api/v.1.0/user").blockingHandler(context->{
      final String query = context.request().query();//query的值是 type=1
      ToolClient.responseSucceed(context,"/api/v.1.0/user 请求方式成功" + query);
    });

    // 重定向302, http://127.0.0.1/redirect
    router.get("/redirect").blockingHandler(context->{
      context.response().setStatusCode(302).putHeader("Location","http://www.yinlz.com").end();
    });

    //退出-重定向,http://127.0.0.1/logout
    router.get("/logout").handler(context -> {
      context.clearUser();
      context.response().setStatusCode(302).putHeader("Location","/").end();
    });

    // 前端模版引擎用法,http://127.0.0.1/thymeleaf
    router.route("/thymeleaf").blockingHandler(context->{
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
    router.route("/thymeleaf2").blockingHandler(new TemplateService(vertx));
  }
}