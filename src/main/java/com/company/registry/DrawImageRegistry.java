package com.company.registry;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.http.javadsl.model.StatusCode;
import akka.http.javadsl.model.StatusCodes;
import com.company.common.AppConfig;
import com.company.service.DrawService;
import com.company.util.FileUtil;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;


public class DrawImageRegistry extends AbstractBehavior<DrawImageRegistry.Request> {
    private static final String FOLDER = AppConfig.config.getString("storage.folder");
    public DrawImageRegistry(ActorContext<Request> context) {
        super(context);
    }

    public static Behavior<Request> create() {
        return Behaviors.setup(DrawImageRegistry::new);
    }

    @Override
    public Receive<Request> createReceive() {
        return newReceiveBuilder()
                .onMessage(DrawRequest.class, this::draw)
                .build();
    }

    private Behavior<Request> draw(DrawRequest drawRequest) {
        try {
            String strJsonData = drawRequest.jsonData;
            BufferedImage bufferedImage = DrawService.INSTANCE.drawImg(strJsonData);

            JSONObject jsonData = new JSONObject(strJsonData);
            String originalNameFile  = jsonData.optString("name", "default");
            String fileName = FileUtil.genFileName(originalNameFile);
//            File file = new File(FOLDER + "/" + fileName + ".png");
//            ImageIO.write(bufferedImage, "png", file);
            String link = drawRequest.domain + "/static/upload/" + fileName + ".png";
            drawRequest.replyTo.tell(new DrawResponse(link, StatusCodes.OK));
        }catch (Exception e) {
            e.printStackTrace();
            drawRequest.replyTo.tell(new DrawResponse(null, StatusCodes.EXPECTATION_FAILED));
        }
        return Behaviors.same();
    }

    public interface Request {
    }

    public static class DrawRequest implements Request {
        private String domain;
        public final String jsonData;
        public final ActorRef<DrawResponse> replyTo;

        public DrawRequest(String domain, String jsonData, ActorRef<DrawResponse> replyTo) {
            this.domain = domain;
            this.jsonData = jsonData;
            this.replyTo = replyTo;
        }
    }

    public interface Response {
    }

    public static class DrawResponse implements Response {
        public final String link;
        public final StatusCode status;

        public DrawResponse(String link, StatusCode status) {
            this.link = link;
            this.status = status;
        }
    }


}
